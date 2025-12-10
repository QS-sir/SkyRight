package com.lizi.skyright;

import android.app.ActivityManager;
import android.os.Bundle;
import android.os.IBinder;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookInit implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)) {
			XposedHelpers.findAndHookMethod(
				MainActivity.class.getName(),
				lpparam.classLoader,
				"isModuleActivated",
				XC_MethodReplacement.returnConstant(true));
		}else if("com.android.providers.settings".equals(lpparam.packageName)){
			XposedHelpers.findAndHookMethod("com.android.server.wm.WindowManagerService",ActivityManager.getService().getClass().getClassLoader(), "addWindowToken",IBinder.class,int.class,int.class,Bundle.class,new DynamicHookImpl(lpparam.classLoader));
		}
		
	}

}
