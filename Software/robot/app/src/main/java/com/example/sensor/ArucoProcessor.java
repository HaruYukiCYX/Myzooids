package com.example.sensor;

import android.util.Log;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ArucoProcessor {
    private static final String TAG = "ArucoProcessor";

    private Dictionary arucoDictionary;
    private org.opencv.aruco.DetectorParameters detectorParams;

    // 内存复用
    private Mat grayBuffer;
    private Mat ids;
    private List<Mat> corners;

    // 持久化数据
    private List<MatOfPoint2f> persistentCorners = new ArrayList<>();
    private List<Integer> persistentIds = new ArrayList<>();

    private boolean isDetected = false;
    private Point latestCenter = new Point(0, 0);
    private double latestAngleRad = 0.0;

    private int trackedMarkerId = -1;
    private static final Scalar RECT_COLOR = new Scalar(0, 255, 0); // 绿框
    private static final Scalar HEAD_COLOR = new Scalar(255, 0, 0); // 红头
    private static final Scalar TEXT_COLOR = new Scalar(0, 255, 255); // 黄字

    public ArucoProcessor() {
        init();
    }

    private void init() {
        try {
            arucoDictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);
            detectorParams = org.opencv.aruco.DetectorParameters.create();
            detectorParams.set_cornerRefinementMethod(0);

            grayBuffer = new Mat();
            ids = new Mat();
            corners = new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Init failed", e);
        }
    }

    public void setTrackedMarkerId(int id) { this.trackedMarkerId = id; }

    /**
     * 计算阶段
     */
    public void detect(Mat frame) {
        if (frame == null || frame.empty()) return;

        try {
            if (grayBuffer == null) grayBuffer = new Mat();
            Imgproc.cvtColor(frame, grayBuffer, Imgproc.COLOR_RGBA2GRAY);

            corners.clear();
            Aruco.detectMarkers(grayBuffer, arucoDictionary, corners, ids, detectorParams);

            List<MatOfPoint2f> currentCorners = new ArrayList<>();
            List<Integer> currentIds = new ArrayList<>();

            boolean found = false;

            if (ids.total() > 0) {
                for (int i = 0; i < corners.size(); i++) {
                    int id = (int) ids.get(i, 0)[0];

                    if (trackedMarkerId == -1 || id == trackedMarkerId) {
                        MatOfPoint2f corner2f = new MatOfPoint2f();
                        corners.get(i).convertTo(corner2f, CvType.CV_32FC2);

                        currentCorners.add(corner2f);
                        currentIds.add(id);

                        if (!found) {
                            Point[] pts = corner2f.toArray();
                            double centerX = 0, centerY = 0;
                            for (Point p : pts) { centerX += p.x; centerY += p.y; }
                            latestCenter = new Point(centerX / 4.0, centerY / 4.0);

                            double dx = pts[1].x - pts[0].x;
                            double dy = pts[1].y - pts[0].y;
                            latestAngleRad = Math.atan2(dy, dx);
                            found = true;
                        }
                    }
                }
            }

            isDetected = found;
            persistentCorners = currentCorners;
            persistentIds = currentIds;

        } catch (Exception e) {
            Log.e(TAG, "Detection Error", e);
            isDetected = false;
        }
    }

    /**
     * 绘制阶段
     */
    public void draw(Mat frame) {
        if (persistentCorners == null || persistentCorners.isEmpty()) return;

        try {
            for (int i = 0; i < persistentCorners.size(); i++) {
                Point[] pts = persistentCorners.get(i).toArray();
                int id = persistentIds.get(i);

                // 1. 画绿框
                for (int j = 0; j < 4; j++) {
                    Imgproc.line(frame, pts[j], pts[(j + 1) % 4], RECT_COLOR, 3);
                }

                // 2. 画红头
                Imgproc.line(frame, pts[0], pts[1], HEAD_COLOR, 4);

                // 3. 【优化】显示 ID
                // 位置：基于角点0 (左上)，向上偏移 20 像素，防止覆盖边框
                Point textPos = new Point(pts[0].x, pts[0].y - 20);

                // 参数：
                // 字体：FONT_HERSHEY_SIMPLEX
                // 大小：0.5 (缩小)
                // 颜色：黄色
                // 粗细：2 (变细)
                Imgproc.putText(frame, "ID:" + id, textPos, Core.FONT_HERSHEY_SIMPLEX, 0.5, TEXT_COLOR, 2);
            }
        } catch (Exception e) {
            // 忽略绘制错误
        }
    }

    public boolean isDetected() { return isDetected; }
    public Point getCenter() { return latestCenter; }
    public double getAngleRad() { return latestAngleRad; }

    public void release() {
        if (grayBuffer != null) grayBuffer.release();
        if (ids != null) ids.release();
    }
}