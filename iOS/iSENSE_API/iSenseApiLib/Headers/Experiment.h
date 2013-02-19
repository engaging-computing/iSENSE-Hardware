//
//  Experiment.h
//  isenseAPI
//
//  Created by James Dalphond on 2/23/11.
//  copyright 2011 UMass Lowell. All rights reserved.
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
@property (copy) NSNumber *default_join;
@property (copy) NSNumber *default_read;
@property (copy) NSNumber *experiment_id;
@property (copy) NSNumber *featured;
@property (copy) NSNumber *hidden;
@property (copy) NSNumber *owner_id;
@property (copy) NSNumber *rating;
@property (copy) NSNumber *rating_votes;
@property (copy) NSNumber *session_count;
@property (copy) NSString *timecreated;
@property (copy) NSString *timemodified;
@property (copy) NSString *provider_url;
@property (copy) NSString *name;
@property (copy) NSString *firstname;
@property (copy) NSString *lastname;
@property (copy) NSString *description;
@property (copy) NSNumber *activity;
@property (copy) NSNumber *activity_for;
@property (copy) NSNumber *req_name;
@property (copy) NSNumber *req_procedure;
@property (copy) NSNumber *req_location;
@property (copy) NSString *name_prefix;
@property (copy) NSString *location;
@property (copy) NSNumber *closed;
@property (copy) NSURL *exp_image;
@property (copy) NSNumber *recommended;
@property (copy) NSNumber *srate;
@property (copy) NSString *default_vis;
@property (copy) NSString *rating_comp;
@property (copy) NSArray *tags;
@property (copy) NSNumber *relevancy;
@property (copy) NSNumber *contrib_count;

@end
