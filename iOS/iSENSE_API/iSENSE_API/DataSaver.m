//
//  DataSaver.m
//  iSENSE API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "DataSaver.h"
#import "DataFieldManager.h"

@implementation DataSaver

@synthesize dataQueue, managedObjectContext;

-(id)initWithContext:(NSManagedObjectContext *)context {
    self = [super init];
    if (self) {
        dataQueue = [[NSMutableDictionary alloc] init];
        managedObjectContext = context;
    }
    return self;
}

// add a DataSet to the queue
-(void)addDataSet:(QDataSet *)dataSet {
    
    QDataSet *ds = [NSEntityDescription insertNewObjectForEntityForName:@"QDataSet" inManagedObjectContext:managedObjectContext];
    
    ds.name = dataSet.name;
    ds.dataDescription = dataSet.dataDescription;
    ds.data = dataSet.data;
    ds.picturePaths = dataSet.picturePaths;
    ds.projID = dataSet.projID;
    ds.uploadable = dataSet.uploadable;
    
    // commitMOCChanges used to be here - moved down to bottom of function
    
    int newKey = arc4random();
    [dataQueue enqueue:dataSet withKey:newKey];
    
    [self commitMOCChanges];

}

-(void)addDataSetFromCoreData:(QDataSet *)dataSet {
    
    int newKey = arc4random();
    [dataQueue enqueue:dataSet withKey:newKey];
    [self commitMOCChanges];
    
}

// if key is nil, call dequeue otherwise dequeue with the given key
-(id)removeDataSet:(NSNumber *)key {
    
    QDataSet *tmp;
    if (key == nil) {
        tmp = [dataQueue dequeue];
    } else {
        tmp = [dataQueue removeFromQueueWithKey:key];
    }

    [managedObjectContext deleteObject:tmp];
    [self commitMOCChanges];
    
    return tmp;
    
}

-(id)getDataSet {
    
    NSNumber *firstKey = [dataQueue.allKeys objectAtIndex:0];
    return [dataQueue objectForKey:firstKey];
    
}

-(id)getDataSetWithKey:(NSNumber *)key {
    
    return [dataQueue objectForKey:key];
    
}

// if key is nil, call dequeue otherwise dequeue with the given key
-(void)removeAllDataSets {
    
    for (int i = 0; i < dataQueue.count; i++) {
        NSNumber *tmp = [dataQueue.allKeys objectAtIndex:i];
        [self removeDataSet:tmp];
    }

    [dataQueue removeAllObjects];
    [self commitMOCChanges];
}

-(void) editDataSetWithKey:(NSNumber *)key andChangeProjIDTo:(NSNumber *)newProjID {
    
    QDataSet *dataSet = [dataQueue objectForKey:key];
    [dataSet setProjID:newProjID];
    
    [self commitMOCChanges];
}

-(void) editDataSetWithKey:(NSNumber *)key andChangeDescription:(NSString *)newDescription {
    
    QDataSet *dataSet = [dataQueue objectForKey:key];
    [dataSet setDataDescription:newDescription];
    
    [self commitMOCChanges];
    
}

// commit changes to the managedObjectContext
-(void) commitMOCChanges {
    
    NSError *error = nil;
    if (![managedObjectContext save:&error]) {
        NSLog(@"Save failed: %@", error);
    }
    
}

