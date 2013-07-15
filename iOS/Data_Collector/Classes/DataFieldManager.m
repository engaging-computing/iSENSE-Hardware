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

- (id) init {
    [self falseEnableFields];
    return self;
}

- (void) falseEnableFields {
    enabledFields[0] = enabledFields[1] = enabledFields[2] = enabledFields[3] = enabledFields[4] = enabledFields[5] = enabledFields[6] = enabledFields[7] =
    enabledFields[8] = enabledFields[9] = enabledFields[10] = enabledFields[11] = enabledFields[12] = enabledFields[13] = enabledFields[14] = enabledFields[15] =
    enabledFields[16] = enabledFields[17] = enabledFields[18] = enabledFields[19] = enabledFields[20] = enabledFields[21] = false;
}

- (void) setEnabledField:(bool)value atIndex:(int)index {
    enabledFields[index] = value;
}

- (bool) enabledFieldAtIndex:(int)index {
    return enabledFields[index];
}

- (NSMutableArray *) getFieldOrderOfExperiment:(int)exp {
    
    if (order) [order release];
    order = [[NSMutableArray alloc] init];
    
    iSENSE *iapi = [iSENSE getInstance];
    NSMutableArray *fields = [iapi getExperimentFields:[NSNumber numberWithInt:exp]];
    
    for (ExperimentField *field in fields) {

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
    
    iSENSE *iapi = [iSENSE getInstance];
    if (![iapi isConnectedToInternet] || !order) {
        
        if (!([f accel_x] == nil))
            [data addObject:[f accel_x]];
        else
            [data addObject:@""];
        
        if (!([f accel_y] == nil))
            [data addObject:[f accel_y]];
        else
            [data addObject:@""];
        
        if (!([f accel_z] == nil))
            [data addObject:[f accel_z]];
        else
            [data addObject:@""];
        
        if (!([f accel_total] == nil))
            [data addObject:[f accel_total]];
        else
            [data addObject:@""];
        
        if (!([f temperature_c] == nil))
            [data addObject:[f temperature_c]];
        else
            [data addObject:@""];
        
        if (!([f temperature_f] == nil))
            [data addObject:[f temperature_f]];
        else
            [data addObject:@""];
        
        if (!([f temperature_k] == nil))
            [data addObject:[f temperature_k]];
        else
            [data addObject:@""];
        
        if (!([f time_millis] == nil))
            [data addObject:[f time_millis]];
        else
            [data addObject:@""];
        
        if (!([f lux] == nil))
            [data addObject:[f lux]];
        else
            [data addObject:@""];
        
        if (!([f angle_deg] == nil))
            [data addObject:[f angle_deg]];
        else
            [data addObject:@""];

        if (!([f angle_rad] == nil))
            [data addObject:[f angle_rad]];
        else
            [data addObject:@""];
        
        if (!([f latitude] == nil))
            [data addObject:[f latitude]];
        else
            [data addObject:@""];
        
        if (!([f longitude] == nil))
            [data addObject:[f longitude]];
        else
            [data addObject:@""];
        
        if (!([f mag_x] == nil))
            [data addObject:[f mag_x]];
        else
            [data addObject:@""];

        if (!([f mag_y] == nil))
            [data addObject:[f mag_y]];
        else
            [data addObject:@""];

        if (!([f mag_z] == nil))
            [data addObject:[f mag_z]];
        else
            [data addObject:@""];
        
        if (!([f mag_total] == nil))
            [data addObject:[f mag_total]];
        else
            [data addObject:@""];
        
        if (!([f altitude] == nil))
            [data addObject:[f altitude]];
        else
            [data addObject:@""];
        
        if (!([f pressure] == nil))
            [data addObject:[f pressure]];
        else
            [data addObject:@""];
  
        if (!([f gyro_x] == nil))
            [data addObject:[f gyro_x]];
        else
            [data addObject:@""];
        
        if (!([f gyro_y] == nil))
            [data addObject:[f gyro_y]];
        else
            [data addObject:@""];
        
        if (!([f gyro_z] == nil))
            [data addObject:[f gyro_z]];
        else
            [data addObject:@""];
        
    } else {
        
        for (NSString *s in order) {
            if ([s isEqualToString:[StringGrabber grabField:@"accel_x"]]) {
                if (!([f accel_x] == nil))
                    [data addObject:[f accel_x]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"accel_y"]]) {
                if (!([f accel_y] == nil))
                    [data addObject:[f accel_y]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"accel_z"]]) {
                if (!([f accel_z] == nil))
                    [data addObject:[f accel_z]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"accel_total"]]) {
                if (!([f accel_total] == nil))
                    [data addObject:[f accel_total]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"temperature_c"]]) {
                if (!([f temperature_c] == nil))
                    [data addObject:[f temperature_c]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"temperature_f"]]) {
                if (!([f temperature_f] == nil))
                    [data addObject:[f temperature_f]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"temperature_k"]]) {
                if (!([f temperature_k] == nil))
                    [data addObject:[f temperature_k]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"time"]]) {
                if (!([f time_millis] == nil))
                    [data addObject:[f time_millis]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"luminous_flux"]]) {
                if (!([f lux] == nil))
                    [data addObject:[f lux]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"heading_deg"]]) {
                if (!([f angle_deg] == nil))
                    [data addObject:[f angle_deg]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"heading_rad"]]) {
                if (!([f angle_rad] == nil))
                    [data addObject:[f angle_rad]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"latitude"]]) {
                if (!([f latitude] == nil))
                    [data addObject:[f latitude]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"longitude"]]) {
                if (!([f longitude] == nil))
                    [data addObject:[f longitude]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"magnetic_x"]]) {
                if (!([f mag_x] == nil))
                    [data addObject:[f mag_x]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"magnetic_y"]]) {
                if (!([f mag_y] == nil))
                    [data addObject:[f mag_y]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"magnetic_z"]]) {
                if (!([f mag_z] == nil))
                    [data addObject:[f mag_z]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"magnetic_total"]]) {
                if (!([f mag_total] == nil))
                    [data addObject:[f mag_total]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"altitude"]]) {
                if (!([f altitude] == nil))
                    [data addObject:[f altitude]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"pressure"]]) {
                if (!([f pressure] == nil))
                    [data addObject:[f pressure]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"gyroscope_x"]]) {
                if (!([f gyro_x] == nil))
                    [data addObject:[f gyro_x]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"gyroscope_y"]]) {
                if (!([f gyro_y] == nil))
                    [data addObject:[f gyro_y]];
                else
                    [data addObject:@""];
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"gyroscope_z"]]) {
                if (!([f gyro_z] == nil))
                    [data addObject:[f gyro_z]];
                else
                    [data addObject:@""];
                continue;
            }
            
            [data addObject:@""];
        }
    }

    return data;    
}

