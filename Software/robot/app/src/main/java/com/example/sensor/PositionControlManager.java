package com.example.sensor;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;

public class PositionControlManager {
    private static final String TAG = "PositionControlManager";

    // ==========================================
    // 1. 接口定义
    // ==========================================
    public interface ControlModeListener {
        MainActivity3.ControlMode getCurrentMode();
    }

    // ==========================================
    // 2. 速度与控制参数
    // ==========================================
    // 适当提高上限，配合下位机的死区补偿，确保能动
    private static final int MAX_MOTOR_SPEED = 60;

    // 控制频率 (ms)
    private int controlInterval = 50;

    // 距离容忍度 (像素)，小于此距离视为到达
    private double errorTolerance = 10.0;

    // 兼容性变量 (MainActivity3 需要)
    private int deadZone = 8;
    private double pixelToCmRatio = 0.05; // 默认值

    // ==========================================
    // 3. 组件引用
    // ==========================================
    private BleManager bleManager;
    private ControlModeListener modeListener;
    private Handler controlHandler = new Handler();

    // ==========================================
    // 4. PID 控制器
    // ==========================================
    private PIDController turnPid;    // 控制转向 (对应角度差)
    private PIDController forwardPid; // 控制前进 (对应距离)

    // ==========================================
    // 5. 状态变量
    // ==========================================
    private double targetX = 0;
    private double targetY = 0;
    private double currentX = 0;
    private double currentY = 0;
    private double currentAngleRad = 0; // 机器人当前朝向 (弧度)

    private boolean isControlling = false;

    // ==========================================
    // 6. 蓝牙协议常量
    // ==========================================
    private static final byte HEADER = (byte) 0xAA;
    private static final byte POSITIONING_COMMAND = (byte) 0x02;
    private static final byte FRAME_END = (byte) 0x55;

    public PositionControlManager(BleManager bleManager, ControlModeListener listener) {
        this.bleManager = bleManager;
        this.modeListener = listener;

        // 初始化 PID 参数 (Kp, Ki, Kd, MaxOut, MinOut, MaxInt)
        // 转向 PID: 响应要快
        turnPid = new PIDController(0.8f, 0.0f, 0.2f, 40, -40, 0);

        // 前进 PID: 接近目标时减速
        forwardPid = new PIDController(0.6f, 0.01f, 0.1f, MAX_MOTOR_SPEED, -MAX_MOTOR_SPEED, 20);
    }

    // ==========================================
    // 7. 兼容性 Getter/Setter (修复报错的关键)
    // ==========================================

    public void setControlInterval(int interval) {
        this.controlInterval = interval;
    }

    public void setErrorTolerance(double tolerance) {
        this.errorTolerance = tolerance;
    }

    public void setDeadZone(int deadZone) {
        this.deadZone = deadZone;
        // 注：新PID算法主要依赖 errorTolerance 判断停止，
        // deadZone 变量保留以兼容代码，也可用于过滤极小的噪声，但目前PID自带抗噪。
    }

    public double getPixelToCmRatio() {
        return pixelToCmRatio;
    }

    // 如果你需要设置比例，可以保留这个方法，虽然 MainActivity3 没报错调用它
    public void setPixelToCmRatio(double ratio) {
        this.pixelToCmRatio = ratio;
    }

    // ==========================================
    // 8. 核心逻辑方法
    // ==========================================

    /**
     * 更新当前机器人状态
     *
     * @param x        图像坐标X
     * @param y        图像坐标Y
     * @param angleRad 机器人朝向 (弧度)
     */
    public void updateCurrentPosition(double x, double y, double angleRad) {
        this.currentX = x;
        this.currentY = y;
        this.currentAngleRad = angleRad;
    }

    // 兼容旧接口 (MainActivity可能只传了x,y，此时默认角度不变)
    public void updateCurrentPosition(double x, double y) {
        updateCurrentPosition(x, y, this.currentAngleRad);
    }

    public void setTargetPosition(double x, double y) {
        this.targetX = x;
        this.targetY = y;
        // 每次设置新目标时，重置PID积分项
        turnPid.reset();
        forwardPid.reset();
    }

