package com.lizi.skyright;

public class MethodHookInit implements HookRegistry.ResourceReleasable {
    
	private HookRegistry hookRegistry;

	public MethodHookInit(HookRegistry hookRegistry) {
		this.hookRegistry = hookRegistry;
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
