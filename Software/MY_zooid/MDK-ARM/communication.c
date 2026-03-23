/**
 ******************************************************************************
 * @file    communication.c
 * @brief   Bluetooth packet parsing and motor command execution
 ******************************************************************************
 */

#include "config.h"
#include "utilities.h"
#include "functions.h" 

/* ========================================================================== */
/* Motor Deadzone Configuration                                               */
/* Micro coreless motors have static friction. A minimum PWM duty cycle is    */
/* required to actually start spinning.                                       */
/* ========================================================================== */
#define MIN_PWM_DUTY 30 

/**
 * @brief  Applies a deadzone compensation to the raw motor speed.
 * This prevents the motor from stalling when the PID output is too low
 * to overcome the gear and surface static friction.
 * @param  speed: Raw target speed (-100 to 100)
 * @retval Compensated speed
 */
int8_t apply_deadzone(int8_t speed) {
    if (speed == 0) {
        return 0; // Absolute stop
    }
    
    if (speed > 0) {
        /* Boost forward speed if it's too low to spin the motor */
        if (speed < MIN_PWM_DUTY) return MIN_PWM_DUTY;
    } else {
        /* Boost backward speed if it's too low to spin the motor */
        if (speed > -MIN_PWM_DUTY) return -MIN_PWM_DUTY;
    }
    return speed;
}

/**
 * @brief  Parses the 6-byte UART packet and dispatches commands.
 * Packet format: [HEADER][CMD_TYPE][DATA1][DATA2][CHECKSUM][TAIL]
 * @param  packet: Pointer to the 6-byte packet buffer
 * @retval 0 on success, negative value on error
 */
int parse_packet(uint8_t *packet) {
    
    /* 1. Verify Checksum (XOR of bytes 0 to 3) */
    uint8_t calculated_checksum = packet[0] ^ packet[1] ^ packet[2] ^ packet[3];
    if (calculated_checksum != packet[4]) {
        return -3; // Checksum mismatch error
    }
    
    uint8_t cmd_type = packet[1];

    /* 2. Process Manual Control Command */
    if (cmd_type == CMD_MANUAL) { 
        
        /* Default manual cruising speed */
        int8_t manual_speed = 50; 

        switch (packet[2]) {
            case DIR_FORWARD:
                setMotor1(manual_speed); 
                setMotor2(manual_speed);
                break;
                
            case DIR_BACKWARD:
                setMotor1(-manual_speed); 
                setMotor2(-manual_speed);
                break;
                
            case DIR_LEFT: // Spin left in place (Differential drive)
                setMotor1(-manual_speed); 
                setMotor2(manual_speed);
                break;
                
            case DIR_RIGHT: // Spin right in place (Differential drive)
                setMotor1(manual_speed); 
                setMotor2(-manual_speed);
                break;
                
            case DIR_STOP:
                setMotor1(0); 
                setMotor2(0);
                break;
            
            default:
                return -1; // Invalid direction parameter
        }
    }
    /* 3. Process Automatic PID Control Command */
    else if (cmd_type == CMD_AUTO_PID) { 
        int8_t raw_m1 = (int8_t)packet[2];
        int8_t raw_m2 = (int8_t)packet[3];
        
        /* * CRITICAL RESTORATION: Apply deadzone compensation!
         * Without this, the PID output will smoothly decrease as the robot 
         * approaches the target, but the motor will stall prematurely due 
         * to physical static friction, making it look like it doesn't slow down.
         */
        int8_t final_m1 = apply_deadzone(raw_m1);
        int8_t final_m2 = apply_deadzone(raw_m2);
        
        setMotor1(final_m1);
        setMotor2(final_m2);
    }
    else {
        return -2; // Unknown command type
    }

    return 0; // Packet parsed and executed successfully
}