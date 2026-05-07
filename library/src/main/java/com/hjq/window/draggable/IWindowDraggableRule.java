package com.hjq.window.draggable;

import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.hjq.window.EasyWindow;
import com.hjq.window.draggable.callback.OnWindowDraggingCallback;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/EasyWindow
 *    time   : 2026/05/07
 *    desc   : 窗口拖拽规则接口
 */
public interface IWindowDraggableRule extends IWindowInfoGet, IWindowAuxiliary {

    /**
     * 开启拖拽，窗口显示后回调这个方法
     */
    void start(@NonNull EasyWindow<?> easyWindow);

    /**
     * 停止拖拽，窗口回收后回调这个方法
     */
    void stop();

    /**
     * 判断当前是否处于触摸移动状态
     */
    boolean isTouchMoving();

    /**
     * 窗口拖拽回调方法
     *
     * @param rootLayout        当前窗口视图
     * @param event             当前触摸事件
     * @return                  根据返回值决定是否拦截该事件
     */
    boolean onDragWindow(@NonNull EasyWindow<?> easyWindow, @NonNull ViewGroup rootLayout, @NonNull MotionEvent event);

    /**
     * 悬浮窗是否跟随屏幕方向变化而发生变化
     */
    default boolean isFollowScreenRotationChanges() {
        return true;
    }

    /**
     * 设置是否可以移动到屏幕安全区域
     */
    void setAllowMoveToScreenSafeArea(boolean allowMoveToScreenSafeArea);

    /**
     * 当前是否可以移动到屏幕安全区域
     */
    boolean isAllowMoveToScreenSafeArea();

    /**
     * 判断当前悬浮窗是否可以移动到屏幕之外的地方
     */
    default boolean isSupportMoveOffScreen() {
        EasyWindow<?> easyWindow = getEasyWindow();
        if (easyWindow == null) {
            return false;
        }
        return easyWindow.hasWindowFlags(LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    /**
     * 通知拖拽规则当前屏幕方向发生了改变
     */
    void onScreenOrientationChange();

    /**
     * 设置拖拽回调
     */
    void setOnWindowDraggingCallback(@Nullable OnWindowDraggingCallback callback);

    /**
     * 更新悬浮窗的位置
     *
     * @param x                                 x 坐标（相对与屏幕左上位置）
     * @param y                                 y 坐标（相对与屏幕左上位置）
     */
    default void updateLocation(float x, float y) {
        updateLocation(x, y, isAllowMoveToScreenSafeArea());
    }

    /**
     * 更新悬浮窗的位置
     *
     * @param x                                 x 坐标（相对与屏幕左上位置）
     * @param y                                 y 坐标（相对与屏幕左上位置）
     * @param allowMoveToScreenSafeArea         是否允许移动到屏幕安全区域
     */
    default void updateLocation(float x, float y, boolean allowMoveToScreenSafeArea) {
        updateLocation((int) x, (int) y, allowMoveToScreenSafeArea);
    }

    /**
     * 更新悬浮窗的位置
     *
     * @param x                                 x 坐标（相对与屏幕左上位置）
     * @param y                                 y 坐标（相对与屏幕左上位置）
     * @param allowMoveToScreenSafeArea         是否允许移动到屏幕安全区域
     */
    void updateLocation(int x, int y, boolean allowMoveToScreenSafeArea);
}