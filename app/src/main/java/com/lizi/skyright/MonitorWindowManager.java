package com.lizi.skyright;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.view.IWindow;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import android.widget.Toast;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Region;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityCache;

public class MonitorWindowManager extends XC_MethodHook implements Runnable {

    private Context context;
    private Object accessibilityWindowManager;
    private Object accessibilityManagerService;
    private Handler handler;
    private volatile Map map;
    private volatile IWindow iWindow;
    private Object windowManagerInternal;
    private Object wms;
    private AccessibilityInteractionClient ac;
    int i;
    

    public MonitorWindowManager(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
        this.map = new HashMap();
         // XposedBridge.log(con.isUiContext()+""+context);
    }

    public void initAccessibilityWindowManager() {
        accessibilityManagerService = XposedHelpers.callMethod(AccessibilityManager.getInstance(context), "getServiceLocked");
        accessibilityWindowManager = XposedHelpers.getObjectField(accessibilityManagerService, "mA11yWindowManager");
        i = XposedHelpers.getIntField(XposedHelpers.callMethod(accessibilityManagerService,"getInteractionBridge"),"mConnectionId");
        ac =  AccessibilityInteractionClient.getInstance(context);
    }

    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        int uid = Binder.getCallingUid();
        if (uid > 10000 && uid != 10221 && uid != 10225 && uid != 10640 && (uid == 10457 || uid == 10315 || uid == 10486 || uid == 10372 || uid == 10319 || uid == 10320 || uid == 10343)) {
//            AccessibilityEvent e =  (AccessibilityEvent) param.args[0];
//            String st = (String) e.getPackageName();
//            if(st!=null&&!st.equals("com.android.systemui")&&!st.equals("com.android.shell")&&!st.equals("com.android.launcher")){
//                AccessibilityInteractionClient ac = AccessibilityInteractionClient.getInstance(context);
//                XposedBridge.log(i +"事件:"+ ac);
//            }
            // handler.postDelayed(this, 2000);
            if (wms == null) {
                wms = param.thisObject;
            }
            
           // XposedBridge.log("调用方法："+param.method.getName());
//            Object obj = XposedHelpers.callMethod(param.thisObject, "windowForClientLocked", param.args[0], param.args[1], false);
//            WindowManager.LayoutParams layout = (WindowManager.LayoutParams) XposedHelpers.getObjectField(obj, "mAttrs");
            //  boolean b = XposedHelpers.callMethod(obj,"isVisible");
            //  iWindow = (IWindow) param.args[1];
            //   XposedBridge.log(layout.type + "");
            // handler.postDelayed(this, 2000);
            Object obj = param.getResult();
            WindowManager.LayoutParams layout = (WindowManager.LayoutParams) XposedHelpers.getObjectField(obj,"mAttrs");
            boolean b = layout.type != WindowManager.LayoutParams.TYPE_BASE_APPLICATION && layout.type < 2000;
            if (b) {
                windowManagerInternal = obj;
             //   iWindow = (IWindow) param.args[1];
              //  map = (Map) XposedHelpers.getObjectField(param.thisObject, "mWindowMap");
                // Object obj = map.get(iWindow.asBinder());
//                Object obj = map.get(iWindow.asBinder());
//                if (obj != null) {
//                    XposedHelpers.callMethod(obj, "removeImmediately");
//                }
                //handler.postDelayed(this, 50);
                 XposedBridge.log("子窗口："+XposedHelpers.getObjectField(obj,"mKeepClearAreas"));
                // handler.post(this);
            } else {
                //XposedBridge.log("不是子窗口");
            }
            //XposedBridge.log("添加窗口："+layout);
        }
    }

    @Override
    public void run() {
        
        //AccessibilityWindowInfo aw = AccessibilityWindowInfo.obtain();
        //XposedBridge.log(aw+"");

//        if (accessibilityWindowManager != null) {   
//            // AccessibilityInteractionClient ac = AccessibilityInteractionClient.getInstance();
//            try {
//                int i = XposedHelpers.callMethod(accessibilityManagerService, "getAccessibilityWindowId", iWindow.asBinder()); 
//                XposedHelpers.callMethod(accessibilityWindowManager, "setAccessibilityFocusedWindowLocked", i);
//                //    AccessibilityWindowInfo aw = (AccessibilityWindowInfo) XposedHelpers.callMethod(accessibilityWindowManager, "findA11yWindowInfoByIdLocked",i);
//                //   Object obh = XposedHelpers.callMethod(accessibilityManagerService, "getInteractionBridge");
//                // Object o = XposedHelpers.getObjectField(accessibilityManagerService,"mSecurityPolicy");
//                // String str[] = (String[]) XposedHelpers.callMethod(o,"computeValidReportedPackages",context.getPackageName(),1000);
//                AccessibilityWindowInfo aw = (AccessibilityWindowInfo) XposedHelpers.callMethod(accessibilityWindowManager, "findA11yWindowInfoByIdLocked", i);
//                XposedBridge.log(i + "" + XposedHelpers.getObjectField(wms,"mViewServer"));
//            } catch (Exception e) {
//                XposedBridge.log(e.toString());  
//            }
//
//        }
        try {
            AccessibilityNodeInfo ani = ac.getRootInActiveWindow(i,4);
            XposedBridge.log(ani+"");
            ani.recycle();
            //Object obj = map.get(iWindow.asBinder());
            //int id = XposedHelpers.callMethod(accessibilityManagerService, "getAccessibilityWindowId", iWindow.asBinder());
            //XposedHelpers.callMethod(windowManagerInternal,"removeImmediately");
            Toast.makeText(context, "拦截子窗口", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            XposedBridge.log(e.toString());
        }

    }
    
   


    private void removeWindow() {

    }
}
