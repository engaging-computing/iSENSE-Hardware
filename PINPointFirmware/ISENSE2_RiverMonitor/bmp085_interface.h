/**
 * @file bmp085_interface.h
 *
 * Interface functions for the BMP085 pressure and temperature sensor.
 */

#include "globals.h"
#include "usart_interface.h"
#include "twi_interface.h"


#define BAR_ADDR 0xEE ///< TWI address of the barometer.

#define BAR_CON_ADDR 0xAA ///< Address of barometer constant registers.
#define BAR_REQ_ADDR 0xF4 ///< Address of barometer request register.
#define BAR_RES_ADDR 0xF6 ///< Address of barometer result register.
#define BAR_TEMP_REQ 0x2E ///< Temperature request value.
#define BAR_PRES_REQ 0x34 ///< Pressure (fastest) request value.

extern void bmp_Init(void);
extern void bmp_Request_Temperature(void);
extern void bmp_Request_Pressure(void);
extern int16_t bmp_Read_Temperature(void);
extern int32_t bmp_Read_Pressure(void);