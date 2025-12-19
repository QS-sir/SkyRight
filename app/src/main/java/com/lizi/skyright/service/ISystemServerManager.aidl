package com.lizi.skyright.service;

interface ISystemServerManager{
    String getStorageData();
    List<String> getServiceLoges();
    void setPauseAllHook(boolean b);
    boolean isPauseAllHook();
    List<ApplicationInfo> getInstalledApplications(int flags);
    List<String> getPackageNames();
    PackageInfo getPackageInfo(String packageName,int flags);
    ApplicationInfo getApplicationInfo(String packageName,int flags);
    boolean getOneplusHideRootStatus();
    void setOneplusHideRootStatus(boolean b);
    void setEnabledHookPackage(String packageName, boolean enable);
    boolean isEnabledHookPackage(String packagekName);
    boolean isDynamicHook();
    void setPackageHideAccessibilityStatus(String packageName,boolean b);
    int getParcelSize();
    void setPackageWhiteList(String packageName,boolean b);
    void setMonitorPackageActivity(String packageName,boolean b);
    void setMonitorActivity(String packageName,String activityName,String action);
    void setModifyPackageStartActivity(String packageName,String activityName);
    String getPackageLaunchActivityName(String packageName);
}

