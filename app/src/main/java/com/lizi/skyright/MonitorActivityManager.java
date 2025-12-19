package com.lizi.skyright;

import android.app.ActivityManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class MonitorActivityManager extends XC_MethodHook {

    public static final String TAG = "MonitorActivityManager";
    private HookRegistry hookRegistry;
    private Context context;
    private PackageManager packageManager;
    private Map<String, String> modifyStartActivityPackages;
    private Set<String> monitorPackagesActivity;
    private Map<String, Map<String, String>> monitorActivitys;
    private Set<String> whiteListPackages;
    private String launcherPackageName;
    private ActivityManager activityManager;
    private Handler handler;
    private long origId = 0;
    private ShowRequestDialog showRequestDialog;
    private ActivityRequestDialog activityRequestDialog;
    private Object atmsObject;

	public MonitorActivityManager(HookRegistry hookRegistry) {
		this.hookRegistry = hookRegistry;
        this.context = hookRegistry.getContext();
        this.packageManager = context.getPackageManager();
        this.activityManager = context.getSystemService(ActivityManager.class);
        this.launcherPackageName = getLauncherPackageName();
        this.handler = new Handler(Looper.getMainLooper());
        this.showRequestDialog = new ShowRequestDialog();
        initActivityRequestDialog();
	}

    private void initActivityRequestDialog() {
        try {
            Context pluginContext = context.createPackageContext("com.lizi.skyright", Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            View view = LayoutInflater.from(pluginContext).inflate(R.layout.test, null);
           // activityRequestDialog = new ActivityRequestDialog(context, view, this);
        } catch (Exception e) {
            XposedBridge.log(TAG + " initActivityRequestDialog error: " + e.toString());
        }
    }

    public void startActivity() {
        showRequestDialog.startActivity();
    }

	@Override
	protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        int uid = Binder.getCallingUid();
        if (uid > 2000 && activityRequestDialog != null) {
            Object pkg = param.args[1];
            Intent intent = (Intent) param.args[3];
            if (pkg != null && intent != null) {
                if (atmsObject == null) {
                    atmsObject = param.thisObject;
                }
                if (whiteListPackages != null && whiteListPackages.contains(pkg)) {
                    param.args[1] = "android";
                    origId = Binder.clearCallingIdentity();
                    return;
                }
                ComponentName con = intent.getComponent();
                if (pkg.equals(launcherPackageName)) {
                    launcherStartActivitySolve(pkg.toString(), intent);
                } else if (con != null && whiteListPackages != null && whiteListPackages.contains(con.getPackageName())) {
                    param.args[1] = "android";
                    origId = Binder.clearCallingIdentity();
                } else if (con != null && monitorActivitys != null && monitorActivitys.containsKey(con.getPackageName())) {
                    otherRequestStartMonitorActivity(intent, param);
                } else if (monitorPackagesActivity != null && monitorPackagesActivity.contains(pkg)) {
                    monitorPackageActivity(intent, param);
                } else if (con != null && monitorActivitys != null && monitorActivitys.containsKey(pkg)) {
                    Map<String,String> activityList = monitorActivitys.get(pkg);
                    String startActivity = con.getClassName();
                    if (activityList.containsKey(startActivity)) {
                        String action = activityList.get(startActivity);
                        if (action.equals(ActivityRequestDialog.REQUEST_ALWAYS_REFUSE)) {
                            param.setResult(0);
                        } else if (action.equals(ActivityRequestDialog.REQUEST_ASK)) {
                            param.setResult(0);
                            ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
                            String act = null;
                            if (cn != null) {
                                act = cn.getClassName();
                            }
                            if (activityRequestDialog.isShowing()) {
                                return;
                            }
                            handler.post(showRequestDialog.setParameter(param.args, intent, act, ActivityRequestDialog.REQUEST_START_MONITOT_ACTIVITY));
                        }
                    }
                }
            }
        }
	}

    private void launcherStartActivitySolve(String pkg, Intent intent) {
        origId = Binder.clearCallingIdentity();
        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return;
        }
        String startPackage = componentName.getPackageName();
        if (modifyStartActivityPackages.containsKey(startPackage)) {
            intent.setClassName(startPackage, modifyStartActivityPackages.get(startPackage));
        }
    }

    private void otherRequestStartMonitorActivity(Intent intent, XC_MethodHook.MethodHookParam param) {
        ComponentName con = intent.getComponent();
        Map<String,String> activityList = monitorActivitys.get(con.getPackageName());
        String startAct = con.getClassName();
        if (activityList.containsKey(startAct)) {
            param.setResult(0);
            ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
            String act = null;
            if (cn != null) {
                act = cn.getClassName();
            }
            if (activityRequestDialog.isShowing()) {
                return;
            }
            handler.post(showRequestDialog.setParameter(param.args, intent, act, ActivityRequestDialog.REQUEST_START_MONITOT_ACTIVITY));
        }
    }

    private void monitorPackageActivity(Intent intent, XC_MethodHook.MethodHookParam param) {
        param.setResult(0);
        ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
        String act = null;
        if (cn != null) {
            act = cn.getClassName();
        }
        if (activityRequestDialog.isShowing()) {
            return;
        }
        handler.post(showRequestDialog.setParameter(param.args, intent, act, ActivityRequestDialog.MONITOT_ALL_ACTIVITY));
    }

    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        if (origId != 0) {
            Binder.restoreCallingIdentity(origId);
            origId = 0;
        }

    }

    public void updateModifyStartActivityPackages(Map<String, String> data) {
        this.modifyStartActivityPackages = data;
    }


    public void updateMonitorPackagesActivity(Set<String> data) {
        this.monitorPackagesActivity = data;
    }


    public void updateMonitorActivitys(Map<String, Map<String, String>> data) {
        this.monitorActivitys = data;
    }

    public void updateWhiteListPackages(Set<String> data) {
        this.whiteListPackages = data;
    }

    private String getLauncherPackageName() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = packageManager.resolveActivity(intent, 0);
        if (res == null) {
            return null;
        }
        ComponentInfo conInfo = res.getComponentInfo();
        if (conInfo == null) {
            return null;
        }
        ApplicationInfo app = conInfo.applicationInfo;
        if (app != null) {
            return app.packageName;
        }
        return "";
    }

    private class ShowRequestDialog implements Runnable {

        private volatile Object obj[];
        private volatile Intent intent;
        private volatile String presentActivity;
        private volatile int requestType;
        private Method md;
        private Object clas[] = {IApplicationThread.class,String.class,String.class,Intent.class,String.class,IBinder.class,String.class,
            int.class,int.class,ProfilerInfo.class,Bundle.class,int.class,boolean.class};

        public ShowRequestDialog() {
            Class<?> cla = XposedHelpers.findClass("com.android.server.wm.ActivityTaskManagerService", hookRegistry.getSystemClassLoader());
            md = XposedHelpers.findMethodExact(cla, "startActivityAsUser", clas);
        }
        @Override
        public void run() {
            activityRequestDialog.show(intent, presentActivity, requestType);
        }

        public Runnable setParameter(Object obj[], Intent intent, String presentActivity, int requestType) {
            this.obj = obj;
            this.intent = intent;
            this.presentActivity = presentActivity;
            this.requestType = requestType;
            return this;
        }

        public void startActivity() {
            try {
                md.invoke(atmsObject, obj);
            }  catch (Exception e) {
                XposedBridge.log(TAG + " startActivity  error:" + e.toString());
            }
        }
    }
}
