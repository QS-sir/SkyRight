package com.lizi.skyright;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MonitorActivityManager extends XC_MethodHook implements DataUpdateListener {

    public static final String TAG = "MonitorActivityManager";
    private ClassLoader classLoader;
    private Context context;
    private PackageManager packageManager;
    private volatile Map<String, String> modifyStartActivityPackages;
    private volatile Set<String> monitorPackagesActivity;
    private volatile Map<String, Map<String, String>> monitorActivitys;
    private volatile Set<String> whiteListPackages;
    private volatile String launcherPackageName;
    private ActivityManager activityManager;
    private Handler handler;
    private long origId = 0;
    private InterceptActivityOperateCallback interceptActivityOperateCallback;
    private ActivityRequestDialog activityRequestDialog;
    private Object atmsObject;
    private MethodHookInit methodHookInit;
    private volatile String packageName;
    private SkipStartActivityPermissionCheck skipStartActivityPermission;

	public MonitorActivityManager(MethodHookInit methodHookInit) {
		this.context = methodHookInit.getHookRegistry().getContext();
        this.classLoader = context.getClassLoader();
        this.methodHookInit = methodHookInit;
        this.packageManager = context.getPackageManager();
        this.activityManager = context.getSystemService(ActivityManager.class);
        this.launcherPackageName = getLauncherPackageName();
        this.handler = new Handler(Looper.getMainLooper());
        this.interceptActivityOperateCallback = new InterceptActivityOperateCallback();
        this.atmsObject = ActivityTaskManager.getService();
        this.skipStartActivityPermission = new SkipStartActivityPermissionCheck();
	}

    @Override
    public void dataUpdate(String key, String data) {
        switch (key) {
            case SystemServerManagerImpl.MODIFY_PACKAGES_START_ACTIVITY:
                modifyStartActivityPackages = JsonParser.getMapStringData(data,key);
                break;
            case SystemServerManagerImpl.MONITOR_PACKAGES_ACTIVITY:
                monitorPackagesActivity = JsonParser.getListData(data,key);
                break;
            case SystemServerManagerImpl.MONITOR_ACTIVITYS:
                monitorActivitys = JsonParser.getMapData(data,key);
                break;
            case SystemServerManagerImpl.WHITE_LIST_PACKAGES:
                whiteListPackages = JsonParser.getListData(data,key);
                break;
        }
    }

    public void releaseDialogResources() {
        if (activityRequestDialog != null) {
            activityRequestDialog.releaseResources();
        }
    }

    public SkipStartActivityPermissionCheck getSkipStartActivityPermission() {
        return skipStartActivityPermission;
    }

    protected void initActivityRequestDialog() {
        Context res = methodHookInit.getMduleResourcesContext();
        if (res != null) {
            View view = LayoutInflater.from(res).inflate(R.layout.activity_request_dialog, null);
            activityRequestDialog = new ActivityRequestDialog(context, view, this);
        }
    }

    public void startActivity() {
        interceptActivityOperateCallback.startActivity();
    }

	@Override
	protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        int uid = Binder.getCallingUid();
        if (uid > 2000) {
            if (activityRequestDialog == null) {
                return;
            }
            packageName = (String) param.args[1];
            Intent intent = (Intent) param.args[3];
            if (packageName != null && intent != null) {
                if (whiteListPackages != null && whiteListPackages.contains(packageName)) {
                    whiteListStartActivity(intent, param);
                    return;
                }
                ComponentName componentName = intent.getComponent();
                if (packageName.equals(launcherPackageName)) {
                    if (componentName == null) {
                        return;
                    }
                    String startPackage = componentName.getPackageName();
                    if (modifyStartActivityPackages != null && modifyStartActivityPackages.containsKey(startPackage)) {
                        String at = modifyStartActivityPackages.get(startPackage);
                        intent.setClassName(startPackage, at);
                    }
                } else if (componentName != null) {
                    String componentPackageName = componentName.getPackageName();
                    if (whiteListPackages != null && whiteListPackages.contains(componentPackageName)) {
                        param.args[1] = "android";
                        origId = Binder.clearCallingIdentity();
                    } else if (monitorActivitys != null && monitorActivitys.containsKey(packageName)) {
                        monitorActivity(packageName, intent, componentName, param);
                    } else if (monitorActivitys != null && monitorActivitys.containsKey(componentPackageName)) {
                        otherRequestStartMonitorActivity(packageName, intent, param);
                    } else if (monitorPackagesActivity != null && monitorPackagesActivity.contains(packageName)) {
                        monitorPackageActivity(packageName, intent, param);
                    }
                } else if (monitorPackagesActivity != null && monitorPackagesActivity.contains(packageName)) {
                    monitorPackageActivity(packageName, intent, param);
                }
            }
        }
	}

    private void whiteListStartActivity(Intent intent, XC_MethodHook.MethodHookParam param) {
        param.args[1] = "android";
        origId = Binder.clearCallingIdentity();
        ComponentName componentName = intent.getComponent();
        if (componentName != null) {
            String startPackage = componentName.getPackageName();
            Intent it = packageManager.getLaunchIntentForPackage(startPackage);
            ComponentName component = it != null ? it.getComponent() : null;
            if (component != null) {
                String startClass = component.getClassName();
                if (startClass.equals(componentName.getClassName()) && modifyStartActivityPackages.containsKey(startPackage)) {
                    String at = modifyStartActivityPackages.get(startPackage);
                    intent.setClassName(startPackage, at);
                }
            }
        }
    }

    private void monitorActivity(String packageName, Intent intent, ComponentName componentName, XC_MethodHook.MethodHookParam param) {
        Map<String,String> activityList = monitorActivitys.get(packageName);
        String startActivity = componentName.getClassName();
        String presenActivity = getPresentActivity(packageName);
        if (activityList.containsKey(startActivity)) {
            String action = activityList.get(startActivity);
            if (action.equals(ActivityRequestDialog.REQUEST_ALWAYS_REFUSE)) {
                param.setResult(0);
                return;
            } else if (action.equals(ActivityRequestDialog.REQUEST_ASK)) {
                param.setResult(0);
                handler.post(interceptActivityOperateCallback.setParameter(packageName, param.args, intent, presenActivity, ActivityRequestDialog.REQUEST_START_MONITOT_ACTIVITY));
                return;
            }
        }
        if (presenActivity != null) {
            if (activityList.containsKey(presenActivity)) {
                param.setResult(0);
                handler.post(interceptActivityOperateCallback.setParameter(packageName, param.args, intent, presenActivity, ActivityRequestDialog.MONITOT_ACTIVITY_REQUEST_START_OTHER));
                return;
            }
        }

        if (monitorPackagesActivity != null && monitorPackagesActivity.contains(packageName)) {
            monitorPackageActivity(packageName, intent, param);
        }

    }

    private String getPresentActivity(String packageName) {
        if (packageName == null) {
            return null;
        }
        List<ActivityManager.RunningTaskInfo> list = activityManager.getRunningTasks(3);
        for (ActivityManager.RunningTaskInfo task :list) {
            ComponentName act = task.topActivity;
            if (act != null) {
                String pkg = act.getPackageName();
                if (pkg.equals(packageName)) {
                    return act.getClassName();
                }
            }
		}
        return null;
    }

    private void otherRequestStartMonitorActivity(String packageName, Intent intent, XC_MethodHook.MethodHookParam param) {
        ComponentName con = intent.getComponent();
        Map<String,String> activityList = monitorActivitys.get(con.getPackageName());
        String startAct = con.getClassName();
        if (activityList.containsKey(startAct)) {
            param.setResult(0);
            String act = getPresentActivity(packageName);
            if (activityRequestDialog.isShowing()) {
                return;
            }
            handler.post(interceptActivityOperateCallback.setParameter(packageName, param.args, intent, act, ActivityRequestDialog.REQUEST_START_MONITOT_ACTIVITY));
        }
    }

    private void monitorPackageActivity(String packageName, Intent intent, XC_MethodHook.MethodHookParam param) {
        param.setResult(0);
        String act = getPresentActivity(packageName);
        if (activityRequestDialog.isShowing()) {
            return;
        }
        handler.post(interceptActivityOperateCallback.setParameter(packageName, param.args, intent, act, ActivityRequestDialog.MONITOT_ALL_ACTIVITY));
    }

    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        if (origId != 0) {
            Binder.restoreCallingIdentity(origId);
            origId = 0;
        }

    }

    public void setRefuseActivityOperate() {
        interceptActivityOperateCallback.setRefuseActivityOperate();
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

    private class SkipStartActivityPermissionCheck extends XC_MethodHook {

        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            if (whiteListPackages != null && (whiteListPackages.contains(packageName) || launcherPackageName.equals(packageName))) {
                param.setResult(true);
            }
        }

    }


    private class InterceptActivityOperateCallback implements Runnable {

        private volatile Object obj[];
        private volatile Intent intent;
        private volatile String presentActivity;
        private volatile int requestType;
        private volatile String packageName;
        private Method md;
        private Object clas[] = {IApplicationThread.class,String.class,String.class,Intent.class,String.class,IBinder.class,String.class,
            int.class,int.class,ProfilerInfo.class,Bundle.class,int.class,boolean.class};

        public InterceptActivityOperateCallback() {
            Class<?> cla = XposedHelpers.findClass("com.android.server.wm.ActivityTaskManagerService", classLoader);
            md = XposedHelpers.findMethodExact(cla, "startActivityAsUser", clas);
        }
        @Override
        public void run() {
            activityRequestDialog.show(intent, presentActivity, requestType);
        }

        public Runnable setParameter(String packageName, Object obj[], Intent intent, String presentActivity, int requestType) {
            this.packageName = packageName;
            this.obj = obj;
            this.intent = intent;
            this.presentActivity = presentActivity;
            this.requestType = requestType;
            return this;
        }

        public void setRefuseActivityOperate() {
            ComponentName comp = intent.getComponent();
            if (comp != null) {
                String pkg = comp.getPackageName();
                String act = comp.getClassName();
                methodHookInit.setRefuseActivityOperate(pkg, act);
            }
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
