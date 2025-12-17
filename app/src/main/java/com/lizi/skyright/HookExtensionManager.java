package com.lizi.skyright;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import android.content.pm.PackageManager.NameNotFoundException;
import java.lang.reflect.Method;

public class HookExtensionManager {

    public static final String TAG = "HookExtensionManager";
    private Map<String, List<XC_MethodHook.Unhook>> hookRegistry = new HashMap<>();
    private Map<String,Object> objs = new HashMap<>();
    private PackageManager packageManager;
    private ClassLoader hostClassLoader;
    private String jsonData;

    public HookExtensionManager(Context context) {
        this.packageManager = context.getPackageManager();
        this.hostClassLoader = context.getClassLoader();
    }

    /**
     * 初始化配置
     */
    public void init(String jsonData) {
        this.jsonData = jsonData;
    }

    public void init() {
        try {
            JSONObject json = new JSONObject(jsonData).getJSONObject("expand_hook_packages");
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String packageName = keys.next();
                boolean isEnabled = json.getBoolean(packageName);
                if (isEnabled) {
                    new PluginLoader(packageName);
                }
            }
        } catch (Exception e) {
            LogManager.log(TAG, " init error:" + e.toString());
        }
    }

    public void checkExpandPackageIsUnInstall(String packageName) {
        if (hookRegistry != null && hookRegistry.containsKey(packageName)) {
            unhook(packageName);
            XposedBridge.log("ExpandPackage : " + packageName + " Has been uninstalled , clear ExpandPackage hook");
        }
    }
    
    public void checkIsCoverPackage(String packageName){
        if (hookRegistry != null && hookRegistry.containsKey(packageName)){
            unhook(packageName);
            new PluginLoader(packageName);
            XposedBridge.log("ExpandPackage : " + packageName + " cover installed , new ExpandPackage hook");
        }
    }
    
    /**
     * 动态开启或关闭指定包的Hook
     */
    public void setEnabledHookPackage(String packageName, boolean enable) {
        if (enable) {
            new PluginLoader(packageName);
        } else {
            unhook(packageName);
        }
        XposedBridge.log("ExpandPackage : " + packageName + "  Open ：" + enable);
    }

    /**
     * 解注册所有Hook
     */
    public void unhookAll() {
        unhook(null);
    }

    /**
     * 解注册指定包或全部的Hook
     */
    public void unhook(String targetPackage) {
        // 如果 targetPackage 为 null，则解注册所有
        Collection<String> packagesToUnhook = (targetPackage == null) 
            ? hookRegistry.keySet() 
            : Collections.singleton(targetPackage);

        for (String pkg : packagesToUnhook) {
            List<XC_MethodHook.Unhook> unhookList = hookRegistry.get(pkg);
            XposedHelpers.callMethod(objs.get(pkg), "closeReleaseResources");
            if (unhookList != null) {
                objs.remove(pkg);
                for (XC_MethodHook.Unhook unhook : unhookList) {
                    unhook.unhook();
                }
                // 清理记录
                if (targetPackage != null) hookRegistry.remove(pkg);
            }
        }
    }
    
    

    /**
     * 通用的Hook回调包装类
     */
    private static class ExtensionHookCallback extends XC_MethodHook {

        private final Object callbackTarget;
        private final Class[] callbackParameterTypes;
        private boolean isRealizeAfter;
        private boolean isRealizeBefore;

        public ExtensionHookCallback(Object callbackTarget) {
            this.callbackTarget = callbackTarget;
            this.callbackParameterTypes = new Class[]{
                Object.class, Object[].class, Object.class, 
                String.class, Object.class};
            Class<?> cs = callbackTarget.getClass();
            Class<?> parameter = XposedHelpers.findClass("com.lizi.skyright.SkyrightMethodHookCallback$MethodHookParam", cs.getClassLoader());
            try {
                cs.getDeclaredMethod("afterHookedMethod",parameter);
                isRealizeAfter = true;
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {}
            
            try {
                cs.getDeclaredMethod("beforeHookedMethod",parameter);
                isRealizeBefore = true;
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {}
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            if (isRealizeAfter) {
                invokeCallback(param, true);
            }
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if (isRealizeBefore) {
                invokeCallback(param, false);
            }
        }

        private void invokeCallback(MethodHookParam param, boolean isAfter) {
            Object obj = XposedHelpers.callMethod(callbackTarget, "param", callbackParameterTypes, param, param.args, param.thisObject, param.method.getName(), param.getResult());
            if (isAfter) {  
                XposedHelpers.callMethod(callbackTarget, "afterHookedMethod", obj);
            } else {
                XposedHelpers.callMethod(callbackTarget, "beforeHookedMethod", obj);
            }
        }
    }

    /**
     * 插件加载器内部类
     */
    private class PluginLoader {
        
        private final String packageName;

        public PluginLoader(String packageName) {
            this.packageName = packageName;
            init();
        }

        /**
         * 初始化插件：加载类并实例化
         */
        public void init() {
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                Bundle metaData = appInfo.metaData;
                if (metaData == null) {
                    XposedBridge.log(TAG + " init error: no meta data");
                    return;
                }
                String hookInitClassName = metaData.getString("skyrightHook", "").trim();
                if (hookInitClassName.isEmpty()) {
                    XposedBridge.log(TAG + " init error: cannot find class in meta-data");
                    return;
                }

                // 使用插件的 APK 路径创建类加载器
                PathClassLoader pluginClassLoader = new PathClassLoader(appInfo.sourceDir, getClass().getClassLoader());

                Class<?> hookInitClass = pluginClassLoader.loadClass(hookInitClassName);
                Class<?> baseInterface = pluginClassLoader.loadClass("com.lizi.skyright.SkyrightHook");

                if (baseInterface.isAssignableFrom(hookInitClass)) {
                    Constructor<?> constructor = hookInitClass.getConstructor(Object.class);
                    Object obj = constructor.newInstance(this);
                    objs.put(packageName, obj);
                } else {
                    XposedBridge.log(TAG + " init error: not implement SkyrightHook");
                }
            } catch (Exception e) {
                XposedBridge.log(TAG + " init error: " + e.toString());
            }
        }

        public void log(String log) {
            XposedBridge.log(log);
        }

        /**
         * Hook 构造函数
         */
        public void findAndHookConstructor(String className, Class<?>... parameterTypes, Object callback) {
            if (invalidParams(className, callback)) return;
            hookMethod(HookType.CONSTRUCTOR, className, null, parameterTypes, callback);
        }

        /**
         * Hook 普通方法
         */
        public void findAndHookMethod(String className, String methodName, Class<?>...parameterTypes, Object callback) {
            if (invalidParams(className, methodName, callback)) return;
            hookMethod(HookType.METHOD, className, methodName, parameterTypes, callback);
        }

        // --- 私有辅助方法 ---

        /**
         * 通用的Hook执行逻辑
         */
        private void hookMethod(HookType type, String className, String methodName, 
                                Class<?>[] parameterTypes, Object callback) {
            try {
                ClassLoader callbackClassLoader = callback.getClass().getClassLoader();
                Class<?> hookCallbackInterface = callbackClassLoader.loadClass("com.lizi.skyright.SkyrightMethodHookCallback");
                Class<?> returnInterface = callbackClassLoader.loadClass("com.lizi.skyright.SkyrightMethodReturn");

                boolean isProperCallback = hookCallbackInterface.isAssignableFrom(callback.getClass());
                if (!isProperCallback) {
                    XposedBridge.log("PluginLoader error: callback not implement SkyrightMethodHookCallback");
                    return;
                }

                // 1. 先尝试从 Map 中获取
                List<XC_MethodHook.Unhook> unhookList = hookRegistry.get(packageName);
                // 2. 如果获取不到（为 null），则新建一个并放入 Map
                if (unhookList == null) {
                    unhookList = new ArrayList<>();
                    hookRegistry.put(packageName, unhookList);
                }

                XC_MethodHook.Unhook unhook = null;
                if (type == HookType.CONSTRUCTOR) {
                    Object hookArgs[] = Arrays.asList(parameterTypes).toArray();
                    Object obj[] = new Object[hookArgs.length + 1];
                    System.arraycopy(hookArgs, 0, obj, 0, hookArgs.length);
                    obj[obj.length - 1] = new ExtensionHookCallback(callback);
                    unhook = XposedHelpers.findAndHookConstructor(className, hostClassLoader, obj);
                } else {
                    Object hookArgs[] = Arrays.asList(parameterTypes).toArray();
                    Object obj[] = new Object[hookArgs.length + 1];
                    System.arraycopy(hookArgs, 0, obj, 0, hookArgs.length);
                    if (returnInterface.isAssignableFrom(callback.getClass())) {
                        obj[obj.length - 1] = XC_MethodReplacement.returnConstant(XposedHelpers.callMethod(callback, "returnConstant"));
                    } else {
                        obj[obj.length - 1] = new ExtensionHookCallback(callback);
                    }
                    unhook = XposedHelpers.findAndHookMethod(className, hostClassLoader, methodName, obj);
                }
                unhookList.add(unhook);

            } catch (Exception e) {
                XposedBridge.log("PluginLoader hook error: " + e.toString());
            }
        }

        private boolean invalidParams(String className, Object callback) {
            if (className == null || callback == null) {
                XposedBridge.log("PluginLoader parameter is null");
                return true;
            }
            return false;
        }

        private boolean invalidParams(String className, String methodName, Object callback) {
            if (invalidParams(className, callback) || methodName == null) {
                XposedBridge.log("PluginLoader parameter is null");
                return true;
            }
            return false;
        }
    }

    // 枚举 Hook 类型，用于区分构造函数和普通方法
    private enum HookType {
        CONSTRUCTOR, METHOD
        }
}

