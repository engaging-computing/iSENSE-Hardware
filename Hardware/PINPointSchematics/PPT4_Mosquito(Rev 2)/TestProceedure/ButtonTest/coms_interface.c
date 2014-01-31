/**
 * @file coms_interface.c
 *
 * Interface functions for communicating over the USB.
 */

#include "coms_interface.h"
#include "usart_interface.h"
#include "data_interface.h"
#include "spi_master.h"
#include "timer_interface.h"
#include "rtc_interface.h"
#include <avr/pgmspace.h>
#include <avr/interrupt.h>

static char comsData[COMS_BUFFER_SIZE]; ///< USB communication buffer.
static char* resetConfirmation = COMS_RESET_REQ_PAYLOAD;
static int comsSize = 0; ///< Size of message in coms buffer.

/**
 * Interrupt to receive new data over the coms line.The data is
 * placed in the 'comsData' buffer and the count is incrmented. If
 * the buffer reaches an overflow condition then new characters
 * are placed at the end of the buffer.
 */
SIGNAL(USART0_RX_vect)
{
    if (comsSize < COMS_BUFFER_SIZE)
    {
        comsData[comsSize++] = UDR0;
    }
    else
    {
        comsData[COMS_BUFFER_SIZE - 1] = UDR0;
    }
}

/**
 * Initializes the usart responsible for serial communication.
 * Also turns on recieve interrupts over serial.
 */
void coms_Init(void)
{
    int i;

    usart_Init(SERIAL, COMS_BAUD);

    for (i = 0; i < COMS_BUFFER_SIZE; i++) comsData[i] = 0xA3;

    usart_Interrupt_RX(SERIAL, ENABLE);
}

/**
 * Polls the serial read buffer for new information and looks for new messages.
 *
 * @return Returns a comsMsg object containing any new message. If no message is
 * found then the returned object with have type 'NONE'
 */
ComsMsg coms_Poll(void)
{
    ComsMsg msg = {NONE, {'\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0'}};

    cli();

    if (comsSize >= COMS_BUFFER_SIZE)
    {
        //usart_Digits(SERIAL, comsSize, 10, SIGNED_ZEROS, 5);
        comsSize = 20;

        msg.type = OVERFLOW;
        coms_Clear(COMS_BUFFER_SIZE);
    }
    else
    {
        switch (comsData[0])
        {
            case 0xA3:
                if (comsSize == 0) msg.type = NONE;
                else
                {
                    msg.type = BAD;
                    msg.msg[0] = comsData[0];
                    coms_Clear(1);
                }
                break;
                
            case COMS_VERIFY_IN:
                msg.type = VERIFY;
                coms_Clear(COMS_VERIFY_SIZE);
                break;
                
            case COMS_READ_FLASH_PAGE_IN:
                if (comsSize >= COMS_READ_FLASH_PAGE_SIZE)
                {
                    msg.type = READ_FLASH;
                    msg.msg[0] = comsData[1];
                    msg.msg[1] = comsData[2];
                    msg.msg[2] = comsData[3];
                    msg.msg[3] = comsData[4];
                    msg.msg[4] = comsData[5];
                    msg.msg[5] = comsData[6];
                    coms_Clear(COMS_READ_FLASH_PAGE_SIZE);
                }
                break;
                
            case COMS_WRITE_TIME_IN:
                if (comsSize >= COMS_WRITE_TIME_SIZE)
                {
                    msg.type = WRITE_TIME;
                    msg.msg[0] = comsData[1];
                    msg.msg[1] = comsData[2];
                    msg.msg[2] = comsData[3];
                    msg.msg[3] = comsData[4];
                    msg.msg[4] = comsData[5];
                    msg.msg[5] = comsData[6];
                    msg.msg[6] = comsData[7];
                    coms_Clear(COMS_WRITE_TIME_SIZE);
                }
                break;
                
            case COMS_READ_CONFIG_IN:
                if (comsSize >= COMS_READ_CONFIG_SIZE)
                {
                    msg.type = READ_CONFIG;
                    msg.msg[0] = comsData[1];
                    msg.msg[1] = comsData[2];
                    coms_Clear(COMS_READ_CONFIG_SIZE);
                }
                break;
                
            case COMS_WRITE_CONFIG_IN:
                if (comsSize >= COMS_WRITE_CONFIG_SIZE)
                {
                    msg.type = WRITE_CONFIG;
                    msg.msg[0] = comsData[1];
                    msg.msg[1] = comsData[2];
                    msg.msg[2] = comsData[3];
                    coms_Clear(COMS_WRITE_CONFIG_SIZE);
                }
                break;
                
            case COMS_LIVE_DATA_IN:
                msg.type = LIVE_DATA;
                coms_Clear(COMS_LIVE_DATA_SIZE);
                break;
                
            case COMS_HEADER_REQ_IN:
                msg.type = HEADER_REQ;
                coms_Clear(COMS_HEADER_REQ_SIZE);
                break;
                
            case COMS_RESET_REQ_IN:
                if (comsSize >= COMS_RESET_REQ_SIZE)
                {
                    msg.type = RESET_REQ;
                    msg.msg[0] = comsData[1];
                    msg.msg[1] = comsData[2];
                    msg.msg[2] = comsData[3];
                    msg.msg[3] = comsData[4];
                    msg.msg[4] = comsData[5];
                    msg.msg[5] = comsData[6];
                    msg.msg[6] = comsData[7];
                    coms_Clear(COMS_RESET_REQ_SIZE);
                }
                break;
                
            default:
                msg.type = BAD;
                msg.msg[0] = comsData[0];
                coms_Clear(1);
                break;
        }
    }

    sei();

    return msg;
}

