//
//  StringGrabber.h
//  iOS Car Ramp Physics
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//


#import <Foundation/Foundation.h>

@interface StringGrabber : NSObject {}    

+ (NSString *) grabString:(NSString *)label;
+ (NSString *) grabField: (NSString *)label;
+ (NSString *) concatenateHardcodedString:(NSString *)label with:(NSString *)string;
+ (NSString *) concatenate:(NSString*)string withHardcodedString:(NSString *)label;

@end
