package com.lizi.skyright;

import java.util.List;
import java.util.Map;

public interface DataUpdateCallback {
    
    void updataAllData(String data);
    void updataModifyStartActivityPackages(Map<String, String> data);
    void updataMonitorPackagesActivity(List<String> data);
    void updataMonitorActivitys(Map<String,Map<String,String>> data);
    void updataHideAccessibilityPackages(List<String> data);
    void setPauseAllHook(boolean b);
    void setOneplusHideRootStatus(boolean b);
}
