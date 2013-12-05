//
//  RProject.h
//  iSENSE_API
//
//  Created by Michael Stowell on 8/21/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RProject : NSObject {
    
}

@property (strong) NSNumber *project_id;
@property (strong) NSNumber *featured_media_id;
@property (strong) NSNumber *default_read;
@property (strong) NSNumber *like_count;
@property (strong) NSNumber *hidden;
@property (strong) NSNumber *featured;
@property (strong) NSString *name;
@property (strong) NSString *url;
@property (strong) NSString *timecreated;
@property (strong) NSString *owner_name;
@property (strong) NSString *owner_url;

@end
