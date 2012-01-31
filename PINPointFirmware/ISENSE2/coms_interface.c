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
#include "data_interface.h"
#include <avr/pgmspace.h>
#include <avr/interrupt.h>
#include <avr/wdt.h>

static char comsData[COMS_BUFFER_SIZE]; ///< USB communication buffer.
static char comsBauds[COMS_NUM_BAUD] = {COMS_BAUD_115200, COMS_BAUD_57600, COMS_BAUD_38400, COMS_BAUD_19200, COMS_BAUD_9600};
static char* resetConfirmation = COMS_CONTROL_REQ_PAYLOAD;
static int comsSize = 0; ///< Size of message in coms buffer.

/**
 * Interrupt to receive new data over the coms line.The data is
 * placed in the 'comsData' buffer and the count is incrmented. If
 * the buffer reaches an overflow condition then new characters
 * are placed at the end of the buffer.
 */
/*
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
    
    timer_Wake();
}
*/

/**
 * Initializes the usart responsible for serial communication.
 * Also turns on recieve interrupts over serial.
 */
void coms_Init(void)
{
    int i;

    if (data_Read_EEPROM(DATA_BT_FLAG_ADDR) == DATA_FLAG_ON)
    {
        data_Write_EEPROM(DATA_BT_FLAG_ADDR, DATA_FLAG_OFF);
        
        coms_Bluetooth_Init();
    }
    
    if (!(coms_Bluetooth_Detect()))
    {
        usart_Init(SERIAL, COMS_BAUD_115200);
    }

    for (i = 0; i < COMS_BUFFER_SIZE; i++) comsData[i] = 0xA3;

    usart_Interrupt_RX(SERIAL, ENABLE);
}

/**
 * Initializes the bluetooth settings. Configures it 
 * as a serial port. Also names the device PINPoint4+HEX.SN.
 * 
 * NOTE: Do not use after interrupts have been enabled.
 */
void coms_Bluetooth_Init(void)
{
    char rate[2];
    
    switch (data_Read_EEPROM(DATA_BT_BAUD_ADDR))
    {
        case COMS_BAUD_115200:
            rate[0] = '1';
            rate[1] = '1';
            break;
        case COMS_BAUD_57600:
            rate[0] = '5';
            rate[1] = '7';
            break;
        case COMS_BAUD_38400:
            rate[0] = '3';
            rate[1] = '8';
            break;
        case COMS_BAUD_19200:
            rate[0] = '1';
            rate[1] = '9';
            break;
        case COMS_BAUD_9600:
            rate[0] = '9';
            rate[1] = '6';
            break;
        default:
            rate[0] = '1';
            rate[1] = '1';
            
            data_Write_EEPROM(DATA_BT_BAUD_ADDR, DATA_DEFAULT_BT_BAUD);
            
            break;
    }
    
    uint32_t sn = 0;
    sn += (uint32_t)data_Read_EEPROM(DATA_SN_24_ADDR) << 24;
    sn += (uint32_t)data_Read_EEPROM(DATA_SN_16_ADDR) << 16;
    sn += (uint32_t)data_Read_EEPROM(DATA_SN_8_ADDR) << 8;
    sn += (uint32_t)data_Read_EEPROM(DATA_SN_0_ADDR);
    
    timer_Wait_MS(1000);
    
    int i;
    for (i = 0; i < COMS_NUM_BAUD; i++)
    {
        usart_Init(SERIAL, comsBauds[i]);
    
        timer_Wait_MS(1000);
        usart_Text(SERIAL, PSTR("$$$"));
        timer_Wait_MS(100);
        
        usart_Text(SERIAL, PSTR("SU,"));
        usart_Write(SERIAL, rate[0]);
        usart_Write(SERIAL, rate[1]);
        usart_Write(SERIAL, '\r');
        timer_Wait_MS(100);
        
        usart_Text(SERIAL, PSTR("SC,1101\r"));
        timer_Wait_MS(100);
        
        usart_Text(SERIAL, PSTR("SS,SerialPort\r"));
        timer_Wait_MS(100);
        
        usart_Text(SERIAL, PSTR("SI,0030\r"));
        timer_Wait_MS(100);
        
        usart_Text(SERIAL, PSTR("SJ,0030\r"));
        timer_Wait_MS(100);
        
        usart_Text(SERIAL, PSTR("SN,PINPoint4"));
        usart_Print_Num(SERIAL, sn);
        usart_Write(SERIAL, '\r');
        timer_Wait_MS(100);
        
        usart_Text(SERIAL, PSTR("---\r"));
        timer_Wait_MS(100);
    }
}

