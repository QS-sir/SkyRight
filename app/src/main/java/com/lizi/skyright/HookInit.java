package com.lizi.skyright;

import android.app.ActivityManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import android.app.ActivityThread;

public class HookInit implements IXposedHookLoadPackage{

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)) {
			XposedHelpers.findAndHookMethod(
				MainActivity.class.getName(),
				lpparam.classLoader,
				"isModuleActivated",
				XC_MethodReplacement.returnConstant(true));
		}else if("com.android.providers.settings".equals(lpparam.packageName)){
            ClassLoader cs = ActivityThread.currentApplication().getClassLoader();
            Class css = XposedHelpers.findClass("com.android.server.wm.WindowManagerService",cs);
			XposedHelpers.findAndHookMethod("com.android.server.am.ActivityManagerService",cs, "setWindowManager",css,new DynamicHookImpl(lpparam.classLoader));
		}
		
	}

}
