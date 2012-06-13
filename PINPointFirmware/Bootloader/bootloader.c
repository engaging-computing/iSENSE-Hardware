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
 * @file bootloader.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 * 
 * Primary source file for the PINPoint bootloader.
 */

#include "usart_interface.h"
#include <inttypes.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include <avr/eeprom.h>
#include <avr/wdt.h>
#include <avr/boot.h>

#define BOOT_EEPROM_ADDR 1019
#define BOOT_EEPROM_ON   0xFF 
#define BOOT_EEPROM_OFF  0x00

#define ring_Inc(x) x=(x+1)%(SPM_PAGESIZE*2)

uint8_t pageEnd = 0, pageSize = 0, pageStart = 0;
uint8_t pageBuf[SPM_PAGESIZE * 2];
uint16_t curPage = 0;

int     main(void);
void    boot_Program_Page(void);
uint8_t boot_Hex_To_Bin(uint8_t in);
int     boot_Read_Page(void);
void    (*main_App)(void) = 0;

//Verify watchdog is off
uint8_t mcusr_mirror __attribute__ ((section (".noinit")));
void get_mcusr(void) \
__attribute__((naked)) \
__attribute__((section(".init3")));
void get_mcusr(void)
{
    mcusr_mirror = MCUSR;
    MCUSR = 0;
    wdt_disable();
}

int main(void)
{
    cli();
    
    usart_Init();
    
    eeprom_busy_wait();
    uint8_t bl = eeprom_read_byte((unsigned char*)BOOT_EEPROM_ADDR);
    
    if (bl == BOOT_EEPROM_ON)
    {
        bootloader:
        usart_Write('B');
        usart_Write('L');
        char in = usart_Read();
        
        if (in == 'Q')
        {
            goto exit;
        }
        else if (in != 'P')
        {
            goto bootloader;
        }
        
        int ret = 0;
        
        while (ret == 0)
        {
            ret = boot_Read_Page();
            
            if (ret > 1)
            {
                break;
            }
            
            boot_Program_Page();
        }
        
        if (ret != 1)
        {
            usart_Write('E');
            usart_Write(ret & 0xFF);
            
            goto bootloader;
        }
        
        eeprom_busy_wait();
        eeprom_write_byte((unsigned char*)BOOT_EEPROM_ADDR, BOOT_EEPROM_OFF);
    }
    
    exit:
    
    sei();
    
    main_App();
    
    return 0;
}

/**
 * Programs a page of device memory using the page buffer.
 * 
 * Adapted from the AVR libc tutorial here,
 * http://www.nongnu.org/avr-libc/user-manual/group__avr__boot.html
 */
void boot_Program_Page()
{
    uint16_t i;
    
    eeprom_busy_wait ();
    
    boot_page_erase(curPage);
    boot_spm_busy_wait();      // Wait until the memory is erased.
    
    for (i=0; i<SPM_PAGESIZE; i+=2)
    {
        // Set up little-endian word.
        
        uint16_t w = pageBuf[pageStart];
        ring_Inc(pageStart);
        
        w += pageBuf[pageStart] << 8;
        ring_Inc(pageStart);
        
        boot_page_fill(curPage + i, w);
    }
    
    boot_page_write (curPage);     // Store buffer in flash page.
    
    curPage  += SPM_PAGESIZE;
    pageSize -= SPM_PAGESIZE;
    
    boot_spm_busy_wait();       // Wait until the memory is written.
    boot_rww_enable(); 
}

/**
 * Converts ASCII hex to a number.
 * 
 * @param in The ASCII.
 * 
 * @return The number.
 */
uint8_t boot_Hex_To_Bin(uint8_t in)
{
    if (in < 'A')
    {
        return in - '0';
    }
    else
    {
        return in - ('A' - 10);
    }
}

/**
 * Reads in a page of intel hex format binary.
 * 
 * Adapted from a project by Preston K. Manwaring (manjagu@byu.edu) 
 * hosted on avrfreaks.net.
 * 
 * @return Returns 0 if a line was succesfully read, and more follow,
 *                 1 if the hex file has ended,
 *                 2 if the hex line did not start with a ':'
 *                 3 if the checksum failed,
 */
int boot_Read_Page()
{
    unsigned char
    i,
    data_pairs,
    data_type,
    temp_byte,
    temp_store,
    address_lo,
    address_hi;
    
    do
    {
        usart_Write('R');
        
        if(usart_Read() != ':') // check to make sure the first character is ':'
        {
            return(2);
        }
        /* get the count of data pairs */
        data_pairs  = boot_Hex_To_Bin(usart_Read()) << 4;
        data_pairs |= boot_Hex_To_Bin(usart_Read());
        
        /* get the address to put the data */
        /* although we collect this data, we do not use it.  All data
         p rogramme*d through this bootloader starts at application program
         space location 0x0000. The collection is neccessary only for
         the checksum calculation. */
        address_hi  = boot_Hex_To_Bin(usart_Read()) << 4;
        address_hi |= boot_Hex_To_Bin(usart_Read());
        
        address_lo  = boot_Hex_To_Bin(usart_Read()) << 4;
        address_lo |= boot_Hex_To_Bin(usart_Read());
        
        /* get the data type */
        data_type  = boot_Hex_To_Bin(usart_Read()) << 4;
        data_type |= boot_Hex_To_Bin(usart_Read());
        
        temp_store = data_pairs + address_hi + address_lo + data_type;
        
        for( i = 0; i < data_pairs; i++ )
        {
            temp_byte  = boot_Hex_To_Bin(usart_Read()) << 4;
            temp_byte |= boot_Hex_To_Bin(usart_Read());
            
            pageBuf[pageEnd] = temp_byte;
            ring_Inc(pageEnd);
            pageSize++;
            
            temp_store += temp_byte;
        }
        
        /* get the checksum */
        temp_byte  = boot_Hex_To_Bin(usart_Read()) << 4;
        temp_byte |= boot_Hex_To_Bin(usart_Read());
        
        usart_Read(); // get and dispose of the LF
        usart_Read(); // get and dispose of the CR
        
        /* check the data and checksum */
        if( (char)(temp_store + temp_byte) )
        {
            return 3;
        }
        
        /* fill the rest of the page buffer with 0xFF if the last records are not 
         * a full page in length */
        if(data_type == 1)
        {
            while (pageSize < SPM_PAGESIZE)
            {
                pageBuf[pageEnd] = 0xFF;
                ring_Inc(pageEnd);
                pageSize++;
            }
            
            return 1;
        }
        
    } while(pageSize <= SPM_PAGESIZE);
    
    return 0;
}
