package com.lizi.skyright;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Binder;
import android.provider.Settings;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;

public class HideAccessibilityStatusManager extends XC_MethodHook implements DataUpdateListener {

    private Context context;
    private ActivityManager am;
    private volatile Set<String> packageHideAccessibilityList;

    public HideAccessibilityStatusManager(Context context) {
        this.context = context;
        this.am = context.getSystemService(ActivityManager.class);
    }

    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (uid <= 2000) {
            return;
        }
        if (packageHideAccessibilityList == null || packageHideAccessibilityList.isEmpty()) {
            return ;
		}
        String packageName = getPackageNameByPid(pid);
        if (!packageName.isEmpty() && packageHideAccessibilityList.contains(packageName)) {
            Member m = param.method;
            if (m.getName().equals("getSecureSetting")) {
                Object object = param.getResult();
                if (object != null) {
                    Object name = XposedHelpers.getObjectField(object, "name");
                    if (name != null && name.equals(Settings.Secure.ACCESSIBILITY_ENABLED)) {
                        XposedHelpers.setObjectField(object, "value", "0");
                    } else if (name != null && name.equals(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)) {
                        XposedHelpers.setObjectField(object, "value", "");
                    }
                }
            } else if (m.getName().equals("addClient")) {
                param.setResult(0L);
            } else if (m.getName().equals("getEnabledAccessibilityServiceList")) {
                param.setResult(new ArrayList<AccessibilityServiceInfo>());
            }
        }
    }

    
    private String getPackageNameByPid(int pid) {
        String packageName = "";
        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo app : appList) {
            if (app.uid <= 2000) {
                continue;
            }
            if (app.pid == pid) {
                packageName = app.pkgList[0];
                break;
            }
		}
        return packageName;
	}

    @Override
    public void dataUpdate(String key, JSONObject data) {
        this.packageHideAccessibilityList = JsonParser.getListData(data,key);
    }

}
