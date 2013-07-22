//
//  SensorCompatability.m
//  iSENSE API
//
//  Created by Mike Stowell on 6/26/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "SensorCompatibility.h"
#include <sys/types.h>
#include <sys/sysctl.h>

@implementation SensorCompatibility

// Initializer for the SensorCompatibility class
-(id) init {
    self = [super init];
    if (self) {
        
        // Columns:
        //  0 - GPS                 1 - Accelerometer   2 - Ambient Light   3 - Gyroscope   4 - Proximity
        int sensorDispatchTable[16][5] = {
            {AVAIL_CONNECTIVITY,    AVAILABLE,          AVAILABLE,          NOT_AVAILABLE,  AVAILABLE      },      // 0 - iPhone 2G
            {AVAIL_CONNECTIVITY,    AVAILABLE,          AVAILABLE,          NOT_AVAILABLE,  AVAILABLE      },      // 1 - iPhone 3G
            {AVAILABLE,             AVAILABLE,          AVAILABLE,          NOT_AVAILABLE,  AVAILABLE      },      // 2 - iPhone 3GS
            {AVAILABLE,             AVAILABLE,          AVAILABLE,          AVAILABLE,      AVAILABLE      },      // 3 - iPhone 4
            {AVAILABLE,             AVAILABLE,          AVAILABLE,          AVAILABLE,      AVAILABLE      },      // 4 - iPhone 4S
            {AVAILABLE,             AVAILABLE,          AVAILABLE,          AVAILABLE,      AVAILABLE      },      // 5 - iPhone 5
            {AVAIL_WIFI_ONLY,       AVAILABLE,          AVAILABLE,          NOT_AVAILABLE,  NOT_AVAILABLE  },      // 6 - iPod Touch 1
            {AVAIL_WIFI_ONLY,       AVAILABLE,          AVAILABLE,          NOT_AVAILABLE,  NOT_AVAILABLE  },      // 7 - iPod Touch 2
            {AVAIL_WIFI_ONLY,       AVAILABLE,          AVAILABLE,          NOT_AVAILABLE,  NOT_AVAILABLE  },      // 8 - iPod Touch 3
            {AVAIL_WIFI_ONLY,       AVAILABLE,          AVAILABLE,          AVAILABLE,      NOT_AVAILABLE  },      // 9 - iPod Touch 4
            {AVAIL_WIFI_ONLY,       AVAILABLE,          NOT_AVAILABLE,      AVAILABLE,      NOT_AVAILABLE  },      // iPod Touch 5
            {AVAILABLE,             AVAILABLE,          AVAILABLE,          NOT_AVAILABLE,  NOT_AVAILABLE  },      // iPad 1
            {AVAILABLE,             AVAILABLE,          AVAILABLE,          AVAILABLE,      NOT_AVAILABLE  },      // iPad 2
            {AVAILABLE,             AVAILABLE,          AVAILABLE,          AVAILABLE,      NOT_AVAILABLE  },      // iPad 3
            {AVAILABLE,             AVAILABLE,          AVAILABLE,          AVAILABLE,      NOT_AVAILABLE  },      // iPad 4
            {AVAILABLE,             AVAILABLE,          AVAILABLE,          AVAILABLE,      NOT_AVAILABLE  }       // iPad Mini 1G
        };
        
        int device = [self getDeviceType];
        [self setCompatibility:device withDispatch:sensorDispatchTable];

    }
    return self;
}

