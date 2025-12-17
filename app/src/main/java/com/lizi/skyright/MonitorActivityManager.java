package com.lizi.skyright;
import de.robv.android.xposed.XC_MethodHook;
import java.util.Map;
import java.util.Set;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import android.os.Binder;
import de.robv.android.xposed.XposedBridge;

public class MonitorActivityManager extends XC_MethodHook {

    private HookRegistry hookRegistry;
    private Map<String, String> modifyStartActivityPackages;
    private Set<String> monitorPackagesActivity;
    private Map<String, Map<String, String>> monitorActivitys;
    private Set<String> whiteListPackages;
    private long origId;

	public MonitorActivityManager(HookRegistry hookRegistry) {
		this.hookRegistry = hookRegistry;
	}

	@Override
	protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        int uid = Binder.getCallingUid();
        if (uid == 10333) {
            origId = Binder.clearCallingIdentity();
        }
	}

    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        if (origId != 0) {
            Binder.restoreCallingIdentity(origId);
            origId = 0;
            XposedBridge.log("触发后勾法");
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
    
}
