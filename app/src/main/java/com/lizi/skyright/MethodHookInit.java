package com.lizi.skyright;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import java.util.List;
import java.util.Map;

public class MethodHookInit implements HookRegistry.ResourceReleasable,DataUpdateCallback {

    private static final String TAG = "MethodHookInit";
	private HookRegistry hookRegistry;
	private HookActivityTaskManagerService hookActivityTaskManagerService;
    private BridgeBindingReceiver bridgeBindingReceiver;
    private SystemServerManagerImpl systemServerManagerImpl;
    private XC_MethodHook.Unhook hideRootHook;

	public MethodHookInit(HookRegistry hookRegistry) {
		this.hookRegistry = hookRegistry;
		init();
	}

	private void init() {
        systemServerManagerImpl = new SystemServerManagerImpl(hookRegistry, this);
        bridgeBindingReceiver = new BridgeBindingReceiver(hookRegistry, systemServerManagerImpl);
        IntentFilter intentFilter = new IntentFilter(BridgeBindingReceiver.BRIDGE_ACTION);
        hookRegistry.getContext().registerReceiver(bridgeBindingReceiver, intentFilter, Context.RECEIVER_EXPORTED);
		hookActivityTaskManagerService = new HookActivityTaskManagerService(hookRegistry);
	}


    @Override
    public void updataAllData(String data) {
        updataModifyStartActivityPackages(JsonParser.getMapStringData(data,"modify_start_activity_list"));
        updataMonitorPackagesActivity(JsonParser.getListData(data,"monitor_packages_activity"));
        updataMonitorActivitys(JsonParser.getMapData(data,"monitor_activity_list"));
        updataHideAccessibilityPackages(JsonParser.getListData(data,"monitor_packages_activity"));
    }

    @Override
    public void updataModifyStartActivityPackages(Map<String, String> data) {
        
    }

 
    @Override
    public void updataMonitorPackagesActivity(List<String> data) {
        
    }

    @Override
    public void updataMonitorActivitys(Map<String, Map<String, String>> data) {

    }

    @Override
    public void updataHideAccessibilityPackages(List<String> data) {

    }

    @Override
    public void setOneplusHideRootStatus(boolean b) {
        if (Build.BRAND.equals("OnePlus")) {
            if (b) {
                hideRootHook = XposedHelpers.findAndHookMethod("com.android.server.oplus.heimdall.service.RootService", hookRegistry.getSystemClassLoader(), "isRoot", String.class, XC_MethodReplacement.returnConstant(false));
            } else {
                if (hideRootHook != null) {
                    hideRootHook.unhook();
                }
            }
        }
    }
    
    @Override
    public void setPauseAllHook(boolean b) {
        if (b) {
            hookRegistry.pauseAllHook();
            if (hideRootHook != null) {
                hideRootHook.unhook();
            }
        } else {
            methodHook();
        }
    }

	public void initMethodHook() {
        try {
            boolean b = systemServerManagerImpl.isPauseAllHook();
            if (!b) {
                methodHook();
            }
        } catch (RemoteException e) {
            LogManager.log(TAG, "initMethodHook  error:" + e.toString());
            methodHook();
        }
	}

	public void initDynamicMethodHook() {
		hookRegistry.setDynamic(true);
		hookRegistry.setResourceReleasable(this);
        try {
            boolean b = systemServerManagerImpl.isPauseAllHook();
            if (!b) {
                methodHook();
            }
        } catch (RemoteException e) {
            LogManager.log(TAG, "initDynamicMethodHook  error:" + e.toString());
            methodHook();
        }
	}

	private void methodHook() {
		hideOnePlusRootStatus();
        Object obj[] = {IApplicationThread.class,String.class,String.class,Intent.class,String.class,IBinder.class,String.class,
        int.class,int.class,ProfilerInfo.class,Bundle.class,int.class,boolean.class,hookActivityTaskManagerService};
        hookRegistry.findAndHookMethod("com.android.server.wm.ActivityTaskManagerService",hookRegistry.getSystemClassLoader(),"startActivityAsUser",obj);
	}
   

    private void hideOnePlusRootStatus() {
        try {
            boolean b = systemServerManagerImpl.getOneplusHideRootStatus();
            setOneplusHideRootStatus(b);
        } catch (RemoteException e) {
            LogManager.log(TAG, "hideOnePlusRootStatus  error:" + e.toString());
        }
    }

	@Override
	public void onRelease() throws Exception {
        if (bridgeBindingReceiver != null) {
            hookRegistry.getContext().unregisterReceiver(bridgeBindingReceiver);
        }
        if (hideRootHook != null) {
            hideRootHook.unhook();
        }
	}

}
