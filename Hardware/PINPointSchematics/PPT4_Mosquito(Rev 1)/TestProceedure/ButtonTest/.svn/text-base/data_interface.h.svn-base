/**
 * @file data_interface.h
 *
 * Interface functions for storing recorded data.
 */

#ifndef _DATA_INTERFACE_H_
#define _DATA_INTERFACE_H_

#include "globals.h"
#include "sensor_interface.h"

#define DATA_GLOBAL_RATE_ADDR_H 0 ///< EEPROM address for the upper byte of the global recording rate.
#define DATA_GLOBAL_RATE_ADDR_L 1 ///< EEPROM address for the lower byte of the global recording rate.
#define DATA_ACCEL_RATE_ADDR_H  2 ///< EEPROM address for the upper byte of the acceleration subsample rate.
#define DATA_ACCEL_RATE_ADDR_L  3 ///< EEPROM address for the lower byte of the acceleration subsample rate.
#define DATA_LIGHT_RATE_ADDR_H  4 ///< EEPROM address for the upper byte of the light subsample rate.
#define DATA_LIGHT_RATE_ADDR_L  5 ///< EEPROM address for the lower byte of the light subsample rate.
#define DATA_BARO_RATE_ADDR_H   6 ///< EEPROM address for the upper byte of the barometer subsample rate.
#define DATA_BARO_RATE_ADDR_L   7 ///< EEPROM address for the lower byte of the barometer subsample rate.
#define DATA_HUM_RATE_ADDR_H    8 ///< EEPROM address for the upper byte of the humidity subsample rate.
#define DATA_HUM_RATE_ADDR_L    9 ///< EEPROM address for the lower byte of the humidity subsample rate.

#define DATA_BTA_1_RATE_ADDR_H    10 ///< EEPROM address for the upper byte of the BTA 1 subsample rate.
#define DATA_BTA_1_RATE_ADDR_L    11 ///< EEPROM address for the lower byte of the BTA 1 subsample rate.
#define DATA_BTA_1_TYPE_ADDR      12 ///< EEPROM address for the BTA 1 sensor type.
#define DATA_BTA_2_RATE_ADDR_H    13 ///< EEPROM address for the upper byte of the BTA 2 subsample rate.
#define DATA_BTA_2_RATE_ADDR_L    14 ///< EEPROM address for the lower byte of the BTA 2 subsample rate.
#define DATA_BTA_2_TYPE_ADDR      15 ///< EEPROM address for the BTA 2 sensor type.

#define DATA_MINI_1_RATE_ADDR_H    16 ///< EEPROM address for the upper byte of the Minijack 1 subsample rate.
#define DATA_MINI_1_RATE_ADDR_L    17 ///< EEPROM address for the lower byte of the Minijack 1 subsample rate.
#define DATA_MINI_1_TYPE_ADDR      18 ///< EEPROM address for the Minijack 1 sensor type.
#define DATA_MINI_2_RATE_ADDR_H    19 ///< EEPROM address for the upper byte of the Minijack 2 subsample rate.
#define DATA_MINI_2_RATE_ADDR_L    20 ///< EEPROM address for the lower byte of the Minijack 2 subsample rate.
#define DATA_MINI_2_TYPE_ADDR      21 ///< EEPROM address for the Minijack 2 sensor type.

#define DATA_GPS_THRESHOLD_ADDR 22 ///< EEPROM address for the GPS threshold.

#define DATA_ACCEL_X_OFFSET_H 23 ///< EEPROM address for x offset in accelerometer values.
#define DATA_ACCEL_X_OFFSET_L 24 ///< EEPROM address for x offset in accelerometer values.
#define DATA_ACCEL_Y_OFFSET_H 25 ///< EEPROM address for y offset in accelerometer values.
#define DATA_ACCEL_Y_OFFSET_L 26 ///< EEPROM address for y offset in accelerometer values.
#define DATA_ACCEL_Z_OFFSET_H 27 ///< EEPROM address for z offset in accelerometer values.
#define DATA_ACCEL_Z_OFFSET_L 28 ///< EEPROM address for z offset in accelerometer values.

#define DATA_DEFAULT_GLOBAL_RATE_H 0x03 ///< Upper byte of the default global recording rate.
#define DATA_DEFAULT_GLOBAL_RATE_L 0xE8 ///< Lower byte of the default global recording rate.
#define DATA_DEFAULT_SENSOR_RATE_H 0x00 ///< Upper byte of the default sensor sample rate.
#define DATA_DEFAULT_SENSOR_RATE_L 0x02 ///< Lower byte of the default sensor sample rate.
#define DATA_DEFAULT_EXT_TYPE      0x01 ///< Default external sensor type.
#define DATA_DEFAULT_GPS_THRESHOLD 0x04 ///< Default GPS threshold.


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
