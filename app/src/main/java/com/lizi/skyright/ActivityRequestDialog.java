package com.lizi.skyright;

import android.app.ActivityTaskManager;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ActivityRequestDialog extends BaseFloatDialog implements BaseFloatDialog.OnShowListener,View.OnClickListener,Runnable {

    public static final String REQUEST_ASK = "ask_about";
    public static final String REQUEST_ALWAYS_REFUSE = "always_refuse";
    public static final String REQUEST_IGNORE = "ignore";

    public static final int MONITOT_ALL_ACTIVITY = 10;
    public static final int REQUEST_START_MONITOT_ACTIVITY = 20;
	public static final int MONITOT_ACTIVITY_REQUEST_START_OTHER = 30;

    private final Object atmsObject;
    private Intent intent;
    private String presentActivity;
    private int requestType;
    private ClipboardManager clipboardManager;
    private Handler handler;
    private MonitorActivityManager monitorActivityManager;

    private TextView title,presentAct,startActivity,intentText;
    private Button alwaysRefuse,refuse,agreeOnce;

    public ActivityRequestDialog(Context context, View layouView, MonitorActivityManager monitorActivityManager) {
        super(context, layouView, WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        this.monitorActivityManager = monitorActivityManager;
        this.atmsObject = ActivityTaskManager.getService();
        this.handler = new Handler(Looper.getMainLooper());
        this.clipboardManager = context.getSystemService(ClipboardManager.class);
    }

    @Override
    public void onCreate() {
        title = findViewById(R.id.activityrequestdialogTextView1);
        presentAct = findViewById(R.id.activityrequestdialogTextView2);
        startActivity = findViewById(R.id.activityrequestdialogTextView3);
        intentText = findViewById(R.id.activityrequestdialogTextView4);
        alwaysRefuse = findViewById(R.id.activityrequestdialogButton1);
        refuse = findViewById(R.id.activityrequestdialogButton2);
        agreeOnce = findViewById(R.id.activityrequestdialogButton3);
        setOnShowListener(this);
        alwaysRefuse.setOnClickListener(this);
        refuse.setOnClickListener(this);
        agreeOnce.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == alwaysRefuse) {
            
        } else if (view == agreeOnce) {
            monitorActivityManager.startActivity();
        }
        dismiss();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        handler.removeCallbacks(this);
    }

    @Override
    public void run() {
        dismiss();
    }

    @Override
    public void onShow() {
        if (presentActivity != null) {
            presentAct.setText(presentActivity);
        } else {
            presentAct.setText("未知");
		}
        ComponentName com = intent.getComponent();
        if (com != null) {
            startActivity.setText(com.getClassName());
        } else {
            startActivity.setText("无");
        }
        if (requestType == MONITOT_ALL_ACTIVITY) {
            if (alwaysRefuse.getVisibility() != View.INVISIBLE) {
                alwaysRefuse.setVisibility(View.INVISIBLE);
            }
			title.setText("应用申请意图");
        } else if (requestType == REQUEST_START_MONITOT_ACTIVITY) {
            if (alwaysRefuse.getVisibility() != View.VISIBLE) {
                alwaysRefuse.setVisibility(View.VISIBLE);
            }
			title.setText("请求打开被管控活动");
        } else if (requestType == MONITOT_ACTIVITY_REQUEST_START_OTHER) {
            if (alwaysRefuse.getVisibility() != View.VISIBLE) {
                alwaysRefuse.setVisibility(View.VISIBLE);
            }
			title.setText("受管控活动请求");
        }
        intentText.setText(intent.toString());
        handler.postDelayed(this, 5000);
    }

    public void show(Intent intent, String presentActivity, int requestType) {
        this.intent = intent;
        this.presentActivity = presentActivity;
        this.requestType = requestType;
        super.show();
    }

}
