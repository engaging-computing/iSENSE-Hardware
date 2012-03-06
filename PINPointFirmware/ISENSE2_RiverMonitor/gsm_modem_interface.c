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
		space = -space;

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
//    usart_Interrupt_RX(SERIAL, ENABLE);

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


/**
 * Sets up the modem for text message transmission.
 */
void gsm_modem_Comms_Setup(void)
{
	timer_Wait_MS(2500);
	usart_Text(SERIAL, PSTR("AT\r\n"));
	timer_Wait_MS(14000); // must wait after power on for device to acquire a signal...
	usart_Text(SERIAL, PSTR("AT+CSQ\r\n"));
	usart_Text(SERIAL, PSTR("AT+CMGF=1\r\n"));
	timer_Wait_MS(400);
//	usart_Text(SERIAL, PSTR("AT+CMGS=\"9784733712\"\r\n"));
//	timer_Wait_MS(400);
//	usart_Text(SERIAL, PSTR(MODEM_TWILIO_PIN " Test"));
//	usart_Write(SERIAL, 0x1A); // send control-z
//	timer_Wait_MS(400);
}

/**
 * Sends a text message to Twilio service.
 */
void gsm_modem_Comms_Send_Msg(char *msg)
{
	usart_Text(SERIAL, PSTR("AT+CMGS=\"" MODEM_CALL_NUMBER "\"\r\n"));
	timer_Wait_MS(400);
	usart_Text(SERIAL, PSTR(MODEM_TWILIO_PIN " "));
	usart_String(SERIAL, msg);
	usart_Write(SERIAL, 0x1A); // send control-z
//	timer_Wait_MS(6000);
}

/**
 * Returns the number of bytes in the ring-buffer.
 */
int gsm_modem_Comms_Available(void)
{
	int amount = (gsmModemTail - gsmModemHead);

	if (amount < 0)
		return -amount;

	return amount;
}

/**
 * Read 'amount' bytes from the input ring buffer into 'buffer'.
 */
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

/*
 * Clear the read ring-buffer.
 */
void gsm_modem_Comms_Clear(void)
{
	// ring buffer initialization
	gsmModemHead = 0;
	gsmModemTail = 0;
}

/*
 * Not currently used.
 */
void gsm_modem_Poll(void)
{

}

/*
 * Not currently used.
 */
void gsm_modem_Comms_Handle(RunData *runData)
{
	
}

/**
 * Transmit the data we have collected and clear the EEPROM.
 */
void gsm_modem_Comms_Transmit_Data(DataPoint* datap)
{
	char msg[64+1];
	DataPoint data;
	int i;
	uint16_t d;

	gsm_modem_Init();
	gsm_modem_On();
	gsm_modem_Comms_Setup();

	// read data stored in EEPROM from the last 24 hours
//	sst_Read((char*)0, (char*)&data, sizeof(DataPoint));
	
	i = 0;

	gsm_modem_Convert_To_Hex((datap->latHigh >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(datap->latHigh & 0x00FF, &msg[i]);
	i+=2;

    msg[i++] = ' ';	
	gsm_modem_Convert_To_Hex((datap->latLow >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(datap->latLow & 0x00FF, &msg[i]);
	i+=2;
	
    msg[i++] = ' ';
	gsm_modem_Convert_To_Hex((datap->lonHigh >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(datap->lonHigh & 0x00FF, &msg[i]);
	i+=2;
	
	msg[i++] = ' ';
	gsm_modem_Convert_To_Hex((datap->lonLow >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(datap->lonLow & 0x00FF, &msg[i]);
	i+=2;

    msg[i++] = ' ';
	gsm_modem_Convert_To_Hex((datap->altitude >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(datap->altitude & 0x00FF, &msg[i]);
	i+=2;

//	gsm_modem_Convert_To_Hex(datap->pressure, &msg[i]);
//	i+=2;

    msg[i++] = ' ';
	gsm_modem_Convert_To_Hex((datap->temperature >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(datap->temperature & 0x00FF, &msg[i]);
	i+=2;
	
    msg[i++] = ' ';
	d = datap->bitpack.data.lightExp;
	gsm_modem_Convert_To_Hex((d >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(d & 0x00FF, &msg[i]);
	i+=2;

    msg[i++] = ' ';
	d = datap->bitpack.data.lightMan;
	gsm_modem_Convert_To_Hex((d >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(d & 0x00FF, &msg[i]);
	i+=2;

    msg[i++] = ' ';
	d = datap->bitpack.data.mini1;
	gsm_modem_Convert_To_Hex((d >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(d & 0x00FF, &msg[i]);
	i+=2;

    msg[i++] = ' ';
	d = datap->bitpack.data.mini2;
	gsm_modem_Convert_To_Hex((d >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(d & 0x00FF, &msg[i]);
	i+=2;
	
    msg[i++] = ' ';
	d = datap->bitpack.data.bta1;
	gsm_modem_Convert_To_Hex((d >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(d & 0x00FF, &msg[i]);
	i+=2;

    msg[i++] = ' ';
	d = datap->bitpack.data.bta2;
	gsm_modem_Convert_To_Hex((d >> 8) & 0x00FF, &msg[i]);
	i+=2;
	gsm_modem_Convert_To_Hex(d & 0x00FF, &msg[i]);
	i+=2;

	msg[i] = '\0';

	gsm_modem_Comms_Send_Msg(msg);

	data_Clear(); // clear EEPROM after sending

	timer_Wait_MS(6000); // wait for transmission to finish
	gsm_modem_Off();
}
	
/**
 * Convert a given byte to ascii hex format.
 */
void gsm_modem_Convert_To_Hex(char in, char* out)
{
    out[1] = (in & 0x0F);

	if (out[1] < 10)
		out[1] += '0';
	else
		out[1] = out[1] - 10 + 'A';
	
    out[0] = (in & 0xF0) >> 4;
	
	if (out[0] < 10)
		out[0] += '0';
	else
		out[0] = out[0] - 10 + 'A';
}

