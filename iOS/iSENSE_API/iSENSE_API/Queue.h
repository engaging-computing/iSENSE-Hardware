//
//  queue.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#ifndef __iSENSE_API__queue__
#define __iSENSE_API__queue__

#import <Foundation/Foundation.h>

@interface NSMutableDictionary (QueueAdditions)
- (id) dequeue;
- (void) enqueue:(id)anObject withKey:(int)key;
- (id) removeFromQueueWithKey:(int)key;


@property (nonatomic, assign) int headKey;
@property (nonatomic, assign) int tailKey;

@end

#endif /* defined(__iSENSE_API__queue__) */
