package com.lizi.skyright;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import de.robv.android.xposed.XC_MethodHook;

public class ProcessStartManager extends XC_MethodHook {

    private Context context;

    public ProcessStartManager(Context context) {
        this.context = context;
    }

    @Override
    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        String str = (String) param.args[0];
        if (str != null && str.equals("com.examhshsbzhsple.application")) {
          //  ApplicationInfo app = (ApplicationInfo) param.args[1];
            
            //param.args[0] = "com.android.settings";
            //app.processName = "com.android.settings";
         //   app.uid = 1000;
        }
    }


}
