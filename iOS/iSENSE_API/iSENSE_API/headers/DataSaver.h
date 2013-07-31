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
#import "DataSet.h"
#import "Queue.h"
#import "iSENSE.h"

#define DATA_NONE_UPLOADED  1500
#define DATA_UPLOAD_SUCCESS 1501
#define DATA_UPLOAD_FAILED  1502

@interface DataSaver : NSObject {
}

-(id)initWithContext:(NSManagedObjectContext *)context;
-(void)addDataSetFromCoreData:(DataSet *)dataSet;
-(void)addDataSet:(DataSet *)dataSet;
-(id)removeDataSet:(NSNumber *)key;
-(void)editDataSetWithKey:(NSNumber *)key;
-(bool)upload:(NSString *)parentName;
-(void)removeAllDataSets;
-(id)getDataSet;

@property (nonatomic, assign) int count;
@property (nonatomic, retain) NSMutableDictionary *dataQueue;
@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;


@end


#endif /* defined(__iSENSE_API__DataSaver__) */
