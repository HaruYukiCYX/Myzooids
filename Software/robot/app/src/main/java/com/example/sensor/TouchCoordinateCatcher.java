package com.example.sensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import org.opencv.android.CameraBridgeViewBase;

public class TouchCoordinateCatcher {
    private static final String TAG = "TouchCoordinateCatcher";

    private OnTouchCoordinateListener listener;
    private Context context;
    private DrawView drawView;

    private float drawLocalX = -1;
    private float drawLocalY = -1;

    private CameraBridgeViewBase cameraView;
    private int opencvImageWidth;
    private int opencvImageHeight;

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

    public void setLongPressCoordinate(float rawX, float rawY) {
        if (cameraView == null || opencvImageWidth == 0 || opencvImageHeight == 0) return;

        int viewWidth = cameraView.getWidth();
        int viewHeight = cameraView.getHeight();
        if (viewWidth == 0 || viewHeight == 0) return;

        int[] cameraLoc = new int[2];
        cameraView.getLocationOnScreen(cameraLoc);
        int viewLeft = cameraLoc[0];
        int viewTop = cameraLoc[1];

        float touchXInView = rawX - viewLeft;
        float touchYInView = rawY - viewTop;

        float scale;
        float realDisplayWidth, realDisplayHeight;
        float offsetX, offsetY;

        float scaleX = (float) viewWidth / opencvImageWidth;
        float scaleY = (float) viewHeight / opencvImageHeight;

        scale = Math.min(scaleX, scaleY);

        realDisplayWidth = opencvImageWidth * scale;
        realDisplayHeight = opencvImageHeight * scale;

        offsetX = (viewWidth - realDisplayWidth) / 2;
        offsetY = (viewHeight - realDisplayHeight) / 2;

        if (touchXInView < offsetX || touchXInView > (offsetX + realDisplayWidth) ||
                touchYInView < offsetY || touchYInView > (offsetY + realDisplayHeight)) {
            clearCircle();
            return;
        }

        float mappedX = (touchXInView - offsetX) / scale;
        float mappedY = (touchYInView - offsetY) / scale;

        if (mappedX < 0) mappedX = 0;
        if (mappedX > opencvImageWidth) mappedX = opencvImageWidth;
        if (mappedY < 0) mappedY = 0;
        if (mappedY > opencvImageHeight) mappedY = opencvImageHeight;

        if (drawView != null) {
            int[] drawLoc = new int[2];
            drawView.getLocationOnScreen(drawLoc);
            drawLocalX = rawX - drawLoc[0];
            drawLocalY = rawY - drawLoc[1];
            drawView.invalidate();
        }

        if (listener != null) {
            listener.onTouchCoordinate(mappedX, mappedY);
        }
    }

    public void setupForCameraView(CameraBridgeViewBase cameraView, int imgWidth, int imgHeight) {
        this.cameraView = cameraView;
        this.opencvImageWidth = imgWidth;
        this.opencvImageHeight = imgHeight;

        if (drawView != null && drawView.getParent() instanceof FrameLayout) {
            ((FrameLayout) drawView.getParent()).removeView(drawView);
        }

        addDrawLayerToParent();
    }

    private void addDrawLayerToParent() {
        if (cameraView == null) return;

        View directParent = (View) cameraView.getParent();
        if (directParent instanceof FrameLayout) {
            initDrawView((FrameLayout) directParent);
        } else if (directParent.getParent() instanceof FrameLayout) {
            initDrawView((FrameLayout) directParent.getParent());
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
            setClickable(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (drawLocalX != -1 && drawLocalY != -1) {
                canvas.drawCircle(drawLocalX, drawLocalY, 40, circlePaint);
                canvas.drawLine(drawLocalX - 40, drawLocalY, drawLocalX + 40, drawLocalY, circlePaint);
                canvas.drawLine(drawLocalX, drawLocalY - 40, drawLocalX, drawLocalY + 40, circlePaint);
            }
        }
    }

    public interface OnTouchCoordinateListener {
        void onTouchCoordinate(float opencvImageX, float opencvImageY);
    }
}