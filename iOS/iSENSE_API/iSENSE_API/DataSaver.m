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

-(BOOL) editDataSetWithKey:(NSNumber *)key andChangeProjIDTo:(NSNumber *)newProjID {
    
    QDataSet *dataSet = [dataQueue objectForKey:key];
    [dataSet setProjID:newProjID];
    
    return [self commitMOCChanges];
}

-(BOOL) editDataSetWithKey:(NSNumber *)key andChangeDescription:(NSString *)newDescription {
    
    QDataSet *dataSet = [dataQueue objectForKey:key];
    [dataSet setDataDescription:newDescription];
    
    return [self commitMOCChanges];
    
}

-(BOOL) editDataSetWithKey:(NSNumber *)key andChangeFieldsTo:(NSMutableArray *)newFields {
    
    QDataSet *dataSet = [dataQueue objectForKey:key];
    [dataSet setFields:newFields];
    
    return [self commitMOCChanges];
    
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
    
    NSLog(@"Insisde DataSaver.m");
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    int dataSetsToUpload = 0;
    int dataSetsFailed = 0;
    
    NSMutableArray *dataSetsToBeRemoved = [[NSMutableArray alloc] init];
    QDataSet *currentDS;
    
    for (NSNumber *currentKey in dataQueue.allKeys) {
        
        // get the next dataset
        currentDS = [dataQueue objectForKey:currentKey];
        
        NSLog(@"Number of pics: %d", ((NSArray *)currentDS.picturePaths).count);
        NSLog(@"Number of datas: %d", ((NSArray *)currentDS.data).count);
        
        // prevent uploading datasets from other sources (e.g. manual vs automatic)
        if (![currentDS.parentName isEqualToString:parentName]) continue;
        
        // prevent trying to upload with an invalid project
        if (currentDS.projID.intValue <= 0) continue;
        
        // check if the session is uploadable
        if (currentDS.uploadable.boolValue) {
            
            NSLog(@"Inside uploading area");
            
            dataSetsToUpload++;
            
            // organize data if no initial project was found
            if (currentDS.hasInitialProj.boolValue == FALSE) {
                NSLog(@"HelloWorld");
                if (currentDS.fields == nil) {
                    continue;
                } else {
                    currentDS.data = [DataFieldManager reOrderData:currentDS.data
                                                      forProjectID:currentDS.projID.intValue
                                                    withFieldOrder:currentDS.fields
                                                       andFieldIDs:nil];
                }
            } else {
                // see if the elements are NSMutableArrays or NSMutableObjects: if arrays, reorder data
                if ([[currentDS.data objectAtIndex:0] isKindOfClass:[NSMutableArray class]]) {
                    currentDS.data = [DataFieldManager reOrderData:currentDS.data
                                                      forProjectID:currentDS.projID.intValue
                                                    withFieldOrder:currentDS.fields
                                                       andFieldIDs:nil];
                }
            }
            
            // upload to iSENSE
            __block int returnID = -1;
            if (((NSArray *)currentDS.data).count) {
                NSLog(@"Shouldn't be here");
                NSMutableDictionary *jobj = [[NSMutableDictionary alloc] init];
                [jobj setObject:currentDS.data forKey:@"data"];
                jobj = [[api rowsToCols:jobj] mutableCopy];
                
                if ([api getCurrentUser] == nil) {
                    NSLog(@"Not logged in");
                    DLAVAlertView *contribKeyAlert = [[DLAVAlertView alloc] initWithTitle:@"Enter Contributor Key" message:[NSString stringWithFormat:@"%@%@", @"Data Set: ", currentDS.name] delegate:nil cancelButtonTitle:@"Cancel" otherButtonTitles:@"Upload", nil];
                    [contribKeyAlert setAlertViewStyle:DLAVAlertViewStyleLoginAndPasswordInput];
                    [contribKeyAlert textFieldAtIndex:0].placeholder = @"Contributor Name";
                    [contribKeyAlert textFieldAtIndex:1].placeholder = @"Contributor Key";
                    [contribKeyAlert showWithCompletion:^(DLAVAlertView *alertView, NSInteger buttonIndex) {
                        if ([[alertView buttonTitleAtIndex:buttonIndex] isEqualToString:@"Upload"]) {
                            returnID = [api uploadDataWithId:currentDS.projID.intValue withData:jobj withContributorKey:[contribKeyAlert textFieldTextAtIndex:1] as:[contribKeyAlert textFieldTextAtIndex:0] andName:currentDS.name];
                        }
                    }];
                } else {
                   returnID = [api uploadDataWithId:currentDS.projID.intValue withData:jobj andName:currentDS.name];
                }
                NSLog(@"Data set ID: %d", returnID);
                
                if (returnID == 0 || returnID == -1) {
                    dataSetsFailed++;
                    continue;
                }
            }
            
            // upload pictures to iSENSE
            if (((NSArray *)currentDS.picturePaths).count) {
                
                NSLog(@"Inside pictures area");
                NSArray *pictures = (NSArray *) currentDS.picturePaths;
                NSMutableArray *newPicturePaths = [NSMutableArray alloc];
                bool failedAtLeastOnce = false;
            
                // loop through all the images and try to upload them
                for (int i = 0; i < pictures.count; i++) {
            
                    // track the images that fail to upload
                    
                    if ([api getCurrentUser] == nil) {
                        NSLog(@"Not logged in");
                        DLAVAlertView *contribKeyAlert = [[DLAVAlertView alloc] initWithTitle:@"Enter Contributor Key" message:[NSString stringWithFormat:@"%@%@", @"Data Set: ", currentDS.name] delegate:nil cancelButtonTitle:@"Cancel" otherButtonTitles:@"Upload", nil];
                        [contribKeyAlert setAlertViewStyle:DLAVAlertViewStyleLoginAndPasswordInput];
                        [contribKeyAlert textFieldAtIndex:0].placeholder = @"Contributor Name";
                        [contribKeyAlert textFieldAtIndex:1].placeholder = @"Contributor Key";
                        [contribKeyAlert showWithCompletion:^(DLAVAlertView *alertView, NSInteger buttonIndex) {
                            if ([[alertView buttonTitleAtIndex:buttonIndex] isEqualToString:@"Upload"]) {
                                returnID = [api uploadMediaWithId:currentDS.projID.intValue withFile:pictures[i] andName:currentDS.name withTarget:PROJECT withContributorKey:[contribKeyAlert textFieldTextAtIndex:1] as:[contribKeyAlert textFieldTextAtIndex:0]];
                                
                            }
                        }];

                    } else {
                        
                        returnID = [api uploadMediaWithId:currentDS.projID.intValue withFile:pictures[i] andName:currentDS.name withTarget:PROJECT];
                    
                        
                        
                    }
                    
                    if (returnID == -1) {
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