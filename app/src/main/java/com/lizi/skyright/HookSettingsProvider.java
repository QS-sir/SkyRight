package com.lizi.skyright;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import android.os.Binder;

public class HookSettingsProvider extends XC_MethodHook {

    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        //XposedBridge.log("  "+param.args[0]);
    }
    
    
}
