package com.lizi.skyright;
import android.content.Context;
import android.os.Binder;
import android.view.WindowManager;
import de.robv.android.xposed.XC_MethodHook;

public class MonitorSubWindowManager extends XC_MethodHook implements Runnable {

    private Context context;
    private Context resourcesContext;
    private Object wms;

    public MonitorSubWindowManager(MethodHookInit methodHookInit) {
        this.context = methodHookInit.getHookRegistry().getContext();
        this.resourcesContext = methodHookInit.getMduleResourcesContext();
    }

    @Override
    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        int uid = Binder.getCallingUid();
        if (uid > 2000) {
            WindowManager.LayoutParams layout = (WindowManager.LayoutParams)param.args[2];
            int type = layout.type;
            if (type > 1 && type < 2000) {
                String packageName = layout.packageName;
                
            }
        }
    }



    public void run() {

    }

    private void removeWindow() {

    }

}
