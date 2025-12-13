package com.lizi.skyright;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Message;
import android.os.UserManager;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Constructor;

public class DynamicHookImpl extends XC_MethodHook {

	private HookRegistry hookRegistry;
	private ClassLoader systemClassLoader;
	private ClassLoader moduleClassLoader;
	private Context context;
	private PackageManager pm;
	private UserManager userManager;
	private Object dynamicHookRegistry;
	private String apkPath;

    public DynamicHookImpl(ClassLoader moduleClassLoader) {
		this.hookRegistry = new HookRegistry(moduleClassLoader);
		this.systemClassLoader = hookRegistry.getSystemClassLoader();
		this.moduleClassLoader = hookRegistry.getModuleClassLoader();
		this.context = hookRegistry.getContext();
		this.pm = context.getPackageManager();
		this.userManager = context.getSystemService(UserManager.class);
	}

	@Override
	protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
		String method = param.method.getName();
		if (method.equals("handleMessage")) {
			initDynamicHook();
		} else if (apkPath == null && userManager.isUserUnlocked() && method.equals("addWindowToken")) {
			try {
				initDynamicHook();
				XposedHelpers.findAndHookMethod("com.android.server.pm.PackageHandler", systemClassLoader, "handleMessage", Message.class, this);
				XposedBridge.log("init dynamic hook ok");
			} catch (ClassNotFoundException e) {
				new MethodHookInit(hookRegistry).initMethodHook();
				XposedBridge.log("init dynamic hook on");
			}
			//初始化完毕就不需要此hook，解除该hook
			XposedBridge.unhookMethod(param.method, this);
		}
	}

	//初始化动态hook
	private void initDynamicHook() throws Exception {
		ApplicationInfo appInfo = null;
		try {
			appInfo = pm.getApplicationInfo("com.lizi.skyright", 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.fillInStackTrace();
		}
		if (appInfo == null || appInfo.sourceDir.equals(apkPath)) {
			return;
		}
		apkPath = appInfo.sourceDir;
		if (dynamicHookRegistry != null) {
			XposedHelpers.callMethod(dynamicHookRegistry, "unhookAll");
			XposedBridge.log("new init dynamic hook");
		}
		PathClassLoader hookClassLoader = new PathClassLoader(apkPath, XposedBridge.BOOTCLASSLOADER);
		Class<?> classMethodHookInit = hookClassLoader.loadClass("com.lizi.skyright.MethodHookInit");
		Class<?> classHookRegistry = hookClassLoader.loadClass("com.lizi.skyright.HookRegistry");
		Constructor<?> conMethodHookInit = classMethodHookInit.getConstructor(classHookRegistry);
		Constructor<?> conHookRegistry = classHookRegistry.getConstructor(ClassLoader.class);
		dynamicHookRegistry = conHookRegistry.newInstance(hookClassLoader);
		Object dynamicHook = conMethodHookInit.newInstance(dynamicHookRegistry);
		XposedHelpers.callMethod(dynamicHook, "initDynamicMethodHook");
	}

}
