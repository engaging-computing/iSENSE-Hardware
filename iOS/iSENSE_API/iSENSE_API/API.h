//
//  API.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 8/21/13.
//  Copyright (c) 2013 Engaging Computing Group, UML. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <RProject.h>
#import <RTutorial.h>
#import <RPerson.h>
#import <RDataSet.h>
#import <RNews.h>
#import <RProjectField.h>
#import "Reachability.h"
#import <MobileCoreServices/UTType.h>
#import <sys/time.h>
#import <AFHTTPRequestOperationManager.h>

// Version number of the API tested and passed on this version
// number of the production iSENSE website.
#define VERSION_MAJOR @"4"
#define VERSION_MINOR @"1"

typedef enum {
    CREATED_AT_DESC,
    CREATED_AT_ASC,
    UPDATED_AT_DESC,
    UPDATED_AT_ASC
} SortType;

typedef enum {
    PROJECT,
    DATA_SET
} TargetType;

@interface API : NSObject {
}

/* getInstance */
+(API *)getInstance;

/* Checks for Connectivity */
+(BOOL)hasConnectivity;

/* Change the baseUrl Value */
-(void)useDev:(BOOL)useDev;
-(void)setBaseUrl:(NSURL *)newUrl;

/* Manage Authentication Key */
-(RPerson *)createSessionWithEmail:(NSString *)p_email andPassword:(NSString *)p_password;
-(void)deleteSession;

/* Doesn't Require Authentication Key */
-(RProject *)   getProjectWithId:       (int)projectId;
-(RTutorial *)  getTutorialWithId:      (int)tutorialId;
-(RDataSet *)   getDataSetWithId:       (int)dataSetId;
-(RNews *)      getNewsWithId:          (int)newsId;
-(NSArray *)    getProjectFieldsWithId: (int)projectId;
-(NSArray *)    getDataSetsWithId:      (int)projectId;

-(NSArray *)    getNewsAtPage:      (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search;
-(NSArray *)    getProjectsAtPage:  (int)page withPageLimit:(int)perPage withFilter:(SortType)descending andQuery:(NSString *)search;
-(NSArray *)    getTutorialsAtPage: (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search;

/* Requires an Authentication Key */
-(NSArray *)    getUsersAtPage:     (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search;

-(RPerson *)    getCurrentUser;
-(int)          createProjectWithName:(NSString *)name  andFields:(NSArray *)fields;
-(void)         appendDataSetDataWithId:(int)dataSetId  andData:(NSDictionary *)data;

-(int) uploadDataWithId:(int)projectId withData:(NSDictionary *)dataToUpload andName:(NSString *)name;
-(int) uploadDataWithId:(int)projectId withData:(NSDictionary *)dataToUpload withContributorKey:(NSString *) conKey as:(NSString *) conName;
-(int)      uploadCSVWithId:         (int)projectId withFile:(NSData *)csvToUpload     andName:(NSString *)name;
-(int) uploadMediaWithId:(int)projectId withFile:(NSData *)mediaToUpload andName:(NSString *) name withTarget: (TargetType) ttype;
-(int) uploadMediaWithId:(int)projectId withFile:(NSData *)mediaToUpload andName:(NSString *) name withTarget: (TargetType) ttype withContributorKey:(NSString *) conKey as:(NSString *) conName;

/* Other methods */
-(NSDictionary *)rowsToCols:(NSDictionary *)original;
-(NSString *) getVersion;

@end
