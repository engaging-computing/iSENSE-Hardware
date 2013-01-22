//
//  Experiment.h
//  isenseAPI
//
//  Created by James Dalphond on 2/23/11.
//  Copyright 2011 UMass Lowell. All rights reserved.
//
//  Modified by John Fertitta on 3/1/11.
//

#import <Foundation/Foundation.h>


@interface Experiment : NSObject {
	
	NSNumber *default_join;
	NSNumber *default_read;
	NSNumber *experiment_id;
	NSNumber *featured;
	NSNumber *hidden;
	NSNumber *owner_id;	
	NSNumber *rating;
	NSNumber *rating_votes;
	NSNumber *session_count;
	NSString *timecreated;
	NSString *timemodified;
	NSString *provider_url;
	NSString *name;
	NSString *firstname;
    NSString *lastname;
	NSString *description;
    NSNumber *srate;
}

/* Properties for getting/setting variables */
@property (assign) NSNumber *default_join;
@property (assign) NSNumber *default_read;
@property (assign) NSNumber *experiment_id;
@property (assign) NSNumber *featured;
@property (assign) NSNumber *hidden;
@property (assign) NSNumber *owner_id;
@property (assign) NSNumber *rating;
@property (assign) NSNumber *rating_votes;
@property (assign) NSNumber *session_count;
@property (assign) NSString *timecreated;
@property (assign) NSString *timemodified;
@property (assign) NSString *provider_url;
@property (assign) NSString *name;
@property (assign) NSString *firstname;
@property (assign) NSString *lastname;
@property (assign) NSString *description;
@property (assign) NSNumber *srate;

@end
