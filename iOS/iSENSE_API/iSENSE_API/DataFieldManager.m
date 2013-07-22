//
//  DataFieldManager.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 2/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "DataFieldManager.h"
#import "iSENSE.h"
#import "FieldGrabber.h"

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
    
    if (exp == -1) {
        [self addAllFieldsToOrder];
        return order;
    }
    
    if (order) order = nil;
    order = [[NSMutableArray alloc] init];
    
    iSENSE *iapi = [iSENSE getInstance];
    NSMutableArray *fields = [iapi getExperimentFields:[NSNumber numberWithInt:exp]];
    
    for (ExperimentField *field in fields) {
        
        switch (field.type_id.intValue) {
            // Temperature (1)
            case TEMPERATURE:
                if (!([field.field_name.lowercaseString rangeOfString:@"f"].location == NSNotFound)) {
                    [order addObject:[FieldGrabber grabField:@"temperature_f"]];
                } else if (!([field.field_name.lowercaseString rangeOfString:@"c"].location == NSNotFound)) {
                    [order addObject:[FieldGrabber grabField:@"temperature_c"]];
                } else if (!([field.field_name.lowercaseString rangeOfString:@"k"].location == NSNotFound)) {
                    [order addObject:[FieldGrabber grabField:@"temperature_k"]];
                } else {
                    [order addObject:[FieldGrabber grabField:@"null_string"]];
                }
                break;
                
            // Potential Altitude (2, 3)
            case LENGTH:
            case DISTANCE:
                if (!([field.field_name.lowercaseString rangeOfString:@"altitude"].location == NSNotFound)) {
                    [order addObject:[FieldGrabber grabField:@"altitude"]];
                } else {
                    [order addObject:[FieldGrabber grabField:@"null_string"]];
                }
                break;
                
            // Time (7)
            case TIME:
                [order addObject:[FieldGrabber grabField:@"time"]];
                break;
                
            // Light (8, 9, 29)
            case LUMINOUS_FLUX:
            case LUMINOUS_INTENSITY:
            case LIGHT:
                [order addObject:[FieldGrabber grabField:@"luminous_flux"]];
                break;
                
            // Angle (10)
            case ANGLE:
                if (!([field.field_name.lowercaseString rangeOfString:@"deg"].location == NSNotFound)) {
                    [order addObject:[FieldGrabber grabField:@"heading_deg"]];
                } else if (!([field.field_name.lowercaseString rangeOfString:@"rad"].location == NSNotFound)) {
                    [order addObject:[FieldGrabber grabField:@"heading_rad"]];
                } else {
                    [order addObject:[FieldGrabber grabField:@"null_string"]];
                }
                break;
                
            // Geospacial (19)
            case GEOSPACIAL:
                if (!([field.field_name.lowercaseString rangeOfString:@"lat"].location == NSNotFound)) {
                    [order addObject:[FieldGrabber grabField:@"latitude"]];
                } else if (!([field.field_name.lowercaseString rangeOfString:@"lon"].location == NSNotFound)) {
                    [order addObject:[FieldGrabber grabField:@"longitude"]];
                } else {
                    [order addObject:[FieldGrabber grabField:@"null_string"]];
                }
                break;
                
            // Numeric/Custom (21, 22)
            case NUMERIC:
            case CUSTOM:
                if (!([field.field_name.lowercaseString rangeOfString:@"mag"].location == NSNotFound)) {
                    if (!([field.field_name.lowercaseString rangeOfString:@"x"].location == NSNotFound)) {
                        [order addObject:[FieldGrabber grabField:@"magnetic_x"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"y"].location == NSNotFound)) {
                        [order addObject:[FieldGrabber grabField:@"magnetic_y"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"z"].location == NSNotFound)) {
                        [order addObject:[FieldGrabber grabField:@"magnetic_z"]];
                    } else {
                        [order addObject:[FieldGrabber grabField:@"magnetic_total"]];
                    }
                } else if (!([field.field_name.lowercaseString rangeOfString:@"altitude"].location == NSNotFound)) {
                    [order addObject:[FieldGrabber grabField:@"altitude"]];
                } else if (!([field.field_name.lowercaseString rangeOfString:@"gyro"].location == NSNotFound)
                           || !([field.field_name.lowercaseString rangeOfString:@"rotation"].location == NSNotFound)) {
                    
                    if (!([field.field_name.lowercaseString rangeOfString:@"x"].location == NSNotFound)) {
                        [order addObject:[FieldGrabber grabField:@"gyroscope_x"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"y"].location == NSNotFound)) {
                        [order addObject:[FieldGrabber grabField:@"gyroscope_y"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"z"].location == NSNotFound)) {
                        [order addObject:[FieldGrabber grabField:@"gyroscope_z"]];
                    }
                } else {
                    [order addObject:[FieldGrabber grabField:@"null_string"]];
                }
                break;
                
            // Acceleration (25)
            case ACCELERATION:
                if (!([field.field_name.lowercaseString rangeOfString:@"accel"].location == NSNotFound)) {
                    if (!([field.field_name.lowercaseString rangeOfString:@"x"].location == NSNotFound)) {
                        [order addObject:[FieldGrabber grabField:@"accel_x"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"y"].location == NSNotFound)) {
                        [order addObject:[FieldGrabber grabField:@"accel_y"]];
                    } else if (!([field.field_name.lowercaseString rangeOfString:@"z"].location == NSNotFound)) {
                        [order addObject:[FieldGrabber grabField:@"accel_z"]];
                    } else {
                        [order addObject:[FieldGrabber grabField:@"accel_total"]];
                    }
                } else {
                    [order addObject:[FieldGrabber grabField:@"null_string"]];
                }
                break;
                
            // Pressure (27)
            case PRESSURE:
                [order addObject:[FieldGrabber grabField:@"pressure"]];
                break;
                
            // No match
            default:
                [order addObject:[FieldGrabber grabField:@"null_string"]];
                break;
        }
    }
    
    return order;
}


