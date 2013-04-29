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
    id headObject = [self objectForKey:headKey];//[self objectAtIndex:0];
    if (headObject != nil) {
       // [[headObject retain] autorelease]; // so it isn't dealloc'ed on remove
      [self removeObjectForKey:headKey];
    }
    return headObject;
}

// Add to the tail of the queue (no one likes it when people cut in line!)
- (void) enqueue:(id)anObject {
    [self addObject:anObject];
    //this method automatically adds to the end of the array
}

@end