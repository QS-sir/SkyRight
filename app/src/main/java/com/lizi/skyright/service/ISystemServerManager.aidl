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
    void setMonitorPackagesSubWindow(String packageName,boolean b);
    void setMonitorActivitySubWindow(String packageName,String className,boolean b);
    String getPackageLaunchActivityName(String packageName);
    boolean isEnabledRebootProtect();
    void setEnabledRebootProtect(boolean b);
    boolean isRemoveWindowSecureFlags();
    void setRemoveWindowSecureFlags(boolean b);
}

