/**
 * @file gsm_modem_interface.h
 *
 * Interface functions for the GSM cell modem.
 */

#include "globals.h"

#define MODEM_PORT_1    C    ///< Port for control lines.
#define MODEM_DTR0_PIN  6    ///<
#define MODEM_DSR0_PIN  7    ///<

#define MODEM_PORT_2    A    ///< ON "key" port.
#define MODEM_ONKEY_PIN 2    ///< On "key" pin.

extern void gsm_modem_Init(void);

