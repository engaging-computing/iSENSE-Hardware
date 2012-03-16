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
 * @file spi_master.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for comunicating with the AT45 flash memory over SPI.
 */


#include "spi_master.h"
#include "usart_interface.h"

/**
 * Initializes SPI pins and clears the SPI registers.
 */
void spi_MasterInit(void)
{
    unsigned char clr;

    spi_End_Command();
    /* Set MOSI, CS and SCK output, all others input */
    SPI_DDR = (1<<MOSI_PIN)|(1<<SCK_PIN)|(1<<CS_PIN);
    /* Enable SPI, Master, set clock rate fck/16 */
    SPCR = (1<<SPE) | (1<<MSTR) | (1<<SPR0) | (1 << CPOL) | (1 << CPHA);
    /* Clear registers */
    clr = SPSR;
    clr = SPDR;

    sst_Write_Protect_Disable();
}

/**
 * Transfers the one byte 'data' through SPI.
 *
 * @param data The byte to send.
 *
 * @return Returns the byte received.
 */
unsigned char spi_Transfer (unsigned char data)
{
    SPDR = data;
    while (!(SPSR & (1<<SPIF)));
    return SPDR;
}

/**
 * Requests the status byte from the sst.
 *
 * @return Returns the status byte.
 */
unsigned char sst_Status(void)
{
    unsigned char data;

    spi_Start_Command();
    spi_Transfer(SST_STATUS);
    data = spi_Transfer(SST_UNUSED_BYTE);
    spi_End_Command();

    return data;
}


/**
 * Waits for the sst to indicate it is ready to accept a
 * new command.
 */
void sst_Finalize(void)
{
    while ((sst_Status() & STATUS_READY_BIT));
}

/**
 * Writes 'size' bytes from 'data to the address '*addr'.
 *
 * @param addr Address of the address to write to.
 * @param data Address of the data to write.
 * @param size size of the data. Must be a multiple of two.
 *
 * @return Returns OK if succesfull, ERROR if given size was invalid.
 */
status sst_AAI_Write(char* addr, char* data, int size)
{
    //Ensure size is even
    if (size % 2 != 0)
    {
        return ERROR;
    }

    if (size <= 0)
    {
        return ERROR;
    }

    int i = 0;

    spi_Start_Command();
    spi_Transfer(SST_WRITE_ENABLE);
    spi_End_Command();

    spi_Start_Command();
    spi_Transfer(SST_AAI_WRITE);
    spi_Transfer(addr[2]);
    spi_Transfer(addr[1]);
    spi_Transfer(addr[0]);
    spi_Transfer(data[i++]);
    spi_Transfer(data[i++]);

    while (i < size)
    {
        spi_End_Command();
        sst_Finalize();
        spi_Start_Command();

        spi_Transfer(SST_AAI_WRITE);
        spi_Transfer(data[i++]);
        spi_Transfer(data[i++]);
    }

    spi_End_Command();
    sst_Finalize();

    spi_Start_Command();
    spi_Transfer(SST_WRITE_DISABLE);
    spi_End_Command();

    sst_Finalize();

    return OK;
}

/**
 * Reads 'size' bytes into 'data' from address '*addr'.
 *
 * @param addr Address of the address to read from.
 * @param data Address to read to.
 * @param size Number of bytes to read.
 *
 * @return Returns ERROR if an invalid size is given, otherwise OK.
 */
status sst_Read(char* addr, char* data, int size)
{
    if (size <= 0)
    {
        return ERROR;
    }

    int i = 0;

    spi_Start_Command();
    spi_Transfer(SST_READ);
    spi_Transfer(addr[2]);
    spi_Transfer(addr[1]);
    spi_Transfer(addr[0]);

    while (i < size)
    {
        data[i++] = spi_Transfer(SST_UNUSED_BYTE);
    }

    spi_End_Command();

    return OK;
}

/**
 * Reads 'size' bytes from '*addr' directly to USB.
 * Also calculates an 8-bit additive checksum.
 *
 * @param addr Address of address to read from.
 * @param size Number of bytes to read.
 * @param chksum Address to store additive 8-bit checksum at.
 *
 * @return Returns ERROR if an invalid size is given, otherwise OK.
 */
status sst_Read_To_Coms(char* addr, uint32_t size)
{
    if (size <= 0)
    {
        return ERROR;
    }

    uint32_t i = 0;
    char data;
    char chksum = 0;

    spi_Start_Command();
    spi_Transfer(SST_READ);
    spi_Transfer(addr[2]);
    spi_Transfer(addr[1]);
    spi_Transfer(addr[0]);

    while (i < size)
    {
        data = spi_Transfer(SST_UNUSED_BYTE);
        chksum = ((uint16_t)(chksum) + data) & 0xFF;
        usart_Write(SERIAL, data);
        i++;
    }

    spi_End_Command();

    usart_Write(SERIAL, chksum);

    return OK;
}

/**
 * Turns off write protect mode.
 *
 * @return Returns OK.
 */
status sst_Write_Protect_Disable(void)
{
    spi_Start_Command();
    spi_Transfer(SST_STATUS_WRITE_ENABLE);
    spi_End_Command();

    spi_Start_Command();
    spi_Transfer(SST_STATUS_WRITE);
    spi_Transfer(SST_WRITE_PROTECT_DISABLE_BITS);
    spi_End_Command();

    spi_Start_Command();
    spi_Transfer(SST_WRITE_DISABLE);
    spi_End_Command();

    return OK;
}

/**
 * Erases the entire memory.
 *
 * @return Returns OK.
 */
status sst_Chip_Erase(void)
{
    spi_Start_Command();
    spi_Transfer(SST_WRITE_ENABLE);
    spi_End_Command();

    spi_Start_Command();
    spi_Transfer(SST_CHIP_ERASE);
    spi_End_Command();

    spi_Start_Command();
    spi_Transfer(SST_WRITE_DISABLE);
    spi_End_Command();

    sst_Finalize();

    return OK;
}