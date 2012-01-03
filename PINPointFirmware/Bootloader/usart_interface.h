/**
 * @file usart_interface.h
 *
 * Interface functions for usart communication.
 */

#ifndef _USART_INTERFACE_H_
#define _USART_INTERFACE_H_

#define BAUD_38400  14
#define BAUD_19200  29
#define BAUD_2400  239
#define  set_bit(var, pin)     var |=   1<<(unsigned char) pin
#define test_bit(var, pin)   ((var &   (1<<(unsigned char) pin)) >> pin)

void usart_Init(void);
unsigned char usart_Read(void);
void usart_Write(unsigned char data);

#endif
