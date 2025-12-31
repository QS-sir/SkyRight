package com.lizi.skyright;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserManager;
import android.view.ContextThemeWrapper;
import android.view.accessibility.IAccessibilityManagerClient;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;

public class MethodHookInit extends XC_MethodHook implements HookRegistry.ResourceReleasable {

    private static final String TAG = "MethodHookInit";
    private Map<String,DataUpdateListener> updateListener;
	private HookRegistry hookRegistry;
    private ClassLoader classLoader;
    private SystemServerManagerImpl systemServerManagerImpl;
	private MonitorActivityManager monitorActivityManager;
    private BridgeBindingReceiver bridgeBindingReceiver;
    private HookExtensionManager hookExtensionManager;
    private HideAccessibilityStatusManager hideAccessibilityStatus;
    private MonitorSubWindowManager monitorSubWindowManager;
    private RemoveWindowSecureFlags removeWindowSecureFlags;
    private XC_MethodHook hideOnePlusRootStatus;
    private boolean isInitMethondHookCallback;
    private UserManager userManager;
    private ProcessStartManager processStartManager;
    private Context moduleResourcesContext;

	public MethodHookInit(HookRegistry hookRegistry) {
		this.hookRegistry = hookRegistry;
        this.updateListener = new HashMap<>();
        this.classLoader = hookRegistry.getSystemClassLoader();
        this.userManager = hookRegistry.getContext().getSystemService(UserManager.class);
	}

    public void tryDynamicLoad() {
        XposedBridge.log("try dynamic hook");
    }

	public void init() {
        systemServerManagerImpl = new SystemServerManagerImpl(this);
        setUnInstallListener();
        registerBridgeBindingReceiver();
        if (!userManager.isUserUnlocked()) {
            UserUnlockListener.initUnlockListener(this);
        } else {
            unlockCallback();
        }
        XposedBridge.log(TAG + " init finish");
	}

    //注册桥接广播，用于与system_server交互
    private void registerBridgeBindingReceiver() {
        bridgeBindingReceiver = new BridgeBindingReceiver(hookRegistry, systemServerManagerImpl);
        IntentFilter intentFilter = new IntentFilter(BridgeBindingReceiver.BRIDGE_ACTION);
        hookRegistry.getContext().registerReceiver(bridgeBindingReceiver, intentFilter, Context.RECEIVER_EXPORTED);
    }


