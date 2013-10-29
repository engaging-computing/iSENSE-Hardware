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

@synthesize order, data, realOrder;

- (id) init {
    [self disableAllFields];
    return self;
}

- (id) instanceWithProjID:(int)projectID API:(API *)isenseAPI andFields:(Fields *)fields {
    
    projID = projectID;
    api    = isenseAPI;
    f      = fields;
    
    order     = [[NSMutableArray alloc] init];
    realOrder = [[NSMutableArray alloc] init];
    
    return [self init];
}

- (void) getOrder {
    if (order.count != 0)
        return;
    
    if (projID == -1) {
        
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
    
    
    } else {
        projFields = [[api getProjectFieldsWithId:projID] mutableCopy];
    }
    
}

- (void) getProjectFieldOrder {
    
    for (RProjectField *field in projFields) {
        
        [realOrder addObject:field.name];
        
        switch (field.type.intValue) {
            case TYPE_NUMBER:
                
                // Temperature
                if ([field.name.lowercaseString rangeOfString:@"temp"].location != NSNotFound) {
                    if ([field.unit.lowercaseString rangeOfString:@"c"].location != NSNotFound) {
                        [order addObject:sTEMPERATURE_C];
                    } else if ([field.unit.lowercaseString rangeOfString:@"k"].location != NSNotFound) {
                        [order addObject:sTEMPERATURE_K];
                    } else {
                        [order addObject:sTEMPERATURE_F];
                    }
                    break;
                }
                
                // Potential Altitude
                else if ([field.name.lowercaseString rangeOfString:@"altitude"].location != NSNotFound) {
                    [order addObject:sALTITUDE];
                    break;
                }
                
                // Light
                else if ([field.name.lowercaseString rangeOfString:@"light"].location != NSNotFound) {
                    [order addObject:sLUX];
                    break;
                }
                
                // Heading
                else if ([field.name.lowercaseString rangeOfString:@"heading"].location != NSNotFound ||
                         [field.name.lowercaseString rangeOfString:@"angle"].location != NSNotFound) {
                    if ([field.unit.lowercaseString rangeOfString:@"rad"].location != NSNotFound) {
                        [order addObject:sANGLE_RAD];
                    } else {
                        [order addObject:sANGLE_DEG];
                    }
                    break;
                }
                
                // Magnetic
                else if ([field.name.lowercaseString rangeOfString:@"magnetic"].location != NSNotFound) {
                    if ([field.unit.lowercaseString rangeOfString:@"x"].location != NSNotFound) {
                        [order addObject:sMAG_X];
                    } else if ([field.unit.lowercaseString rangeOfString:@"y"].location != NSNotFound) {
                        [order addObject:sMAG_Y];
                    } else if ([field.unit.lowercaseString rangeOfString:@"z"].location != NSNotFound) {
                        [order addObject:sMAG_Z];
                    } else {
                        [order addObject:sMAG_TOTAL];
                    }
                    break;
                }
                
                // Acceleration
                else if ([field.name.lowercaseString rangeOfString:@"accel"].location != NSNotFound) {
                    if ([field.unit.lowercaseString rangeOfString:@"x"].location != NSNotFound) {
                        [order addObject:sACCEL_X];
                    } else if ([field.unit.lowercaseString rangeOfString:@"y"].location != NSNotFound) {
                        [order addObject:sACCEL_Y];
                    } else if ([field.unit.lowercaseString rangeOfString:@"z"].location != NSNotFound) {
                        [order addObject:sACCEL_Z];
                    } else {
                        [order addObject:sACCEL_TOTAL];
                    }
                    break;
                }
                
                // Pressure
                else if ([field.name.lowercaseString rangeOfString:@"pressure"].location != NSNotFound) {
                    [order addObject:sPRESSURE];
                    break;
                }
                
                // Gyroscope
                else if ([field.name.lowercaseString rangeOfString:@"gyro"].location != NSNotFound) {
                    if ([field.unit.lowercaseString rangeOfString:@"x"].location != NSNotFound) {
                        [order addObject:sGYRO_X];
                    } else if ([field.unit.lowercaseString rangeOfString:@"y"].location != NSNotFound) {
                        [order addObject:sGYRO_Y];
                    } else {
                        [order addObject:sGYRO_Z];
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
                break;
                
            case TYPE_LAT:
                
                [order addObject:sLATITUDE];
                break;
                
            case TYPE_LON:
                
                [order addObject:sLONGITUDE];
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

- (Fields *) getFields {
    return f;
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
        else if ([s isEqualToString:fMAG_TOTAL])
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

- (NSMutableDictionary *) putData {
    
    NSMutableDictionary *dataJSON = [[NSMutableDictionary alloc] init];
    
    for (int i = 0; i < order.count; i++) {
        
        NSString *s = [order objectAtIndex:i];
        
        if ([s isEqualToString:sACCEL_X]) {
            if (enabledFields[fACCEL_X])
                [dataJSON setObject:f.accel_x forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sACCEL_Y]) {
            if (enabledFields[fACCEL_Y])
                [dataJSON setObject:f.accel_y forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sACCEL_Z]) {
            if (enabledFields[fACCEL_Z])
                [dataJSON setObject:f.accel_z forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sACCEL_TOTAL]) {
            if (enabledFields[fACCEL_TOTAL])
                [dataJSON setObject:f.accel_total forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sTEMPERATURE_C]) {
            if (enabledFields[fTEMPERATURE_C])
                [dataJSON setObject:f.temperature_c forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sTEMPERATURE_F]) {
            if (enabledFields[fTEMPERATURE_F])
                [dataJSON setObject:f.temperature_f forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sTEMPERATURE_K]) {
            if (enabledFields[fTEMPERATURE_K])
                [dataJSON setObject:f.temperature_k forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sTIME_MILLIS]) {
            if (enabledFields[fTIME_MILLIS])
                [dataJSON setObject:f.time_millis forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sLUX]) {
            if (enabledFields[fLUX])
                [dataJSON setObject:f.lux forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sANGLE_DEG]) {
            if (enabledFields[fANGLE_DEG])
                [dataJSON setObject:f.angle_deg forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sANGLE_RAD]) {
            if (enabledFields[fANGLE_RAD])
                [dataJSON setObject:f.angle_rad forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sLATITUDE]) {
            if (enabledFields[fLATITUDE])
                [dataJSON setObject:f.latitude forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sLONGITUDE]) {
            if (enabledFields[fLONGITUDE])
                [dataJSON setObject:f.longitude forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sMAG_X]) {
            if (enabledFields[fMAG_X])
                [dataJSON setObject:f.mag_x forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sMAG_Y]) {
            if (enabledFields[fMAG_Y])
                [dataJSON setObject:f.mag_y forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sMAG_Z]) {
            if (enabledFields[fMAG_Z])
                [dataJSON setObject:f.mag_z forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sMAG_TOTAL]) {
            if (enabledFields[fMAG_TOTAL])
                [dataJSON setObject:f.mag_total forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sALTITUDE]) {
            if (enabledFields[fALTITUDE])
                [dataJSON setObject:f.altitude forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sPRESSURE]) {
            if (enabledFields[fPRESSURE])
                [dataJSON setObject:f.pressure forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sGYRO_X]) {
            if (enabledFields[fGYRO_X])
                [dataJSON setObject:f.gyro_x forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sGYRO_Y]) {
            if (enabledFields[fGYRO_Y])
                [dataJSON setObject:f.gyro_y forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        if ([s isEqualToString:sGYRO_Z]) {
            if (enabledFields[fGYRO_Z])
                [dataJSON setObject:f.gyro_z forKey:[NSString stringWithFormat:@"%d", i]];
            else
                [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
            continue;
        }
        
        [dataJSON setObject:@"" forKey:[NSString stringWithFormat:@"%d", i]];
        
    }
    
    NSLog(@"Data line: %@", dataJSON);
    
    return dataJSON;
}

- (NSMutableArray *) putDataForNoProjectID {
    
    NSMutableArray *dataJSON = [[NSMutableArray alloc] init];
    
    if (enabledFields[fACCEL_X])
        [dataJSON addObject:f.accel_x];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fACCEL_Y])
        [dataJSON addObject:f.accel_y];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fACCEL_Z])
        [dataJSON addObject:f.accel_z];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fACCEL_TOTAL])
        [dataJSON addObject:f.accel_total];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fTEMPERATURE_C])
        [dataJSON addObject:f.temperature_c];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fTEMPERATURE_F])
        [dataJSON addObject:f.temperature_f];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fTEMPERATURE_K])
        [dataJSON addObject:f.temperature_k];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fTIME_MILLIS])
        [dataJSON addObject:f.time_millis];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fLUX])
        [dataJSON addObject:f.lux];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fANGLE_DEG])
        [dataJSON addObject:f.angle_deg];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fANGLE_RAD])
        [dataJSON addObject:f.angle_rad];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fLATITUDE])
        [dataJSON addObject:f.latitude];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fLONGITUDE])
        [dataJSON addObject:f.longitude];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fMAG_X])
        [dataJSON addObject:f.mag_x];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fMAG_Y])
        [dataJSON addObject:f.mag_y];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fMAG_Z])
        [dataJSON addObject:f.mag_z];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fMAG_TOTAL])
        [dataJSON addObject:f.mag_total];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fALTITUDE])
        [dataJSON addObject:f.altitude];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fPRESSURE])
        [dataJSON addObject:f.pressure];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fGYRO_X])
        [dataJSON addObject:f.gyro_x];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fGYRO_Y])
        [dataJSON addObject:f.gyro_y];
    else
        [dataJSON addObject:@""];
    if (enabledFields[fGYRO_Z])
        [dataJSON addObject:f.gyro_z];
    else
        [dataJSON addObject:@""];

    NSLog(@"Data line: %@", dataJSON);
    
    return dataJSON;
    
}

