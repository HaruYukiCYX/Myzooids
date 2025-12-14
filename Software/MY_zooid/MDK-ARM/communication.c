#include "config.h"
#include "utilities.h"
#include "functions.h" 

int parse_packet(uint8_t *packet) {
    
    uint8_t calculated_checksum = packet[0] ^ packet[1] ^ packet[2] ^ packet[3];
    
    if (calculated_checksum != packet[4]) {
        return -3; 
    }
	
    uint8_t cmd_type = packet[1];

    if (cmd_type == CMD_MANUAL) { 
        switch (packet[2]) {
            case DIR_FORWARD:
                setMotor1(40); 
                setMotor2(40);
                break;
                
            case DIR_BACKWARD:
                setMotor1(-40); 
                setMotor2(-40);
                break;
                
            case DIR_LEFT:
                setMotor1(-30); 
                setMotor2(30);
                break;
                
            case DIR_RIGHT:
                setMotor1(30); 
                setMotor2(-30);
                break;
                
            case DIR_STOP:
                setMotor1(0); 
                setMotor2(0);
                break;
            
            default:
                break;
        }
    }
    else if (cmd_type == CMD_AUTO_PID) { 
        int8_t motor1_speed = (int8_t)packet[2];
        int8_t motor2_speed = (int8_t)packet[3];
        
        setMotor1(motor1_speed);
        setMotor2(motor2_speed);
    }
    else {
        return -2; 
    }

    return 0; 
}