package com.lizi.skyright;

import de.robv.android.xposed.XC_MethodHook;
import java.util.List;
import java.util.ArrayList;

public abstract class DynamicHook {

	private List<XC_MethodHook.Unhook> hookList;

	public DynamicHook() {
		hookList = new ArrayList<>();
	}

	public abstract ClassLoader baseClassLoader();

	public abstract ClassLoader hookClassLoader();

	public abstract ClassLoader xposedClassLoader();

    public final void addMethodHook(XC_MethodHook.Unhook methodHook) {
		hookList.add(methodHook);
	}

	public final void releaseMethodHook() {
		int size = hookList.size();
		if (size == 0) {
			return;
		}
		for (XC_MethodHook.Unhook hook : hookList) {
			hook.unhook();
		}
	}

}
