## Integration Makefile
BOOTHEX = bootloader.hex
APPHEX = isense.hex
COMBHEX = pinpoint.hex
EEPHEX = eep.hex


## Define MCU
MCU = atmega324p

## Define Programmer MCU
PROGRAMMER_MCU = m324p

## Define Programmer ID
PROGRAMMER_ID = dragon_isp

## Define Programmer port
PROGRAMMER_PORT = usb

## Define Programmer bitclock
PROGRAMMER_BC = 12

## List of Executables
AVRDUDE = avrdude

comb:
	make others
	rm -f $(COMBHEX)
	python joinHex.py

pinpoint:
	make fuseinit
	sleep 2
	make writeeeprom
	sleep 2
	make writecomb

others:
	(cd Bootloader && make)
	(cd ISENSE2 && make)
	cp -f Bootloader/$(BOOTHEX) $(BOOTHEX)
	cp -f ISENSE2/$(APPHEX) $(APPHEX)

clean:
	(cd Bootloader && make clean)
	(cd ISENSE2 && make clean)

purge:
	rm -f $(BOOTHEX) $(COMBHEX) $(APPHEX) $(EEPHEX)

writecomb:
	$(AVRDUDE) -p $(PROGRAMMER_MCU) \
	-c $(PROGRAMMER_ID) -B $(PROGRAMMER_BC) -P $(PROGRAMMER_PORT) -e -U flash:w:$(COMBHEX)

writeboot:
	$(AVRDUDE) -p $(PROGRAMMER_MCU) \
	-c $(PROGRAMMER_ID) -B $(PROGRAMMER_BC) -P $(PROGRAMMER_PORT) -e -U flash:w:$(BOOTHEX)

writeeeprom:
	$(AVRDUDE) -p $(PROGRAMMER_MCU) \
	-c $(PROGRAMMER_ID) -B $(PROGRAMMER_BC) -P $(PROGRAMMER_PORT) -e -U eeprom:w:$(EEPHEX)

readeeprom:
	$(AVRDUDE) -p $(PROGRAMMER_MCU) \
	-c $(PROGRAMMER_ID) -B $(PROGRAMMER_BC) -P $(PROGRAMMER_PORT) -U eeprom:r:$(EEPHEX):i
	chmod a+wr $(EEPHEX)

fuseinit:
	$(AVRDUDE) -p $(PROGRAMMER_MCU) \
	-c $(PROGRAMMER_ID) -B $(PROGRAMMER_BC) -u -P $(PROGRAMMER_PORT) \
	-U efuse:w:0xfe:m \
	-U hfuse:w:0xd2:m \
	-U lfuse:w:0xcf:m
