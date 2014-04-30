/**
 * @file adx_interface.h
 *
 * Interface functions for the adxl345 accelerometer.
 */

#include "globals.h"
#include "usart_interface.h"
#include "twi_interface.h"


#define ADXL_ADDR      0xA6 ///< TWI address of the adx accelerometer.
#define ADXL_DATA_ADDR 0x32 ///< Address of the adx data register.

#define ADXL_OFF_REG 0x1F ///< Address of the adx offset register.
#define ADXL_TAP_REG 0x2A ///< Address of the adx tap control register.
#define ADXL_RAT_REG 0x2C ///< Address of the adx rate control register.
#define ADXL_CTL_REG 0x2D ///< Address of the adx control register.
#define ADXL_INT_REG 0x2E ///< Address of the adx interrupt register.
#define ADXL_DAT_REG 0x31 ///< Address of the adx data config register.
#define ADXL_FIF_REG 0x38 ///< Address of the adx fifo config register.


#define ADXL_TAP_VAL 0x00 ///< Config value for the adx tap control register.
#define ADXL_RAT_VAL 0x08 ///< Config value for the adx rate control register.
#define ADXL_CTL_VAL 0x08 ///< Config value for the adx control register.
#define ADXL_INT_VAL 0x00 ///< Config value for the adx interrupt register.
#define ADXL_DAT_VAL 0x02 ///< Config value for the adx data config register.
#define ADXL_FIF_VAL 0x0F ///< Config value for the adx fifo config register.

#define ADXL_1G 64 ///< Offset value for one g.


extern void adx_Init(void);
extern void adx_Calibrate(void);
extern void adx_Read_Accel(int* x, int* y, int* z);