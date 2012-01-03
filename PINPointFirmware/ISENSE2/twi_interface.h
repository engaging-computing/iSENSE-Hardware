/**
 * @file twi_interface.h
 *
 * Interface functions for TWI communication.
 */

#include "globals.h"
#include "usart_interface.h"
#include <util/twi.h>

#define twi_Start()   (TWCR = (1 << TWINT) | (1 << TWSTA) | (1 << TWEN)) ///< Macro for starting a TWI command.
#define twi_Stop()    (TWCR = (1 << TWINT) | (1 << TWSTO) | (1 << TWEN)) ///< Macro for stopping a TWI command.
#define twi_Ack()     (TWCR = (1 << TWINT) | (1 << TWEA)  | (1 << TWEN)) ///< Macro for ack'ing over TWI.
#define twi_Nack()    (TWCR = (1 << TWINT) |                (1 << TWEN)) ///< Macro for nack'ing over TWI.

#define twi_Wait()     while(!(TWCR & (1 << TWINT))) ///< Macro for waiting during a TWI command.
#define twi_Check(x)  ((TWSR & 0xF8) == x)           ///< Macro for checking the TWI status register.
#define twi_WriteByte(x)  (TWDR = x)                 ///< Macro for writing to TWI.
#define twi_ReadByte(x)   (x = TWDR)                 ///< Macro for reading from TWI.

#define TW_PRESCALE 0x00 ///< Twi prescaler = 0x00(1), 0x01(4), 0x02(16), 0x03(64)
#define TW_BITRATE  0x20 ///< Twi bitrate, baud = fosc/(16 + 2*BR*4^PS)

extern status twi_Init(void);
extern void twi_Write(char slave, char* data, int size);
extern void twi_Read(char slave, char* data, int size);