package com.example.sensor;

import android.os.Handler;
import android.util.Log;

public class PositionControlManager {
    private static final String TAG = "PositionControlManager";

    public interface ControlModeListener {
        MainActivity3.ControlMode getCurrentMode();
    }

    /* ========================================================================== */
    /* 连续滑行优化版：解决“蹭过去”的断续感                                          */
    /* ========================================================================== */
    private static final int MAX_MOTOR_SPEED = 40;
    private int controlInterval = 50;

    private double errorTolerance = 20.0;
    private double turnLockThreshold = 60.0;

    // 【新增核心参数：左轮 0.75 硬件补偿】
    private static final float LEFT_WHEEL_COMPENSATION = 1.15f;

    private BleManager bleManager;
    private ControlModeListener modeListener;
    private Handler controlHandler = new Handler();

    private PIDController turnPid;
    private PIDController forwardPid;

    private double targetX = 0, targetY = 0;
    private double currentX = 0, currentY = 0, currentAngleRad = 0;
    private boolean isControlling = false;

    private long lastVisionUpdateTime = 0;

    private float smoothedLeft = 0;
    private float smoothedRight = 0;
    // 稍微调大 RAMP_FACTOR (0.15 -> 0.25)，让速度跟随更及时，减少滞后感
    private static final float RAMP_FACTOR = 0.25f;

    private static final byte HEADER = (byte) 0xAA;
    private static final byte POSITIONING_COMMAND = (byte) 0x02;
    private static final byte FRAME_END = (byte) 0x55;

    public PositionControlManager(BleManager bleManager, ControlModeListener listener) {
        this.bleManager = bleManager;
        this.modeListener = listener;

        // 转向环保持稳定
        turnPid = new PIDController(0.10f, 0.0f, 0.12f, 10, -10, 0);

        // 【前进环微调】：削弱 Kd (0.35 -> 0.15)，让刹车不再那么突兀，允许惯性滑行
        forwardPid = new PIDController(0.12f, 0.0f, 0.15f, MAX_MOTOR_SPEED, -MAX_MOTOR_SPEED, 0);
    }

    public void setControlInterval(int interval) { this.controlInterval = interval; }
    public void setErrorTolerance(double tolerance) { this.errorTolerance = tolerance; }

    public void updateCurrentPosition(double x, double y, double angleRad) {
        this.currentX = x;
        this.currentY = y;
        this.currentAngleRad = angleRad;
        this.lastVisionUpdateTime = System.currentTimeMillis();
    }

    public void setTargetPosition(double x, double y) {
        this.targetX = x;
        this.targetY = y;
        turnPid.reset();
        forwardPid.reset();
        smoothedLeft = 0;
        smoothedRight = 0;
    }

    public void startControl() {
        if (isControlling || bleManager == null || !bleManager.isConnected()) return;
        isControlling = true;
        lastVisionUpdateTime = System.currentTimeMillis();
        controlHandler.post(controlRunnable);
    }

    public void stopControl() {
        isControlling = false;
        controlHandler.removeCallbacks(controlRunnable);
        smoothedLeft = 0; smoothedRight = 0;
        try { sendMotorSpeed(0, 0); } catch (Exception e) { }
    }

    public void suspendControl() {
        smoothedLeft = 0; smoothedRight = 0;
        try { sendMotorSpeed(0, 0); } catch (Exception e) { }
    }

    private Runnable controlRunnable = new Runnable() {
        private long lastTime = System.currentTimeMillis();

        @Override
        public void run() {
            if (!isControlling) { stopControl(); return; }

            long currentTime = System.currentTimeMillis();
            long deltaTime = Math.max(1, currentTime - lastTime);
            lastTime = currentTime;

            if (currentTime - lastVisionUpdateTime > 150) {
                smoothedLeft = 0; smoothedRight = 0;
                try { sendMotorSpeed(0, 0); } catch (Exception e) {}
                controlHandler.postDelayed(this, controlInterval);
                return;
            }

            double dx = targetX - currentX;
            double dy = targetY - currentY;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist <= errorTolerance) {
                stopControl();
                return;
            }

            double targetAngle = Math.atan2(dy, dx);
            double angleDiff = targetAngle - currentAngleRad;
            while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
            while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

            double absAngleDiffDeg = Math.abs(Math.toDegrees(angleDiff));

            float turnOutput = 0;
            if (dist > turnLockThreshold && absAngleDiffDeg > 3.0) {
                turnOutput = (float) turnPid.compute(0, -Math.toDegrees(angleDiff), deltaTime);
            }

            float baseSpeed = (float) forwardPid.compute(0, dist, deltaTime);

            // 【策略调整 1】：放松限速。
            // 只有离得非常近（< 80）才限速，且限速值提高到 22，保证电机不卡死
            if (dist < 80.0) {
                baseSpeed = Math.min(baseSpeed, 22.0f);
            }

            // 【策略调整 2】：动力保底（消除卡顿的关键）
            // 只要距离没到终点，且车头方向大致对准（cos > 0.5），强行保证最小 baseSpeed 为 13
            // 这样小车会一直“滑”下去，而不是走走停停
            if (dist > errorTolerance && Math.cos(angleDiff) > 0.5) {
                baseSpeed = Math.max(baseSpeed, 13.0f);
            }

            baseSpeed *= Math.max(0, Math.cos(angleDiff));

            int targetLeft, targetRight;
            if (absAngleDiffDeg > 45 && dist > turnLockThreshold) {
                if (turnOutput > 0) { targetLeft = (int)turnOutput; targetRight = 0; }
                else { targetLeft = 0; targetRight = (int)-turnOutput; }
            } else {
                targetLeft = (int)(baseSpeed + turnOutput);
                targetRight = (int)(baseSpeed - turnOutput);
            }

            smoothedLeft += RAMP_FACTOR * (targetLeft - smoothedLeft);
            smoothedRight += RAMP_FACTOR * (targetRight - smoothedRight);

            try {
                // 【核心修改点】：仅在这里对最终发送给电机的左轮速度应用 0.75 补偿
                int finalLeft = Math.round(smoothedLeft * LEFT_WHEEL_COMPENSATION);
                int finalRight = Math.round(smoothedRight);

                sendMotorSpeed(clamp(finalLeft), clamp(finalRight));
            } catch (Exception e) { stopControl(); }

            controlHandler.postDelayed(this, controlInterval);
        }
    };

    private int clamp(int val) { return Math.max(-100, Math.min(100, val)); }

    private void sendMotorSpeed(int left, int right) throws Exception {
        if (bleManager == null || !bleManager.isConnected()) return;
        byte[] packet = new byte[6];
        packet[0] = HEADER;
        packet[1] = (byte)0x02; // POSITIONING_COMMAND
        packet[2] = (byte) left;
        packet[3] = (byte) right;
        packet[4] = (byte) ((packet[0] & 0xFF) ^ (packet[1] & 0xFF) ^ (packet[2] & 0xFF) ^ (packet[3] & 0xFF));
        packet[5] = (byte)0x55; // FRAME_END
        bleManager.sendData(packet);
    }
}