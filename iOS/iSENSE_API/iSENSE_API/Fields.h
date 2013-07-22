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

@property (retain) NSNumber *accel_x;
@property (retain) NSNumber *accel_y;
@property (retain) NSNumber *accel_z;
@property (retain) NSNumber *accel_total;
@property (retain) NSNumber *temperature_c;
@property (retain) NSNumber *temperature_f;
@property (retain) NSNumber *temperature_k;
@property (retain) NSNumber *time_millis;
@property (retain) NSNumber *lux;
@property (retain) NSNumber *angle_deg; // TODO doesn't recognize field if it's called "angle-degrees"
@property (retain) NSNumber *angle_rad; // TODO see above
@property (retain) NSNumber *latitude;
@property (retain) NSNumber *longitude;
@property (retain) NSNumber *mag_x;
@property (retain) NSNumber *mag_y;
@property (retain) NSNumber *mag_z;
@property (retain) NSNumber *mag_total;
@property (retain) NSNumber *altitude;
@property (retain) NSNumber *pressure;  // TODO doesn't recognize "pressure"
@property (retain) NSNumber *gyro_x;
@property (retain) NSNumber *gyro_y;
@property (retain) NSNumber *gyro_z;

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
