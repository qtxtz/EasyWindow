package com.hjq.window.draggable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.hjq.window.EasyWindow;
import com.hjq.window.OnWindowScreenRotationCallback;
import com.hjq.window.draggable.callback.OnWindowDraggingCallback;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/EasyWindow
 *    time   : 2019/01/04
 *    desc   : 窗口拖拽规则基类
 */
public abstract class BaseWindowDraggableRule implements IWindowDraggableRule, OnTouchListener {

    @Nullable
    private EasyWindow<?> mEasyWindow;
    @Nullable
    private ViewGroup mRootLayout;

    /** 是否允许移动到屏幕安全区域 */
    private boolean mAllowMoveToScreenSafeArea = true;

    /** 拖拽回调监听对象（可能为空） */
    @Nullable
    private OnWindowDraggingCallback mOnWindowDraggingCallback;

    @NonNull
    private final Rect mTempRect = new Rect();

    private int mCurrentScreenWidth;
    private int mCurrentScreenHeight;

    private int mCurrentScreenInvisibleWidth;
    private int mCurrentScreenInvisibleHeight;

    /** 当前屏幕的物理尺寸 */
    private double mScreenPhysicalSize;

    /** 需要消费触摸事件的 View（可能为空）*/
    @Nullable
    private View mConsumeTouchView;

    @Nullable
    @Override
    public EasyWindow<?> getEasyWindow() {
        return mEasyWindow;
    }

    @Nullable
    @Override
    public ViewGroup getRootLayout() {
        return mRootLayout;
    }

    @Override
    public int getScreenWidth() {
        return mCurrentScreenWidth;
    }

    @Override
    public int getScreenHeight() {
        return mCurrentScreenHeight;
    }

    @Override
    public int getScreenInvisibleWidth() {
        return mCurrentScreenInvisibleWidth;
    }

    @Override
    public int getScreenInvisibleHeight() {
        return mCurrentScreenInvisibleHeight;
    }

    @Override
    public double getScreenPhysicalSize() {
        return mScreenPhysicalSize;
    }

    @Override
    public void setAllowMoveToScreenSafeArea(boolean allowMoveToScreenSafeArea) {
        mAllowMoveToScreenSafeArea = allowMoveToScreenSafeArea;
    }

    @Override
    public boolean isAllowMoveToScreenSafeArea() {
        return mAllowMoveToScreenSafeArea;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void start(@NonNull EasyWindow<?> easyWindow) {
        mEasyWindow = easyWindow;
        mRootLayout = easyWindow.getRootLayout();
        if (mRootLayout == null) {
            return;
        }
        mRootLayout.setOnTouchListener(this);

        refreshWindowInfo();
        refreshScreenPhysicalSize();
    }

    @Override
    public void stop() {
        mEasyWindow = null;
        if (mRootLayout != null) {
            mRootLayout.setOnTouchListener(null);
            mRootLayout = null;
        }
    }

    @Override
    public final boolean onTouch(@NonNull View view, @NonNull MotionEvent event) {
        EasyWindow<?> easyWindow = mEasyWindow;
        ViewGroup rootLayout = mRootLayout;
        if (easyWindow == null || rootLayout == null) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 在按下的时候先更新一下窗口信息和坐标信息，否则点击可能会出现坐标偏移的问题
                // 全局的悬浮窗在非全屏的页面创建，跳转到全屏的页面展示就会导致坐标偏移
                // 这是因为在跳转到全屏的悬浮窗的时候没有更新当前 Window 信息导致的
                // 目前能想到比较好的办法就是在悬浮窗移动前之前先更新 Window 信息和 View 坐标
                // Github issue 地址：https://github.com/getActivity/EasyWindow/issues/69
                refreshWindowInfo();
                refreshScreenPhysicalSize();

                mConsumeTouchView = null;
                View consumeTouchEventView = findNeedConsumeTouchView(rootLayout, event);
                if (consumeTouchEventView != null && dispatchTouchEventToChildView(rootLayout, consumeTouchEventView, event)) {
                    mConsumeTouchView = consumeTouchEventView;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mConsumeTouchView != null) {
                    try {
                        return dispatchTouchEventToChildView(rootLayout, mConsumeTouchView, event);
                    } finally {
                        // 释放/置空对象
                        mConsumeTouchView = null;
                    }
                }
            default:
                if (mConsumeTouchView != null) {
                    return dispatchTouchEventToChildView(rootLayout, mConsumeTouchView, event);
                }
                break;
        }

        return onDragWindow(easyWindow, rootLayout, event);
    }

