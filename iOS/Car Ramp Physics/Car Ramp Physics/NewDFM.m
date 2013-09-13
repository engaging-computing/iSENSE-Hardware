//
//  DataFieldManager.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 2/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "NewDFM.h"
#import "iSENSE.h"
#import "FieldGrabber.h"

@implementation NewDFM

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

- (NSMutableArray *) getFieldOrderOfProject:(int)projID {
    
    if (projID == -1) {
        [self addAllFieldsToOrder];
        return order;
    }
    
    if (order) order = nil;
    order = [[NSMutableArray alloc] init];
    API *api = [API getInstance];
    dispatch_queue_t queue = dispatch_queue_create("edu.uml.cs.isense.car-ramp-physics", NULL);
    dispatch_async(queue, ^{
        NSArray *fields = [api getProjectFieldsWithId:projID];
        
        for (RProjectField *field in fields) {
            switch ([field.type intValue]) {
                case RProjectField.TYPE_NUMBER:
                    if ([[field.name lowercaseString] rangeOfString:@"temp"].location != NSNotFound) {
                        if ([[field.name lowercaseString] rangeOfString:@"c"].location != NSNotFound) {
                            [order addObject:[FieldGrabber grabField:@"temperature_c"]];
                        } else if ([[field.name lowercaseString] rangeOfString:@"k"].location != NSNotFound) {
                            [order addObject:[FieldGrabber grabField:@"temperature_k"]];
                        } else {
                            [order addObject:[FieldGrabber grabField:@"temperature_f"]];
                        }
                    } else if ([[field.name lowercaseString] rangeOfString:@"altitude"].location != NSNotFound) {
                        [order addObject:[FieldGrabber grabField:@"altitude"]];
                    } else if ([[field.name lowercaseString] rangeOfString:@"light"].location != NSNotFound) {
                        [order addObject:[FieldGrabber grabField:@"luminous_flux"]];
                    } else if ([[field.name lowercaseString] rangeOfString:@"heading"].location != NSNotFound || [[field.name lowercaseString] rangeOfString:@"angle"].location != NSNotFound) {
                        if ([[field.name lowercaseString] rangeOfString:@"rad"].location != NSNotFound) {
                            [order addObject:[FieldGrabber grabField:@"heading_rad"]];
                        } else {
                            [order addObject:[FieldGrabber grabField:@"heading_deg"]];
                        }
                    } else if ([[field.name lowercaseString] rangeOfString:@"magnetic"].location != NSNotFound) {
                        if ([[field.name lowercaseString] rangeOfString:@"x"].location != NSNotFound) {
                            [order addObject:[FieldGrabber grabField:@"magnetic_x"]];
                        } else if ([[field.name lowercaseString] rangeOfString:@"y"].location != NSNotFound) {
                            [order addObject:[FieldGrabber grabField:@"magnetic_y"]];
                        } else if ([[field.name lowercaseString] rangeOfString:@"z"].location != NSNotFound) {
                            [order addObject:[FieldGrabber grabField:@"magnetic_z"]];
                        } else {
                            [order addObject:[FieldGrabber grabField:@"magnetic_total"]];
                        }
                    }  else if ([[field.name lowercaseString] rangeOfString:@"accel"].location != NSNotFound) {
                        if ([[field.name lowercaseString] rangeOfString:@"x"].location != NSNotFound) {
                            [order addObject:[FieldGrabber grabField:@"accel_x"]];
                        } else if ([[field.name lowercaseString] rangeOfString:@"y"].location != NSNotFound) {
                            [order addObject:[FieldGrabber grabField:@"accel_y"]];
                        } else if ([[field.name lowercaseString] rangeOfString:@"z"].location != NSNotFound) {
                            [order addObject:[FieldGrabber grabField:@"accel_z"]];
                        } else {
                            [order addObject:[FieldGrabber grabField:@"accel_total"]];
                        }
                    } else if ([[field.name lowercaseString] rangeOfString:@"pressure"].location != NSNotFound) {
                        [order addObject:[FieldGrabber grabField:@"pressure"]];
                    } else {
                        [order addObject:[FieldGrabber grabField:@"null_string"]];
                    }
                    break;
                case RProjectField.TYPE_TIMESTAMP:
                    [order addObject:[FieldGrabber grabField:@"time"]];
                    break;
                case RProjectField.TYPE_LAT:
                    [order addObject:[FieldGrabber grabField:@"latitude"]];
                    break;
                case RProjectField.TYPE_LON:
                    [order addObject:[FieldGrabber grabField:@"longitude"]];
                    break;
                default:
                    [order addObject:[FieldGrabber grabField:@"null_string"]];
                    break;
            }
        }
    });
    
        
    return order;
}

