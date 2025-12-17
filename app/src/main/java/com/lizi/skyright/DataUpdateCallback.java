package com.lizi.skyright;

import java.util.List;
import java.util.Map;

public interface DataUpdateCallback {
    void updateAllData(String data);
    void updateModifyStartActivityPackages(Map<String, String> data);
    void updateMonitorPackagesActivity(List<String> data);
    void updateMonitorActivitys(Map<String,Map<String,String>> data);
    void updateHideAccessibilityPackages(List<String> data);
    void setPauseAllHook(boolean b);
    void setOneplusHideRootStatus(boolean b);
    void setEnabledHookPackage(String packageName, boolean enable);
}
