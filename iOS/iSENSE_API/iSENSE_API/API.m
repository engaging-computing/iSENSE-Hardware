//
//  API.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 8/21/13.
//  Copyright (c) 2013 Engaging Computing Group, UML. All rights reserved.
//

#import "API.h"

@implementation API

#define LIVE_URL @"http://129.63.16.128"
#define DEV_URL  @"http://129.63.16.30"

#define GET     @"GET"
#define POST    @"POST"
#define PUT     @"PUT"
#define DELETE  @"DELETE"

static NSString *baseUrl, *authenticityToken;
static RPerson *currentUser;

/**
 * Access the current instance of an API object.
 *
 * @return An instance of the API object
 */
+(id)getInstance {
    static API *api = nil;
    static dispatch_once_t initApi;
    dispatch_once(&initApi, ^{
        api = [[self alloc] init];
    });
    return api;
}

/*
 * Initializes all the static variables in the API.
 *
 * @return The current instance of the API
 */
- (id)init {
    if (self = [super init]) {
        baseUrl = LIVE_URL;
        authenticityToken = nil;
        currentUser = nil;
    }
    return self;
}

/**
 * Change the baseUrl directly.
 *
 * @param newUrl NSString version of the URL you want to use.
 */
-(void)setBaseUrl:(NSString *)newUrl {
    baseUrl = newUrl;
}

/**
 * The ever important switch between live iSENSE and our development site.
 *
 * @param useDev Set to true if you want to use the development site.
 */
