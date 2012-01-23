/**
 * @file gsm_modem_interface.c
 *
 * Interface functions for communicating with a GSM cell modem and text messages.
 */

#include "gsm_modem_interface.h"
#include "usart_interface.h"
#include "data_interface.h"
#include "spi_master.h"
#include "timer_interface.h"
#include "rtc_interface.h"
#include "data_interface.h"
#include <avr/pgmspace.h>
#include <avr/interrupt.h>
#include <avr/wdt.h>

static char gsmModemData[MODEM_BUFFER_SIZE]; ///< GSM modem communication buffer.
static int gsmModemHead;
static int gsmModemTail;

/**
 * Interrupt to receive new data over the serial line. The data is
 * placed in the 'gsmModemData' buffer and the head is incremented.
 * When the buffer is full we just throw data away.
 */
SIGNAL(USART0_RX_vect)
{
	int space = (gsmModemHead - gsmModemTail);

	if (space < 0) // head pointer wrapped
		space = -space;*

    if ((space - 1) > 0) // we keep one byte to distinguish full from empty
	{
        gsmModemData[gsmModemHead] = UDR0;
		gsmModemHead = (gsmModemHead + 1) % MODEM_BUFFER_SIZE;
	}		
    
    timer_Wake();
}

/**
 * Initializes the GSM cell modem IO pins.
 */
void gsm_modem_Init(void)
{
	// ring buffer initialization
	gsmModemHead = 0;
	gsmModemTail = 0;

	usart_Init(SERIAL, MODEM_BAUD_115200);
    usart_Interrupt_RX(SERIAL, ENABLE);

    setPinMode(MODE_HIGH, MODEM_PORT_1, MODEM_DTR0_PIN);
    setPinMode(MODE_HIGH, MODEM_PORT_1, MODEM_DSR0_PIN);

    setPinMode(MODE_HIGH, MODEM_PORT_2, MODEM_ONKEY_PIN);
	setPinMode(MODE_LOW, MODEM_PORT_2, MODEM_PWRSUP_PIN);	
}

/**
 * Turns on the GSM cell modem, power supply first, then actual modem.
 * NOTE: The call to this function will block for ~4 seconds.
 */
void gsm_modem_On(void)
{
	// switch on the power supply and wait for it to stabilize
    setPinMode(MODE_HIGH, MODEM_PORT_2, MODEM_PWRSUP_PIN);	
	timer_Wait_MS(1000);

    setPinMode(MODE_LOW, MODEM_PORT_2, MODEM_ONKEY_PIN);
	timer_Wait_MS(2500); // must wait at least 2 seconds to turn on the modem
    setPinMode(MODE_HIGH, MODEM_PORT_2, MODEM_ONKEY_PIN);
	timer_Wait_MS(200);

	setPinMode(MODE_LOW, MODEM_PORT_1, MODEM_DTR0_PIN);
    setPinMode(MODE_LOW, MODEM_PORT_1, MODEM_DSR0_PIN);
	timer_Wait_MS(100);
	
	// the modem should now be ready to communicate with via the UART
}

/**
 * Turns off the GSM cell modem, actual modem first, then power supply.
 */
void gsm_modem_Off(void)
{
	setPinMode(MODE_LOW, MODEM_PORT_2, MODEM_ONKEY_PIN);
	timer_Wait_MS(2500); // must wait at least 2 seconds to turn on the modem
    setPinMode(MODE_HIGH, MODEM_PORT_2, MODEM_ONKEY_PIN);
	timer_Wait_MS(200);

    setPinMode(MODE_LOW, MODEM_PORT_2, MODEM_PWRSUP_PIN);	
}


void gsm_modem_Comms_Setup(void)
{
	timer_Wait_MS(2500);
	usart_Text(SERIAL, PSTR("AT\r\n"));
	timer_Wait_MS(14000); // must wait after power on for device to acquire a signal...
	usart_Text(SERIAL, PSTR("AT+CSQ\r\n"));
	usart_Text(SERIAL, PSTR("AT+CMGF=1\r\n"));
	timer_Wait_MS(400);
	usart_Text(SERIAL, PSTR("AT+CMGS=\"9788700293\"\r\n"));
	timer_Wait_MS(400);
	usart_Text(SERIAL, PSTR("Hello World, from the PINPoint!"));
	usart_Write(SERIAL, 0x1A); // send control-z
	timer_Wait_MS(400);
}

int gsm_modem_Comms_Available(void)
{
	int amount = (gsmModemTail - gsmModemHead);

	if (amount < 0)
		return -amount;

	return amount;
}

int gsm_modem_Comms_ReadBuffer(int amount, char* buffer)
{
	int i;

	for (i = 0; i < amount; i++)
	{
		// check for empty ring buffer
		if (gsmModemTail == gsmModemHead)
			break;
			
		buffer[i] = gsmModemData[gsmModemTail];
		gsmModemTail = (gsmModemTail + 1) % MODEM_BUFFER_SIZE;
	}

	return i;
}

void gsm_modem_Poll(void)
{
	/*
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
	*/
}

void gsm_modem_Comms_Clear(void)
{
	// ring buffer initialization
	gsmModemHead = 0;
	gsmModemTail = 0;
}

/**
 */
void gsm_modem_Comms_Handle(RunData *runData)
{
	
	
    //ComsMsg msg = gsm_modem_Poll();
    //bool valid = true;
    //int i;
/*
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
	
	*/
}
