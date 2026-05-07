package com.hjq.window;

import androidx.annotation.NonNull;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/EasyWindow
 *    time   : 2026/04/29
 *    desc   : 窗口屏幕旋转监听
 */
public interface OnWindowScreenRotationCallback {

    /** 屏幕旋转缓冲时间 */
    long SCREEN_ROTATION_BUFFER_TIME = 100;

    /** 屏幕旋转后回调时间 */
    long SCREEN_ROTATION_AFTER_TIME = SCREEN_ROTATION_BUFFER_TIME + 10;

    /**
     * 窗口屏幕旋转前
     * @param screenOrientation     屏幕方向类型，有两种类型：
     *                              {@link android.content.res.Configuration#ORIENTATION_LANDSCAPE}
     *                              {@link android.content.res.Configuration#ORIENTATION_PORTRAIT}
     */
    default void onWindowScreenRotationBefore(@NonNull EasyWindow<?> easyWindow, int screenOrientation) {
        // default implementation ignored
    }

    /**
     * 窗口屏幕旋转后
     * @param screenOrientation     屏幕方向类型，有两种类型：
     *                              {@link android.content.res.Configuration#ORIENTATION_LANDSCAPE}
     *                              {@link android.content.res.Configuration#ORIENTATION_PORTRAIT}
     */
    default void onWindowScreenRotationAfter(@NonNull EasyWindow<?> easyWindow, int screenOrientation) {
        // default implementation ignored
    }
}