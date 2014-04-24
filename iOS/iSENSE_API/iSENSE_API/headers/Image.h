//
//  Image.h
//  iSENSE API
//
//  Created by James Dalphond on 2/23/11. 
//  Copyright 2011 UMass Lowell. All rights reserved.
//
//  Modified by John Fertitta on 3/1/11.
//

#import <Foundation/Foundation.h>

@interface OldImage : NSObject {

}

/*Properties for setting/getting variables*/
@property (assign) NSNumber *experiment_id;
@property (assign) NSNumber *picture_id;

@property (assign) NSString *description;
@property (assign) NSString *provider_id;
@property (assign) NSString *provider_url;
@property (assign) NSString *timecreated;
@property (assign) NSString *title;

@end