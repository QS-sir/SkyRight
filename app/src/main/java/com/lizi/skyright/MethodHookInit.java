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
import android.view.accessibility.IAccessibilityManagerClient;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.Map;
import java.util.Set;

public class MethodHookInit extends XC_MethodHook implements HookRegistry.ResourceReleasable,DataUpdateCallback {

    private static final String TAG = "MethodHookInit";
	private HookRegistry hookRegistry;
	private MonitorActivityManager monitorActivityManager;
    private BridgeBindingReceiver bridgeBindingReceiver;
    private SystemServerManagerImpl systemServerManagerImpl;
    private XC_MethodHook.Unhook hideRootHook;
    private HookExtensionManager hookExtensionManager;
    private HideAccessibilityStatus hideAccessibilityStatus;

	public MethodHookInit(HookRegistry hookRegistry) {
        XposedBridge.log("MethodHookInit init finish");
		this.hookRegistry = hookRegistry;
        init();
	}

	private void init() {
        initMethodHookCallback();
        systemServerManagerImpl = new SystemServerManagerImpl(hookRegistry, this);
        setUnInstallListener();
        registerBridgeBindingReceiver();
	}

    private void initMethodHookCallback() {
        monitorActivityManager = new MonitorActivityManager(hookRegistry);
        hideAccessibilityStatus = new HideAccessibilityStatus(hookRegistry.getContext());
        hookExtensionManager = new HookExtensionManager(hookRegistry.getContext());
    }

    private void registerBridgeBindingReceiver() {
        bridgeBindingReceiver = new BridgeBindingReceiver(hookRegistry, systemServerManagerImpl);
        IntentFilter intentFilter = new IntentFilter(BridgeBindingReceiver.BRIDGE_ACTION);
        hookRegistry.getContext().registerReceiver(bridgeBindingReceiver, intentFilter, Context.RECEIVER_EXPORTED);
    }


    private void setUnInstallListener() {
        Object obj[] = {String.class,long.class,int.class,int.class,boolean.class,this};
        hookRegistry.findAndHookMethod("com.android.server.pm.DeletePackageHelper", hookRegistry.getSystemClassLoader(), "deletePackageX", obj);
    }

    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        String packageName = param.args[0].toString();
        checkExpandPackageIsUnInstall(packageName);
    }


    @Override
    public void updateAllData(String data) {
        updateModifyStartActivityPackages(JsonParser.getMapStringData(data, SystemServerManagerImpl.MODIFY_PACKAGES_START_ACTIVITY));
        updateMonitorPackagesActivity(JsonParser.getListData(data, SystemServerManagerImpl.MONITOR_PACKAGES_ACTIVITY));
        updateMonitorActivitys(JsonParser.getMapData(data, SystemServerManagerImpl.MONITOR_ACTIVITYS));
        updatePackagesHideAccessibility(JsonParser.getListData(data, SystemServerManagerImpl.PACKAGES_HIDE_ACCESSIBILITY));
        updateWhiteListPackages(JsonParser.getListData(data,SystemServerManagerImpl.WHITE_LIST_PACKAGES));
        hookExtensionManager.init(data);
    }

    @Override
    public void updateModifyStartActivityPackages(Map<String, String> data) {
        monitorActivityManager.updateModifyStartActivityPackages(data);
    }


    @Override
    public void updateMonitorPackagesActivity(Set<String> data) {
        monitorActivityManager.updateMonitorPackagesActivity(data);
    }

    @Override
    public void updateMonitorActivitys(Map<String, Map<String, String>> data) {
        monitorActivityManager.updateMonitorActivitys(data);
    }

    @Override
    public void updateWhiteListPackages(Set<String> data) {
        
    }
    

    @Override
    public void updatePackagesHideAccessibility(Set<String> data) {
        hideAccessibilityStatus.updatePackagesHideAccessibility(data);
    }

    @Override
    public void setEnabledHookPackage(String packageName, boolean enable) {
        hookExtensionManager.setEnabledHookPackage(packageName, enable);
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
            hookExtensionManager.unhookAll();
        } else {
            methodHook();
        }
    }

    public HookRegistry getHookRegistry() {
        return this.hookRegistry;
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
        hookExtensionManager.init();
        Object obj[] = {IApplicationThread.class,String.class,String.class,Intent.class,String.class,IBinder.class,String.class,
            int.class,int.class,ProfilerInfo.class,Bundle.class,int.class,boolean.class,monitorActivityManager};
        hookRegistry.findAndHookMethod("com.android.server.wm.ActivityTaskManagerService", hookRegistry.getSystemClassLoader(), "startActivityAsUser", obj);
        initHideAccessibilityStatus();
	}

    private void initHideAccessibilityStatus() {
        Class<?> cs = XposedHelpers.findClass("com.android.server.accessibility.AccessibilityManagerService", hookRegistry.getSystemClassLoader());
        hookRegistry.findAndHookMethod("com.android.providers.settings.SettingsProvider", hookRegistry.getModuleClassLoader(), "getSecureSetting", String.class, int.class, hideAccessibilityStatus);
        hookRegistry.findAndHookMethod(cs, "addClient", IAccessibilityManagerClient.class, int.class, hideAccessibilityStatus);
        hookRegistry.findAndHookMethod(cs, "getEnabledAccessibilityServiceList", int.class, int.class, hideAccessibilityStatus);
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
            try {
                hookRegistry.getContext().unregisterReceiver(bridgeBindingReceiver);
            } catch (IllegalArgumentException e) {
                e.fillInStackTrace();
            }
        }
        if (hideRootHook != null) {
            hideRootHook.unhook();
        }
        hookExtensionManager.unhookAll();
	}

    //检查扩展模块是否被卸载
    public void checkExpandPackageIsUnInstall(String packageName) {
        systemServerManagerImpl.checkExpandPackageIsUnInstall(packageName);
        hookExtensionManager.checkExpandPackageIsUnInstall(packageName);
    }

    //检查扩展模块是否被覆盖安装
    public void checkIsCoverPackage(String packageName) {
        hookExtensionManager.checkIsCoverPackage(packageName);
    }

}
