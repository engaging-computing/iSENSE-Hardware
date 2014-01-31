/**
 * @file gps_interface.c
 *
 * Interface functions for communicating with the GPS.
 */

//#define GPS_DEBUG

#include "gps_interface.h"
#include "usart_interface.h"
#include "data_interface.h"
#include "timer_interface.h"
#include <string.h>
#include <stdlib.h>
#include <avr/pgmspace.h>
#include <avr/interrupt.h>

static char gpsData[GPS_BUFFER_SIZE];
static char goodData[GPS_STRING_SIZE];
static int gpsSize = 0;

static bool valid = false;
static unsigned char satNum = 0;
static signed int latHigh = 20000, lonHigh = 20000;
static unsigned int latLow = 0, lonLow = 0;
static unsigned int altitude = 60000;
static unsigned char gpsThreshold = 0;

/**
 * Interrupt to receive new data over the gps line.The data is
 * placed in the 'gpsData' buffer and the count is incrmented. If
 * the buffer reaches an overflow condition then new characters
 * are placed at the end of the buffer.
 */
SIGNAL(USART1_RX_vect)
{
    if (gpsSize < GPS_BUFFER_SIZE)
    {
        gpsData[gpsSize++] = UDR1;
    }
    else
    {
        gpsData[gpsSize - 1] = UDR1;
    }
    
    timer_Wake();
}

/**
 * Initializes the GPS communications by intializing
 * the apropriate uart connection and enabling interrupts.
 */
void gps_Init(void)
{
    usart_Init(GPS, GPS_BAUD_INIT);

    timer_Wait_MS(1000);
    gps_Send_Msg(GPS_RATE_MSG);
    timer_Wait_MS(100);

    gpsThreshold = data_Read_EEPROM(DATA_GPS_THRESHOLD_ADDR);

    usart_Interrupt_RX(GPS, ENABLE);

    gps_Clear();
}

/**
 * Sends a message to the GPS itself.
 *
 * @param msg The message to send.
 */
void gps_Send_Msg(const char *msg)
{
    usart_Text(GPS, msg);
}

/**
 * Polls the GPS communications buffer for new data.
 */
void gps_Poll()
{
    cli();
    bool newString = gps_Clear();
    sei();

    if (newString)
    {
        valid = gps_Parse();
    }
    else
    {
#ifdef GPS_DEBUG
        usart_Text(SERIAL, PSTR("Bad at GPS_CLR, SIZE : "));
        usart_Digits(SERIAL, gpsSize, BASE_10, UNSIGNED_ZEROS, 3);
        usart_Text(SERIAL, PSTR("\r\n"));

        if (gpsSize >= GPS_BUFFER_SIZE)
        {
            usart_Text(SERIAL, PSTR("*************OVERFLOW by : "));
            usart_Digits(SERIAL, gpsSize - GPS_BUFFER_SIZE, BASE_10, UNSIGNED_ZEROS, 3);
            usart_Text(SERIAL, PSTR("\r\n"));
        }
#endif
        gpsSize = 0;
    }

    if (valid)
    {
#ifdef GPS_DEBUG
        usart_Text(SERIAL, PSTR("GOOD parse : "));
        usart_String(SERIAL, goodData);
        usart_Write(SERIAL, '\r');
        usart_Write(SERIAL, '\n');
        usart_Text(SERIAL, PSTR("RAW        : "));
        usart_String(SERIAL, gpsData);
#endif
    }
    else
    {
#ifdef GPS_DEBUG
        usart_Text(SERIAL, PSTR("BAD parse : "));
        usart_String(SERIAL, goodData);
        usart_Write(SERIAL, '\r');
        usart_Write(SERIAL, '\n');
        usart_Text(SERIAL, PSTR("RAW       : "));
        usart_String(SERIAL, gpsData);
#endif

        latHigh = 20000;
        latLow = 0;
        lonHigh = 20000;
        lonLow = 0;
        altitude = 60000;
        satNum = 0;
    }
}

/**
 * Pulls the last polled data into 'data'.
 *
 * @param data Address of DataPoint to load into.
 *
 * @return T/F last GPS fix was valid.
 */
extern bool gps_Pull(DataPoint* data)
{
    data->latHigh = latHigh;
    data->latLow = latLow;
    data->lonHigh = lonHigh;
    data->lonLow = lonLow;
    data->altitude = altitude;

    return valid;
}

/**
 * Clears any garbage out of the GPS communications buffer
 * and moves any valid GPS string into the 'goodData' buffer.
 *
 * @return Returns true if a new message was found, false if not.
 */
bool gps_Clear(void)
{
    int i, j, k;

    for (k = 0; k < gpsSize; k++)
    {
        if (gpsData[k] == ',')
        {
            for (i = k + 1; i < gpsSize; i++)
            {
                if (gpsData[i] == '\n' && i > 3 && gpsData[i - 4] == '*')
                {
                    for (j = 0; j <= i - 4; j++)
                    {
                        goodData[j] = gpsData[j + k - 1];
                    }
                    goodData[j] = '\0';
                    gpsSize = 0;

                    return true;
                }
            }
        }
    }

    //gpsSize = 0;

    return false;
}

