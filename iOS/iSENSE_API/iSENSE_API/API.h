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
#import "Reachability.h"

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
-(BOOL)createSessionWithUsername:(NSString *)username andPassword:(NSString *)password;
-(void)deleteSession;

/* Doesn't Require Authentication Key */
-(RProject *)   getProjectWithId:       (int)projectId;
-(RTutorial *)  getTutorialWithId:      (int)tutorialId;
-(RDataSet *)   getDataSetWithId:       (int)dataSetId;
-(NSArray *)    getProjectFieldsWithId: (int)projectId;
-(NSArray *)    getDataSetsWithId:      (int)projectId;

-(NSArray *)    getProjectsAtPage:  (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search;
-(RTutorial *)  getTutorialsAtPage: (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search;

/* Requires an Authentication Key */
-(NSArray *)    getUsersAtPage:     (int)page withPageLimit:(int)perPage withFilter:(BOOL)descending andQuery:(NSString *)search;

-(RPerson *)    getCurrentUser;
-(RPerson *)    getUserWithUsername:(NSString *)username;
-(int)          createProjectWithName:(NSString *)name  andFields:(NSArray *)fields;
-(void)         appendDataSetDataWithId:(int)dataSetId  andData:(NSDictionary *)data;

-(int)      uploadDataSetWithId:     (int)projectId withData:(NSDictionary *)dataToUpload    andName:(NSString *)name;
-(int)      uploadCSVWithId:         (int)projectId withFile:(NSFileHandle *)csvToUpload     andName:(NSString *)name;
-(int)      uploadProjectMediaWithId:(int)projectId withFile:(NSFileHandle *)mediaToUpload;
-(int)      uploadDataSetMediaWithId:(int)dataSetId withFile:(NSFileHandle *)mediaToUpload;

/* Convenience Method for Uploading */
-(NSDictionary *)rowsToCols:(NSDictionary *)original;


@end
