package com.lizi.skyright;
import android.view.WindowManager;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class RemoveWindowSecureFlags extends XC_MethodHook {

    @Override
    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        WindowManager.LayoutParams layout = (WindowManager.LayoutParams) param.args[2];
        if (layout != null && layout.flags != 0) {
            int flags = layout.flags;
            boolean b = (flags & WindowManager.LayoutParams.FLAG_SECURE) != 0;
            if(b){
                layout.flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
            }
        }
    }


}
