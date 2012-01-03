/**
 * @file isense.c
 *
 * Main program file.
 */

#include "globals.h"
#include "user_interface.h"
#include "usart_interface.h"
#include "adc_interface.h"
#include "adx_interface.h"
#include "bmp085_interface.h"
#include "rtc_interface.h"
#include "spi_master.h"
#include "timer_interface.h"
#include "light_interface.h"
#include "coms_interface.h"

/**
 * Initializes all of the device systems. If the clear button
 * is held down at the end of the initialization then defaults
 * are restored to all EEPROM settings.
 *
 * @return Returns 'OK' if previous data was found by data_Init(), 'ERROR' otherwise.
 */
int main(void)
{
    cli();
    ///Verify Watchdog is off
    MCUSR &= ~(1<<WDRF);
    WDTCSR |= (1 << WDE) | (1 << WDCE);
    WDTCSR = 0x00;

    adc_Init(ADC_EXTERNAL, ADC_10BIT_RES);
    user_Init();
    coms_Init();

    sei();
    
    DataPoint d;
    
    timer_Wait_MS(1000);
    
    /////////////////////////////////Accel Test////////////////////////////////////////////////
    
    usart_Text(SERIAL, PSTR("Initializing accelerometer....\r\n"));
    usart_Text(SERIAL, PSTR("(If this does not complete within one second, there is an issue with the accelerometer)\r\n"));
    
    timer_Init();
    twi_Init();
    adx_Init();
    
    usart_Text(SERIAL, PSTR("Initialization complete.\r\n"));
    usart_Text(SERIAL, PSTR("Testing accelerometer...\r\n"));
    
    int x, y, z;
    adx_Read_Accel(&x, &y, &z);
    
    usart_Print_Num(SERIAL, x + y + z);
    usart_Text(SERIAL, PSTR("\r\n"));
    
    if (0 != x + y + z)
    {
        usart_Text(SERIAL, PSTR("Accelerometer test passed.\r\n\r\n"));
    }
    else
    {
        usart_Text(SERIAL, PSTR("Accelerometer test failed.\r\n"));
        usart_Text(SERIAL, PSTR("Verify Chip at U5 is correct.\r\n"));
        usart_Text(SERIAL, PSTR("Please attempt to fix the issue and re-run this test.\r\n"));
        
        usart_Write(SERIAL, 2);
        return ERROR;
    }
    
    /////////////////////////////////RTC Test/////////////////////////////////////////////////
    
    usart_Text(SERIAL, PSTR("Initializing rtc....\r\n"));
    usart_Text(SERIAL, PSTR("(If this does not complete within one second, there is an issue with the rtc)\r\n"));
    
    rtc_Init();
    
    usart_Text(SERIAL, PSTR("Initialization complete.\r\n"));
    usart_Text(SERIAL, PSTR("Testing rtc...\r\n"));
    
    rtc_Read_Time(&d);
    
    usart_Text(SERIAL, PSTR("rtc test passed.\r\n\r\n"));
    
    /////////////////////////////////Pressure/Temperature Test////////////////////////////////////////
    
    usart_Text(SERIAL, PSTR("Initializing pressure/temperature....\r\n"));
    usart_Text(SERIAL, PSTR("(If this does not complete within one second, there is an issue with the pressure/temperature sensor)\r\n"));
    
    bmp_Init();
    
    usart_Text(SERIAL, PSTR("Initialization complete.\r\n"));
    usart_Text(SERIAL, PSTR("Testing pressure/temperature...\r\n"));
    
    int16_t temp;
    int32_t pressure;
    bmp_Request_Temperature();
    timer_Wait_MS(200);
    temp = bmp_Read_Temperature();
    usart_Print_Num(SERIAL, temp);
    usart_Text(SERIAL, PSTR("\r\n"));
    
    bmp_Request_Pressure();
    timer_Wait_MS(200);
    pressure = bmp_Read_Pressure();
    usart_Print_Num(SERIAL, pressure);
    usart_Text(SERIAL, PSTR("\r\n"));
    
    if (0 != temp && 0 != pressure)
    {
        usart_Text(SERIAL, PSTR("Pressure/Temperature test passed.\r\n\r\n"));
    }
    else
    {
        usart_Text(SERIAL, PSTR("Pressure/Temperature test failed.\r\n"));
        usart_Text(SERIAL, PSTR("Verify Chip at U11 is correct.\r\n"));
        usart_Text(SERIAL, PSTR("Please attempt to fix the issue and re-run this test.\r\n"));
        
        usart_Write(SERIAL, 2);
        return ERROR;
    }
    
    /////////////////////////////////Light Test////////////////////////////////////////
    
    usart_Text(SERIAL, PSTR("Initializing light....\r\n"));
    usart_Text(SERIAL, PSTR("(If this does not complete within one second, there is an issue with the light sensor)\r\n"));
    
    light_Init();
    
    usart_Text(SERIAL, PSTR("Initialization complete.\r\n"));
    usart_Text(SERIAL, PSTR("Testing light...\r\n"));
    
    uint8_t exp, man;
    
    timer_Wait_MS(200);
    light_Read(&exp, &man);
    usart_Print_Num(SERIAL, exp);
    usart_Text(SERIAL, PSTR("\r\n"));
    usart_Print_Num(SERIAL, man);
    usart_Text(SERIAL, PSTR("\r\n"));
    
    if (0 != exp && 0 != man)
    {
        usart_Text(SERIAL, PSTR("Light test passed.\r\n\r\n"));
    }
    else
    {
        usart_Text(SERIAL, PSTR("Light test failed.\r\n"));
        usart_Text(SERIAL, PSTR("Verify Chip at U6 is correct.\r\n"));
        usart_Text(SERIAL, PSTR("Please attempt to fix the issue and re-run this test.\r\n"));
        
        usart_Write(SERIAL, 2);
        return ERROR;
    }
    
    /////////////////////////////////Flash Test////////////////////////////////////////
    usart_Text(SERIAL, PSTR("Initializing flash....\r\n"));
    usart_Text(SERIAL, PSTR("(If this does not complete within one second, there is an issue with the flash memory.)\r\n"));
    
    char failed = 0;
    retry_flash:
    
    spi_MasterInit();
    
    timer_Wait_MS(250);
    
    usart_Text(SERIAL, PSTR("Initialization complete.\r\n"));
    usart_Text(SERIAL, PSTR("Testing flash...\r\n"));
    
    uint32_t addr = 0;
    uint16_t dat = 42;
    
    sst_AAI_Write((char*)(&addr), (char*)(&dat), 2);
    sst_Read((char*)(&addr), (char*)(&dat), 2);
    
    if (dat != 42)
    {
        usart_Text(SERIAL, PSTR("Flash test failed.\r\n"));
        
        if (failed++ < 3)
        {
            usart_Text(SERIAL, PSTR("Attempting to retry... \r\n"));
            goto retry_flash;
        }
        else
        {
            usart_Text(SERIAL, PSTR("Verify Chip at U3 is correct.\r\n"));
            usart_Text(SERIAL, PSTR("Please attempt to fix the issue and re-run this test.\r\n"));
            usart_Write(SERIAL, 2);
            return ERROR;
        }
    }
    
    sst_Chip_Erase();
    sst_Read((char*)&addr, (char*)&dat, 4);
    
    if (dat != 0xFFFF)
    {
        usart_Text(SERIAL, PSTR("Flash test failed.\r\n"));
        
        usart_Text(SERIAL, PSTR("Expected:"));
        usart_Print_Num(SERIAL, 0xFFFF);
        usart_Text(SERIAL, PSTR("\r\n"));
        
        usart_Text(SERIAL, PSTR("Got     :"));
        usart_Print_Num(SERIAL, dat);
        usart_Text(SERIAL, PSTR("\r\n"));
        
        if (failed++ < 3)
        {
            usart_Text(SERIAL, PSTR("Attempting to retry... \r\n"));
            goto retry_flash;
        }
        else
        {
            usart_Text(SERIAL, PSTR("Verify Chip at U3 is correct.\r\n"));
            usart_Text(SERIAL, PSTR("Please attempt to fix the issue and re-run this test.\r\n"));
            
            usart_Write(SERIAL, 2);
            return ERROR;
        }
    }
    
    usart_Text(SERIAL, PSTR("Flash test passed.\r\n\r\n"));
    
    /////////////////////////////////BTA Tests/////////////////////////////////////
    
    usart_Text(SERIAL, PSTR("Testing BTAs...\r\n"));
    setPinMode(MODE_INPUT, A, 3);
    setPinMode(MODE_INPUT, A, 4);
    
    setPinMode(MODE_HIGH, C, 5);
    setPinMode(MODE_HIGH, C, 4);
    
    if (adc_Read(3) < 30) 
    {
        usart_Text(SERIAL, PSTR("BTA 1, Pullup Off Passed.\r\n"));
    }
    else
    {
        usart_Text(SERIAL, PSTR("BTA 1, Pullup Off Failed!\r\n"));
        usart_Text(SERIAL, PSTR("Verify MOSFETs at Q1 and Q5 are correct.\r\n"));
        usart_Text(SERIAL, PSTR("Please attempt to fix the issue and re-run this test.\r\n"));
        return ERROR;
    }
    if (adc_Read(4) < 30) 
    {
        usart_Text(SERIAL, PSTR("BTA 2, Pullup Off Passed.\r\n"));
    }
    else
    {
        usart_Text(SERIAL, PSTR("BTA 2, Pullup Off Failed!\r\n"));
        usart_Text(SERIAL, PSTR("Verify MOSFETs at Q3 and Q6 are correct.\r\n"));
        usart_Text(SERIAL, PSTR("Please attempt to fix the issue and re-run this test.\r\n"));
        
        usart_Write(SERIAL, 2);
        return ERROR;
    }
    
    setPinMode(MODE_LOW, C, 5);
    setPinMode(MODE_LOW, C, 4);
    
    if (abs(adc_Read(3) - 768) < 50) 
    {
        usart_Text(SERIAL, PSTR("BTA 1, Pullup On Passed.\r\n"));
    }
    else
    {
        usart_Text(SERIAL, PSTR("BTA 1, Pullup On Failed!\r\n"));
        usart_Text(SERIAL, PSTR("Verify MOSFETs at Q1 and Q5 are correct.\r\n"));
        usart_Text(SERIAL, PSTR("Please attempt to fix the issue and re-run this test.\r\n"));
        
        usart_Write(SERIAL, 2);
        return ERROR;
    }
    if (abs(adc_Read(4) - 768) < 50) 
    {
        usart_Text(SERIAL, PSTR("BTA 2, Pullup On Passed.\r\n"));
    }
    else
    {
        usart_Text(SERIAL, PSTR("BTA 2, Pullup On Failed!\r\n"));
        usart_Text(SERIAL, PSTR("Verify MOSFETs at Q3 and Q6 are correct.\r\n"));
        usart_Text(SERIAL, PSTR("Please attempt to fix the issue and re-run this test.\r\n"));
        
        usart_Write(SERIAL, 2);
        return ERROR;
    }
    //////////////////////////////////////////////////////////////////////////////////


    usart_Text(SERIAL, PSTR("\r\nEntering Button Test Mode\r\n"));
    usart_Text(SERIAL, PSTR("Pressing buttons should turn on the corresponding LED. Please verify the colors.\r\n"));
    usart_Text(SERIAL, PSTR("Clear should be Red.\r\n"));
    usart_Text(SERIAL, PSTR("Stop should be Amber.\r\n"));
    usart_Text(SERIAL, PSTR("Start should be Green.\r\n"));
    
    usart_Write(SERIAL, 1);
    
    RunData r;
    
    while (1)
    {
        user_Set_LED(LED_STOP,  user_Get_Button(BUTTON_STOP));
        user_Set_LED(LED_CLEAR, user_Get_Button(BUTTON_CLEAR));
        user_Set_LED(LED_START, user_Get_Button(BUTTON_START));
        
        coms_Handle(&r);
    }

    return OK;
}
