/**
 * @file light_interface.h
 *
 * Interface functions for the onboard light sensor.
 */

#include "globals.h"
#include "usart_interface.h"
#include "twi_interface.h"

#define LIGHT_ADDR       0xB4 ///< TWI address of the light sensor.
#define LIGHT_VAL_1_ADDR 0x03 ///< Address of the first light value register.
#define LIGHT_VAL_2_ADDR 0x04 ///< Address of the second light value register.
#define LIGHT_ICTRL_ADDR 0x01 ///< Address of the light interrupt control register.
#define LIGHT_CTRL_ADDR  0x02 ///< Address of the light control register.

#define LIGHT_ICTRL_VAL 0x00 ///< Config value for the interrupt control register.
#define LIGHT_CTRL_VAL  0xC3 ///< Config value for the control register.

extern void light_Init(void);
extern void light_Read(uint8_t* exp, uint8_t* man);
