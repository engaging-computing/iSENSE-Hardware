/**
 * @file rtc_interface.h
 *
 * Interface functions for the onboard real-time clock.
 */

#include "globals.h"
#include "usart_interface.h"
#include "twi_interface.h"
#include "sensor_interface.h"

#define RTC_ADDR      0xD0 ///< TWI address of the real-time clock.
#define RTC_TIME_ADDR 0x00 ///< Address of the time register.
#define RTC_CTRL_ADDR 0x07 ///< Address of the control registers.

#define RTC_CTRL_1 0x80 ///< Config value for the first control register.
#define RTC_CTRL_2 0x00 ///< Config value for the second control register.
#define RTC_CTRL_3 0x00 ///< Config value for the third control register.

/**
 * @struct Time Stores a calandar time.
 */
typedef struct
{
    char seconds; ///< Seconds.
    char minutes; ///< Minutes.
    char hours;   ///< Hours (24 hour format).
    char dow;     ///< Day of the week (Sunday is 1 by convention).
    char date;    ///< Date of the month.
    char month;   ///< Month (January is 1).
    char year;    ///< Last two digits of the year (assumed 20XY for the time being).
} Time;

extern void rtc_Init(void);
extern void rtc_Read_Time(DataPoint *data);
extern void rtc_Set_Time(Time *t);