/**
 * Clear the first 'num' bytes of the communications buffer.
 * Other information in the buffer is shifted into the front,
 * cleared cells are nulled and placed in the back.
 *
 * @param num The number of bytes to clear.
 */
void coms_Clear(int num)
{
    int i, j;
    for (i = num, j = 0; i < COMS_BUFFER_SIZE; i++, j++)
    {
        comsData[j] = comsData[i];
    }

    for (; j < COMS_BUFFER_SIZE; j++) comsData[j] = 0xA3;
    comsSize -= num;
}

/**
 * Polls the serial communications (via coms_Poll) and takes
 * the apropriate action in response.
 *
 * @param runData The runData state of the program. This is
 * required so the record and livedata flags can be switched.
 */
void coms_Handle(RunData *runData)
{
    ComsMsg msg = coms_Poll();

    switch (msg.type)
    {
        case BAD:
        case NONE:
            //Ignore bad coms
            break;
            
        case OVERFLOW:
            //ignore - stub for debug
            break;

        case VERIFY:
            runData->record = false;
            runData->liveData = false;
            usart_Write(SERIAL, COMS_VERIFY_OUT);
            usart_Write(SERIAL, COMS_VERIFY_MAJOR_VERSION);
            usart_Write(SERIAL, COMS_VERIFY_MINOR_VERSION);
            break;

        case READ_FLASH:
            ;
            uint32_t addr = ((uint32_t)msg.msg[0] << 16) + ((uint32_t)msg.msg[1] << 8) + msg.msg[2];
            uint32_t size = ((uint32_t)msg.msg[3] << 16) + ((uint32_t)msg.msg[4] << 8) + msg.msg[5];

            runData->record = false;
            runData->liveData = false;
            sst_Read_To_Coms((char*)&addr, size);

            break;

        case WRITE_TIME:
            ;
            Time t;

            runData->record = false;
            runData->liveData = false;

            t.seconds = msg.msg[0];
            t.minutes = msg.msg[1];
            t.hours   = msg.msg[2];
            t.dow     = msg.msg[3];
            t.date    = msg.msg[4];
            t.month   = msg.msg[5];
            t.year    = msg.msg[6];

            rtc_Set_Time(&t);

            usart_Write(SERIAL, COMS_WRITE_TIME_OUT);
            break;

        case READ_CONFIG:
            runData->record = false;
            runData->liveData = false;
            usart_Write(SERIAL, data_Read_EEPROM((msg.msg[0] << 8) + msg.msg[1]));
            break;

        case WRITE_CONFIG:
            runData->record = false;
            runData->liveData = false;
            data_Write_EEPROM((msg.msg[0] << 8) + msg.msg[1], msg.msg[2]);
            usart_Write(SERIAL, COMS_WRITE_CONFIG_OUT);
            break;

        case LIVE_DATA:
            runData->record = false;
            runData->liveData = true;
            break;

        case HEADER_REQ:
            ;
            uint32_t header = data_Cur_Addr();

            usart_Write(SERIAL, ((char*)(&header))[2]);
            usart_Write(SERIAL, ((char*)(&header))[1]);
            usart_Write(SERIAL, ((char*)(&header))[0]);
            usart_Write(SERIAL, sizeof(DataPoint));
            break;
        case RESET_REQ:
            ;
            int i;
            bool valid = true;
            
            for (i = 0; i < COMS_RESET_REQ_SIZE; i++)
            {
                if (msg.msg[i] != resetConfirmation[i])
                {
                    valid = false;
                }
            }
            
            if (valid)
            {
                resetDevice();
            }
            
            break;
    }
}

