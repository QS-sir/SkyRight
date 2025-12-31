package com.lizi.skyright;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.os.UserManager;

public class SystemServerManagerImpl extends ISystemServerManager.Stub {

    public static final String TAG = "SystemServerManagerImpl";
    public static final String MONITOR_PACKAGES_ACTIVITY = "monitor_packages_activity";
    public static final String MODIFY_PACKAGES_START_ACTIVITY = "modify_packages_start_activity";
    public static final String MONITOR_ACTIVITYS = "monitor_activitys";
    public static final String PACKAGES_HIDE_ACCESSIBILITY = "packages_hide_accessibility";
    public static final String SUNDRIES_DATA = "sundries_data";
    public static final String EXPAND_HOOK_PACKAGES = "expand_hook_packages";
    public static final String WHITE_LIST_PACKAGES = "white_list_packages";
    public static final String MONITOR_PACKAGE_SUBWINDOW = "monitor_packages_subwindow";
    public static final String MONITOR_ACTIVITY_SUBWINDOW = "monitor_activity_subwindow";

    private HookRegistry hookRegistry;
    private PackageManager pm;
    private ActivityManager am;
    private UserManager userManager;
    private File file;
    private MethodHookInit methodHookInit;
    private JSONObject json,modifyPackagesStartActivity,monitorActivity,sundriesData,
    expandHookPackages,monitorActivitySubWindow;
    private JSONArray monitorPackagesActivityBehaviour,whiteListPackages,packagesHideAccessibility,
    monitorPackagesSubWindow;

    public SystemServerManagerImpl(MethodHookInit methodHookInit) {
        this.methodHookInit = methodHookInit;
        this.hookRegistry = methodHookInit.getHookRegistry();
        Context context = hookRegistry.getContext();
        this.pm = context.getPackageManager();
        this.am = context.getSystemService(ActivityManager.class);
        this.userManager = context.getSystemService(UserManager.class);
        this.file = new File("/data/system/skyright_data.json");
        initData();
    }

    private void initData() {
        try {
            String data = readData();
            json = new JSONObject(data);
            initDistribute();
        } catch (JSONException e) {
            XposedBridge.log(TAG + "  initData  error:" + e.toString());
        }
    }

    private void initDistribute() throws JSONException {
        monitorPackagesActivityBehaviour = getJsonArray(MONITOR_PACKAGES_ACTIVITY);
        modifyPackagesStartActivity = getJsonObject(MODIFY_PACKAGES_START_ACTIVITY);
        monitorActivity = getJsonObject(MONITOR_ACTIVITYS);
        packagesHideAccessibility = getJsonArray(PACKAGES_HIDE_ACCESSIBILITY);
        sundriesData = getJsonObject(SUNDRIES_DATA);
        expandHookPackages = getJsonObject(EXPAND_HOOK_PACKAGES);
        whiteListPackages = getJsonArray(WHITE_LIST_PACKAGES);
        monitorPackagesSubWindow = getJsonArray(MONITOR_PACKAGE_SUBWINDOW);
        monitorActivitySubWindow = getJsonObject(MONITOR_ACTIVITY_SUBWINDOW);
        methodHookInit.updateData(null, json.toString());
    }

    private JSONArray getJsonArray(String keyName) throws JSONException {
        if (json.has(keyName)) {
            return json.getJSONArray(keyName);
        } 
        JSONArray newArray = new JSONArray();
        json.put(keyName, newArray);
        return newArray; 
    }

