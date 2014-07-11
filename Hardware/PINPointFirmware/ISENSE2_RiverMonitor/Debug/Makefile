################################################################################
# Automatically-generated file. Do not edit!
################################################################################

SHELL := cmd.exe
RM := rm -rf

USER_OBJS :=

LIBS := 
PROJ := 

O_SRCS := 
C_SRCS := 
S_SRCS := 
S_UPPER_SRCS := 
OBJ_SRCS := 
ASM_SRCS := 
PREPROCESSING_SRCS := 
OBJS := 
OBJS_AS_ARGS := 
C_DEPS := 
C_DEPS_AS_ARGS := 
EXECUTABLES := 
LIB_AS_ARGS :=
OUTPUT_FILE_PATH :=
OUTPUT_FILE_PATH_AS_ARGS :=
HEX_FLASH_FILE_PATH :=
HEX_FLASH_FILE_PATH_AS_ARGS :=
HEX_EEPROM_FILE_PATH :=
HEX_EEPROM_FILE_PATH_AS_ARGS :=
LSS_FILE_PATH :=
LSS_FILE_PATH_AS_ARGS :=
MAP_FILE_PATH :=
MAP_FILE_PATH_AS_ARGS :=
AVR_APP_PATH :=C:/Program Files (x86)/Atmel/AVR Studio 5.0/AVR ToolChain/bin/
QUOTE := "
ADDITIONAL_DEPENDENCIES:=
OUTPUT_FILE_DEP:=

# Every subdirectory with source files must be described here
SUBDIRS := 


# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS +=  \
../adc_interface.c \
../adx_interface.c \
../bmp085_interface.c \
../coms_interface.c \
../data_interface.c \
../globals.c \
../gps_interface.c \
../isense.c \
../light_interface.c \
../rtc_interface.c \
../sensor_interface.c \
../spi_master.c \
../timer_interface.c \
../twi_interface.c \
../usart_interface.c \
../user_interface.c


PREPROCESSING_SRCS += 


ASM_SRCS += 


OBJS +=  \
adc_interface.o \
adx_interface.o \
bmp085_interface.o \
coms_interface.o \
data_interface.o \
globals.o \
gps_interface.o \
isense.o \
light_interface.o \
rtc_interface.o \
sensor_interface.o \
spi_master.o \
timer_interface.o \
twi_interface.o \
usart_interface.o \
user_interface.o


OBJS_AS_ARGS +=  \
adc_interface.o \
adx_interface.o \
bmp085_interface.o \
coms_interface.o \
data_interface.o \
globals.o \
gps_interface.o \
isense.o \
light_interface.o \
rtc_interface.o \
sensor_interface.o \
spi_master.o \
timer_interface.o \
twi_interface.o \
usart_interface.o \
user_interface.o


C_DEPS +=  \
adc_interface.d \
adx_interface.d \
bmp085_interface.d \
coms_interface.d \
data_interface.d \
globals.d \
gps_interface.d \
isense.d \
light_interface.d \
rtc_interface.d \
sensor_interface.d \
spi_master.d \
timer_interface.d \
twi_interface.d \
usart_interface.d \
user_interface.d


C_DEPS_AS_ARGS +=  \
adc_interface.d \
adx_interface.d \
bmp085_interface.d \
coms_interface.d \
data_interface.d \
globals.d \
gps_interface.d \
isense.d \
light_interface.d \
rtc_interface.d \
sensor_interface.d \
spi_master.d \
timer_interface.d \
twi_interface.d \
usart_interface.d \
user_interface.d


OUTPUT_FILE_PATH +=ISENSE2.elf

OUTPUT_FILE_PATH_AS_ARGS +=ISENSE2.elf

HEX_FLASH_FILE_PATH +=ISENSE2.hex

HEX_FLASH_FILE_PATH_AS_ARGS +=ISENSE2.hex

HEX_EEPROM_FILE_PATH +=ISENSE2.eep

HEX_EEPROM_FILE_PATH_AS_ARGS +=ISENSE2.eep

LSS_FILE_PATH +=ISENSE2.lss

LSS_FILE_PATH_AS_ARGS +=ISENSE2.lss

MAP_FILE_PATH =ISENSE2.map

MAP_FILE_PATH_AS_ARGS =ISENSE2.map

LIB_AS_ARGS +=libISENSE2.a

ADDITIONAL_DEPENDENCIES:= $(HEX_FLASH_FILE_PATH) $(LSS_FILE_PATH) $(HEX_EEPROM_FILE_PATH) size

OUTPUT_FILE_DEP:= ./makedep.mk

# AVR/GNU C Compiler

































./%.o: .././%.c
	@echo Building file: $<
	@echo Invoking: AVR/GNU C Compiler
	$(QUOTE)$(AVR_APP_PATH)avr-gcc.exe$(QUOTE) -funsigned-char -funsigned-bitfields -O0 -fpack-struct -fshort-enums -g2 -Wall -c -std=gnu99  -mmcu=atmega324p   -MD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo Finished building: $<



# AVR/GNU Assembler




ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: $(OUTPUT_FILE_PATH) $(ADDITIONAL_DEPENDENCIES)

# AVR/GNU C/C++ Linker
$(OUTPUT_FILE_PATH): $(OBJS) $(USER_OBJS) $(OUTPUT_FILE_DEP)
	@echo Building target: $@
	@echo Invoking: AVR/GNU C/C++ Linker
	$(QUOTE)$(AVR_APP_PATH)avr-gcc.exe$(QUOTE)  -mmcu=atmega324p  -Wl,-Map=$(MAP_FILE_PATH_AS_ARGS) -o$(OUTPUT_FILE_PATH_AS_ARGS) $(OBJS_AS_ARGS) $(USER_OBJS) $(LIBS)
	@echo Finished building target: $@



$(HEX_FLASH_FILE_PATH): $(OUTPUT_FILE_PATH)
	$(QUOTE)$(AVR_APP_PATH)avr-objcopy.exe$(QUOTE) -O ihex -R .eeprom -R .fuse -R .lock -R .signature  $(QUOTE)$<$(QUOTE) $(QUOTE)$@$(QUOTE)

$(HEX_EEPROM_FILE_PATH): $(OUTPUT_FILE_PATH)
	-$(QUOTE)$(AVR_APP_PATH)avr-objcopy.exe$(QUOTE) -j .eeprom --set-section-flags=.eeprom=alloc,load --change-section-lma .eeprom=0 --no-change-warnings -O ihex $(QUOTE)$<$(QUOTE) $(QUOTE)$@$(QUOTE) || exit 0

$(LSS_FILE_PATH): $(OUTPUT_FILE_PATH)
	$(QUOTE)$(AVR_APP_PATH)avr-objdump.exe$(QUOTE) -h -S $(QUOTE)$<$(QUOTE) > $(QUOTE)$@$(QUOTE)

size: $(OUTPUT_FILE_PATH)
	@$(QUOTE)$(AVR_APP_PATH)avr-size.exe$(QUOTE) -C --mcu=atmega324p $(OUTPUT_FILE_PATH_AS_ARGS)

# Other Targets
clean:
	-$(RM) $(OBJS_AS_ARGS)$(C_DEPS_AS_ARGS) $(EXECUTABLES) $(LIB_AS_ARGS) $(HEX_FLASH_FILE_PATH_AS_ARGS) $(HEX_EEPROM_FILE_PATH_AS_ARGS) $(LSS_FILE_PATH_AS_ARGS) $(MAP_FILE_PATH_AS_ARGS)

