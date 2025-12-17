package com.lizi.skyright;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataUpdateCallback {
    void updateAllData(String data);
    void updateModifyStartActivityPackages(Map<String, String> data);
    void updateMonitorPackagesActivity(Set<String> data);
    void updateMonitorActivitys(Map<String,Map<String,String>> data);
    void updatePackagesHideAccessibility(Set<String> data);
    void updateWhiteListPackages(Set<String> data);
    void setPauseAllHook(boolean b);
    void setOneplusHideRootStatus(boolean b);
    void setEnabledHookPackage(String packageName, boolean enable);
}
