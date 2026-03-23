#include "functions.h"
#include "colors.h"

/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/

/* Private variables ---------------------------------------------------------*/

/* Timer Output Compare Configuration Structure declaration */

/////////////////////////////////////////////////////////////
//
//   ! ! ! 
//
// DON'T FORGET TO CHECK THE PHOTODIODE CONFIGURATION PINS //
// #define USE_EXTERNAL_PHOTODIODES in config.h            //
/////////////////////////////////////////////////////////////

uint8_t robotId = 9;
/* Peripherals initialized states
/* Interrupt flags */
volatile bool touchEvent = false;
volatile bool radioEvent = false;
volatile bool imuEvent = false;
bool isAddressValid = true;
volatile bool charging = false;
Target currentGoal = {500, 500, 0, 1};
Motor motorValues = {0, 0, 1.0f, 22, MAX_SPEED, 60};
bool isPositionControl = true;


/* Private function prototypes -----------------------------------------------*/
/* Private functions ---------------------------------------------------------*/
/*============================================================================
Name    :   upateRobot
------------------------------------------------------------------------------
Purpose :   updates the position and orientation of the robot and prepare the
            message to be sent
Input   :
Output  :
Return	:
Notes   :
============================================================================*/
void initRobot()
{
    HAL_Init();
    SystemClock_Config();
    initRGBLed();
    initMotors();

    for(int i=0;i<3000;i+=500)
    {
        HAL_Delay(250);
        setColor(yellow);
        HAL_Delay(250);
        setColor(black);
    }
		initUART();
		HAL_UART_Transmit(&huart1, (uint8_t *)"Bluetooth Initialization Successful.\r\n",38, HAL_MAX_DELAY);
}


void sleep()
{
    SleepMode_Measure();
}

