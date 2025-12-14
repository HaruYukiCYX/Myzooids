package com.example.sensor;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;

public class PositionControlManager {
    private static final String TAG = "PositionControlManager";

    // ================== 调试开关（关键） ==================
    // 1. 如果目标在右，机器人往左转（反了），请把这里改为 false
    //    如果目标在右，机器人往右转（对了），请保持 true
    private static final boolean REVERSE_STEERING = true;

    // 2. 如果目标在前方，机器人后退（反了），请把这里改为 true
    //    如果目标在前方，机器人前进（对了），请保持 false
    private static final boolean REVERSE_FORWARD = false;
    // ====================================================

    // 速度限制
    private static final int MAX_MOTOR_SPEED = 35 ;

    // 蓝牙相关
    private BleManager bleManager;
    public interface ControlModeListener {
        MainActivity3.ControlMode getCurrentMode();
    }
    private ControlModeListener modeListener;

    // PID控制器
    private PIDController xAxisPid;
    private PIDController yAxisPid;

    private double targetX = 0;
    private double targetY = 0;
    private double currentX = 0;
    private double currentY = 0;

    private int controlInterval = 50;
    private double errorTolerance = 0.5;
    private double pixelToCmRatio = 0.1;

    private int deadZone = 15;
    private float turnCompensationRatio = 0.3f;

    private boolean isControlling = false;
    private Handler controlHandler = new Handler();

    private static final byte HEADER = (byte) 0xAA;
    private static final byte POSITIONING_COMMAND = (byte) 0x02;
    private static final byte FRAME_END = (byte) 0x55;

    public PositionControlManager(BleManager bleManager, ControlModeListener listener) {
        this.bleManager = bleManager;
        this.modeListener = listener;

        // PID参数
        xAxisPid = new PIDController(0.15f, 0.005f, 0.05f, 30, -30, 10);
        yAxisPid = new PIDController(0.35f, 0.01f, 0.15f, MAX_MOTOR_SPEED, -MAX_MOTOR_SPEED, 20);
    }

    // Getters & Setters
    public void updateCurrentPosition(double x, double y) { this.currentX = x; this.currentY = y; }
    public void setTargetPosition(double x, double y) { this.targetX = x; this.targetY = y; xAxisPid.reset(); yAxisPid.reset(); }
    public boolean isControlling() { return isControlling; }
    public void setControlInterval(int interval) { this.controlInterval = interval; }
    public void setErrorTolerance(double tolerance) { this.errorTolerance = tolerance; }
    public void setDeadZone(int deadZone) { this.deadZone = deadZone; }
    public void setTurnCompensationRatio(float ratio) { this.turnCompensationRatio = ratio; }
    public double getPixelToCmRatio() { return pixelToCmRatio; }

    public void startControl() {
        if (isControlling || bleManager == null || !bleManager.isConnected()
                || modeListener == null || modeListener.getCurrentMode() != MainActivity3.ControlMode.AUTO) {
            return;
        }
        isControlling = true;
        controlHandler.post(controlRunnable);
    }

    public void stopControl() {
        isControlling = false;
        controlHandler.removeCallbacks(controlRunnable);
        try { sendMotorSpeed(0, 0); } catch (IOException e) {}
    }

    private Runnable controlRunnable = new Runnable() {
        private long lastTime = System.currentTimeMillis();

        @Override
        public void run() {
            if (!isControlling || modeListener == null || modeListener.getCurrentMode() != MainActivity3.ControlMode.AUTO) {
                stopControl();
                return;
            }

            long currentTime = System.currentTimeMillis();
            int deltaTime = (int) (currentTime - lastTime);
            lastTime = currentTime;

            // 1. 计算原始偏差
            // DeltaX > 0 表示目标在右边
            double deltaX = targetX - currentX;

            // DeltaY > 0 表示目标在上方（Y轴向下增大，所以是 Current - Target）
            // 如果 REVERSE_FORWARD 为 true，则反转逻辑
            double deltaY = REVERSE_FORWARD ? (targetY - currentY) : (currentY - targetY);

            // 2. 死区处理
            if (Math.abs(deltaX) < deadZone) deltaX = 0;
            if (Math.abs(deltaY) < deadZone) deltaY = 0;

            // 3. 距离判断
            double pixelDistanceError = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if ((pixelDistanceError * pixelToCmRatio) < errorTolerance) {
                stopControl();
                return;
            }

            // 4. PID计算
            float baseSpeed = (float) yAxisPid.compute(0, (float) deltaY, deltaTime);
            float turnOutput = (float) xAxisPid.compute(0, (float) deltaX, deltaTime);

            // 5. 转向限幅
            float maxTurn = Math.abs(baseSpeed) * turnCompensationRatio;
            if (maxTurn < 10) maxTurn = 10;
            if (turnOutput > maxTurn) turnOutput = maxTurn;
            if (turnOutput < -maxTurn) turnOutput = -maxTurn;

            // ============================================================
            // 核心修复逻辑：反转左右混控
            // ============================================================
            int leftSpeed, rightSpeed;

            if (REVERSE_STEERING) {
                leftSpeed = (int) (baseSpeed - turnOutput);
                rightSpeed = (int) (baseSpeed + turnOutput);
            } else {
                // 【正常模式】(你之前的代码)
                leftSpeed = (int) (baseSpeed + turnOutput);
                rightSpeed = (int) (baseSpeed - turnOutput);
            }

            // 6. 最终限幅
            leftSpeed = clamp(leftSpeed, -MAX_MOTOR_SPEED, MAX_MOTOR_SPEED);
            rightSpeed = clamp(rightSpeed, -MAX_MOTOR_SPEED, MAX_MOTOR_SPEED);

            try {
                sendMotorSpeed(leftSpeed, rightSpeed);
            } catch (IOException e) {
                stopControl();
            }

            controlHandler.postDelayed(this, controlInterval);
        }
    };

    private void sendMotorSpeed(int leftSpeed, int rightSpeed) throws IOException {
        if (bleManager == null || !bleManager.isConnected()) return;

        byte[] packet = new byte[6];
        packet[0] = HEADER;
        packet[1] = POSITIONING_COMMAND;
        packet[2] = (byte) leftSpeed;
        packet[3] = (byte) rightSpeed;
        packet[4] = (byte) (packet[0] ^ packet[1] ^ packet[2] ^ packet[3]);
        packet[5] = FRAME_END;

        bleManager.sendData(packet);
    }

    private int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}