- (void)useDev:(BOOL)useDev {
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
+(BOOL)hasConnectivity {
    Reachability *reachability = [Reachability reachabilityForInternetConnection];
    NetworkStatus networkStatus = [reachability currentReachabilityStatus];
    return !(networkStatus == NotReachable);
}

/**
 * Log in to iSENSE. After calling this function, authenticated API functions will work properly.
 *
 * @param username The username of the user to log in as
 * @param password The password of the user to log in as
 * @return TRUE if login succeeds, FALSE if it does not
 */
-(BOOL)createSessionWithUsername:(NSString *)username andPassword:(NSString *)password {
    
    NSString *parameters = [NSString stringWithFormat:@"%@%s%@%s", @"username_or_email=", [username UTF8String], @"&password=", [password UTF8String]];
    NSDictionary *result = [self makeRequestWithBaseUrl:baseUrl withPath:@"login" withParameters:parameters withRequestType:POST andPostData:nil];

    authenticityToken = [result objectForKey:@"authenticity_token"];
    
    if (authenticityToken) {
        currentUser = [self getUserWithUsername:username];
        return TRUE;
    }
    
    return FALSE;
}

/**
 * Log out of iSENSE.
 */
-(void)deleteSession {
    
    NSString *parameters = [NSString stringWithFormat:@"authenticity_token=%@", [self getEncodedAuthtoken]];
    [self makeRequestWithBaseUrl:baseUrl withPath:@"login" withParameters:parameters withRequestType:DELETE andPostData:nil];
    currentUser = nil;
    
}

/**
 * Retrieves information about a single project on iSENSE.
 *
 * @param projectId The ID of the project to retrieve
 * @return A Project object
 */
-(RProject *)getProjectWithId:(int)projectId {
    
    RProject *proj = [[RProject alloc] init];
    
    NSString *path = [NSString stringWithFormat:@"projects/%d", projectId];
    NSDictionary *results = [self makeRequestWithBaseUrl:baseUrl withPath:path withParameters:@"" withRequestType:GET andPostData:nil];
    
    proj.project_id = [results objectForKey:@"id"];
    proj.name = [results objectForKey:@"name"];
    proj.url = [results objectForKey:@"url"];
    proj.hidden = [results objectForKey:@"hidden"];
    proj.featured = [results objectForKey:@"featured"];
    proj.like_count = [results objectForKey:@"likeCount"];
    proj.timecreated = [results objectForKey:@"createdAt"];
    proj.owner_name = [results objectForKey:@"ownerName"];
    proj.owner_url = [results objectForKey:@"ownerUrl"];
    
    return proj;
    
}


-(RTutorial *)  getTutorialWithId:      (int)tutorialId{ return nil; }
-(RDataSet *)   getDataSetWithId:       (int)dataSetId { return nil; }

/**
 * Gets all of the fields associated with a project.
 *
 * @param projectId The unique ID of the project whose fields you want to see
 * @return An ArrayList of ProjectField objects
 */
-(NSArray *)getProjectFieldsWithId:(int)projectId {
    NSMutableArray *fields = [[NSMutableArray alloc] init];
    
    NSDictionary *requestResult = [self makeRequestWithBaseUrl:baseUrl withPath:[NSString stringWithFormat:@"projects/%d", projectId] withParameters:@"" withRequestType:GET andPostData:nil];
    NSArray *innerFields = [requestResult objectForKey:@"fields"];
    
    for (int i = 0; i < innerFields.count; i++) {
        NSDictionary *innermostField = [innerFields objectAtIndex:i];
        RProjectField *newProjField = [[RProjectField alloc] init];
        
        newProjField.field_id = [innermostField objectForKey:@"id"];
        newProjField.name = [innermostField objectForKey:@"name"];
        newProjField.type = [innermostField objectForKey:@"type"];
        newProjField.unit = [innermostField objectForKey:@"unit"];

        [fields addObject:newProjField];
    }
    
    return fields;
}


-(NSArray *)    getDataSetsWithId:      (int)projectId { return nil; }

/**
 * 	Retrieves multiple projects off of iSENSE.
 *
 * @param page Which page of results to start from. 1-indexed
 * @param perPage How many results to display per page
 * @param descending Whether to display the results in descending order (true) or ascending order (false)
 * @param search A string to search all projects for
 * @return An ArrayList of Project objects
 */
-(NSArray *)getProjectsAtPage:(int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search {
    NSMutableArray *results = [[NSMutableArray alloc] init];
    NSString *sortMode = descending ? @"DESC" : @"ASC";
    NSString *parameters = [NSString stringWithFormat:@"page=%d&per_page=%d&sort=%s&search=%s", page, perPage, sortMode.UTF8String, search.UTF8String];
    NSArray *reqResult = (NSArray *)[self makeRequestWithBaseUrl:baseUrl withPath:@"projects" withParameters:parameters withRequestType:GET andPostData:nil];
    
    for (NSDictionary *innerProjJSON in reqResult) {
        RProject *proj = [[RProject alloc] init];
        
        proj.project_id = [innerProjJSON objectForKey:@"id"];
        proj.name = [innerProjJSON objectForKey:@"name"];
        proj.url = [innerProjJSON objectForKey:@"url"];
        proj.hidden = [innerProjJSON objectForKey:@"hidden"];
        proj.featured = [innerProjJSON objectForKey:@"featured"];
        proj.like_count = [innerProjJSON objectForKey:@"likeCount"];
        proj.timecreated = [innerProjJSON objectForKey:@"createdAt"];
        proj.owner_name = [innerProjJSON objectForKey:@"ownerName"];
        proj.owner_url = [innerProjJSON objectForKey:@"ownerUrl"];
        
        NSLog(@"%@", proj);
        
        [results addObject:proj];
        
    }
    
    return results;
    
}
-(RTutorial *)  getTutorialsAtPage: (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search { return nil; }

/* Requires an Authentication Key */
-(NSArray *)    getUsersAtPage:     (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search { return nil; }


/*
 * Returns the current saved user object.
 *
 * @return
 */
-(RPerson *)getCurrentUser {
    return currentUser;
}

/**
 * Gets a user off of iSENSE.
 *
 * @param username The username of the user to retrieve
 * @return A Person object
 */
-(RPerson *)getUserWithUsername:(NSString *)username {
    
    RPerson *person = [[RPerson alloc] init];
    NSString *path = [NSString stringWithFormat:@"users/%@", username];
    NSDictionary *result = [self makeRequestWithBaseUrl:baseUrl withPath:path withParameters:@"" withRequestType:GET andPostData:nil];
    person.person_id = [result objectForKey:@"id"];
    person.name = [result objectForKey:@"name"];
    person.username = [result objectForKey:@"username"];
    person.url = [result objectForKey:@"url"];
    person.gravatar = [result objectForKey:@"gravatar"];
    person.timecreated = [result objectForKey:@"createdAt"];
    person.hidden = [result objectForKey:@"hidden"];
    
    return person;
}

/**
 * Creates a new project on iSENSE. The Field objects in the second parameter must have
 * at a type and a name, and can optionally have a unit. This is an authenticated function.
 *
 * @param name The name of the new project to be created
 * @param fields An ArrayList of field objects that will become the fields on iSENSE.
 * @return The ID of the created project
 */
-(int)createProjectWithName:(NSString *)name andFields:(NSArray *)fields {
    
    NSMutableDictionary *postData = [[NSMutableDictionary alloc] init];
    [postData setObject:name forKey:@"project_name"];
    
    NSString *parameters = [NSString stringWithFormat:@"authenticity_token=%@", [self getEncodedAuthtoken]];
    NSData *postReqData = [NSKeyedArchiver archivedDataWithRootObject:fields];
    
    NSDictionary *requestResult = [self makeRequestWithBaseUrl:baseUrl withPath:@"projects" withParameters:parameters withRequestType:POST andPostData:postReqData];
    
    NSNumber *projectId = [requestResult objectForKey:@"id"];
    
    for (RProjectField *projField in fields) {
        NSMutableDictionary *fieldMetaData = [[NSMutableDictionary alloc] init];
        [fieldMetaData setObject:projectId forKey:@"project_id"];
        [fieldMetaData setObject:projField.type forKey:@"field_type"];
        [fieldMetaData setObject:projField.name forKey:@"name"];
        [fieldMetaData setObject:projField.unit forKey:@"unit"];
        
        NSMutableDictionary *fullFieldMeta = [[NSMutableDictionary alloc] init];
        [fullFieldMeta setObject:fieldMetaData forKey:@"field"];
        [fullFieldMeta setObject:projectId forKey:@"project_id"];
        
        NSData *fieldPostReqData = [NSKeyedArchiver archivedDataWithRootObject:fieldMetaData];
        [self makeRequestWithBaseUrl:baseUrl withPath:@"fields" withParameters:parameters withRequestType:POST andPostData:fieldPostReqData];
        
        return projectId.intValue;
    }
    
    return -1;
}

-(void)appendDataSetDataWithId:(int)dataSetId  andData:(NSDictionary *)data {}

/**
 * Uploads a new data set to a project on iSENSE
 *
 * @param projectId The ID of the project to upload data to
 * @param dataToUpload The data to be uploaded. Must be in column-major format to upload correctly
 * @param name The name of the dataset
 * @return The integer ID of the newly uploaded dataset, or -1 if upload fails
 */
-(int)uploadDataSetWithId:(int)projectId withData:(NSDictionary *)dataToUpload andName:(NSString *)name {
    
    NSArray *fields = [self getProjectFieldsWithId:projectId];
    
    NSMutableDictionary *requestData = [[NSMutableDictionary alloc] init];
    NSMutableArray *headers = [[NSMutableArray alloc] init];
    
    for (RProjectField *field in fields) {
        [headers addObject:[NSString stringWithFormat:@"%@", field.field_id]];
    }
    
    [requestData setObject:[NSString stringWithFormat:@"%d", projectId] forKey:@"id"];
    [requestData setObject:headers forKey:@"headers"];
    [requestData setObject:dataToUpload forKey:@"data"];
    if (![name isEqualToString:@""]) [requestData setObject:name forKey:@"name"];
    
    NSString *parameters = [NSString stringWithFormat:@"authenticity_token=%@", [self getEncodedAuthtoken]];
    
    NSError *error;
    NSData *postReqData = [NSJSONSerialization dataWithJSONObject:requestData
                                                       options:0
                                                         error:&error];
    NSLog(@"Parsed JSONObject = %@", [[NSString alloc] initWithData:postReqData encoding:NSUTF8StringEncoding]);
    
    if (error) {
        NSLog(@"Error parsing object to JSON: %@", error);
    }
    
    NSDictionary *requestResult = [self makeRequestWithBaseUrl:baseUrl withPath:[NSString stringWithFormat:@"projects/%d/manualUpload", projectId] withParameters:parameters withRequestType:POST andPostData:postReqData];
    NSNumber *dataSetId = [requestResult objectForKey:@"id"];
    
    NSLog(@"Result = %@", requestResult);
    
    return dataSetId.intValue;

}

-(int)uploadCSVWithId:         (int)projectId withFile:(NSFileHandle *)csvToUpload     andName:(NSString *)name { return -1; }
-(int)uploadProjectMediaWithId:(int)projectId withFile:(NSFileHandle *)mediaToUpload { return -1; }
-(int)uploadDataSetMediaWithId:(int)dataSetId withFile:(NSFileHandle *)mediaToUpload { return -1; }

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

-(NSString *)getEncodedAuthtoken {
    CFStringRef encodedToken = CFURLCreateStringByAddingPercentEscapes(kCFAllocatorDefault, CFBridgingRetain(authenticityToken), NULL, CFSTR("!*'();:@&=+@,/?#[]"), kCFStringEncodingUTF8);
    return CFBridgingRelease(encodedToken);
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
 * @return An object dump of a JSONObject or JSONArray representing the requested data
 */
-(id)makeRequestWithBaseUrl:(NSString *)baseUrl withPath:(NSString *)path withParameters:(NSString *)parameters withRequestType:(NSString *)reqType andPostData:(NSData *)postData {
    
    NSURL *url = [[NSURL alloc] initWithString:[NSString stringWithFormat:@"%@/%@?%@", baseUrl, path, parameters]];
    NSLog(@"Connect to: %@", url);
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url
                                                           cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                                       timeoutInterval:10];
    [request setHTTPMethod:reqType];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    
    if (postData) {
        [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
        [request setValue:[NSString stringWithFormat:@"%d", postData.length] forHTTPHeaderField:@"Content-Length"];
        [request setHTTPBody:postData];
    }
    
    NSError *requestError;
    NSHTTPURLResponse *urlResponse;
    
    NSData *dataResponse = [NSURLConnection sendSynchronousRequest:request returningResponse:&urlResponse error:&requestError];
    if (requestError) NSLog(@"Error received from server: %@", requestError);
    
    if (urlResponse.statusCode == 200) {
        id parsedJSONResponse = [NSJSONSerialization JSONObjectWithData:dataResponse options:NSJSONReadingMutableContainers error:&requestError];
        return parsedJSONResponse;
    } else if (urlResponse.statusCode == 403) {
        NSLog(@"Authenticity token not accepted. %@", [[NSString alloc] initWithData:dataResponse encoding:NSUTF8StringEncoding]);
    } else if (urlResponse.statusCode == 422) {
        NSLog(@"Unprocessable entity. %@", [[NSString alloc] initWithData:dataResponse encoding:NSUTF8StringEncoding]);
    } else if (urlResponse.statusCode == 500) {
        NSLog(@"Internal server error. %@", [[NSString alloc] initWithData:dataResponse encoding:NSUTF8StringEncoding]);
    } else {
        NSLog(@"Unrecognized status code = %d. %@", urlResponse.statusCode, [[NSString alloc] initWithData:dataResponse encoding:NSUTF8StringEncoding]);
    }
    
    return nil;
}



@end
