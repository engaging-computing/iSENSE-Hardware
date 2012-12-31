//
//  RestAPIConnectionDelegate.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 11/16/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <Foundation/Foundation.h>

@interface RestAPIConnectionDelegate : NSURLConnection {
	NSMutableData *data;

}

@property (nonatomic, retain) NSMutableData *data;

@end
