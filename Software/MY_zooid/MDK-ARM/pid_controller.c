#include "pid_controller.h"
#include <math.h>

/**
 * @brief Initialize PID controller
 */
void PID_Init(PID_Controller *pid, float Kp, float Ki, float Kd,
              int32_t output_max, int32_t output_min, float integral_max) {
    if (pid == NULL) return;
    
    // Initialize PID parameters
    pid->Kp = Kp;
    pid->Ki = Ki;
    pid->Kd = Kd;
    
    // Set output limits
    pid->output_max = output_max;
    pid->output_min = output_min;
    
    // Set integral limits (symmetric)
    pid->integral_max = integral_max;
    pid->integral_min = -integral_max;
    
    // Initialize PID variables
    pid->setpoint = 0.0f;
    pid->integral = 0.0f;
    pid->prev_error = 0.0f;
    pid->prev_measure = 0.0f;
    pid->first_run = true;
}

/**
 * @brief Set new setpoint for PID controller
 */
void PID_SetSetpoint(PID_Controller *pid, float setpoint) {
    if (pid == NULL) return;
    pid->setpoint = setpoint;
}

/**
 * @brief Calculate PID output using position form algorithm
 */
int32_t PID_Compute(PID_Controller *pid, float measure) {
    if (pid == NULL) return 0;
    
    float error;
    float proportional;
    float derivative;
    float output;
    
    // Calculate current error
    error = pid->setpoint - measure;
    
    // Proportional term
    proportional = pid->Kp * error;
    
    // Integral term with anti-windup
    pid->integral += pid->Ki * error;
    
    // Apply integral limits
    if (pid->integral > pid->integral_max) {
        pid->integral = pid->integral_max;
    } else if (pid->integral < pid->integral_min) {
        pid->integral = pid->integral_min;
    }
    
    // Derivative term (using derivative on measurement to avoid derivative kick)
    if (pid->first_run) {
        // First run, no previous measurement
        derivative = 0.0f;
        pid->first_run = false;
    } else {
        derivative = -pid->Kd * (measure - pid->prev_measure);
    }
    
    // Calculate total output
    output = proportional + pid->integral + derivative;
    
    // Apply output limits
    if (output > pid->output_max) {
        output = pid->output_max;
    } else if (output < pid->output_min) {
        output = pid->output_min;
    }
    
    // Save values for next iteration
    pid->prev_error = error;
    pid->prev_measure = measure;
    
    // Return output as integer
    return (int32_t)round(output);
}

/**
 * @brief Reset PID controller variables
 */
void PID_Reset(PID_Controller *pid) {
    if (pid == NULL) return;
    
    pid->integral = 0.0f;
    pid->prev_error = 0.0f;
    pid->prev_measure = 0.0f;
    pid->first_run = true;
}
    