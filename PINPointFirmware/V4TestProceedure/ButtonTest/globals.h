/**
 * @file globals.h
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

#define swap(a, b) a^=b;b^=a;a^=b; ///< XOR swap.

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
