/**
 * @file timer_interface.c
 *
 * Interface functions for reading the internal timer.
 */

#include "usart_interface.h"

#include "timer_interface.h"
#include <util/delay_basic.h>
#include <avr/sleep.h>

volatile unsigned int clockSecondsPassed = 0, clockMilisecondsPassed = 0;
int clockSleepCount = 0;
unsigned int timerNextEvent = MAX_IDLE_CLOCKS / CLOCKS_PER_MILISECOND + CLOCKS_PER_MILISECOND * IDLE_LEAD_TIME;
unsigned int timerNextEventTime = MAX_IDLE_CLOCKS / CLOCKS_PER_MILISECOND + CLOCKS_PER_MILISECOND * IDLE_LEAD_TIME;
unsigned char timerSleep = 0;

/**
 * Increments the clock.
 */
ISR(TIMER1_COMPA_vect)
{
    clockMilisecondsPassed += 1;
    TCNT1 = 1;
    clockSecondsPassed += clockMilisecondsPassed / MILISECONDS_PER_SECOND;
    clockMilisecondsPassed %= MILISECONDS_PER_SECOND;
    
    if (clockSleepCount > 0)
    {
        if (--clockSleepCount == 0)
        {
            timerSleep = 0;
        }
    }
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

    set_bit(TCCR1B, CS12);
    clear_bit(TCCR1B, CS11);
    set_bit(TCCR1B, CS10);

    set_bit(TIMSK1, OCIE1A);

    OCR1A = CLOCKS_PER_MILISECOND;
}

/**
 * Uses data collected by timer_job_ready to enter idle mode for some time.
 * 
 * Time spent idling is bounded by *MAX_IDLE_CLOCKS / CLOCKS_PER_MILISECOND*
 * and by 10 ms before the next event as collected by timer_job_ready. If
 * There is an event within the next IDLE_LEAD_TIME ms then no time is
 * spent idling.
 */
void timer_Sleep(void)
{
    clockSleepCount = timerNextEvent * CLOCKS_PER_MILISECOND - CLOCKS_PER_MILISECOND * IDLE_LEAD_TIME;
    clockSleepCount = clockSleepCount < MAX_IDLE_CLOCKS ? clockSleepCount : MAX_IDLE_CLOCKS;
    clockSleepCount = clockSleepCount > 0 ? clockSleepCount : 0;
    
    cli();
    
    if (clockSleepCount > 0)
    {
        timerSleep = 1;
        
        set_sleep_mode(SLEEP_MODE_IDLE);
        sleep_enable();
        sei();
        
        while (timerSleep > 0)
        {
            timer_Wait_US(1);
            sleep_cpu();
        }
        
        sleep_disable();
    }
    
    timerNextEvent = MAX_IDLE_CLOCKS / CLOCKS_PER_MILISECOND + CLOCKS_PER_MILISECOND * IDLE_LEAD_TIME;
    
    sei();
}

/**
 * Signals that idle mode should end if it is enabled.
 */
void timer_Wake(void)
{
    timerSleep = 0;
}

/**
 * @return Returns the number of seconds passed since the last clock reset.
 */
unsigned int timer_Clock_Read_S(void)
{
    return clockSecondsPassed;
}

/**
 * @return Returns the number of miliseconds passed since the last whole second.
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
#define MAX_MILIS_PER_LOOP (65355 / (FOSC / 4000))

    while (ms > 0)
    {
        _delay_loop_2((ms < MAX_MILIS_PER_LOOP ? ms : MAX_MILIS_PER_LOOP) * LOOPS_PER_MILI);
        ms -= ms < MAX_MILIS_PER_LOOP ? ms : MAX_MILIS_PER_LOOP;
    }
}

/**
 * Halts operation for the given number of microseconds. Interrupts
 * will still trigger.
 *
 * @param ms Number of microseconds to wait.
 */
void timer_Wait_US(unsigned char us)
{
#define LOOPS_PER_MICRO     (FOSC / 3000000L)
#define MAX_MICROS_PER_LOOP (225 / (FOSC / 3000000L))

    while (us > 0)
    {
        _delay_loop_1((us < MAX_MICROS_PER_LOOP ? us : MAX_MICROS_PER_LOOP) * LOOPS_PER_MICRO);
        us -= us < MAX_MICROS_PER_LOOP ? us : MAX_MICROS_PER_LOOP;
    }
}

/**
 * Increments the 'timer' by the miliseconds passed since 'last'.
 * This must be called at more than 1Hz or the timer will not be
 * accurate. If the timer reachs the value 'trigger' then the
 * 'timer' is reset to 'timer - trigger' if 'fullReset' is false,
 * it is set to otherwise 0.
 * 
 * Also keeps track of the next most recent event using timerNextEvent
 * and timerNextEventTime.
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

    //Update timer
    if (time >= *last) 
    {
        *timer += time - *last;
    }
    else 
    {
        *timer += time + MILISECONDS_PER_SECOND - *last;
    }
    *last = time;

    //Update idle timer
    if (time >= timerNextEventTime) 
    {
        timerNextEvent -= min(time - timerNextEvent, timerNextEvent);
    }
    else 
    {
        timerNextEvent -= min(time + MILISECONDS_PER_SECOND - timerNextEventTime, timerNextEvent);
    }
    timerNextEventTime = time;
    
    //Trigger?
    if (*timer >= trigger)
    {
        *timer = fullReset ? 0 : *timer - trigger;
        
        timerNextEvent = min(timerNextEvent, trigger - *timer);
        
        return true;
    }
    
    timerNextEvent = min(timerNextEvent, trigger - *timer);

    return false;
}
