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
 * @file spi_master.h
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for comunicating with the AT45 flash memory over SPI.
 */

#ifndef _SPI_MASTER_H_
#define _SPI_MASTER_H_

#include "globals.h"

#define SPI_PRT  PORTB ///< SPI Port.
#define SPI_DDR  DDRB  ///< SPI DDR Register.
#define CS_PIN   4     ///< Chip select pin.
#define MOSI_PIN 5     ///< Master out slave in pin.
#define MISO_PIN 6     ///< Master in slave out pin.
#define SCK_PIN  7     ///< System clock pin.
#define HOLD_PIN 2     ///< ???

#define spi_End_Command() set_bit(SPI_PRT, CS_PIN) ///< Macro to end an SPI command.

#define spi_Start_Command() clear_bit(SPI_PRT, CS_PIN) ///< Macro to begin an SPI command.

#define STATUS_READY_BIT  0x01 ///< Status ready bit for the SST.
#define SST_STATUS        0x05 ///< SST command for requesting status byte.
#define SST_AAI_WRITE     0xAD ///< SST command for sequential write.
#define SST_WRITE_DISABLE 0x04 ///< SST Write-Disable command.
#define SST_WRITE_ENABLE  0x06 ///< SST Write-Enable command.
#define SST_READ          0x03 ///< SST Read command.
#define SST_CHIP_ERASE    0x60 ///< SST Chip erase command.

#define SST_STATUS_WRITE_ENABLE  0x50 ///< SST Status register write enable command.
#define SST_STATUS_WRITE         0x01 ///< SST Status register write command.

#define SST_WRITE_PROTECT_DISABLE_BITS 0x00 ///< SST Status bits for 0 write protection.

#define SST_UNUSED_BYTE         0x12 ///< Unused byte for use in SST command arguments.

#define MAX_MEM_ADDR 0x1FFFFFL ///< Maximum address on SST memory.

extern void spi_MasterInit(void);
extern unsigned char spi_Transfer (unsigned char data);
extern unsigned char sst_Status(void);
extern void sst_Finalize(void);
extern status sst_AAI_Write(char* addr, char* data, int size);
extern status sst_Read(char* addr, char* data, int size);
extern status sst_Read_To_Coms(char* addr, uint32_t size);
extern status sst_Write_Protect_Disable(void);
extern status sst_Chip_Erase(void);

#endif
