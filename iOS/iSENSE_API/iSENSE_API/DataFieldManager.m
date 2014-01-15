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

@synthesize order, realOrder, fieldIDs;

- (id) initWithProjID:(int)projectID API:(API *)isenseAPI andFields:(Fields *)fields {
 
    self = [super init];
    if (!self) return nil;
    
    projID = projectID;
    api    = isenseAPI;
    f      = fields;
    
    [self disableAllFields];
    
    order     = [[NSMutableArray alloc] init];
    realOrder = [[NSMutableArray alloc] init];
    fieldIDs  = [[NSMutableArray alloc] init];
    
    return self;
}

- (void) getOrder {
    if (order.count != 0)
        return;
    
    if (projID == -1) {
        [order removeAllObjects];
        
        [order addObject:sACCEL_X];
        [order addObject:sACCEL_Y];
        [order addObject:sACCEL_Z];
        [order addObject:sACCEL_TOTAL];
        [order addObject:sTEMPERATURE_C];
        [order addObject:sTEMPERATURE_F];
        [order addObject:sTEMPERATURE_K];
        [order addObject:sTIME_MILLIS];
        [order addObject:sLUX];
        [order addObject:sANGLE_DEG];
        [order addObject:sANGLE_RAD];
        [order addObject:sLATITUDE];
        [order addObject:sLONGITUDE];
        [order addObject:sMAG_X];
        [order addObject:sMAG_Y];
        [order addObject:sMAG_Z];
        [order addObject:sMAG_TOTAL];
        [order addObject:sALTITUDE];
        [order addObject:sPRESSURE];
        [order addObject:sGYRO_X];
        [order addObject:sGYRO_Y];
        [order addObject:sGYRO_Z];
    
        [self enableAllFields];
    
    } else {
        projFields = [[api getProjectFieldsWithId:projID] mutableCopy];
        [self getProjectFieldOrder];
    }
    
}

