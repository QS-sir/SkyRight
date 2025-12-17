package com.lizi.skyright;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.RemoteException;
import com.lizi.skyright.service.ISystemServerManager;
import de.robv.android.xposed.XposedBridge;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.pm.PackageManager.NameNotFoundException;

public class SystemServerManagerImpl extends ISystemServerManager.Stub {

    public static final String TAG = "SystemServerManagerImpl";
    private HookRegistry hookRegistry;
    private PackageManager pm;
    private ActivityManager am;
    private File file;
    private DataUpdateCallback dataUpdateCallback;
    private JSONObject json;
    private JSONArray monitorPackagesActivity;
    private JSONObject modifyStartActivityList;
    private JSONObject monitorActivityList;
	private JSONArray packageHideAccessibilityList;
    private JSONObject sundriesData;
    private JSONObject expandHookPackages;

    public SystemServerManagerImpl(HookRegistry hookRegistry, DataUpdateCallback dataUpdateCallback) {
        this.hookRegistry = hookRegistry;
        this.dataUpdateCallback = dataUpdateCallback;
        Context context = hookRegistry.getContext();
        this.pm = context.getPackageManager();
        this.am = context.getSystemService(ActivityManager.class);
        this.file = new File("/data/system/skyright_data.json");
        initData();
    }

    private void initData() {
        try {
            String data = readData();
            json = new JSONObject(data);
            String value1 = "monitor_packages_activity";
            String value2 = "modify_start_activity_list";
            String value3 = "monitor_activity_list";
            String value4 = "package_hide_accessibility_list";
            String value5 = "sundries_data";
            String value6 = "expand_hook_packages";
            if (json.has(value1)) {
                monitorPackagesActivity = json.getJSONArray(value1);
            } else {
                json.put(value1, new JSONArray());
                monitorPackagesActivity = json.getJSONArray(value1);
            }
            if (json.has(value2)) {
                modifyStartActivityList = json.getJSONObject(value2);
            } else {
                json.put(value2, new JSONObject());
                modifyStartActivityList = json.getJSONObject(value2);
            }

            if (json.has(value3)) {
                monitorActivityList = json.getJSONObject(value3);
            } else {
                json.put(value3, new JSONObject());
                monitorActivityList = json.getJSONObject(value3);
            }
            if (json.has(value4)) {
                packageHideAccessibilityList = json.getJSONArray(value4);
            } else {
                json.put(value4, new JSONArray());
                packageHideAccessibilityList = json.getJSONArray(value4);
            }
            if (json.has(value5)) {
                sundriesData = json.getJSONObject(value5);
            } else {
                json.put(value5, new JSONObject());
                sundriesData = json.getJSONObject(value5);
            }
            if(json.has(value6)){
                expandHookPackages = json.getJSONObject(value6);
            }else{
                json.put(value6,new JSONObject());
                expandHookPackages = json.getJSONObject(value6);
            }
            dataUpdateCallback.updateAllData(json.toString());
        } catch (JSONException e) {
            XposedBridge.log(TAG + "  initData  error:" + e.toString());
        }
    }

    private String readData() {
        InputStreamReader isr = null;
        BufferedReader read = null;
        StringBuilder str = new StringBuilder();
        if (!file.exists()) {
            return "{}";
        }
        try {
            isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            read = new BufferedReader(isr);
            char[] buff = new char[1024];
            int byteRead = 0;
            while ((byteRead = read.read(buff)) != -1) {
                str.append(new String(buff, 0, byteRead));
            }
            isr.close();
            read.close();
            isr = null;
            read = null;
            return str.toString();
        } catch (IOException e) {
            return "{}";
        }
    }

    @Override
    public void setEnabledHookPackage(String packageName, boolean enable) throws RemoteException {
        long origId = Binder.clearCallingIdentity();
        try {
            expandHookPackages.put(packageName, enable);
            dataUpdateCallback.setEnabledHookPackage(packageName,enable);
        } catch (JSONException e) {
            LogManager.log(TAG, " setEnabledHookPackage JSONException error:" + e.toString());
        }finally {
            Binder.restoreCallingIdentity(origId);
        }
        try {
            writeFile();
        } catch (Exception e) {
            LogManager.log(TAG, "setEnabledHookPackage Exception error: " + e.toString());
		}
    }
    
