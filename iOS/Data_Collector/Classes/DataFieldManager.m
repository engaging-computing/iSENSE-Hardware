//
//  DataFieldManager.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 2/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "DataFieldManager.h"

@implementation DataFieldManager

@synthesize order, data;

- (NSMutableArray *) getFieldOrderOfExperiment:(int)exp {
    
    if (order) [order release];
    order = [[NSMutableArray alloc] init];
    
    iSENSE *iapi = [iSENSE getInstance];
    NSMutableArray *fields = [iapi getExperimentFields:[NSNumber numberWithInt:exp]];
    
    for (ExperimentField *field in fields) {
        NSLog(@"%d, %@", field.type_id.intValue, field.field_name);
        switch (field.type_id.intValue) {
            // Temperature (1)
            case TEMPERATURE:
                if (!([field.field_name.lowercaseString rangeOfString:@"f"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"temperature_f"]];
                } else if (!([field.field_name.lowercaseString rangeOfString:@"c"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"temperature_c"]];
                } else if (!([field.field_name.lowercaseString rangeOfString:@"k"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"temperature_k"]];
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
            
            // Potential Altitude (2, 3)
            case LENGTH:
            case DISTANCE:
                if (!([field.field_name.lowercaseString rangeOfString:@"altitude"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"altitude"]];
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
            // Time (7)
            case TIME:
                [order addObject:[StringGrabber grabField:@"time"]];
                break;
                
            // Light (8, 9, 29)
            case LUMINOUS_FLUX:
            case LUMINOUS_INTENSITY:
            case LIGHT:
                [order addObject:[StringGrabber grabField:@"luminous_flux"]];
                break;
                
            // Angle (10)
            case ANGLE:
                if (!([field.field_name.lowercaseString rangeOfString:@"deg"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"heading_deg"]];
                } else if (!([field.field_name.lowercaseString rangeOfString:@"rad"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"heading_rad"]];
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
            // Geospacial (19)
            case GEOSPACIAL:
                if (!([field.field_name.lowercaseString rangeOfString:@"lat"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"latitude"]];
                } else if (!([field.field_name.lowercaseString rangeOfString:@"lon"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"longitude"]];
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
            // Numeric/Custom (21, 22)
            case NUMERIC:
            case CUSTOM:
                if (!([field.field_name.lowercaseString rangeOfString:@"mag"].location == NSNotFound)) {
                    if (!([field.field_name.lowercaseString rangeOfString:@"x"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"magnetic_x"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"y"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"magnetic_y"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"z"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"magnetic_z"]];
                    } else {
                        [order addObject:[StringGrabber grabField:@"magnetic_total"]];
                    }
                } else if (!([field.field_name.lowercaseString rangeOfString:@"altitude"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"altitude"]];
                } else if (!([field.field_name.lowercaseString rangeOfString:@"gyro"].location == NSNotFound)
                           || !([field.field_name.lowercaseString rangeOfString:@"rotation"].location == NSNotFound)) {
                
                    if (!([field.field_name.lowercaseString rangeOfString:@"x"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"gyroscope_x"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"y"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"gyroscope_y"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"z"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"gyroscope_z"]];
                    } 
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
            // Acceleration (25)
            case ACCELERATION:
                if (!([field.field_name.lowercaseString rangeOfString:@"accel"].location == NSNotFound)) {
                    if (!([field.field_name.lowercaseString rangeOfString:@"x"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"accel_x"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"y"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"accel_y"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"z"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"accel_z"]];
                    } else {
                        [order addObject:[StringGrabber grabField:@"accel_total"]];
                    }
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
            // Pressure (27)
            case PRESSURE:
                [order addObject:[StringGrabber grabField:@"pressure"]];
                break;
                
            // No match
            default:
                [order addObject:[StringGrabber grabField:@"null_string"]];
                break;
        }
    }
    
    return order;
}


- (NSMutableArray *) orderDataFromFields:(Fields *)f {
    
    if (data) [data release];
    data = [[NSMutableArray alloc] init];
    
    if (!order) return data;
    
    for (NSString *s in order) {
        if ([s isEqualToString:[StringGrabber grabField:@"accel_x"]]) {
            [data addObject:[f accel_x]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"accel_y"]]) {
            [data addObject:[f accel_y]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"accel_z"]]) {
            [data addObject:[f accel_z]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"accel_total"]]) {
            [data addObject:[f accel_total]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"temperature_c"]]) {
            [data addObject:[f temperature_c]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"temperature_f"]]) {
            [data addObject:[f temperature_f]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"temperature_k"]]) {
            [data addObject:[f temperature_k]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"time"]]) {
            [data addObject:[f time_millis]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"luminous_flux"]]) {
            [data addObject:[f lux]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"heading_deg"]]) {
            [data addObject:[f angle_deg]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"heading_rad"]]) {
            [data addObject:[f angle_rad]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"latitude"]]) {
            [data addObject:[f latitude]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"longitude"]]) {
            [data addObject:[f longitude]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"magnetic_x"]]) {
            [data addObject:[f mag_x]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"magnetic_y"]]) {
            [data addObject:[f mag_y]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"magnetic_z"]]) {
            [data addObject:[f mag_z]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"magnetic_total"]]) {
            [data addObject:[f mag_total]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"altitude"]]) {
            [data addObject:[f altitude]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"pressure"]]) {
            [data addObject:[f pressure]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"gyroscope_x"]]) {
            [data addObject:[f gyro_x]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"gyroscope_y"]]) {
            [data addObject:[f gyro_y]];
            continue;
        }
        if ([s isEqualToString:[StringGrabber grabField:@"gyroscope_z"]]) {
            [data addObject:[f gyro_z]];
            continue;
        }

        
        [data addObject:[NSNull null]];
    }
    
    return data;    
}

- (void) dealloc {
    [order release];
    [data  release];
    [super dealloc];
}


@end
