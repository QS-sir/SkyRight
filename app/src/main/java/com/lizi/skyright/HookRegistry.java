package com.lizi.skyright;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.util.ArrayList;
import java.util.List;
import de.robv.android.xposed.XposedHelpers;

public final class HookRegistry {

	//1. 保存hook方法，用于取消hook
    private final List<XC_MethodHook.Unhook> hooks = new ArrayList<>();

    // 2. 系统/框架层的 ClassLoader (由 ActivityManager.getService() 获取)
    private final ClassLoader systemClassLoader;

    // 3. 模块的 ClassLoader
    private final ClassLoader moduleClassLoader;

    // 4. 系统 Context
    private final Context context;

    // 5. 是否为动态 Hook (无需重启)
    private boolean dynamic;

    // 6. 资源释放回调
    private ResourceReleasable resourceReleasable;

    public HookRegistry(ClassLoader moduleClassLoader) {
        this.moduleClassLoader = moduleClassLoader;
        this.systemClassLoader = ActivityManager.getService().getClass().getClassLoader();
        this.context = ActivityThread.currentActivityThread().getApplication();
    }

    /**
     * 设置资源释放回调
     */
    public void setResourceReleasable(ResourceReleasable releasable) {
        this.resourceReleasable = releasable;
    }

    /**
     * 设置是否为动态 Hook 模式
     */
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    /**
     * 判断是否为动态 Hook
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * 获取系统框架层 ClassLoader
     */
    public ClassLoader getSystemClassLoader() {
        return systemClassLoader;
    }

    /**
     * 获取模块 ClassLoader
     */
    public ClassLoader getModuleClassLoader() {
        return moduleClassLoader;
    }

	/**
     * 获取 Xposed ClassLoader
     */
    public ClassLoader getXposedClassLoader() {
        return XposedBridge.BOOTCLASSLOADER;
    }

    /**
     * 获取宿主应用上下文
     */
    public Context getContext() {
        return context;
    }
    
    
    
    public void findAndHookConstructor(Class<?> clazz,Object... parameterTypesAndCallback){
        addMethodHook(XposedHelpers.findAndHookConstructor(clazz,parameterTypesAndCallback));
    }
    
    public void findAndHookConstructor(String className, ClassLoader classLoader, Object... parameterTypesAndCallback){
        addMethodHook(XposedHelpers.findAndHookConstructor(className,classLoader,parameterTypesAndCallback));
    }
    
    public void findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        addMethodHook(XposedHelpers.findAndHookMethod(className,classLoader, methodName, parameterTypesAndCallback));
    }

    public void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        addMethodHook(XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback));
    }

    
    /**
     * 添加 Method Hook
     */
    private void addMethodHook(XC_MethodHook.Unhook hook) {
		//只有动态hook才能添加对象
		if (dynamic) {
			hooks.add(hook);
		}
    }

    /**
     * 取消注册所有 Hook 并释放相关资源
     */

    public void unhookAll() throws Exception {
        if (resourceReleasable != null) {
            resourceReleasable.onRelease();
        }
        if (hooks.isEmpty()) {
            return;
        }
        // 遍历并取消所有 Hook
        for (XC_MethodHook.Unhook hook : hooks) {
            hook.unhook();
        }
        hooks.clear(); // 清空列表，防止内存泄漏或重复操作
    }

    /**
     * 资源释放回调接口
     */
    public interface ResourceReleasable {
        void onRelease() throws Exception;
    }
}

