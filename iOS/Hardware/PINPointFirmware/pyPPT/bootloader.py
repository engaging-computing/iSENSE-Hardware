import serial
import datetime

def programFlash(port, hexFile, prog = True):
    size = len(hexFile)
    x = 0
    printed = 0

    if prog:
        print '0%       10%       20%       30%       40%       50%       60%       70%       80%       90%      100%'
        print '|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|'
        print '|',
    
    port.flushInput()

    port.write('P')
    ret = port.read(1)

    for line in hexFile:
        port.write(line)
        ret = port.read(1)

        if (ret == 'E'):
            ret = 'E' + str(list(port.read(1)))

            print 'ERROR - ' + ret

            return False

        x += 1

        if prog:
            while ((printed + 2) < (100 * (float(x + 1) / size))):
                printed += 2
                print '*',

    return True


