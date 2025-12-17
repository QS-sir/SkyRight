package com.lizi.skyright;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements BridgeManager.ConnectionCallback {

    private TextView phoneInfo;
    private AdditionalFunctionDialog additionalFunctionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isTaskRoot()) {
            finish();
            return;
		}
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!isModuleActivated()) {
            setActionBarText(getResources().getString(R.string.xposed_unactivated));
        } else {
            setActionBarText(getResources().getString(R.string.xposed_activated) + "\t系统服务未连接");
		}
        initConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "模块运行日志");
        menu.add(2, 2, 2, "活动监控日志");
		menu.add(3, 3, 3, "附加功能");
        menu.add(4, 4, 4, "模块扩展");
        menu.add(5, 5, 5, "日志");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!SystemServerManager.getManagerInstance().isInitService()) {
            Toast.makeText(getApplication(), "服务管理器未初始化不可用", Toast.LENGTH_SHORT).show();
            return false;
        }
        switch (item.getItemId()) {
            case 1:

                break;
            case 2:

                break;
            case 3:
                additionalFunctionDialog.show();
                break;
            case 4:
                startActivity(new Intent(this, HookExtensionActivity.class));
                break;
            case 5:
                showLoges();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showLoges() {
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("标题")
            .setMessage("" + LogManager.getAllLogs())
            .setPositiveButton(android.R.string.ok, null)
            .create();
        dialog.show();
    }

    private void initConnected() {
        BridgeManager.getInstance().setConnectionCallback(this);
        Intent intent = new Intent().setAction(BridgeBindingReceiver.BRIDGE_ACTION);
		sendBroadcast(intent);
    }

    private void initViews() {
        phoneInfo = findViewById(R.id.activitymainTextView1);
        phoneInfo.setText(getInfo());
    }

    private String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("品牌：" + Build.BRAND);
        sb.append("\n型号：" + Build.MODEL);
        sb.append("\n安卓版本：" + Build.VERSION.RELEASE);
        sb.append("\n内核版本：" + System.getProperty("os.version") + "\r");
        boolean b = SystemServerManager.getManagerInstance().isDynamicHook();
        sb.append("\n模块模式：" + (b ? "动态模式" : "静态模式"));
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
        initViews();
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
