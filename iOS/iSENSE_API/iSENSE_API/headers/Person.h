//
//  Person.h
//  iSENSE API
//
//  Created by James Dalphond on 2/23/11.
//  Copyright 2011 UMass Lowell. All rights reserved.
//
//  Modified by John Fertitta on 3/1/11.
//

#import <Foundation/Foundation.h>

@interface Person : NSObject {

}

/*Properties for getting/setting variables*/
@property (assign) NSNumber *confirmed;
@property (assign) NSNumber *user_id;

@property (assign) NSNumber *experiment_count;
@property (assign) NSNumber *latitude;
@property (assign) NSNumber *longitude;
@property (assign) NSNumber *session_count;

@property (assign) NSString *aim;
@property (assign) NSString *city;
@property (assign) NSString *country;
@property (assign) NSString *date_diff;
@property (assign) NSString *department;
@property (assign) NSString *email;
@property (assign) NSString *firstaccess;
@property (assign) NSString *firstname;
@property (assign) NSString *icq;
@property (assign) NSString *institution;
@property (assign) NSString *language;
@property (assign) NSString *lastaccess;
@property (assign) NSString *lastlogin;
@property (assign) NSString *lastname;
@property (assign) NSString *msn;
@property (assign) NSString *picture;
@property (assign) NSString *skype;
@property (assign) NSString *street;
@property (assign) NSString *timeobj;
@property (assign) NSString *url;
@property (assign) NSString *yahoo;

@end