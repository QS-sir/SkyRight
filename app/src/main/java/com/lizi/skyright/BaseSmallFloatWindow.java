package com.lizi.skyright;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

public abstract class BaseSmallFloatWindow implements Runnable {

    private final int ANIM_DURATION = 200;
    private WindowManager windowManager;
    private Context context;
    private View layoutView;
    private boolean isShowing = false;

    private Handler handler;
    private WindowManager.LayoutParams windowParams;
    private Point point;
    private boolean initOnCreate;
    private HideWindow hideWindow;
    private ShowWindow showWimdow;
    private DecelerateInterpolator decelerateInterpolator;
    private AccelerateInterpolator accelerateInterpolator;
    private OnShowListener onShowListener;
    private OnDismissListener onDismissListener;
    private long autoRemoveTime;
    private boolean timeEnd = false;

    public BaseSmallFloatWindow(Context context, View layoutView, long autoRemoveTime) {
        this.handler = new Handler(Looper.getMainLooper());
        this.context = context;
        this.layoutView = layoutView;
        this.autoRemoveTime = autoRemoveTime;
        this.windowManager = context.getSystemService(WindowManager.class);
        this.decelerateInterpolator = new DecelerateInterpolator();
        this.accelerateInterpolator = new AccelerateInterpolator();
        this.point = new Point();
        this.hideWindow = new HideWindow();
        this.showWimdow = new ShowWindow();
        initWindowParams();
    }

    private void initWindowParams() {
        windowManager.getDefaultDisplay().getRealSize(point);
        windowParams = new WindowManager.LayoutParams();
        windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;               
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.gravity = Gravity.CENTER;
        windowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        windowParams.format = PixelFormat.TRANSPARENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        setWindowCornerRadius(36);
    }

    private void setWindowCornerRadius(int radius) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE); // 设置为矩形
        gradientDrawable.setCornerRadius(radius); // 设置圆角半径
        gradientDrawable.setColor(Color.parseColor("#8F000000"));
        layoutView.setBackground(gradientDrawable);
    }

    public void setOnShowListener(OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public final View getContentView() {
        return this.layoutView;
    }

    private boolean getScreenOrientation() {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    public void run() {
        timeEnd = true;
        dismiss();
    }

    public final void show() {
        if (isShowing) {
            return;
        }
        if (getScreenOrientation()) {
            windowParams.y = point.y / 8 * 2;
        } else {
            windowParams.y = point.y / 10;
        }
        
        windowManager.addView(layoutView, windowParams);
        isShowing = true;
        if (!initOnCreate) {
            onCreate();
            initOnCreate = true;
        }
        startShowAnimation();
        if (autoRemoveTime > 1000) {
            handler.postDelayed(this, autoRemoveTime);
        }
    }

    private void startShowAnimation() {
        layoutView.setAlpha(0f);
        layoutView.setScaleX(0.5f);
        layoutView.setScaleY(0.5f);
        layoutView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(ANIM_DURATION).setInterpolator(decelerateInterpolator).withEndAction(showWimdow).start();
    }

    public final void setAutoRemoveTime(long time) {
        this.autoRemoveTime = time;
    }

    public final void dismiss() {
        if (!isShowing) {
            return;
        }
        if (autoRemoveTime > 1000) {
            handler.removeCallbacks(this);
        }
        startDismissAnimation();
    }

    private void startDismissAnimation() {
        layoutView.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f).setDuration(ANIM_DURATION).setInterpolator(accelerateInterpolator).withEndAction(hideWindow).start();
    }


    public boolean isShowing() {
        return isShowing;
    }

    protected abstract void onCreate();

    public final <T extends View> T findViewById(int id) {
        return layoutView.findViewById(id);
    }

    private class HideWindow implements Runnable {
        @Override
        public void run() {
            if (layoutView.getParent() != null) {
                windowManager.removeViewImmediate(layoutView);
            }
            isShowing = false;
            if (onDismissListener != null) {
                onDismissListener.onDismiss(timeEnd);
            }
            timeEnd = false;
        }
    }

    private class ShowWindow implements Runnable {
        @Override
        public void run() {
            if (onShowListener != null) {
                onShowListener.onShow();
            }
        }
    }


    public static interface OnShowListener {
        void onShow();
    }


    public static interface OnDismissListener {
        void onDismiss(boolean b);
    }

}
