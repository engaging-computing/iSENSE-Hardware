/**
 * @file isense.c
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
#include "bmp085_interface.h"
#include "rtc_interface.h"
#include "adx_interface.h"
#include "spi_master.h"
#include "gsm_modem_interface.h"
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
	Time time;
    int i;

    cli();

    user_Init();
    timer_Wait_MS(1000);
	// The serial port is now connected to the GSM modem, not the FTDI chip...
//	coms_Init();
    gsm_modem_Init();
    usart_Print_Num(SERIAL, UBRR1L);
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

    // Hard-coded time and date for now since we don't have
	// an application to set the time with...
	// We should really try to get this from the GSM modem
	time.hours = 12; // set to noon
	time.minutes = 0;
	time.seconds = 0;
	
	time.month = 2; // March
	time.dow = 5;   // Thursday
	time.year = 12; // 2012
	time.date = 1; // The 1st
	rtc_Set_Time(&time);

    sei();
    return OK;
}

/**
 * Main function of the program. Communications and buttons are continuously
 * handled while data is collected according to settings and then stored at
 * the prescribed intervals if recording is enabled.
 */
int main (void)
{
    init();
    
	// keep track of if batched data sent via GSM today
	bool gsmSentData = false;
	
    int i;
    bool dataReady = false, recordReady = false;
    
    RunData runData = {false, false, 0, 0, 0};
    
    i = 0;

    DataPoint data;
    sensor_Reset(&data);

    // set the global rate for our river monitoring application
    runData.recordPeriod = (unsigned int) MODEM_DATA_GLOBAL_RATE_H << 8;
    runData.recordPeriod += (unsigned int) MODEM_DATA_GLOBAL_RATE_L;
	data_Clear(); // erase all old data initially	

	// don't read from settings from EEPROM for our application
//    runData.recordPeriod = (unsigned int)data_Read_EEPROM(DATA_GLOBAL_RATE_ADDR_H) << 8;
//    runData.recordPeriod += (unsigned int)data_Read_EEPROM(DATA_GLOBAL_RATE_ADDR_L);

    if (runData.recordPeriod < MINIMUM_GLOBAL_INTERVAL)
    {
        runData.recordPeriod = MINIMUM_GLOBAL_INTERVAL;
    }

    while(1)
    {
        user_Handle_Buttons(&runData);

        // The serial port is now connected to the GSM modem, not the FTDI chip...
//		coms_Handle(&runData);

        gsm_modem_Comms_Handle(&runData);

        dataReady = sensor_Read(&data);

        recordReady |= timer_Job_Ready4(&(runData.recordTimer), runData.recordPeriod, &(runData.recordLastTime), false);

        if (dataReady && recordReady)
        {
            if (runData.record)
            {
				if (data_Write(&data) == ERROR)
				{
					// since this is an un-monitored system we need to keep trying...
	                //runData.record = false;
				}		
				
				// at hours == 12 (noon) we power up the GSM modem and send all the data we have collected
				// then we clear EEPROM
				if (data.bitpack.data.hours == 12 && !gsmSentData)
				{
					gsm_modem_Comms_Transmit_Data(&data);
					gsmSentData = true;
				}
				else if (data.bitpack.data.hours == 13)
				{
					// reset for next day
					gsmSentData = false;
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

