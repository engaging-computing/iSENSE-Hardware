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
#include "sensor_interface.h"
#include <avr/pgmspace.h>

/**
 * Initializes usart for the given device (Serial or GPS)
 * with the given baud rate.
 *
 * @param port Descriptor of the port to initialize.
 * @param baud The baud rate.
 */
void usart_Init(usartPort port, unsigned long baud)
{
    if (port == SERIAL)
    {
        /* Set baud rate registers */
        UBRR0H = (unsigned char)(baud>>8);
        UBRR0L = (unsigned char)baud;

        /* Set character size to 8-bit */
        UCSR0C = (3<<UCSZ00);
    }
    else //if (port == GPS)
    {
        /* Set baud rate registers */
        UBRR1H = (unsigned char)(baud>>8);
        UBRR1L = (unsigned char)baud;

        /* Set character size to 8-bit */
        UCSR1C = (3<<UCSZ10);
    }

    usart_RX(port, ENABLE);
    usart_TX(port, ENABLE);
}

/**
 * Waits for a byte of data to be recieved from the given
 * device.
 *
 * @param port Port to read.
 *
 * @return Returns the read byte.
 */
unsigned char usart_Read(usartPort port)
{
    if (port == SERIAL)
    {
        while ( !(UCSR0A & (1<<RXC0)) );
        return UDR0;
    }
    else //if (port == GPS)
    {
        while ( !(UCSR1A & (1<<RXC1)) );
        return UDR1;
    }
}

/**
 * Waits for the given device's transmit buffer to be empty and
 * then sends the given data to that device.
 *
 * @param port Port to write to.
 * @param data Data to write.
 */
void usart_Write(usartPort port, unsigned char data)
{
    if (port == SERIAL)
    {
        while (!(test_bit(UCSR0A, UDRE0)));
        UDR0 = data;
    }
    else //if (port == GPS)
    {
        while (!(test_bit(UCSR1A, UDRE1)));
        UDR1 = data;
    }
}

/**
 * Sends the given string to the given device.
 *
 * @param port Port to write to.
 * @param s String to write.
 */
void usart_String(usartPort port, char *s)
{
    while (s[0] != '\0')
    {
        usart_Write(port, s[0]);
        s++;
    }
}

/**
 * Sends the given string constant to the given device.
 *
 * @param port Port to write to.
 * @param s String constant to write.
 */
void usart_Text(usartPort port, const char *s)
{
    while (pgm_read_byte(s) != 0x00)
    {
        usart_Write(port, pgm_read_byte(s));
        s++;
    }
}

/**
 * Sends an number over uart as ASCII.
 *
 * @param port Port to write to.
 * @param value Number to write.
 */
void usart_Print_Num(usartPort port, int64_t value)
{
    if (value < 0)
    {
        value = -value;
        usart_Write(port, '-');
    }
    else
    {
        usart_Write(port, '+');
    }

    int64_t base = 10;

    while (value / base != 0)
    {
        base *= 10;
    }

    do
    {
        base /= 10;
        usart_Write(port, (value / base) + '0');
        value = value % base;
    }
    while (base > 1);
}

/**
 * Enables or disables interrupt for the input from the given device.
 *
 * @param port Port to configure.
 * @param value Value to set.
 */
void usart_Interrupt_RX(usartPort port, unsigned char value)
{
    if (port == SERIAL)
    {
        if (value == ENABLE) set_bit(UCSR0B, RXCIE0);
        else clear_bit(UCSR0B, RXCIE0);
    }
    else //if (port == GPS)
    {
        if (value == ENABLE) set_bit(UCSR1B, RXCIE1);
        else clear_bit(UCSR1B, RXCIE1);
    }
}

/**
 * Enables or disables interrupt for the input to the given device.
 *
 * @param port Port to configure.
 * @param value Value to set.
 */
void usart_Interrupt_TX(usartPort port, unsigned char value)
{
    if (port == SERIAL)
    {
        if (value == ENABLE) set_bit(UCSR0B, TXCIE0);
        else clear_bit(UCSR0B, TXCIE0);
    }
    else //if (port == GPS)
    {
        if (value == ENABLE) set_bit(UCSR1B, TXCIE1);
        else clear_bit(UCSR1B, TXCIE1);
    }
}

/**
 * Enables or disables input from the given device.
 *
 * @param port Port to configure.
 * @param value Value to set.
 */
void usart_RX(usartPort port, unsigned char value)
{
    if (port == SERIAL)
    {
        if (value == ENABLE) set_bit(UCSR0B, RXEN0);
        else clear_bit(UCSR0B, RXEN0);
    }
    else //if (port == GPS)
    {
        if (value == ENABLE) set_bit(UCSR1B, RXEN1);
        else clear_bit(UCSR1B, RXEN1);
    }
}

/**
 * Enables or disables output to the given device.
 *
 * @param port Port to configure.
 * @param value Value to set.
 */
void usart_TX(usartPort port, unsigned char value)
{
    if (port == SERIAL)
    {
        if (value == ENABLE) set_bit(UCSR0B, TXEN0);
        else clear_bit(UCSR0B, TXEN0);
    }
    else //if (port == GPS)
    {
        if (value == ENABLE) set_bit(UCSR1B, TXEN1);
        else clear_bit(UCSR1B, TXEN1);
    }
}
