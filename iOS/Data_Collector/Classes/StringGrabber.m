//
//  StringGrabber.m
//  Data_Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//


#import "StringGrabber.h"
  
@implementation StringGrabber  

+ (NSString *) getString:(NSString *)label {
	
	NSString *fname = [[NSBundle mainBundle] pathForResource:@"Strings" ofType:@"strings"];
	NSDictionary *d = [NSDictionary dictionaryWithContentsOfFile:fname];
	NSString *loc = [d objectForKey:label];
	return loc;
}

+ (NSString *) concatenateHardcodedString:(NSString *)label with:(NSString *)string {
	
	NSString *hardCodedString = [[[NSString alloc] init] autorelease]; 
	hardCodedString = [self getString:label];
	
	NSMutableString *temp = [[[NSMutableString alloc] init] autorelease];
	temp = [NSMutableString stringWithFormat:@"%@%@", hardCodedString, string]; 
	
	return [NSString stringWithString:temp];
}

@end