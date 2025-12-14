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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ArucoProcessor {
    private static final String TAG = "ArucoProcessor";

    // Aruco 核心对象
    private Dictionary arucoDictionary;
    private org.opencv.aruco.DetectorParameters detectorParams;

    // 线程与并发控制
    private ExecutorService detectionExecutor;
    private AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final Object cacheLock = new Object(); // 锁对象

    // 缓存结果（用于 UI 线程绘制，避免画面闪烁）
    private List<MatOfPoint2f> cachedCorners = new ArrayList<>();
    private List<Integer> cachedIds = new ArrayList<>();

    // 状态变量
    private volatile boolean isDetected = false; // 统一名称
    private volatile Point latestCenter = new Point(0, 0);
    private int trackedMarkerId = -1; // -1 表示追踪任意 ID
    private int lostFrameCount = 0;
    private static final int MAX_LOST_FRAMES = 15; // 容错帧数

    // 绘图样式
    private static final Scalar RECT_COLOR = new Scalar(0, 255, 0); // 绿色
    private static final Scalar TEXT_COLOR = new Scalar(255, 0, 0); // 红色

    public ArucoProcessor() {
        init();
    }

    private void init() {
        try {
            // 初始化字典 (4x4) 和参数
            arucoDictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);
            detectorParams = org.opencv.aruco.DetectorParameters.create();
            // 参数微调：提高检测率
            detectorParams.set_cornerRefinementMethod(1);

            // 单线程池：专门跑算法
            detectionExecutor = Executors.newSingleThreadExecutor();
        } catch (Exception e) {
            Log.e(TAG, "Aruco 初始化失败: " + e.getMessage());
        }
    }

    public void setTrackedMarkerId(int id) {
        this.trackedMarkerId = id;
    }

    /**
     * 核心方法：处理每一帧
     */
    public Mat processFrame(Mat frame) {
        if (frame == null || frame.empty()) return frame;

        // --- 异步检测逻辑 ---
        // 只有当后台空闲，且线程池未关闭时，才提交新任务
        if (!isProcessing.get() && detectionExecutor != null && !detectionExecutor.isShutdown()) {
            isProcessing.set(true);

            // 克隆灰度图（轻量级），原图 frame 会被相机覆盖，不能直接传
            final Mat grayFrame = new Mat();
            try {
                Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_RGBA2GRAY);

                detectionExecutor.execute(() -> runAsyncDetection(grayFrame));

            } catch (RejectedExecutionException e) {
                // 捕获线程池关闭异常（App退出时发生），防止崩溃
                isProcessing.set(false);
                grayFrame.release();
            } catch (Exception e) {
                isProcessing.set(false);
                grayFrame.release();
            }
        }

        // --- 绘制逻辑 ---
        // 始终使用最新的缓存结果绘制，保证画面流畅
        drawCachedResults(frame);

        return frame;
    }

    /**
     * 后台耗时任务
     */
    private void runAsyncDetection(Mat grayImage) {
        List<Mat> corners = new ArrayList<>();
        Mat ids = new Mat();

        try {
            // 运行检测算法
            Aruco.detectMarkers(grayImage, arucoDictionary, corners, ids, detectorParams);

            // 临时容器
            List<MatOfPoint2f> validCorners = new ArrayList<>();
            List<Integer> validIds = new ArrayList<>();
            boolean found = false;
            Point center = null;

            if (ids.total() > 0) {
                for (int i = 0; i < corners.size(); i++) {
                    int id = (int) ids.get(i, 0)[0];

                    // 转换数据格式
                    MatOfPoint2f corner2f = new MatOfPoint2f();
                    corners.get(i).convertTo(corner2f, CvType.CV_32FC2);

                    validCorners.add(corner2f);
                    validIds.add(id);

                    // 追踪逻辑
                    if (trackedMarkerId == -1 || id == trackedMarkerId) {
                        if (!found) { // 只锁定第一个
                            center = calculateCenter(corner2f);
                            found = true;
                        }
                    }
                }
            }

            // 同步更新状态
            synchronized (cacheLock) {
                cachedCorners = validCorners;
                cachedIds = validIds;

                if (found) {
                    isDetected = true;
                    latestCenter = center;
                    lostFrameCount = 0;
                } else {
                    lostFrameCount++;
                    if (lostFrameCount > MAX_LOST_FRAMES) {
                        isDetected = false;
                    }
                }
            }

        } catch (Exception e) {
            // 忽略一般计算错误
        } finally {
            // 必须释放内存
            grayImage.release();
            ids.release();
            for (Mat m : corners) m.release();

            // 标记任务完成
            isProcessing.set(false);
        }
    }

    private void drawCachedResults(Mat frame) {
        synchronized (cacheLock) {
            for (int i = 0; i < cachedCorners.size(); i++) {
                MatOfPoint2f corners = cachedCorners.get(i);
                int id = cachedIds.get(i);

                Point[] pts = corners.toArray();
                // 画框
                for (int j = 0; j < 4; j++) {
                    Imgproc.line(frame, pts[j], pts[(j + 1) % 4], RECT_COLOR, 3);
                }
                // 画ID
                Imgproc.putText(frame, "ID:" + id, pts[0], Core.FONT_HERSHEY_SIMPLEX, 0.8, TEXT_COLOR, 2);
            }

            // 调试信息：屏幕左上角显示状态
            if (isDetected) {
                Imgproc.putText(frame, "TRACKING", new Point(20, 40), Core.FONT_HERSHEY_SIMPLEX, 0.7, RECT_COLOR, 2);
            } else {
                Imgproc.putText(frame, "SEARCHING", new Point(20, 40), Core.FONT_HERSHEY_SIMPLEX, 0.7, TEXT_COLOR, 2);
            }
        }
    }

    private Point calculateCenter(MatOfPoint2f corners) {
        Point[] pts = corners.toArray();
        double x = 0, y = 0;
        for (Point p : pts) { x += p.x; y += p.y; }
        return new Point(x / 4.0, y / 4.0);
    }

    // --- 公开 Getter 方法 (与 MainActivity 对接) ---

    public boolean isDetected() {
        return isDetected;
    }

    public Point getCenter() {
        return latestCenter;
    }

    // 资源释放
    public void release() {
        if (detectionExecutor != null && !detectionExecutor.isShutdown()) {
            detectionExecutor.shutdownNow(); // 强行关闭
        }
        cachedCorners.clear();
    }
}