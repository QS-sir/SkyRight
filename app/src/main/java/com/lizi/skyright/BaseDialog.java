package com.lizi.skyright;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public abstract class BaseDialog extends Dialog {

    private View view;
    private Window window;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private int radius;
    private Point point;

    public BaseDialog(Context context,int resId){
        this(context,resId,25);
    }

    public BaseDialog(Context context,View view){
        this(context,view,25);
    }

    public BaseDialog(Context context,int resId,int radius){
        this(context,View.inflate(context,resId,null),radius);
    }

    public BaseDialog(Context context,View view,int radius){
        this(context,0,view,radius);
    }

    public BaseDialog(Context context,int theme,int resId,int radius){
        this(context,theme,View.inflate(context,resId,null),radius);
    }

    public BaseDialog(Context context,int theme,View view,int radius){
        super(context,theme);
        this.view = view;
        this.radius = radius;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initWindowAttribute();
        setContentView(view);
        init();
    }

    protected abstract void init();

    @Override
    public <T extends View> T findViewById(int p) {
        return view.findViewById(p);
    }

    private void initWindowAttribute(){
        window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        windowManager = window.getWindowManager();
        point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);
        window.setBackgroundDrawable(getDrawable(this.radius));
        layoutParams = window.getAttributes();
    }

    protected final void setWindowSize(int width,int height) {
        layoutParams.width = width;
        layoutParams.height = height;
        window.setAttributes(layoutParams);
    }

    protected final int getScreenWidth(){
        return point.x;
    }

    protected final int getScreenHeight(){
        return point.y;
    }

    private Drawable getDrawable(int radius) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE); // 设置为矩形
        gradientDrawable.setColor(Color.parseColor("#FFFFFFFF")); // 设置颜色
        gradientDrawable.setCornerRadius(radius); // 设置圆角半径
        return gradientDrawable;
    }

    public Resources getResources(){
        return getContext().getResources();
    }

    @Override
    public final void setTitle(int p) {
        super.setTitle(p);
    }

    @Override
    public final void setTitle(CharSequence charSequence) {
        super.setTitle(charSequence);
    }


    protected View getDecorView(){
        return this.view;
    }

}
