//
//  iSENSE_Data_CollectorAppDelegate.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 10/3/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@class iSENSE_Data_CollectorViewController;

@interface iSENSE_Data_CollectorAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
	
    iSENSE_Data_CollectorViewController *viewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) iSENSE_Data_CollectorViewController *viewController;

@end

