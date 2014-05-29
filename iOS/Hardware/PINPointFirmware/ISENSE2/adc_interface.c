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
 * @file adc_interface.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
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
