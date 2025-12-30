package com.lizi.skyright;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ApplicationIcon {

    private static ApplicationIcon applicationIcon;
    private Map<String,Drawable> map;
    private PackageManager pm;
    private SystemServerManager server;
    private ApplicationIcon(Context context) {
        this.map = new HashMap<>();
        this.pm = context.getPackageManager();
        this.server = SystemServerManager.getManagerInstance();
    }

    public static void initApplicationIcon(Context context) {
        if (applicationIcon == null) {
            applicationIcon = new ApplicationIcon(context);
            applicationIcon.new LoadIconThread().start();
        }
    }
    
    public static void newLoadIcon(){
        applicationIcon.new LoadIconThread().start();
    }

    public static Drawable getPackageIcon(String packageName) {
        if (applicationIcon.map.containsKey(packageName)) {
            return applicationIcon.map.get(packageName);
        } else {
            Drawable icon = applicationIcon.server.getApplicationInfo(packageName).loadIcon(applicationIcon.pm);
            applicationIcon.map.put(packageName, icon);
            return icon;
        }
    }

    private class LoadIconThread extends Thread {
        @Override
        public void run() {
            boolean b = server.isInitService();
            if (b) {
                List<String> packages = server.getPackageNames();
                int size = packages.size();
                for (int i = 0; i < size; i++) {
                    String pkg = packages.get(i);
                    Drawable icon = server.getApplicationInfo(pkg).loadIcon(pm);
                    map.put(pkg, icon);
                }
            }
        }
    }

}
