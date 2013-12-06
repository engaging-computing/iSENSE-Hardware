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
-(BOOL)addDataSet:(QDataSet *)dataSet {
    
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
    
    BOOL success = [self commitMOCChanges];
    return success;
    
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

-(void) editDataSetWithKey:(NSNumber *)key andChangeFieldsTo:(NSMutableArray *)newFields {
    
    QDataSet *dataSet = [dataQueue objectForKey:key];
    [dataSet setFields:newFields];
    
    [self commitMOCChanges];
    
}

// commit changes to the managedObjectContext
-(BOOL) commitMOCChanges {
    
    NSError *error = nil;
    if (![managedObjectContext save:&error]) {
        NSLog(@"Save failed: %@", error);
        return FALSE;
    }
    
    return TRUE;
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
                if (currentDS.fields == nil) {
                    continue;
                } else {
                    currentDS.data = [DataFieldManager reOrderData:currentDS.data forProjectID:currentDS.projID.intValue API:api andFieldOrder:currentDS.fields];
                }
            }
            
            // upload to iSENSE
            int returnID = -1;
            if (((NSArray *)currentDS.data).count) {
                
                NSMutableDictionary *jobj = [[NSMutableDictionary alloc] init];
                [jobj setObject:currentDS.data forKey:@"data"];
                jobj = [[api rowsToCols:jobj] mutableCopy];
                
                returnID = [api uploadDataSetWithId:currentDS.projID.intValue withData:jobj andName:currentDS.name];
                NSLog(@"Data set ID: %d", returnID);
                
                if (returnID == 0 || returnID == -1) {
                    dataSetsFailed++;
                    continue;
                }
            }
            
            // upload pictures to iSENSE
            if (((NSArray *)currentDS.picturePaths).count) {
                NSArray *pictures = (NSArray *) currentDS.picturePaths;
                NSMutableArray *newPicturePaths = [NSMutableArray alloc];
                bool failedAtLeastOnce = false;
            
                // loop through all the images and try to upload them
                for (int i = 0; i < pictures.count; i++) {
            
                    // track the images that fail to upload
                    if (![api uploadDataSetMediaWithId:returnID withFile:pictures[i] andName:currentDS.name]) {
                        dataSetsFailed++;
                        failedAtLeastOnce = true;
                        [newPicturePaths addObject:pictures[i]];
                        continue;
                    }
            
                }
            
                // add back the images that need to be uploaded
                if (failedAtLeastOnce) {
                    currentDS.picturePaths = newPicturePaths;
                    continue;
                }
            }
            
            [dataSetsToBeRemoved addObject:currentKey];
            
        } else {
            continue;
        }
        
        
    }
    
    [self removeDataSets:dataSetsToBeRemoved];
    
    bool status = FALSE;
    if (dataSetsToUpload > 0)
        if (dataSetsFailed > 0)
            [prefs setInteger:DATA_UPLOAD_FAILED forKey:KEY_DATA_UPLOADED];
        else {
            [prefs setInteger:DATA_UPLOAD_SUCCESS forKey:KEY_DATA_UPLOADED];
            status = TRUE;
        }
        else
            [prefs setInteger:DATA_NONE_UPLOADED forKey:KEY_DATA_UPLOADED];
    
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