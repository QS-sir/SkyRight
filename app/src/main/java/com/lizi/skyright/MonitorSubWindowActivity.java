package com.lizi.skyright;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.content.Context;

public class MonitorSubWindowActivity extends Activity implements OnItemClickListener,TextWatcher,Runnable,PackageListAdapter.HideLoadWindow {
    
    private ListView appList;
    private EditText searchInput;
    private PackageListAdapter packageListAdapter;
    private String search;
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;
    private View view;
    private ManageSubWindowDialog manageSubWindowDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.package_list_layout);
        init();
    }

    private void init() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        windowManager = getWindowManager();
        windowParams = new WindowManager.LayoutParams();
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        windowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        windowParams.gravity = Gravity.CENTER;
        windowParams.dimAmount = 0.5f;
        windowParams.format = PixelFormat.TRANSLUCENT;
        manageSubWindowDialog = new ManageSubWindowDialog(this);
        initViews();
    }

    private void initViews() {
        view = View.inflate(this, R.layout.load_interface, null);
        view.setVisibility(View.GONE);
        appList = findViewById(R.id.packagelistlayoutListView1);
        searchInput = findViewById(R.id.packagelistlayoutEditText1);
        packageListAdapter = new PackageListAdapter(this);
        appList.setAdapter(packageListAdapter);
        appList.setOnItemClickListener(this);
        searchInput.addTextChangedListener(this);
        windowManager.addView(view, windowParams);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int p, long p1) {
        manageSubWindowDialog.show(packageListAdapter.getItem(p));
    }

    @Override
    public void run() {
        showLoadWindow();
        packageListAdapter.notifyDataSetChanged(search);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        search = editable.toString();
        appList.removeCallbacks(this);
        if (search != null && !search.isEmpty()) {
            appList.postDelayed(this, 600);
        }else{
            packageListAdapter.notifyDataSetChanged(null);
        }
    }

    private void showLoadWindow() {
        view.setVisibility(View.VISIBLE);
        windowManager.updateViewLayout(view, windowParams);
    }

    @Override
    public void onHideWindow() {
        view.setVisibility(View.GONE);
        windowManager.updateViewLayout(view, windowParams);
    }

    @Override
    public Context getContext() {
        return getApplication();
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
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    
    
}
