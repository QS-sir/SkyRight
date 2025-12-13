package com.lizi.skyright.service;

interface ISystemServerManager{
    String getStorageData();
    void setPauseAllHook(boolean b);
    boolean isPauseAllHook();
    List<ApplicationInfo> getInstalledApplications();
    List<String> getPackageNames();
    PackageInfo getPackageInfo(String packageName,int flags);
    ApplicationInfo getApplicationInfo(String packageName,int flags);
    boolean getOneplusHideRootStatus();
    void setOneplusHideRootStatus(boolean b);
}

