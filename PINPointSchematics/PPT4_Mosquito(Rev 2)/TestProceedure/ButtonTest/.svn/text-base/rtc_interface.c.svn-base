/**
* @file rtc_interface.c
*
* Interface functions for the onboard real-time clock.
*/

#include "rtc_interface.h"

/**
 * Initializes the real-time clock.
 */
void rtc_Init(void)
{
    char out[4] = {RTC_CTRL_ADDR, RTC_CTRL_1, RTC_CTRL_2, RTC_CTRL_3};
    twi_Write(RTC_ADDR, out, 4);
}

/**
 * Reads the time from the real-time clock.
 *
 * @param data Address of the DataPoint which the time
 * is read into.
 */
void rtc_Read_Time(DataPoint *data)
{
    char in[7];
    char out = RTC_TIME_ADDR;

    twi_Write(RTC_ADDR, &out, 1);
    twi_Read(RTC_ADDR, in, 7);

    data->bitpack.data.seconds = (in[0] & 0x0F) + (10 * ((in[0] & 0x70) >> 4));
    data->bitpack.data.minutes = (in[1] & 0x0F) + (10 * ((in[1] & 0x70) >> 4));
    data->bitpack.data.hours   = (in[2] & 0x0F) + (10 * ((in[2] & 0x30) >> 4));
    data->bitpack.data.day     = (in[3] & 0x07);
    data->bitpack.data.date    = (in[4] & 0x0F) + (10 * ((in[4] & 0x30) >> 4));
    data->bitpack.data.month   = (in[5] & 0x0F) + (10 * ((in[5] & 0x10) >> 4));
    data->bitpack.data.year    = (in[6] & 0x0F) + (10 * ((in[6] & 0xF0) >> 4));
}

/**
 * Sets the time of the real-time clock.
 *
 * @param t The time value to set the clock to.
 */
void rtc_Set_Time(Time *t)
{
    char out[8];

    out[0] = RTC_TIME_ADDR;
    out[1] = (t->seconds % 10) + ((t->seconds / 10) << 4);
    out[2] = (t->minutes % 10) + ((t->minutes / 10) << 4);
    out[3] = (t->hours   % 10) + ((t->hours   / 10) << 4);
    out[4] =  t->dow;
    out[5] = (t->date    % 10) + ((t->date    / 10) << 4);
    out[6] = (t->month   % 10) + ((t->month   / 10) << 4);
    out[7] = (t->year    % 10) + ((t->year    / 10) << 4);

    twi_Write(RTC_ADDR, out, 8);
}
