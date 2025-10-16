
/* Define to prevent recursive inclusion -------------------------------------*/
#ifndef __CONFIG_H
#define __CONFIG_H

#ifdef __cplusplus
extern "C" {
#endif

/* Includes ------------------------------------------------------------------*/
#include "stm32f0xx.h"
#include "stm32f0xx_hal.h"
#include "stm32f0xx_it.h"
#include "string.h"
#include "math.h"
#include <__cross_studio_io.h>
#include "stdlib.h"

/* Exported macro ------------------------------------------------------------*/
#define BIT(x) (1 << (x))
#define COUNTOF(__BUFFER__) (sizeof(__BUFFER__) / sizeof(*(__BUFFER__)))
#define DEBUG 1
#define DEBUG_ENABLED() (DEBUG)

/*
  CONSTANTS DECLARATIONS
*/

//#define USE_EXTERNAL_PHOTODIODES
#define USE_FAST_MOTORS
//#define USE_SIMULATOR

///////////////////////////////////
//     OPERATING CONSTANTS       //
///////////////////////////////////
#define PI                                      3.14159265358979323846f
#define RAD_TO_DEG_CST                          (180.0f / PI)
#define DEG_TO_RAD_CST                          (PI / 180.0f)
  
#define FALSE                                   0
#define TRUE                                    1
#define LOW                                     GPIO_PIN_RESET
#define HIGH                                    GPIO_PIN_SET

#define ON                                      TRUE
#define OFF                                     FALSE
#define false                                   FALSE
#define true                                    TRUE


///////////////////////////////////
//    COMMUNICATION CONSTANTS    //
///////////////////////////////////
#define RADIO_CHANNEL                           22//22 //88 //channel at which you want to communicate through
#define PAYLOAD_MAX_SIZE                        32
#define DEFAULT_ADDRESS                         0xF0F0F0F0D000
#define DEFAULT_ID                              0xFF
#define COMMUNICATION_TIMEOUT                   4000 //in ms

#define NB_MAX_ROBOTS                           10
#define RECEIVER_ID                             250

///////////////////////////////////
//      MESSAGE TYPES            //
///////////////////////////////////
//Operating messages
#define TYPE_UPDATE                             0x03
#define TYPE_STATUS                             0x04
#define TYPE_MOTORS_VELOCITY                    0x05
#define TYPE_ROBOT_POSITION                     0x06

//Configuration Messages
#define TYPE_NEW_ROBOT_REQUEST                  0xF0
#define TYPE_NEW_ROBOT_INFORMATION              0xF1

#define TYPE_REBOOT_ROBOT 0xFF

///////////////////////////////////
//  LED DECLARATIONS             //
///////////////////////////////////
#define RGB_GPIO_PORT                           GPIOA
#define RGB_GPIO_AF                             GPIO_AF2_TIM1
#define RGB_GPIO_CLK_ENABLE()                   __HAL_RCC_GPIOA_CLK_ENABLE()
#define RED_PIN                                 GPIO_PIN_10
#define GREEN_PIN                               GPIO_PIN_11
#define BLUE_PIN                                GPIO_PIN_9

#define RGB_TIM                                 TIM1
#define RGB_FREQ                                200
#define RGB_CLK                                 2000000
#define RGB_PERIOD                              (uint32_t)(RGB_CLK / RGB_FREQ) /* Period Value  */
#define RED_CHANNEL                             TIM_CHANNEL_3
#define GREEN_CHANNEL                           TIM_CHANNEL_4
#define BLUE_CHANNEL                            TIM_CHANNEL_2
#define RGB_TIMER_CLK_ENABLE()                  __HAL_RCC_TIM1_CLK_ENABLE()

///////////////////////////////////
//  MOTORS DECLARATIONS          //
///////////////////////////////////
#ifdef USE_FAST_MOTORS
#define MAX_SPEED                              60
#else
#define MAX_SPEED                               100
#endif

#define MOTOR1_PIN                              GPIO_PIN_0
#define MOTOR1_GPIO_PORT                        GPIOA
#define MOTOR1_GPIO_CLK_ENABLE()                __HAL_RCC_GPIOA_CLK_ENABLE()

#define MOTOR2_PIN                              GPIO_PIN_1
#define MOTOR2_GPIO_PORT                        GPIOA
#define MOTOR2_GPIO_CLK_ENABLE()                __HAL_RCC_GPIOA_CLK_ENABLE()

#define MOTOR3_PIN                              GPIO_PIN_2
#define MOTOR3_GPIO_PORT                        GPIOA
#define MOTOR3_GPIO_CLK_ENABLE()                __HAL_RCC_GPIOA_CLK_ENABLE()

#define MOTOR4_PIN                              GPIO_PIN_3
#define MOTOR4_GPIO_PORT                        GPIOA
#define MOTOR4_GPIO_CLK_ENABLE()                __HAL_RCC_GPIOA_CLK_ENABLE()

#define MOTORS_TIM                              TIM2
#define MOTORS_GPIO_AF                          GPIO_AF2_TIM2
#define MOTORS_FREQ                             40000
#define MOTORS_CLK                              4000000
#define MOTORS_PERIOD                           (uint32_t)(MOTORS_CLK / MOTORS_FREQ) /* Period Value  */
#define MOTOR1_CHANNEL                          TIM_CHANNEL_1
#define MOTOR2_CHANNEL                          TIM_CHANNEL_2
#define MOTOR3_CHANNEL                          TIM_CHANNEL_3
#define MOTOR4_CHANNEL                          TIM_CHANNEL_4
#define MOTORS_CLK_ENABLE()                     __HAL_RCC_TIM2_CLK_ENABLE()

///////////////////////////////////
//  INPUT PINS DECLARATIONS      //
///////////////////////////////////

#define CHARGING_STATUS_PIN                     GPIO_PIN_13
#define CHARGING_STATUS_GPIO_PORT               GPIOB
#define CHARGING_STATUS_IRQn                    EXTI4_15_IRQn
#define CHARGING_STATUS_GPIO_CLK_ENABLE()       __HAL_RCC_GPIOB_CLK_ENABLE()
#define CHARGING_STATUS_IRQ_PRIORITY            3

/* Definition for IMU Int Pin */
#define IMU_INT_PIN                             GPIO_PIN_12
#define IMU_INT_GPIO_PORT                       GPIOB
#define IMU_INT_IRQn                            EXTI4_15_IRQn
#define IMU_INT_GPIO_CLK_ENABLE()               __HAL_RCC_GPIOB_CLK_ENABLE()
#define IMU_INT_IRQ_PRIORITY                    3

///////////////////////////////////
//  SENSORS DECLARATIONS         // 
///////////////////////////////////
/* Definition for I2C clock resources */
#define I2Cx                                    I2C2
#define I2Cx_CLK_ENABLE()                       __HAL_RCC_I2C2_CLK_ENABLE()
#define I2Cx_TIMING                             0x00901850 //0x0010020A
#define I2Cx_FORCE_RESET()                      __HAL_RCC_I2C2_FORCE_RESET()
#define I2Cx_RELEASE_RESET()                    __HAL_RCC_I2C2_RELEASE_RESET()

/* Definition for I2C Pins */
#define I2Cx_SCL_PIN                            GPIO_PIN_10
#define I2Cx_SCL_GPIO_PORT                      GPIOB
#define I2Cx_SCL_AF                             GPIO_AF1_I2C2
#define I2Cx_SCL_GPIO_CLK_ENABLE()              __HAL_RCC_GPIOB_CLK_ENABLE()

#define I2Cx_SDA_PIN                            GPIO_PIN_11
#define I2Cx_SDA_GPIO_PORT                      GPIOB
#define I2Cx_SDA_AF                             GPIO_AF1_I2C2
#define I2Cx_SDA_GPIO_CLK_ENABLE()              __HAL_RCC_GPIOB_CLK_ENABLE()

/* Definition for I2Cx's DMA */
#define I2Cx_DMA                                DMA1
#define I2Cx_TX_DMA_INSTANCE                    DMA1_Channel4
#define I2Cx_RX_DMA_INSTANCE                    DMA1_Channel5
#define I2Cx_DMA_CLK_ENABLE()                   __HAL_RCC_DMA1_CLK_ENABLE()

/* Definition for I2Cx's DMA NVIC */
#define I2Cx_DMA_TX_IRQn                        DMA1_Channel4_5_IRQn
#define I2Cx_DMA_RX_IRQn                        DMA1_Channel4_5_IRQn
#define I2Cx_DMA_TX_IRQHandler                  DMA1_Channel4_5_IRQHandler
#define I2Cx_DMA_RX_IRQHandler                  DMA1_Channel4_5_IRQHandler

///////////////////////////////////
// MICROSECONDS DELAY TIMER      //
///////////////////////////////////
#define MICRO_TIM                               TIM3
#define MICRO_FREQ                              100
#define MICRO_CLK                               1000000
#define MICRO_PERIOD                            (uint32_t)(MICRO_CLK / MICRO_FREQ) /* Period Value  */
#define MICRO_TIM_CLK_ENABLE()                  __HAL_RCC_TIM3_CLK_ENABLE();
#define MICRO_IRQn                              TIM3_IRQn
#define MICRO_IRQHandler                        TIM3_IRQHandler
#define MICRO_IRQ_PRIORITY                      0


#define POWER_MGMT_CLK_ENABLE()                 __HAL_RCC_PWR_CLK_ENABLE()

///////////////////////////////////
// Exported types                //
///////////////////////////////////
typedef uint8_t MotorDirection;
typedef uint8_t bool;

typedef struct {
	uint16_t positionX;
	uint16_t positionY;
	uint8_t colorRed;
	uint8_t colorGreen;
	uint8_t colorBlue;
	uint8_t preferredSpeed;
	int16_t orientation;
	bool isFinalGoal;
	uint8_t empty;
}PositionControlMessage;

typedef struct {
	uint16_t positionX;
	uint16_t positionY;
	int16_t orientation;
	uint8_t touch;
	uint8_t batteryLevel;
}StatusMessage;

typedef struct
{
    int8_t motor1;
    int8_t motor2;
    float motorGain;
    int8_t minVelocity;
    int8_t preferredVelocity;
    int8_t maxVelocity;
} Motor;

typedef struct
{
  uint16_t x;
  uint16_t y;
  float angle;
  bool finalGoal;
} Target;

typedef struct
{
    uint16_t x;
    uint16_t y;
} Position;

typedef struct
{
    uint8_t type;
    uint8_t id;
} Header;

typedef struct
{
    Header header;
    uint8_t payload[PAYLOAD_MAX_SIZE - sizeof(Header)];
} Message;

typedef struct
{
    int16_t ax;
    int16_t ay;
    int16_t az;
    int16_t gx;
    int16_t gy;
    int16_t gz;
    int16_t T;
} ImuRawData;

typedef struct
{
    float ax;
    float ay;
    float az;
    float gx;
    float gy;
    float gz;
    float T;
} MotionData;

typedef struct
{
    int16_t ax;
    int16_t ay;
    int16_t az;
} AccelRawData;

typedef struct
{
    int16_t gx;
    int16_t gy;
    int16_t gz;
} GyroRawData;

typedef struct
{
    float w;
    float x;
    float y;
    float z;
} Quaternion;


typedef struct
{
  int8_t x;
  int8_t y;
  int8_t z;
} OrientationVec;

typedef struct
{
    uint8_t id;
    uint64_t pipeAddress;
} Robot;

#ifdef __cplusplus
}
#endif

#endif