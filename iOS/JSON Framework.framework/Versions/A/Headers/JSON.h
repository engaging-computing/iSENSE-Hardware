//
//  JSON.h
//  JSON
//
//  Created by Sebastian Bittmann on 03.03.10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//


#import <Foundation/Foundation.h>
#import "JSONNull.h"
#import "JSONDelegate.h"
#import "BOOLPointer.h"


@interface JSON : NSObject {
	NSMutableDictionary * Data;
	NSString * JSONinNSString;
	JSONDelegate *  Delegate;
	BOOL runwithdelegate;
}
-(id) initWithFile:(id)aFile;
-(id) initWithUrl:(NSURL*) aUrl;
-(id) initWithString:(NSString*) aString;
-(void) parse;
-(void) setDelegate:(id)aDelegate;
@end
