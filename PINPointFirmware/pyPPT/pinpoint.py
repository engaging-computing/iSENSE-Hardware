 # -*- coding: utf-8 -*-
import serial
import time
import struct
import math
import datetime
import itertools
import bootloader

def unsignedToSigned(num, bits):

    ret = 1

    if ((num & (1 << (bits - 1))) > 0):
        for x in range(bits - 1):
            if ((num & (1 << x)) == 0):
                ret += 1 << x
        ret = -ret
    else:
        return num

    return ret

def chunks(l, n):
    """
    Yield successive n-sized chunks from l.
    """
    for i in xrange(0, len(l), n):
        yield l[i:i+n]

class PPTError(Exception):

    def __repr__(self):
        return str(self)

    def __str__(self):
        return "PINPoint " + str(self.ppt) + " has encountered error: " + str(self.details)

    def __init__(self, ppt, details):
        self.ppt = ppt
        self.details = details

class PPT4:

    #Coms constants
    handshakeOut = bytes('\x01')
    handshakeIn  = 2

    readPageOut = bytes('\x02')

    writeTimeOut = bytes('\x03')
    writeTimeIn  = 6

    readEepromOut = bytes('\x04')

    writeEepromOut = bytes('\x05')
    writeEepromIn  = 10

    liveDataReqOut = bytes('\x06')
    liveDataReqIn = 12

    dataHeaderReqOut = bytes('\x07')

    resetReqOut = bytes('\x08') + 'CONFIRM'

    eraseReqOut = bytes('\x09') + 'CONFIRM'
    eraseReqIn = 18

    startReqOut = bytes('\x0A') + 'CONFIRM'
    startReqIn = 20

    bootOn  = 255
    bootOff = 0

    bluetoothBauds = {9600:  59,
                      19200: 29,
                      38400: 14,
                      57600:  9,
                      115200: 4}

    #EEPROM constants (addr, size)

    eepromFormat = dict()

    eepromFormat['globalSampleRate'] = (0, 2)

    eepromFormat['bta1Type']  = (12, 1)
    eepromFormat['bta2Type']  = (15, 1)
    eepromFormat['mini1Type'] = (18, 1)
    eepromFormat['mini2Type'] = (21, 1)

    eepromFormat['gpsThresh'] = (22, 1)

    eepromFormat['accelOffsetX'] = (23, 2)
    eepromFormat['accelOffsetY'] = (25, 2)
    eepromFormat['accelOffsetZ'] = (27, 2)

    eepromFormat['bluetoothBaud'] = (29, 1)
    eepromFormat['bluetoothFlag'] = (30, 1)

    eepromFormat['bootloaderFlag'] = (1019, 1)
    
    eepromFormat['serialNumber'] = (1020, 4)

    def __repr__(self):
        return str(self)
    
    def __str__(self):
        return str(self.ser)

    def __init__(self, port):
        
        self.ser = serial.Serial(port, 115200, timeout = 1)

        self.eepromValues = dict()

        self.ser.flushInput()

        self.handshake()
        self.dataHeaderReq()
        self.readEepromConfig()

        self.ser.timeout = self.eepromValues['globalSampleRate'] / 1000.0 + 1.0

    def handshake(self):

        self.ser.flushInput()
        self.ser.write(PPT4.handshakeOut)

        ret = struct.unpack('<BBB', self.ser.read(3))

        if (ret[0] != PPT4.handshakeIn):
            raise PPTError(self, "Failed to handshake, expected: " + str(PPT4.handshakeIn)
                           + ", received: " + str(ret[0]) + ".")

        self.majorVersion = ret[1]
        self.minorVersion = ret[2]

        print("Handshook with PPT4 software Ver" + str(self.majorVersion) +'.' + str(self.minorVersion))

        return

    def readPage(self, addr, size):

        self.ser.flushInput()

        self.ser.write(PPT4.readPageOut)
        self.ser.write(struct.pack('B', addr / 0x10000))
        self.ser.write(struct.pack('B', (addr / 0x100) % 0x100))
        self.ser.write(struct.pack('B', addr % 0x100))

        self.ser.write(struct.pack('B', size / 0x10000))
        self.ser.write(struct.pack('B', (size / 0x100) % 0x100))
        self.ser.write(struct.pack('B', size % 0x100))

        ret = self.ser.read(size + 1)

        chksum = struct.unpack('B', ret[-1:])[0]
        verify = 0

        for byte in ret[:-1]:
            verify += struct.unpack('B', byte)[0]
            verify = verify & 0xFF

        if (verify != chksum):
            print('Error - Bad Checksum')
            print str(verify) + ' != ' + str(chksum)
            return None

        dps = []

        for raw in chunks(ret[:-1], self.dataPointSize):
            dps.append(DataPoint6(raw))

        return dps

    def writeTime(self, dat = None):

        if (dat == None):
            dat = datetime.datetime.utcnow()

        packed = struct.pack('BBBBBBB', dat.second, dat.minute,
                             dat.hour, ((dat.weekday() + 1) % 7) + 1,
                             dat.day, dat.month, dat.year % 100)

        self.ser.flushInput()
        self.ser.write(PPT4.writeTimeOut)
        self.ser.write(packed)

    def readEeprom(self, addr):

        self.ser.flushInput()
        self.ser.write(PPT4.readEepromOut)
        self.ser.write(struct.pack('B', addr / 0x100))
        self.ser.write(struct.pack('B', addr % 0x100))

        ret = struct.unpack('B', self.ser.read(1))

        return ret[0]

    def writeEeprom(self, addr, val):

        self.ser.flushInput()
        self.ser.write(PPT4.writeEepromOut)
        self.ser.write(struct.pack('B', addr / 0x100))
        self.ser.write(struct.pack('B', addr % 0x100))
        self.ser.write(struct.pack('B', val))

        ret = struct.unpack('B', self.ser.read(1))

        return ret == PPT4.writeEepromIn

    def liveDataReq(self):
        
        self.ser.flushInput()
        self.ser.write(PPT4.liveDataReqOut)

        ret = struct.unpack('B', self.ser.read(1))[0]

        return ret == PPT4.liveDataReqIn

    def liveDataRead(self):

        dp = None

        if (self.dataPointSize == 0):
            print('Err: DataPoint size is 0.')

        ret = self.ser.read(self.dataPointSize)

        if (self.dataPointSize == 32):
            dp = DataPoint6(ret)

        return dp

    def dataHeaderReq(self):

        self.ser.flushInput()
        self.ser.write(PPT4.dataHeaderReqOut)

        ret = struct.unpack('<BBBB', self.ser.read(4))

        self.dataHeader = (ret[0] << 16) + (ret[1] << 8) + ret[2]
        self.dataPointSize = ret[3]

        return

    def resetReq(self):

        self.ser.flushInput()
        self.ser.write(PPT4.resetReqOut)

        return

    def eraseReq(self):

        self.ser.flushInput()
        self.ser.write(PPT4.eraseReqOut)

        ret = struct.unpack('B', self.ser.read(1))[0]

        return ret == PPT4.eraseReqIn

    def startReq(self):

        self.ser.flushInput()
        self.ser.write(PPT4.startReqOut)

        ret = struct.unpack('B', self.ser.read(1))[0]

        return ret == PPT4.startReqIn

    #Below are meta-commands

    def configBluetooth(self, baud):
        if baud in PPT4.bluetoothBauds.keys():
            baud = PPT4.bluetoothBauds[baud]
        else:
            print 'Unsupported baud requested, defaulting to 57600.'
            baud = PPT4.bluetoothBauds[57600]

        self.writeEepromKey('bluetoothBaud', baud)
        self.writeEepromKey('bluetoothFlag', PPT4.bootOn)

        print 'Switch the jumpers to bluetooth and restart to complete the configuration.'

    def writeFirmware(self, hexPath, prog = True):

        hexFile = open(hexPath).readlines()
        
        self.writeEepromKey('bootloaderFlag', PPT4.bootOn)
        self.resetReq()

        time.sleep(1)

        self.ser.baudrate = 19200

        ret = bootloader.programFlash(self.ser, hexFile, prog)

        self.ser.baudrate = 115200

        return ret

    def readAll(self):

        gen = self.getDataGenerator()

        ret = []

        for dat in gen:
            ret.append(dat)
        
        return ret

    def readEepromField(self, addrSize):

        size = addrSize[1]
        addr = addrSize[0] + size - 1
        val = 0
        
        for x in range(size):
            val += self.readEeprom(addr - x) << (x * 8)

        return val

    def writeEepromField(self, addrSize, val):

        size = addrSize[1]
        addr = addrSize[0] + size - 1

        for x in range(addrSize[1]):
            self.writeEeprom(addr - x, val % 256)
            val = val / 256

    def writeEepromKey(self, key, val):

        self.writeEepromField(PPT4.eepromFormat[key], val)

    def readEepromConfig(self):

        for kvp in PPT4.eepromFormat.iteritems():
            
            key = kvp[0]
            form = kvp[1]
            self.eepromValues[key] = self.readEepromField(form)

        return self.eepromValues

    def writeEepromConfig(self, config):

        self.eepromValues = config

        for kvp in PPT4.eepromFormat.iteritems():

            key = kvp[0]
            addrSize = kvp[1]

            self.writeEepromField(addrSize, config[key])

    def getLiveGenerator(self, samples = None):

        self.liveDataReq()

        if samples == None:
            for i in itertools.repeat(True):

                ret = self.liveDataRead()
                ok = self.liveDataReq()

                yield ret
        else:
            for i in itertools.repeat(True, samples):

                ret = self.liveDataRead()
                ok = self.liveDataReq()

                yield ret

    def getDataGenerator(self):

        self.dataHeaderReq()

        for i in range(0, self.dataHeader, self.dataPointSize):

            ret = self.readPage(i, self.dataPointSize)[0]

            yield ret


