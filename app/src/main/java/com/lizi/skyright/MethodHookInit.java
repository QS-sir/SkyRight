package com.lizi.skyright;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
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
import java.lang.reflect.Method;
import android.os.UserManager;

public class MethodHookInit extends XC_MethodHook implements HookRegistry.ResourceReleasable,DataUpdateCallback {

    private static final String TAG = "MethodHookInit";
	private HookRegistry hookRegistry;
    private ClassLoader classLoader;
    private SystemServerManagerImpl systemServerManagerImpl;
	private MonitorActivityManager monitorActivityManager;
    private BridgeBindingReceiver bridgeBindingReceiver;
    private HookExtensionManager hookExtensionManager;
    private HideAccessibilityStatusManager hideAccessibilityStatus;
    private MonitorWindowManager monitorWindowManager;
    private XC_MethodHook.Unhook hideRootHook;
    private boolean isInitMethondHookCallback;
    private UserManager userManager;

	public MethodHookInit(HookRegistry hookRegistry) {
        XposedBridge.log("MethodHookInit init finish");
		this.hookRegistry = hookRegistry;
        this.classLoader = hookRegistry.getSystemClassLoader();
        this.userManager = hookRegistry.getContext().getSystemService(UserManager.class);
        init();
	}

	private void init() {
        systemServerManagerImpl = new SystemServerManagerImpl(hookRegistry, this);
        setUnInstallListener();
        registerBridgeBindingReceiver();
        if (!userManager.isUserUnlocked()){
            UserUnlockListener.initUnlockListener(this);
        }else{
            unlockCallback();
        }
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
        if (!isInitMethondHookCallback) {
            initMethodHookCallback();
        }
        updateModifyStartActivityPackages(JsonParser.getMapStringData(data, SystemServerManagerImpl.MODIFY_PACKAGES_START_ACTIVITY));
        updateMonitorPackagesActivity(JsonParser.getListData(data, SystemServerManagerImpl.MONITOR_PACKAGES_ACTIVITY));
        updateMonitorActivitys(JsonParser.getMapData(data, SystemServerManagerImpl.MONITOR_ACTIVITYS));
        updatePackagesHideAccessibility(JsonParser.getListData(data, SystemServerManagerImpl.PACKAGES_HIDE_ACCESSIBILITY));
        updateWhiteListPackages(JsonParser.getListData(data, SystemServerManagerImpl.WHITE_LIST_PACKAGES));
        hookExtensionManager.init(data);
    }

    private void initMethodHookCallback() {
        monitorActivityManager = new MonitorActivityManager(this);
        hideAccessibilityStatus = new HideAccessibilityStatusManager(hookRegistry.getContext());
        hookExtensionManager = new HookExtensionManager(hookRegistry.getContext());
        monitorWindowManager = new MonitorWindowManager(hookRegistry.getContext());
        isInitMethondHookCallback = true;
        XposedBridge.log(TAG + " initMethodHookCallback finish ");
    }
    
    protected void unlockCallback(){
        monitorActivityManager.initActivityRequestDialog();
        monitorWindowManager.initAccessibilityWindowManager();
    }

    public void setRefuseActivityOperate(String pkg, String act) {
        try {
            systemServerManagerImpl.setMonitorActivity(pkg, act, ActivityRequestDialog.REQUEST_ALWAYS_REFUSE);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setRefuseActivityOperate  error:" + e.toString());
        }
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
        monitorActivityManager.updateWhiteListPackages(data);
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
        initMonitorActivityManager();
        initSkipStartActivityPermissionCheck();
        initHideAccessibilityStatus();
       // initMonitorWindowManager();
	}

    private void initMonitorActivityManager() {
        Object obj[] = {IApplicationThread.class,String.class,String.class,Intent.class,String.class,IBinder.class,String.class,
            int.class,int.class,ProfilerInfo.class,Bundle.class,int.class,boolean.class,monitorActivityManager};
        hookRegistry.findAndHookMethod("com.android.server.wm.ActivityTaskManagerService", hookRegistry.getSystemClassLoader(), "startActivityAsUser", obj);

    }

    private void initSkipStartActivityPermissionCheck() {
        ClassLoader clas = hookRegistry.getSystemClassLoader();
        Object parameter[] = {Intent.class,ActivityInfo.class,String.class,int.class,int.class,
            int.class,String.class,String.class,boolean.class,boolean.class,XposedHelpers.findClass("com.android.server.wm.WindowProcessController", clas),
            XposedHelpers.findClass("com.android.server.wm.ActivityRecord", clas),XposedHelpers.findClass("com.android.server.wm.Task", clas),monitorActivityManager.getSkipStartActivityPermission()};
        hookRegistry.findAndHookMethod("com.android.server.wm.ActivityTaskSupervisor", hookRegistry.getSystemClassLoader(), "checkStartAnyActivityPermission", parameter);
    }

    private void initHideAccessibilityStatus() {
        Class<?> cs = XposedHelpers.findClass("com.android.server.accessibility.AccessibilityManagerService", hookRegistry.getSystemClassLoader());
        hookRegistry.findAndHookMethod("com.android.providers.settings.SettingsProvider", hookRegistry.getModuleClassLoader(), "getSecureSetting", String.class, int.class, hideAccessibilityStatus);
        hookRegistry.findAndHookMethod(cs, "addClient", IAccessibilityManagerClient.class, int.class, hideAccessibilityStatus);
        hookRegistry.findAndHookMethod(cs, "getEnabledAccessibilityServiceList", int.class, int.class, hideAccessibilityStatus);
    }
    
    private void initMonitorWindowManager(){
        Class<?> cs = XposedHelpers.findClass("com.android.server.wm.WindowManagerService",classLoader);
//        Method md[] = cs.getDeclaredMethods();
//        for (int i = 0; i < md.length; i++) {
//            hookRegistry.hookAllMethods(cs,md[i].getName(),monitorWindowManager);
//        }
        hookRegistry.hookAllMethods(cs,"getFocusedWindowLocked",monitorWindowManager);
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
        hookExtensionManager.unhookAll();
        XposedBridge.log("[com.lizi.skyright] release hook resources");
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
