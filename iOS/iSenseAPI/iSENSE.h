//
//  iSENSE.h
//  isenseAPI
//
//  Created by John Fertitta on 2/23/11.
//  Copyright 2011 UMass Lowell. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Experiment.h"
#import "ExperimentField.h"
#import "Image.h"
#import "Item.h"
#import "Person.h"
#import "Session.h"
#import "SessionData.h"
#import "JSON.h"

@interface iSENSE : NSObject {
	NSString *username;
	NSString *session_key;
	
	NSNumber * uid;
}

-(NSDictionary *)isenseQuery:(NSString*)target;
+ (iSENSE *) instance;
- (NSString *) getSessionKey;
- (NSNumber *) getUID;
- (bool) isLoggedIn;
- (NSString *) getLoggedInUsername;
- (void) logout;
//- (bool) upload:(NSFile)Picture toExperiment:(NSNumber *)exp_id withName:(NSString *)name andDescirption:(NSString *)description;
- (bool) login:(NSString *)User with:(NSString *)Password;
- (Experiment *) getExperiment:(NSNumber *)exp_id;
- (NSMutableArray *) sessionData:(NSString *)sessions;
- (NSMutableArray *) getPeople:(NSNumber *)fromPage withPageSize:(NSNumber *)limit withAction:(NSString *)action andQuery:(NSString *)query;
- (Item *) getProfile:(NSNumber *)user_id;
- (NSMutableArray *) getExperiments:(NSNumber *)fromPage withPageSize:(NSNumber *)limit withAction:(NSString *)action andQuery:(NSString *)query;
- (NSMutableArray *) getExperimentImages:(NSNumber *)exp_id;
- (NSMutableArray *) getExperimentVideos:(NSNumber *)exp_id;
- (NSMutableArray *) getExperimentTags:(NSNumber *)exp_id;
- (NSMutableArray *) getExperimentFields:(NSNumber *)exp_id;
- (NSMutableArray *) getSessions:(NSNumber *)exp_id;
- (NSNumber *) createSession:(NSString *)name withDescription:(NSString *)description Street:(NSString *)street City:(NSString *)city Country:(NSString *)country toExperiment:(NSNumber *)exp_id;
- (bool) putSessionData:(NSString *)dataJSON forSession:(NSNumber *)session_id inExperiment:(NSNumber *)exp_id;
- (bool) updateSessionData:(NSString *)dataJSON forSession:(NSNumber *)sessioN_id inExperiment:(NSNumber *)exp_id;

@end


