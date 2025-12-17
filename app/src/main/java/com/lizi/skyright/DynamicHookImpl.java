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
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class DynamicHookImpl extends XC_MethodHook {

	private HookRegistry hookRegistry;
	private ClassLoader systemClassLoader;
	private ClassLoader moduleClassLoader;
	private Context context;
	private PackageManager pm;
    private MethodHookInit methodHookInit;
	private volatile Object dynamicHookRegistry;
    private Object dynamicHook;

    public DynamicHookImpl(ClassLoader moduleClassLoader) {
		this.hookRegistry = new HookRegistry(moduleClassLoader);
		this.systemClassLoader = hookRegistry.getSystemClassLoader();
		this.moduleClassLoader = hookRegistry.getModuleClassLoader();
		this.context = hookRegistry.getContext();
		this.pm = context.getPackageManager();
	}

	@Override
	protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        String name = param.method.getName();
        if (name.equals("handleMessage")) {
            Message m = (Message) param.args[0];
            if (m != null && m.obj != null) {
                dynamicHook(m.obj.toString());
            }
        } else if (name.equals("setWindowManager")) {
            initDynamicHook();
            //初始化完毕就不需要此hook，解除该hook
            XposedBridge.unhookMethod(param.method,this);
        }
	} 

    private void dynamicHook(String packageName) throws Exception {
        if (hookRegistry.isDynamic()) {
            if ("com.lizi.skyright".equals(packageName)) {
                Object hook = dynamicHookRegistry;
                XposedHelpers.callMethod(hook, "unhookAll");
                XposedBridge.log("new init dynamic hook");
                String apkPath = pm.getApplicationInfo("com.lizi.skyright", 0).sourceDir;
                PathClassLoader hookClassLoader = new PathClassLoader(apkPath, XposedBridge.BOOTCLASSLOADER);
                Class<?> classMethodHookInit = hookClassLoader.loadClass("com.lizi.skyright.MethodHookInit");
                Class<?> classHookRegistry = hookClassLoader.loadClass("com.lizi.skyright.HookRegistry");
                Constructor<?> conMethodHookInit = classMethodHookInit.getConstructor(classHookRegistry);
                Constructor<?> conHookRegistry = classHookRegistry.getConstructor(ClassLoader.class);
                dynamicHookRegistry = conHookRegistry.newInstance(moduleClassLoader);
                dynamicHook = conMethodHookInit.newInstance(dynamicHookRegistry);
                XposedHelpers.callMethod(dynamicHook, "initDynamicMethodHook");
            } 
            XposedHelpers.callMethod(dynamicHook, "checkIsCoverPackage", packageName);
        } else {
            methodHookInit.checkIsCoverPackage(packageName);
        }
    }


	//初始化动态hook
	private void initDynamicHook() throws Exception {
		String apkPath = pm.getApplicationInfo("com.lizi.skyright", 0).sourceDir;
		PathClassLoader hookClassLoader = new PathClassLoader(apkPath, XposedBridge.BOOTCLASSLOADER);
        try {
            Class<?> classMethodHookInit = hookClassLoader.loadClass("com.lizi.skyright.MethodHookInit");
            Class<?> classHookRegistry = hookClassLoader.loadClass("com.lizi.skyright.HookRegistry");
            Constructor<?> conMethodHookInit = classMethodHookInit.getConstructor(classHookRegistry);
            Constructor<?> conHookRegistry = classHookRegistry.getConstructor(ClassLoader.class); 
            Object hook =  conHookRegistry.newInstance(moduleClassLoader);
            dynamicHookRegistry = hook;
            dynamicHook = conMethodHookInit.newInstance(hook);
            XposedHelpers.callMethod(dynamicHook, "initDynamicMethodHook");
            hookRegistry.setDynamic(true);
        } catch (Exception e) {
            methodHookInit = new MethodHookInit(hookRegistry);
            methodHookInit.initMethodHook();
        }
        XposedHelpers.findAndHookMethod("com.android.server.pm.PackageHandler", systemClassLoader, "handleMessage", Message.class, this);
	}
}
