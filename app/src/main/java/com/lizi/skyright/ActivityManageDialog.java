package com.lizi.skyright;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Map;
import java.util.Set;

public class ActivityManageDialog extends BaseDialog implements TextWatcher,View.OnClickListener,DialogInterface.OnShowListener,CompoundButton.OnCheckedChangeListener {

    private SystemServerManager systemServerManager;
    private PackageManager pm;
    private String packageName;
    private ApplicationInfo applicationInfo;
    private ImageView imageView;
    private TextView appName,appPackageName,startActivityName;
    private Switch whiteList,monitorPackage;
    private TextView modifyStartActivity;
    private ListView activitys;
    private LinearLayout layout;
    private ActivityInfo activityInfo[];
    private boolean isExistStartActivity;
    private EditText searchInput;
    private String launcherPackageName;
    private Set<String> whiteListPackages,monitorPackagesActivity;
    private Map<String,String> startActivitys;
    private MonitorActivityListAdapter activityListAdapter;
    private ActivityListDialog activityListDialog;

    public ActivityManageDialog(Context context) {
        super(context, R.layout.activity_manage_dialog);
        this.systemServerManager = SystemServerManager.getManagerInstance();
        this.pm = context.getPackageManager();
        this.launcherPackageName = getLauncherPackageName();
        this.activityListDialog = new ActivityListDialog(context);
    }

