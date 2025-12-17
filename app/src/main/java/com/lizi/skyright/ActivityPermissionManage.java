package com.lizi.skyright;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

public class ActivityPermissionManage extends Activity implements TextWatcher,Runnable {
    
    private ListView appList;
    private EditText searchInput;
    private PackageListAdapter packageListAdapter;
    private Handler handler;
    private String search;
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;
    private View view;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_manage);
        init();
    }
    
    private void init(){
        handler = new Handler();
        windowManager = getWindowManager();
        windowParams = new WindowManager.LayoutParams();
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        windowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        windowParams.gravity = Gravity.CENTER;
        windowParams.dimAmount = 0.5f;
        windowParams.format = PixelFormat.TRANSLUCENT;
        initViews();
    }
    
    private void initViews(){
        view = View.inflate(this,R.layout.load_interface,null);
        view.setVisibility(View.GONE);
        appList = findViewById(R.id.activitypermissionmanageListView1);
        searchInput = findViewById(R.id.activitypermissionmanageEditText1);
        packageListAdapter = new PackageListAdapter(this);
        appList.setAdapter(packageListAdapter);
        searchInput.addTextChangedListener(this);
        windowManager.addView(view,windowParams);
    }

    @Override
    public void run() {
        showLoadWindow();
        packageListAdapter.notifyDataSetChanged(search);
    }
    
    @Override
    public void afterTextChanged(Editable editable) {
        search = editable.toString();
        handler.removeCallbacks(this);
        handler.postDelayed(this,600);
    }
    
    private void showLoadWindow(){
        view.setVisibility(View.VISIBLE);
        windowManager.updateViewLayout(view,windowParams);
    }
    
    public void hideLoadWindow(){
        view.setVisibility(View.GONE);
        windowManager.updateViewLayout(view,windowParams);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int p, int p1, int p2) {
        
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int p, int p1, int p2) {
       
    }
    
    
}
