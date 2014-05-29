/**
 * @file usart_interface.h
 *
 * Interface functions for usart communication.
 */

#ifndef _USART_INTERFACE_H_
#define _USART_INTERFACE_H_

#include "globals.h"

#define CARRIAGE_RETURN 0x0D ///< Carriage return value.
#define LINE_FEED	    0x0A ///< Line feed value.

typedef enum {GPS, SERIAL} usartPort;

extern void usart_Init(usartPort port, unsigned long baud);
extern unsigned char usart_Read(usartPort port);
extern void usart_Write (usartPort port, unsigned char data );
extern void usart_String(usartPort port, char *s);
extern void usart_Text (usartPort port, const char *s );
extern void usart_Print_Num(usartPort port, int64_t value);
extern void usart_Interrupt_RX(usartPort port, unsigned char value);
extern void usart_Interrupt_TX(usartPort port, unsigned char);
extern void usart_RX(usartPort port, unsigned char value);
extern void usart_TX(usartPort port, unsigned char value);

#endif
