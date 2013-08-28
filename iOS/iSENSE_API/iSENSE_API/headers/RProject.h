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

@property (assign) NSNumber *project_id;
@property (assign) NSNumber *featured_media_id;
@property (assign) NSNumber *default_read;
@property (assign) NSNumber *like_count;
@property (assign) NSNumber *hidden;
@property (assign) NSNumber *featured;
@property (assign) NSString *name;
@property (assign) NSString *url;
@property (assign) NSString *timecreated;
@property (assign) NSString *owner_name;
@property (assign) NSString *owner_url;

@end
