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
static NSString *baseUrl, *authenticityToken;

/**
 * Behaves as the "getInstance" method of the API. Will only get called once by the system,
 * but doesn't prevent the user from calling alloc or copy methods.
 */
+ (void)initialize {
    static BOOL initialized = NO;
    if(!initialized) {
        initialized = YES;
        api = [[API alloc] init];
        baseUrl = LIVE_URL;
        authenticityToken = nil;
    }
}

/**
 * Change the baseUrl directly.
 *
 * @param newUrl NSString version of the URL you want to use.
 */
-(void) setBaseUrl:(NSString *)newUrl {
    baseUrl = newUrl;
}

/**
 * The ever important switch between live iSENSE and our development site.
 *
 * @param useDev Set to true if you want to use the development site.
 */
- (void) useDev:(BOOL)useDev {
	if (useDev) {
		baseUrl = DEV_URL;
	} else {
		baseUrl = LIVE_URL;
	}
}

/**
 * Checks for connectivity using Apple's reachability class.
 *
 * @return YES if you have connectivity, NO if it does not
 */
+(BOOL) hasConnectivity {
    Reachability *reachability = [Reachability reachabilityForInternetConnection];
    NetworkStatus networkStatus = [reachability currentReachabilityStatus];
    return !(networkStatus == NotReachable);
}

/**
 * Below methods are unimplemented.  They were added to prevent warnings in the API for
 * an unfinished implementation.
 *
 */

/**
 * Log in to iSENSE. After calling this function, authenticated API functions will work properly.
 *
 * @param username The username of the user to log in as
 * @param password The password of the user to log in as
 * @return TRUE if login succeeds, FALSE if it does not
 */
-(BOOL) createSessionWithUsername:(NSString *)username andPassword:(NSString *)password {
    /*
     try {
     String result = makeRequest(baseURL, "login", "username_or_email="+URLEncoder.encode(username, "UTF-8")+"&password="+URLEncoder.encode(password, "UTF-8"), "POST", null);
     System.out.println(result);
     JSONObject j =  new JSONObject(result);
     authToken = j.getString("authenticity_token");
     if( j.getString("status").equals("success")) {
     currentUser = getUser(username);
     return true;
     } else {
     return false;
     }
     } catch (Exception e) {
     e.printStackTrace();
     }
     return false;    */
    NSString *parameters = [NSString stringWithFormat:@"%@%s%@%s", @"&username=", [username UTF8String], @"&password=", [password UTF8String]];
    NSDictionary *result = [self makeRequestWithBaseUrl:baseUrl withPath:@"login" withParameters:parameters withReqestType:@"POST" andPostData:nil];
    NSLog(@"%@", result.description);
    
    
        return TRUE;
}

/* Manage Authentication Key */
-(void) deleteSession {}

/* Doesn't Require Authentication Key */
-(RProject *)   getProjectWithId:       (int)projectId { return nil; }
-(RTutorial *)  getTutorialWithId:      (int)tutorialId{ return nil; }
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

/**
 * Reformats a row-major JSONObject to column-major.
 *
 * @param original The row-major formatted JSONObject
 * @return A column-major reformatted version of the original JSONObject
 */
-(NSDictionary *)rowsToCols:(NSDictionary *)original {
    NSMutableDictionary *reformatted = [[NSMutableDictionary alloc] init];
    NSArray *inner = [original objectForKey:@"data"];
    for(int i = 0; i < inner.count; i++) {
        NSDictionary *innermost = (NSDictionary *) [inner objectAtIndex:i];
        for (NSString *currKey in [innermost allKeys]) {
            NSMutableArray *currArray = nil;
            if(!(currArray = [reformatted objectForKey:currKey])) {
                currArray = [[NSMutableArray alloc] init];
            }
            [currArray addObject:[innermost objectForKey:currKey]];
            [reformatted setObject:currArray forKey:currKey];
        }
    }
    return reformatted;
}

/**
 * Makes an HTTP request for JSON-formatted data. Functions that
 * call this function should not be run on the UI thread.
 *
 * @param baseUrl The base of the URL to which the request will be made
 * @param path The path to append to the request URL
 * @param parameters Parameters separated by ampersands (&)
 * @param reqType The request type as a string (i.e. GET or POST)
 * @param postData The data to be given to iSENSE as NSData
 * @return An NSDictionary dump of a JSONObject representing the requested data
 */
-(NSDictionary *)makeRequestWithBaseUrl:(NSString *)baseUrl withPath:(NSString *)path withParameters:(NSString *)parameters withReqestType:(NSString *)reqType andPostData:(NSData *)postData {
    NSURL *url = [[NSURL alloc] initWithString:[NSString stringWithFormat:@"%@%@%@", baseUrl, path, parameters]];
    NSLog(@"Connect to: %@", url);

    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url
                                                           cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                                       timeoutInterval:10];
    [request setHTTPMethod:reqType];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    
    if (postData) {
        [request setValue:[NSString stringWithFormat:@"%d", postData.length] forHTTPHeaderField:@"Content-Length"];
        [request setHTTPBody:postData];
    }
    
    NSError *requestError;
    NSHTTPURLResponse *urlResponse;
    
    NSData *dataResponse = [NSURLConnection sendSynchronousRequest:request returningResponse:&urlResponse error:&requestError];
    if (urlResponse.statusCode == 200) {
        NSDictionary *parsedJSONResponse = [NSJSONSerialization JSONObjectWithData:dataResponse options:NSJSONReadingMutableContainers error:&requestError];
        if (requestError) NSLog(@"Error received from server: %@", requestError);
        return parsedJSONResponse;
    } else if (urlResponse.statusCode == 403){
        NSLog(@"Authenticity token not accepted.");
    } else if (urlResponse.statusCode == 422) {
        NSLog(@"Unprocessable entity. (Something is wrong with the request.)");
    }
    
    return nil;
}



@end
