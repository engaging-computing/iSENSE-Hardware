/**
 * @file gps_interface.h
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
