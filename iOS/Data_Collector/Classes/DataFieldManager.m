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

- (NSMutableArray *) getFieldOrderOfExperiment:(int)exp {
    
    NSMutableArray *order = [[NSMutableArray alloc] init];
    
    iSENSE *iapi;
    NSMutableArray *fields = [iapi getExperimentFields:[NSNumber numberWithInt:exp]];
    
    for (ExperimentField *field in fields) {
        switch (field.type_id.intValue) {
            // Temperature
            case 1:
                if (!([field.unit_name.lowercaseString rangeOfString:@"f"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"temperature_f"]];
                } else if (!([field.unit_name.lowercaseString rangeOfString:@"c"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"temperature_c"]];
                } else if (!([field.unit_name.lowercaseString rangeOfString:@"k"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"temperature_k"]];
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
            
            // Potential Altitude
            case 2:
            case 3:
                if (!([field.unit_name.lowercaseString rangeOfString:@"altitude"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"altitude"]];
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
            // Time
            case 7:
                [order addObject:[StringGrabber grabField:@"time"]];
                break;
                
            // Light
            case 8:
            case 9:
            case 29:
                [order addObject:[StringGrabber grabField:@"luminous_flux"]];
                break;
                
            // Angle
            case 10:
                if (!([field.unit_name.lowercaseString rangeOfString:@"deg"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"heading_deg"]];
                } else if (!([field.unit_name.lowercaseString rangeOfString:@"rad"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"heading_rad"]];
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
            // Geospacial
            case 19:
                if (!([field.unit_name.lowercaseString rangeOfString:@"lat"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"latitude"]];
                } else if (!([field.unit_name.lowercaseString rangeOfString:@"lon"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"longitude"]];
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
            // Numeric/Custom
            case 21:
            case 22:
                if (!([field.unit_name.lowercaseString rangeOfString:@"mag"].location == NSNotFound)) {
                    if (!([field.unit_name.lowercaseString rangeOfString:@"x"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"magnetic_x"]];
                    } else if (!([field.unit_name.lowercaseString rangeOfString:@"y"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"magnetic_y"]];
                    } else if (!([field.unit_name.lowercaseString rangeOfString:@"z"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"magnetic_z"]];
                    } else {
                        [order addObject:[StringGrabber grabField:@"magnetic_total"]];
                    }
                } else if (!([field.unit_name.lowercaseString rangeOfString:@"altitude"].location == NSNotFound)) {
                    [order addObject:[StringGrabber grabField:@"altitude"]];
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
            // Acceleration
            case 25:
                if (!([field.unit_name.lowercaseString rangeOfString:@"accel"].location == NSNotFound)) {
                    if (!([field.unit_name.lowercaseString rangeOfString:@"x"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"accel_x"]];
                    } else if (!([field.unit_name.lowercaseString rangeOfString:@"y"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"accel_y"]];
                    } else if (!([field.unit_name.lowercaseString rangeOfString:@"z"].location == NSNotFound)) {
                        [order addObject:[StringGrabber grabField:@"accel_z"]];
                    } else {
                        [order addObject:[StringGrabber grabField:@"accel_total"]];
                    }
                } else {
                    [order addObject:[StringGrabber grabField:@"null_string"]];
                }
                break;
                
            // Pressure
            case 27:
                [order addObject:[StringGrabber grabField:@"pressure"]];
                break;
                
            // No match
            default:
                [order addObject:[StringGrabber grabField:@"null_string"]];
                break;
        }
    }
    
    return [order autorelease];
}


- (NSMutableArray *) putData {
    
    NSMutableArray *data = [[NSMutableArray alloc] init];
    
    /*
     
     
     
     JSONArray dataJSON = new JSONArray();
     
     for (String s : this.order) {
     try {
     if (s.equals(mContext.getString(R.string.accel_x))) {
     dataJSON.put(f.accel_x);
     continue;
     }
     if (s.equals(mContext.getString(R.string.accel_y))) {
     dataJSON.put(f.accel_y);
     continue;
     }
     if (s.equals(mContext.getString(R.string.accel_z))) {
     dataJSON.put(f.accel_z);
     continue;
     }
     if (s.equals(mContext.getString(R.string.accel_total))) {
     dataJSON.put(f.accel_total);
     continue;
     }
     if (s.equals(mContext.getString(R.string.temperature_c))) {
     dataJSON.put(f.temperature_c);
     continue;
     }
     if (s.equals(mContext.getString(R.string.temperature_f))) {
     dataJSON.put(f.temperature_f);
     continue;
     }
     if (s.equals(mContext.getString(R.string.temperature_k))) {
     dataJSON.put(f.temperature_k);
     continue;
     }
     if (s.equals(mContext.getString(R.string.time))) {
     dataJSON.put(f.timeMillis);
     continue;
     }
     if (s.equals(mContext.getString(R.string.luminous_flux))) {
     dataJSON.put(f.lux);
     continue;
     }
     if (s.equals(mContext.getString(R.string.heading_deg))) {
     dataJSON.put(f.angle_deg);
     continue;
     }
     if (s.equals(mContext.getString(R.string.heading_rad))) {
     dataJSON.put(f.angle_rad);
     continue;
     }
     if (s.equals(mContext.getString(R.string.latitude))) {
     dataJSON.put(f.latitude);
     continue;
     }
     if (s.equals(mContext.getString(R.string.longitude))) {
     dataJSON.put(f.longitude);
     continue;
     }
     if (s.equals(mContext.getString(R.string.magnetic_x))) {
     dataJSON.put(f.mag_x);
     continue;
     }
     if (s.equals(mContext.getString(R.string.magnetic_y))) {
     dataJSON.put(f.mag_y);
     continue;
     }
     if (s.equals(mContext.getString(R.string.magnetic_z))) {
     dataJSON.put(f.mag_z);
     continue;
     }
     if (s.equals(mContext.getString(R.string.magnetic_total))) {
     dataJSON.put(f.mag_total);
     continue;
     }
     if (s.equals(mContext.getString(R.string.altitude))) {
     dataJSON.put(f.altitude);
     continue;
     }
     if (s.equals(mContext.getString(R.string.pressure))) {
     dataJSON.put(f.pressure);
     continue;
     }
     dataJSON.put(null);
     } catch (JSONException e) {
     e.printStackTrace();
     }
     }
     
     return dataJSON;

     
     
     
     
     
     
     
     */
    
    return [data autorelease];
}


@end
