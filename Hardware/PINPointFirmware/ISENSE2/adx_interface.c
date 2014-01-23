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
 * @file adx_interface.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for the adxl345 accelerometer.
 */

#include "adx_interface.h"
#include "data_interface.h"
#include "timer_interface.h"

static int16_t xOff; ///< X offset
static int16_t yOff; ///< Y offset
static int16_t zOff; ///< Z offset

/**
 * Send initialization messages to the adx accelerometer.
 */
void adx_Init(void)
{
    char out[4];

    out[0] = ADXL_TAP_REG;
    out[1] = ADXL_TAP_VAL;
    twi_Write(ADXL_ADDR, out, 2);

    out[0] = ADXL_RAT_REG;
    out[1] = ADXL_RAT_VAL;
    twi_Write(ADXL_ADDR, out, 2);

    out[0] = ADXL_CTL_REG;
    out[1] = ADXL_CTL_VAL;
    twi_Write(ADXL_ADDR, out, 2);

    out[0] = ADXL_INT_REG;
    out[1] = ADXL_INT_VAL;
    twi_Write(ADXL_ADDR, out, 2);

    out[0] = ADXL_DAT_REG;
    out[1] = ADXL_DAT_VAL;
    twi_Write(ADXL_ADDR, out, 2);

    out[0] = ADXL_FIF_REG;
    out[1] = ADXL_FIF_VAL;
    twi_Write(ADXL_ADDR, out, 2);

    out[0] = ADXL_OFF_REG;
    out[1] = 0;
    out[2] = 0;
    out[3] = 0;
    twi_Write(ADXL_ADDR, out, 4);

    char tmp[2];

    tmp[0] = data_Read_EEPROM(DATA_ACCEL_X_OFFSET_L);
    tmp[1] = data_Read_EEPROM(DATA_ACCEL_X_OFFSET_H);
    xOff = ((int16_t*)tmp)[0];

    tmp[0] = data_Read_EEPROM(DATA_ACCEL_Y_OFFSET_L);
    tmp[1] = data_Read_EEPROM(DATA_ACCEL_Y_OFFSET_H);
    yOff = ((int16_t*)tmp)[0];

    tmp[0] = data_Read_EEPROM(DATA_ACCEL_Z_OFFSET_L);
    tmp[1] = data_Read_EEPROM(DATA_ACCEL_Z_OFFSET_H);
    zOff = ((int16_t*)tmp)[0];
}

/**
 * Gathers 10 samples and sets the acceleration offsets
 * assuming the device is on a flat surface facing up.
 */
void adx_Calibrate()
{
    int i;
    int16_t x = 0, y = 0, z = 0;
    char in[6];
    char out = ADXL_DATA_ADDR;

    for (i = 0; i < 10; i++)
    {
        twi_Write(ADXL_ADDR, &out, 1);
        twi_Read(ADXL_ADDR, in, 6);

        x += ((int*)in)[0];
        y += ((int*)in)[1];
        z += ((int*)in)[2];
    }

    xOff = -(x / 10);
    yOff = -(y / 10);
    zOff = -(z / 10) + ADXL_1G;

    data_Write_EEPROM(DATA_ACCEL_X_OFFSET_L, ((char*)(&xOff))[0]);
    data_Write_EEPROM(DATA_ACCEL_X_OFFSET_H, ((char*)(&xOff))[1]);

    data_Write_EEPROM(DATA_ACCEL_Y_OFFSET_L, ((char*)(&yOff))[0]);
    data_Write_EEPROM(DATA_ACCEL_Y_OFFSET_H, ((char*)(&yOff))[1]);

    data_Write_EEPROM(DATA_ACCEL_Z_OFFSET_L, ((char*)(&zOff))[0]);
    data_Write_EEPROM(DATA_ACCEL_Z_OFFSET_H, ((char*)(&zOff))[1]);
}

/**
 * Reads acceleration values from the adx accelerometer.
 *
 * @param x Address to integer for storing the x acceleration.
 * @param y Address to integer for storing the y acceleration.
 * @param z Address to integer for storing the z acceleration.
 */
void adx_Read_Accel(int* x, int* y, int* z)
{
    char out = ADXL_DATA_ADDR;
    char in[6];

    twi_Write(ADXL_ADDR, &out, 1);
    twi_Read(ADXL_ADDR, in, 6);

    *x = (int)(in[0] + ((in[1]) << 8)) + xOff;
    *y = (int)(in[2] + ((in[3]) << 8)) + yOff;
    *z = (int)(in[4] + ((in[5]) << 8)) + zOff;
}
