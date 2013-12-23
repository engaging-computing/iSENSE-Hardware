//
//  ISenseSearch.m
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 2/5/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "ISenseSearch.h"

@implementation ISenseSearch

@synthesize query, buildType, page, perPage;

- (id) init {
    self = [super init];
    if (self) {
        query = @"";
        buildType = NEW;
        page = 1;
        perPage = 20;
    }
    
    return self;
}

- (id)initWithQuery:(NSString *)search page:(int)pageNumber itemsPerPage:(int)itemsPerPage andBuildType:(BuildType)type {
    self = [self init];
    if (self) {
        query = search;
        buildType = type;
        page = pageNumber;
        perPage = itemsPerPage;
    }
    return self;
}

@end