    @Override
    public void onScreenOrientationChange() {
        // Log.i(getClass().getSimpleName(), "屏幕方向发生了改变");
        final ViewGroup rootLayout = getRootLayout();
        if (rootLayout == null) {
            return;
        }

        final EasyWindow<?> easyWindow = getEasyWindow();
        if (easyWindow == null) {
            return;
        }

        final WindowManager.LayoutParams windowParams = easyWindow.getWindowParams();

        if (!isFollowScreenRotationChanges()) {
            easyWindow.sendTask(() -> {
                refreshWindowInfo();
                refreshScreenPhysicalSize();
            }, OnWindowScreenRotationCallback.SCREEN_ROTATION_BUFFER_TIME);
            return;
        }

        final int screenWidth = getScreenWidth();
        final int screenHeight = getScreenHeight();
        // Log.i(getClass().getSimpleName(), "屏幕旋转前 screenWidth = " + screenWidth + "，screenHeight = " + screenHeight);

        final int screenInvisibleWidth = getScreenInvisibleWidth();
        final int screenInvisibleHeight = getScreenInvisibleHeight();
        // Log.i(getClass().getSimpleName(), "屏幕旋转前 screenInvisibleWidth = " + screenInvisibleWidth + "，screenInvisibleHeight = " + screenInvisibleHeight);

        final int windowViewWidth = getWindowViewWidth();
        final int windowViewHeight = getWindowViewHeight();
        // Log.i(getClass().getSimpleName(), "屏幕旋转前 windowViewWidth = " + windowViewWidth + "，windowViewHeight = " + windowViewHeight);

        final int windowGravity;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            windowGravity = Gravity.getAbsoluteGravity(windowParams.gravity, rootLayout.getLayoutDirection());
        } else {
            windowGravity = windowParams.gravity;
        }
        final int windowHorizontalOffset = windowParams.x;
        final int windowVerticalOffset = windowParams.y;

        int windowHorizontalGravity = windowGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        final int currentViewOnScreenX;
        if (windowHorizontalGravity == Gravity.LEFT) {
            currentViewOnScreenX = windowHorizontalOffset;
        } else if (windowHorizontalGravity == Gravity.RIGHT) {
            currentViewOnScreenX = (screenWidth - screenInvisibleWidth) - windowHorizontalOffset;
        } else {
            // Gravity.CENTER_HORIZONTAL
            currentViewOnScreenX = (screenWidth - screenInvisibleWidth - windowViewWidth) / 2 + windowHorizontalOffset;
        }

        int windowVerticalGravity = windowGravity & Gravity.VERTICAL_GRAVITY_MASK;
        final int currentViewOnScreenY;
        if (windowVerticalGravity == Gravity.TOP) {
            currentViewOnScreenY = windowVerticalOffset;
        } else if (windowVerticalGravity == Gravity.BOTTOM) {
            currentViewOnScreenY = (screenHeight - screenInvisibleHeight) - windowVerticalOffset;
        } else {
            // Gravity.CENTER_VERTICAL
            currentViewOnScreenY = (screenHeight - screenInvisibleHeight - windowViewHeight) / 2 + windowVerticalOffset;
        }
        // Log.i(getClass().getSimpleName(), "currentViewOnScreenX = " + currentViewOnScreenX + "，currentViewOnScreenY = " + currentViewOnScreenY);

        // 先扣除 View 自身宽度，用剩余空余空间算比例
        final int currentHorizontalGap = Math.max(screenWidth - windowViewWidth, 0);
        final float leftGapRatio = calculateGapSizeRatio(currentViewOnScreenX, currentHorizontalGap);
        // 先扣除 View 自身高度，用剩余空余空间算比例
        final int currentVerticalGap = Math.max(screenHeight - windowViewHeight, 0);
        final float topGapRatio = calculateGapSizeRatio(currentViewOnScreenY, currentVerticalGap);
        // Log.i(getClass().getSimpleName(), "leftGapRatio = " + leftGapRatio + "，topGapRatio = " + topGapRatio);

