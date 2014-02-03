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
 * @file sensor_interface.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for extracting sensor data.
 */

#include "user_interface.h"
#include "timer_interface.h"
#include "sensor_interface.h"
#include "adc_interface.h"
#include "usart_interface.h"
#include "data_interface.h"

#include "gps_interface.h"
#include "rtc_interface.h"
#include "adx_interface.h"
#include "bmp085_interface.h"
#include "light_interface.h"

#include <avr/pgmspace.h>

static JobTimer mainJob = {0,0,0};
static JobTimer baroJob = {0,0,0};

static JobTimer bta1Job = {0,0,0};
static JobTimer bta2Job = {0,0,0};
static JobTimer mini1Job = {0,0,0};
static JobTimer mini2Job = {0,0,0};

static JobTimer gpsPollJob = {0,1000,0};

static JobTimer powerJob = {0,500,0};
static JobTimer badPowerJob = {0,200,0};

static char bta1Type, bta2Type, mini1Type, mini2Type, extNum;
static uint16_t extInts, ready;
static char baroState = 2;

static bool gpsEnabled, baroEnabled;

/**
 * Increments a counter in the event of a rising edge
 * on the external sensor. Only active when count-type
 * is enabled via EEPROM setting.
 */
ISR(PCINT0_vect)
{
    extInts++;
}

/**
 * Initializes the EEPROM-stored sensor settings.
 * Also calls the timer, user and gps initializers.
 */
void sensor_Init(bool gps)
{
    adc_Init(ADC_EXTERNAL, ADC_10BIT_RES);

    timer_Init();
    twi_Init();
    bmp_Init();
    adx_Init();
    light_Init();

    if (gps) gps_Init();

    unsigned int globalRate = (unsigned int)data_Read_EEPROM(DATA_GLOBAL_RATE_ADDR_H) << 8;
    globalRate += (unsigned int)data_Read_EEPROM(DATA_GLOBAL_RATE_ADDR_L);
    
    bta1Type = data_Read_EEPROM(DATA_BTA_1_TYPE_ADDR);
    bta2Type = data_Read_EEPROM(DATA_BTA_2_TYPE_ADDR);

    mini1Type = data_Read_EEPROM(DATA_MINI_1_TYPE_ADDR);
    mini2Type = data_Read_EEPROM(DATA_MINI_2_TYPE_ADDR);

    gpsEnabled = (data_Read_EEPROM(DATA_GPS_THRESHOLD_ADDR) != 0);
    baroEnabled = globalRate >= MINIMUM_BARO_INTERVAL;
    
    //Enforce rate limits
    if (globalRate < MINIMUM_GLOBAL_INTERVAL) globalRate = MINIMUM_GLOBAL_INTERVAL;

    //Set Porta A to input mode.
    DDRA = PORTA = 0;

    //Turn ON pullups
    setPinMode(MODE_LOW, BTA_PULLUP_PORT, BTA_1_PULLUP_PIN);
    setPinMode(MODE_LOW, BTA_PULLUP_PORT, BTA_2_PULLUP_PIN);

    cli();

    clear_bit(EICRA, 0);
    clear_bit(EICRA, 1);
    clear_bit(PCMSK0, 6);
    PCICR = 0;
    extNum = 0;

    if (bta1Type == EXTERNAL_COUNT_TYPE)
    {
        set_bit(EICRA, 0);
        set_bit(EICRA, 1);
        set_bit(PCMSK0, 6);
        set_bit(PCICR, BTA_1_PIN);

        setPinMode(MODE_HIGH, BTA_PULLUP_PORT, BTA_1_PULLUP_PIN);
        extNum = 1;
    }
    else if (bta2Type == EXTERNAL_COUNT_TYPE)
    {
        set_bit(EICRA, 0);
        set_bit(EICRA, 1);
        set_bit(PCMSK0, 6);
        set_bit(PCICR, BTA_2_PIN);

        setPinMode(MODE_HIGH, BTA_PULLUP_PORT, BTA_2_PULLUP_PIN);
        extNum = 2;
    }
    else if (mini1Type == EXTERNAL_COUNT_TYPE)
    {
        set_bit(EICRA, 0);
        set_bit(EICRA, 1);
        set_bit(PCMSK0, 6);
        set_bit(PCICR, MINIJACK_1_PIN);
        extNum = 3;
    }
    else if (mini2Type == EXTERNAL_COUNT_TYPE)
    {
        set_bit(EICRA, 0);
        set_bit(EICRA, 1);
        set_bit(PCMSK0, 6);
        set_bit(PCICR, MINIJACK_2_PIN);
        extNum = 4;
    }

    sei();

    mainJob.trigger = globalRate / 2;
    baroJob.trigger = globalRate / 2 - 6;

    bta1Job.trigger  = extNum == 1 ? globalRate : globalRate / 2;
    bta2Job.trigger  = extNum == 2 ? globalRate : globalRate / 2;
    mini1Job.trigger = extNum == 3 ? globalRate : globalRate / 2;
    mini2Job.trigger = extNum == 4 ? globalRate : globalRate / 2;
}

/**
 * Reads in values from sensors and adds them to a moving average contained
 * in the given DataPoint. Each sensor has a job timer that is determined
 * either by defaults or values in EEPROM. The sensor is read and averaged
 * every time its job timer activates.
 *
 * @param data The DataPoint to store the sensor data in.
 *
 * @return Returns true if the full data set has been gathered, false if it is still in process.
 */
