package com.lizi.skyright;

import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import com.lizi.skyright.service.ISystemServerManager;
import java.util.List;
import java.util.ArrayList;
import android.os.RemoteException;

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

    public List<ApplicationInfo> getInstalledApplications() {
        return getInstalledApplications(0);
    }

    public List<ApplicationInfo> getInstalledApplications(int flags) {
        List<ApplicationInfo> list = new ArrayList<>();
        ISystemServerManager localService = this.service;
        if (localService != null) {
            try {
                List<ApplicationInfo> remoteList = localService.getInstalledApplications(flags);
                if (remoteList != null) {
                    list = remoteList;
                }
            } catch (RemoteException e) {
                LogManager.log(TAG, "getInstalledApplications RemoteException: " + e.toString());
                List<String> appList = getPackageNames();
                int size = appList.size();
                for (int i = 0; i < size; i++) {
                    list.add(getApplicationInfo(appList.get(i)));
                }
            }
        } else {
            LogManager.log(TAG, "Service not connected. Returning empty list");
        }
        return list;
    }
    
    public boolean isDynamicHook(){
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

}

