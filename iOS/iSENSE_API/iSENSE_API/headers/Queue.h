//
//  Queue.h
//  iSENSE API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#ifndef __iSENSE_API__queue__
#define __iSENSE_API__queue__

#import <Foundation/Foundation.h>

@interface NSMutableDictionary (QueueAdditions)
- (id) dequeue;
- (void) enqueue:(id)anObject withKey:(int)key;
- (id) removeFromQueueWithKey:(NSNumber *)key;

@end

#endif /* defined(__iSENSE_API__queue__) */