    @Override
    protected void init() {
        getWindow().setWindowAnimations(R.style.dialog_anim);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setWindowSize(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        activityListAdapter = new MonitorActivityListAdapter(getContext());
        setOnShowListener(this);
        initViews();
    }

    private void initViews() {
        imageView = findViewById(R.id.activitymanagedialogImageView1);
        appName = findViewById(R.id.activitymanagedialogTextView1);
        appPackageName = findViewById(R.id.activitymanagedialogTextView2);
        startActivityName = findViewById(R.id.activitymanagedialogTextView3);
        whiteList = findViewById(R.id.activitymanagedialogSwitch1);
        monitorPackage = findViewById(R.id.activitymanagedialogSwitch2);
        modifyStartActivity = findViewById(R.id.activitymanagedialogTextView4);
        activitys = findViewById(R.id.activitymanagedialogListView1);
        searchInput = findViewById(R.id.activitymanagedialogEditText1);
        layout = findViewById(R.id.activitymanagedialogLinearLayout1);
        modifyStartActivity.setOnClickListener(this);
        activitys.setAdapter(activityListAdapter);
        whiteList.setOnCheckedChangeListener(this);
        monitorPackage.setOnCheckedChangeListener(this);
        searchInput.addTextChangedListener(this);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        activityListAdapter.notifyDataSetChanged(editable.toString());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int p, int p1, int p2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int p, int p1, int p2) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (searchInput == null) {
                return super.onKeyDown(keyCode, event);
            }
            if (searchInput.getText() != null && searchInput.getText().length() > 0) {
                searchInput.setText(null);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void onClick(View view) {
        if (whiteListPackages.contains(packageName)) {
            Toast.makeText(getContext(), "该应用是白名单，不能修改", Toast.LENGTH_SHORT).show();
        } else if (packageName.equals(launcherPackageName)) {
            Toast.makeText(getContext(), "该应用为系统桌面，不能修改", Toast.LENGTH_SHORT).show();
        } else if (applicationInfo.uid <= 2000) {
            Toast.makeText(getContext(), "该应用为系统核心应用，不能修改", Toast.LENGTH_SHORT).show();
        } else if (packageName.equals("com.lizi.skyright")) {
            Toast.makeText(getContext(), "主程序不能修改……", Toast.LENGTH_SHORT).show();
        } else if (!isExistStartActivity) {
            Toast.makeText(getContext(), "该应用没有入口活动，不能修改", Toast.LENGTH_SHORT).show();
        } else {
            activityListDialog.show(activityInfo);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean p) {
        if (compoundButton.isPressed()) {
            if (compoundButton == whiteList) {
                systemServerManager.setPackageWhiteList(packageName, p);
                monitorPackage.setEnabled(!p);
            } else if (compoundButton == monitorPackage) {
                systemServerManager.setMonitorPackageActivity(packageName, p);
                whiteList.setEnabled(!p);
            }
        }
        if (p && layout.getVisibility() != View.GONE) {
            layout.setVisibility(View.GONE);
        } else if (!p && layout.getVisibility() != View.VISIBLE) {
            layout.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onShow(DialogInterface dialogInterface) {
        //activitys.removeCallbacks(this);
        if (applicationInfo == null || !applicationInfo.packageName.equals(packageName)) {
            whiteListPackages = JsonParser.getListData(systemServerManager.getStorageData(), SystemServerManagerImpl.WHITE_LIST_PACKAGES);
            monitorPackagesActivity = JsonParser.getListData(systemServerManager.getStorageData(), SystemServerManagerImpl.MONITOR_PACKAGES_ACTIVITY);
            startActivitys = JsonParser.getMapStringData(systemServerManager.getStorageData(), SystemServerManagerImpl.MODIFY_PACKAGES_START_ACTIVITY);
            packageName = applicationInfo.packageName;
            imageView.setBackground(applicationInfo.loadIcon(pm));
            appName.setText(applicationInfo.loadLabel(pm));
            appPackageName.setText(packageName);
            String start = systemServerManager.getPackageLaunchActivityName(packageName);
            isExistStartActivity = start != null && !start.equals("");
            if (isExistStartActivity) {
                if (startActivitys.containsKey(packageName)) {
                    startActivityName.setText(startActivitys.get(packageName));
                } else {
                    startActivityName.setText(start);
                }
            } else {
                startActivityName.setText("无");
            }
            boolean isWhiteList = whiteListPackages.contains(packageName);
            whiteList.setChecked(isWhiteList);
            boolean isMonitor = monitorPackagesActivity.contains(packageName);
            monitorPackage.setChecked(isMonitor);
            
            PackageInfo appinfo = pm.getPackageArchiveInfo(applicationInfo.sourceDir, PackageManager.GET_ACTIVITIES);
            activityInfo = appinfo.activities;
            activityListAdapter.notifyDataSetChanged(activityInfo);
            int uid = applicationInfo.uid;
            boolean isHide = isWhiteList || isMonitor || uid <= 2000 || packageName.equals(launcherPackageName) || packageName.equals("com.lizi.skyright");
            if (isHide && layout.getVisibility() != View.GONE) {
                layout.setVisibility(View.GONE);
                
            } else if (!isHide && layout.getVisibility() != View.VISIBLE) {
                layout.setVisibility(View.VISIBLE);
            }
            boolean isSystem = uid <= 2000 || packageName.equals(launcherPackageName) || packageName.equals("com.lizi.skyright");
            if(isSystem){
                whiteList.setEnabled(false);
                monitorPackage.setEnabled(false);
            }else{
                if(isWhiteList){
                    monitorPackage.setEnabled(false);
                }else if(isMonitor){
                    whiteList.setEnabled(false);
                }else{
                    monitorPackage.setEnabled(true);
                    whiteList.setEnabled(true);
                }
            }
            
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && isShowing()) {
            update();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private void update() {
        whiteListPackages = JsonParser.getListData(systemServerManager.getStorageData(), SystemServerManagerImpl.WHITE_LIST_PACKAGES);
        monitorPackagesActivity = JsonParser.getListData(systemServerManager.getStorageData(), SystemServerManagerImpl.MONITOR_PACKAGES_ACTIVITY);
        startActivitys = JsonParser.getMapStringData(systemServerManager.getStorageData(), SystemServerManagerImpl.MODIFY_PACKAGES_START_ACTIVITY);
        String start = systemServerManager.getPackageLaunchActivityName(packageName);
        isExistStartActivity = start != null && !start.equals("");
        if (isExistStartActivity) {
            if (startActivitys.containsKey(packageName)) {
                startActivityName.setText(startActivitys.get(packageName));
            } else {
                startActivityName.setText(start);
            }
        } else {
            startActivityName.setText("无");
        }
        searchInput.setText(null);
    }

    public void show(ApplicationInfo applicationInfo) {
        this.applicationInfo = applicationInfo;
        super.show();
    }

    private String getLauncherPackageName() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = pm.resolveActivity(intent, 0);
        if (res == null) {
            return null;
        }
        ActivityInfo conInfo = res.activityInfo;
        if (conInfo == null) {
            return null;
        }
        ApplicationInfo app = conInfo.applicationInfo;
        if (app != null) {
            return app.packageName;
        }
        return "";
    }

}
