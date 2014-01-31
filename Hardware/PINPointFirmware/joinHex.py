firmware   = open('isense.hex').readlines()[:-1]
bootloader = open('bootloader.hex').readlines()
pinpoint   = open('pinpoint.hex', 'w')

for line in firmware:
    pinpoint.write(line)

for line in bootloader:
    pinpoint.write(line)

pinpoint.close()
