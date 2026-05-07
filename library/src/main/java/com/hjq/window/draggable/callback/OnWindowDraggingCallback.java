package com.hjq.window.draggable.callback;

import androidx.annotation.NonNull;
import com.hjq.window.EasyWindow;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/EasyWindow
 *    time   : 2026/05/07
 *    desc   : 窗口拖拽监听接口
 */
public interface OnWindowDraggingCallback {

    /**
     * 开始拖拽
     */
    default void onWindowDraggingStart(@NonNull EasyWindow<?> easyWindow) {
        // default implementation ignored
    }

    /**
     * 拖拽中
     */
    default void onWindowDraggingRunning(@NonNull EasyWindow<?> easyWindow) {
        // default implementation ignored
    }

    /**
     * 停止拖拽
     */
    default void onWindowDraggingStop(@NonNull EasyWindow<?> easyWindow) {
        // default implementation ignored
    }
}