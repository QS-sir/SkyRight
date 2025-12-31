package com.lizi.skyright;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ListView;

public class HideAccessibilityStatusActivity extends Activity implements TextWatcher,Runnable {
    
    private ListView appList;
    private EditText searchInput;
    private String search;
    private HideAccessibilityStatusAdapter hideAccessibilityStatusAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hide_accessibility_status_activity);
        init();
    }
    
    private void init(){
        appList = findViewById(R.id.hideaccessibilitydialogListView1);
        searchInput = findViewById(R.id.hideaccessibilitydialogEditText1);
        hideAccessibilityStatusAdapter = new HideAccessibilityStatusAdapter(this);
        appList.setAdapter(hideAccessibilityStatusAdapter);
        searchInput.addTextChangedListener(this);
    }
    
    @Override
    public void run() {
        hideAccessibilityStatusAdapter.notifyDataSetChanged(search);
    }


    @Override
    public void afterTextChanged(Editable editable) {
        search = editable.toString();
        searchInput.postDelayed(this,500);
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
