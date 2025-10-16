#ifndef PID_CONTROLLER_H
#define PID_CONTROLLER_H

#include "config.h"
/**
 * @brief PID Controller structure
 */
typedef struct {
    // PID parameters
    float Kp;              // Proportional gain
    float Ki;              // Integral gain
    float Kd;              // Derivative gain
    
    // Output limits
    int32_t output_max;    // Maximum output value
    int32_t output_min;    // Minimum output value
    
    // Integral limits
    float integral_max;    // Maximum integral value
    float integral_min;    // Minimum integral value
    
    // PID variables
    float setpoint;        // Target value
    float integral;        // Integral sum
    float prev_error;      // Previous error value
    float prev_measure;    // Previous measured value
    bool  first_run;       // Flag for first run
} PID_Controller;

/**
 * @brief Initialize PID controller
 * @param pid: Pointer to PID_Controller structure
 * @param Kp: Proportional gain
 * @param Ki: Integral gain
 * @param Kd: Derivative gain
 * @param output_max: Maximum output value
 * @param output_min: Minimum output value
 * @param integral_max: Maximum integral value
 */
void PID_Init(PID_Controller *pid, float Kp, float Ki, float Kd,
              int32_t output_max, int32_t output_min, float integral_max);

/**
 * @brief Set new setpoint for PID controller
 * @param pid: Pointer to PID_Controller structure
 * @param setpoint: New target value
 */
void PID_SetSetpoint(PID_Controller *pid, float setpoint);

/**
 * @brief Calculate PID output
 * @param pid: Pointer to PID_Controller structure
 * @param measure: Current measured value
 * @return Calculated PID output
 */
int32_t PID_Compute(PID_Controller *pid, float measure);

/**
 * @brief Reset PID controller
 * @param pid: Pointer to PID_Controller structure
 */
void PID_Reset(PID_Controller *pid);

#endif /* PID_CONTROLLER_H */
    