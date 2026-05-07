package com.hjq.window.draggable;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingParent;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import com.hjq.window.EasyWindow;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/EasyWindow
 *    time   : 2026/05/07
 *    desc   : 窗口辅助接口
 */
public interface IWindowAuxiliary {

    /**
     * 判断用户手指是否移动了，判断标准以下：
     * 根据手指按下和抬起时的坐标进行判断，不能根据有没有 move 事件来判断
     * 因为在有些机型上面，就算用户没有手指移动，只是简单点击也会产生 move 事件
     *
     * @param minTouchSlop  最小触摸距离，单位：像素
     * @param downX         手指按下时的 x 坐标
     * @param upX           手指抬起时的 x 坐标
     * @param downY         手指按下时的 y 坐标
     * @param upY           手指抬起时的 y 坐标
     */
    default boolean isFingerMove(double minTouchSlop, float downX, float upX, float downY, float upY) {
        return Math.abs(downX - upX) >= minTouchSlop || Math.abs(downY - upY) >= minTouchSlop;
    }

    /**
     * 获取最小触摸距离
     */
    default float getMinTouchDistance(double screenPhysicalSize) {
        // 疑问一：为什么要使用 1dp 来作为最小触摸距离？
        //        这是因为用户点击的时候，手指 down 和 up 的坐标不相等，会存在一点误差
        //        在有些手机上面，误差会比较小，还有一些手机上面，误差会比较大
        //        经过拿不同的手机测试和验证，这个误差值可以锁定在 1dp 内
        //        当然我的结论不一定正确，你要是有发现新的问题也可以找我反馈，我会持续优化这个问题
        // 疑问二：为什么不使用 ViewConfiguration.get(context).getScaledTouchSlop() ？
        //        这是因为这个 API 获取到的数值太大了，有一定概率会出现误判，同样的手机上面
        //        用 getScaledTouchSlop 获取到的是 24，而系统 1dp 获取的到是 3px，
        //        两者相差太大，因为 getScaledTouchSlop API 默认获取的是 8dp * 3 = 24px
        // 疑问三：为什么要用 Resources.getSystem 来获取，而不是 context.getResources？
        //        这是因为如果用了 AutoSize 这个框架，上下文中的 1dp 就不是 3px 了
        //        使用 Resources.getSystem 能够保证 Resources 对象 dp 计算规则不被第三方框架篡改
        // 疑问四：为什么用屏幕的物理尺寸来算出最小触摸距离呢？
        //        这是因为在超大屏的设备上面，单击悬浮窗的误差就不止 1dp，可能是更大的值，所以需要更大的值来兼容
        //        Github issue：https://github.com/getActivity/EasyWindow/pull/79
        int dpValue;
        if (screenPhysicalSize > 0) {
            // 市面上的平板最大尺寸不超过 15 英寸
            dpValue = (int) Math.ceil(screenPhysicalSize / 15);
        } else {
            dpValue = 1;
        }
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
            Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 获取当前屏幕安全区域
     */
    @Nullable
    default Rect getSafeInsetRect(@Nullable EasyWindow<?> easyWindow) {
        if (easyWindow == null) {
            return null;
        }
        Context context = easyWindow.getContext();
        Window window;
        if (!(context instanceof Activity)) {
            return null;
        }

        window = ((Activity) context).getWindow();
        if (window == null) {
            return null;
        }

        return getSafeInsetRect(window);
    }

    /**
     * 根据 Window 对象获取屏幕安全区域位置（返回的对象可能为空）
     */
    @Nullable
    default Rect getSafeInsetRect(@Nullable Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return null;
        }

        View activityDecorView = null;
        if (window != null) {
            activityDecorView = window.getDecorView();
        }
        WindowInsets rootWindowInsets = null;
        if (activityDecorView != null) {
            rootWindowInsets = activityDecorView.getRootWindowInsets();
        }
        DisplayCutout displayCutout = null;
        if (rootWindowInsets != null) {
            displayCutout = rootWindowInsets.getDisplayCutout();
        }

        if (displayCutout != null) {
            // 安全区域距离屏幕左边的距离
            int safeInsetLeft = displayCutout.getSafeInsetLeft();
            // 安全区域距离屏幕顶部的距离
            int safeInsetTop = displayCutout.getSafeInsetTop();
            // 安全区域距离屏幕右部的距离
            int safeInsetRight = displayCutout.getSafeInsetRight();
            // 安全区域距离屏幕底部的距离
            int safeInsetBottom = displayCutout.getSafeInsetBottom();

            // Log.i(getClass().getSimpleName(), "安全区域距离屏幕左侧的距离 SafeInsetLeft：" + safeInsetLeft);
            // Log.i(getClass().getSimpleName(), "安全区域距离屏幕右侧的距离 SafeInsetRight：" + safeInsetRight);
            // Log.i(getClass().getSimpleName(), "安全区域距离屏幕顶部的距离 SafeInsetTop：" + safeInsetTop);
            // Log.i(getClass().getSimpleName(), "安全区域距离屏幕底部的距离 SafeInsetBottom：" + safeInsetBottom);

            return new Rect(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom);
        }

        return null;
    }

