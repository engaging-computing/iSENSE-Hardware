//
//  Fields.h
//  iOS Car Ramp Physics
//
//  Created by Mike Stowell on 3/4/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <Foundation/Foundation.h>

@interface Fields : NSObject {}

@property (assign) NSNumber *accel_x;
@property (assign) NSNumber *accel_y;
@property (assign) NSNumber *accel_z;
@property (assign) NSNumber *accel_total;
@property (assign) NSNumber *temperature_c;
@property (assign) NSNumber *temperature_f;
@property (assign) NSNumber *temperature_k;
@property (assign) NSNumber *time_millis;
@property (assign) NSNumber *lux;
@property (assign) NSNumber *angle_deg;
@property (assign) NSNumber *angle_rad;
@property (assign) NSNumber *latitude;
@property (assign) NSNumber *longitude;
@property (assign) NSNumber *mag_x;
@property (assign) NSNumber *mag_y;
@property (assign) NSNumber *mag_z;
@property (assign) NSNumber *mag_total;
@property (assign) NSNumber *altitude;
@property (assign) NSNumber *pressure;
@property (assign) NSNumber *gyro_x;
@property (assign) NSNumber *gyro_y;
@property (assign) NSNumber *gyro_z;

@end
