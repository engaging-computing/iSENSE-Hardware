//
//  DataSaver.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#include "DataSaver.h"

@implementation DataSaver

@synthesize count;

-(id) init {
    self = [super init];
    if (self) {
        dataQueue = [[NSMutableDictionary alloc] init];
        count = 0;
    }
    return self;
}

-(void)addDataSet:(DataSet *)dataSet {
    int newKey = arc4random();
    [dataQueue enqueue:dataSet withKey:newKey];
    count++;
}

-(id)removeDataSet:(int)key {
    count--;
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
        currentDS = [self removeDataSet:dataQueue.headKey];
        
        // check if the session is uploadable
        if ([currentDS uploadable_]) {
            
            // create a session
            if (currentDS.sid_ == -1) {
                int sessionID = [isenseAPI createSession:currentDS.name_ withDescription:currentDS.description_ Street:currentDS.address_ City:currentDS.city_ Country:currentDS.country_ toExperiment:[NSNumber numberWithInt:currentDS.eid_]];
                if (sessionID == -1) {
                    [self addDataSet:currentDS];
                    continue;
                } else {
                    currentDS.sid_ = sessionID;
                }
            }
            
            // Upload to iSENSE (pass me JSON data so we can putSessionData)
            if (currentDS.data_.count) {
                NSError *error = nil;
                NSData *dataJSON = [NSJSONSerialization dataWithJSONObject:currentDS.data_ options:0 error:&error];
                if (error != nil) {
                    [self addDataSet:currentDS];
                    NSLog(@"%@", error);
                    return false;
                }
                
                if (![isenseAPI putSessionData:dataJSON forSession:[NSNumber numberWithInt:currentDS.sid_] inExperiment:[NSNumber numberWithInt:currentDS.eid_]]) {
                    [self addDataSet:currentDS];
                    continue;
                }
            }
            
            // Upload pictures to iSENSE
            if (currentDS.picturePaths_.count) {
                NSMutableArray *newPicturePaths = [NSMutableArray alloc];
                bool failedAtLeastOnce = false;
                
                // Loop through all the images and try to upload them
                for (int i = 0; i < currentDS.picturePaths_.count; i++) {
                    
                    // Track the images that fail to upload
                    if (![isenseAPI upload:currentDS.picturePaths_[i] toExperiment:[NSNumber numberWithInt:currentDS.eid_] forSession:[NSNumber numberWithInt:currentDS.sid_] withName:currentDS.name_ andDescription:currentDS.description_]) {
                        failedAtLeastOnce = true;
                        [newPicturePaths addObject:currentDS.picturePaths_[i]];
                        continue;
                    }

                }
                
                // Add back the images that need to be uploaded
                if (failedAtLeastOnce) {
                    currentDS.picturePaths_ = newPicturePaths;
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