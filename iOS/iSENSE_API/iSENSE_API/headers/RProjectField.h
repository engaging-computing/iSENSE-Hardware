//
//  RProjectField.h
//  iSENSE_API
//
//  Created by Michael Stowell on 8/21/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <Foundation/Foundation.h>

#define TYPE_TIMESTAMP 1
#define TYPE_NUMBER 2
#define TYPE_TEXT 3
#define TYPE_LAT 4
#define TYPE_LON 5

@interface RProjectField : NSObject {
    
}

@property (assign) NSNumber *field_id;
@property (assign) NSString *name;
@property (assign) NSNumber *type;
@property (assign) NSString *unit;

@end
