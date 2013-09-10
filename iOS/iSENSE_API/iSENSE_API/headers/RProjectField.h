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

@property (strong) NSNumber *field_id;
@property (strong) NSString *name;
@property (strong) NSNumber *type;
@property (strong) NSString *unit;

@end
