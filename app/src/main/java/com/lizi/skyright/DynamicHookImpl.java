package com.lizi.skyright;
import android.content.Context;
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
	private ClassLoader baseClassLoader;
	private ClassLoader hookClassLoader;
	private Context context;
	private PackageManager pm;
	private UserManager userManager;
	private Object dynamicHookRegistry;
	private String apkPath;

    public DynamicHookImpl(ClassLoader hookClassLoader) {
		this.hookRegistry = new HookRegistry(hookClassLoader);
		this.baseClassLoader = hookRegistry.baseClassLoader();
		this.hookClassLoader = hookRegistry.hookClassLoader();
		this.context = hookRegistry.getContext();
		this.pm = context.getPackageManager();
		this.userManager = context.getSystemService(UserManager.class);
	}

	@Override
	protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
		String method = param.method.getName();
		if (apkPath == null && userManager.isUserUnlocked() && method.equals("addWindowToken")) {
			try {
				initDynamicHook();
				XposedHelpers.findAndHookMethod("com.android.server.pm.PackageHandler", baseClassLoader, "handleMessage", Message.class, this);
				hookRegistry.setIsDynamicHook(true);
				XposedBridge.log("init dynamic hook ok");
			} catch (Exception e) {
				new MethodHookInit(hookRegistry);
				XposedBridge.log("init dynamic hook on");
			}
			//初始化完毕就不需要此hook，解除该hook
			XposedBridge.unhookMethod(param.method, this);
		} else if (method.equals("handleMessage")) {
			initDynamicHook();
		}
	}

	private String getApkPath() {
		try {
			return pm.getApplicationInfo("com.lizi.skyright", 0).sourceDir;
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}
	}

	private void initDynamicHook() throws Exception {
		if (apkPath != null && apkPath.equals(getApkPath())) {
			return;
		}
		apkPath = getApkPath();
		if (dynamicHookRegistry != null) {
			XposedHelpers.callMethod(dynamicHookRegistry, "releaseMethodHook");
		}
		PathClassLoader hookClassLoader = new PathClassLoader(apkPath, hookRegistry.hookClassLoader());
		Class<?> classMethodHookInit = hookClassLoader.loadClass("com.lizi.skyright.MethodHookInit");
		Class<?> classHookRegistry = hookClassLoader.loadClass("com.lizi.skyright.HookRegistry");
		Constructor<?> conMethodHookInit = classMethodHookInit.getConstructor(classHookRegistry);
		Constructor<?> conHookRegistry = classHookRegistry.getConstructor(ClassLoader.class);
		dynamicHookRegistry = conHookRegistry.newInstance(hookClassLoader);
		conMethodHookInit.newInstance(dynamicHookRegistry);
	}

}
