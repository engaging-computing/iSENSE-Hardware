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
 * @file twi_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for TWI communication.
 */

#include "globals.h"
#include "usart_interface.h"
#include <util/twi.h>

#define twi_Start()   (TWCR = (1 << TWINT) | (1 << TWSTA) | (1 << TWEN)) ///< Macro for starting a TWI command.
#define twi_Stop()    (TWCR = (1 << TWINT) | (1 << TWSTO) | (1 << TWEN)) ///< Macro for stopping a TWI command.
#define twi_Ack()     (TWCR = (1 << TWINT) | (1 << TWEA)  | (1 << TWEN)) ///< Macro for ack'ing over TWI.
#define twi_Nack()    (TWCR = (1 << TWINT) |                (1 << TWEN)) ///< Macro for nack'ing over TWI.

#define twi_Wait()     while(!(TWCR & (1 << TWINT))) ///< Macro for waiting during a TWI command.
#define twi_Check(x)  ((TWSR & 0xF8) == x)           ///< Macro for checking the TWI status register.
#define twi_WriteByte(x)  (TWDR = x)                 ///< Macro for writing to TWI.
#define twi_ReadByte(x)   (x = TWDR)                 ///< Macro for reading from TWI.

#define TW_PRESCALE 0x00 ///< Twi prescaler = 0x00(1), 0x01(4), 0x02(16), 0x03(64)
#define TW_BITRATE  0x20 ///< Twi bitrate, baud = fosc/(16 + 2*BR*4^PS)

extern status twi_Init(void);
extern void twi_Write(char slave, char* data, int size);
extern void twi_Read(char slave, char* data, int size);