    public void startControl() {
        // 安全检查
        if (isControlling || bleManager == null || !bleManager.isConnected()) {
            return;
        }
        if (modeListener != null && modeListener.getCurrentMode() != MainActivity3.ControlMode.AUTO) {
            Log.w(TAG, "非自动模式，拒绝启动PID");
            return;
        }

        isControlling = true;
        controlHandler.post(controlRunnable);
    }

    public void stopControl() {
        isControlling = false;
        controlHandler.removeCallbacks(controlRunnable);
        try {
            sendMotorSpeed(0, 0);
        } catch (IOException e) {
        }
    }

    private Runnable controlRunnable = new Runnable() {
        private long lastTime = System.currentTimeMillis();

        @Override
        public void run() {
            // 安全检查
            if (!isControlling || (modeListener != null && modeListener.getCurrentMode() != MainActivity3.ControlMode.AUTO)) {
                stopControl();
                return;
            }

            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastTime;
            if (deltaTime <= 0) deltaTime = 1;
            lastTime = currentTime;

            // 计算全局误差向量
            double globalDx = targetX - currentX;
            double globalDy = targetY - currentY;

            // 距离检测
            double distance = Math.sqrt(globalDx * globalDx + globalDy * globalDy);

            // 使用 errorTolerance 判断是否到达
            if (distance < errorTolerance) {
                Log.i(TAG, "Target Reached. Distance: " + distance);
                stopControl();
                return;
            }

            // 计算角度逻辑
            // 目标角度：从机器人指向目标的向量角度 (0 ~ 2PI 或 -PI ~ PI)
            double targetAngle = Math.atan2(globalDy, globalDx);

            // 角度误差
            double angleDiff = targetAngle - currentAngleRad;

            // 角度归一化 (-PI 到 PI)
            while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
            while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

            // PID 计算

            // A. 转向输出 (输入是角度差，单位：度)
            // angleDiff > 0 表示目标在顺时针方向
            float turnOutput = (float) turnPid.compute(0, -Math.toDegrees(angleDiff), deltaTime);

            // B. 前进输出 (输入是距离)
            float baseSpeed = (float) forwardPid.compute(0, distance, deltaTime);

            // 智能混合策略
            double absAngleDiffDeg = Math.abs(Math.toDegrees(angleDiff));

            if (absAngleDiffDeg > 45) {
                baseSpeed = 0; // 角度大时，原地旋转
            } else {
                // 角度小时，边走边转，速度随角度增加而衰减
                baseSpeed = baseSpeed * (float) Math.cos(angleDiff);
            }

            // 计算左右轮速度 (差速模型)
            int leftSpeed = (int) (baseSpeed + turnOutput);
            int rightSpeed = (int) (baseSpeed - turnOutput);

            // 最终限幅并发送
            leftSpeed = clamp(leftSpeed);
            rightSpeed = clamp(rightSpeed);

            try {
                sendMotorSpeed(leftSpeed, rightSpeed);
            } catch (IOException e) {
                Log.e(TAG, "Send Failed", e);
                stopControl();
            }

            controlHandler.postDelayed(this, controlInterval);
        }
    };

    private int clamp(int val) {
        if (val > 100) return 100;
        if (val < -100) return -100;
        return val;
    }

    private void sendMotorSpeed(int left, int right) throws IOException {
        if (bleManager == null || !bleManager.isConnected()) return;

        byte[] packet = new byte[6];
        // 1. 包头
        packet[0] = HEADER;             // 0xAA
        packet[1] = POSITIONING_COMMAND;// 0x02
        packet[2] = (byte) left;        // 速度值可能为负
        packet[3] = (byte) right;       // 速度值可能为负

        // 2. 关键修复：使用 & 0xFF 强制转为无符号整数进行异或计算
        int checkSum = (packet[0] & 0xFF) ^
                (packet[1] & 0xFF) ^
                (packet[2] & 0xFF) ^
                (packet[3] & 0xFF);

        packet[4] = (byte) checkSum;    // 校验位
        packet[5] = FRAME_END;          // 0x55

        // 3. 调试日志：打印发出的实际数据 (方便你对比下位机接收到的)
        // 格式如: AA 02 1E E2 4A 55
        Log.d(TAG, String.format("TX: %02X %02X %02X %02X %02X %02X",
                packet[0], packet[1], packet[2], packet[3], packet[4], packet[5]));

        bleManager.sendData(packet);
    }
}