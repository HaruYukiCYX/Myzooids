/**
  ******************************************************************************
  * @file    stm32f0xx_it.c 
  * @author  MCD Application Team
  * @version V1.3.0
  * @date    26-June-2015
  * @brief   Main Interrupt Service Routines.
  ******************************************************************************
  */

/* Includes ------------------------------------------------------------------*/
#include "stm32f0xx_it.h"
#include "functions.h"

/* Declare the bare-metal callback located in main.c */
extern void BareMetal_UART_RX_Callback(uint8_t byte);

/* Private variables ---------------------------------------------------------*/
extern TIM_HandleTypeDef microTimHandle;
extern ADC_HandleTypeDef AdcHandle;

volatile uint64_t microTick = 0;
extern volatile bool batteryLevelReady;

/* Private function prototypes -----------------------------------------------*/
/* Private functions ---------------------------------------------------------*/

/******************************************************************************/
/* Cortex-M0 Processor Exceptions Handlers                         */
/******************************************************************************/

void NMI_Handler(void)
{
}

void HardFault_Handler(void)
{
  while (1)
  {
      HAL_GPIO_WritePin(GPIOA, GREEN_PIN, GPIO_PIN_RESET);
      HAL_GPIO_TogglePin(GPIOA, RED_PIN);
      for(volatile int i=0; i<500000; i++);
  }
}

void SVC_Handler(void)
{
}

void PendSV_Handler(void)
{
}

void SysTick_Handler(void)
{
    HAL_IncTick();

    if (HAL_GetTick() % 1000 == 0)
    {
//        startReadBatteryLevel();
    }
}

/******************************************************************************/
/* STM32F0xx Peripherals Interrupt Handlers                   */
/******************************************************************************/

void EXTI0_1_IRQHandler(uint16_t GPIO_Pin)
{
}

void EXTI2_3_IRQHandler(uint16_t GPIO_Pin)
{
}

void EXTI4_15_IRQHandler(uint16_t GPIO_Pin)
{
}

void MICRO_IRQHandler(void)
{
    HAL_TIM_IRQHandler(&microTimHandle);
    microTick += MICRO_PERIOD;
}

/**
  * @brief This function handles USART1 global interrupt.
  * It completely bypasses HAL library to prevent lockups.
  */
void USART1_IRQHandler(void)
{
    /* 1. Check if the interrupt is caused by Receive Data Register Not Empty (RXNE) */
    if ((USART1->ISR & USART_ISR_RXNE) != 0) 
    {
        /* Directly read from hardware RDR register. 
         * Note: Reading the RDR register automatically clears the RXNE flag! 
         */
        uint8_t data = (uint8_t)(USART1->RDR & 0xFF);
        
        /* Pass the data to the ring buffer in main.c */
        BareMetal_UART_RX_Callback(data);
    }

    /* 2. Check if an Overrun Error (ORE) occurred due to high-speed burst data */
    if ((USART1->ISR & USART_ISR_ORE) != 0) 
    {
        /* Forcefully clear the Overrun flag by writing 1 to the ICR register.
         * This instantly breaks any potential Interrupt Storms!
         */
        USART1->ICR = USART_ICR_ORECF;
    }
}