- (NSMutableDictionary *) putDataFromFields:(Fields *)f {
    
    NSMutableDictionary *dataJSON = [[NSMutableDictionary alloc] init];
    
    for (int i = 0 ; i < order.count ; i++) {
        NSString *s = [order objectAtIndex:i];
        
        @try {
            if ([s isEqualToString:[FieldGrabber grabField:@"accel_x"]]) {
                if (enabledFields[fACCEL_X])
                    [dataJSON setObject:f.accel_x forKey:[NSString stringWithFormat:@"%d", i]];
                else
                   [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"accel_y"]]) {
                if (enabledFields[fACCEL_Y])
                    [dataJSON setObject:f.accel_y forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"accel_z"]]) {
                if (enabledFields[fACCEL_Z])
                    [dataJSON setObject:f.accel_z forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"accel_total"]]) {
                if (enabledFields[fACCEL_TOTAL])
                    [dataJSON setObject:f.accel_total forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"temperature_c"]]) {
                if (enabledFields[fTEMPERATURE_C])
                    [dataJSON setObject:f.temperature_c forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"temperature_f"]]) {
                if (enabledFields[fTEMPERATURE_F])
                    [dataJSON setObject:f.temperature_f forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"temperature_k"]]) {
                if (enabledFields[fTEMPERATURE_K])
                    [dataJSON setObject:f.temperature_k forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"time"]]) {
                if (enabledFields[fTIME_MILLIS])
                    [dataJSON setObject:f.time_millis forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"luminous flux"]]) {
                if (enabledFields[fLUX])
                    [dataJSON setObject:f.lux forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"heading_deg"]]) {
                if (enabledFields[fANGLE_DEG])
                    [dataJSON setObject:f.angle_deg forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"heading_rad"]]) {
                if (enabledFields[fANGLE_RAD])
                    [dataJSON setObject:f.angle_rad forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"latitude"]]) {
                if (enabledFields[fLATITUDE])
                    [dataJSON setObject:f.latitude forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"longitude"]]) {
                if (enabledFields[fLONGITUDE])
                    [dataJSON setObject:f.longitude forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_x"]]) {
                if (enabledFields[fMAG_X])
                    [dataJSON setObject:f.mag_x forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_y"]]) {
                if (enabledFields[fMAG_Y])
                    [dataJSON setObject:f.mag_y forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_z"]]) {
                if (enabledFields[fMAG_Z])
                    [dataJSON setObject:f.mag_z forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_total"]]) {
                if (enabledFields[fMAG_TOTAL])
                    [dataJSON setObject:f.mag_total forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"altitude"]]) {
                if (enabledFields[fALTITUDE])
                    [dataJSON setObject:f.altitude forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
            if ([s isEqualToString:[FieldGrabber grabField:@"pressure"]]) {
                if (enabledFields[fPRESSURE])
                    [dataJSON setObject:f.pressure forKey:[NSString stringWithFormat:@"%d", i]];
                else
                    [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
                continue;
            }
        
        }
        @catch (NSException *exception) {
            //
        }
    
    }

}


- (NSMutableArray *) putDataForNoProjectIDFromFields:(Fields *)f {
    
    if (data) data = nil;
    data = [[NSMutableArray alloc] init];
    
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

- (id) reOrderData:(NSMutableArray*)oldData forProjectID:(int)projID {
    
    NSMutableArray *outData = [[NSMutableArray alloc] init];
    NSMutableDictionary *outRow;
    NSMutableArray *row;
    
    [self getFieldOrderOfProject:projID];

    for (int i = 0; i < [oldData count]; i++) {
        @try {
            row = [oldData objectAtIndex:i];
            outRow = [[NSMutableDictionary alloc] init];
            
            
            for (int j = 0 ; j < order.count ; j++) {
                NSString *s = [order objectAtIndex:j];
                @try {
                    if ([s isEqualToString:[FieldGrabber grabField:@"accel_x"]]) {
                        [outRow setObject:[row objectAtIndex:fACCEL_X] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"accel_y"]]) {
                        [outRow setObject:[row objectAtIndex:fACCEL_Y] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"accel_z"]]) {
                        [outRow setObject:[row objectAtIndex:fACCEL_Z] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"accel_total"]]) {
                        [outRow setObject:[row objectAtIndex:fACCEL_TOTAL] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"temperature_c"]]) {
                        [outRow setObject:[row objectAtIndex:fTEMPERATURE_C] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"temperature_f"]]) {
                        [outRow setObject:[row objectAtIndex:fTEMPERATURE_F] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"temperature_k"]]) {
                        [outRow setObject:[row objectAtIndex:fTEMPERATURE_K] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"time"]]) {
                        [outRow setObject:[row objectAtIndex:fTIME_MILLIS] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"luminous_flux"]]) {
                        [outRow setObject:[row objectAtIndex:fLUX] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"heading_deg"]]) {
                        [outRow setObject:[row objectAtIndex:fANGLE_DEG] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"heading_rad"]]) {
                        [outRow setObject:[row objectAtIndex:fANGLE_RAD] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"latitude"]]) {
                        [outRow setObject:[row objectAtIndex:fLATITUDE] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"longitude"]]) {
                        [outRow setObject:[row objectAtIndex:fLONGITUDE] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_x"]]) {
                        [outRow setObject:[row objectAtIndex:fMAG_X] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_y"]]) {
                        [outRow setObject:[row objectAtIndex:fMAG_Y] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_z"]]) {
                        [outRow setObject:[row objectAtIndex:fMAG_Z] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"magnetic_total"]]) {
                        [outRow setObject:[row objectAtIndex:fMAG_TOTAL] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"altitude"]]) {
                        [outRow setObject:[row objectAtIndex:fALTITUDE] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    if ([s isEqualToString:[FieldGrabber grabField:@"pressure"]]) {
                        [outRow setObject:[row objectAtIndex:fPRESSURE] forKey:[NSString stringWithFormat:@"%d", j]];
                        continue;
                    }
                    [outRow setObject:nil forKey:[NSString stringWithFormat:@"%d", j]];
                }
                @catch (NSException *exception) {
                    //
                }
            }
            
        [outData addObject:outRow];
        }
        @catch (NSException *exception) {
            //
        }
    }
        
    return outData;
}


@end
