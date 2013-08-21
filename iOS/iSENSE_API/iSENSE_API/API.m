//
//  API.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 8/21/13.
//  Copyright (c) 2013 Engaging Computing Group, UML. All rights reserved.
//

#import "API.h"

@implementation API

static API *api;

/**
 * Behaves as the "getInstance" method of the API. Will only get called once by the system, 
 * but doesn't prevent the user from calling alloc or copy methods.
 */
+ (void)initialize {
    static BOOL initialized = NO;
    if(!initialized) {
        initialized = YES;
        api = [[API alloc] init];
    }
}

/**
 * Log in to iSENSE. After calling this function, authenticated API functions will work properly
 *
 * @param username The username of the user to log in as
 * @param password The password of the user to log in as
 * @return TRUE if login succeeds, FALSE if it doesn't
 */
-(BOOL) createSessionWithUsername:(NSString *)username andPassword:(NSString *)password {
    return TRUE;
}

@end
