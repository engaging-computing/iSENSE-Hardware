//
//  RPerson.h
//  iSENSE_API
//
//  Created by Michael Stowell on 8/21/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RPerson : NSObject {
    
}

@property (assign) int person_id;
@property (assign) NSString *name;
@property (assign) NSString *username;
@property (assign) NSString *url;
@property (assign) NSString *timecreated;
@property (assign) NSString *gravatar;
@property (assign) bool hidden;

@end
