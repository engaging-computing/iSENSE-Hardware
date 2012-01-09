/**
 * @file gsm_modem_interface.c
 *
 * Interface functions for communicating with a GSM cell modem and text messages.
 */

#include "gsm_modem_interface.h"
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

/**
 * Initializes the GSM cell modem IO pins.
 */
void gsm_modem_Init(void)
{
    setPinMode(MODE_LOW, MODEM_PORT_1, MODEM_DTR0_PIN);
    setPinMode(MODE_LOW, MODEM_PORT_1, MODEM_DSR0_PIN);

    setPinMode(MODE_LOW, MODEM_PORT_2, MODEM_ONKEY_PIN);
}

/**
 * Turns on the GSM cell modem, power supply first, then actual modem.
 */
void gsm_modem_On(void)
{
    setPinMode(MODE_LOW, MODEM_PORT_1, MODEM_DTR0_PIN);
    setPinMode(MODE_LOW, MODEM_PORT_1, MODEM_DSR0_PIN);

    setPinMode(MODE_LOW, MODEM_PORT_2, MODEM_ONKEY_PIN);
}

/**
 * Turns off the GSM cell modem, actual modem first, then power supply.
 */
void gsm_modem_Off(void)
{
    setPinMode(MODE_LOW, MODEM_PORT_1, MODEM_DTR0_PIN);
    setPinMode(MODE_LOW, MODEM_PORT_1, MODEM_DSR0_PIN);

    setPinMode(MODE_LOW, MODEM_PORT_2, MODEM_ONKEY_PIN);
}

