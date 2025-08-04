/**
  ******************************************************************************
  * @file    Templates/Src/main.c 
  * @author  MCD Application Team
  * @version V1.3.0
  * @date    26-June-2015
  * @brief   Main program body
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; COPYRIGHT(c) 2015 STMicroelectronics</center></h2>
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *   1. Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *   2. Redistributions in binary form must reproduce the above copyright notice,
  *      this list of conditions and the following disclaimer in the documentation
  *      and/or other materials provided with the distribution.
  *   3. Neither the name of STMicroelectronics nor the names of its contributors
  *      may be used to endorse or promote products derived from this software
  *      without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  ******************************************************************************
  */

/* Includes ------------------------------------------------------------------*/
#include "config.h"
#include "utilities.h"
#include "functions.h"

/** @addtogroup STM32F0xx_HAL_Examples
  * @{
  */

/** @addtogroup Templates
  * @{
  */
/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
uint8_t testData1[5]; 
uint8_t packet[4];

/* Private function prototypes -----------------------------------------------*/

/* Private functions ---------------------------------------------------------*/

/**
  * @brief  Main program
  * @param  None
  * @retval None
  */
int main(void)
{
		
		initRobot();
//	  HAL_UART_Transmit(&huart1, (uint8_t *)"Send 'green' to veri.fy the RGB light and serial communication.\r\n", 65, HAL_MAX_DELAY);
//		HAL_UART_Transmit(&huart1, (uint8_t *)"Waiting for data...\r\n", 21, HAL_MAX_DELAY);
//    if (HAL_UART_Receive(&huart1, testData1, sizeof(testData1), 30000) == HAL_OK)
//			{
//				HAL_UART_Transmit(&huart1, (uint8_t *)"Data Received\r\n", 15, HAL_MAX_DELAY);
//        testRGB(testData1);
//      }
//    else
//      {
//        HAL_UART_Transmit(&huart1, (uint8_t *)"Receive Timeout\r\n", 17, HAL_MAX_DELAY);
//      }
		HAL_UART_Transmit(&huart1, (uint8_t *)"Robot starts, waiting for commands.\r\n", 36, HAL_MAX_DELAY);
		
		
				
		
		while(1)
		{
			
			if (HAL_UART_Receive(&huart1, packet, 4, 3000) == HAL_OK)
			{
				HAL_UART_Transmit(&huart1, (uint8_t *)"Data Received\r\n", 15, HAL_MAX_DELAY);
        parse_packet(packet);
      }
    else
      {
        HAL_UART_Transmit(&huart1, (uint8_t *)"Receive Timeout\r\n", 17, HAL_MAX_DELAY);
      }
		}
}