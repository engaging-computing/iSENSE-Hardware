/**
 * @file data_interface.c
 *
 * Interface functions for storing recorded data.
 */

#include "data_interface.h"
#include "spi_master.h"
#include <avr/pgmspace.h>
#include <avr/eeprom.h>

static uint32_t curAddr; ///< Current address of recording header.

/**
 * Initializes spi and attempts to recover any old data header.
 *
 * @return Returns 'OK' if previous data was recovered, 'ERROR'
 * if no previous data was found or it was full. In both error
 * cases the previous data is deleted.
 */
status data_Init(void)
{
    spi_MasterInit();

    return data_Recover_Header();
}

/**
 * Resets values stored in EEPROM to thier default state.
 */
void data_Reset_EEPROM()
{
    data_Write_EEPROM(DATA_GLOBAL_RATE_ADDR_H, DATA_DEFAULT_GLOBAL_RATE_H);
    data_Write_EEPROM(DATA_GLOBAL_RATE_ADDR_L, DATA_DEFAULT_GLOBAL_RATE_L);

    data_Write_EEPROM(DATA_ACCEL_RATE_ADDR_H, DATA_DEFAULT_SENSOR_RATE_H);
    data_Write_EEPROM(DATA_ACCEL_RATE_ADDR_L, DATA_DEFAULT_SENSOR_RATE_L);

    data_Write_EEPROM(DATA_LIGHT_RATE_ADDR_H, DATA_DEFAULT_SENSOR_RATE_H);
    data_Write_EEPROM(DATA_LIGHT_RATE_ADDR_L, DATA_DEFAULT_SENSOR_RATE_L);

    data_Write_EEPROM(DATA_BARO_RATE_ADDR_H, DATA_DEFAULT_SENSOR_RATE_H);
    data_Write_EEPROM(DATA_BARO_RATE_ADDR_L, DATA_DEFAULT_SENSOR_RATE_L);

    data_Write_EEPROM(DATA_HUM_RATE_ADDR_H, DATA_DEFAULT_SENSOR_RATE_H);
    data_Write_EEPROM(DATA_HUM_RATE_ADDR_L, DATA_DEFAULT_SENSOR_RATE_L);

    data_Write_EEPROM(DATA_BTA_1_RATE_ADDR_H, DATA_DEFAULT_SENSOR_RATE_H);
    data_Write_EEPROM(DATA_BTA_1_RATE_ADDR_L, DATA_DEFAULT_SENSOR_RATE_L);
    data_Write_EEPROM(DATA_BTA_1_TYPE_ADDR, DATA_DEFAULT_EXT_TYPE);

    data_Write_EEPROM(DATA_BTA_2_RATE_ADDR_H, DATA_DEFAULT_SENSOR_RATE_H);
    data_Write_EEPROM(DATA_BTA_2_RATE_ADDR_L, DATA_DEFAULT_SENSOR_RATE_L);
    data_Write_EEPROM(DATA_BTA_2_TYPE_ADDR, DATA_DEFAULT_EXT_TYPE);

    data_Write_EEPROM(DATA_MINI_1_RATE_ADDR_H, DATA_DEFAULT_SENSOR_RATE_H);
    data_Write_EEPROM(DATA_MINI_1_RATE_ADDR_L, DATA_DEFAULT_SENSOR_RATE_L);
    data_Write_EEPROM(DATA_MINI_1_TYPE_ADDR, DATA_DEFAULT_EXT_TYPE);

    data_Write_EEPROM(DATA_MINI_2_RATE_ADDR_H, DATA_DEFAULT_SENSOR_RATE_H);
    data_Write_EEPROM(DATA_MINI_2_RATE_ADDR_L, DATA_DEFAULT_SENSOR_RATE_L);
    data_Write_EEPROM(DATA_MINI_2_TYPE_ADDR, DATA_DEFAULT_EXT_TYPE);

    data_Write_EEPROM(DATA_GPS_THRESHOLD_ADDR, DATA_DEFAULT_GPS_THRESHOLD);
}

