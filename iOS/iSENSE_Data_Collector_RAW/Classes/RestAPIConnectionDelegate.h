//
//  RestAPIConnectionDelegate.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 11/16/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RestAPIConnectionDelegate : NSURLConnection {
	NSMutableData *data;

}

@property (nonatomic, retain) NSMutableData *data;

@end
