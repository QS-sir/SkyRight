package com.lizi.skyright;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

public class ManageSubWindowDialog extends BaseDialog implements DialogInterface.OnShowListener,CompoundButton.OnCheckedChangeListener {

    private SystemServerManager systemServerManager;
    private PackageManager pm;
    private ApplicationInfo applicationInfo;
    private String packageName;
    private ImageView imageView;
    private TextView appName,appPackageName;
    private Switch monitorPackageSubwindow;
    private ListView activitys;
    private LinearLayout layout;
    private EditText searchInput;
    private ActivityInfo activityInfo[];
    private Set<String> monitorPackageList;
    private Map<String,Set<String>> monitorActivityList;


    public ManageSubWindowDialog(Context context) {
        super(context, R.layout.manage_subwindow_dialog);
        this.systemServerManager = SystemServerManager.getManagerInstance();
        this.pm = context.getPackageManager();
    }

    @Override
    protected void init() {
        getWindow().setWindowAnimations(R.style.dialog_anim);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setWindowSize(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        setOnShowListener(this);
        initViews();
    }

    private void initViews() {
        imageView = findViewById(R.id.managesubwindowdialogImageView1);
        appName = findViewById(R.id.managesubwindowdialogTextView1);
        appPackageName = findViewById(R.id.managesubwindowdialogTextView2);
        monitorPackageSubwindow = findViewById(R.id.managesubwindowdialogSwitch1);
        activitys = findViewById(R.id.managesubwindowdialogListView1);
        searchInput = findViewById(R.id.managesubwindowdialogEditText1);
        layout = findViewById(R.id.managesubwindowdialogLinearLayout1);
        monitorPackageSubwindow.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean p) {
        if (compoundButton.isPressed()) {
            if (applicationInfo.uid <= 2000) {
                Toast.makeText(getContext(), "系统核心应用不能操作", Toast.LENGTH_SHORT).show();
                compoundButton.setChecked(false);
                return;
            }
            systemServerManager.setMonitorPackagesSubWindow(packageName, p);
            if (p) {
                layout.setVisibility(View.GONE);
            } else {
                layout.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onShow(DialogInterface dialogInterface) {
        if (applicationInfo != null && (!applicationInfo.packageName.equals(packageName)||packageName == null)) {
            updateData();
            packageName = applicationInfo.packageName;
            activityInfo = systemServerManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities;
            boolean isMonitorPackageSubwindow = monitorPackageList.contains(packageName);
            boolean isHide = applicationInfo.uid < 2000 || packageName.equals(getContext().getPackageName());
            if (isHide || isMonitorPackageSubwindow) {
                layout.setVisibility(View.GONE);
            } else {
                layout.setVisibility(View.VISIBLE);
            }
            imageView.setBackground(applicationInfo.loadIcon(pm));
            appName.setText(applicationInfo.loadLabel(pm));
            appPackageName.setText(packageName);
            monitorPackageSubwindow.setChecked(isMonitorPackageSubwindow);
        }
    }

    private void updateData() {
        String data = systemServerManager.getStorageData();
        monitorPackageList = JsonParser.getListData(data, SystemServerManagerImpl.MONITOR_PACKAGE_SUBWINDOW);
        monitorActivityList = JsonParser.getMapSet(data, SystemServerManagerImpl.MONITOR_ACTIVITY_SUBWINDOW);
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


    public void show(ApplicationInfo applicationInfo) {
        this.applicationInfo = applicationInfo;
        super.show();
    }

}
