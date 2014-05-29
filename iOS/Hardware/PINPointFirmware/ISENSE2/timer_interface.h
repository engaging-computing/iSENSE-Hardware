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
 * @file timer_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for reading the internal timer.
 */

#include "globals.h"

#define MILISECONDS_PER_SECOND  1000  ///< Number of 'miliseconds' (as defined below) in a full second.
#define CLOCKS_PER_MILISECOND   9     ///< Number of timer cycles per 'milisecond'.
#define MAX_IDLE_CLOCKS         90    ///< Max number of clocks per idle.
#define IDLE_LEAD_TIME          5    ///< Number of ms to wake up before the next event at minimum.

#define timer_Job_Ready2(x, b) timer_Job_Ready4(&(x.timer),x.trigger,&(x.last),b) ///< Macro for using the timer_Job_Ready function with the JobTimer struct.

/**
 * @struct JobTimer Provides a packaged set of integers
 * for holding time data realted to a timer_Job_Ready call.
 */
typedef struct
{
    uint16_t timer;   ///< How many miliseconds have passed.
    uint16_t trigger; ///< How many miliseconds between job activations.
    uint16_t last;    ///< What the clock read during the last update.
} JobTimer;

extern void timer_Init(void);
extern void timer_Sleep(void);
extern void timer_Wake(void);
extern unsigned int timer_Clock_Read_S(void);
extern unsigned int timer_Clock_Read_MS(void);
extern void timer_Clock_Reset(void);
extern void timer_Wait_MS(unsigned int ms);
extern void timer_Wait_US(unsigned char us);
extern bool timer_Job_Ready4(unsigned int *timer, unsigned int trigger, unsigned int *last, bool fullReset);