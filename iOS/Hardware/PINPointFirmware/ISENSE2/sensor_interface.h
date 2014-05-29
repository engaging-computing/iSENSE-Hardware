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
 * @file sensor_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for extracting sensor data.
 */

#ifndef _SENSOR_INTERFACE_H_
#define _SENSOR_INTERFACE_H_

#include "globals.h"

#define MAIN_READY     0x01
#define BARO_READY     0x02 ///< Ready bitflag for barometer.
#define BTA_1_READY    0x04 ///< Ready bitflag for bta 1.
#define BTA_2_READY    0x08 ///< Ready bitflag for bta 2
#define MINI_1_READY   0x10 ///< Ready bitflag for minijack 1.
#define MINI_2_READY   0x20 ///< Ready bitflag for minijack 2.

#define ALL_READY 0x3F ///< Sum of all ready bitflags.

#define POWER_PIN   0 ///< Pin number of battery power level.

#define BTA_1_PIN       3 ///< Pin number of BTA 1.
#define BTA_2_PIN       4 ///< Pin number of BTA 2.
#define MINIJACK_1_PIN  5 ///< Pin number of minijack 1.
#define MINIJACK_2_PIN  6 ///< Pin number of minijack 1.
#define HUMIDITY_PIN    7 ///< Pin number of humidity sensor.


#define BTA_PULLUP_PORT C ///< Port letter of the pullup pins.
#define BTA_1_PULLUP_PIN 5 ///< Pin number of BTA 1's pullup.
#define BTA_2_PULLUP_PIN 4 ///< Pin number of BTA 2's pullup.

#define MINIMUM_GLOBAL_INTERVAL  10 ///< Max recording rate is 100Hz.
#define MINIMUM_BARO_INTERVAL    25 ///< Max barometer sample rate is 40Hz.

#define EXTERNAL_COUNT_TYPE 0 ///< External type for counts.

#define LOW_POWER 558 ///< Min power level for low power.
#define BAD_POWER 527 ///< Power level at which sensors start malfunctioning


/**
 * @struct DataPoint Main structure for data storage on the device.
 * All recordings consist of DataPoints stored in flash memory and
 * all live data requests are in the form of DataPoints.
 */
typedef struct
{
    int16_t latHigh;     ///< High byte of the latitude.
    uint16_t latLow;     ///< Low byte of the latitude.
    int16_t lonHigh;     ///< High byte of the longitude.
    uint16_t lonLow;     ///< Low Byte of the longitude.
    uint16_t altitude;   ///< Altitude.
    int32_t pressure;    ///< Pressure (Pa)
    int16_t temperature; ///< Temperature (0.1C)

    /**
     * @union bitpack Union of the bitpacked data and its raw byte-array form.
     */
    union
    {
        unsigned char rawData[16]; ///< Raw data from the bitpack.

        /**
         * @struct data Holds all of the bitpacked data.
         */
        struct
        {
            signed accelX:10; ///< Accelerometer X axis data.
            signed accelY:10; ///< Accelerometer Y axis data.
            signed accelZ:10; ///< Accelerometer Z axis data.

            unsigned lightExp:4;  ///< Light exponent.
            unsigned lightMan:8;  ///< Light Mantissa.
            unsigned humidity:10; ///< Humidity raw data.

            unsigned mini1:10; ///< Minijack 1 raw data.
            unsigned mini2:10; ///< Minijack 2 raw data.
            unsigned  bta1:10; ///< BTA 1 raw data.
            unsigned  bta2:10; ///< BTA 2 raw data.

            unsigned seconds:6; ///< Seconds.
            unsigned minutes:6; ///< Minutes.
            unsigned   hours:5; ///< Hours (24-hour format).
            unsigned     day:3; ///< Day of the week (Sunday is 1 by convention).
            unsigned    date:5; ///< Date of the month.
            unsigned   month:4; ///< Month (January is 1).
            unsigned    year:7; ///< Last two digits of the year (assumed 20XY for the time being).
        } data;
    } bitpack;
} DataPoint;

extern void sensor_Init(bool gps);
extern bool sensor_Read(DataPoint *data);
extern void sensor_Reset(DataPoint *data);

#endif
