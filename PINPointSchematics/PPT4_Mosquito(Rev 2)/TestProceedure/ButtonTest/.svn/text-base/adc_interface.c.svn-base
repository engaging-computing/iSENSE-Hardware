/**
 * @file adc_interface.c
 *
 * Interface functions for performing analog-digital conversions.
 */

#include "adc_interface.h"

static adcRes resolution; ///< Holds the current ADC resolution.

/**
 * Initializes ADC using the given voltage reference with
 * the given resolution (8 or 10 bit).
 *
 * @param ref The reference voltage source.
 * @param res The ADC conversion resolution.
 */
void adc_Init(adcRef ref, adcRes res)
{
    adc_Control(OFF);

    switch (ref)
    {
        case ADC_EXTERNAL:
            set_bit(ADMUX, REFS0);
            clear_bit(ADMUX, REFS1);
            break;
        case ADC_INTERNAL_11V:
            clear_bit(ADMUX, REFS0);
            set_bit(ADMUX, REFS1);
            break;
        case ADC_INTERNAL_256V:
            set_bit(ADMUX, REFS0);
            set_bit(ADMUX, REFS1);
            break;
    }
    set_bit(ADMUX, ADLAR);

    set_bit(ADCSRA, ADPS0);
    set_bit(ADCSRA, ADPS1);
    set_bit(ADCSRA, ADPS2);

    resolution = res;

    adc_Control(ON);
}

/**
 * Resets ADC selection settings then reads the given channel.
 *
 * @param channel The ADC channel to read (0-7).
 *
 * @return Returns the ADC value of the requested channel.
 */
unsigned int adc_Read(char channel)
{
    unsigned int low, high;

    ADMUX &= ADC_RESET;
    ADMUX |= channel;
    set_bit(ADCSRA, ADSC);

    while (test_bit(ADCSRA, ADSC));

    low = ADCL;
    high = ADCH;

    if (resolution == ADC_8BIT_RES)
    {
        return high;
    }
    else
    {
        return (high << 2) + (low >> 6);
    }
}

/**
 * Sets the ADC to the given state.
 *
 * @param state State to set the ADC to (ON or OFF).
 */
void adc_Control(char state)
{
    if (state == ON) set_bit(ADCSRA, ADEN);
    else clear_bit(ADCSRA, ADEN);
}
