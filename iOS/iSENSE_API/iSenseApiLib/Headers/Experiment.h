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
    NSNumber *activity;
    NSNumber *activity_for;
    NSNumber *req_name;
    NSNumber *req_procedure;
    NSNumber *req_location;
    NSString *name_prefix;
    NSString *location;
    NSNumber *closed;
    NSURL *exp_image;
    NSNumber *recommended;
    NSNumber *srate;
    NSString *default_vis;
    NSString *rating_comp;
    NSArray *tags;
    NSNumber *relevancy;
    NSNumber *contrib_count;
}

/* Properties for getting/setting variables */
@property (retain) NSNumber *default_join;
@property (retain) NSNumber *default_read;
@property (retain) NSNumber *experiment_id;
@property (retain) NSNumber *featured;
@property (retain) NSNumber *hidden;
@property (retain) NSNumber *owner_id;
@property (retain) NSNumber *rating;
@property (retain) NSNumber *rating_votes;
@property (retain) NSNumber *session_count;
@property (retain) NSString *timecreated;
@property (retain) NSString *timemodified;
@property (retain) NSString *provider_url;
@property (retain) NSString *name;
@property (retain) NSString *firstname;
@property (retain) NSString *lastname;
@property (retain) NSString *description;
@property (retain) NSNumber *activity;
@property (retain) NSNumber *activity_for;
@property (retain) NSNumber *req_name;
@property (retain) NSNumber *req_procedure;
@property (retain) NSNumber *req_location;
@property (retain) NSString *name_prefix;
@property (retain) NSString *location;
@property (retain) NSNumber *closed;
@property (retain) NSURL *exp_image;
@property (retain) NSNumber *recommended;
@property (retain) NSNumber *srate;
@property (retain) NSString *default_vis;
@property (retain) NSString *rating_comp;
@property (retain) NSArray *tags;
@property (retain) NSNumber *relevancy;
@property (retain) NSNumber *contrib_count;

@end