class DataPoint6:

    def __repr__(self):
        return str(self.dataDict)

    def __str__(self):

        return ('{date} {latitude:5.2f} {longitude:5.2f} {altitude:5d}m {pressure:6d}Pa ' + \
                '{temperature:3.1f}C {xAccel: 6.2f}m/s^2 {yAccel: 6.2f}m/s^2 {zAccel: 6.2f}m/s^2 ' + \
                '{accel: 6.2f}m/s^2 {light:6.2f}lux {humidity:3.1f} {bta1:4d} {bta2:4d} {mini1:4d} ' + \
                '{mini2:4d}').format(**self.dataDict)

    def __init__(self, byteString):

        def fixAccel(num):
            num = unsignedToSigned(num, 10)
            return num * 0.1533

        def fixLight(exp, man):
            return pow(2, exp) * 0.025 * man

        def fixHumidity(num):

            t = self.dataDict['temperature']

            ret = (0.204036 * num - 25.8065) / (1.0546 - 0.00216 * t) + 6
            
            return max(min(ret, 100.0), 0.0)

        def fixDate(seconds, minutes, hours, day, date, month, year):

            #print 'fixing... ' + str([seconds, minutes, hours, day, date, month, year])
            
            d = None

            try:
                d = datetime.datetime(2000 + year, month, date, hours, minutes, seconds)
            except:
                d = datetime.datetime.utcnow()

            return d
        

        data = struct.unpack('<hH hH H i h BBBBBBBBBBBBBBBB', byteString)

        self.dataDict = dict()

        self.dataDict['latitude']  = math.floor(data[0] / 100) + float(str(data[0] % 100) +  '.' + str(data[1])) / 60.0
        self.dataDict['longitude'] = math.floor(data[2] / 100) + float(str(data[2] % 100) +  '.' + str(data[3])) / 60.0

        self.dataDict['altitude'] = data[4]

        self.dataDict['pressure'] = data[5]

        self.dataDict['temperature'] = data[6] / 10.0

        rawBitpack = data[7:]

        self.dataDict['xAccel'] =   rawBitpack[0]               + ((rawBitpack[1] & 0x03) << 8)
        self.dataDict['yAccel'] = ((rawBitpack[1] & 0xFC) >> 2) + ((rawBitpack[2] & 0x0F) << 6)
        self.dataDict['zAccel'] = ((rawBitpack[2] & 0xF0) >> 4) + ((rawBitpack[3] & 0x3F) << 4)

        self.dataDict['xAccel'] = fixAccel(self.dataDict['xAccel'])
        self.dataDict['yAccel'] = fixAccel(self.dataDict['yAccel'])
        self.dataDict['zAccel'] = fixAccel(self.dataDict['zAccel'])
        self.dataDict['accel']  = math.sqrt(pow(self.dataDict['xAccel'], 2) + pow(self.dataDict['yAccel'], 2) + pow(self.dataDict['zAccel'], 2))

        lightExp = ((rawBitpack[3] & 0xC0) >> 6) + ((rawBitpack[4] & 0x03) << 2)
        lightMan = ((rawBitpack[4] & 0xFC) >> 2) + ((rawBitpack[5] & 0x03) << 6)
        self.dataDict['light'] = fixLight(lightExp, lightMan)

        humidity = ((rawBitpack[5] & 0xFC) >> 2) + ((rawBitpack[6] & 0x0F) << 6)
        self.dataDict['humidity'] = fixHumidity(humidity)

        self.dataDict['mini1'] = ((rawBitpack[6] & 0xF0) >> 4) + ((rawBitpack[7] & 0x3F) << 4)
        self.dataDict['mini2'] = ((rawBitpack[7] & 0xC0) >> 6) + ((rawBitpack[8] & 0xFF) << 2)

        self.dataDict['bta1'] =  (rawBitpack[9]  & 0xFF)       + ((rawBitpack[10] & 0x03) << 8)
        self.dataDict['bta2'] = ((rawBitpack[10] & 0xFC) >> 2) + ((rawBitpack[11] & 0x0F) << 6)

        seconds = ((rawBitpack[11] & 0xF0) >> 4) + ((rawBitpack[12] & 0x03) << 4)
        minutes = ((rawBitpack[12] & 0xFC) >> 2)
        hours   =  (rawBitpack[13] & 0x1F)
        day     = ((rawBitpack[13] & 0xE0) >> 5)
        date    =  (rawBitpack[14] & 0x1F)
        month   = ((rawBitpack[14] & 0xE0) >> 5) + ((rawBitpack[15] & 0x01) << 3)
        year    = ((rawBitpack[15] & 0xFE) >> 1)

        self.dataDict['date'] = fixDate(seconds, minutes, hours, day, date, month, year)

import threading

class DataThread(threading.Thread):

    def __init__(self, ppt, num):
        super(DataThread, self).__init__()

        self.ppt = ppt
        self.num = num
        self.data = []

    def getData(self):
        self.join()
        return self.data

    def run(self):
        for dat in self.ppt.getLiveGenerator(self.num):
            self.data.append(dat)

class BootThread(threading.Thread):

    def __init__(self, ppt, path):
        super(BootThread, self).__init__()
        self.ppt = ppt
        self.path = path

    def run(self):
        self.ppt.writeFirmware(self.path, False)

