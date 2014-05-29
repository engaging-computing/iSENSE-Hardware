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
 * @file globals.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Some basic utility includes/enums/structs.
 */

#include "globals.h"

/**
 * Puts the described pin into the given mode.
 *
 * @param mode Mode to put the pin in.
 * @param portLetter Letter descriptor of the pin's port.
 * @param pinNum Pin number being configured.
 */
void setPinMode(pinMode mode, portLetter portLetter, int pinNum)
{
    switch (mode)
    {
        case MODE_INPUT:
            switch (portLetter)
            {
                case A:
                    clear_bit(DDRA,  pinNum);
                    clear_bit(PORTA, pinNum);
                    break;
                case B:
                    clear_bit(DDRB,  pinNum);
                    clear_bit(PORTB, pinNum);
                    break;
                case C:
                    clear_bit(DDRC,  pinNum);
                    clear_bit(PORTC, pinNum);
                    break;
                case D:
                    clear_bit(DDRD,  pinNum);
                    clear_bit(PORTD, pinNum);
                    break;
            }
            break;

        case MODE_INPUT_SRC:
            switch (portLetter)
            {
                case A:
                    clear_bit(DDRA,  pinNum);
                    set_bit(PORTA,   pinNum);
                    break;
                case B:
                    clear_bit(DDRB,  pinNum);
                    set_bit(PORTB,   pinNum);
                    break;
                case C:
                    clear_bit(DDRC,  pinNum);
                    set_bit(PORTC,   pinNum);
                    break;
                case D:
                    clear_bit(DDRD,  pinNum);
                    set_bit(PORTD,   pinNum);
                    break;
            }
            break;

        case MODE_LOW:
            switch (portLetter)
            {
                case A:
                    set_bit(DDRA,    pinNum);
                    clear_bit(PORTA, pinNum);
                    break;
                case B:
                    set_bit(DDRB,    pinNum);
                    clear_bit(PORTB, pinNum);
                    break;
                case C:
                    set_bit(DDRC,    pinNum);
                    clear_bit(PORTC, pinNum);
                    break;
                case D:
                    set_bit(DDRD,    pinNum);
                    clear_bit(PORTD, pinNum);
                    break;
            }
            break;

        case MODE_HIGH:
            switch (portLetter)
            {
                case A:
                    set_bit(DDRA,  pinNum);
                    set_bit(PORTA, pinNum);
                    break;
                case B:
                    set_bit(DDRB,  pinNum);
                    set_bit(PORTB, pinNum);
                    break;
                case C:
                    set_bit(DDRC,  pinNum);
                    set_bit(PORTC, pinNum);
                    break;
                case D:
                    set_bit(DDRD,  pinNum);
                    set_bit(PORTD, pinNum);
                    break;
            }
            break;
    }
}


/**
 * Toggles the given pin output.
 *
 * @param pl Port descriptor for pin.
 * @param pinNum Pin number for pin.
 */
void togglePinOutput(portLetter pl, int pinNum)
{
    switch (pl)
    {
        case A:
            toggle_bit(PORTA, pinNum);
            break;
        case B:
            toggle_bit(PORTB, pinNum);
            break;
        case C:
            toggle_bit(PORTC, pinNum);
            break;
        case D:
            toggle_bit(PORTD, pinNum);
            break;
    }
}
