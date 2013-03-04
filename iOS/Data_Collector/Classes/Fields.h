//
//  Fields.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 3/4/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <Foundation/Foundation.h>

@interface Fields : NSObject {}

@property (assign) NSString *accel_x;
@property (assign) NSString *accel_y;
@property (assign) NSString *accel_z;
@property (assign) NSString *accel_total;
@property (assign) NSString *temperature_c;
@property (assign) NSString *temperature_f;
@property (assign) NSString *temperature_k;
@property (assign) NSNumber *time_millis;   // long
@property (assign) NSString *lux;
@property (assign) NSString *angle_deg;
@property (assign) NSString *angle_rad;
@property (assign) NSNumber *latitude;      // double
@property (assign) NSNumber *longitude;     // double
@property (assign) NSNumber *mag_x;         // double
@property (assign) NSNumber *mag_y;         // double
@property (assign) NSNumber *mag_z;         // double
@property (assign) NSNumber *mag_total;     // double
@property (assign) NSString *altitude;
@property (assign) NSString *pressure;

@end
