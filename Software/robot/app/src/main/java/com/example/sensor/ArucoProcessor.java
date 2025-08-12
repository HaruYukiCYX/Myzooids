package com.example.sensor;

import android.util.Log;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvException;

import java.util.ArrayList;
import java.util.List;

public class ArucoProcessor {
    private static final String TAG = "ArucoProcessor";
    private Dictionary arucoDictionary;
    private org.opencv.aruco.DetectorParameters detectorParams;

    // 样式配置
    private static final int MARKER_LINE_WIDTH = 4;  // 加粗边框
    private static final float AXIS_LENGTH = 50;     // 轴长度
    private static final Scalar MARKER_COLOR = new Scalar(0, 255, 0);   // 绿色边框
    private static final Scalar X_AXIS_COLOR = new Scalar(0, 0, 255);   // 红色X轴
    private static final Scalar Y_AXIS_COLOR = new Scalar(0, 255, 0);   // 绿色Y轴
    private static final Scalar Z_AXIS_COLOR = new Scalar(255, 0, 0);   // 蓝色Z轴
    private static final Scalar TEXT_COLOR = new Scalar(0, 0, 255);     // 红色ID文本

    // 相机参数
    private Mat cameraMatrix;
    private MatOfDouble distCoeffs;

    public ArucoProcessor() {
        try {
            // 1. 初始化Aruco字典
            arucoDictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_250);

            // 2. 初始化检测器参数（兼容3.4.4版本）
            detectorParams = org.opencv.aruco.DetectorParameters.create();


            // 3. 初始化相机内参
            cameraMatrix = Mat.eye(3, 3, CvType.CV_64FC1);
            distCoeffs = new MatOfDouble(0, 0, 0, 0, 0); // 无畸变假设

            // 示例内参（根据实际设备调整）
            cameraMatrix.put(0, 0, 960);  // fx
            cameraMatrix.put(0, 2, 640);  // cx
            cameraMatrix.put(1, 1, 960);  // fy
            cameraMatrix.put(1, 2, 360);  // cy

            Log.d(TAG, "初始化完成，相机矩阵:\n" + cameraMatrix.dump());
        } catch (Exception e) {
            Log.e(TAG, "初始化失败: " + e.getMessage());
        }
    }

    public Mat processFrame(Mat frame) {
        if (frame == null || frame.empty()) {
            Log.e(TAG, "输入帧为空");
            return frame;
        }

        // 转换为灰度图
        Mat gray = new Mat();
        try {
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY);
        } catch (Exception e) {
            Log.e(TAG, "灰度转换失败: " + e.getMessage());
            gray.release();
            return frame;
        }

        // 检测Aruco标记
        List<Mat> corners = new ArrayList<>();
        Mat ids = new Mat();
        try {
            Aruco.detectMarkers(gray, arucoDictionary, corners, ids, detectorParams);
            Log.d(TAG, "检测到标记数量: " + ids.total());
        } catch (Exception e) {
            Log.e(TAG, "标记检测失败: " + e.getMessage());
            gray.release();
            ids.release();
            return frame;
        }

        // 处理检测结果
        if (ids.total() > 0) {
            // 转换角点为MatOfPoint2f并校验格式
            List<MatOfPoint2f> validCorners = new ArrayList<>();
            for (Mat cornerMat : corners) {
                // 强制转换为CV_32FC2格式
                MatOfPoint2f corner = new MatOfPoint2f();
                cornerMat.convertTo(corner, CvType.CV_32FC2);

                // 校验2D点数量
                int vectorCount = corner.checkVector(2);
                Log.d(TAG, "角点向量校验结果: " + vectorCount + " (期望4)");

                if (vectorCount == 4) {
                    validCorners.add(corner);
                } else {
                    Log.w(TAG, "无效角点集（数量=" + vectorCount + "），已过滤");
                    corner.release();
                }
            }

            // 绘制有效标记
            drawValidMarkers(frame, validCorners, ids);
        }

        // 释放资源
        gray.release();
        ids.release();
        for (Mat corner : corners) corner.release();

        return frame;
    }

    /**
     * 绘制有效的标记（含加粗边框、ID和姿态轴）
     */
    private void drawValidMarkers(Mat frame, List<MatOfPoint2f> validCorners, Mat ids) {
        // Prepare 3D reference points for ArUco marker (100mm x 100mm)
        MatOfPoint3f objPoints = new MatOfPoint3f(
                new Point3(-50, -50, 0), // Left-top
                new Point3(50, -50, 0),  // Right-top
                new Point3(50, 50, 0),   // Right-bottom
                new Point3(-50, 50, 0)   // Left-bottom
        );
        objPoints.convertTo(objPoints, CvType.CV_32FC3);

        // Validate camera parameters
        if (cameraMatrix.empty() || distCoeffs.empty()) {
            Log.e(TAG, "Camera matrix or distortion coefficients are empty");
            objPoints.release();
            return;
        }

        // Traverse valid markers
        for (int i = 0; i < validCorners.size() && i < ids.total(); i++) {
            MatOfPoint2f corner = validCorners.get(i);
            int markerId = (int) ids.get(i, 0)[0];

            // Validate corner points
            if (corner.checkVector(2, CvType.CV_32F) != 4) {
                Log.e(TAG, "Invalid corner points for marker ID=" + markerId + ": " + corner.dump());
                continue;
            }

            // Draw marker border
            Point[] points = corner.toArray();
            for (int j = 0; j < 4; j++) {
                Imgproc.line(frame, points[j], points[(j + 1) % 4], MARKER_COLOR, MARKER_LINE_WIDTH);
            }

            // Draw ID text
            Point center = new Point(
                    (points[0].x + points[1].x + points[2].x + points[3].x) / 4,
                    (points[0].y + points[1].y + points[2].y + points[3].y) / 4
            );
            Imgproc.putText(frame, String.valueOf(markerId), center,
                    Core.FONT_HERSHEY_SIMPLEX, 0.8, TEXT_COLOR, 2);

            // Calculate pose
            Mat rvec = new Mat();
            Mat tvec = new Mat();
            try {
                boolean success = Calib3d.solvePnP(
                        objPoints,
                        corner,
                        cameraMatrix,
                        distCoeffs,
                        rvec,
                        tvec,
                        false
                );
 
                if (!success) {
                    Log.e(TAG, "solvePnP failed for marker ID=" + markerId);
                    continue;
                }

                // Project 3D axes
                MatOfPoint3f axis3D = new MatOfPoint3f(
                        new Point3(0, 0, 0),
                        new Point3(AXIS_LENGTH, 0, 0),  // X-axis
                        new Point3(0, AXIS_LENGTH, 0),  // Y-axis
                        new Point3(0, 0, AXIS_LENGTH)   // Z-axis (pointing outward)
                );
                MatOfPoint2f axis2D = new MatOfPoint2f();
                Calib3d.projectPoints(axis3D, rvec, tvec, cameraMatrix, distCoeffs, axis2D);
                Point[] axisPoints = axis2D.toArray();

                // Draw axes
                if (axisPoints.length >= 4) {
                    Imgproc.line(frame, axisPoints[0], axisPoints[1], X_AXIS_COLOR, MARKER_LINE_WIDTH);
                    Imgproc.line(frame, axisPoints[0], axisPoints[2], Y_AXIS_COLOR, MARKER_LINE_WIDTH);
                    Imgproc.line(frame, axisPoints[0], axisPoints[3], Z_AXIS_COLOR, MARKER_LINE_WIDTH);
                }

                axis3D.release();
                axis2D.release();
            } catch (CvException e) {
                Log.e(TAG, "solvePnP exception for marker ID=" + markerId + ": " + e.getMessage());
            } finally {
                rvec.release();
                tvec.release();
            }
        }
        objPoints.release();
    }
}
