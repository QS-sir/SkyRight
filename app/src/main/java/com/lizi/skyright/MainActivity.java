package com.lizi.skyright;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.TextView;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements BridgeManager.ConnectionCallback {

    private TextView phoneInfo;
    private AdditionalFunctionDialog additionalFunctionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!isModuleActivated()) {
            setActionBarText(getResources().getString(R.string.xposed_unactivated));
        } else {
            setActionBarText(getResources().getString(R.string.xposed_activated) + "\t系统服务未连接");
		}
        initConnected();
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "模块运行日志");
        menu.add(2, 2, 2, "活动监控日志");
		menu.add(3, 3, 3, "附加功能");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!SystemServerManager.getManagerInstance().isInitService()){
            Toast.makeText(getApplication(), "服务管理器未初始化不可用", Toast.LENGTH_SHORT).show();
            return false;
        }
        switch(item.getItemId()){
            case 1:
                
                break;
            case 2:

                break;
            case 3:
                additionalFunctionDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    

    private void initConnected() {
        BridgeManager.getInstance().setConnectionCallback(this);
        Intent intent = new Intent().setAction(BridgeBindingReceiver.BRIDGE_ACTION);
		sendBroadcast(intent);
    }

    private void initViews() {
        phoneInfo = findViewById(R.id.activitymainTextView1);
        phoneInfo.setText(getPhoneInfo());
    }

    private String getPhoneInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("品牌：" + Build.BRAND);
        sb.append("\n型号：" + Build.MODEL);
        sb.append("\n安卓版本：" + Build.VERSION.RELEASE);
        sb.append("\n内核版本：" + System.getProperty("os.version")+"\r");
        return sb.toString();
    }

    @Override
    public void onConnected() {
        setActionBarText(getResources().getString(R.string.xposed_activated) + "\t系统服务已连接");
        init();
        Toast.makeText(getApplication(), "系统服务已连接", Toast.LENGTH_SHORT).show();
    }

    private void init() {
        additionalFunctionDialog = new AdditionalFunctionDialog(this);
    }

    private void setActionBarText(String string) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(string);
        }
    }


    public static boolean isModuleActivated() {
        return false;
    }
}
