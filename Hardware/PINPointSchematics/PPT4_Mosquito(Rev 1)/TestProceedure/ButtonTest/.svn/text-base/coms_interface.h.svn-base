/**
 * @file coms_interface.h
 *
 * Interface functions for communicating over the USB.
 */

#ifndef _COMS_INTERFACE_H_
#define _COMS_INTERFACE_H_

#include "globals.h"

/**
 * @enum comsType Describes the type of communication received.
 */
typedef enum
{
    NONE = 0,         ///< No message received.
    OVERFLOW = 1,     ///< Communications buffer overflow.
    BAD = 2,          ///< Message received but not recognized.
    VERIFY = 3,       ///< Verification message received.
    READ_FLASH = 4,   ///< Flash read request received.
    READ_CONFIG = 5,  ///< EEPROM read request received.
    WRITE_TIME = 6,   ///< Time write request received.
    WRITE_CONFIG = 7, ///< EEPROM write request received.
    LIVE_DATA = 8,    ///< Live data request received.
    HEADER_REQ = 9,   ///< Data header request.
    RESET_REQ = 10    ///< Reset request.
} comsType;

/**
 * @struct ComsMsg Contains a communications message and its type.
 */
typedef struct
{
    comsType type; ///< Type of message.
    char msg[8];   ///< Message contents, not including the message code itself.
} ComsMsg;

#define COMS_BAUD 4 ///< 115,200  (FOSC/(16L * baud)) - 1


#define COMS_BUFFER_SIZE 20///< Size of communications buffer.


#define COMS_VERIFY_IN  0x01 ///< Message code for a verification request.
#define COMS_VERIFY_OUT 0x02 ///< Message code for confirming a verification request.

#define COMS_VERIFY_MAJOR_VERSION 0x06 ///< Current device major version number.
#define COMS_VERIFY_MINOR_VERSION 0x00 ///< Current device minor version number.

#define COMS_VERIFY_SIZE 1 ///< Size of verification request message.

#define COMS_READ_FLASH_PAGE_IN   0x02 ///< Message code for a read flash page request.
#define COMS_READ_FLASH_PAGE_SIZE    7 ///< Size of a read flash page request message.

#define COMS_WRITE_TIME_IN   0x03 ///< Message code for a write time request.
#define COMS_WRITE_TIME_OUT  0x06 ///< Message code for confirming a write time request.
#define COMS_WRITE_TIME_SIZE    8 ///< Size of a write flash page request message.

#define COMS_READ_CONFIG_IN   0x04 ///< Message code for a read config request.
#define COMS_READ_CONFIG_SIZE    3 ///< Size of a read config request.

#define COMS_WRITE_CONFIG_IN   0x05 ///< Message code for a write config request.
#define COMS_WRITE_CONFIG_OUT  0x0A ///< Message code for confirming a write config request.
#define COMS_WRITE_CONFIG_SIZE    4 ///< Size of a write config request message.

#define COMS_LIVE_DATA_IN   0x06 ///< Message code for a live data request.
#define COMS_LIVE_DATA_SIZE    1 ///< Size of a live data request message.

#define COMS_HEADER_REQ_IN   0x07 ///< Message code for a data header request.
#define COMS_HEADER_REQ_SIZE    1 ///< Size of a data header request message.

#define COMS_RESET_REQ_IN   0x08 ///< Message code for a reset request.
#define COMS_RESET_REQ_SIZE    8 ///< Size of a reset request message.

#define COMS_RESET_REQ_PAYLOAD "CONFIRM" ///< Correct payload for a reset request message.

#define COMS_ERROR 0xFF ///< Message code for an error.

extern void coms_Init(void);
extern ComsMsg coms_Poll(void);
extern void coms_Clear(int num);
extern void coms_Handle(RunData *runData);

#endif
