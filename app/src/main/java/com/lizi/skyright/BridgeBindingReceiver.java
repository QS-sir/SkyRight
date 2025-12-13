package com.lizi.skyright;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;
import com.lizi.skyright.service.IBridgeManager;

public class BridgeBindingReceiver extends BroadcastReceiver implements ServiceConnection {

    public static final String BRIDGE_ACTION = "com.lizi.skyright.intent.action.BRIDGING_SERVICE";
    private HookRegistry hookRegistry;
    private Context context;
    private SystemServerManagerImpl systemServerManagerImpl;

    public BridgeBindingReceiver(HookRegistry hookRegistry,SystemServerManagerImpl systemServerManagerImpl) {
        this.hookRegistry = hookRegistry;
        this.context = hookRegistry.getContext();
        this.systemServerManagerImpl = systemServerManagerImpl;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BRIDGE_ACTION.equals(intent.getAction())) {
            bindService();
        }
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setClassName("com.lizi.skyright", "com.lizi.skyright.BridgeService");
		hookRegistry.getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        IBridgeManager bridgeManager = IBridgeManager.Stub.asInterface(iBinder);
        try {
            bridgeManager.attachClientBinder(systemServerManagerImpl);
        } catch (RemoteException e) {
            Toast.makeText(context, "服务桥接失败", Toast.LENGTH_SHORT).show();
		}
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Toast.makeText(context, "服务桥接已断开", Toast.LENGTH_SHORT).show();
    }

}
