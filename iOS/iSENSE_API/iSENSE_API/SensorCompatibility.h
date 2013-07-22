//
//  SensorCompatability.h
//  iSENSE API
//
//  Created by Mike Stowell on 6/26/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import <UIKit/UIKit.h>
#import "SensorEnums.h"

@interface SensorCompatibility : NSObject {
    
    enum DeviceList {
        IPHONE_2G = 0, IPHONE_3G = 1, IPHONE_3GS = 2, IPHONE_4 = 3, IPHONE_4S = 4, IPHONE_5 = 5,
        IPOD_TOUCH_1 = 6, IPOD_TOUCH_2 = 7, IPOD_TOUCH_3 = 8, IPOD_TOUCH_4 = 9, IPOD_TOUCH_5 = 10,
        IPAD_1 = 11, IPAD_2 = 12, IPAD_3 = 13, IPAD_4 = 14, IPAD_MINI = 15, NONSENSE = -1
    };
    
    int sensorType;
    int compatible[5];
}

- (int)  getDeviceType;
- (void) setCompatibility:(int)deviceType withDispatch:(int[16][5])dispatch;
- (void) fillArray:(int)v0 v1:(int)v1 v2:(int)v2 v3:(int)v3 v4:(int)v4;
- (int)  getCompatibilityForSensorType:(int)index;

@end
