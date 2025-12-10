package com.lizi.skyright;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import android.os.Binder;

public class HookActivityTaskManagerService extends XC_MethodHook {
    
    private HookRegistry hookRegistry;

	public HookActivityTaskManagerService(HookRegistry hookRegistry) {
		this.hookRegistry = hookRegistry;
	}

	@Override
	protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
		XposedBridge.log("test hook "+Binder.getCallingUid());
	}
	
    
}
