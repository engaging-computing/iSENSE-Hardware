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

@property (strong) NSNumber *accel_x;
@property (strong) NSNumber *accel_y;
@property (strong) NSNumber *accel_z;
@property (strong) NSNumber *accel_total;
@property (strong) NSNumber *temperature_c;
@property (strong) NSNumber *temperature_f;
@property (strong) NSNumber *temperature_k;
@property (strong) NSNumber *time_millis;
@property (strong) NSNumber *lux;
@property (strong) NSNumber *angle_deg;
@property (strong) NSNumber *angle_rad;
@property (strong) NSNumber *latitude;
@property (strong) NSNumber *longitude;
@property (strong) NSNumber *mag_x;
@property (strong) NSNumber *mag_y;
@property (strong) NSNumber *mag_z;
@property (strong) NSNumber *mag_total;
@property (strong) NSNumber *altitude;
@property (strong) NSNumber *pressure;
@property (strong) NSNumber *gyro_x;
@property (strong) NSNumber *gyro_y;
@property (strong) NSNumber *gyro_z;

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
