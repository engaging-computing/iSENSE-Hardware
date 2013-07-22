//
//  Queue.m
//  iSENSE API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#include "Queue.h"


@implementation NSMutableDictionary (QueueAdditions)

// Queues are first-in-first-out, so we remove objects from the head
- (id) dequeue {
    NSArray *keys = [self allKeys];
    id firstKey = [keys objectAtIndex:0];
    id headObject = [self objectForKey:firstKey];
    if (headObject != nil) {
        [self removeObjectForKey:firstKey];
        return headObject;
    }
    
    if (self.count != 0) NSLog(@"Cannot dequeue dataSet: invalid key");
    else NSLog(@"Cannot dequeue dataSet: empty queue");
    
    return headObject;
}

// Add to the tail of the queue (no one likes it when people cut in line!)
- (void) enqueue:(id)anObject withKey:(int)key {
          
    // This method automatically adds to the end of the array
    NSNumber *keyObj = [NSNumber numberWithInt:key];
    [self setObject:anObject forKey:keyObj];
}


// Allows any arbitrary node to be removed
- (id) removeFromQueueWithKey:(NSNumber *)key {
    NSLog(@"%@", [self allKeys].description);
    NSArray *keys = [self allKeys];
    NSNumber *firstKey = [keys objectAtIndex:0];
    id headObject;
    if (key == firstKey) {
        headObject = [self dequeue];
        return headObject;
    } else {
        headObject = [self objectForKey:key];
        if (headObject != nil) {
            [self removeObjectForKey:key];
            NSLog(@"deleted dataset so new queue count is %d", self.count);
            return headObject;
        } else {
            NSLog(@"Cannot remove dataSet: invalid key");
        }
    }
    NSLog(@"I should never get here from queue remove with key");
    return headObject;
}

@end