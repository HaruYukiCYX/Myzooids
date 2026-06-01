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

    // 持久化数据（用于画面连贯渲染）
    private List<MatOfPoint2f> persistentCorners = new ArrayList<>();
    private List<Integer> persistentIds = new ArrayList<>();

    private boolean isDetected = false;
    private Point latestCenter = new Point(0, 0);
    private double latestAngleRad = 0.0;

    private int trackedMarkerId = -1;

    // 视觉渲染颜色定义
    private static final Scalar RECT_COLOR = new Scalar(0, 255, 0);     // 绿框
    private static final Scalar HEAD_COLOR = new Scalar(255, 0, 0);     // 红色方向箭头
    private static final Scalar TEXT_COLOR = new Scalar(0, 255, 255);   // 黄字
    private static final Scalar TEXT_BORDER = new Scalar(0, 0, 0);      // 黑边(防看不清)

    public ArucoProcessor() {
        init();
    }

    private void init() {
        try {
            arucoDictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);
            detectorParams = org.opencv.aruco.DetectorParameters.create();

            // 开启亚像素角点优化，彻底根除边框闪烁和跳动
            detectorParams.set_cornerRefinementMethod(1);

            grayBuffer = new Mat();
            ids = new Mat();
            corners = new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Init failed", e);
        }
    }

    public void setTrackedMarkerId(int id) { this.trackedMarkerId = id; }

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

                            Point topCenter = new Point((pts[0].x + pts[1].x) / 2.0, (pts[0].y + pts[1].y) / 2.0);
                            Point bottomCenter = new Point((pts[2].x + pts[3].x) / 2.0, (pts[2].y + pts[3].y) / 2.0);
                            double dx = bottomCenter.x - topCenter.x;
                            double dy = bottomCenter.y - topCenter.y;
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

    public void draw(Mat frame) {
        if (persistentCorners == null || persistentCorners.isEmpty()) return;

        try {
            for (int i = 0; i < persistentCorners.size(); i++) {
                Point[] pts = persistentCorners.get(i).toArray();
                int id = persistentIds.get(i);

                // 1. 画抗锯齿绿色外框 (线宽6)
                for (int j = 0; j < 4; j++) {
                    Imgproc.line(frame, pts[j], pts[(j + 1) % 4], RECT_COLOR, 6, Imgproc.LINE_AA, 0);
                }

                // 2. 画红色方向箭头（从标记顶部中心，指向底部中心）
                Point topCenter = new Point((pts[0].x + pts[1].x) / 2.0, (pts[0].y + pts[1].y) / 2.0);
                Point bottomCenter = new Point((pts[2].x + pts[3].x) / 2.0, (pts[2].y + pts[3].y) / 2.0);
                Imgproc.arrowedLine(frame, topCenter, bottomCenter, HEAD_COLOR, 6, Imgproc.LINE_AA, 0, 0.2);

                // 3. 画高对比度 ID 文本
                Point textPos = new Point(pts[0].x, pts[0].y - 20);
                Imgproc.putText(frame, "ID:" + id, textPos, Core.FONT_HERSHEY_SIMPLEX, 0.6, TEXT_BORDER, 4, Imgproc.LINE_AA, false);
                Imgproc.putText(frame, "ID:" + id, textPos, Core.FONT_HERSHEY_SIMPLEX, 0.6, TEXT_COLOR, 2, Imgproc.LINE_AA, false);
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