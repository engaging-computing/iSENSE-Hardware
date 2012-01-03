/**
 * @file adc_interface.h
 *
 * Interface functions for performing analog-digital conversions.
 */

#ifndef _ADC_INTERFACE_H_
#define _ADC_INTERFACE_H_

#include "globals.h"

/**
 * @enum adcRef Describes the voltage reference used for ADC.
 */
typedef enum
{
    ADC_EXTERNAL,     ///< External ADCREF pin.
    ADC_INTERNAL_11V, ///< Internal 1.1V reference.
    ADC_INTERNAL_256V ///< Internal 2.56V reference.
} adcRef;

/**
 * @enum adcRes Describes the resolution used for ADC.
 */
typedef enum
{
    ADC_8BIT_RES, ///< 8-bit ADC results.
    ADC_10BIT_RES ///< 10-bit ADC results.
} adcRes;

///Mask for resetting the ADMUX register.
#define ADC_RESET 	 0xE0

///Mask for initializing the ADCSRA register.
#define ADC_ENABLE 	 0x80

extern void adc_Init(adcRef ref, adcRes res);
extern unsigned int adc_Read(char channel);
extern void adc_Control(char state);

#endif
