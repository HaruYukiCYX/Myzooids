/**
 ******************************************************************************
 * @file    main.c
 * @brief   Main program body - Bare-Metal UART RX & Robust Packet Processing
 ******************************************************************************
 */

#include "config.h"
#include "utilities.h"
#include "functions.h"

/* External variables --------------------------------------------------------*/
extern UART_HandleTypeDef huart1;

/* ======================= Ring Buffer ======================= */
static RingBuffer uartRxBuffer;

/* ======================= Packet Parser ===================== */
static uint8_t packetBuffer[6];
static uint8_t packetIndex = 0;

/* ======================= Function Prototypes ================ */
static void RingBuffer_Init(RingBuffer *rb);
static void RingBuffer_Put(RingBuffer *rb, uint8_t data);
static int  RingBuffer_Get(RingBuffer *rb, uint8_t *data);
static void process_uart_stream(void);

/* ============================================================ */
/* Bare-Metal UART RX Callback (Called from stm32f0xx_it.c)     */
/* ============================================================ */
void BareMetal_UART_RX_Callback(uint8_t byte)
{
    /* Keep it extremely short: just push to the ring buffer */
    RingBuffer_Put(&uartRxBuffer, byte);
}

/* ============================================================ */
/* Main Program                                                 */
/* ============================================================ */
int main(void)
{
    /* Disable CRS interrupt if not used */
    NVIC_DisableIRQ(RCC_CRS_IRQn);
    
    /* Initialize hardware, motors, LEDs, timers, UART, etc. */
    initRobot();
    
    /* Initialize ring buffer */
    RingBuffer_Init(&uartRxBuffer);

    /* * CRITICAL CHANGE: Completely abandon HAL_UART_Receive_IT()!
     * Directly manipulate the registers to enable the UART RX Not Empty (RXNE) 
     * interrupt. This bypasses the fragile HAL state machine locks.
     */
    __HAL_UART_ENABLE_IT(&huart1, UART_IT_RXNE);

    /* Main loop */
    while (1)
    {
        /* Extract and parse packets from the ring buffer */
        process_uart_stream();

        /* Heartbeat LED: Non-blocking check to ensure main loop is alive */
        static uint32_t lastTick = 0;
        if (HAL_GetTick() - lastTick > 500)
        {
            toggleGreenLed();
            lastTick = HAL_GetTick();
        }
    }
}

/* ============================================================ */
/* Ring Buffer Implementation                                   */
/* ============================================================ */

static void RingBuffer_Init(RingBuffer *rb)
{
    rb->head = 0;
    rb->tail = 0;
}

static void RingBuffer_Put(RingBuffer *rb, uint8_t data)
{
    uint16_t next = (rb->head + 1) & (UART_RX_BUFFER_SIZE - 1);

    /* Drop data if buffer is full to prevent unread data corruption */
    if (next != rb->tail)
    {
        rb->buffer[rb->head] = data;
        rb->head = next;
    }
}

static int RingBuffer_Get(RingBuffer *rb, uint8_t *data)
{
    if (rb->head == rb->tail)
    {
        return 0; // buffer empty
    }

    *data = rb->buffer[rb->tail];
    rb->tail = (rb->tail + 1) & (UART_RX_BUFFER_SIZE - 1);
    return 1;
}

/* ============================================================ */
/* UART Stream Packet Parser (With Self-Healing Capability)     */
/* ============================================================ */

static void process_uart_stream(void)
{
    uint8_t byte;

    /* Process all available bytes in the ring buffer */
    while (RingBuffer_Get(&uartRxBuffer, &byte))
    {
        if (packetIndex == 0)
        {
            /* State 1: Waiting for Header */
            if (byte == PROTOCOL_HEADER)
            {
                packetBuffer[0] = byte;
                packetIndex = 1;
            }
        }
        else if (packetIndex < 5)
        {
            /* State 2: Receiving Payload */
            packetBuffer[packetIndex++] = byte;
        }
        else if (packetIndex == 5)
        {
            /* State 3: Receiving Tail and Validating */
            packetBuffer[5] = byte;
            packetIndex = 0; // Reset state machine for the next packet

            if (packetBuffer[5] == PROTOCOL_TAIL)
            {
                /* Structurally complete, send to logic parser */
                int result = parse_packet(packetBuffer);

//                if (result == 0) {
//                    toggleBlueLed();   // Command executed successfully
//                } else {
//                    toggleRedLed();    // Checksum or logic parsing error
//                }
            }
            else
            {
                /* Tail mismatch: Packet corrupted or bytes shifted due to noise */
                toggleRedLed();

                /* * CRITICAL SELF-HEALING MECHANISM
                 * If the mismatched tail happens to be the header (0xAA),
                 * we likely lost a byte earlier. Treat this byte as the start 
                 * of a brand new packet to resync immediately!
                 */
                if (byte == PROTOCOL_HEADER)
                {
                    packetBuffer[0] = byte;
                    packetIndex = 1;
                }
            }
        }
    }
}