/**
 * @file timer_interface.h
 *
 * Interface functions for reading the internal timer.
 */

#include "globals.h"

#define MILISECONDS_PER_SECOND  1000 ///< Number of 'miliseconds' (as defined below) in a full second.
#define CLOCKS_PER_MILISECOND   144  ///< Number of timer cycles per 'milisecond'.

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
extern unsigned int timer_Clock_Read_S(void);
extern unsigned int timer_Clock_Read_MS(void);
extern void timer_Clock_Reset(void);
extern void timer_Wait_MS(unsigned int ms);
extern bool timer_Job_Ready4(unsigned int *timer, unsigned int trigger, unsigned int *last, bool fullReset);