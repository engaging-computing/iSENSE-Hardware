/**
 * @file gsm_modem_interface.h
 *
 * Interface functions for the GSM cell modem.
 */

#ifndef _GSM_MODEM_INTERFACE_H_
#define _GSM_MODEM_INTERFACE_H_

#include "globals.h"
#include "sensor_interface.h"

#define MODEM_DATA_GLOBAL_RATE_H 0x20 ///< Upper byte of the default global recording rate.
#define MODEM_DATA_GLOBAL_RATE_L 0xE8 ///< Lower byte of the default global recording rate.

#define MODEM_CALL_NUMBER "4155992671" // Twilio phone number
//#define MODEM_CALL_NUMBER "9784733712" // Chris Granz' phone number
#define MODEM_TWILIO_PIN "2113-1493"

#define MODEM_PORT_1    C    ///< Port for control lines.
#define MODEM_DTR0_PIN  6    ///<
#define MODEM_DSR0_PIN  7    ///<

#define MODEM_PORT_2    A    ///< ON "key" and power supply enable pin port.
#define MODEM_ONKEY_PIN 2    ///< On "key" pin.
#define MODEM_PWRSUP_PIN 1    ///< modem power supply pin.

#define MODEM_BAUD_115200  4 ///< Baud rate setting for 115200:  (FOSC/(16L * baud)) - 1
#define MODEM_BUFFER_SIZE 20 ///< Size of communications buffer.

extern void gsm_modem_Init(void);
extern void gsm_modem_On(void);
extern void gsm_modem_Off(void);
extern void gsm_modem_Comms_Setup(void);
extern void gsm_modem_Comms_Send_Msg(char *msg);
extern int gsm_modem_Comms_Available(void);
extern int gsm_modem_Comms_ReadBuffer(int amount, char* buffer);
extern void gsm_modem_Comms_Clear(void);
extern void gsm_modem_Comms_Transmit_Data(DataPoint* datap);
extern void gsm_modem_Convert_To_Hex(char in, char* out);

#endif // _GSM_MODEM_INTERFACE_H_

