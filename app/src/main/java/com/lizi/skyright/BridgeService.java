package com.lizi.skyright;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BridgeService extends Service {
    
    @Override
    public IBinder onBind(Intent intent) {
        return BridgeManager.getInstance();
    }
    
}