-(bool)upload:(NSString *)parentName {
    API *api = [API getInstance];
    if ([api getCurrentUser] == nil) {
        
        NSLog(@"Not logged in.");
        return false;
    }
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    int dataSetsToUpload = 0;
    int dataSetsFailed = 0;
    
    NSMutableArray *dataSetsToBeRemoved = [[NSMutableArray alloc] init];
    QDataSet *currentDS;
    
    for (NSNumber *currentKey in dataQueue.allKeys) {
        
        // get the next dataset
        currentDS = [dataQueue objectForKey:currentKey];
        
        // prevent uploading datasets from other sources (e.g. manual vs automatic)
        if (![currentDS.parentName isEqualToString:parentName]) continue;
        
        // prevent trying to upload with an invalid project
        if (currentDS.projID.intValue <= 0) continue;
        
        // check if the session is uploadable
        if (currentDS.uploadable.boolValue) {
            
            dataSetsToUpload++;
            
            // organize data if no initial project was found
            if (currentDS.hasInitialProj.boolValue == FALSE) {
                DataFieldManager *dfm = [[DataFieldManager alloc] init];
                currentDS.data = [dfm reOrderData:currentDS.data forExperimentID:currentDS.projID.intValue];
            }
            
            // upload to iSENSE
            if (((NSArray *)currentDS.data).count) {
//                NSError *error = nil;
//                NSData *dataJSON = [NSJSONSerialization dataWithJSONObject:currentDS.data options:0 error:&error];
//                if (error != nil) {
//                    NSLog(@"%@", error);
//                    return false;
//                }
                
                int returnID = [api uploadDataSetWithId:currentDS.projID.intValue withData:currentDS.data andName:currentDS.name];
                if (returnID == 0 || returnID == -1) {
                    dataSetsFailed++;
                    continue;
                }
                
            }
            
            // upload pictures to iSENSE TODO - implement with the new API
//            if (((NSArray *)currentDS.picturePaths).count) {
//                NSArray *pictures = (NSArray *) currentDS.picturePaths;
//                NSMutableArray *newPicturePaths = [NSMutableArray alloc];
//                bool failedAtLeastOnce = false;
//                
//                // loop through all the images and try to upload them
//                for (int i = 0; i < pictures.count; i++) {
//                    
//                    // track the images that fail to upload
//                    if (![isenseAPI upload:pictures[i] toExperiment:currentDS.projID forSession:currentDS.sid withName:currentDS.name andDescription:currentDS.dataDescription]) {
//                        dataSetsFailed++;
//                        failedAtLeastOnce = true;
//                        [newPicturePaths addObject:pictures[i]];
//                        continue;
//                    }
//
//                }
//            
//                // add back the images that need to be uploaded
//                if (failedAtLeastOnce) {
//                    currentDS.picturePaths = newPicturePaths;
//                    continue;
//                }
//            }
            
        [dataSetsToBeRemoved addObject:currentKey];
            
        } else {
            continue;
        }
        
        
    }
    
    [self removeDataSets:dataSetsToBeRemoved];
    
    bool status = FALSE;
    if (dataSetsToUpload > 0)
        if (dataSetsFailed > 0)
            [prefs setInteger:DATA_UPLOAD_FAILED forKey:@"key_data_uploaded"];
        else {
            [prefs setInteger:DATA_UPLOAD_SUCCESS forKey:@"key_data_uploaded"];
            status = TRUE;
        }
    else
        [prefs setInteger:DATA_NONE_UPLOADED forKey:@"key_data_uploaded"];
    
    return status;
}

-(void)removeDataSets:(NSArray *)keys {
    
    for(NSNumber *key in keys) {
        [self removeDataSet:key];
    }
    
}

-(int) dataSetCountWithParentName:(NSString *)pn {
    [self clearGarbageWithoutParentName:pn];
    
    NSArray *keys = [self.dataQueue allKeys];
    return keys.count;
}

// removes malformed or garbage data sets caused by things like deleting data sets, resetting the app, etc.
-(void) clearGarbageWithoutParentName:(NSString *)pn {
    
    NSArray *keys = [self.dataQueue allKeys];
    for (int i = 0; i < keys.count; i++) {
        QDataSet *tmp = [self.dataQueue objectForKey:keys[i]];
        if (!([tmp.parentName isEqualToString:pn])) {
            // remove garbage
            [self.dataQueue removeObjectForKey:keys[i]];
        } else {
           // keep data set
        }
    }
    [self commitMOCChanges];
}

@end