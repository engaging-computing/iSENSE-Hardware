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
 * @file usart_interface.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for usart communication.
 */

#include "usart_interface.h"
#include <avr/io.h>

/**
 * Initializes usart0 at 2400 baud.
 *
 */
void usart_Init()
{
    /* Set baud rate registers */
    UBRR0H = (unsigned char)(BAUD_19200>>8);
    UBRR0L = (unsigned char)BAUD_19200;
    
    /* Set character size to 8-bit */
    UCSR0C = (3<<UCSZ00);
    
    /* Enable RX and TX */
    set_bit(UCSR0B, RXEN0);
    set_bit(UCSR0B, TXEN0);
}

/**
 * Waits for a byte of data to be recieved from the RX0.
 *
 * @return Returns the read byte.
 */
unsigned char usart_Read()
{
    while ( !(UCSR0A & (1<<RXC0)) );
    return UDR0;
}

/**
 * Waits for TX0's transmit buffer to be empty and
 * then sends the given data to TX0.
 *
 * @param data Data to write.
 */
void usart_Write(unsigned char data)
{
    while (!(test_bit(UCSR0A, UDRE0)));
    UDR0 = data;
}

