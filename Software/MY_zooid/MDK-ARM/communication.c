#include "config.h"
#include "utilities.h"
#include "functions.h"

// Packet header
#define HEADER 0xAA
// Movement command type
#define MOVE_COMMAND 0x01

// Packet header
#define HEADER 0xAA
// Movement command type
#define MOVE_COMMAND 0x01
#define POSITIONING_COMMAND 0x02

float current_error = 0.0f;  // Stores latest distance error

// Movement directions and stop
typedef enum {
    DIRECTION_FORWARD = 0x01,
    DIRECTION_BACKWARD = 0x02,
    DIRECTION_LEFT = 0x03,
    DIRECTION_RIGHT = 0x04,
    DIRECTION_STOP = 0x05
} Direction;

// Create a packet
void create_packet(uint8_t *packet, Direction direction) {
    packet[0] = HEADER;
    packet[1] = MOVE_COMMAND;
    packet[2] = (uint8_t)direction;
    // Simple XOR checksum
    packet[3] = packet[0] ^ packet[1] ^ packet[2];
}

// Parse a packet
int parse_packet(uint8_t *packet) {
    if (packet[0] != HEADER) return -1; // Header error
	  uint8_t calculated_checksum = packet[0] ^ packet[1] ^ packet[2];
	  if (calculated_checksum != packet[3]) return -3; // Checksum error
	
    if (packet[1] = MOVE_COMMAND){
    switch (packet[2]) {
        case 0x01:
            HAL_UART_Transmit(&huart1, (uint8_t *)"The robot moves forward.\r\n", 25, HAL_MAX_DELAY);
            setMotor1(30);
			     	setMotor2(30);
            break;
        case 0x02:
            HAL_UART_Transmit(&huart1, (uint8_t *)"The robot moves backward.\r\n", 25, HAL_MAX_DELAY);
            setMotor1(-30);
			     	setMotor2(-30);
            break;
        case 0x03:
            HAL_UART_Transmit(&huart1, (uint8_t *)"The robot moves left.\r\n", 21, HAL_MAX_DELAY);
            setMotor1(30);
			     	setMotor2(20);
            break;
        case 0x04:
            HAL_UART_Transmit(&huart1, (uint8_t *)"The robot moves right.\r\n", 22, HAL_MAX_DELAY);
            setMotor1(20);
			     	setMotor2(30);
            break;
        case 0x05:
            HAL_UART_Transmit(&huart1, (uint8_t *)"The robot stops.\r\n", 18, HAL_MAX_DELAY);
            setMotor1(0);
			     	setMotor2(0);
            break;
        default:
            break;
    }
	}
		if (packet[1] = POSITIONING_COMMAND){
			current_error = packet[2];
			Position_Control_Loop(current_error);
		
		
		
		}
    return 0; // Success
}    