        easyWindow.sendTask(() -> {
            refreshWindowInfo();
            refreshScreenPhysicalSize();
            int newScreenWidth = getScreenWidth();
            int newScreenHeight = getScreenHeight();
            // Log.i(getClass().getSimpleName(), "屏幕旋转后 screenWidth = " + newScreenWidth + "，screenHeight = " + newScreenHeight);

            int newHorizontalGap = newScreenWidth - windowViewWidth;
            int newVerticalGap = newScreenHeight - windowViewHeight;

            int newViewOnScreenX = newHorizontalGap <= 0 ? 0 : (int) (newHorizontalGap * leftGapRatio);
            int newViewOnScreenY = newVerticalGap <= 0 ? 0 : (int) (newVerticalGap * topGapRatio);

            // 边界安全限位，防止浮点误差越界
            newViewOnScreenX = Math.max(0, Math.min(newViewOnScreenX, newScreenWidth - windowViewWidth));
            newViewOnScreenY = Math.max(0, Math.min(newViewOnScreenY, newScreenHeight - windowViewHeight));

            // Log.i(getClass().getSimpleName(), "屏幕旋转后 newViewOnScreenX = " + newViewOnScreenX + "，newViewOnScreenY = " + newViewOnScreenY);
            updateLocation(newViewOnScreenX, newViewOnScreenY);

        }, OnWindowScreenRotationCallback.SCREEN_ROTATION_BUFFER_TIME);
    }

    /**
     * 更新悬浮窗的位置
     *
     * @param x                                 x 坐标（相对与屏幕左上位置）
     * @param y                                 y 坐标（相对与屏幕左上位置）
     * @param allowMoveToScreenSafeArea         是否允许移动到屏幕安全区域
     */
    @Override
    public void updateLocation(int x, int y, boolean allowMoveToScreenSafeArea) {
        if (allowMoveToScreenSafeArea) {
            updateWindowCoordinate(x, y);
            return;
        }

        Rect safeInsetRect = getSafeInsetRect(getEasyWindow());
        if (safeInsetRect == null ||
            (safeInsetRect.left == 0 && safeInsetRect.right == 0 &&
            safeInsetRect.top == 0 && safeInsetRect.bottom == 0)) {
            updateWindowCoordinate(x, y);
            return;
        }

        int viewWidth = getWindowViewWidth();
        int viewHeight = getWindowViewHeight();

        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();

        // Log.i(getClass().getSimpleName(), "开始 x 坐标为：" + x);
        // Log.i(getClass().getSimpleName(), "开始 y 坐标为：" + y);

        if (x < safeInsetRect.left - getScreenInvisibleWidth()) {
            x = safeInsetRect.left - getScreenInvisibleWidth();
            // Log.i(getClass().getSimpleName(), "x 坐标已经触碰到屏幕左侧的安全区域");
        } else if (x > screenWidth - safeInsetRect.right - viewWidth) {
            x = screenWidth - safeInsetRect.right - viewWidth;
            // Log.i(getClass().getSimpleName(), "x 坐标已经触碰到屏幕右侧的安全区域");
        }

        // Log.i(getClass().getSimpleName(), "最终 x 坐标为：" + x);

        if (y < safeInsetRect.top - getScreenInvisibleHeight()) {
            y = safeInsetRect.top - getScreenInvisibleHeight();
            // Log.i(getClass().getSimpleName(), "y 坐标已经触碰到屏幕顶侧的安全区域");
        } else if (y > screenHeight - safeInsetRect.bottom - viewHeight) {
            y = screenHeight - safeInsetRect.bottom - viewHeight;
            // Log.i(getClass().getSimpleName(), "y 坐标已经触碰到屏幕底部的安全区域");
        }

        // Log.i(getClass().getSimpleName(), "最终 y 坐标为：" + y);

        updateWindowCoordinate(x, y);
    }

    /**
     * 更新悬浮窗的坐标位置，单位为像素（相对与屏幕左上位置）
     *
     * @param x                                 x 坐标（相对与屏幕左上位置）
     * @param y                                 y 坐标（相对与屏幕左上位置）
     */
    public void updateWindowCoordinate(int x, int y) {
        if (mEasyWindow == null) {
            return;
        }
        WindowManager.LayoutParams params = mEasyWindow.getWindowParams();

        // 屏幕默认的重心（一定要先设置重心位置为左上角）
        int screenGravity = Gravity.LEFT | Gravity.TOP;

        // 判断本次移动的位置是否跟当前的窗口位置是否一致
        if (params.gravity == screenGravity && params.x == x && params.y == y) {
            return;
        }

        params.x = x;
        params.y = y;
        params.gravity = screenGravity;

        mEasyWindow.update();
    }

    /**
     * 设置拖拽回调
     */
    @Override
    public void setOnWindowDraggingCallback(@Nullable OnWindowDraggingCallback callback) {
        mOnWindowDraggingCallback = callback;
    }

    /**
     * 判断用户手指是否移动了
     */
    public boolean isFingerMove(float downX, float upX, float downY, float upY) {
        return isFingerMove(getMinTouchDistance(getScreenPhysicalSize()), downX, upX, downY, upY);
    }

    /**
     * 派发开始拖拽事件
     */
    public void dispatchDraggingStartCallback() {
        // Log.i(getClass().getSimpleName(), "开始拖拽");
        if (mEasyWindow == null) {
            return;
        }
        if (mOnWindowDraggingCallback == null) {
            return;
        }
        mOnWindowDraggingCallback.onWindowDraggingStart(mEasyWindow);
    }

    /**
     * 派发拖拽中事件
     */
    public void dispatchDraggingRunningCallback() {
        // Log.i(getClass().getSimpleName(), "拖拽中");
        if (mEasyWindow == null) {
            return;
        }
        if (mOnWindowDraggingCallback == null) {
            return;
        }
        mOnWindowDraggingCallback.onWindowDraggingRunning(mEasyWindow);
    }

    /**
     * 派发停止拖拽事件
     */
    public void dispatchDraggingStopCallback() {
        // Log.i(getClass().getSimpleName(), "停止拖拽");
        if (mEasyWindow == null) {
            return;
        }
        if (mOnWindowDraggingCallback == null) {
            return;
        }
        mOnWindowDraggingCallback.onWindowDraggingStop(mEasyWindow);
    }

    /**
     * 刷新当前 Window 信息
     */
    public void refreshWindowInfo() {
        if (mEasyWindow == null) {
            return;
        }

        Context context = mEasyWindow.getContext();
        if (context == null) {
            return;
        }

        // 相关问题地址：https://github.com/getActivity/EasyWindow/issues/85
        View decorView = getRootLayout();

        if (decorView == null && context instanceof Activity) {
            decorView = ((Activity) context).getWindow().getDecorView();
        }

        if (decorView == null) {
            return;
        }

        // Log.i(getClass().getSimpleName(), "刷新当前 Window 信息");

        // 这里为什么要这么写，因为发现了鸿蒙手机在进行屏幕旋转的时候
        // 回调 onConfigurationChanged 方法的时候获取到这些参数已经变化了
        // 所以需要提前记录下来，避免后续进行坐标计算的时候出现问题
        decorView.getWindowVisibleDisplayFrame(mTempRect);
        mCurrentScreenWidth = mTempRect.right - mTempRect.left;
        mCurrentScreenHeight = mTempRect.bottom - mTempRect.top;

        mCurrentScreenInvisibleWidth = Math.max(mTempRect.left, 0);
        mCurrentScreenInvisibleHeight = Math.max(mTempRect.top, 0);

        /*
        Log.i(getClass().getSimpleName(),
            "CurrentScreenWidth = " + mCurrentScreenWidth +
            "，CurrentScreenHeight = " + mCurrentScreenHeight +
            "，CurrentScreenInvisibleWidth = " + mCurrentScreenInvisibleWidth +
            "，CurrentScreenInvisibleHeight = " + mCurrentScreenInvisibleHeight);
         */
    }

    /**
     * 刷新当前屏幕的物理尺寸
     */
    @SuppressWarnings("deprecation")
    public void refreshScreenPhysicalSize() {
        if (mEasyWindow == null) {
            return;
        }

        WindowManager windowManager = mEasyWindow.getWindowManager();
        Display defaultDisplay = windowManager.getDefaultDisplay();
        if (defaultDisplay == null) {
            return;
        }

        DisplayMetrics metrics = new DisplayMetrics();
        defaultDisplay.getMetrics(metrics);

        float screenWidthInInches;
        float screenHeightInInches;
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
            Point point = new Point();
            defaultDisplay.getRealSize(point);
            screenWidthInInches = point.x / metrics.xdpi;
            screenHeightInInches = point.y / metrics.ydpi;
        } else {
            screenWidthInInches = metrics.widthPixels / metrics.xdpi;
            screenHeightInInches = metrics.heightPixels / metrics.ydpi;
        }

        // 勾股定理：直角三角形的两条直角边的平方和等于斜边的平方
        mScreenPhysicalSize = Math.sqrt(Math.pow(screenWidthInInches, 2) + Math.pow(screenHeightInInches, 2));
    }
}