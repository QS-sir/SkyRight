package com.lizi.skyright;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

public class HideAccessibilityStatusDialog extends BaseDialog implements TextWatcher,Runnable{
    
    private ListView appList;
    private EditText searchInput;
    private String search;
    private HideAccessibilityStatusAdapter hideAccessibilityStatusAdapter;
    
    public HideAccessibilityStatusDialog(Context context){
        super(context,R.layout.hide_accessibility_dialog);
    }

    @Override
    protected void init() {
        getWindow().setWindowAnimations(android.R.style.Animation_Toast);
        setWindowSize(getScreenWidth() / 9 * 8, WindowManager.LayoutParams.WRAP_CONTENT);
        appList = findViewById(R.id.hideaccessibilitydialogListView1);
        searchInput = findViewById(R.id.hideaccessibilitydialogEditText1);
        hideAccessibilityStatusAdapter = new HideAccessibilityStatusAdapter(getContext());
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
    
}
