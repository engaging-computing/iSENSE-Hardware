/**
 * @file timer_interface.c
 *
 * Interface functions for reading the internal timer.
 */

#include "timer_interface.h"
#include <util/delay_basic.h>

volatile unsigned int clockSecondsPassed = 0, clockMilisecondsPassed = 0;

/**
 * Increments the clock.
 */
ISR(TIMER1_COMPA_vect)
{
    clockMilisecondsPassed += 1;
    TCNT1 = 1;
    clockSecondsPassed += clockMilisecondsPassed / MILISECONDS_PER_SECOND;
    clockMilisecondsPassed %= MILISECONDS_PER_SECOND;
}

/**
 * Initialzes timer 1 to run off the CPU clock with a prescaler of 1024.
 */
void timer_Init(void)
{
    clear_bit(PRR, PRTIM1);

    clear_bit(TCCR1A, WGM10);
    clear_bit(TCCR1A, WGM11);
    set_bit(TCCR1B, WGM12);
    clear_bit(TCCR1B, WGM13);

    clear_bit(TCCR1B, CS12);
    set_bit(TCCR1B, CS11);
    set_bit(TCCR1B, CS10);

    set_bit(TIMSK1, OCIE1A);

    OCR1A = CLOCKS_PER_MILISECOND;
}

/**
 * @return Returns the number of seconds passed since the last clock reset.
 */
unsigned int timer_Clock_Read_S(void)
{
    return clockSecondsPassed;
}

/**
 * @return Returns the number of miliseconds passed since the last clock reset.
 */
unsigned int timer_Clock_Read_MS(void)
{
    return clockMilisecondsPassed;
}

/**
 * Resets the clock.
 */
void timer_Clock_Reset(void)
{
    cli();
    TCNT1 = 0;
    sei();
    clockSecondsPassed = 0;
    clockMilisecondsPassed = 0;
}

/**
 * Halts operation for the given number of miliseconds. Interrupts
 * will still trigger.
 *
 * @param ms Number of miliseconds to wait.
 */
void timer_Wait_MS(unsigned int ms)
{
    #define FOSC               9216000L
    #define LOOPS_PER_MILI     (FOSC / 4000)
    #define MAX_MILIS_PER_LOOP 28
    
    while (ms > 0)
    {
        _delay_loop_2((ms < 28 ? ms : 28) * LOOPS_PER_MILI);
        ms -= ms < 28 ? ms : 28;
    }
}

/**
 * Increments the 'timer' by the miliseconds passed since 'last'.
 * This must be called at more than 1Hz or the timer will not be
 * accurate. If the timer reachs the value 'trigger' then the
 * 'timer' is reset to 'timer - trigger' if 'fullReset' is false,
 * it is set to otherwise 0.clockMilisecondsPassed
 *
 * @param timer Address of the miliseconds passed so far in the timer.
 * @param trigger The number of miliseconds between each job trigger.
 * @param last Address of the last milisecond clock value this timer was
 * checked.
 * @param fullReset Should the timer be reset to 0?
 *
 * @return Returns true if the 'timer' has reached 'trigger'.
 */
bool timer_Job_Ready4(unsigned int *timer, unsigned int trigger, unsigned int *last, bool fullReset)
{
    unsigned int time = clockMilisecondsPassed;

    if (time >= *last) *timer += time - *last;
    else *timer += time + MILISECONDS_PER_SECOND - *last;
    *last = time;

    if (*timer >= trigger)
    {
        *timer = fullReset ? 0 : *timer - trigger;
        return true;
    }

    return false;
}