+ (NSMutableArray *) reOrderData:(NSMutableArray *)data forProjectID:(int)projectID API:(API *)api andFieldOrder:(NSMutableArray *)fieldOrder {
    
    NSMutableArray *row     = [[NSMutableArray alloc] init];
    NSMutableArray *outData = [[NSMutableArray alloc] init];
    NSMutableDictionary *outRow;
    
    int len = data.count;
    if (fieldOrder == nil || fieldOrder.count == 0)
        ; // TODO: getOrder(proj, api, c);
    
    for (int i = 0; i < len; i++) {
        // TODO - this stuff
    }
        
    return outData;
}


/******************************************************************/

- (void) setEnabledField:(bool)value atIndex:(int)index {
    enabledFields[index] = value;
}

- (bool) enabledFieldAtIndex:(int)index {
    return enabledFields[index];
}

- (id) reOrderData:(id)oldData forExperimentID:(int)eid {
    
    NSMutableArray *outData = [[NSMutableArray alloc] init];
    //[self getFieldOrderOfExperiment:eid];
    
    for (NSMutableArray *row in oldData) {
        
        NSLog(@"%@", row.description);
        
        NSMutableArray *outRow = [[NSMutableArray alloc] init];
        
        for (NSString *s in order) {
            NSLog(@"World");
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
            NSLog(@"NULLFROGS");
        }
        
        
        [outData addObject:outRow];
    }
    
    return outData;
}


@end
