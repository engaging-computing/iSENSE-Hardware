/**
 * @file usart_interface.c
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

