#! /usr/bin/env python

import serial
import pinpoint
import sys
import platform

""" Scans the system for USB devices, reporting the port numbers of those found. """
def usbScan():

    #Default, should support Windows and maybe Mac (untested)
    usbGen = lambda portNum : serial.Serial(portNum)
    usbDev = []

    if (platform.system() == 'Linux'):
        usbGen = lambda portNum : serial.Serial('/dev/ttyUSB' + str(portNum))
    
    for x in range(100):
        try:
            ser = usbGen(x)
            usbDev.append(ser.portstr)
            ser.close()
        except:
            pass
        
    return usbDev

"""
Returns a list of valid PINPoints constructed using typeFunc on the port string.
Defaults to constructing V4 PINPoints.
"""
def findPPTs(typeFunc = lambda portstring : pinpoint.PPT4(portstring)):
    ports = usbScan()
    ppts = []

    for port in ports:
        try:
            print 'Attempting to connect to ' + port + ' ...'
            ppt = typeFunc(port)
            ppts.append(ppt)
            print 'Success.'
        except Exception as e:
            print 'Failed: ' + str(e) + '. Not a PINPoint of the requested version.'

    return ppts
            
def progSerial(num):
    ppt = findPPTs()[0]
    ppt.writeEepromKey('serialNumber', num)
    print("Wrote: " + str(num))

def flashPPTs(path = '../isense.hex'):
    ppts = findPPTs()
    thds = []
    for ppt in ppts[:-1]:
        thds.append(pinpoint.BootThread(ppt, path))

    for thd in thds:
        thd.start()

    ppts[-1].writeFirmware(path)

    for thd in thds:
        thd.join()

def noiseTest(num):

    p = findPPTs()[0]
    dats = []

    for dat in p.getLiveGenerator(num):
        dats.append(dat.dataDict['accel'])
        print dats[-1],
        print '...',
    print
    print [min(dats), max(dats), sum(dats) / len(dats)]

    
