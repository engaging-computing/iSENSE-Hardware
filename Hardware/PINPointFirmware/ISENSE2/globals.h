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
 * @file globals.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Some basic utility includes/enums/structs.
 */

#ifndef _GLOBALS_H_
#define _GLOBALS_H_

#include <stdbool.h>
#include <avr/pgmspace.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <stdlib.h>
#include <stdint.h>

#define HIGH       1 ///< High signal.
#define LOW        0 ///< Low signal.
#define ON         1 ///< On state.
#define OFF        0 ///< Off state.
#define ENABLE     1 ///< Enable.
#define DISABLE    0 ///< Disable.
#define PRESSED    1 ///< Button pressed state.
#define UNPRESSED  0 ///< Button not pressed state.

#define set_bit(var, pin)      var |=   1<<(unsigned char) pin           ///< Sets the value of given bit to on.
#define clear_bit(var, pin)    var &= ~(1<<(unsigned char) pin)          ///< Sets the value of given bit to off.
#define test_bit(var, pin)   ((var &   (1<<(unsigned char) pin)) >> pin) ///< Gets value of given bit.
#define toggle_bit(var, pin)   var ^=   1<<(unsigned char) pin           ///< Toggles the value of given bit.

#define swap(a, b) a^=b;b^=a;a^=b ///< XOR swap.
#define min(a, b) (a < b ? a : b) ///< Ternary minimum.
#define max(a, b) (a > b ? a : b) ///< Ternary maximum.

/**
 * @enum portLetter Port letter descriptor.
 */
typedef enum
{
    A, ///< Corrisponds to port A.
    B, ///< Corrisponds to port B.
    C, ///< Corrisponds to port C.
    D  ///< Corrisponds to port D.
} portLetter;

/**
 * @enum portMode Pin mode descriptor.
 */
typedef enum
{
    MODE_INPUT,     ///< Input mode as a sink.
    MODE_INPUT_SRC, ///< Input mode as a source.
    MODE_LOW,       ///< Output mode as a sink.
    MODE_HIGH       ///< Output mode as a source.
} pinMode;

/**
 * @enum status Basic return status for function returns.
 */
typedef enum
{
    OK = 1,   ///< Function operated correctly.
    ERROR = 0 ///< Function encountered some kind of error.
} status;

/**
 * @struct RunData Holds some global runtime data about the program's state.
 */
typedef struct
{
    bool record;                 ///< Stores if the program is currently recording data into flash.
    bool liveData;               ///< Stores if there is a currently active request for live data.
    unsigned int recordPeriod;   ///< Stores the global data recording rate.
    unsigned int recordTimer;    ///< Stores the global recording state.
    unsigned int recordLastTime; ///< Stores the last time the global recording state was updated.
} RunData;

void setPinMode(pinMode m, portLetter l, int pinNum);
void togglePinOutput(portLetter pl, int pinNum);
void resetDevice(void);

#endif
