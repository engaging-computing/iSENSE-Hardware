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
 * @file data_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for storing recorded data.
 */

#ifndef _DATA_INTERFACE_H_
#define _DATA_INTERFACE_H_

#include "globals.h"
#include "sensor_interface.h"

#define DATA_GLOBAL_RATE_ADDR_H 0 ///< EEPROM address for the upper byte of the global recording rate.
#define DATA_GLOBAL_RATE_ADDR_L 1 ///< EEPROM address for the lower byte of the global recording rate.

#define DATA_BTA_1_TYPE_ADDR 12 ///< EEPROM address for the BTA 1 sensor type.
#define DATA_BTA_2_TYPE_ADDR 15 ///< EEPROM address for the BTA 2 sensor type. rate.

#define DATA_MINI_1_TYPE_ADDR 18 ///< EEPROM address for the Minijack 1 sensor type.
#define DATA_MINI_2_TYPE_ADDR 21 ///< EEPROM address for the Minijack 2 sensor type.

#define DATA_GPS_THRESHOLD_ADDR 22 ///< EEPROM address for the GPS threshold.

#define DATA_ACCEL_X_OFFSET_H 23 ///< EEPROM address for x offset in accelerometer values.
#define DATA_ACCEL_X_OFFSET_L 24 ///< EEPROM address for x offset in accelerometer values.
#define DATA_ACCEL_Y_OFFSET_H 25 ///< EEPROM address for y offset in accelerometer values.
#define DATA_ACCEL_Y_OFFSET_L 26 ///< EEPROM address for y offset in accelerometer values.
#define DATA_ACCEL_Z_OFFSET_H 27 ///< EEPROM address for z offset in accelerometer values.
#define DATA_ACCEL_Z_OFFSET_L 28 ///< EEPROM address for z offset in accelerometer values.

#define DATA_BT_BAUD_ADDR 29 ///< EEPROM address for bluetooth baud rate
#define DATA_BT_FLAG_ADDR 30 ///< EEPROM address for the bluetooth config flag.

#define DATA_BL_FLAG_ADDR 1019 ///< EEPROM address of the bootloader flag.
#define DATA_SN_24_ADDR   1020 ///< EEPROM address of the upper byte of the serial number.
#define DATA_SN_16_ADDR   1021 ///< EEPROM address of the middle upper byte of the serial number.
#define DATA_SN_8_ADDR    1022 ///< EEPROM address of the middle lower byte of the serial number.
#define DATA_SN_0_ADDR    1023 ///< EEPROM address of the lower byte of the serial number.

#define DATA_DEFAULT_GLOBAL_RATE_H 0x03 ///< Upper byte of the default global recording rate.
#define DATA_DEFAULT_GLOBAL_RATE_L 0xE8 ///< Lower byte of the default global recording rate.
#define DATA_DEFAULT_EXT_TYPE      0x01 ///< Default external sensor type.
#define DATA_DEFAULT_GPS_THRESHOLD 0x05 ///< Default GPS threshold.
#define DATA_DEFAULT_BT_BAUD       0x04 ///< Default bluetooth baud rate.

#define DATA_FLAG_OFF 0x00 ///< Bootloader flag value for normal operation.
#define DATA_FLAG_ON  0xFF ///< Bootloader flag value for update mode.

extern status data_Init(void);
extern void data_Reset_EEPROM(void);
extern status data_Recover_Header(void);
extern status data_Write(DataPoint *data);
extern void data_Clear(void);
extern void data_Write_EEPROM(unsigned int addr, unsigned char data);
extern unsigned char data_Read_EEPROM(unsigned int addr);
extern bool data_Compare(DataPoint *a, DataPoint *b);
extern uint32_t data_Cur_Addr(void);

#endif