    /**
     * 寻找需要消费触摸事件的 View（可能为空）
     */
    @Nullable
    default View findNeedConsumeTouchView(@NonNull ViewGroup viewGroup, @NonNull MotionEvent event) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {

            View childView = viewGroup.getChildAt(i);
            int[] location = new int[2];
            childView.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int right = left + childView.getWidth();
            int bottom = top + childView.getHeight();

            float x = event.getRawX();
            float y = event.getRawY();

            // 判断触摸位置是否在这个 View 内
            if (x >= left && x <= right && y >= top && y <= bottom) {
                if (isViewNeedConsumeTouchEvent(childView)) {
                    return childView;
                } else if (childView instanceof ViewGroup) {
                    return findNeedConsumeTouchView((ViewGroup) childView, event);
                }
            }
        }
        return null;
    }

    /**
     * 判断 View 是否需要消费当前触摸事件
     */
    default boolean isViewNeedConsumeTouchEvent(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && view instanceof ViewGroup && view.isScrollContainer()) {
            return canTouchByView(view);
        }

        // NestedScrollingChild 的子类有：RecyclerView、NestedScrollView、SwipeRefreshLayout 等等
        try {
            if (view instanceof NestedScrollingChild || view instanceof NestedScrollingParent ||
                view instanceof WebView || view instanceof ScrollView || view instanceof ListView ||
                view instanceof SeekBar || view instanceof ViewPager || view instanceof ViewPager2) {
                return canTouchByView(view);
            }
        } catch (Exception ignored) {
            // 需要注意：Support 没有 ViewPager2，AndroidX 才有 ViewPager2
            // java.lang.ClassNotFoundException: Didn't find class "androidx.viewpager2.widget.ViewPager2"
            // default implementation ignored
        }

        return false;
    }

    /**
     * 判断 View 是否能被触摸
     */
    default boolean canTouchByView(@NonNull View view) {
        if (view instanceof RecyclerView && !canScrollByRecyclerView(((RecyclerView) view))) {
            // 如果这个 RecyclerView 禁止了触摸事件，就不要启动触摸事件
            return false;
        }

        // 这个 View 必须是启用状态，才认为有可能传递触摸事件
        return view.isEnabled();
    }

    /**
     * 判断 RecyclerView 是否能被触摸
     */
    default boolean canScrollByRecyclerView(@NonNull RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) {
            // 如果没有设置 LayoutManager，则默认不需要触摸事件
            return false;
        }

        // 当前这个 LayoutManager 必须开启垂直滚动或者水平滚动
        return layoutManager.canScrollVertically() || layoutManager.canScrollHorizontally();
    }

    /**
     * 派发触摸事件给子 View
     *
     * @param event                 触摸事件
     * @param parentView            父 View
     * @param childView             子 View（同时也是被触摸的 View）
     */
    default boolean dispatchTouchEventToChildView(@NonNull View parentView, @NonNull View childView, @NonNull MotionEvent event) {
        // 派发触摸事件之前，先将 MotionEvent 对象中的位置进行纠偏，否则会导致点击坐标对不上的情况
        offsetMotionEventLocation(parentView, childView, event);
        return childView.dispatchTouchEvent(event);
    }

    /**
     * 偏移触摸事件坐标，这样子 View 能接受到正确的坐标
     *
     * @param event                 触摸事件
     * @param parentView            父 View
     * @param childView             子 View（同时也是被触摸的 View）
     */
    default void offsetMotionEventLocation(@NonNull View parentView, @NonNull View childView, @NonNull MotionEvent event) {
        // 这部分代码参考自 ViewGroup.dispatchTransformedTouchEvent 方法实现
        final int offsetX = parentView.getScrollX() - childView.getLeft();
        final int offsetY = parentView.getScrollY() - childView.getTop();
        event.offsetLocation(offsetX, offsetY);
    }

    /**
     * 计算空余空间的比例
     *
     * @param gapSize        空余空间的大小
     * @param totalSize      总空间的大小
     */
    default float calculateGapSizeRatio(float gapSize, float totalSize) {
        // 防止除零异常，如果没有空余空间了，就默认放在中间位置
        if (totalSize <= 0) {
            return 0.5f;
        }
        float ratio = gapSize / totalSize;
        if (ratio > 0.99f) {
            // 如果比例超过了 99%，就认为它已经贴边了，直接放在边缘位置
            return 1f;
        } else if (ratio < 0.01f) {
            // 如果比例小于了 1%，就认为它已经贴边了，直接放在边缘位置
            return 0f;
        } else {
            // 正常情况，返回计算的比例
            return ratio;
        }
    }
}