/**
 * Searches for the old data header from the last session.
 *
 * @return Returns 'OK' if an old data head is recovered,
 * otherwise 'ERROR' is returned and the data header is
 * initalized to 0:0.
 */
status data_Recover_Header(void)
{
    static DataPoint endOfData = {-1, 65535, -1, 65535, 65535, -1, -1};
    int i;

    for (i = 0; i < 16; i++)
    {
        endOfData.bitpack.rawData[i] = 255;
    }

    curAddr = 0;

    DataPoint tmp;
    uint32_t max = (MAX_MEM_ADDR / sizeof(DataPoint)) - 1;
    uint32_t min = 0;
    uint32_t avg;

    while (max != min)
    {
        avg = ((max + min) / 2) * sizeof(DataPoint);

        sst_Read((char*)(&avg), (char*)&tmp, sizeof(DataPoint));

        if (data_Compare(&tmp, &endOfData))
        {
            max = ((max + min) / 2) - 1;

            if (max < min) max = min;
        }
        else
        {
            min = ((max + min) / 2) + 1;

            if (min > max) min = max;
        }
    }

    curAddr = min * sizeof(DataPoint);

    if (min == (MAX_MEM_ADDR / sizeof(DataPoint)) - 1)
    {
        return ERROR;
    }

    return OK;
}

/**
 * Writes the given DataPoint to memory. If the current page does not
 * contain enough empty space, the next page is written to instead.
 *
 * @return Returns 'OK' if the operation is successful, if there
 * is no room left to store data then 'ERROR' is returned and
 * no operations are performed.
 */
status data_Write(DataPoint *data)
{
    if (curAddr > (MAX_MEM_ADDR / sizeof(DataPoint)) - 1) return ERROR;

    status ret = sst_AAI_Write((char*)&curAddr, (char*)data, sizeof(DataPoint));

    curAddr += sizeof(DataPoint);

    return ret;
}

/**
 * Erases the memory.
 */
void data_Clear(void)
{
    sst_Chip_Erase();

    curAddr = 0;
}

/**
 * Waits for previous writes to complete then writes the given
 * data byte to the given address in EEPROM.
 *
 * @param addr Address to write to in EEPROM.
 * @param data Data to write in EEPROM.
 */
void data_Write_EEPROM(unsigned int addr, unsigned char data)
{
    eeprom_busy_wait();

    eeprom_write_byte((unsigned char*)addr, data);
}

/**
 * Waits for previous writes to complete then reads the given
 * address in EEPROM.
 *
 * @param addr Address in EEPROM to read.
 *
 * @return Returns the value at the requested EEPROM address.
 */
unsigned char data_Read_EEPROM(unsigned int addr)
{
    eeprom_busy_wait();

    return eeprom_read_byte((unsigned char*)addr);
}



/**
 * Compares two DataPoint (for use in searching).
 *
 * @param a Datapoint to compare.
 * @param b Datapoint to compare.
 *
 * @return Returns true if the DataPoints are the same, false otherwise.
 */
bool data_Compare(DataPoint *a, DataPoint *b)
{
    if (a->latLow  != b->latLow)  return false;
    if (a->latHigh != b->latHigh) return false;
    if (a->lonLow  != b->lonLow)  return false;
    if (a->lonHigh != b->lonHigh) return false;

    if (a->altitude    != b->altitude)    return false;
    if (a->pressure    != b->pressure)    return false;
    if (a->temperature != b->temperature) return false;

    int i;

    for (i = 0; i < 16; i++)
    {
        if (a->bitpack.rawData[i] != b->bitpack.rawData[i]) return false;
    }

    return true;
}

/**
 * Returns the current header address for DataPoint storage.
 *
 * @return The address of the data header.
 */
uint32_t data_Cur_Addr()
{
    return curAddr;
}