- (void) addAllFieldsToOrder {
    [order addObject:[StringGrabber grabField:@"time"]];
    [order addObject:[StringGrabber grabField:@"accel_x"]];
    [order addObject:[StringGrabber grabField:@"accel_y"]];
    [order addObject:[StringGrabber grabField:@"accel_z"]];
    [order addObject:[StringGrabber grabField:@"accel_total"]];
    [order addObject:[StringGrabber grabField:@"latitude"]];
    [order addObject:[StringGrabber grabField:@"longitude"]];
    [order addObject:[StringGrabber grabField:@"magnetic_x"]];
    [order addObject:[StringGrabber grabField:@"magnetic_y"]];
    [order addObject:[StringGrabber grabField:@"magnetic_z"]];
    [order addObject:[StringGrabber grabField:@"magnetic_total"]];
    [order addObject:[StringGrabber grabField:@"heading_deg"]];
    [order addObject:[StringGrabber grabField:@"heading_rad"]];
    [order addObject:[StringGrabber grabField:@"temperature_c"]];
    [order addObject:[StringGrabber grabField:@"pressure"]];
    [order addObject:[StringGrabber grabField:@"altitude"]];
    [order addObject:[StringGrabber grabField:@"luminous_flux"]];
    [order addObject:[StringGrabber grabField:@"gyroscope_x"]];
    [order addObject:[StringGrabber grabField:@"gyroscope_y"]];
    [order addObject:[StringGrabber grabField:@"gyroscope_z"]];
    [order addObject:[StringGrabber grabField:@"temperature_f"]];
    [order addObject:[StringGrabber grabField:@"temperature_k"]];
}

- (void) dealloc {
    [order release];
    [data  release];
    [super dealloc];
}


@end
