//
//  API.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 8/21/13.
//  Copyright (c) 2013 Engaging Computing Group, UML. All rights reserved.
//

#import "API.h"

@implementation API

#define LIVE_URL @"http://129.63.16.128/"
#define DEV_URL  @"http://129.63.16.30/"

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

/**
 * The ever important switch between live iSENSE and our development site.
 *
 * @param useDev Set to true if you want to use the development site.
 */
 - (void) useDev:(BOOL)useDev {
	if (useDev) {
		baseURL = DEV_URL;
	} else {
		baseURL = LIVE_URL;
	}
 }





/**
 * Below methods are unimplemented.  They were added to prevent warnings in the API for
 * an unfinished implementation.
 *
 */

/* Checks for Connectivity */
+(BOOL) hasConnectivity { return YES; }

/* Change the baseURL Value */
-(void) setBaseUrl:(NSURL *)newUrl {}

/* Manage Authentication Key */
-(void) deleteSession {}

/* Doesn't Require Authentication Key */
-(RProject *)   getProjectWithId:       (int)projectId { return nil; }
-(RTutorial *)  getTutorialWithId:      (int)tutorialId { return nil; }
-(RDataSet *)   getDataSetWithId:       (int)dataSetId { return nil; }
-(NSArray *)    getProjectFieldsWithId: (int)projectId { return nil; }
-(NSArray *)    getDataSetsWithId:      (int)projectId { return nil; }

-(NSArray *)    getProjectsAtPage:  (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search { return nil; }
-(RTutorial *)  getTutorialsAtPage: (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search { return nil; }

/* Requires an Authentication Key */
-(NSArray *)    getUsersAtPage:     (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search { return nil; }

-(RPerson *)    getCurrentUser { return nil; };
-(RPerson *)    getUserWithUsername:(NSString *)username { return nil; }
-(int)          createProjectWithName:(NSString *)name  andFields:(NSArray *)fields { return -1; }
-(void)         appendDataSetDataWithId:(int)dataSetId  andData:(NSDictionary *)data {}

-(int)      uploadDataSetWithId:     (int)projectId withData:(NSDictionary *)dataToUpload    andName:(NSString *)name { return -1; }
-(int)      uploadCSVWithId:         (int)projectId withFile:(NSFileHandle *)csvToUpload     andName:(NSString *)name { return -1; }
-(int)      uploadProjectMediaWithId:(int)projectId withFile:(NSFileHandle *)mediaToUpload { return -1; }
-(int)      uploadDataSetMediaWithId:(int)dataSetId withFile:(NSFileHandle *)mediaToUpload { return -1; }

/* Convenience Method for Uploading */
-(NSDictionary *)rowsToCols:(NSDictionary *)original { return nil; }
 

@end
