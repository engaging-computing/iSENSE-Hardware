//
//  DataSaver.h
//  iSENSE API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#ifndef __iSENSE_API__DataSaver__
#define __iSENSE_API__DataSaver__

#import <Foundation/Foundation.h>
#import "QDataSet.h"
#import "Queue.h"
#import <iSENSE_API/API.h>

#define DATA_NONE_UPLOADED  1500
#define DATA_UPLOAD_SUCCESS 1501
#define DATA_UPLOAD_FAILED  1502

@interface DataSaver : NSObject {}

-(id)   initWithContext:(NSManagedObjectContext *)context;
-(void) addDataSetFromCoreData:(QDataSet *)dataSet;
-(BOOL) addDataSet:(QDataSet *)dataSet;
-(id)   removeDataSet:(NSNumber *)key;
-(void) editDataSetWithKey:(NSNumber *)key andChangeProjIDTo:(NSNumber *)newProjID;
-(void) editDataSetWithKey:(NSNumber *)key andChangeDescription:(NSString *)newDescription;
-(bool) upload:(NSString *)parentName;
-(void) removeAllDataSets;
-(id)   getDataSet;
-(id)   getDataSetWithKey:(NSNumber *)key;
-(int)  dataSetCountWithParentName:(NSString *)pn;

@property (nonatomic, retain) NSMutableDictionary *dataQueue;
@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;


@end


#endif