    public void checkExpandPackageIsUnInstall(String packageName){
        if(expandHookPackages.has(packageName)){
            expandHookPackages.remove(packageName);
            try {
                writeFile();
            } catch (Exception e) {
                LogManager.log(TAG, "setEnabledHookPackage Exception error: " + e.toString());
            }
        }
    }

    @Override
    public boolean isDynamicHook() throws RemoteException {
        return hookRegistry.isDynamic();
    }
    

    @Override
    public boolean isEnabledHookPackage(String packagekName) throws RemoteException {
        try {
            if (expandHookPackages.has(packagekName)) {
                return expandHookPackages.getBoolean(packagekName);
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "isEnabledHookPackage JSONException error: " + e.toString());
		}
        return false;
    }
    
    private void pauseExpandHook(){
        Iterator<String> keys = expandHookPackages.keys();
        while(keys.hasNext()){
            String k = keys.next();
            try {
                expandHookPackages.put(k,false);
            } catch (JSONException e) {
                LogManager.log(TAG, " pauseExpandHook JSONException error:" + e.toString());
            }
        }
    }

    @Override
    public void setPauseAllHook(boolean b) throws RemoteException {
        long origId = Binder.clearCallingIdentity();
        try {
            sundriesData.put("pause_hooks", b);
            dataUpdateCallback.setPauseAllHook(b);
            if(b){
                pauseExpandHook();
            }
        } catch (JSONException e) {
            LogManager.log(TAG, " setPauseAllHook JSONException error:" + e.toString());
        }finally {
            Binder.restoreCallingIdentity(origId);
        }
        try {
            writeFile();
        } catch (Exception e) {
            LogManager.log(TAG, "setPauseAllHook Exception error: " + e.toString());
		}
    }

    @Override
    public boolean isPauseAllHook() throws RemoteException {
        try {
            if (sundriesData.has("pause_hooks")) {
                return sundriesData.getBoolean("pause_hooks");
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "isPauseAllHook JSONException error: " + e.toString());
		}
        return false;
    }

    @Override
    public String getStorageData() throws RemoteException {
        return null;
    }

    @Override
    public boolean getOneplusHideRootStatus() throws RemoteException {
        try {
            if (sundriesData.has("Oneplus_hide_root")) {
                return sundriesData.getBoolean("Oneplus_hide_root");
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "getOneplusHideRootStatus JSONException error: " + e.toString());
		}
        return false;
    }

    @Override
    public void setOneplusHideRootStatus(boolean b) throws RemoteException {
        try {
            sundriesData.put("Oneplus_hide_root", b);
            dataUpdateCallback.setOneplusHideRootStatus(b);
        } catch (JSONException e) {
            LogManager.log(TAG, " setOneplusHideRootStatus JSONException error:" + e.toString());
        }
        try {
            writeFile();
        } catch (Exception e) {
            LogManager.log(TAG, "setOneplusHideRootStatus Exception error: " + e.toString());
		}
    }

    private void writeFile()throws Exception {
        if (json == null) {
            return;
        }
        String data = json.toString(2);
        FileOutputStream outStream = new FileOutputStream(file);
        outStream.write(data.getBytes());
        outStream.close();
	}

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) throws RemoteException {
        long origId = Binder.clearCallingIdentity();
        try {
            return pm.getInstalledApplications(flags);
        } finally {
            Binder.restoreCallingIdentity(origId);
		}
    }

    @Override
    public List<String> getPackageNames() throws RemoteException {
        long origId = Binder.clearCallingIdentity();
        List<String> list = new ArrayList<>();
        List<ApplicationInfo> appList = pm.getInstalledApplications(0);
        int l = appList.size();
        for (int i = 0; i < l; i++) {
            list.add(appList.get(i).packageName);
        }
        try {
            return list;
        } finally {
            Binder.restoreCallingIdentity(origId);
		}
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws RemoteException {
        long origId = Binder.clearCallingIdentity();
        try {
            return pm.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            LogManager.log(TAG, "getPackageInfo error: " + e.toString());
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
		return new PackageInfo();
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws RemoteException {
        long origId = Binder.clearCallingIdentity();
        try {
            return pm.getApplicationInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            LogManager.log(TAG, "getApplicationInfo error: " + e.toString());
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
		return new ApplicationInfo();
    }

}
