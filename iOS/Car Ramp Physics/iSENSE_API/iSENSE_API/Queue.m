//
//  queue.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#include "Queue.h"


@implementation NSMutableDictionary (QueueAdditions)

// Queues are first-in-first-out, so we remove objects from the head
- (id) dequeue {
    int headKey = self.allKeys[0];
    NSNumber *head = [NSNumber numberWithInt:headKey];
    id headObject = [self objectForKey:head];
    if (headObject != nil) {
      [self removeObjectForKey:head];
    }
    return headObject;
}

// Add to the tail of the queue (no one likes it when people cut in line!)
- (void) enqueue:(id)anObject withKey:(int)key {
      
    // This method automatically adds to the end of the array
    NSNumber *keyObj = [NSNumber numberWithInt:key];
    [self setObject:anObject forKey:keyObj];
}


// Allows any arbitrary node to be removed
- (id) removeFromQueueWithKey:(int)key {
    int headKey = self.allKeys[0];
    
    id headObject;
    if (key == headKey) {
        headObject = [self dequeue];
    } else {
        NSNumber *keyObj = [NSNumber numberWithInt:key];
        headObject = [self objectForKey:keyObj];
        if (headObject != nil) {
            [self removeObjectForKey:keyObj];
        }
    }
    return headObject;
}

@end