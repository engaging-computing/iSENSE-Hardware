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
 * @file bmp085_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
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