//
//  DataFieldManager.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 2/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//
#import "iSENSE.h"
#import "StringGrabber.h"
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
    NSLog(@"Hello");
    
    if ([order count] > 0){}
    else
        order = [[NSMutableArray alloc] init];
    
    iSENSE *iapi = [iSENSE getInstance];
    NSMutableArray *fields = [iapi getExperimentFields:[NSNumber numberWithInt:exp]];
    
    for (ExperimentField *field in fields) {
        NSLog(@"Looping");
        switch (field.type_id.intValue) {
                
            case TIME:
                NSLog(@"Time");
                [order addObject:[StringGrabber grabField:@"time"]];
                break;
                
                // Acceleration (25)
            case ACCELERATION:
                NSLog(@"Acceleration");
                if ([field.field_name.lowercaseString rangeOfString:@"accel"].location != NSNotFound) {
                    if ([field.field_name.lowercaseString rangeOfString:@"x"].location != NSNotFound) {
                        [order addObject:[StringGrabber grabField:@"accel_x"]];
                    } else if ([field.field_name.lowercaseString rangeOfString:@"y"].location != NSNotFound) {
                        [order addObject:[StringGrabber grabField:@"accel_y"]];
                    } else if ([field.field_name.lowercaseString rangeOfString:@"z"].location != NSNotFound) {
                        [order addObject:[StringGrabber grabField:@"accel_z"]];
                    } else {
                        [order addObject:[StringGrabber grabField:@"accel_total"]];
                    }
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
                // No match
            default:
                [order addObject:[StringGrabber grabField:@"null_string"]];
                break;
        }
    }
    NSLog(@"World");
    return order;
}


- (NSMutableArray *) orderDataFromFields:(Fields *)f {
    
    if (data)
        data = nil;
    data = [[NSMutableArray alloc] init];
    
    iSENSE *iapi = [iSENSE getInstance];
    if (![iapi isConnectedToInternet] || !order) {
        
        if (!([f accel_x] == nil))
            [data addObject:[f accel_x]];
        else
            [data addObject:@""];
        NSLog(@"X added");
        if (!([f accel_y] == nil))
            [data addObject:[f accel_y]];
        else
            [data addObject:@""];
        NSLog(@"Y added");
        if (!([f accel_z] == nil))
            [data addObject:[f accel_z]];
        else
            [data addObject:@""];
        NSLog(@"Z added");
        if (!([f accel_total] == nil))
            [data addObject:[f accel_total]];
        else
            [data addObject:@""];
        NSLog(@"Total added");
        if (!([f time_millis] == nil))
            [data addObject:[f time_millis]];
        else
            [data addObject:@""];
        NSLog(@"Time added");
    } else {
        
        for (NSString *s in order) {
            if ([s isEqualToString:[StringGrabber grabField:@"accel_x"]]) {
                if (!([f accel_x] == nil))
                    [data addObject:[f accel_x]];
                else
                    [data addObject:@""];
                NSLog(@"X added");
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"accel_y"]]) {
                if (!([f accel_y] == nil))
                    [data addObject:[f accel_y]];
                else
                    [data addObject:@""];
                NSLog(@"Y added");
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"accel_z"]]) {
                if (!([f accel_z] == nil))
                    [data addObject:[f accel_z]];
                else
                    [data addObject:@""];
                NSLog(@"Z added");
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"accel_total"]]) {
                if (!([f accel_total] == nil))
                    [data addObject:[f accel_total]];
                else
                    [data addObject:@""];
                NSLog(@"Total added");
                continue;
            }
            if ([s isEqualToString:[StringGrabber grabField:@"time"]]) {
                if (!([f time_millis] == nil))
                    [data addObject:[f time_millis]];
                else
                    [data addObject:@""];
                NSLog(@"Time added");
                continue;
            }
            
            [data addObject:@""];
        }
    }
    
    return data;
}

- (void) dealloc {
}


@end
