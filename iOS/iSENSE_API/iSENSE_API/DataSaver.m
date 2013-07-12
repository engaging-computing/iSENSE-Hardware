//
//  DataSaver.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#include "DataSaver.h"

@implementation DataSaver

@synthesize count, dataQueue, managedObjectContext;

-(id)initWithContext:(NSManagedObjectContext *)context {
    self = [super init];
    if (self) {
        count = 0;
        dataQueue = [[NSMutableDictionary alloc] init];
        managedObjectContext = context;
    }
    return self;
}

// Add a DataSet to the queue
-(void)addDataSet:(DataSet *)dataSet {
    
    DataSet *ds = [NSEntityDescription insertNewObjectForEntityForName:@"DataSet" inManagedObjectContext:managedObjectContext];
    
    ds.name = dataSet.name;
    ds.dataDescription = dataSet.dataDescription;
    ds.data = dataSet.data;
    ds.picturePaths = dataSet.picturePaths;
    ds.eid = dataSet.eid;
    ds.city = dataSet.city;
    ds.country = dataSet.country;
    ds.uploadable = dataSet.uploadable;
    ds.address = dataSet.address;
    ds.sid = dataSet.sid;
        
    // Commit the changes
    NSError *error = nil;
    if (![managedObjectContext save:&error]) {
        // Handle the error.
        NSLog(@"%@", error);
    }
    
}

-(void)addDataSetFromCoreData:(DataSet *)dataSet {
    int newKey = arc4random();
    [dataQueue enqueue:dataSet withKey:newKey];
    count++;

}

// if key is nil, call dequeue otherwise dequeue with the given key
-(id)removeDataSet:(int)key {
    count--;
    DataSet *tmp;
    if (key == NO_KEY) {
        tmp = [dataQueue dequeue];
    } else {
        tmp = [dataQueue removeFromQueueWithKey:key];
    }
    
    if (tmp != nil) NSLog(@"hope");
    
    [managedObjectContext deleteObject:tmp];
    
    if (tmp != nil) NSLog(@"more hope");
    
    // Commit the changes
    NSError *error = [[NSError alloc] init];
    if (![managedObjectContext save:&error]) {
        // Handle the error.
        NSLog(@"Save failed with error: %@", error);
    }
    
    if (tmp != nil) NSLog(@"yet more hope");

    
    return tmp;
}

// if key is nil, call dequeue otherwise dequeue with the given key
-(void)removeAllDataSets {
    
    for (NSNumber *tmp in dataQueue.allKeys) {
        NSLog(@"delete");
        [self removeDataSet:tmp.intValue];
    }
    
    [dataQueue removeAllObjects];
    count = 0;

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
        NSLog(@"Not logged in."); // MIKE I KEN HAZ WAFFLES?
        return false;
    }
    
    DataSet *currentDS;
    for (int i = 0; i < count; i++) {
        // get the next dataset
        currentDS = [self removeDataSet:NO_KEY];
        
        // check if the session is uploadable
        if (currentDS.uploadable.boolValue) {
            
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
            NSLog(@"Ya nub");
            [self addDataSet:currentDS];
        }
        
        
    }
    
    return true;
}

@end