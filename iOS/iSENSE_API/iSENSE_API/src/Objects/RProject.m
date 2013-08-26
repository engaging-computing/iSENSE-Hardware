//
//  RProject.m
//  iSENSE_API
//
//  Created by Michael Stowell on 8/21/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "RProject.h"

@implementation RProject

@synthesize project_id, featured_media_id, default_read, like_count, hidden, featured, name, url, timecreated, owner_name, owner_url;

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

@end
