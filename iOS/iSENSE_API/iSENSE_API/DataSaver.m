//
//  DataSaver.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#include "DataSaver.h"

@implementation DataSaver

@synthesize count, dataQueue;

-(id) init {
    self = [super init];
    if (self) {
        count = 0;
        dataQueue = [[NSMutableDictionary alloc] init];
    }
    return self;
}

// Add a DataSet to the queue
-(void)addDataSet:(DataSet *)dataSet {
    int newKey = arc4random();
    [dataQueue enqueue:dataSet withKey:newKey];
    count++;
}

// if key is nil, call dequeue otherwise dequeue with the given key
-(id)removeDataSet:(int)key {
    count--;
    if (key == NO_KEY) return [dataQueue dequeue];
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
    iSENSE *isenseAPI = [iSENSE getInstance];
    if (![isenseAPI isLoggedIn]) {
        NSLog(@"Not logged in.");
        return false;
    }
    
    DataSet *currentDS;
    while (count) {
        // get the next dataset
        NSArray *keys = [dataQueue allKeys];
        NSNumber *firstKey = [keys objectAtIndex:0];
        currentDS = [self removeDataSet:firstKey.intValue];
        
        // check if the session is uploadable
        if ([currentDS uploadable]) {
            
            // create a session
            if (currentDS.sid.intValue == -1) {
                NSNumber *sessionID = [isenseAPI createSession:currentDS.name withDescription:currentDS.dataDescription Street:currentDS.address City:currentDS.city Country:currentDS.country toExperiment:currentDS.eid];
                if (sessionID.intValue == -1) {
                    [self addDataSet:currentDS];
                    continue;
                } else {
                    currentDS.sid = sessionID;
                }
            }
            
            // Upload to iSENSE (pass me JSON data so we can putSessionData)
            if (((NSArray *)currentDS.data).count) {
                NSError *error = nil;
                NSData *dataJSON = [NSJSONSerialization dataWithJSONObject:currentDS.data options:0 error:&error];
                if (error != nil) {
                    [self addDataSet:currentDS];
                    NSLog(@"%@", error);
                    return false;
                }
                
                if (![isenseAPI putSessionData:dataJSON forSession:currentDS.sid inExperiment:currentDS.eid]) {
                    [self addDataSet:currentDS];
                    continue;
                }
            }
            
            // Upload pictures to iSENSE
            if (((NSArray *)currentDS.picturePaths).count) {
                NSArray *pictures = (NSArray *) currentDS.picturePaths;
                NSMutableArray *newPicturePaths = [NSMutableArray alloc];
                bool failedAtLeastOnce = false;
                
                // Loop through all the images and try to upload them
                for (int i = 0; i < pictures.count; i++) {
                    
                    // Track the images that fail to upload
                    if (![isenseAPI upload:pictures[i] toExperiment:currentDS.eid forSession:currentDS.sid withName:currentDS.name andDescription:currentDS.dataDescription]) {
                        failedAtLeastOnce = true;
                        [newPicturePaths addObject:pictures[i]];
                        continue;
                    }

                }
                
                // Add back the images that need to be uploaded
                if (failedAtLeastOnce) {
                    currentDS.picturePaths = newPicturePaths;
                    [self addDataSet:currentDS];
                    continue;
                }
            }
            
        } else {
            [self addDataSet:currentDS];
        }
        
        
    }
    
    return true;
}

@end