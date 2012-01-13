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
 * @file gps_interface.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for communicating with the GPS.
 */

#ifndef _GPS_INTERFACE_H_
#define _GPS_INTERFACE_H_

#include "globals.h"
#include "sensor_interface.h"

/**
 * @enum ggaField Indicates a specific field wthin a GGA message.
 */
typedef enum
{
    ///Latitude field.
    GGA_LAT = 2,
    ///North/South indicator field.
    GGA_NS = 3,
    ///Longitude field.
    GGA_LON = 4,
    ///East/West indicator field.
    GGA_EW = 5,
    ///Number of satellite locks field.
    GGA_SAT = 7,
    ///Altitude field.
    GGA_ALT = 9
} ggaField;

///38,400  (FOSC/(16L * baud)) - 1
#define GPS_BAUD_INIT       9 //4800  (FOSC/(16L * baud)) - 1
#define GPS_BAUD_RUNTIME    9 //19200  (FOSC/(16L * baud)) - 1

///Size of GPS message buffer.
#define GPS_BUFFER_SIZE 90
///maximum size of a single GPS string.
#define GPS_STRING_SIZE 80

///Broadcast rate message.
#define GPS_RATE_MSG PSTR("$PMTK314,0,0,0,5,0,0,0,0,0,0,0,0,0,0,0,0,0*2D\r\n")

extern void gps_Init(void);
extern void gps_Send_Msg(const char *msg);
extern void gps_Poll(void);
extern bool gps_Pull(DataPoint* data);
extern bool gps_Clear(void);
extern bool gps_Parse(void);

#endif
