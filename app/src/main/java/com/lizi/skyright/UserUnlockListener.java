package com.lizi.skyright;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedHelpers;

public class UserUnlockListener extends XC_MethodHook {

    private MethodHookInit methodHookInit;
    private XC_MethodHook.Unhook unlockCallback;
    private static UserUnlockListener userUnlockListener;

    public UserUnlockListener(MethodHookInit methodHookInit) {
        this.methodHookInit = methodHookInit;
    }

    public static void initUnlockListener(MethodHookInit methodHookInit) {
        if (userUnlockListener == null) {
            userUnlockListener = new UserUnlockListener(methodHookInit);
            userUnlockListener.unlockCallback = XposedHelpers.findAndHookMethod("com.android.server.SystemServiceManager", methodHookInit.getHookRegistry().getSystemClassLoader(), "onUserUnlocked", int.class, userUnlockListener);
        }
    }

    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        unlockCallback.unhook();
        methodHookInit.unlockCallback();
    }
}
