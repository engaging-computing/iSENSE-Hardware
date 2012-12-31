//
//  RestAPI.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 11/6/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>
#import "RestAPIConnectionDelegate.h"

@interface RestAPI :NSObject {
	int loginKey;

}

-(BOOL)login:(NSString*) username:(NSString*) password;
-(NSString*)makeRequest:(NSString*)target;
-(void)useDev:(BOOL)toggle;
+(RestAPI *)getInstance;

@end
