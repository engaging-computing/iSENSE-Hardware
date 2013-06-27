//
//  SensorCompatibility.m
//  iSENSE_API
//
//  Created by Michael Stowell on 6/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "SensorCompatibility.h"

@implementation SensorCompatibility

-(id) init {
    self = [super init];
    if (self) {
        // TODO - initialization code
        
        
        // Columns:   0 - GPS
        //            1 - Accelerometer
        //            2 - Ambient Light
        //            3 - Gyroscope
        //            4 - Proximity
        int sensorDispatchTable[16][5] = {
            {AVAIL_CONNECTIVITY,    AVAILABLE,  AVAILABLE,       NOT_AVAILABLE,  AVAILABLE      },      // iPhone 1
            {AVAIL_CONNECTIVITY,    AVAILABLE,  AVAILABLE,       NOT_AVAILABLE,  AVAILABLE      },      // iPhone 3G
            {AVAILABLE,             AVAILABLE,  AVAILABLE,       NOT_AVAILABLE,  AVAILABLE      },      // iPhone 3GS
            {AVAILABLE,             AVAILABLE,  AVAILABLE,       AVAILABLE,      AVAILABLE      },      // iPhone 4
            {AVAILABLE,             AVAILABLE,  AVAILABLE,       AVAILABLE,      AVAILABLE      },      // iPhone 4S
            {AVAILABLE,             AVAILABLE,  AVAILABLE,       AVAILABLE,      AVAILABLE      },      // iPhone 5
            {AVAIL_WIFI_ONLY,       AVAILABLE,  AVAILABLE,       NOT_AVAILABLE,  NOT_AVAILABLE  },      // iPod Touch 1
            {AVAIL_WIFI_ONLY,       AVAILABLE,  AVAILABLE,       NOT_AVAILABLE,  NOT_AVAILABLE  },      // iPod Touch 2
            {AVAIL_WIFI_ONLY,       AVAILABLE,  AVAILABLE,       NOT_AVAILABLE,  NOT_AVAILABLE  },      // iPod Touch 3
            {AVAIL_WIFI_ONLY,       AVAILABLE,  AVAILABLE,       AVAILABLE,      NOT_AVAILABLE  },      // iPod Touch 4
            {AVAIL_WIFI_ONLY,       AVAILABLE,  NOT_AVAILABLE,   AVAILABLE,      NOT_AVAILABLE  },      // iPod Touch 5
            {AVAILABLE,             AVAILABLE,  AVAILABLE,       NOT_AVAILABLE,  NOT_AVAILABLE  },      // iPad 1
            {AVAILABLE,             AVAILABLE,  AVAILABLE,       AVAILABLE,      NOT_AVAILABLE  },      // iPad 2
            {AVAILABLE,             AVAILABLE,  AVAILABLE,       AVAILABLE,      NOT_AVAILABLE  },      // iPad 3
            {AVAILABLE,             AVAILABLE,  AVAILABLE,       AVAILABLE,      NOT_AVAILABLE  },      // iPad 4
            {AVAILABLE,             AVAILABLE,  AVAILABLE,       AVAILABLE,      NOT_AVAILABLE  }       // iPad Mini
        };
        // Values:  0 - not available
        //          1 - always available
        //          2 - available on wifi or mobile connectivity only
        //          3 - available on wifi only
    }
    return self;
}

@end
