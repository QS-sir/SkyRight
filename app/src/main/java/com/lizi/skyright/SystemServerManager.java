package com.lizi.skyright;

import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import com.lizi.skyright.service.ISystemServerManager;
import java.util.List;
import java.util.ArrayList;
import android.os.RemoteException;
import android.content.pm.PackageInfo;

public final class SystemServerManager {

    public static final String TAG = "SystemServerManager";
    private static volatile SystemServerManager systemServerManager;
    private volatile ISystemServerManager service;

    private SystemServerManager() {
        IBinder binder = BridgeManager.getInstance().getClientBinder();
        this.service = ISystemServerManager.Stub.asInterface(binder);
    }

    public static SystemServerManager getManagerInstance() {
        if (systemServerManager == null) {
            synchronized (SystemServerManager.class) {
                if (systemServerManager == null) {
                    systemServerManager = new SystemServerManager();
                }
            }
        }
        return systemServerManager;
    }

    public boolean isInitService() {
        return service != null;
    }

    public List<String> getServiceLoges(){
        List<String> list = new ArrayList<>();
        try {
            list = service.getServiceLoges();
        } catch (RemoteException e) {
            LogManager.log(TAG, "getServiceLoges error:" + e.toString());
        }
        return list;
    }

    public void setPackageWhiteList(String packageName, boolean b) {
        try {
            service.setPackageWhiteList(packageName, b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "RemoteException error:" + e.toString());
        }
    }


    public void setMonitorPackageActivity(String packageName, boolean b) {
        try {
            service.setMonitorPackageActivity(packageName, b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setMonitorPackageActivity error:" + e.toString());
        }
    }
    

    public void setMonitorActivity(String packageName, String activityName, String action) {
        try {
            service.setMonitorActivity(packageName, activityName, action);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setMonitorActivity error:" + e.toString());
        }
    }

    public void setModifyPackageStartActivity(String packageName, String activityName) {
        try {
            service.setModifyPackageStartActivity(packageName, activityName);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setModifyPackageStartActivity error:" + e.toString());
        }
    }
    

    public String getStorageData() {
        String st = "{}";
        try {
            st = service.getStorageData();
        } catch (RemoteException e) {
            LogManager.log(TAG, "getStorageData error:" + e.toString());
        }
        return st;
    }

    public int getParcelSize() {
        try {
            return service.getParcelSize();
        } catch (RemoteException e) {
            LogManager.log(TAG, "getParcelSize error:" + e.toString());
        }
        return 0;
    }

    public void setPackageHideAccessibilityStatus(String packageName, boolean b) {
        try {
            service.setPackageHideAccessibilityStatus(packageName, b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setPackageHideAccessibilityStatus error:" + e.toString());
        }
    }

    public List<ApplicationInfo> getInstalledApplications() {
        return getInstalledApplications(0);
    }

    public List<ApplicationInfo> getInstalledApplications(int flags) {
        List<ApplicationInfo> list = new ArrayList<>();
        List<String> packageNames = getPackageNames();
        for (String packageName : packageNames) {
            list.add(getApplicationInfo(packageName, flags));
        }
        return list;
    }

    public boolean isDynamicHook() {
        try {
            return service.isDynamicHook();
        } catch (RemoteException e) {
            LogManager.log(TAG, "isDynamicHook error:" + e.toString());
        }
        return false;
    }

    public List<String> getPackageNames() {
        List<String> list = new ArrayList<>();
        try {
            list = service.getPackageNames();
        } catch (RemoteException e) {
            LogManager.log(TAG, "getPackageNames error:" + e.toString());
        }
        return list;
    }

    public boolean getOneplusHideRootStatus() {
        try {
            return service.getOneplusHideRootStatus();
        } catch (RemoteException e) {
            LogManager.log(TAG, "getOneplusHideRootStatus error:" + e.toString());
        }
        return false;
    }

    public boolean isPauseAllHook() {
        try {
            return service.isPauseAllHook();
        } catch (RemoteException e) {
            LogManager.log(TAG, "isPauseAllHook error:" + e.toString());
        }
        return false;
    }

    public void setPauseAllHook(boolean b) {
        try {
            service.setPauseAllHook(b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setPauseAllHook error:" + e.toString());
        }
    }

    public void setOneplusHideRootStatus(boolean b) {
        try {
            service.setOneplusHideRootStatus(b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setOneplusHideRootStatus error:" + e.toString());
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags) {
        ApplicationInfo info = new ApplicationInfo();
        try {
            info = service.getApplicationInfo(packageName, flags);
        } catch (RemoteException e) {
            LogManager.log(TAG, "getApplicationInfo error:" + e.toString());
        }
        return info;
    }

    public ApplicationInfo getApplicationInfo(String packageName) {
        return getApplicationInfo(packageName, 0);
    }

    public void setEnabledHookPackage(String packageName, boolean enable) {
        try {
            service.setEnabledHookPackage(packageName, enable);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setEnabledHookPackage error:" + e.toString());
        }
    }

    public boolean isEnabledHookPackage(String packageName) {
        try {
            return service.isEnabledHookPackage(packageName);
        } catch (RemoteException e) {
            LogManager.log(TAG, "isEnabledHookPackage error:" + e.toString());
        }
        return false;
    }

    public PackageInfo getPackageInfo(String packageName, int flags) {
        PackageInfo info = new PackageInfo();
        try {
            return service.getPackageInfo(packageName, flags);
        } catch (RemoteException e) {
            LogManager.log(TAG, "getPackageInfo error:" + e.toString());
        }
        return info;
    }

    public String getPackageLaunchActivityName(String packageName){
        try {
            return service.getPackageLaunchActivityName(packageName);
        } catch (RemoteException e) {
            LogManager.log(TAG, "getPackageLaunchActivityName error:" + e.toString());
        }
        return null;
    }
    
    public boolean isEnabledRebootProtect(){
        try {
            return service.isEnabledRebootProtect();
        } catch (RemoteException e) {
            LogManager.log(TAG, "isEnabledRebootProtect error:" + e.toString());
        }
        return false;
    }
    
    public void setEnabledRebootProtect(boolean b){
        try {
            service.setEnabledRebootProtect(b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setEnabledRebootProtect error:" + e.toString());
        }
    }
    
    public boolean isRemoveWindowSecureFlags(){
        try {
           return service.isRemoveWindowSecureFlags();
        } catch (RemoteException e) {
            LogManager.log(TAG, "isRemoveWindowSecureFlags error:" + e.toString());
        }
        return false;
    }
    
    public void setRemoveWindowSecureFlags(boolean b){
        try {
            service.setRemoveWindowSecureFlags(b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setRemoveWindowSecureFlags error:" + e.toString());
        }
    }
    
    public void setMonitorPackagesSubWindow(String packageName,boolean b){
        try {
            service.setMonitorPackagesSubWindow(packageName, b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setMonitorPackagesSubWindow error:" + e.toString());
        }
    }
    
    public void setMonitorActivitySubWindow(String packageName,String className,boolean b){
        try {
            service.setMonitorActivitySubWindow(packageName, className, b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setMonitorActivitySubWindow error:" + e.toString());
        }
    }
}

