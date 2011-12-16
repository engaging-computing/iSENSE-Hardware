/**
 * @file globals.c
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

/**
 * Resets the power of the device by starting up the watchdog
 * and purposfully not feeding it during an infintie loop.
 */
void resetDevice()
{
    cli();
    clear_bit(WDTCSR, WDP3);
    clear_bit(WDTCSR, WDP2);
    clear_bit(WDTCSR, WDP1);
    clear_bit(WDTCSR, WDP0);
    
    set_bit(WDTCSR, WDE);
    sei();
    while (1);
}
