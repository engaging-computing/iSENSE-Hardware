//
//  SensorCompatibility.h
//  iSENSE_API
//
//  Created by Michael Stowell on 6/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@interface SensorCompatibility : NSObject {
    enum DispatchValues {
        NOT_AVAILABLE = 0, AVAILABLE = 1, AVAIL_CONNECTIVITY = 3, AVAIL_WIFI_ONLY = 4
    };
    
}

// enum SensorTypes
// int sensorType
// bool[] compat
// int[][] dispatch


@end
