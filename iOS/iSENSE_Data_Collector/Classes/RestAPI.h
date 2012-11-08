//
//  RestAPI.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 11/6/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface RestAPI : UIView {
	int login_key;

}

-(BOOL)login:(NSString*) username:(NSString*) password;

@end
