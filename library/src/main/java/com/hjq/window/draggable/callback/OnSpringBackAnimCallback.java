package com.hjq.window.draggable.callback;

import android.animation.Animator;
import androidx.annotation.NonNull;
import com.hjq.window.EasyWindow;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/EasyWindow
 *    time   : 2026/05/07
 *    desc   : 窗口回弹动画监听接口
 */
public interface OnSpringBackAnimCallback {

    /**
     * 回弹动画开始执行
     */
    default void onSpringBackAnimationStart(@NonNull EasyWindow<?> easyWindow, @NonNull Animator animator) {
        // default implementation ignored
    }

    /**
     * 回弹动画结束执行
     */
    default void onSpringBackAnimationEnd(@NonNull EasyWindow<?> easyWindow, @NonNull Animator animator) {
        // default implementation ignored
    }
}