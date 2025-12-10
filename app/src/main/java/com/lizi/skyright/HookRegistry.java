package com.lizi.skyright;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.util.List;
import java.util.ArrayList;

public class HookRegistry {

	private List<XC_MethodHook.Unhook> methodHookList;
	private ClassLoader baseClassLoader;
	private ClassLoader hookClassLoader;
	private Context context;
	private boolean isDynamicHook;
	private ReleaseResources releaseResources;

	public HookRegistry(ClassLoader hookClassLoader) {
		this.methodHookList = new ArrayList<>();
		this.baseClassLoader = ActivityManager.getService().getClass().getClassLoader();
		this.hookClassLoader = hookClassLoader;
		this.context = ActivityThread.currentActivityThread().getApplication();
	}

	public void setReleaseResources(ReleaseResources releaseResources) {
		this.releaseResources = releaseResources;
	}

	public void setIsDynamicHook(boolean isDynamicHook) {
		this.isDynamicHook = isDynamicHook;
	}

	public boolean isDynamicHook() {
		return isDynamicHook;
	}

	public ClassLoader baseClassLoader() {
		return baseClassLoader;
	}

	public ClassLoader hookClassLoader() {
		return hookClassLoader;
	}

	public ClassLoader xposedClassLoader() {
		return XposedBridge.BOOTCLASSLOADER;
	}

	public Context getContext() {
		return context;
	}

	public final void addMethodHook(XC_MethodHook.Unhook methodHook) {
		methodHookList.add(methodHook);
	}

	public final void releaseMethodHook() throws Exception {
		int size = methodHookList.size();
		if (size == 0) {
			return;
		}
		for (XC_MethodHook.Unhook hook : methodHookList) {
			hook.unhook();
		}
		
		if (releaseResources != null) {
			releaseResources.releaseResources();
		}
	}



	public static interface ReleaseResources {
		void releaseResources() throws Exception;
	}

}
