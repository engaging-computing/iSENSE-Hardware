#####################################
# Machine Science Makefile template # 
#####################################
##Fuse Settings for bootloader
#w efuse 0 0xf8
#w hfuse 0 0xdf
#w lfuse 0 0xe2
#d efuse to view settings
 
## Name of project
PROJECT = isense

## Define MCU
MCU = atmega324p

## Define Programmer MCU
PROGRAMMER_MCU = m324p

## Define Programmer ID
PROGRAMMER_ID = dragon_isp
#PROGRAMMER_ID = stk500v2

## Define Programmer port
PROGRAMMER_PORT = usb
#PROGRAMMER_PORT = /dev/ttyS1

## Define Programmer bitclock
#PROGRAMMER_BC = 250
PROGRAMMER_BC = 10

## List of Executables
CC = avr-gcc
OBJCOPY = avr-objcopy
AVRSIZE = avr-size
AVRDUDE = avrdude

## Compile for MCU
COMMON = -mmcu=$(MCU)

## Define Debugging format
# stabs or dwarf-2
DEBUG = dwarf-2

## Define Optimization level 
# Possible leveles are [0, 1, 2, 3, s]
# 0 = turn off optimization. s = optimize for size.
OPT = 3

# Define C Standard level
# c89	- "ANSI" C
# gnu89	- c89 plus GCC extenstions
# c99	- ISO C99 standard (not yet full implemented)
# gnu99	- c99 plus GCC extentions
CSTANDARD = -std=gnu99

## Set all compile options
CFLAGS = $(COMMON)
CFLAGS += -O$(OPT)
#CFLAGS += -g$(DEBUG)
CFLAGS += -Wall -Wstrict-prototypes
CFLAGS += -funsigned-char -funsigned-bitfields -fpack-struct -fshort-enums
CFLAGS += -fno-builtin

## Define Print libaries
# Not defined
 PRINTF_LIB = 
# Minimalistic printf version
# PRINTF_LIB = -Wl,-u,vfprintf -lprintf_min
# Floating point printf version (requires MATH_LIB = -lm below)
#PRINTF_LIB = -Wl,-u,vfprintf -lprintf_flt

## Additional Math lib
#MATH_LIB = -lm
MATH_LIB = 

## Linker flags
LDFLAGS = $(COMMON)
LDFLAGS += $(PRINTF_LIB) $(MATH_LIB)

## Intel Hex file production flags
HEX_FLASH_FLAGS = -R .eeprom

HEX_EEPROM_FLAGS = -j .eeprom
HEX_EEPROM_FLAGS += --set-section-flags=.eeprom="alloc,load"
HEX_EEPROM_FLAGS += --change-section-lma .eeprom=0 --no-change-warnings

## Include Directories
INCLUDES =
#INCLUDES = -I../avr_inc 

## Library Directories
LIBDIRS = 
#-L avr_lib

## Libraries
LIBS =	
#LIBS = -llcd -lpins -lusart -lmp3 -lheart -l595 -l8usart -l8delay -l8adc -l8lcd

## Objects that must be built in order to link
OBJECTS = user_interface.o usart_interface.o adc_interface.o globals.o adx_interface.o bmp085_interface.o rtc_interface.o timer_interface.o twi_interface.o spi_master.o light_interface.o coms_interface.o data_interface.o
## Objects explicitly added by the user
LINKONLYOBJECTS = 

## Build
all: $(PROJECT).elf $(PROJECT).hex size

## Compile
$(PROJECT).o: $(PROJECT).c *.h
	$(CC) $(INCLUDES) $(CFLAGS) -c $<

$(OBJECTS) : %.o : %.c *.h
	$(CC) $(INCLUDES) $(CFLAGS) -c $<

##Link (LDFLADS, specifcally -lm MUST go last!)
$(PROJECT).elf: $(OBJECTS) $(PROJECT).o
	 $(CC) $(OBJECTS) $(PROJECT).o $(LINKONLYOBJECTS) $(LIBDIRS) $(LIBS) -o $(PROJECT).elf $(LDFLAGS)

%.hex: $(PROJECT).elf
	avr-objcopy -O ihex $(HEX_FLASH_FLAGS)  $< $@

%.eep: $(PROJECT).elf
	-avr-objcopy $(HEX_EEPROM_FLAGS) -O ihex $< $@ || exit 0

%.lss: $(PROJECT).elf
	avr-objdump -h -S $< > $@

size: ${PROJECT}.elf
	@echo
	@$(AVRSIZE) -Bdt $(PROJECT).elf

writeflash:
	$(AVRDUDE) -p $(PROGRAMMER_MCU) \
	-c $(PROGRAMMER_ID) -B $(PROGRAMMER_BC) -P $(PROGRAMMER_PORT) -e  -U flash:w:$(PROJECT).hex 

runterminal:
	$(AVRDUDE) -p $(PROGRAMMER_MCU) \
	-c $(PROGRAMMER_ID) -B $(PROGRAMMER_BC) -P $(PROGRAMMER_PORT) -t

setfuse:
	$(AVRDUDE) -p $(PROGRAMMER_MCU) \
	-c $(PROGRAMMER_ID) -B $(PROGRAMMER_BC) -u -P $(PROGRAMMER_PORT) -t

fuseinit:
	$(AVRDUDE) -p $(PROGRAMMER_MCU) \
	-c $(PROGRAMMER_ID) -B 16 -u -P $(PROGRAMMER_PORT) \
	-U efuse:w:0xfe:m \
	-U hfuse:w:0xd1:m \
	-U lfuse:w:0xcf:m

bugout:
	$(AVRDUDE) -p $(PROGRAMMER_MCU) \
	-c $(PROGRAMMER_ID) -B $(PROGRAMMER_BC) -P $(PROGRAMMER_PORT) -F

## Clean PROJECT
.PHONY: clean
clean:
	-rm -rf $(PROJECT).o $(OBJECTS) $(PROJECT).elf dep/* $(PROJECT).hex $(PROJECT).eep *~


