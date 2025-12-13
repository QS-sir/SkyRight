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
        List<ApplicationInfo> list = new ArrayList<>();
        ISystemServerManager localService = this.service;
        if (localService != null) {
            try {
                List<ApplicationInfo> remoteList = localService.getInstalledApplications();
                if (remoteList != null) {
                    list = remoteList;
                }
            } catch (RemoteException e) {
                LogManager.log(TAG, "getInstalledApplications RemoteException: " + e.toString());
            } catch (Exception e) {
                LogManager.log(TAG, "getInstalledApplications Unknown Error: " + e.toString());
            }
        } else {
            LogManager.log(TAG, "Service not connected. Returning empty list.");
        }
        return list;
    }

    public boolean getOneplusHideRootStatus(){
        try {
            return service.getOneplusHideRootStatus();
        } catch (RemoteException e) {
            LogManager.log(TAG, "getOneplusHideRootStatus error:");
        }
        return false;
    }
    
    public boolean isPauseAllHook(){
        try {
            return service.isPauseAllHook();
        } catch (RemoteException e) {
            LogManager.log(TAG, "isPauseAllHook error:");
        }
        return false;
    }
    
    public void setPauseAllHook(boolean b){
        try {
            service.setPauseAllHook(b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setPauseAllHook error:");
        }
    }
    
    public void setOneplusHideRootStatus(boolean b){
        try {
            service.setOneplusHideRootStatus(b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setOneplusHideRootStatus error:");
        }
    }

}