    private JSONObject getJsonObject(String keyName)throws JSONException {
        if (json.has(keyName)) {
            return json.getJSONObject(keyName);
        } 
        JSONObject jsonObject = new JSONObject();
        json.put(keyName, jsonObject);
        return jsonObject;
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
    public void setRemoveWindowSecureFlags(boolean b) throws RemoteException {
        if (isPauseAllHook()) {
            return;
        }
        try {
            sundriesData.put("remove_window_secure_flags", b);
            methodHookInit.setRemoveWindowSecureFlags(b);
        } catch (JSONException e) {
            LogManager.log(TAG, " setRemoveWindowSecureFlags JSONException error:" + e.toString());
        }
        try {
            writeFile();
        } catch (Exception e) {
            LogManager.log(TAG, "setRemoveWindowSecureFlags Exception error: " + e.toString());
		}
    }

    @Override
    public boolean isRemoveWindowSecureFlags() throws RemoteException {
        if ((!userManager.isUserUnlocked() && isEnabledRebootProtect()) || isPauseAllHook()) {
            return false;
        }
        try {
            if (sundriesData.has("remove_window_secure_flags")) {
                return sundriesData.getBoolean("remove_window_secure_flags");
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "isRemoveWindowSecureFlags JSONException error: " + e.toString());
		}
        return false;
    }


    @Override
    public boolean isEnabledRebootProtect() throws RemoteException {
        try {
            if (sundriesData.has("reboot_protect")) {
                return sundriesData.getBoolean("reboot_protect");
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "isEnabledRebootProtect JSONException error: " + e.toString());
		}
        return true;
    }

    @Override
    public void setEnabledRebootProtect(boolean b) throws RemoteException {
        try {
            sundriesData.put("reboot_protect", b);
        } catch (JSONException e) {
            LogManager.log(TAG, " setEnabledRebootProtect JSONException error:" + e.toString());
        }
        try {
            writeFile();
        } catch (Exception e) {
            LogManager.log(TAG, "setPauseAllHook Exception error: " + e.toString());
		}
    }

    @Override
    public void setPackageWhiteList(String packageName, boolean b) throws RemoteException {
        if (b) {
            whiteListPackages.put(packageName);
        } else {
            int size = whiteListPackages.length() - 1;
            for (int i = size ; i >= 0; i--) {
                String pkg = whiteListPackages.optString(i, "");
                if (pkg.equals(packageName)) {
                    whiteListPackages.remove(i);
                    break;
                }
            }
        }
        methodHookInit.updateData(WHITE_LIST_PACKAGES, json.toString());
        try {
            writeFile();
        } catch (Exception e) {
            LogManager.log(TAG, "setPackageWhiteList Exception error: " + e.toString());
		}
    }

    @Override
    public List<String> getServiceLoges() throws RemoteException {
        return LogManager.getAllLogs();
    }

    @Override
    public void setMonitorPackageActivity(String packageName, boolean b) throws RemoteException {
        if (b) {
            monitorPackagesActivityBehaviour.put(packageName);
        } else {
            int size = monitorPackagesActivityBehaviour.length() - 1;
            for (int i = size ; i >= 0; i--) {
                String pkg = monitorPackagesActivityBehaviour.optString(i);
                if (pkg.equals(packageName)) {
                    monitorPackagesActivityBehaviour.remove(i);
                    break;
                }
            }
        }
        methodHookInit.updateData(MONITOR_PACKAGES_ACTIVITY, json.toString());
        try {
            writeFile();
        } catch (Exception e) {
            LogManager.log(TAG, "setMonitorPackageActivity Exception error: " + e.toString());
		}
    }

    @Override
    public void setMonitorActivity(String packageName, String activityName, String action) throws RemoteException {
        if (action != null && action.equals(ActivityRequestDialog.REQUEST_IGNORE)) {
            if (monitorActivity.has(packageName)) {
                JSONObject list = monitorActivity.optJSONObject(packageName);
                if (list.has(activityName)) {
                    list.remove(activityName);
                    if (list.length() == 0) {
                        monitorActivity.remove(packageName);
                    }
                }
            }
        } else {
            try {
                if (monitorActivity.has(packageName)) {
                    JSONObject list = monitorActivity.optJSONObject(packageName);
                    list.put(activityName, action);
                } else {
                    JSONObject list = new JSONObject();
                    list.put(activityName, action);
                    monitorActivity.put(packageName, list);
                }
            } catch (JSONException e) {
                LogManager.log(TAG, "setMonitorActivity JSONException error: " + e.toString());
            }
        }
        methodHookInit.updateData(MONITOR_ACTIVITYS, json.toString());
        try {
            writeFile();
        } catch (Exception e) {
            LogManager.log(TAG, "setMonitorActivity Exception error: " + e.toString());
		}
    }

    @Override
    public void setModifyPackageStartActivity(String packageName, String activityName) throws RemoteException {
        try {
            if (activityName != null && !activityName.isEmpty()) {
                modifyPackagesStartActivity.put(packageName, activityName);
                methodHookInit.updateData(MODIFY_PACKAGES_START_ACTIVITY, json.toString());
                writeFile();
            } else if (modifyPackagesStartActivity.has(packageName)) {
                modifyPackagesStartActivity.remove(packageName);
                methodHookInit.updateData(MODIFY_PACKAGES_START_ACTIVITY, json.toString());
                writeFile();
            }
        } catch (Exception e) {
            LogManager.log(TAG, "setModifyPackageStartActivity Exception error: " + e.toString());
        }
    }


    @Override
    public void setPackageHideAccessibilityStatus(String packageName, boolean b) throws RemoteException {
        if (b) {
            packagesHideAccessibility.put(packageName);
        } else {
            int size = packagesHideAccessibility.length();
            for (int i = 0; i < size; i++) {
                String pkg = packagesHideAccessibility.optString(i);
                if (pkg.equals(packageName)) {
                    packagesHideAccessibility.remove(i);
                    break; 
                }
            }
        }
        methodHookInit.updateData(PACKAGES_HIDE_ACCESSIBILITY, json.toString());
        try {
            writeFile();
        } catch (Exception e) {
            LogManager.log(TAG, "setPackageHideAccessibilityList Exception error: " + e.toString());
		}
    }

    @Override
    public int getParcelSize() throws RemoteException {
        return 0;
    }


    @Override
    public void setEnabledHookPackage(String packageName, boolean enable) throws RemoteException {
        long origId = Binder.clearCallingIdentity();
        try {
            expandHookPackages.put(packageName, enable);
            methodHookInit.setEnabledHookPackage(packageName, enable);
        } catch (JSONException e) {
            LogManager.log(TAG, " setEnabledHookPackage JSONException error:" + e.toString());
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
        try {
            writeFile();
        } catch (Exception e) {
            LogManager.log(TAG, "setEnabledHookPackage Exception error: " + e.toString());
		}
    }

    public void checkExpandPackageIsUnInstall(String packageName) {
        if (expandHookPackages.has(packageName)) {
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

    private void pauseExpandHook() {
        Iterator<String> keys = expandHookPackages.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            try {
                expandHookPackages.put(k, false);
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
            methodHookInit.setPauseAllHook(b);
            if (b) {
                pauseExpandHook();
            }
        } catch (JSONException e) {
            LogManager.log(TAG, " setPauseAllHook JSONException error:" + e.toString());
        } finally {
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
            if (!userManager.isUserUnlocked() && isEnabledRebootProtect()) {
                sundriesData.put("pause_hooks", true);
                return true;
            }
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
        return json.toString();
    }

    @Override
    public boolean getOneplusHideRootStatus() throws RemoteException {
        if ((!userManager.isUserUnlocked() && isEnabledRebootProtect()) || isPauseAllHook()) {
            return false;
        }
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
        if (isPauseAllHook()) {
            return;
        }
        try {
            sundriesData.put("Oneplus_hide_root", b);
            methodHookInit.setOneplusHideRootStatus(b);
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
            PackageInfo p = pm.getPackageInfo(packageName, flags);
            return p;
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

    @Override
    public String getPackageLaunchActivityName(String packageName) throws RemoteException {
        long origId = Binder.clearCallingIdentity();
        try {
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                ComponentName com = intent.getComponent();
                if (com != null) {
                    return com.getClassName();
                }
            }
            return "";
        } finally {
            Binder.restoreCallingIdentity(origId);
		}
    }

}
