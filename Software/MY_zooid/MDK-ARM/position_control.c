#include "pid_controller.h"
PID_Controller error_pid;

// Motor control parameters (adjust according to actual hardware)
#define MAX_SPEED  60
#define MIN_SPEED -60
#define INTEGRAL_LIMIT 200.0f 
#define CONTROL_PERIOD 0.05f  // 50ms control loop (matches upper computer update rate)



/**
 * @brief Initialize PID controller for distance error control
 * @note Kp: Responsiveness to error, Ki: Eliminates steady-state error, Kd: Reduces overshoot
 */
void Error_PID_Init() {
    PID_Init(
        &error_pid,
        1.2f,   // Kp: Proportional gain
        0.15f,  // Ki: Integral gain
        0.25f,  // Kd: Derivative gain
        MAX_SPEED,
        MIN_SPEED,
        INTEGRAL_LIMIT
    );
    
    // Target error is 0 (reach target position)
    PID_SetSetpoint(&error_pid, 0.0f);
}



/**
 * @brief Main control loop executing PID calculation and motor control
 */
void Position_Control_Loop(float current_error) {
    
    // 2. Dead zone handling: Stop when error is small enough (<5cm)
    if (fabsf(current_error) < 0.05f) {
        setMotor1(0);
		  	setMotor2(0);
        PID_Reset(&error_pid);  // Reset to prevent integral accumulation
        return;
    }
    
    // 3. PID calculation using the received error
    int32_t speed = PID_Compute(&error_pid, current_error);
    
    // 4. Send control signal to motor
    setMotor1(speed);
		setMotor2(speed);
    
}