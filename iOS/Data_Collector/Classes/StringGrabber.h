//
//  StringGrabber.h
//  Data_Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//


#import <Foundation/Foundation.h>

@interface StringGrabber : NSObject {}    

+ (NSString *) getString:(NSString *)label;
+ (NSString *) concatenateHardcodedString:(NSString *)label with:(NSString *)string;

@end