    //设置卸载监听器
    private void setUnInstallListener() {
        Object obj[] = {String.class,long.class,int.class,int.class,boolean.class,this};
        hookRegistry.findAndHookMethod("com.android.server.pm.DeletePackageHelper", hookRegistry.getSystemClassLoader(), "deletePackageX", obj);
    }


    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        String packageName = param.args[0].toString();
        checkExpandPackageIsUnInstall(packageName);
    }

    public void updateData(String key, String data) {
        if (!isInitMethondHookCallback) {
            initMethodHookCallback();
        }
        if (key == null) {
            Iterator<String> iterator = updateListener.keySet().iterator();
            while (iterator.hasNext()) {
                String k = iterator.next();
                DataUpdateListener dataUpdateListener = updateListener.get(k);
                dataUpdateListener.dataUpdate(k, data);
            }
            hookExtensionManager.init(data);
        } else {
            DataUpdateListener dataUpdateListener = updateListener.get(key);
            dataUpdateListener.dataUpdate(key, data);
        }
    }

    private void initDataUpdateListener() {
        updateListener.put(SystemServerManagerImpl.MODIFY_PACKAGES_START_ACTIVITY, monitorActivityManager);
        updateListener.put(SystemServerManagerImpl.MONITOR_PACKAGES_ACTIVITY, monitorActivityManager);
        updateListener.put(SystemServerManagerImpl.MONITOR_ACTIVITYS, monitorActivityManager);
        updateListener.put(SystemServerManagerImpl.PACKAGES_HIDE_ACCESSIBILITY, hideAccessibilityStatus);
        updateListener.put(SystemServerManagerImpl.WHITE_LIST_PACKAGES, monitorActivityManager);
    }

    private void initMethodHookCallback() {
        monitorActivityManager = new MonitorActivityManager(this);
        hideAccessibilityStatus = new HideAccessibilityStatusManager(hookRegistry.getContext());
        hookExtensionManager = new HookExtensionManager(hookRegistry.getContext());
        monitorSubWindowManager = new MonitorSubWindowManager(this);
        processStartManager = new ProcessStartManager(hookRegistry.getContext());
        removeWindowSecureFlags = new RemoveWindowSecureFlags();
        hideOnePlusRootStatus = XC_MethodReplacement.returnConstant(false);
        isInitMethondHookCallback = true;
        initDataUpdateListener();
        XposedBridge.log(TAG + " initMethodHookCallback finish ");
    }

    protected void unlockCallback() {
        monitorActivityManager.initActivityRequestDialog();
        //monitorWindowManager.initAccessibilityWindowManager();
    }

    protected Context getMduleResourcesContext() {
        try {
            if (moduleResourcesContext == null) {
                Context pluginContext = hookRegistry.getContext().createPackageContext("com.lizi.skyright", Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_REGISTER_PACKAGE);
                Resources newResources = pluginContext.getResources();
                moduleResourcesContext = new ContextThemeWrapper(pluginContext, newResources.newTheme());
            }
        } catch (Exception e) {
            XposedBridge.log(TAG + " moduleResourcesContext error:");
        }
        return moduleResourcesContext;
    }

    public void setRefuseActivityOperate(String pkg, String act) {
        try {
            systemServerManagerImpl.setMonitorActivity(pkg, act, ActivityRequestDialog.REQUEST_ALWAYS_REFUSE);
        } catch (RemoteException e) {
            LogManager.log(TAG, "setRefuseActivityOperate  error:" + e.toString());
        }
    }

    public void setEnabledHookPackage(String packageName, boolean enable) {
        hookExtensionManager.setEnabledHookPackage(packageName, enable);
    }

    
    public void setPauseAllHook(boolean b) {
        if (b) {
            hookRegistry.pauseAllHook();
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
            //methodHook();
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
            // methodHook();
        }
	}

    //统合所有hook方法
	private void methodHook() {
		hideOnePlusRootStatus();
        hookExtensionManager.init();
        initMonitorActivityManager();
        initHideAccessibilityStatus();
        setRemoveWindowSecureFlags();
        initMonitorWindowManager();
       // initProcessStartManager();
	}

    private void setRemoveWindowSecureFlags() {
        try {
            boolean b = systemServerManagerImpl.isRemoveWindowSecureFlags();
            if (b) {
                setRemoveWindowSecureFlags(b);
            }
        } catch (RemoteException e) {
            LogManager.log(TAG, "setRemoveWindowSecureFlags  error:" + e.toString());
        }
    }

    public void setRemoveWindowSecureFlags(boolean b) {
        if (b) {
            Class<?> cs = XposedHelpers.findClass("com.android.server.wm.WindowManagerService", classLoader);
            hookRegistry.hookAllMethods(cs, "relayoutWindow", removeWindowSecureFlags);
        } else {
            hookRegistry.unhook(removeWindowSecureFlags);
        }
    }

    private void initMonitorActivityManager() {
        Object obj[] = {IApplicationThread.class,String.class,String.class,Intent.class,String.class,IBinder.class,String.class,
            int.class,int.class,ProfilerInfo.class,Bundle.class,int.class,boolean.class,monitorActivityManager};
        hookRegistry.findAndHookMethod("com.android.server.wm.ActivityTaskManagerService", hookRegistry.getSystemClassLoader(), "startActivityAsUser", obj);
        initSkipStartActivityPermissionCheck();
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

    private void initProcessStartManager() {
        Class<?> cs = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", hookRegistry.getSystemClassLoader());
        hookRegistry.hookAllMethods(cs, "startProcessLocked", processStartManager);
    }

    private void initMonitorWindowManager() {
        Class<?> cs = XposedHelpers.findClass("com.android.server.wm.WindowManagerService",classLoader);
        int i = hookRegistry.hookAllMethods(cs, "addWindow", monitorSubWindowManager);
        XposedBridge.log("hook方法数量："+i);
    }

    private void hideOnePlusRootStatus() {
        try {
            boolean b = systemServerManagerImpl.getOneplusHideRootStatus();
            if (b) {
                setOneplusHideRootStatus(b);
            }
        } catch (RemoteException e) {
            LogManager.log(TAG, "hideOnePlusRootStatus  error:" + e.toString());
        }
    }
    
    public void setOneplusHideRootStatus(boolean b) {
        if (Build.BRAND.equals("OnePlus")) {
            if (b) {
                hookRegistry.findAndHookMethod("com.android.server.oplus.heimdall.service.RootService", hookRegistry.getSystemClassLoader(), "isRoot", String.class, hideOnePlusRootStatus);
            } else {
                hookRegistry.unhook(hideOnePlusRootStatus);
            }
        }
    }
    

	@Override
	public void onRelease() throws Exception {
        if (bridgeBindingReceiver != null) {
            hookRegistry.getContext().unregisterReceiver(bridgeBindingReceiver);
        }

        if (hookExtensionManager != null) {
            hookExtensionManager.unhookAll();
        }

        if (monitorActivityManager != null) {
            monitorActivityManager.releaseDialogResources();
        }

        XposedBridge.log("[com.lizi.skyright] release hook");
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
