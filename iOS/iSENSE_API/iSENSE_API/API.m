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
#define NONE    @""

#define BOUNDARY @"*****"

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
 * @return An RProject object
 */
-(RProject *)getProjectWithId:(int)projectId {
    
    RProject *proj = [[RProject alloc] init];
    
    NSString *path = [NSString stringWithFormat:@"projects/%d", projectId];
    NSDictionary *results = [self makeRequestWithBaseUrl:baseUrl withPath:path withParameters:NONE withRequestType:GET andPostData:nil];
    
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

/**
 * Get a tutorial from iSENSE.
 *
 * @param tutorialId The ID of the tutorial to retrieve
 * @return A RTutorial object
 */
-(RTutorial *)getTutorialWithId:(int)tutorialId {
    RTutorial *tutorial = [[RTutorial alloc] init];
    
    NSDictionary *results = [self makeRequestWithBaseUrl:baseUrl withPath:[NSString stringWithFormat:@"tutorials/%d", tutorialId] withParameters:NONE withRequestType:GET andPostData:nil];
    tutorial.tutorial_id = [results objectForKey:@"id"];
    tutorial.name = [results objectForKey:@"name"];
    tutorial.url = [results objectForKey:@"url"];
    tutorial.hidden = [results objectForKey:@"hidden"];
    tutorial.timecreated = [results objectForKey:@"createdAt"];
    tutorial.owner_name = [results objectForKey:@"ownerName"];
    tutorial.owner_url = [results objectForKey:@"ownerUrl"];
    
    return tutorial;
}

/**
 * Retrieve a data set from iSENSE, with it's data field filled in.
 * The internal data set will be converted to column-major format, to make it compatible with
 * the uploadDataSet function
 *
 * @param dataSetId The unique ID of the data set to retrieve from iSENSE
 * @return An RDataSet object
 */
-(RDataSet *)getDataSetWithId:(int)dataSetId {
    RDataSet *dataSet = [[RDataSet alloc] init];
    
    NSDictionary *results = [self makeRequestWithBaseUrl:baseUrl withPath:[NSString stringWithFormat:@"data_set/%d", dataSetId] withParameters:@"recur=true" withRequestType:GET andPostData:nil];
    
    dataSet.ds_id = [results objectForKey:@"id"];
    dataSet.name = [results objectForKey:@"name"];
    dataSet.hidden = [results objectForKey:@"hidden"];
    dataSet.url = [results objectForKey:@"url"];
    dataSet.timecreated = [results objectForKey:@"createdAt"];
    dataSet.fieldCount = [results objectForKey:@"fieldCount"];
    dataSet.datapointCount = [results objectForKey:@"datapointCount"];
    dataSet.data = [results objectForKey:@"data"];
    dataSet.project_id = [[results objectForKey:@"project"] objectForKey:@"id"];

    return dataSet;
}

/**
 * Gets all of the fields associated with a project.
 *
 * @param projectId The unique ID of the project whose fields you want to see
 * @return An ArrayList of RProjectField objects
 */
-(NSArray *)getProjectFieldsWithId:(int)projectId {
    NSMutableArray *fields = [[NSMutableArray alloc] init];
    
    NSDictionary *requestResult = [self makeRequestWithBaseUrl:baseUrl withPath:[NSString stringWithFormat:@"projects/%d", projectId] withParameters:NONE withRequestType:GET andPostData:nil];
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

/**
 * Gets all the data sets associated with a project
 * The data sets returned by this function do not have their data field filled.
 *
 * @param projectId The project ID whose data sets you want
 * @return An ArrayList of RDataSet objects, with their data fields left null
 */
-(NSArray *)getDataSetsWithId:(int)projectId {
    NSMutableArray *dataSets = [[NSMutableArray alloc] init];
    
    NSDictionary *results = [self makeRequestWithBaseUrl:baseUrl withPath:[NSString stringWithFormat:@"projects/%d", projectId] withParameters:@"recur=true" withRequestType:GET andPostData:nil];
    NSArray *resultsArray = [results objectForKey:@"dataSets"];
    for (int i = 0; i < results.count; i++) {
        RDataSet *dataSet = [[RDataSet alloc] init];
        NSDictionary *innermost = [resultsArray objectAtIndex:i];
        
        dataSet.ds_id = [innermost objectForKey:@"id"];
        dataSet.name = [innermost objectForKey:@"name"];
        dataSet.hidden = [innermost objectForKey:@"hidden"];
        dataSet.url = [innermost objectForKey:@"url"];
        dataSet.timecreated = [innermost objectForKey:@"createdAt"];
        dataSet.fieldCount = [innermost objectForKey:@"fieldCount"];
        dataSet.datapointCount = [innermost objectForKey:@"datapointCount"];
        
        [dataSets addObject:dataSet];
    }
    
    return dataSets;
}

/**
 * 	Retrieves multiple projects off of iSENSE.
 *
 * @param page Which page of results to start from. 1-indexed
 * @param perPage How many results to display per page
 * @param descending Whether to display the results in descending order (true) or ascending order (false)
 * @param search A string to search all projects for
 * @return An ArrayList of RProject objects
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

/**
 * Retrieves multiple tutorials off of iSENSE.
 *
 * @param page Which page of results to start from. 1-indexed
 * @param perPage How many results to display per page
 * @param descending Whether to display the results in descending order (true) or ascending order (false)
 * @param search A string to search all tutorials for
 * @return An ArrayList of RTutorial objects
 */
-(NSArray *)getTutorialsAtPage:(int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search {
    
    NSMutableArray *tutorials = [[NSMutableArray alloc] init];
    
    NSString *sortMode = descending ? @"DESC" : @"ASC";
    NSString *parameters = [NSString stringWithFormat:@"authenticity_token=%@&page=%d&per_page%d&sort=%s&search=%s", [self getEncodedAuthtoken], page, perPage, sortMode.UTF8String, search.UTF8String];

    NSArray *results = [self makeRequestWithBaseUrl:baseUrl withPath:@"tutorials" withParameters:parameters withRequestType:GET andPostData:nil];
    for (int i = 0; i < results.count; i++) {
        NSDictionary *inner = [results objectAtIndex:i];
        RTutorial *tutorial = [[RTutorial alloc] init];
        
        tutorial.tutorial_id = [inner objectForKey:@"id"];
        tutorial.name = [inner objectForKey:@"name"];
        tutorial.url = [inner objectForKey:@"url"];
        tutorial.hidden = [inner objectForKey:@"hidden"];
        tutorial.timecreated = [inner objectForKey:@"createdAt"];
        tutorial.owner_name = [inner objectForKey:@"ownerName"];
        tutorial.owner_url = [inner objectForKey:@"ownerUrl"];
        
        [tutorials addObject:tutorial];
    }
    
    return tutorials;
}

/**
 * Retrieves a list of users on iSENSE.
 * This is an authenticated function and requires that the createSession function was called earlier.
 *
 * @param page Which page of users to start the request from
 * @param perPage How many users per page to perform the search with
 * @param descending Whether the list of users should be in descending order or not
 * @param search A string to search all users for
 * @return A list of RPerson objects
 */
-(NSArray *)getUsersAtPage:(int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search {
    
    NSMutableArray *persons = [[NSMutableArray alloc] init];
    
    NSString *sortMode = descending ? @"DESC" : @"ASC";
    NSString *parameters = [NSString stringWithFormat:@"authenticity_token=%@&page=%d&per_page%d&sort=%s&search=%s", [self getEncodedAuthtoken], page, perPage, sortMode.UTF8String, search.UTF8String];

    NSArray *results = [self makeRequestWithBaseUrl:baseUrl withPath:@"users" withParameters:parameters withRequestType:GET andPostData:nil];
    for (int i = 0; i < results.count; i++) {
        NSDictionary *inner = [results objectAtIndex:i];
        RPerson *person = [[RPerson alloc] init];
        
        person.person_id = [inner objectForKey:@"id"];
        person.name = [inner objectForKey:@"name"];
        person.username = [inner objectForKey:@"username"];
        person.url = [inner objectForKey:@"url"];
        person.gravatar = [inner objectForKey:@"gravatar"];
        person.timecreated = [inner objectForKey:@"createdAt"];
        person.hidden = [inner objectForKey:@"hidden"];
        
        [persons addObject:person];
    }
    
    return persons;
}

/*
 * Returns the current saved user object.
 *
 * @return An RPerson object that corresponds to the owner of the current session
 */
-(RPerson *)getCurrentUser {
    return currentUser;
}

/**
 * Gets a user off of iSENSE.
 *
 * @param username The username of the user to retrieve
 * @return An RPerson object
 */
-(RPerson *)getUserWithUsername:(NSString *)username {
    
    RPerson *person = [[RPerson alloc] init];
    NSString *path = [NSString stringWithFormat:@"users/%@", username];
    NSDictionary *result = [self makeRequestWithBaseUrl:baseUrl withPath:path withParameters:NONE withRequestType:GET andPostData:nil];
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

// TODO
-(void)appendDataSetDataWithId:(int)dataSetId  andData:(NSDictionary *)data {}

/**
 * Uploads a new data set to a project on iSENSE.
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
    if (![name isEqualToString:NONE]) [requestData setObject:name forKey:@"name"];
    
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

/*
 * Gets the MIME time from a file path.
 */
-(NSString *)getMimeType:(NSString *)path{
    
    CFStringRef pathExtension = (__bridge_retained CFStringRef)[path pathExtension];
    CFStringRef type = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, pathExtension, NULL);
    
    // The UTI can be converted to a mime type:
    NSString *mimeType = (__bridge_transfer NSString *)UTTypeCopyPreferredTagWithClass(type, kUTTagClassMIMEType);
    
    return mimeType;
}


// TODO
-(int)uploadCSVWithId:         (int)projectId withFile:(NSData *)csvToUpload andName:(NSString *)name { return -1; }

/**
 * Uploads a file to the media section of a project.
 *
 * @param projectId The project ID to upload to
 * @param mediaToUpload The file to upload
 * @return ??? or -1 if upload fails
 */
-(int)uploadProjectMediaWithId:(int)projectId withFile:(NSData *)mediaToUpload andName:(NSString *)name {
       
    // Make sure there aren't any characters in the name
    name = [name stringByReplacingOccurrencesOfString:@" " withString:@"+"];
    
    // Tries to get the mime type of the specified file
    NSString *mimeType = [self getMimeType:name];
   
    // create request
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
    [request setCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
    [request setHTTPShouldHandleCookies:YES];
    [request setTimeoutInterval:30];
    [request setHTTPMethod:POST];
    
    // set Content-Type in HTTP header
    NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@", BOUNDARY];
    [request setValue:contentType forHTTPHeaderField: @"Content-Type"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    
    // post body
    NSMutableData *body = [NSMutableData data];
    
    // add image data   
    if (mediaToUpload) {
        [body appendData:[[NSString stringWithFormat:@"--%@\r\n", BOUNDARY] dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"file\"; filename=\"%@\"\r\n", name] dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:[[NSString stringWithFormat:@"Content-Type: %@\r\nContent-Transfer-Encoding: binary\r\n\r\n", mimeType] dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:mediaToUpload];
        [body appendData:[[NSString stringWithFormat:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding]];
    }
    
    [body appendData:[[NSString stringWithFormat:@"--%@--\r\n", BOUNDARY] dataUsingEncoding:NSUTF8StringEncoding]];
    
    // setting the body of the post to the reqeust
    [request setHTTPBody:body];
    
    // set the content-length
    NSString *postLength = [NSString stringWithFormat:@"%d", [body length]];
    [request setValue:postLength forHTTPHeaderField:@"Content-Length"];
    
    // set URL
    [request setURL:[NSURL URLWithString:[NSString stringWithFormat:@"%@/media_objects/saveMedia/project/%d?authenticity_token=%@", baseUrl, projectId, [self getEncodedAuthtoken]]]];
    NSLog(@"%@", request);
    
    // do the request thang
    NSError *requestError;
    NSHTTPURLResponse *urlResponse;
    
    [NSURLConnection sendSynchronousRequest:request returningResponse:&urlResponse error:&requestError];
    if (requestError) {
        NSLog(@"Error received from server: %@", requestError);
        return -1;
    }
       
    return [urlResponse statusCode];
}

/**
 * Uploads a file to the media section of a data set.
 *
 * @param dataSetId The data set ID to upload to
 * @param mediaToUpload The file to upload
 * @return ??? or -1 if upload fails
 */
-(int)uploadDataSetMediaWithId:(int)dataSetId withFile:(NSData *)mediaToUpload andName:(NSString *)name {
    
    // Make sure there aren't any characters in the name
    name = [name stringByReplacingOccurrencesOfString:@" " withString:@"+"];
    
    // Tries to get the mime type of the specified file
    NSString *mimeType = [self getMimeType:name];
    
    // create request
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
    [request setCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
    [request setHTTPShouldHandleCookies:YES];
    [request setTimeoutInterval:30];
    [request setHTTPMethod:POST];
    
    // set Content-Type in HTTP header
    NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@", BOUNDARY];
    [request setValue:contentType forHTTPHeaderField: @"Content-Type"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    
    // post body
    NSMutableData *body = [NSMutableData data];
    
    // add image data
    if (mediaToUpload) {
        [body appendData:[[NSString stringWithFormat:@"--%@\r\n", BOUNDARY] dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"file\"; filename=\"%@\"\r\n", name] dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:[[NSString stringWithFormat:@"Content-Type: %@\r\nContent-Transfer-Encoding: binary\r\n\r\n", mimeType] dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:mediaToUpload];
        [body appendData:[[NSString stringWithFormat:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding]];
    }
    
    [body appendData:[[NSString stringWithFormat:@"--%@--\r\n", BOUNDARY] dataUsingEncoding:NSUTF8StringEncoding]];
    
    // setting the body of the post to the reqeust
    [request setHTTPBody:body];
    
    // set the content-length
    NSString *postLength = [NSString stringWithFormat:@"%d", [body length]];
    [request setValue:postLength forHTTPHeaderField:@"Content-Length"];
    
    // set URL
    [request setURL:[NSURL URLWithString:[NSString stringWithFormat:@"%@/media_objects/saveMedia/data_set/%d?authenticity_token=%@", baseUrl, dataSetId, [self getEncodedAuthtoken]]]];
    NSLog(@"%@", request);
    
    // do the request thang
    NSError *requestError;
    NSHTTPURLResponse *urlResponse;
    
    [NSURLConnection sendSynchronousRequest:request returningResponse:&urlResponse error:&requestError];
    if (requestError) {
        NSLog(@"Error received from server: %@", requestError);
        return -1;
    }
    
    return [urlResponse statusCode];

}

/**
 * Reformats a row-major NSDictionary to column-major.
 *
 * @param original The row-major formatted NSDictionary
 * @return A column-major reformatted version of the original NSDictionary
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
  * Bro, do you even read the function names?
  *
  * @return An percent escaped version of the current user authentication token
  */
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
    
    if (urlResponse.statusCode >= 200 && urlResponse.statusCode < 300) {
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

/**
 * Retrieves a list of news articles on iSENSE.
 *
 * @param page Which page of news to start the request from
 * @param perPage How many entries per page to perform the search with
 * @param descending Whether the list of articles should be in descending order or not
 * @param search A string to search all articles for
 * @return A list of RNews objects
 */
-(NSArray *)getNewsAtPage:(int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search {
    NSMutableArray *newsArray = [[NSMutableArray alloc] init];

    NSString *sortMode = descending ? @"DESC" : @"ASC";
    NSString *parameters = [NSString stringWithFormat:@"authenticity_token=%@&page=%d&per_page%d&sort=%s&search=%s", [self getEncodedAuthtoken], page, perPage, sortMode.UTF8String, search.UTF8String];

    NSArray *results = [self makeRequestWithBaseUrl:baseUrl withPath:@"news" withParameters:parameters withRequestType:GET andPostData:nil];
    for (int i = 0; i < results.count; i++) {
        NSDictionary *inner = [results objectAtIndex:i];
        RNews *news = [[RNews alloc] init];
    
        news.news_id = [inner objectForKey:@"id"];
        news.name = [inner objectForKey:@"name"];
        news.url = [inner objectForKey:@"url"];
        news.hidden = [inner objectForKey:@"hidden"];
        news.timecreated = [inner objectForKey:@"createdAt"];
        news.content = [inner objectForKey:@""];
    
        [newsArray addObject:news];
    }
    
    return newsArray;
}

/**
 * Gets a news article off iSENSE.
 *
 * @param newsId The id of the news entry to retrieve
 * @return An RNews object
 */
-(RNews *)getNewsWithId:(int)newsId {
    RNews *news = [[RNews alloc] init];
    
    NSDictionary *results = [self makeRequestWithBaseUrl:baseUrl withPath:[NSString stringWithFormat:@"news/%d", newsId] withParameters:@"recur=true" withRequestType:GET andPostData:nil];
    news.news_id = [results objectForKey:@"id"];
    news.name = [results objectForKey:@"name"];
    news.hidden = [results objectForKey:@"hidden"];
    news.url = [results objectForKey:@"url"];
    news.timecreated = [results objectForKey:@"createdAt"];
    news.content = [results objectForKey:@"content"];
    
    return news;
}

@end
