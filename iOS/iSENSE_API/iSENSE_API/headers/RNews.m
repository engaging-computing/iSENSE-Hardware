//
//  RNews.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 9/19/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "RNews.h"

@implementation RNews
@synthesize url, timecreated, hidden, name, news_id, content;

- (id) init {
    if (self = [super init]) {
        name = @"";
        url = @"";
        timecreated = @"";
        content = @"";
    }
    return self;
}

-(NSString *)description {
    NSString *objString = [NSString stringWithFormat:@"RNews: {\n\tnews_id: %@\n\tname: %@\n\turl: %@\n\ttimecreated: %@\n\thidden: %@\n\tcontent: %@\n}", news_id, name, url, timecreated, hidden, content];
    return objString;
}

@end
