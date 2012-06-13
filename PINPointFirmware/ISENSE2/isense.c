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
 * @file isense.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Main program file.
 */

#include "globals.h"
#include "user_interface.h"
#include "timer_interface.h"
#include "usart_interface.h"
#include "adc_interface.h"
#include "gps_interface.h"
#include "data_interface.h"
#include "sensor_interface.h"
#include "coms_interface.h"
#include "bmp085_interface.h"
#include "rtc_interface.h"
#include "adx_interface.h"
#include "spi_master.h"
#include <avr/pgmspace.h>
#include <avr/wdt.h>

//Verify watchdog is off
uint8_t mcusr_mirror __attribute__ ((section (".noinit")));
void get_mcusr(void) \
__attribute__((naked)) \
__attribute__((section(".init3")));
void get_mcusr(void)
{
    mcusr_mirror = MCUSR;
    MCUSR = 0;
    wdt_disable();
}

/**
 * Initializes all of the device systems. If the clear button
 * is held down at the end of the initialization then defaults
 * are restored to all EEPROM settings.
 *
 * @return Returns 'OK' if previous data was found by data_Init(), 'ERROR' otherwise.
 */
status init(void)
{
    int i;

    cli();

    user_Init();
    timer_Wait_MS(1000);
    coms_Init();
    sensor_Init(true);
    
    data_Init();

    if (user_Get_Button(BUTTON_CLEAR))
    {

        for (i = 0; i < 50; i++)
        {
            timer_Wait_MS(50);
            user_Toggle_LED(LED_CLEAR);
            user_Toggle_LED(LED_STOP);
            user_Toggle_LED(LED_START);
            user_Toggle_LED(LED_POWER);
        }

        adx_Calibrate();
        rtc_Init();
        data_Reset_EEPROM();

        wdt_enable(WDTO_120MS);
    }

    sei();

    return OK;
}

/**
 * Main function of the program. Communications and buttons are continously
 * handled while data is collected according to settings and then stored at
 * the perscribed intervals if recording is enabled.
 */
int main (void)
{
    init();
    
    int i;
    bool dataReady = false, recordReady = false;
    
    RunData runData = {false, false, 0, 0, 0};
    
    i = 0;

    DataPoint data;
    sensor_Reset(&data);

    runData.recordPeriod = (unsigned int)data_Read_EEPROM(DATA_GLOBAL_RATE_ADDR_H) << 8;
    runData.recordPeriod += (unsigned int)data_Read_EEPROM(DATA_GLOBAL_RATE_ADDR_L);

    if (runData.recordPeriod < MINIMUM_GLOBAL_INTERVAL)
    {
        runData.recordPeriod = MINIMUM_GLOBAL_INTERVAL;
    }

    while(1)
    {
        user_Handle_Buttons(&runData);

        coms_Handle(&runData);

        dataReady = sensor_Read(&data);

        recordReady |= timer_Job_Ready4(&(runData.recordTimer), runData.recordPeriod, &(runData.recordLastTime), false);

        if (dataReady && recordReady)
        {

            if (runData.record)
            {
                if (data_Write(&data) == ERROR)
                {
                    runData.record = false;
                }
            }
            else if (runData.liveData)
            {
                for (i = 0; i < sizeof(DataPoint); i++) usart_Write(SERIAL, ((unsigned char*)(&data))[i]);
                runData.liveData = false;
            }

            runData.recordTimer = 0;
            recordReady = false;

            sensor_Reset(&data);
        }
        
        timer_Sleep();
    }
}

