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
 * @file rtc_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
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

