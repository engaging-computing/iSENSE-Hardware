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

@property (strong) NSNumber *person_id;
@property (strong) NSString *name;
@property (strong) NSString *username;
@property (strong) NSString *url;
@property (strong) NSString *timecreated;
@property (strong) NSString *gravatar;
@property (strong) NSNumber *hidden;

@end
