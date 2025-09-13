package com.example.sensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import org.opencv.android.JavaCameraView;

public class TouchCoordinateCatcher implements View.OnTouchListener {
    private static final String TAG = "TouchCoordinateCatcher";

    private OnTouchCoordinateListener listener;
    private GestureDetector gestureDetector;
    private Context context;
    private DrawView drawView;
    private float drawLocalX = -1; // DrawView局部X坐标（用于绘制）
    private float drawLocalY = -1; // DrawView局部Y坐标（用于绘制）
    private float opencvImageX = -1; // OpenCV图像坐标系X
    private float opencvImageY = -1; // OpenCV图像坐标系Y
    private Paint circlePaint;
    private JavaCameraView cameraView;
    private int cameraViewDisplayWidth; // 相机视图显示宽度
    private int cameraViewDisplayHeight; // 相机视图显示高度
    private int opencvImageWidth; // OpenCV图像宽度
    private int opencvImageHeight; // OpenCV图像高度
    private int drawViewLeft; // DrawView左上角的屏幕X坐标
    private int drawViewTop; // DrawView左上角的屏幕Y坐标

    public TouchCoordinateCatcher(Context context, OnTouchCoordinateListener listener) {
        this.context = context;
        this.listener = listener;
        this.gestureDetector = new GestureDetector(context, new GestureListener());
        initPaint();
    }

    private void initPaint() {
        circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(5f);
        circlePaint.setAntiAlias(true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            if (cameraView == null || opencvImageWidth == 0 || opencvImageHeight == 0) {
                Log.w(TAG, "相机视图未初始化或图像尺寸无效");
                return true;
            }

            // 1. 获取屏幕绝对坐标（从屏幕左上角开始计算）
            float rawX = e.getRawX();
            float rawY = e.getRawY();
            Log.d(TAG, "屏幕绝对坐标: X=" + rawX + ", Y=" + rawY);

            // 2. 获取相机视图在屏幕上的位置
            int[] cameraLoc = new int[2];
            cameraView.getLocationOnScreen(cameraLoc);
            int cameraLeft = cameraLoc[0];
            int cameraTop = cameraLoc[1];
            Log.d(TAG, "相机视图位置: left=" + cameraLeft + ", top=" + cameraTop);

            // 3. 计算相机视图内的相对坐标
            float relativeX = rawX - cameraLeft;
            float relativeY = rawY - cameraTop;
            Log.d(TAG, "相机视图内相对坐标: X=" + relativeX + ", Y=" + relativeY);

            // 过滤超出相机视图范围的点击
            if (relativeX < 0 || relativeX > cameraViewDisplayWidth ||
                    relativeY < 0 || relativeY > cameraViewDisplayHeight) {
                Log.w(TAG, "点击位置超出相机视图范围");
                clearCircle();
                return true;
            }

            // 4. 转换为OpenCV图像坐标系坐标
            opencvImageX = relativeX * ((float) opencvImageWidth / cameraViewDisplayWidth);
            opencvImageY = relativeY * ((float) opencvImageHeight / cameraViewDisplayHeight);
            Log.d(TAG, "转换后OpenCV坐标: X=" + opencvImageX + ", Y=" + opencvImageY);

            // 5. 转换为DrawView局部坐标（关键修正）
            drawLocalX = rawX - drawViewLeft;
            drawLocalY = rawY - drawViewTop;
            Log.d(TAG, "DrawView局部坐标: X=" + drawLocalX + ", Y=" + drawLocalY);

            // 6. 回调OpenCV坐标并刷新绘制
            if (listener != null) {
                listener.onTouchCoordinate(opencvImageX, opencvImageY);
            }
            if (drawView != null) {
                drawView.invalidate();
            }

            return true;
        }
    }

    public void setupForCameraView(JavaCameraView cameraView, int opencvImageWidth, int opencvImageHeight) {
        this.cameraView = cameraView;
        this.opencvImageWidth = opencvImageWidth;
        this.opencvImageHeight = opencvImageHeight;

        // 移除旧的DrawView（如果存在）
        if (drawView != null && drawView.getParent() instanceof FrameLayout) {
            ((FrameLayout) drawView.getParent()).removeView(drawView);
        }

        // 获取相机视图显示尺寸（布局完成后）
        cameraView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                cameraViewDisplayWidth = cameraView.getWidth();
                cameraViewDisplayHeight = cameraView.getHeight();
                Log.d(TAG, "相机视图显示尺寸: 宽=" + cameraViewDisplayWidth + ", 高=" + cameraViewDisplayHeight);

                // 移除监听器
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    cameraView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    cameraView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        // 添加绘制层到相机父容器
        addDrawLayerToParent();
        cameraView.setOnTouchListener(this);
    }

    private void addDrawLayerToParent() {
        // 获取相机视图的直接父容器
        View directParent = (View) cameraView.getParent();
        if (directParent instanceof FrameLayout) {
            initDrawView((FrameLayout) directParent);
        } else if (directParent.getParent() instanceof FrameLayout) {
            initDrawView((FrameLayout) directParent.getParent());
        } else {
            Log.e(TAG, "无法找到合适的父容器放置DrawView");
        }
    }

    private void initDrawView(FrameLayout container) {
        drawView = new DrawView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        params.leftMargin = 0;
        params.topMargin = 0;
        params.rightMargin = 0;
        params.bottomMargin = 0;
        drawView.setLayoutParams(params);
        container.addView(drawView);

        // 监听DrawView布局完成，获取其在屏幕上的位置
        drawView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int[] drawLoc = new int[2];
                drawView.getLocationOnScreen(drawLoc);
                drawViewLeft = drawLoc[0];
                drawViewTop = drawLoc[1];

                // 移除监听器
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    drawView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    drawView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private class DrawView extends View {
        public DrawView(Context context) {
            super(context);
            setWillNotDraw(false); // 允许绘制
            setClickable(false);   // 不拦截触摸事件
            setPadding(0, 0, 0, 0); // 确保无内边距
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // 使用DrawView局部坐标绘制，确保位置准确
            if (drawLocalX != -1 && drawLocalY != -1) {
                canvas.drawCircle(drawLocalX, drawLocalY, 50, circlePaint);
                // 可添加十字线辅助定位
                canvas.drawLine(drawLocalX - 30, drawLocalY, drawLocalX + 30, drawLocalY, circlePaint);
                canvas.drawLine(drawLocalX, drawLocalY - 30, drawLocalX, drawLocalY + 30, circlePaint);
            }
        }
    }

    public void clearCircle() {
        drawLocalX = -1;
        drawLocalY = -1;
        if (drawView != null) {
            drawView.invalidate();
        }
    }

    public interface OnTouchCoordinateListener {
        void onTouchCoordinate(float opencvImageX, float opencvImageY);
    }
}
