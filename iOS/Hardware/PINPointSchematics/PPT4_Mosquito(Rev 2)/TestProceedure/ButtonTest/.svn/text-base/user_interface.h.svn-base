/**
 * @file user_interface.h
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