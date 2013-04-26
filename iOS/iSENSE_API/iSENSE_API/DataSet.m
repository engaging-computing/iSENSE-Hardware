//
//  Dataset.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#include "DataSet.h"

@implementation DataSet

@synthesize name_, description_, eid_, picturePaths_, data_, city_, country_, state_, address_, uploadable_;


-(id) init {
    self = [super init];
    if (self) {
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

- (id) initWithName:(NSString *)name {
    self = [self init];
    if (self) {
        name_ = name;
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

- (id) initWithName:(NSString *)name andDescription:(NSString *)description {
    self = [self initWithName:name];
    if (self) {
        description_ = description;
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid {
    self = [self initWithName:name andDescription:description];
    if (self) {
        eid_ = eid;
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

-(BOOL) canBeUploaded {
    return true;
}

@end