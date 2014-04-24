//
//  RPerson.h
//  iSENSE_API
//
//  Created by Michael Stowell on 8/21/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface RPerson : NSObject {
    
}

@property (strong) NSNumber *person_id;
@property (strong) NSString *name;
@property (strong) NSString *url;
@property (strong) NSString *timecreated;
@property (strong) NSString *gravatar;
@property (strong) UIImage *gravatarImage;
@property (strong) NSNumber *hidden;

@end
