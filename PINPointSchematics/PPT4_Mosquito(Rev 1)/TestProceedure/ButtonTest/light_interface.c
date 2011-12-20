/**
 * @file light_interface.c
 *
 * Interface functions for the onboard light sensor.
 */

#include "light_interface.h"
#include "usart_interface.h"

/**
 * Sends initialization values to the light sensor.
 */
void light_Init(void)
{
    char out[2] = {LIGHT_ICTRL_ADDR, LIGHT_ICTRL_VAL};
    twi_Write(LIGHT_ADDR, out, 2);

    out[0] = LIGHT_CTRL_ADDR;
    out[1] = LIGHT_CTRL_VAL;
    twi_Write(LIGHT_ADDR, out, 2);
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
    twi_Read(LIGHT_ADDR, in, 1);

    out[0] = LIGHT_VAL_2_ADDR;

    twi_Write(LIGHT_ADDR, out, 1);
    twi_Read(LIGHT_ADDR, in + 1, 1);

    *exp =  (0xF0 & in[0]) >> 4;
    *man = ((0x0F & in[0]) << 4) + (0x0F & in[1]);
}

/**
 * Adds two light values togther.
 *
 * @param oldExp Address of one value's exponent, and
 * where to store the new exponent.
 * @param oldMan Address of one value's mantissa and
 * where to store the new mantissa.
 * @param exp Address of the other exponent.
 * @param man Address of the other mantissa.
 */
void light_Add(uint16_t* oldExp, uint32_t* oldMan, uint8_t* exp, uint8_t* man)
{
    uint16_t newExp, fixedExp, fixedMan;
    uint32_t newMan;

    fixedExp = *exp;
    fixedMan = (*man) << 8;

    if (*oldExp > fixedExp)
    {
        newExp = *oldExp;
        newMan = (*oldMan) + (fixedMan >> (*oldExp - fixedExp));
    }
    else if (fixedExp > *oldExp)
    {
        newExp = fixedExp;
        newMan = (fixedMan) + ((*oldMan) >> (fixedExp - *oldExp));
    }
    else
    {
        newExp = fixedExp;
        newMan = fixedMan + *oldMan;
    }

    while (newMan > 0xFFFF)
    {
        newExp++;
        newMan >>= 1;
    }

    *oldExp = newExp;
    *oldMan = newMan;
}

