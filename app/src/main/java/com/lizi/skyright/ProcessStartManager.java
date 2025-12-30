package com.lizi.skyright;
import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import android.os.Binder;

public class ProcessStartManager extends XC_MethodHook {

    private Context context;

    public ProcessStartManager(Context context) {
        this.context = context;
    }

    @Override
    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        String str = (String) param.args[0];
        XposedBridge.log("启动：" + str);
        
    }


}
