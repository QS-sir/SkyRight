package com.lizi.skyright;

import android.content.Context;
import android.widget.LinearLayout;
import android.view.WindowManager;

public class BaseSmallFloatWindow {
    
    private WindowManager windowManager;
    private Context context;
    private LinearLayout layout;
    private WindowManager.LayoutParams windowParams;

    public BaseSmallFloatWindow(Context context) {
        this.context = context;
        this.windowManager = context.getSystemService(WindowManager.class);
        this.layout = new LinearLayout(context);
        initWindowParams();
    }
    
    private void initWindowParams(){
        windowParams = new WindowManager.LayoutParams();
    }
    
}
