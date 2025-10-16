package com.example.sensor;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;

public class PositionControlManager {
    private static final String TAG = "PositionControlManager";

    // 蓝牙相关
    private BleManager bleManager;

    // PID控制器（左右电机分别控制）
    private PIDController leftMotorPid;
    private PIDController rightMotorPid;

    // 坐标与目标
    private double targetX = 0;
    private double targetY = 0;
    private double currentX = 0;
    private double currentY = 0;

    // 控制参数
    private int controlInterval = 50; // 控制周期（ms）
    private double errorTolerance = 0.5; // 误差容忍度（cm）
    private double pixelToCmRatio = 0.1; // 像素到厘米的转换比例（10像素=1cm）

    // 状态控制
    private boolean isControlling = false;
    private Handler controlHandler = new Handler();

    // 数据包常量 - 添加帧尾
    private static final byte HEADER = (byte) 0xAA;
    private static final byte SPEED_COMMAND = 0x05; // 双电机转速命令
    private static final byte FRAME_END = (byte) 0x55; // 帧尾标识

    // 构造函数
    public PositionControlManager(BleManager bleManager) {
        this.bleManager = bleManager;

        // 初始化双电机PID控制器（参数示例，需实际调试）
        // 输出范围限制在-127~127（单字节）
        leftMotorPid = new PIDController(1.2f, 0.1f, 0.2f, 127, -127, 50);
        rightMotorPid = new PIDController(1.2f, 0.1f, 0.2f, 127, -127, 50);
    }

    // 更新当前位置（像素坐标）
    public void updateCurrentPosition(double x, double y) {
        this.currentX = x;
        this.currentY = y;
    }

    // 设置目标位置（像素坐标）
    public void setTargetPosition(double x, double y) {
        this.targetX = x;
        this.targetY = y;

        // 重置PID控制器
        leftMotorPid.reset();
        rightMotorPid.reset();
    }

    // 启动控制循环
    public void startControl() {
        if (isControlling || bleManager == null || !bleManager.isConnected()) {
            return;
        }

        isControlling = true;
        Log.d(TAG, "开始双电机控制");
        controlHandler.post(controlRunnable);
    }

    // 停止控制循环
    public void stopControl() {
        isControlling = false;
        controlHandler.removeCallbacks(controlRunnable);

        // 发送停止指令
        try {
            sendMotorSpeed(0, 0);
        } catch (IOException e) {
            Log.e(TAG, "发送停止指令失败: " + e.getMessage());
        }
        Log.d(TAG, "停止双电机控制");
    }

    // 控制循环逻辑
    private Runnable controlRunnable = new Runnable() {
        private long lastTime = System.currentTimeMillis();

        @Override
        public void run() {
            if (!isControlling) {
                return;
            }

            // 计算时间间隔（ms）
            long currentTime = System.currentTimeMillis();
            int deltaTime = (int) (currentTime - lastTime);
            lastTime = currentTime;

            // 1. 计算像素误差
            double pixelErrorX = targetX - currentX;
            double pixelErrorY = targetY - currentY;

            // 2. 转换为实际距离误差（厘米）
            double cmErrorX = pixelErrorX * pixelToCmRatio;
            double cmErrorY = pixelErrorY * pixelToCmRatio;

            // 3. 计算综合误差（距离）
            double distanceError = Math.sqrt(cmErrorX * cmErrorX + cmErrorY * cmErrorY);

            Log.d(TAG, "误差: " + String.format("%.2fcm", distanceError));

            // 4. 检查是否到达目标
            if (distanceError < errorTolerance) {
                stopControl();
                return;
            }

            // 5. 计算左右电机转速（通过PID）
            // 差速控制逻辑：Y误差控制前进/后退，X误差控制转向
            int baseSpeed = (int) rightMotorPid.compute(0, (float)cmErrorY, deltaTime);
            int turnSpeed = (int) leftMotorPid.compute(0, (float)cmErrorX, deltaTime);

            int leftSpeed = baseSpeed - turnSpeed;  // 左电机转速 = 基础速度 - 转向补偿
            int rightSpeed = baseSpeed + turnSpeed; // 右电机转速 = 基础速度 + 转向补偿

            // 6. 限制在单字节范围内（-127~127）
            leftSpeed = clamp(leftSpeed, -127, 127);
            rightSpeed = clamp(rightSpeed, -127, 127);

            // 7. 发送转速指令
            try {
                sendMotorSpeed(leftSpeed, rightSpeed);
                Log.d(TAG, "发送转速 - 左: " + leftSpeed + ", 右: " + rightSpeed);
            } catch (IOException e) {
                Log.e(TAG, "发送转速失败: " + e.getMessage());
            }

            // 8. 调度下一次控制
            controlHandler.postDelayed(this, controlInterval);
        }
    };

    // 发送双电机转速（带帧尾的格式）
    private void sendMotorSpeed(int leftSpeed, int rightSpeed) throws IOException {
        if (bleManager == null || !bleManager.isConnected()) {
            throw new IOException("蓝牙未连接");
        }

        // 数据包格式（6字节）
        // [0]帧头(0xAA) + [1]命令(0x05) + [2]左电机转速 + [3]右电机转速 + [4]校验位 + [5]帧尾(0x55)
        byte[] packet = new byte[6];
        packet[0] = HEADER;
        packet[1] = SPEED_COMMAND;
        packet[2] = (byte) leftSpeed;   // 左电机转速（单字节）
        packet[3] = (byte) rightSpeed;  // 右电机转速（单字节）

        // 计算校验位（前4字节异或）
        packet[4] = (byte) (packet[0] ^ packet[1] ^ packet[2] ^ packet[3]);

        // 添加帧尾
        packet[5] = FRAME_END;

        bleManager.sendData(packet);
    }

    // 辅助方法：限制值范围
    private int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    // 校准像素到厘米的转换比例
    public void calibratePixelToCm(double actualCm, double pixelDistance) {
        if (pixelDistance > 0) {
            this.pixelToCmRatio = actualCm / pixelDistance;
            Log.d(TAG, "校准完成，像素-厘米比例: " + pixelToCmRatio);
        }
    }

    // 设置PID参数
    public void setPidParameters(float kp, float ki, float kd) {
        leftMotorPid.setKp(kp);
        leftMotorPid.setKi(ki);
        leftMotorPid.setKd(kd);

        rightMotorPid.setKp(kp);
        rightMotorPid.setKi(ki);
        rightMotorPid.setKd(kd);
    }

    // 获取控制状态
    public boolean isControlling() {
        return isControlling;
    }

    // 设置控制周期（ms）
    public void setControlInterval(int interval) {
        this.controlInterval = interval;
    }

    // 设置误差容忍度（cm）
    public void setErrorTolerance(double tolerance) {
        this.errorTolerance = tolerance;
    }
}
    