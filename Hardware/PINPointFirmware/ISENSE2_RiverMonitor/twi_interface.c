/**
 * @file twi_interface.c
 *
 * Interface functions for TWI communication.
 */

#include "twi_interface.h"

/**
 * Initializes the TWI bus.
 *
 * @return Returns OK.
 */
status twi_Init(void)
{
    TWSR = TW_PRESCALE;

    TWBR = TW_BITRATE;

    clear_bit(DDRC, 1);
    clear_bit(PORTC, 1);
    clear_bit(DDRC, 0);
    clear_bit(PORTC, 0);

    //DDRC &= ~((1 << 0) | (1 << 1));
    //PORTC &= ~((1 << 0) | (1 << 1));

    return OK;
}

/**
 * Writes 'size' bytes from 'data' to 'slave' over TWI.
 *
 * @param slave TWI address of slave device.
 * @param data Address of data to write.
 * @param size Number of bytes to write.
 */
void twi_Write(char slave, char* data, int size)
{
    twi_Start();
    twi_Wait();

    twi_WriteByte(slave | TW_WRITE);
    twi_Nack();
    twi_Wait();

    int i = 0;

    for (i = 0; i < size; i++)
    {
        twi_WriteByte(data[i]);
        twi_Nack();
        twi_Wait();
    }

    twi_Stop();
}

/**
 * Reads 'size' bytes into 'data' from 'slave' over TWI.
 *
 * @param slave TWI address of slave device.
 * @param data The address to write data to.
 * @param size Number of bytes to read.
 */
void twi_Read(char slave, char* data, int size)
{
    twi_Start();
    twi_Wait();

    twi_WriteByte(slave | TW_READ);
    twi_Nack();
    twi_Wait();

    int i = 0;

    for (i = 0; i < size - 1; i++)
    {
        twi_Ack();
        twi_Wait();
        twi_ReadByte(data[i]);
    }

    twi_Nack();
    twi_Wait();
    twi_ReadByte(data[i]);

    twi_Stop();
}
