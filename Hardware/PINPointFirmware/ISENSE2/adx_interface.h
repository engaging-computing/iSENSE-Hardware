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
 * @file adx_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
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
#define ADXL_RAT_VAL 0x0E ///< Config value for the adx rate control register.
#define ADXL_CTL_VAL 0x08 ///< Config value for the adx control register.
#define ADXL_INT_VAL 0x00 ///< Config value for the adx interrupt register.
#define ADXL_DAT_VAL 0x02 ///< Config value for the adx data config register.
#define ADXL_FIF_VAL 0x0F ///< Config value for the adx fifo config register.

#define ADXL_1G 64 ///< Offset value for one g.


extern void adx_Init(void);
extern void adx_Calibrate(void);
extern void adx_Read_Accel(int* x, int* y, int* z);