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
 * @file adc_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
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
