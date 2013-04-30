//
//  Upload_Queue.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#ifndef __iSENSE_API__Upload_Queue__
#define __iSENSE_API__Upload_Queue__

#import <Foundation/Foundation.h>

@interface DataSaver : NSObject {
    NSMutableDictionary *dataQueue;
}

-(void)addDataSet;
-(void)removeDataSet;
/* allow dataSet to be changed in place -- not sure how to approach this one */
-(void)editDataSetWithKey:(int)key;

@end


#endif /* defined(__iSENSE_API__Upload_Queue__) */