- (NSMutableArray *) orderDataFromFields:(Fields *)f {
        
    if (data) data = nil;
    data = [[NSMutableArray alloc] init];
    
    NSLog(@"Fields accel_x = %@", [f accel_x]);
    
    for (NSString *s in order) {
        if ([s isEqualToString:[FieldGrabber grabField:@"accel_x"]]) {
            if (!([f accel_x] == nil))
                [data addObject:[f accel_x]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"accel_y"]]) {
            if (!([f accel_y] == nil))
                [data addObject:[f accel_y]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"accel_z"]]) {
            if (!([f accel_z] == nil))
                [data addObject:[f accel_z]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"accel_total"]]) {
            if (!([f accel_total] == nil))
                [data addObject:[f accel_total]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"temperature_c"]]) {
            if (!([f temperature_c] == nil))
                [data addObject:[f temperature_c]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"temperature_f"]]) {
            if (!([f temperature_f] == nil))
                [data addObject:[f temperature_f]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"temperature_k"]]) {
            if (!([f temperature_k] == nil))
                [data addObject:[f temperature_k]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"time"]]) {
            if (!([f time_millis] == nil))
                [data addObject:[f time_millis]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"luminous_flux"]]) {
            if (!([f lux] == nil))
                [data addObject:[f lux]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"heading_deg"]]) {
            if (!([f angle_deg] == nil))
                [data addObject:[f angle_deg]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"heading_rad"]]) {
            if (!([f angle_rad] == nil))
                [data addObject:[f angle_rad]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"latitude"]]) {
            if (!([f latitude] == nil))
                [data addObject:[f latitude]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"longitude"]]) {
            if (!([f longitude] == nil))
                [data addObject:[f longitude]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_x"]]) {
            if (!([f mag_x] == nil))
                [data addObject:[f mag_x]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_y"]]) {
            if (!([f mag_y] == nil))
                [data addObject:[f mag_y]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_z"]]) {
            if (!([f mag_z] == nil))
                [data addObject:[f mag_z]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_total"]]) {
            if (!([f mag_total] == nil))
                [data addObject:[f mag_total]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"altitude"]]) {
            if (!([f altitude] == nil))
                [data addObject:[f altitude]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"pressure"]]) {
            if (!([f pressure] == nil))
                [data addObject:[f pressure]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"gyroscope_x"]]) {
            if (!([f gyro_x] == nil))
                [data addObject:[f gyro_x]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"gyroscope_y"]]) {
            if (!([f gyro_y] == nil))
                [data addObject:[f gyro_y]];
            else
                [data addObject:@""];
            
        } else if ([s isEqualToString:[FieldGrabber grabField:@"gyroscope_z"]]) {
            if (!([f gyro_z] == nil))
                [data addObject:[f gyro_z]];
            else
                [data addObject:@""];
            
        } else {
            [data addObject:@""];
        }
        
    }
    
    return data;
}

