//
//  queue.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#include "Queue.h"


@implementation NSMutableDictionary (QueueAdditions)
@synthesize headKey, tailKey;


// Queues are first-in-first-out, so we remove objects from the head
- (id) dequeue {
    NSNumber *head = [NSNumber numberWithInt:self.headKey];
    id headObject = [self objectForKey:head];//[self objectAtIndex:0];
    if (headObject != nil) {
       NSArray *keyList = [self allKeys];
        if (keyList != nil && (keyList.count > 1) ) {
            self.headKey = keyList[1];
        } else {
            self.headKey = 0;
        }
       // [[headObject retain] autorelease]; // so it isn't dealloc'ed on remove
      [self removeObjectForKey:head];
    }
    return headObject;
}

// Add to the tail of the queue (no one likes it when people cut in line!)
- (void) enqueue:(id)anObject withKey:(int)key {
    self.tailKey = key;
    
    // Initialize headKey
    if (self.headKey == 0) {
        self.headKey = key;
    }
    
    // This method automatically adds to the end of the array
    NSNumber *keyObj = [NSNumber numberWithInt:key];
    [self setObject:anObject forKey:keyObj];
}


// Allows any arbitrary 
- (id) removeFromQueueWithKey:(int)key {
    id headObject;
    if (key == self.headKey) {
        headObject = [self dequeue];
    } else {
        NSNumber *keyObj = [NSNumber numberWithInt:key];
        headObject = [self objectForKey:keyObj];
        if (headObject != nil) {
            [self removeObjectForKey:keyObj];
        }
        if (key == self.tailKey) {
            NSArray *keyList = [self allKeys];
            if (keyList != nil && (keyList.count >= 1) ) {
                self.tailKey = keyList[keyList.count - 1];
            } else {
                self.tailKey = 0;
            }

        }
    }
    return headObject;
}

//
//- (int) headKey {
//    return self.headKey;
//}
//
//- (void) setHeadKey:(int)headKey {
//    self.headKey = headKey;
//}
//
//- (int) tailKey {
//    return self.tailKey;
//}
//
//- (void) setTailKey:(int)tailKey {
//    self.tailKey = tailKey;
//}

@end