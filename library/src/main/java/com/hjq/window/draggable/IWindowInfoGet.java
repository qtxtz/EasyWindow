package com.hjq.window.draggable;

import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.hjq.window.EasyWindow;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/EasyWindow
 *    time   : 2026/05/07
 *    desc   : 窗口信息配置接口
 */
public interface IWindowInfoGet {

    @Nullable
    EasyWindow<?> getEasyWindow();

    @Nullable
    ViewGroup getRootLayout();

    /**
     * 获取当前屏幕的宽度
     */
    int getScreenWidth();

    /**
     * 获取当前屏幕的高度
     */
    int getScreenHeight();

    /**
     * 获取屏幕不可用的宽度，一般情况下为横屏状态下刘海的高度
     */
    int getScreenInvisibleWidth();

    /**
     * 获取屏幕不可用的高度，一般情况下为状态栏的高度
     */
    int getScreenInvisibleHeight();

    /**
     * 获取屏幕的物理尺寸（单位：英寸）
     */
    double getScreenPhysicalSize();

    /**
     * 获取当前窗口视图的宽度
     */
    default int getWindowViewWidth() {
        EasyWindow<?> easyWindow = getEasyWindow();
        if (easyWindow == null) {
            return 0;
        }
        return easyWindow.getWindowViewWidth();
    }

    /**
     * 获取当前窗口视图的高度
     */
    default int getWindowViewHeight() {
        EasyWindow<?> easyWindow = getEasyWindow();
        if (easyWindow == null) {
            return 0;
        }
        return easyWindow.getWindowViewHeight();
    }
}