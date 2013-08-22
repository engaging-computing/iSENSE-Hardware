//
//  RDataSet.h
//  iSENSE_API
//
//  Created by Michael Stowell on 8/21/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RDataSet : NSObject {
    
}

@property (assign) int ds_id;
@property (assign) int project_id;
@property (assign) bool hidden;
@property (assign) NSString *name;
@property (assign) NSString *url;
@property (assign) NSString *timecreated;
@property (assign) int fieldCount;
@property (assign) int datapointCount;
@property (assign) NSDictionary *data;

@end