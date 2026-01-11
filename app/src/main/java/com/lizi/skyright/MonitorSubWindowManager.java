package com.lizi.skyright;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.view.IWindow;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class MonitorSubWindowManager extends XC_MethodHook implements Runnable,DataUpdateListener {

    public static final String TAG = "MonitorSubWindowManager";
    private MethodHookInit methodHookInit;
    private Handler handler;
    private final Context context;
    private final Object wms;
    private volatile Set<String> monitorPackagesSubWindow;
    private volatile Map<String,Set<String>> monitorActivitySubWindow;
    private volatile boolean isRemoveWindowSecureFlags = false;
    private volatile Object windowState;
    private volatile IWindow mWindow;
    private Map map;
    private InterceptSubWindowRequestCallback interceptSubWindowRequestCallback;
    private ActivityChangedListener activityChangedListener;
    private volatile long showTime;

    public MonitorSubWindowManager(MethodHookInit methodHookInit) {
        this.methodHookInit = methodHookInit;
        this.handler = new Handler(Looper.getMainLooper());
        this.context = methodHookInit.getHookRegistry().getContext();
        this.monitorPackagesSubWindow = new HashSet<>();
        this.monitorActivitySubWindow = new HashMap<>();
        this.wms = XposedHelpers.getObjectField(ActivityManager.getService(), "mWindowManager");
        this.map = (Map) XposedHelpers.getObjectField(wms, "mWindowMap");
    }

    public void registerRelayoutWindowCallback() {
        HookRegistry hookRegistry = methodHookInit.getHookRegistry();
        Class<?> cs = XposedHelpers.findClass("com.android.server.wm.WindowManagerService", context.getClassLoader());
        hookRegistry.hookAllMethods(cs, "relayoutWindow", new RelayoutWindowCallback());
    }

    public void setRemoveWindowSecureFlags(boolean b) {
        this.isRemoveWindowSecureFlags = b;
    }

    public void setActivityChangedListener(ActivityChangedListener activityChangedListener) {
        this.activityChangedListener = activityChangedListener;
    }

    public void initRequestWindow() {
        Context resourcesContext = methodHookInit.getModuleResourcesContext();
        interceptSubWindowRequestCallback = new InterceptSubWindowRequestCallback(context, View.inflate(resourcesContext, R.layout.subwindow_request_layout, null));
    }


    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        int uid = Binder.getCallingUid();
        WindowManager.LayoutParams layout = (WindowManager.LayoutParams)param.args[2];
        if (uid <= 2000 || layout == null || interceptSubWindowRequestCallback == null) {
            return;
        }
        int type = layout.type;
        if (type != WindowManager.LayoutParams.TYPE_BASE_APPLICATION && type != WindowManager.LayoutParams.TYPE_APPLICATION_STARTING && type < 2000) {
            String packageName = layout.packageName;
            if ((monitorPackagesSubWindow != null && monitorActivitySubWindow != null) && (monitorPackagesSubWindow.contains(packageName) || monitorActivitySubWindow.containsKey(packageName))) {
                monitorSubWindow(param, packageName);
            }
        }
    }

    private void monitorSubWindow(XC_MethodHook.MethodHookParam param, String packageName) {
        if (monitorActivitySubWindow.containsKey(packageName)) {
            monitorActivitySubWindow(param, monitorActivitySubWindow.get(packageName));
        } else {
            monitorSubWindow(param);
        }
    }

    private void monitorActivitySubWindow(XC_MethodHook.MethodHookParam param, Set<String> set) {
        IWindow window = (IWindow) param.args[1];
        Object windowState = map.get(window.asBinder());
        String currentActivity = getCurrentActivity(windowState);
        if (set.contains(currentActivity)) {
            monitorSubWindow(param);
        }
    }


    private void monitorSubWindow(XC_MethodHook.MethodHookParam param) {
        if (!interceptSubWindowRequestCallback.isShowing()) {
            mWindow = (IWindow) param.args[1];
            windowState = map.get(mWindow.asBinder());
            handler.post(this);
        } else {
            mWindow = (IWindow) param.args[1];
            windowState = map.get(mWindow.asBinder());
        }
        setSubwindowVisibility(false);
    }

    private String getCurrentActivity(Object windowState) {
        if (windowState != null) {
            Object obj = XposedHelpers.getObjectField(windowState, "mActivityRecord");
            Intent intent = (Intent) XposedHelpers.getObjectField(obj, "intent");
            return intent != null ? intent.getComponent().getClassName() : null;
        }
        return null;
    }


    private void setSubwindowVisibility(boolean b) {
        if (mWindow != null) {
            try {
                mWindow.dispatchAppVisibility(b);
            } catch (RemoteException e) {
                XposedBridge.log(TAG + " setSubwindowVisibility error:" + e.toString());
            }
        }
    }


    @Override
    public void run() {
        showTime = System.currentTimeMillis();
        interceptSubWindowRequestCallback.show();
    }

    @Override
    public void dataUpdate(String key, JSONObject data) {
        if (key.equals(SystemServerManagerImpl.MONITOR_PACKAGE_SUBWINDOW)) {
            monitorPackagesSubWindow = JsonParser.getListData(data, key);
        } else if (key.equals(SystemServerManagerImpl.MONITOR_ACTIVITY_SUBWINDOW)) {
            monitorActivitySubWindow = JsonParser.getMapSet(data, key);
        }
    }

    public void completeRemoveWindow() {
        if (windowState != null) {
            XposedHelpers.callMethod(windowState, "removeImmediately");
        }
    }

    private class InterceptSubWindowRequestCallback extends BaseSmallFloatWindow implements View.OnClickListener,BaseSmallFloatWindow.OnDismissListener {

        private TextView remove,ignore,show;

        public InterceptSubWindowRequestCallback(Context context, View layoutView) {
            super(context, layoutView, 5000);
        }

        @Override
        protected void onCreate() {
            setOnDismissListener(this);
            remove = findViewById(R.id.subwindowrequestlayoutTextView1);
            ignore = findViewById(R.id.subwindowrequestlayoutTextView2);
            show = findViewById(R.id.subwindowrequestlayoutTextView3);
            show.setOnClickListener(this);
            ignore.setOnClickListener(this);
            remove.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == show) {
                setSubwindowVisibility(true);
            } else if (view == remove) {
                completeRemoveWindow();
            }
            dismiss();
        }

        @Override
        public void onDismiss(boolean b) {
            mWindow = null;
            windowState = null;
        }
    }


    private class RelayoutWindowCallback extends XC_MethodHook implements Runnable {

        private Object currentActivityRecord;

        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            WindowManager.LayoutParams layout = (WindowManager.LayoutParams) param.args[2];
            if (layout == null) {
                return;
            }

            if (layout.flags != 0 && isRemoveWindowSecureFlags) {
                int flags = layout.flags;
                boolean b = (flags & WindowManager.LayoutParams.FLAG_SECURE) != 0;
                if (b) {
                    layout.flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                }
            }

            int type = layout.type;
            IWindow window = (IWindow) param.args[1];
            if (type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION) {
                onActivityChangedListener(window);
            }

            if (windowState != null && window != null && interceptSubWindowRequestCallback != null && interceptSubWindowRequestCallback.isShowing() && System.currentTimeMillis() - showTime > 300L) {
                currentActivityRecord = XposedHelpers.getObjectField(map.get(window.asBinder()), "mActivityRecord");
                Object obj2 = XposedHelpers.getObjectField(windowState, "mActivityRecord");
                if (currentActivityRecord != null && !currentActivityRecord.equals(obj2) || param.args[5] == View.GONE) {
                    handler.post(this);
                }
            }
        }

        private void onActivityChangedListener(IWindow window) {
            if (activityChangedListener != null) {
                Object obj = XposedHelpers.getObjectField(map.get(window.asBinder()), "mActivityRecord");
                if (obj != null) {
                    if (currentActivityRecord == null) {
                        onActivityChanged(obj);
                    } else if (!obj.equals(currentActivityRecord)) {
                        onActivityChanged(obj);
                        currentActivityRecord = obj;
                    }
                }
            }
        }

        private void onActivityChanged(Object activityRecord) {
            Intent intent = (Intent) XposedHelpers.getObjectField(activityRecord, "intent");
            if (intent != null) {
                ComponentName cn = intent.getComponent();
                String packageName = cn.getPackageName();
                String className = cn.getClassName();
                activityChangedListener.onActivityChanged(packageName, className);
            }
        }


        @Override
        public void run() {
            interceptSubWindowRequestCallback.dismiss();
        }
    }

    public static interface ActivityChangedListener {
        void onActivityChanged(String packageName, String className);
    }

}