/**
 * Interprets the goodData GPS string and extracts any valid data
 * from it. If the string is found to be incomplete or the required
 * number of satellites has not been connected, the default data is saved.
 * Otherwise updated data from the stirng is stored. Information about
 * the string and storage format can be found in inline comments.
 *
 * @return Returns true if a valid GPS message was parsed that contained
 * the required number of satellites.
 */
bool gps_Parse(void)
{
    int i = 0;
    int j = 0;
    int search = 0;
    char temp[6];
    static bool ret = false;

    temp[5] = '\0';

    if (goodData[0] == 'A')
    {
        /*
         * GPGGA Message format
         * MessageID,UTC,LAT,NS,LON,EW,FQ,SAT,HDoP,ALT,UNIT,HoG,UNIT,?,?
         */
        for (i = 0; i < GPS_STRING_SIZE - 1 && goodData[i] != '*';)
        {
            if (goodData[i] == ',')
            {
                search++;
                i++;

                if (search > 9) break;
            }

            switch ((ggaField)search)
            {
                    /*
                     * Extract latitude information.
                     * GPS format: ddmm.mmm
                     * Storage format: dddddddd dddddddd ffffffff ffffffff
                     * (d - degrees and minutes, f - fractional minutes)
                     */
                case GGA_LAT:
                {
                    if (goodData[i] == ',')
                    {
#ifdef GPS_DEBUG
                        usart_Text(SERIAL, PSTR("Bad at GGA_LAT\r\n"));
#endif
                        ret = false;
                        break;
                    }

                    for (j = 0; j < 4; j++)
                    {
                        temp[j] = goodData[i + j];
                    }
                    temp[j] = '\0';
                    i += j;
                    latHigh = atoi(temp);

                    i++;

                    for (j = 0; j < 3; j++)
                    {
                        temp[j] = goodData[i + j];
                    }
                    temp[j] = '\0';
                    i += j;
                    latLow = atoi(temp);

                    break;
                }

                /*
                 * Extract North/South indicator.
                 */
                case GGA_NS:
                {
                    if (goodData[i] == ',')
                    {
#ifdef GPS_DEBUG
                        usart_Text(SERIAL, PSTR("Bad at GGA_NS\r\n"));
#endif
                        ret = false;
                        break;
                    }

                    if (goodData[i++] == 'S') latHigh = -latHigh;

                    break;
                }

                /*
                 * Extract longitude information.
                 * GPS format: dddmm.mmm
                 * Storage format: dddddddd dddddddd ffffffff ffffffff
                 * (d - degrees and minutes, f - fractional minutes)
                 */
                case GGA_LON:
                {
                    if (goodData[i] == ',')
                    {
#ifdef GPS_DEBUG
                        usart_Text(SERIAL, PSTR("Bad at GGA_LON\r\n"));
#endif
                        ret = false;
                        break;
                    }

                    for (j = 0; j < 5; j++)
                    {
                        temp[j] = goodData[i + j];
                    }
                    temp[j] = '\0';
                    i += j;
                    lonHigh = atoi(temp);

                    i++;

                    for (j = 0; j < 3; j++)
                    {
                        temp[j] = goodData[i + j];
                    }
                    temp[j] = '\0';
                    i += j;
                    lonLow = atoi(temp);

                    break;
                }

                /*
                 * Extract East/West indicator.
                 */
                case GGA_EW:
                {
                    if (goodData[i] == ',')
                    {
#ifdef GPS_DEBUG
                        usart_Text(SERIAL, PSTR("Bad at GGA_EW\r\n"));
#endif
                        ret = false;
                        break;
                    }

                    if (goodData[i++] == 'W') lonHigh = -lonHigh;

                    break;
                }
                /*
                 * Extract number of tracked satelites.
                 * GPS format: NN
                 * Not stored, used to check validity.
                 */
                case GGA_SAT:
                {
                    if (goodData[i] == ',')
                    {
#ifdef GPS_DEBUG
                        usart_Text(SERIAL, PSTR("Bad at GGA_SAT\r\n"));
#endif
                        ret = false;
                        satNum = 0;
                        break;
                    }

                    for (j = 0; goodData[i + j] != ','; j++)
                    {
                        temp[j] = goodData[i + j];
                    }
                    temp[j] = '\0';
                    satNum = atoi(temp);
                    ret = satNum >= gpsThreshold;
#ifdef GPS_DEBUG
                    if (!ret) usart_Text(SERIAL, PSTR("Bad at GGA_SAT_CHK\r\n"));
#endif

                    break;
                }
                /*
                 * Extract the altitude above mean sea level.
                 * GPS format: varies, floating point.
                 * Storage format: unsigned integer of the whole portion.
                 */
                case GGA_ALT:
                {
                    if (goodData[i] == ',')
                    {
#ifdef GPS_DEBUG
                        usart_Text(SERIAL, PSTR("Bad at GGA_ALT\r\n"));
#endif
                        ret = false;
                        break;
                    }

                    for (j = 0; goodData[i + j] != '.'; j++)
                    {
                        temp[j] = goodData[i + j];
                    }
                    temp[j] = '\0';
                    altitude = atoi(temp);

                    break;
                }
                default:
                {
                    break;
                }
            }

            while (goodData[i] != ',' && goodData[i] != '*' && i < GPS_STRING_SIZE) i++;
        }
    }
    else
    {
#ifdef GPS_DEBUG
        usart_Text(SERIAL, PSTR("Bad at TYPE\r\n"));
#endif
        return false;
    }

    return ret;
}
