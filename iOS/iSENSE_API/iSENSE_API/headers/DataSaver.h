//
//  DataSaver.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#ifndef __iSENSE_API__DataSaver__
#define __iSENSE_API__DataSaver__

#import <Foundation/Foundation.h>
#import "DataSet.h"
#import "Queue.h"
#import "iSENSE.h"

#define NO_KEY 1

@interface DataSaver : NSObject {
}

-(id)initWithContext:(NSManagedObjectContext *)context;
-(void)addDataSetFromCoreData:(DataSet *)dataSet;
-(void)addDataSet:(DataSet *)dataSet;
-(id)removeDataSet:(int)key;
-(void)editDataSetWithKey:(int)key;
-(bool)upload;
-(void)removeAllDataSets;

@property (nonatomic, assign) int count;
@property (nonatomic, retain) NSMutableDictionary *dataQueue;
@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;


@end


#endif /* defined(__iSENSE_API__DataSaver__) */