bool sensor_Read(DataPoint *data)
{
    if (timer_Job_Ready2(mainJob, false))
    {
        ///Acelerometer
        int16_t x, y, z;
        adx_Read_Accel(&x, &y, &z);
        
        (*data).bitpack.data.accelX = x;
        (*data).bitpack.data.accelY = y;
        (*data).bitpack.data.accelZ = z;
        
        ///Light
        uint8_t exp, man;
        light_Read(&exp, &man);
            
        (*data).bitpack.data.lightExp = exp;
        (*data).bitpack.data.lightMan = man;
        
        ///Calendar
        rtc_Read_Time(data);
        
        ///Humidity
        (*data).bitpack.data.humidity = adc_Read(HUMIDITY_PIN);
        
         ///GPS Pull
        if (gpsEnabled)
        {
            if (gps_Pull(data))
            {
                user_Set_LED(LED_GPS, ON);
            }
            else
            {
                user_Set_LED(LED_GPS, OFF);
            }
        }
        
        ready |= MAIN_READY;
    }
    
    ///Barometer
    if (baroEnabled && timer_Job_Ready2(baroJob, false))
    {
        baroState = (baroState + 1) % 3;
        
        switch (baroState)
        {
            case 0:
                    bmp_Request_Temperature();
                    baroJob.timer = baroJob.trigger - 6;
                    break;
            case 1:
                (*data).temperature = bmp_Read_Temperature();
                bmp_Request_Pressure();
                baroJob.timer = baroJob.trigger - 6;
                break;
            case 2:
                (*data).pressure = bmp_Read_Pressure();
                
                ready |= BARO_READY;
                
                break;
        }
    }
    
    ///BTA1
    if (timer_Job_Ready2(bta1Job, false))
    {
        if (extNum == 1)
        {
            (*data).bitpack.data.bta1 = extInts / 2;
            ready |= BTA_1_READY;
        }
        else
        {
            (*data).bitpack.data.bta1 = adc_Read(BTA_1_PIN);
            
            ready |= BTA_1_READY;
        }
    }

    ///BTA2
    if (timer_Job_Ready2(bta2Job, false))
    {
        if (extNum == 2)
        {
            (*data).bitpack.data.bta2 = extInts / 2;
            ready |= BTA_2_READY;
        }
        else
        {
            (*data).bitpack.data.bta2 = adc_Read(BTA_2_PIN);
            
            ready |= BTA_2_READY;
        }
    }


    ///Mini1
    if (timer_Job_Ready2(mini1Job, false))
    {
        if (extNum == 3)
        {
            (*data).bitpack.data.mini1 = extInts / 2;
            ready |= MINI_1_READY;
        }
        else
        {
            (*data).bitpack.data.mini1 = adc_Read(MINIJACK_1_PIN);
            
            ready |= MINI_1_READY;
        }
    }


    ///Mini1
    if (timer_Job_Ready2(mini2Job, false))
    {
        if (extNum == 4)
        {
            (*data).bitpack.data.mini2 = extInts / 2;
            ready |= MINI_2_READY;
        }
        else
        {
            (*data).bitpack.data.mini2 = adc_Read(MINIJACK_2_PIN);
            
            ready |= MINI_2_READY;
        }
    }

    ///GPS Poll
    if (gpsEnabled && timer_Job_Ready2(gpsPollJob, false))
    {
        gps_Poll();
    }

    ///Low Power
    if (timer_Job_Ready2(powerJob, false))
    {
        if (adc_Read(POWER_PIN) <= LOW_POWER && adc_Read(POWER_PIN) > BAD_POWER)
        {
            user_Toggle_LED(LED_POWER);
        }
        else if (adc_Read(POWER_PIN) > LOW_POWER)
        {
            user_Set_LED(LED_POWER, ON);
        }
    }

    ///Critical Power
    if (timer_Job_Ready2(badPowerJob, false))
    {
        if (adc_Read(POWER_PIN) <= BAD_POWER)
        {
            user_Toggle_LED(LED_POWER);
        }
    }

    return ready == ALL_READY;
}

/**
 * Resets the moving averages in the given DataPoint. Also resets all of the
 * counters controlling data collection.
 *
 * @param data The DataPoint to reset.
 */
void sensor_Reset(DataPoint *data)
{
    unsigned int time = timer_Clock_Read_MS();

    ready = 0;

    mainJob.timer = 0;
    mainJob.last = time;
    
    baroJob.timer = 0;
    baroJob.last = time;
    baroState = 2;

    bta1Job.timer = 0;
    bta1Job.last = time;

    bta2Job.timer = 0;
    bta2Job.last = time;

    mini1Job.timer = 0;
    mini1Job.last = time;

    mini2Job.timer = 0;
    mini2Job.last = time;

    if (!baroEnabled)
    {
        data->temperature = 0;
        data->pressure = 0;
        ready |= BARO_READY;
    }
    
    if (!gpsEnabled)
    {
       data->latHigh = 20000;
       data->latLow = 0;
       data->lonHigh = 20000;
       data->lonLow = 0;
       data->altitude = 60000;
    }
}

