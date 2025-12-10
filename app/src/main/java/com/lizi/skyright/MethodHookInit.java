package com.lizi.skyright;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class MethodHookInit implements HookRegistry.ResourceReleasable {
    
	private HookRegistry hookRegistry;
	private HookActivityTaskManagerService hookActivityTaskManagerService;

	public MethodHookInit(HookRegistry hookRegistry) {
		this.hookRegistry = hookRegistry;
		initHook();
	}
	
	private void initHook(){
		hookActivityTaskManagerService = new HookActivityTaskManagerService(hookRegistry);
	}
    
	public void initMethodHook(){
		methodHook();
	}
	
	public void initDynamicMethodHook(){
		hookRegistry.setDynamic(true);
		hookRegistry.setResourceReleasable(this);
		methodHook();
	}
	
	private void methodHook(){
		
	}
	
	
	@Override
	public void onRelease() throws Exception {
		
	}
    
}