- (void) getProjectFieldOrder {
    
    order = [[NSMutableArray alloc] init];
    realOrder = [[NSMutableArray alloc] init];
    fieldIDs = [[NSMutableArray alloc] init];
    
    for (RProjectField *field in projFields) {
        
        [realOrder addObject:field.name];
        [fieldIDs  addObject:field.field_id];
        
        switch (field.type.intValue) {
            case TYPE_NUMBER: // TODO - should we enable fields here too?
                
                // Temperature
                if ([field.name.lowercaseString rangeOfString:@"temp"].location != NSNotFound) {
                    if ([field.unit.lowercaseString rangeOfString:@"c"].location != NSNotFound) {
                        [order addObject:sTEMPERATURE_C];
                        enabledFields[fTEMPERATURE_C] = true;
                    } else if ([field.unit.lowercaseString rangeOfString:@"k"].location != NSNotFound) {
                        [order addObject:sTEMPERATURE_K];
                        enabledFields[fTEMPERATURE_K] = true;
                    } else {
                        [order addObject:sTEMPERATURE_F];
                        enabledFields[fTEMPERATURE_F] = true;
                    }
                    break;
                }
                
                // Potential Altitude
                else if ([field.name.lowercaseString rangeOfString:@"altitude"].location != NSNotFound) {
                    [order addObject:sALTITUDE];
                    enabledFields[fALTITUDE] = true;
                    break;
                }
                
                // Light
                else if ([field.name.lowercaseString rangeOfString:@"light"].location != NSNotFound) {
                    [order addObject:sLUX];
                    enabledFields[fLUX] = true;
                    break;
                }
                
                // Heading
                else if ([field.name.lowercaseString rangeOfString:@"heading"].location != NSNotFound ||
                         [field.name.lowercaseString rangeOfString:@"angle"].location != NSNotFound) {
                    if ([field.unit.lowercaseString rangeOfString:@"rad"].location != NSNotFound) {
                        [order addObject:sANGLE_RAD];
                        enabledFields[fANGLE_RAD] = true;
                    } else {
                        [order addObject:sANGLE_DEG];
                        enabledFields[fANGLE_DEG] = true;
                    }
                    break;
                }
                
                // Magnetic
                else if ([field.name.lowercaseString rangeOfString:@"magnetic"].location != NSNotFound) {
                    if ([field.name.lowercaseString rangeOfString:@"x"].location != NSNotFound) {
                        [order addObject:sMAG_X];
                        enabledFields[fMAG_X] = true;
                    } else if ([field.name.lowercaseString rangeOfString:@"y"].location != NSNotFound) {
                        [order addObject:sMAG_Y];
                        enabledFields[fMAG_Y] = true;
                    } else if ([field.name.lowercaseString rangeOfString:@"z"].location != NSNotFound) {
                        [order addObject:sMAG_Z];
                        enabledFields[fMAG_Z] = true;
                    } else {
                        [order addObject:sMAG_TOTAL];
                        enabledFields[fMAG_TOTAL] = true;
                    }
                    break;
                }
                
                // Acceleration
                else if ([field.name.lowercaseString rangeOfString:@"accel"].location != NSNotFound) {
                    if ([field.name.lowercaseString rangeOfString:@"x"].location != NSNotFound) {
                        [order addObject:sACCEL_X];
                        enabledFields[fACCEL_X] = true;
                    } else if ([field.name.lowercaseString rangeOfString:@"y"].location != NSNotFound) {
                        [order addObject:sACCEL_Y];
                        enabledFields[fACCEL_Y] = true;
                    } else if ([field.name.lowercaseString rangeOfString:@"z"].location != NSNotFound) {
                        [order addObject:sACCEL_Z];
                        enabledFields[fACCEL_Z] = true;
                    } else {
                        [order addObject:sACCEL_TOTAL];
                        enabledFields[fACCEL_TOTAL] = true;
                    }
                    break;
                }
                
                // Pressure
                else if ([field.name.lowercaseString rangeOfString:@"pressure"].location != NSNotFound) {
                    [order addObject:sPRESSURE];
                    enabledFields[fPRESSURE] = true;
                    break;
                }
                
                // Gyroscope
                else if ([field.name.lowercaseString rangeOfString:@"gyro"].location != NSNotFound) {
                    if ([field.name.lowercaseString rangeOfString:@"x"].location != NSNotFound) {
                        [order addObject:sGYRO_X];
                        enabledFields[fGYRO_X] = true;
                    } else if ([field.name.lowercaseString rangeOfString:@"y"].location != NSNotFound) {
                        [order addObject:sGYRO_Y];
                        enabledFields[fGYRO_Y] = true;
                    } else {
                        [order addObject:sGYRO_Z];
                        enabledFields[fGYRO_Z] = true;
                    }
                    break;
                }
                
                // No match found
                else {
                    [order addObject:sNULL_STRING];
                    break;
                }
                
                break;
                
            case TYPE_TIMESTAMP:
                
                [order addObject:sTIME_MILLIS];
                enabledFields[fTIME_MILLIS] = true;
                break;
                
            case TYPE_LAT:
                
                [order addObject:sLATITUDE];
                enabledFields[fLATITUDE] = true;
                break;
                
            case TYPE_LON:
                
                [order addObject:sLONGITUDE];
                enabledFields[fLONGITUDE] = true;
                break;
                
            default:
                
                [order addObject:sNULL_STRING];
                break;
        }
        
    }
    
}

- (int) getProjID {
    return projID;
}

- (void) setProjID:(int)projectID {
    projID = projectID;
}

- (NSMutableArray *) getProjectFields {
    return projFields;
}

- (NSMutableArray *) getOrderList {
    return order;
}

- (NSMutableArray *) getRealOrder {
    return realOrder;
}

- (Fields *) getFields {
    return f;
}

- (void) setFields:(Fields *)fields {
    f = fields;
}

