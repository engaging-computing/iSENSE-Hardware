//
//  DataSaver.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#include "DataSaver.h"

@implementation DataSaver

-(id) init {
    self = [super init];
    if (self) {
        dataQueue = [dataQueue init];
    }
    return self;
}

-(void)addDataSet:(DataSet *)dataSet {
    int newKey = arc4random();
    [dataQueue enqueue:dataSet withKey:newKey];
}

-(id)removeDataSet:(int)key {
    return [dataQueue removeFromQueueWithKey:key];
}

-(void)editDataSetWithKey:(int)key {
    DataSet *dataSet = [dataQueue removeFromQueueWithKey:key];
    /*
     * Do editing code here!
     */
    [dataQueue enqueue:dataSet withKey:key];
}

-(bool)upload {
    /*
     * Do uploading code here!
     */
    return true;
}

@end