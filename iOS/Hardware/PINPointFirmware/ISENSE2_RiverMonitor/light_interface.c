/**
 * @file light_interface.c
 *
 * Interface functions for the onboard light sensor.
 */

#include "light_interface.h"
#include "usart_interface.h"
#include "timer_interface.h"

/**
 * Sends initialization values to the light sensor.
 */
void light_Init(void)
{
    char out[2] = {LIGHT_ICTRL_ADDR, LIGHT_ICTRL_VAL};
    twi_Write(LIGHT_ADDR, out, 2);

    timer_Wait_MS(100);

    out[0] = LIGHT_CTRL_ADDR;
    out[1] = LIGHT_CTRL_VAL;
    twi_Write(LIGHT_ADDR, out, 2);

    twi_Write(LIGHT_ADDR, out, 1);
    twi_Read(LIGHT_ADDR, out + 1, 1);
}

/**
 * Reads the current light value from the light sensor.
 *
 * @param exp Address to store the light exponent.
 * @param man Address to store the light mantissa.
 */
void light_Read(uint8_t* exp, uint8_t* man)
{
    //NOTE: This device does not seem to follow all of the I2C protocol.
    //      Specifically, multiple byte reads do NOT increment address.
    //      Worked around below with two one-byte reads.

    char out[1] = {LIGHT_VAL_1_ADDR};
    char in[2];

    twi_Write(LIGHT_ADDR, out, 1);
    twi_Read(LIGHT_ADDR, in, 2);

    *exp =  (0xF0 & in[0]) >> 4;
    *man = ((0x0F & in[0]) << 4) + (0x0F & in[1]);
}
