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
 * @file user_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for the user interface.
 */

#include "globals.h"

#define BUTTON_PORT  B    ///< Button port.
#define BUTTON_IN    PINB ///< Button input.
#define BUTTON_1_PIN 2    ///< Button 1 pin.
#define BUTTON_2_PIN 1    ///< Button 2 pin.
#define BUTTON_3_PIN 0    ///< Button 3 pin.

#define BUTTON_CLEAR 1 ///< Clear button value.
#define BUTTON_STOP  2 ///< Stop button value.
#define BUTTON_START 3 ///< Start button value.

#define LED_CLEAR 1 ///< Clear LED value.
#define LED_STOP  2 ///< Stop LED value.
#define LED_START 3 ///< Start LED value.
#define LED_POWER 4 ///< Power LED value.
#define LED_GPS   5 ///< GPS LED value.

#define LED_1_PORT  C ///< LED 1 port.
#define LED_1_PIN   3 ///< LED 1 pin.

#define LED_2_PORT  D ///< LED 2 port.
#define LED_2_PIN   6 ///< LED 2 pin.

#define LED_3_PORT  D ///< LED 3 port.
#define LED_3_PIN   5 ///< LED 3 pin.

#define LED_POWER_PORT D ///< Power LED port.
#define LED_POWER_PIN  4 ///< Power LED pin.

#define LED_GPS_PORT D ///< GPS LED port.
#define LED_GPS_PIN  7 ///< GPS LED pin.

extern void user_Init(void);
extern char user_Get_Button(char num);
extern void user_Set_LED(char num, char state);
extern void user_Toggle_LED(char num);
extern void user_Handle_Buttons(RunData *runData);