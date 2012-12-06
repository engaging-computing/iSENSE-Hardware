//
//  BOOLPointer.h
//  JSON COCOA
//
//  Created by Sebastian Bittmann on 03.03.10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface BOOLPointer : NSObject {
	BOOL _yesorno;
}
@property BOOL yesorno;

-(id) init:(BOOL) YESORNO;
@end
