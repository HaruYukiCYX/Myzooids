package com.example.sensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import org.opencv.android.JavaCameraView;

public class TouchCoordinateCatcher {
    private static final String TAG = "TouchCoordinateCatcher";

    private OnTouchCoordinateListener listener;
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

    // 构造函数：移除原TouchListener，改为外部触发
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

    // --------------------------
    // 新增：外部调用（MainActivity3长按触发）
    // 传入长按的屏幕绝对坐标，触发坐标转换、画圈、回调
    // --------------------------
    public void setLongPressCoordinate(float rawX, float rawY) {
        if (cameraView == null || opencvImageWidth == 0 || opencvImageHeight == 0) {
            Log.w(TAG, "相机视图未初始化或图像尺寸无效");
            clearCircle();
            return;
        }

        // 1. 获取相机视图在屏幕上的位置（左上角坐标）
        int[] cameraLoc = new int[2];
        cameraView.getLocationOnScreen(cameraLoc);
        int cameraLeft = cameraLoc[0];
        int cameraTop = cameraLoc[1];
        Log.d(TAG, "相机视图位置: left=" + cameraLeft + ", top=" + cameraTop);

        // 2. 计算相机视图内的相对坐标（排除相机视图外的点击）
        float relativeX = rawX - cameraLeft;
        float relativeY = rawY - cameraTop;
        Log.d(TAG, "相机视图内相对坐标: X=" + relativeX + ", Y=" + relativeY);

        // 过滤超出相机视图范围的长按（清除旧圆圈）
        if (relativeX < 0 || relativeX > cameraViewDisplayWidth ||
                relativeY < 0 || relativeY > cameraViewDisplayHeight) {
            Log.w(TAG, "长按位置超出相机视图范围");
            clearCircle();
            return;
        }

        // 3. 转换为OpenCV图像坐标系（关键：匹配OpenCV帧尺寸）
        opencvImageX = relativeX * ((float) opencvImageWidth / cameraViewDisplayWidth);
        opencvImageY = relativeY * ((float) opencvImageHeight / cameraViewDisplayHeight);
        Log.d(TAG, "长按触发：OpenCV坐标=" + opencvImageX + ", " + opencvImageY);

        // 4. 转换为DrawView局部坐标（用于准确画圈）
        drawLocalX = rawX - drawViewLeft;
        drawLocalY = rawY - drawViewTop;
        Log.d(TAG, "DrawView局部坐标=" + drawLocalX + ", " + drawLocalY);

        // 5. 触发回调（给MainActivity3传OpenCV坐标）+ 刷新画圈
        if (listener != null) {
            listener.onTouchCoordinate(opencvImageX, opencvImageY);
        }
        if (drawView != null) {
            drawView.invalidate(); // 刷新绘制圆圈
        }
    }

    // 原有初始化方法：绑定相机视图、添加画图层
    public void setupForCameraView(JavaCameraView cameraView, int opencvImageWidth, int opencvImageHeight) {
        this.cameraView = cameraView;
        this.opencvImageWidth = opencvImageWidth;
        this.opencvImageHeight = opencvImageHeight;

        // 移除旧的DrawView（避免重复添加）
        if (drawView != null && drawView.getParent() instanceof FrameLayout) {
            ((FrameLayout) drawView.getParent()).removeView(drawView);
        }

        // 监听相机视图布局完成，获取显示尺寸
        cameraView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                cameraViewDisplayWidth = cameraView.getWidth();
                cameraViewDisplayHeight = cameraView.getHeight();
                Log.d(TAG, "相机视图显示尺寸: 宽=" + cameraViewDisplayWidth + ", 高=" + cameraViewDisplayHeight);

                // 移除监听器（避免重复触发）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    cameraView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    cameraView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        // 添加DrawView（画圈层）到相机父容器
        addDrawLayerToParent();
    }

    // 原有方法：添加画圈层到相机的父容器
    private void addDrawLayerToParent() {
        View directParent = (View) cameraView.getParent();
        // 优先找FrameLayout父容器（确保画圈层覆盖相机）
        if (directParent instanceof FrameLayout) {
            initDrawView((FrameLayout) directParent);
        } else if (directParent.getParent() instanceof FrameLayout) {
            initDrawView((FrameLayout) directParent.getParent());
        } else {
            Log.e(TAG, "无法找到合适的父容器放置DrawView（需FrameLayout）");
        }
    }

    // 原有方法：初始化画圈视图（DrawView）
    private void initDrawView(FrameLayout container) {
        drawView = new DrawView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        container.addView(drawView, params); // 覆盖在相机视图上

        // 监听DrawView布局完成，获取其屏幕位置（用于计算局部坐标）
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

    // 原有内部类：画圈视图（绘制红色圆圈+十字线）
    private class DrawView extends View {
        public DrawView(Context context) {
            super(context);
            setWillNotDraw(false); // 允许View绘制
            setClickable(false);   // 不拦截触摸事件（确保相机能接收长按）
            setPadding(0, 0, 0, 0);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // 长按坐标有效时，绘制圆圈和十字线
            if (drawLocalX != -1 && drawLocalY != -1) {
                canvas.drawCircle(drawLocalX, drawLocalY, 50, circlePaint); // 圆圈
                canvas.drawLine(drawLocalX - 30, drawLocalY, drawLocalX + 30, drawLocalY, circlePaint); // 水平十字线
                canvas.drawLine(drawLocalX, drawLocalY - 30, drawLocalX, drawLocalY + 30, circlePaint); // 垂直十字线
            }
        }
    }

    // 原有方法：清除圆圈
    public void clearCircle() {
        drawLocalX = -1;
        drawLocalY = -1;
        if (drawView != null) {
            drawView.invalidate();
        }
    }

    // 原有接口：回调OpenCV坐标给MainActivity3
    public interface OnTouchCoordinateListener {
        void onTouchCoordinate(float opencvImageX, float opencvImageY);
    }
}