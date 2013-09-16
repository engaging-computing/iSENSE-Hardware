//
//  RDataSet.m
//  iSENSE_API
//
//  Created by Michael Stowell on 8/21/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "RDataSet.h"

@implementation RDataSet

@synthesize ds_id, project_id, hidden, name, url, timecreated, fieldCount, datapointCount, data;

- (id) init {
    if (self = [super init]) {
        hidden = false;
        name = @"";
        url = @"";
        timecreated = @"";
    }
    return self;
}

-(NSString *)description {
    NSString *objString = [NSString stringWithFormat:@"RDataSet: {\n\tds_id: %@\n\tproject_id: %@\n\thidden: %@\n\tname: %@\n\turl: %@\n\ttimecreated:= %@\n\tfieldCount:= %@\n\tdataPointCount:= %@\n\tdata:= %@\n}", ds_id, project_id, hidden, name, url, timecreated, fieldCount, datapointCount, data];
    return objString;
}

@end