/**
 * Determines if the device is using bluetooth by
 * attempting to activate the bluetooth configuration
 * mode. Will fail if the baudrate stored in EEPROM
 * is incorrect, or if the device has been powered
 * on for longer than ~1 minute. All failures should
 * be false negatives.
 * 
 * NOTE: Do not use after interrupts have been enabled.
 * 
 * @return True if connected to bluetooth.
 */
bool coms_Bluetooth_Detect(void)
{
    sei();
    bool ret = false;
    usart_Init(SERIAL, data_Read_EEPROM(DATA_BT_BAUD_ADDR));
    usart_Interrupt_RX(SERIAL, ENABLE);
    
    timer_Wait_MS(500);
    usart_Text(SERIAL, PSTR("$$$"));
    timer_Wait_MS(100);
    
    if ((comsData[0] == 'C') && 
        (comsData[1] == 'M') &&
        (comsData[2] == 'D'))
    {
        usart_Text(SERIAL, PSTR("---\r"));
        
        ret = true;
    }
    
    cli();
    usart_Interrupt_RX(SERIAL, DISABLE);
    
    comsSize = 0;
    
    return ret;
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

            case COMS_ERASE_REQ_IN:
                if (comsSize >= COMS_ERASE_REQ_SIZE)
                {
                    msg.type = ERASE_REQ;
                    msg.msg[0] = comsData[1];
                    msg.msg[1] = comsData[2];
                    msg.msg[2] = comsData[3];
                    msg.msg[3] = comsData[4];
                    msg.msg[4] = comsData[5];
                    msg.msg[5] = comsData[6];
                    msg.msg[6] = comsData[7];
                    coms_Clear(COMS_ERASE_REQ_SIZE);
                }
                break;

            case COMS_START_REQ_IN:
                if (comsSize >= COMS_START_REQ_SIZE)
                {
                    msg.type = START_REQ;
                    msg.msg[0] = comsData[1];
                    msg.msg[1] = comsData[2];
                    msg.msg[2] = comsData[3];
                    msg.msg[3] = comsData[4];
                    msg.msg[4] = comsData[5];
                    msg.msg[5] = comsData[6];
                    msg.msg[6] = comsData[7];
                    coms_Clear(COMS_START_REQ_SIZE);
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
    bool valid = true;
    int i;

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

            usart_Write(SERIAL, COMS_LIVE_DATA_OUT);

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
            for (i = 0; i < COMS_RESET_REQ_SIZE; i++)
            {
                if (msg.msg[i] != resetConfirmation[i])
                {
                    valid = false;
                }
            }

            if (valid)
            {
                wdt_enable(WDTO_120MS);
            }

            break;

        case ERASE_REQ:
            for (i = 0; i < COMS_RESET_REQ_SIZE; i++)
            {
                if (msg.msg[i] != resetConfirmation[i])
                {
                    valid = false;
                }
            }

            if (valid)
            {
                data_Clear();
            }

            usart_Write(SERIAL, COMS_ERASE_REQ_OUT);

            break;

        case START_REQ:
            for (i = 0; i < COMS_RESET_REQ_SIZE; i++)
            {
                if (msg.msg[i] != resetConfirmation[i])
                {
                    valid = false;
                }
            }

            if (valid)
            {
                runData->record = true;
                runData->liveData = false;
            }

            usart_Write(SERIAL, COMS_START_REQ_OUT);

            break;
    }
}

