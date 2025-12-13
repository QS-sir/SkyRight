package com.lizi.skyright;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class AdditionalFunctionDialog extends BaseDialog implements CompoundButton.OnCheckedChangeListener,DialogInterface.OnShowListener{
    
    private Switch pauseHook,hideRoot;
    private SystemServerManager systemServerManager;
    
    public AdditionalFunctionDialog(Context context){
        super(context,R.layout.additional_function_dialog);
        this.systemServerManager = SystemServerManager.getManagerInstance();
    }

    @Override
    protected void init() {
        setWindowSize(getScreenWidth() / 9 * 7, WindowManager.LayoutParams.WRAP_CONTENT);
        initViews();
        setOnShowListener(this);
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        pauseHook.setChecked(systemServerManager.isPauseAllHook());
        hideRoot.setChecked(systemServerManager.getOneplusHideRootStatus());
    }
    
    private void initViews(){
        pauseHook = findViewById(R.id.additionalfunctiondialogSwitch1);
        hideRoot = findViewById(R.id.additionalfunctiondialogSwitch2);
        pauseHook.setOnCheckedChangeListener(this);
        hideRoot.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean p) {
        if(!compoundButton.isPressed()){
            return;
        }
        if(compoundButton == pauseHook){
            systemServerManager.setPauseAllHook(p);
        }else if(compoundButton == hideRoot){
            if(Build.BRAND.equals("OnePlus")){
                systemServerManager.setOneplusHideRootStatus(p);
            }else{
                compoundButton.setChecked(false);
                Toast.makeText(getContext(), "非一加手机无效", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
}
