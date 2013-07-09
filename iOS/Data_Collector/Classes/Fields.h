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

// Fields constants
#define fACCEL_X        0
#define fACCEL_Y        1
#define fACCEL_Z        2
#define fACCEL_TOTAL    3
#define fTEMPERATURE_C  4
#define fTEMPERATURE_F  5
#define fTEMPERATURE_K  6
#define fTIME_MILLIS    7
#define fLUX            8
#define fANGLE_DEG      9
#define fANGLE_RAD      10
#define fLATITUDE       11
#define fLONGITUDE      12
#define fMAG_X          13
#define fMAG_Y          14
#define fMAG_Z          15
#define fMAG_TOTAL      16
#define fALTITUDE       17
#define fPRESSURE       18
#define fGYRO_X         19
#define fGYRO_Y         20
#define fGYRO_Z         21

@end
