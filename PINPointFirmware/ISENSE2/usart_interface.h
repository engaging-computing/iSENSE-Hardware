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
 * @file usart_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
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
