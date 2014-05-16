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

-(RPerson *)createSessionWithEmail:(NSString *)p_email andPassword:(NSString *)p_password;
-(void)deleteSession;

-(RProject *)   getProjectWithId:       (int)projectId;
-(RDataSet *)   getDataSetWithId:       (int)dataSetId;
-(NSArray *)    getProjectFieldsWithId: (int)projectId;
-(NSArray *)    getDataSetsWithId:      (int)projectId;

-(NSArray *)    getProjectsAtPage:  (int)page withPageLimit:(int)perPage withFilter:(SortType)descending andQuery:(NSString *)search;

-(RPerson *)    getCurrentUser;
-(int)          createProjectWithName:(NSString *)name  andFields:(NSArray *)fields;
-(void)         appendDataSetDataWithId:(int)dataSetId  andData:(NSDictionary *)data;
-(void)         appendDataSetDataWithId:(int)dataSetId  andData:(NSDictionary *)data withContributorKey:(NSString *)conKey;

-(int) uploadDataWithId:(int)projectId withData:(NSDictionary *)dataToUpload andName:(NSString *)name;
-(int) uploadDataWithId:(int)projectId withData:(NSDictionary *)dataToUpload withContributorKey:(NSString *) conKey as:(NSString *) conName andName:(NSString *)name;
-(int) uploadMediaWithId:(int)projectId withFile:(NSData *)mediaToUpload andName:(NSString *) name withTarget: (TargetType) ttype;
-(int) uploadMediaWithId:(int)projectId withFile:(NSData *)mediaToUpload andName:(NSString *) name withTarget: (TargetType) ttype withContributorKey:(NSString *) conKey as:(NSString *) conName;

/* Other methods */
-(NSDictionary *)rowsToCols:(NSDictionary *)original;
-(NSString *) getVersion;

@end
