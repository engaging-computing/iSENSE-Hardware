import pptscanner
import pinpoint
import time
import math

def gatherData(ppt, points, interval, totalEst = None):

    config = ppt.readEepromConfig()
    config['globalSampleRate'] = interval
    ppt.writeEepromConfig(config)

    sleepTime = (interval / 1000.0) * points

    ppt.resetReq()

    time.sleep(5)

    ppt.eraseReq()
    ppt.startReq()
    #############

    print
    print
    print 'Testing for ' + str(points) + ' at an interval of ' + str(interval) + 'ms.'
    print 'Estimated time: ' + str((interval * points) / 1000) + 's.'
    if (totalEst != None):
        print 'Total estimated time: ' + str(totalEst) + 's.'
    print
    print '0%       10%       20%       30%       40%       50%       60%       70%       80%       90%      100%'
    print '|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|'
    print '|',

    if (interval < 500):
        interval = 500

    points = int(math.ceil(sleepTime / (interval / 1000.0)))

    printed = 0
        
    for x in range(points):
        time.sleep(interval / 1000.0)

        while ((printed + 2) < (100 * (float(x + 1) / points))):
            printed += 2
            print '*',
    print '*'

    return ppt.readAll()

def trimFront(data):
    front = data[0].dataDict['date']
    i = 0

    while (data[i].dataDict['date'] == front):
        i = i + 1

    return data[i:]

def analyizeData(ppt, data):

    expectedRate = 1 / (ppt.readEepromConfig()['globalSampleRate'] / 1000.0)

    if (expectedRate > 1):
        return analyizeFast(trimFront(data), int(math.ceil(expectedRate)))
    else:
        return analyizeSlow(data[1:-1], int( 1.0 / expectedRate))

def analyizeSlow(data, interval):
    #print interval
    last = data[0].dataDict['date']
    lates = 0
    earlies = 0

    for datum in data[1:]:
        #print datum

        new = datum.dataDict['date']
        delta = new - last

        if (delta.seconds < interval):
            earlies += 1
            #print 'EARLY ABOVE HERE'
        elif (delta.seconds > interval):
            lates += 1
            #print 'LATE ABOVE HERE'

        last = new

    ret = dict()

    ret['Points'] = len(data)
    ret['Late Errors'] = lates
    ret['Early Errors'] = earlies
    ret['Late Error %'] = lates / float(len(data))
    ret['Early Error %'] = earlies / float(len(data))

    return ((1.0 / interval), ret['Points'], ret['Late Error %'], ret['Early Error %'], lates + earlies)
              

def analyizeFast(data, rate):
    
    last = data[0].dataDict['date']
    count = 0
    errorLow = 0
    errorHigh = 0

    for datum in data:
        #print datum
        
        if (datum.dataDict['date'] == last):
            count += 1
        else:
            if (count < rate):
                errorLow += 1
                #print 'LOW ABOVE HERE'
            if (count > rate):
                errorHigh += 1
                #print 'HIGH ABOVE HERE'

            count = 1
            last = datum.dataDict['date']

    ret = dict()

    ret['Points'] = len(data)
    ret['Low Errors'] = errorLow
    ret['High Errors'] = errorHigh
    ret['Low Error %'] = errorLow / float(len(data) / rate)
    ret['High Error %'] = errorHigh / float(len(data) / rate)

    return (rate, ret['Points'], ret['Low Error %'], ret['High Error %'], errorLow, errorHigh)

def runTestList(ppt, testIDs):
    
    analysisHeader = ("| Rate (Hz) | Total Points | Late/Low % | Early/High % | Total Errors |")

    def printer(analTup):
        
        print '| {0:9.2f} | {1:12d} | {2:10.2f} | {3:12.2f} | {4:12d} |'.format(*analTup)

    totalEst = reduce(lambda a, b: a + b , map(lambda (p, i): (p * i) / 1000.0, testIDs))

    data = []

    for point, interval in testIDs:
        data.append(analyizeData(ppt, gatherData(ppt, point, interval, totalEst)))
        totalEst -= point * interval / 1000.0

    print analysisHeader
    map(printer, data)


p = pptscanner.findPPTs()[0]
sampleTests = ((6000, 10),
               (3000, 20),
               (2400, 25),
               (1200, 50),
               (600, 100),
               (300, 200),
               (120, 500),
               (60, 1000),
               (30, 2000),
               (20, 3000),
               (12, 5000),
               (6, 10000))

