package com.lizi.skyright;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import android.os.Binder;
import android.os.UserManager;

public class HookActivityTaskManagerService extends XC_MethodHook {
    
    private HookRegistry hookRegistry;

	public HookActivityTaskManagerService(HookRegistry hookRegistry) {
		this.hookRegistry = hookRegistry;
	}

	@Override
	protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
		XposedBridge.log("调用者："+Binder.getCallingUid()+"   "+Binder.getCallingPid());
	}
	
    
}
