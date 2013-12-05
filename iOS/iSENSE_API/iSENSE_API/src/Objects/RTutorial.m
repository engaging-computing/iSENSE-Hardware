//
//  RTutorial.m
//  iSENSE_API
//
//  Created by Michael Stowell on 8/21/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "RTutorial.h"

@implementation RTutorial

@synthesize tutorial_id, hidden, name, url, timecreated, owner_name, owner_url;

- (id) init {
    if (self = [super init]) {
        name = @"";
        url = @"";
        timecreated = @"";
        owner_name = @"";
        owner_url = @"";
    }
    return self;
}

-(NSString *)description {
    
    NSString *objString = [NSString stringWithFormat:@"RTutorial: {\n\ttutorial_id: %@\n\tname: %@\n\turl: %@\n\ttimecreated: %@\n\thidden: %@\n\towner_name: %@\n\towner_url: %@\n}", tutorial_id, name, url, timecreated, hidden, owner_name, owner_url];
    return objString;
}

@end