- (void) enableAllFields {
    enabledFields[0] = enabledFields[1] = enabledFields[2] = enabledFields[3] = enabledFields[4] = enabledFields[5] = enabledFields[6] = enabledFields[7] =
    enabledFields[8] = enabledFields[9] = enabledFields[10] = enabledFields[11] = enabledFields[12] = enabledFields[13] = enabledFields[14] = enabledFields[15] =
    enabledFields[16] = enabledFields[17] = enabledFields[18] = enabledFields[19] = enabledFields[20] = enabledFields[21] = true;
}

- (void) disableAllFields {
    enabledFields[0] = enabledFields[1] = enabledFields[2] = enabledFields[3] = enabledFields[4] = enabledFields[5] = enabledFields[6] = enabledFields[7] =
    enabledFields[8] = enabledFields[9] = enabledFields[10] = enabledFields[11] = enabledFields[12] = enabledFields[13] = enabledFields[14] = enabledFields[15] =
    enabledFields[16] = enabledFields[17] = enabledFields[18] = enabledFields[19] = enabledFields[20] = enabledFields[21] = false;
}

- (void) setEnabledFields:(NSMutableArray *)acceptedFields {
    for (NSString *s in acceptedFields) {
        if ([s isEqualToString:sACCEL_X])
            enabledFields[fACCEL_X] = true;
        else if ([s isEqualToString:sACCEL_Y])
            enabledFields[fACCEL_Y] = true;
        else if ([s isEqualToString:sACCEL_Z])
            enabledFields[fACCEL_Z] = true;
        else if ([s isEqualToString:sACCEL_TOTAL])
            enabledFields[fACCEL_TOTAL] = true;
        else if ([s isEqualToString:sTEMPERATURE_C])
            enabledFields[fTEMPERATURE_C] = true;
        else if ([s isEqualToString:sTEMPERATURE_F])
            enabledFields[fTEMPERATURE_F] = true;
        else if ([s isEqualToString:sTEMPERATURE_K])
            enabledFields[fTEMPERATURE_K] = true;
        else if ([s isEqualToString:sTIME_MILLIS])
            enabledFields[fTIME_MILLIS] = true;
        else if ([s isEqualToString:sLUX])
            enabledFields[fLUX] = true;
        else if ([s isEqualToString:sANGLE_DEG])
            enabledFields[fANGLE_DEG] = true;
        else if ([s isEqualToString:sANGLE_RAD])
            enabledFields[fANGLE_RAD] = true;
        else if ([s isEqualToString:sLATITUDE])
            enabledFields[fLATITUDE] = true;
        else if ([s isEqualToString:sLONGITUDE])
            enabledFields[fLONGITUDE] = true;
        else if ([s isEqualToString:sMAG_X])
            enabledFields[fMAG_X] = true;
        else if ([s isEqualToString:sMAG_Y])
            enabledFields[fMAG_Y] = true;
        else if ([s isEqualToString:sMAG_Z])
            enabledFields[fMAG_Z] = true;
        else if ([s isEqualToString:sMAG_TOTAL])
            enabledFields[fMAG_TOTAL] = true;
        else if ([s isEqualToString:sALTITUDE])
            enabledFields[fALTITUDE] = true;
        else if ([s isEqualToString:sPRESSURE])
            enabledFields[fPRESSURE] = true;
        else if ([s isEqualToString:sGYRO_X])
            enabledFields[fGYRO_X] = true;
        else if ([s isEqualToString:sGYRO_Y])
            enabledFields[fGYRO_Y] = true;
        else if ([s isEqualToString:sGYRO_Z])
            enabledFields[fGYRO_Z] = true;
    }
}

