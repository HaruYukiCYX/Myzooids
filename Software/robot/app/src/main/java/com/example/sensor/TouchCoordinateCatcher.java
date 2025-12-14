package com.example.sensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import org.opencv.android.JavaCameraView;

public class TouchCoordinateCatcher {
    private static final String TAG = "TouchCoordinateCatcher";

    private OnTouchCoordinateListener listener;
    private Context context;
    private DrawView drawView;

    // 绘图坐标 (相对于 DrawView 左上角)
    private float drawLocalX = -1;
    private float drawLocalY = -1;

    // 图像数据参数
    private JavaCameraView cameraView;
    private int opencvImageWidth;  // 例如 1280
    private int opencvImageHeight; // 例如 720

    // 画笔
    private Paint circlePaint;

    public TouchCoordinateCatcher(Context context, OnTouchCoordinateListener listener) {
        this.context = context;
        this.listener = listener;
        initPaint();
    }

    private void initPaint() {
        circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(5f);
        circlePaint.setAntiAlias(true);
    }

    /**
     * 核心修复：处理长按坐标，考虑黑边偏移
     */
    public void setLongPressCoordinate(float rawX, float rawY) {
        if (cameraView == null || opencvImageWidth == 0 || opencvImageHeight == 0) {
            return;
        }

        // 1. 获取 View 在屏幕上的实际尺寸和位置
        int viewWidth = cameraView.getWidth();
        int viewHeight = cameraView.getHeight();
        if (viewWidth == 0 || viewHeight == 0) return;

        int[] cameraLoc = new int[2];
        cameraView.getLocationOnScreen(cameraLoc);
        int viewLeft = cameraLoc[0];
        int viewTop = cameraLoc[1];

        // 2. 触摸点相对于 View 左上角的坐标
        float touchXInView = rawX - viewLeft;
        float touchYInView = rawY - viewTop;

        // ============================================================
        // 关键算法修正：计算图像在 View 中的实际显示区域 (保持长宽比)
        // ============================================================

        float scale;
        float realDisplayWidth, realDisplayHeight;
        float offsetX, offsetY;

        // 计算宽高缩放比
        float scaleX = (float) viewWidth / opencvImageWidth;
        float scaleY = (float) viewHeight / opencvImageHeight;

        // OpenCV 采用的是 Center-Inside 模式，取较小的缩放比，保证图像完全显示且不变形
        scale = Math.min(scaleX, scaleY);

        // 计算图像在屏幕上实际占据的宽和高
        realDisplayWidth = opencvImageWidth * scale;
        realDisplayHeight = opencvImageHeight * scale;

        // 计算黑边的大小 (View尺寸 - 实际图像尺寸) / 2
        offsetX = (viewWidth - realDisplayWidth) / 2;
        offsetY = (viewHeight - realDisplayHeight) / 2;

        // 3. 判断点击是否落在有效图像区域内 (排除黑边)
        if (touchXInView < offsetX || touchXInView > (offsetX + realDisplayWidth) ||
                touchYInView < offsetY || touchYInView > (offsetY + realDisplayHeight)) {
            Log.w(TAG, "点击在了黑边区域，忽略");
            clearCircle();
            return;
        }

        // 4. 坐标映射：(View相对坐标 - 黑边偏移) / 缩放比 = OpenCV图像坐标
        float mappedX = (touchXInView - offsetX) / scale;
        float mappedY = (touchYInView - offsetY) / scale;

        // 双重保险：限制在图像范围内
        if (mappedX < 0) mappedX = 0;
        if (mappedX > opencvImageWidth) mappedX = opencvImageWidth;
        if (mappedY < 0) mappedY = 0;
        if (mappedY > opencvImageHeight) mappedY = opencvImageHeight;

        // 5. 更新 DrawView 用于绘制红色标记 (UI 坐标)
        // DrawView 是全屏覆盖的，所以直接用 rawX, rawY 即可，
        // 但需要减去 DrawView 自身的屏幕偏移 (万一 DrawView 不是全屏)
        if (drawView != null) {
            int[] drawLoc = new int[2];
            drawView.getLocationOnScreen(drawLoc);
            drawLocalX = rawX - drawLoc[0];
            drawLocalY = rawY - drawLoc[1];
            drawView.invalidate();
        }

        Log.d(TAG, String.format("屏幕点(%.1f, %.1f) -> 图像点(%.1f, %.1f) [缩放:%.2f, 偏移X:%.1f]",
                rawX, rawY, mappedX, mappedY, scale, offsetX));

        // 6. 回调结果
        if (listener != null) {
            listener.onTouchCoordinate(mappedX, mappedY);
        }
    }

    public void setupForCameraView(JavaCameraView cameraView, int imgWidth, int imgHeight) {
        this.cameraView = cameraView;
        this.opencvImageWidth = imgWidth;
        this.opencvImageHeight = imgHeight;

        // 移除旧视图
        if (drawView != null && drawView.getParent() instanceof FrameLayout) {
            ((FrameLayout) drawView.getParent()).removeView(drawView);
        }

        // 添加新的绘图层
        addDrawLayerToParent();
    }

    private void addDrawLayerToParent() {
        if (cameraView == null) return;

        View directParent = (View) cameraView.getParent();
        if (directParent instanceof FrameLayout) {
            initDrawView((FrameLayout) directParent);
        } else if (directParent.getParent() instanceof FrameLayout) {
            initDrawView((FrameLayout) directParent.getParent());
        } else {
            // 尝试直接添加到根布局（兼容性处理）
            Log.w(TAG, "CameraView父容器不是FrameLayout，可能导致绘制层级错误");
        }
    }

    private void initDrawView(FrameLayout container) {
        drawView = new DrawView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        container.addView(drawView, params);
    }

    public void clearCircle() {
        drawLocalX = -1;
        drawLocalY = -1;
        if (drawView != null) {
            drawView.invalidate();
        }
    }

    private class DrawView extends View {
        public DrawView(Context context) {
            super(context);
            setWillNotDraw(false);
            setClickable(false); // 穿透点击事件
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (drawLocalX != -1 && drawLocalY != -1) {
                // 绘制红点
                canvas.drawCircle(drawLocalX, drawLocalY, 40, circlePaint);
                // 绘制十字准星
                canvas.drawLine(drawLocalX - 40, drawLocalY, drawLocalX + 40, drawLocalY, circlePaint);
                canvas.drawLine(drawLocalX, drawLocalY - 40, drawLocalX, drawLocalY + 40, circlePaint);
            }
        }
    }

    public interface OnTouchCoordinateListener {
        void onTouchCoordinate(float opencvImageX, float opencvImageY);
    }
}