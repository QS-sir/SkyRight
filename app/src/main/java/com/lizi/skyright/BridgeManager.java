package com.lizi.skyright;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import com.lizi.skyright.service.IBridgeManager;

public class BridgeManager extends IBridgeManager.Stub implements Runnable {

    private IBinder binder;
    private Handler handler;
    private ConnectionCallback connectionCallback;
    private static volatile BridgeManager instance;

    private BridgeManager() {
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    public static BridgeManager getInstance() {
        if (instance == null) {
            // 同步代码块：确保线程安全
            synchronized (BridgeManager.class) {
                // 第二层检查：防止多个线程同时通过第一层检查后重复创建
                if (instance == null) {
                    instance = new BridgeManager();
                }
            }
        }
        return instance;
    }
    

    @Override
    public void attachClientBinder(IBinder clientBinder)throws RemoteException {
        this.binder = clientBinder;
        if (connectionCallback != null) {
            handler.post(this);
        }
    }

    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    @Override
    public void run() {
        ConnectionCallback callback = connectionCallback;
        if (callback != null) {
            callback.onConnected();
        }
    }

    public IBinder getClientBinder() {
        return binder;
    }

    public static interface ConnectionCallback {
        void onConnected();
    }

}