- (NSMutableArray *) putData {
    
    NSMutableArray *dataJSON = [[NSMutableArray alloc] init];
    
    if (enabledFields[fACCEL_X])
        [dataJSON addObject:(f.accel_x == nil) ? @"" : [NSString stringWithFormat:@"%@", f.accel_x]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fACCEL_Y])
        [dataJSON addObject:(f.accel_y == nil) ? @"" : [NSString stringWithFormat:@"%@", f.accel_y]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fACCEL_Z])
        [dataJSON addObject:(f.accel_z == nil) ? @"" : [NSString stringWithFormat:@"%@", f.accel_z]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fACCEL_TOTAL])
        [dataJSON addObject:(f.accel_total == nil) ? @"" : [NSString stringWithFormat:@"%@", f.accel_total]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fTEMPERATURE_C])
        [dataJSON addObject:(f.temperature_c == nil) ? @"" : [NSString stringWithFormat:@"%@", f.temperature_c]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fTEMPERATURE_F])
        [dataJSON addObject:(f.temperature_f == nil) ? @"" : [NSString stringWithFormat:@"%@", f.temperature_f]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fTEMPERATURE_K])
        [dataJSON addObject:(f.temperature_k == nil) ? @"" : [NSString stringWithFormat:@"%@", f.temperature_k]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fTIME_MILLIS])
        [dataJSON addObject:(f.time_millis == nil) ? @"" : [NSString stringWithFormat:@"%@", f.time_millis]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fLUX])
        [dataJSON addObject:(f.lux == nil) ? @"" : [NSString stringWithFormat:@"%@", f.lux]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fANGLE_DEG])
        [dataJSON addObject:(f.angle_deg == nil) ? @"" : [NSString stringWithFormat:@"%@", f.angle_deg]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fANGLE_RAD])
        [dataJSON addObject:(f.angle_rad == nil) ? @"" : [NSString stringWithFormat:@"%@", f.angle_rad]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fLATITUDE])
        [dataJSON addObject:(f.latitude == nil) ? @"" : [NSString stringWithFormat:@"%@", f.latitude]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fLONGITUDE])
        [dataJSON addObject:(f.longitude == nil) ? @"" : [NSString stringWithFormat:@"%@", f.longitude]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fMAG_X])
        [dataJSON addObject:(f.mag_x == nil) ? @"" : [NSString stringWithFormat:@"%@", f.mag_x]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fMAG_Y])
        [dataJSON addObject:(f.mag_y == nil) ? @"" : [NSString stringWithFormat:@"%@", f.mag_y]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fMAG_Z])
        [dataJSON addObject:(f.mag_z == nil) ? @"" : [NSString stringWithFormat:@"%@", f.mag_z]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fMAG_TOTAL])
        [dataJSON addObject:(f.mag_total == nil) ? @"" : [NSString stringWithFormat:@"%@", f.mag_total]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fALTITUDE])
        [dataJSON addObject:(f.altitude == nil) ? @"" : [NSString stringWithFormat:@"%@", f.altitude]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fPRESSURE])
        [dataJSON addObject:(f.pressure == nil) ? @"" : [NSString stringWithFormat:@"%@", f.pressure]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fGYRO_X])
        [dataJSON addObject:(f.gyro_x == nil) ? @"" : [NSString stringWithFormat:@"%@", f.gyro_x]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fGYRO_Y])
        [dataJSON addObject:(f.gyro_y == nil) ? @"" : [NSString stringWithFormat:@"%@", f.gyro_y]];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fGYRO_Z])
        [dataJSON addObject:(f.gyro_z == nil) ? @"" : [NSString stringWithFormat:@"%@", f.gyro_z]];
    else
        [dataJSON addObject:@""];

    // log the data line
    NSString *dataLog = [[NSString alloc] initWithFormat:@"%@", dataJSON];
    dataLog = [dataLog stringByReplacingOccurrencesOfString:@"\n" withString:@""];
    dataLog = [dataLog stringByReplacingOccurrencesOfString:@" " withString:@""];
    NSLog(@"Data line: %@\n", dataLog);
    
    return dataJSON;
    
}

