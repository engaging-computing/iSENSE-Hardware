/**
 * @file spi_master.h
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

#define MAX_MEM_ADDR 0x1FFFFF ///< Maximum address on SST memory.

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
