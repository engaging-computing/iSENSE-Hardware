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

@end
