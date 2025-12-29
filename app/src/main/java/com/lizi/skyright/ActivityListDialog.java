package com.lizi.skyright;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ActivityListDialog extends BaseDialog implements DialogInterface.OnDismissListener,View.OnClickListener,TextWatcher,DialogInterface.OnShowListener {

    private ListView listView;
    private ActivityInfo[] activityInfo;
    private ModifyStartActivityAdapter modifyStartActivityAdapter;
    private Button button;
    private EditText searchInput;

    public ActivityListDialog(Context context) {
        super(context, R.layout.activity_list_dialog);
    }

    @Override
    protected void init() {
        getWindow().setWindowAnimations(R.style.dialog_anim);
        setWindowSize(getScreenWidth() / 9 * 8, WindowManager.LayoutParams.WRAP_CONTENT);
        modifyStartActivityAdapter = new ModifyStartActivityAdapter(getContext());
        listView = findViewById(R.id.activitylistdialogListView1);
        button = findViewById(R.id.activitylistdialogButton1);
        searchInput = findViewById(R.id.activitylistdialogEditText1);
        listView.setAdapter(modifyStartActivityAdapter);
        setOnShowListener(this);
        setOnDismissListener(this);
        searchInput.addTextChangedListener(this);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        SystemServerManager.getManagerInstance().setModifyPackageStartActivity(activityInfo[0].packageName,null);
        Toast.makeText(getContext(), "已恢复默认启动活动", Toast.LENGTH_SHORT).show();
    }

    
    @Override
    public void afterTextChanged(Editable editable) {
        modifyStartActivityAdapter.notifyDataSetChanged(editable.toString());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int p, int p1, int p2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int p, int p1, int p2) {
        
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        searchInput.setText(null);
    }
    

    @Override
    public void onShow(DialogInterface dialogInterface) {
        modifyStartActivityAdapter.notifyDataSetChanged(activityInfo);
    }

    public void show(ActivityInfo[] activityInfo) {
        if (this.activityInfo != activityInfo) {
            this.activityInfo = activityInfo;
        }
        super.show();
    }

}
