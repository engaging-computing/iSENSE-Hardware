//
//  RTutorial.h
//  iSENSE_API
//
//  Created by Michael Stowell on 8/21/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RTutorial : NSObject {
    
}

@property (assign) int tutorial_id;
@property (assign) bool hidden;
@property (assign) NSString *name;
@property (assign) NSString *url;
@property (assign) NSString *timecreated;
@property (assign) NSString *owner_name;
@property (assign) NSString *owner_url;

@end
