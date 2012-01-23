/**
 * @file gsm_modem_interface.h
 *
 * Interface functions for the GSM cell modem.
 */

#ifndef _GSM_MODEM_INTERFACE_H_
#define _GSM_MODEM_INTERFACE_H_

#include "globals.h"

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

#endif // _GSM_MODEM_INTERFACE_H_

