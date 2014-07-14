/**
 * @Copyright (c) 2008, iSENSE Project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution. Neither the name of the University of
 * Massachusetts Lowell nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

/**
 * @file light_interface.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
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
