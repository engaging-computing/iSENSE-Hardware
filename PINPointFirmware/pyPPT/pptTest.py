import pptscanner
import pinpoint
import serial
import sys


def test():
    ser = serial.Serial(pptscanner.usbScan()[0], 115200, timeout = 1)

    ser.flushInput()

    c = ser.read()

    while (c != '\x02' and c != '\x01'):
        #print list(c),
        sys.stdout.write(c)
        c = ser.read()

    if (c == '\x01'):

        print
        raw_input('Press enter to continue...')
        
        port = ser.portstr
        ppt = pinpoint.PPT4(port)

        sn = int(input("Please enter the device's serial number: "))

        for x in range(3):
            ppt.setEepromKey('serialNumber', sn)
            verify = ppt.readEepromConfig()

            if (verify['serialNumber'] == sn):
                print 'Serial number programed, please proceed to the next portion of the test.'
                return
            elif (x < 2):
                print 'Failed to burn serial number. Re-trying.'
            else:
                print 'Failed to burn serial number. Please note this and continue with the next portion of the test.'
                return
        
test()