+ (NSMutableArray *) reOrderData:(NSMutableArray *)data forProjectID:(int)projectID withFieldOrder:(NSMutableArray *)fieldOrder andFieldIDs:(NSMutableArray *)ids {
    
    API *api = [API getInstance];
    
    NSMutableArray *row     = [[NSMutableArray alloc] init];
    NSMutableArray *outData = [[NSMutableArray alloc] init];
    NSMutableDictionary *outRow;
    int len = data.count;
    
    // if the field order is null, set up the fieldOrder/fieldIDs.  otherwise, just get fieldIDs
    if (fieldOrder == nil || fieldOrder.count == 0) {
        DataFieldManager *d = [[DataFieldManager alloc] initWithProjID:projectID API:api andFields:nil];
        [d getOrder];
        fieldOrder = [d getOrderList];
        ids = [d getFieldIDs];
    } else if (ids == nil || ids.count == 0) {
        DataFieldManager *d = [[DataFieldManager alloc] initWithProjID:projectID API:api andFields:nil];
        [d getOrder];
        ids = [d getFieldIDs];
    }
    
    // reorder the data
    for (int i = 0; i < len; i++) {
        
        row = [data objectAtIndex:i];
        outRow = [[NSMutableDictionary alloc] init];
        
        for (int j = 0; j < fieldOrder.count; j++) {
            
            NSString *s = [fieldOrder objectAtIndex:j];
            NSNumber *idField = [ids objectAtIndex:j];
            
            if ([s isEqualToString:sACCEL_X]) {
                [outRow setObject:[row objectAtIndex:fACCEL_X] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sACCEL_Y]) {
                [outRow setObject:[row objectAtIndex:fACCEL_Y] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sACCEL_Z]) {
                [outRow setObject:[row objectAtIndex:fACCEL_Z] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sACCEL_TOTAL]) {
                [outRow setObject:[row objectAtIndex:fACCEL_TOTAL] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sTEMPERATURE_C]) {
                [outRow setObject:[row objectAtIndex:fTEMPERATURE_C] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sTEMPERATURE_F]) {
                [outRow setObject:[row objectAtIndex:fTEMPERATURE_F] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sTEMPERATURE_K]) {
                [outRow setObject:[row objectAtIndex:fTEMPERATURE_K] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sTIME_MILLIS]) {
                [outRow setObject:[NSString stringWithFormat:@"u %@",[row objectAtIndex:fTIME_MILLIS]] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sLUX]) {
                [outRow setObject:[row objectAtIndex:fLUX] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sANGLE_DEG]) {
                [outRow setObject:[row objectAtIndex:fANGLE_DEG] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sANGLE_RAD]) {
                [outRow setObject:[row objectAtIndex:fANGLE_RAD] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sLATITUDE]) {
                [outRow setObject:[row objectAtIndex:fLATITUDE] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sLONGITUDE]) {
                [outRow setObject:[row objectAtIndex:fLONGITUDE] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sMAG_X]) {
                [outRow setObject:[row objectAtIndex:fMAG_X] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sMAG_Y]) {
                [outRow setObject:[row objectAtIndex:fMAG_Y] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sMAG_Z]) {
                [outRow setObject:[row objectAtIndex:fMAG_Z] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sMAG_TOTAL]) {
                [outRow setObject:[row objectAtIndex:fMAG_TOTAL] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sALTITUDE]) {
                [outRow setObject:[row objectAtIndex:fALTITUDE] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sPRESSURE]) {
                [outRow setObject:[row objectAtIndex:fPRESSURE] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sGYRO_X]) {
                [outRow setObject:[row objectAtIndex:fGYRO_X] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sGYRO_Y]) {
                [outRow setObject:[row objectAtIndex:fGYRO_Y] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            if ([s isEqualToString:sGYRO_Z]) {
                [outRow setObject:[row objectAtIndex:fGYRO_Z] forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
                continue;
            }
            
            [outRow setObject:@"" forKey:[NSString stringWithFormat:@"%ld", idField.longValue]];
            
        }
        
        [outData addObject:outRow];
        
    }
        
    return outData;
}

- (void) setOrder:(NSMutableArray *)newOrderFields {
    if (order != nil)
        [order removeAllObjects];
    
    order = [[NSMutableArray alloc] initWithArray:newOrderFields];
}

- (void) setEnabledField:(bool)value atIndex:(int)index {
    enabledFields[index] = value;
}

- (bool) enabledFieldAtIndex:(int)index {
    return enabledFields[index];
}

- (NSMutableArray *) getFieldIDs {
    return fieldIDs;
}


@end
