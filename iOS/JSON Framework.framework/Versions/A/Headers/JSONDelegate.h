//
//  JSONDelegate2.h
//  JSON2
//
//  Created by Sebastian Bittmann on 04.03.10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//a

#import <Foundation/Foundation.h>


@interface JSONDelegate : NSObject {
	
}
-(void) occurenceOfString:(NSString*)aString;
-(void) occurenceOfNumber:(NSNumber*)aNumber;
-(void) occurenceOfBOOL:(BOOL) aBool;
-(void)	occurenceOfNil;

-(void) occurenceOfPair:(NSString*) identifier;

-(void) occurenceOfObject;
-(void) endOfObject;
-(void) occurenceOfArray;
-(void) endOfArray:(NSArray*) array;

@end