- (int) getDeviceType {
    // Get a String determining user's device and generation, e.g. iPad1,1
    size_t size;
    sysctlbyname("hw.machine", NULL, &size, NULL, 0);
    char *machine = malloc(size);
    sysctlbyname("hw.machine", machine, &size, NULL, 0);
    NSString* platform = [NSString stringWithCString:machine encoding:NSASCIIStringEncoding];
    free(machine);
    
    // Parse the string into device and generation: strip the piece past the comma, then separate the generation
    NSArray *split = [platform componentsSeparatedByString:@","];
    NSString *deviceAndGen = [split objectAtIndex: 0];
    
    NSString *device = [deviceAndGen substringToIndex:[deviceAndGen length] - 1];
    NSString *gen = [deviceAndGen substringFromIndex:[deviceAndGen length] - 1];
    NSString *sku;
    @try {
        sku = [split objectAtIndex:1];
    } @catch (NSException *e) {
        sku = @"-1";
    }
    
    // iphone 2g = 1,1
    // iphone 3g = 1,2
    // iphone 3gs = 2,1
    // iphone 4 = 3,x
    // iphone 4S = 4,x
    // iphone 5 = 5,x
    
    // ipod kG = k,x
    
    // ipad 1 = 1,x
    // ipad 2 = 2,1-4
    // ipad 3 = 3,1-3
    // ipad 4 = 3,4-6
    
    // ipad mini = ipad 2,5-7
    
    // Find the device type based on the table above ( http://theiphonewiki.com/wiki/Models )
    if ([sku isEqualToString:@"-1"]) return NONSENSE;
    if ([[device lowercaseString] rangeOfString:@"iphone"].location != NSNotFound) {
        if ([gen isEqualToString:@"1"]) {
            if ([sku isEqualToString:@"1"]) {
                return IPHONE_2G;
            } else if ([sku isEqualToString:@"2"]) {
                return IPHONE_3G;
            }
        } else if ([gen isEqualToString:@"2"]) {
            return IPHONE_3GS;
        } else if ([gen isEqualToString:@"3"]) {
            return IPHONE_4;
        } else if ([gen isEqualToString:@"4"]) {
            return IPHONE_4S;
        } else if ([gen isEqualToString:@"5"]) {
            return IPHONE_5;
        }
    } else if ([[device lowercaseString] rangeOfString:@"ipod"].location != NSNotFound) {
        if ([gen isEqualToString:@"1"]) {
            return IPOD_TOUCH_1;
        } else if ([gen isEqualToString:@"2"]) {
            return IPOD_TOUCH_2;
        } else if ([gen isEqualToString:@"3"]) {
            return IPOD_TOUCH_3;
        } else if ([gen isEqualToString:@"4"]) {
            return IPOD_TOUCH_4;
        } else if ([gen isEqualToString:@"5"]) {
            return IPOD_TOUCH_5;
        }
    } else if ([[device lowercaseString] rangeOfString:@"ipad"].location != NSNotFound) {
        if ([gen isEqualToString:@"1"]) {
            return IPAD_1;
        } else if ([gen isEqualToString:@"2"]) {
            if ([sku isEqualToString:@"1"] || [sku isEqualToString:@"2"] || [sku isEqualToString:@"3"] || [sku isEqualToString:@"4"]) {
                return IPAD_2;
            } else if ([sku isEqualToString:@"5"] || [sku isEqualToString:@"6"] || [sku isEqualToString:@"7"]) {
                return IPAD_MINI;
            }
        } else if ([gen isEqualToString:@"3"]) {
            if ([sku isEqualToString:@"1"] || [sku isEqualToString:@"2"] || [sku isEqualToString:@"3"]) {
                return IPAD_3;
            } else if ([sku isEqualToString:@"4"] || [sku isEqualToString:@"5"] || [sku isEqualToString:@"6"]) {
                return IPAD_4;
            }
        }
    }
    
    return NONSENSE; // most likely a simulator or newer device not caught by this application yet
}

- (void) setCompatibility:(int)deviceType withDispatch:(int[16][5])dispatch {
    
    // received either newer device or simulator
    if (deviceType == NONSENSE) {
        [self fillArray:NOT_DETECTED v1:NOT_DETECTED v2:NOT_DETECTED v3:NOT_DETECTED v4:NOT_DETECTED];
        return;
    }
    
    int hasGPS = dispatch[deviceType][0];
    int hasAccelerometer = dispatch[deviceType][1];
    int hasAmbientLight = dispatch[deviceType][2];
    int hasGyroscope = dispatch[deviceType][3];
    int hasProximity = dispatch[deviceType][4];
    
    [self fillArray:hasGPS v1:hasAccelerometer v2:hasAmbientLight v3:hasGyroscope v4:hasProximity];
}

- (void) fillArray:(int)v0 v1:(int)v1 v2:(int)v2 v3:(int)v3 v4:(int)v4 {
    compatible[0] = v0;
    compatible[1] = v1;
    compatible[2] = v2;
    compatible[3] = v3;
    compatible[4] = v4;
}

- (int) getCompatibilityForSensorType:(int)index {
    return compatible[index];
}

@end