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
 * @file twi_interface.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for TWI communication.
 */

#include "twi_interface.h"

/**
 * Initializes the TWI bus.
 *
 * @return Returns OK.
 */
status twi_Init(void)
{
    TWSR = TW_PRESCALE;

    TWBR = TW_BITRATE;

    clear_bit(DDRC, 1);
    clear_bit(PORTC, 1);
    clear_bit(DDRC, 0);
    clear_bit(PORTC, 0);

    //DDRC &= ~((1 << 0) | (1 << 1));
    //PORTC &= ~((1 << 0) | (1 << 1));

    return OK;
}

/**
 * Writes 'size' bytes from 'data' to 'slave' over TWI.
 *
 * @param slave TWI address of slave device.
 * @param data Address of data to write.
 * @param size Number of bytes to write.
 */
void twi_Write(char slave, char* data, int size)
{
    twi_Start();
    twi_Wait();

    twi_WriteByte(slave | TW_WRITE);
    twi_Nack();
    twi_Wait();

    int i = 0;

    for (i = 0; i < size; i++)
    {
        twi_WriteByte(data[i]);
        twi_Nack();
        twi_Wait();
    }

    twi_Stop();
}

/**
 * Reads 'size' bytes into 'data' from 'slave' over TWI.
 *
 * @param slave TWI address of slave device.
 * @param data The address to write data to.
 * @param size Number of bytes to read.
 */
void twi_Read(char slave, char* data, int size)
{
    twi_Start();
    twi_Wait();

    twi_WriteByte(slave | TW_READ);
    twi_Nack();
    twi_Wait();

    int i = 0;

    for (i = 0; i < size - 1; i++)
    {
        twi_Ack();
        twi_Wait();
        twi_ReadByte(data[i]);
    }

    twi_Nack();
    twi_Wait();
    twi_ReadByte(data[i]);

    twi_Stop();
}
