//
//  Data_CollectorAppDelegate.h
//  Data_Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>
#import "AboutView.h"
#import "GuideView.h"


@interface Data_CollectorAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
	UINavigationController *navControl;
	UIBarButtonItem *about;
	UIBarButtonItem *guide;
}

- (IBAction) showAbout:(id)sender;
- (IBAction) showGuide:(id)sender;

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet UINavigationController *navControl;
@property (nonatomic, retain) IBOutlet UIBarButtonItem *about;
@property (nonatomic, retain) IBOutlet UIBarButtonItem *guide;

@end

