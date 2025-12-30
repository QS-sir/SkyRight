package com.lizi.skyright;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public abstract class BaseFloatDialog implements View.OnTouchListener {

    private final int ANIM_DURATION = 200;
    private Context context;
    private WindowManager windowManager;
    private View layoutView;
    private WindowManager.LayoutParams windowParams;
    private boolean closing = false;
    private HideDialog hideDialog;
    private ShowDialog showDialog;
    private boolean isShowing = false;
    private OnShowListener onShowListener;
    private OnDismissListener onDismissListener;
    private boolean initOnCreate = false;
    private DecelerateInterpolator decelerateInterpolator;
    private AccelerateInterpolator accelerateInterpolator;
    private boolean isCancelable = true;
    private Point point;
    private int windowType;

    public BaseFloatDialog(Context context, View layoutView) {
        this(context,layoutView,WindowManager.LayoutParams.TYPE_APPLICATION);
    }

    public BaseFloatDialog(Context context, View layoutView,int windowType){
        this.context = context;
        this.windowManager = context.getSystemService(WindowManager.class);
        this.layoutView = layoutView;
        this.hideDialog = new HideDialog();
        this.showDialog = new ShowDialog();
        this.decelerateInterpolator = new DecelerateInterpolator();
        this.accelerateInterpolator = new AccelerateInterpolator();
        this.point = new Point();
        this.windowType = windowType;
        initWindowParams();
    }

    private void initWindowParams() {
        windowParams = new WindowManager.LayoutParams();
        windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;               
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.gravity = Gravity.CENTER;    
        windowParams.type = windowType;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        windowParams.dimAmount = 0.5f; // 背景暗度 0~1
        layoutView.setOnTouchListener(this);
        setDialogCornerRadius(15);
    }

    public void setDialogCornerRadius(int radius) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE); // 设置为矩形
        gradientDrawable.setCornerRadius(radius); // 设置圆角半径
        gradientDrawable.setColor(Color.parseColor("#FFFFFFFF"));
        layoutView.setBackground(gradientDrawable);
    }

    @Override
    public final boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            boolean isOutside = x < 0 || x > v.getWidth() || y < 0 || y > v.getHeight();
            if (isOutside && !closing && isCancelable) {
                dismiss();
                return true;
            }
        }
        return false;
    }

    public void show() {
        if (isShowing || layoutView.getParent() != null) {
            return;
        }
        if (point.x == 0) {
            windowManager.getDefaultDisplay().getRealSize(point);
            windowParams.width = point.x / 9 * 7;
        }
        windowManager.addView(layoutView, windowParams);
        if (!initOnCreate) {
            onCreate();
            initOnCreate = true;
        }
        if (onShowListener != null) {
            onShowListener.onShow();
        }
        startShowAnimation();
    }

    public abstract void onCreate();

    public boolean isShowing() {
        return isShowing;
    }

    public final void setCancelable(boolean isCancelable) {
        this.isCancelable = isCancelable;
    }

    public void dismiss() {
        if (!isShowing) {
            return;
        }
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
        closing = true;
        startDismissAnimation();
    }

    public void setOnShowListener(BaseFloatDialog.OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
    }

    public void setOnDismissListener(BaseFloatDialog.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    private void startShowAnimation() {
        layoutView.setAlpha(0f);
        layoutView.setScaleX(0.8f);
        layoutView.setScaleY(0.8f);
        layoutView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(ANIM_DURATION).setInterpolator(decelerateInterpolator).withEndAction(showDialog).start();
    }

    public <T extends View> T findViewById(int id) {
        return layoutView.findViewById(id);
    }

    public final Context getContext() {
        return this.context;
    }

    private void startDismissAnimation() {
        layoutView.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(ANIM_DURATION).setInterpolator(accelerateInterpolator).withEndAction(hideDialog).start();
    }

    public final View getContentView() {
        return this.layoutView;
    }

    private class HideDialog implements Runnable {
        @Override
        public void run() {
            if (layoutView.getParent() != null) {
                windowManager.removeViewImmediate(layoutView); // 彻底移除
            }
            isShowing = false;
            closing = false;
        }
    }

    private class ShowDialog implements Runnable {
        @Override
        public void run() {
            closing = false;
            isShowing = true;
        }
    }

    public static interface OnShowListener {
        void onShow();
    }


    public static interface OnDismissListener {
        void onDismiss();
    }

    private class ReleaseResources implements Runnable {

        @Override
        public void run() {
            if (layoutView != null && layoutView.getParent() != null) {
                try {
                    // 使用 removeView 而不是 removeViewImmediate，更平滑
                    windowManager.removeView(layoutView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            onShowListener = null;
            onDismissListener = null;
            hideDialog = null;
            showDialog = null;
            // 3. 清理视图引用
            if (layoutView != null) {
                // 移除所有监听器
                layoutView.setOnTouchListener(null);
                // 清除背景 drawable，防止 drawable 持有 view 引用
                layoutView.setBackground(null); 
            }
            // 4. 置空关键对象
            context = null;
            windowManager = null;
            layoutView = null;
            windowParams = null;
            layoutView = null;
        }
    }

    public void releaseResources() {
        if (layoutView != null) {
            layoutView.post(new ReleaseResources());
        }
    }

}
