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
* @file rtc_interface.c
* @author Michael McGuinness <mmcguinn@cs.uml.edu>
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
