//
//  RPerson.m
//  iSENSE_API
//
//  Created by Michael Stowell on 8/21/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "RPerson.h"

@implementation RPerson

@synthesize person_id, name, username, url, timecreated, gravatar, hidden;

- (id) init {
    if (self = [super init]) {
        name = @"";
        username = @"";
        url = @"";
        timecreated = @"";
        gravatar = @"";
    }
    return self;
}

-(NSString *)description {
    NSString *objString = [NSString stringWithFormat:@"RPerson: {\n\tperson_id: %@\n\tname: %@\n\tusername: %@\n\turl: %@\n\ttimecreated: %@\n\tgravatar:= %@\n\thidden:= %@\n}", person_id, name, username, url, timecreated, gravatar, hidden];
    return objString;
}

@end
