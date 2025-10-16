package com.example.sensor;

public class PIDController {
    // PID参数
    private float kp;
    private float ki;
    private float kd;

    // 输出限制
    private double outputMax;
    private double outputMin;

    // 积分限制
    private double integralMax;
    private double integralMin;

    // 内部状态变量
    private double integral;
    private double prevMeasure;  // 上一次测量值（用于微分计算）
    private boolean firstRun;

    // 构造函数
    public PIDController(float kp, float ki, float kd,
                         double outputMax, double outputMin,
                         double integralMax) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.outputMax = outputMax;
        this.outputMin = outputMin;
        this.integralMax = integralMax;
        this.integralMin = -integralMax;

        reset();
    }

    // 计算PID输出（目标值为0，直接传入当前误差）
    public double compute(double target, double currentError, long deltaTimeMs) {
        // 确保时间间隔有效
        if (deltaTimeMs <= 0) deltaTimeMs = 1;

        // 比例项：Kp × 当前误差
        double proportional = kp * currentError;

        // 积分项：Ki × 误差累积（带限幅防积分饱和）
        integral += ki * currentError * deltaTimeMs;
        if (integral > integralMax) {
            integral = integralMax;
        } else if (integral < integralMin) {
            integral = integralMin;
        }

        // 微分项：Kd × 误差变化率（基于测量值，避免微分冲击）
        double derivative = 0;
        if (!firstRun) {
            derivative = -kd * (currentError - prevMeasure) / deltaTimeMs;
        } else {
            firstRun = false;
        }

        // 总输出
        double output = proportional + integral + derivative;

        // 输出限幅
        if (output > outputMax) {
            output = outputMax;
        } else if (output < outputMin) {
            output = outputMin;
        }

        // 保存当前误差用于下次微分计算
        prevMeasure = currentError;

        return output;
    }

    // 重置PID状态
    public void reset() {
        integral = 0;
        prevMeasure = 0;
        firstRun = true;
    }

    // PID参数设置接口
    public void setKp(float kp) { this.kp = kp; }
    public void setKi(float ki) { this.ki = ki; }
    public void setKd(float kd) { this.kd = kd; }

    public float getKp() { return kp; }
    public float getKi() { return ki; }
    public float getKd() { return kd; }
}
