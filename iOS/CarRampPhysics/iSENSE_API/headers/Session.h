//
//  Session.h
//  isenseAPI
//
//  Created by James Dalphond on 2/23/11.
//  Copyright 2011 UMass Lowell. All rights reserved.
//
//  Modified by John Fertitta on 3/1/11.
//

#import <Foundation/Foundation.h>

@interface Session : NSObject {

}

/*Properties for getting/setting variables*/
@property (assign) NSNumber *experiment_id;
@property (assign) NSNumber *owner_id;
@property (assign) NSNumber *session_id;

@property (assign) NSNumber *latitude;
@property (assign) NSNumber *longitude;

@property (assign) NSString *name;
@property (assign) NSString *city;
@property (assign) NSString *country;
@property (assign) NSString *debug_data;
@property (assign) NSString *description;
@property (assign) NSString *firstname;
@property (assign) NSString *imageURL;
@property (assign) NSString *lastname;
@property (assign) NSString *street;
@property (assign) NSString *timecreated;
@property (assign) NSString *timemodified;

@end

