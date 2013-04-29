//
//  Dataset.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#include "DataSet.h"

@implementation DataSet

@synthesize name_, description_, eid_, sid_, picturePaths_, data_, city_, country_, state_, address_, uploadable_;


-(id) init {
    self = [super init];
    if (self) {
        name_ = @"";
        description_ = @"Session data gathered and uploaded from mobile phone using iSENSE DataCollector application.";
        eid_ = 0;
        data_ = [data_ init];
        picturePaths_ = [picturePaths_ init];
        city_ = @"";
        country_ = @"";
        state_ = @"";
        address_ = @"";
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

- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data {
    self = [self initWithName:name andDescription:description andEID:eid];
    if (self) {
        data_ = data;
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths {
    self = [self initWithName:name andDescription:description andEID:eid andData:data];
    if (self) {
        picturePaths_ = picturePaths;
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths andSessionId:(int)sid {
    self = [self initWithName:name andDescription:description andEID:eid andData:data andPicturePaths:picturePaths];
    if (self) {
        sid_ = sid;
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths andSessionId:(int)sid andCity:(NSString *)city {
    self = [self initWithName:name andDescription:description andEID:eid andData:data andPicturePaths:picturePaths andSessionId:sid];
    if (self) {
        city_ = city;
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths andSessionId:(int)sid andCity:(NSString *)city andState:(NSString *)state {
    self = [self initWithName:name andDescription:description andEID:eid andData:data andPicturePaths:picturePaths andSessionId:sid andCity:city];
    if (self) {
        state_ = state;
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths andSessionId:(int)sid andCity:(NSString *)city andState:(NSString *)state andCountry:(NSString *)country {
    self = [self initWithName:name andDescription:description andEID:eid andData:data andPicturePaths:picturePaths andSessionId:sid andCity:city andState:state];
    if (self) {
        country_ = country;
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths andSessionId:(int)sid andCity:(NSString *)city andState:(NSString *)state andCountry:(NSString *)country andAddress:(NSString *)address {
    self = [self initWithName:name andDescription:description andEID:eid andData:data andPicturePaths:picturePaths andSessionId:sid andCity:city andState:state andCountry:country];
    if (self) {
        address_ = address;
        uploadable_ = [self canBeUploaded];
    }
    return self;
}

-(BOOL) canBeUploaded {
    if ( ( name_.length > 0 ) && ( eid_ > 0 ) ) {
        if ( data_.count != 0 ) return true;
        else if ( picturePaths_.count != 0 ) return true;
    }
    
    return false;
}

@end