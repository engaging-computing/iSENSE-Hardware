//
//  DataSet.m
//  Data_Collector
//
//  Created by Michael Stowell on 2/5/13.
//
//

#import "DataSet.h"

@implementation DataSet

@synthesize type, name, desc, eid, readyForUpload, data, sid, city, state, country, addr;

- (id) init {
    [super init];
    
    readyForUpload = YES;
    sid = -1;
    city = @"";
    state = @"";
    country = @"";
    addr = @"";
    
    return self;
}

- (void) dealloc {
    
    [name release];
    [desc release];
    [eid release];
    [data release];
    //[picture release];
    [city release];
    [state release];
    [country release];
    [addr release];
    
    [super dealloc];
}

@end