- (void) addAllFieldsToOrder {
    if (order) order = nil;
    order = [[NSMutableArray alloc] init];
    
    [order addObject:[FieldGrabber grabField:@"accel_x"]];
    [order addObject:[FieldGrabber grabField:@"accel_y"]];
    [order addObject:[FieldGrabber grabField:@"accel_z"]];
    [order addObject:[FieldGrabber grabField:@"accel_total"]];
    [order addObject:[FieldGrabber grabField:@"temperature_c"]];
    [order addObject:[FieldGrabber grabField:@"temperature_f"]];
    [order addObject:[FieldGrabber grabField:@"temperature_k"]];
    [order addObject:[FieldGrabber grabField:@"time"]];
    [order addObject:[FieldGrabber grabField:@"luminous_flux"]];
    [order addObject:[FieldGrabber grabField:@"heading_deg"]];
    [order addObject:[FieldGrabber grabField:@"heading_rad"]];
    [order addObject:[FieldGrabber grabField:@"latitude"]];
    [order addObject:[FieldGrabber grabField:@"longitude"]];
    [order addObject:[FieldGrabber grabField:@"magnetic_x"]];
    [order addObject:[FieldGrabber grabField:@"magnetic_y"]];
    [order addObject:[FieldGrabber grabField:@"magnetic_z"]];
    [order addObject:[FieldGrabber grabField:@"magnetic_total"]];
    [order addObject:[FieldGrabber grabField:@"altitude"]];
    [order addObject:[FieldGrabber grabField:@"pressure"]];
    [order addObject:[FieldGrabber grabField:@"gyroscope_x"]];
    [order addObject:[FieldGrabber grabField:@"gyroscope_y"]];
    [order addObject:[FieldGrabber grabField:@"gyroscope_z"]];
    
}

- (id) reOrderData:(id)oldData forExperimentID:(int)eid {
    
    NSMutableArray *outData = [[NSMutableArray alloc] init];
    [self getFieldOrderOfExperiment:eid];
    
    for (NSMutableArray *row in oldData) {
        
        NSMutableArray *outRow = [[NSMutableArray alloc] init];
        
        for (NSString *s in order) {
            if ([s isEqualToString:[FieldGrabber grabField:@"accel_x"]])
                [outRow addObject:[row objectAtIndex:fACCEL_X]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"accel_y"]])
                [outRow addObject:[row objectAtIndex:fACCEL_Y]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"accel_z"]])
                [outRow addObject:[row objectAtIndex:fACCEL_Z]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"accel_total"]])
                [outRow addObject:[row objectAtIndex:fACCEL_TOTAL]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"temperature_c"]])
                [outRow addObject:[row objectAtIndex:fTEMPERATURE_C]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"temperature_f"]])
                [outRow addObject:[row objectAtIndex:fTEMPERATURE_F]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"temperature_k"]])
                [outRow addObject:[row objectAtIndex:fTEMPERATURE_K]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"time"]])
                [outRow addObject:[row objectAtIndex:fTIME_MILLIS]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"luminous_flux"]])
                [outRow addObject:[row objectAtIndex:fLUX]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"heading_deg"]])
                [outRow addObject:[row objectAtIndex:fANGLE_DEG]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"heading_rad"]])
                [outRow addObject:[row objectAtIndex:fANGLE_RAD]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"latitude"]])
                [outRow addObject:[row objectAtIndex:fLATITUDE]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"longitude"]])
                [outRow addObject:[row objectAtIndex:fLONGITUDE]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_x"]])
                [outRow addObject:[row objectAtIndex:fMAG_X]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_y"]])
                [outRow addObject:[row objectAtIndex:fMAG_Y]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_z"]])
                [outRow addObject:[row objectAtIndex:fMAG_Z]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_total"]])
                [outRow addObject:[row objectAtIndex:fMAG_TOTAL]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"altitude"]])
                [outRow addObject:[row objectAtIndex:fALTITUDE]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"pressure"]])
                [outRow addObject:[row objectAtIndex:fPRESSURE]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"gyroscope_x"]])
                [outRow addObject:[row objectAtIndex:fGYRO_X]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"gyroscope_y"]])
                [outRow addObject:[row objectAtIndex:fGYRO_Y]];
            else if ([s isEqualToString:[FieldGrabber grabField:@"gyroscope_z"]])
                [outRow addObject:[row objectAtIndex:fGYRO_Z]];
            else
                [outRow addObject:@""];
        }
        
        [outData addObject:outRow];
    }
    
    return outData;
}


@end
