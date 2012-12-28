//
//  SplashAppDelegate.h
//  Splash
//
//  Created by CS Admin on 12/28/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AboutView.h"
#import "GuideView.h"

@interface SplashAppDelegate : NSObject <UIApplicationDelegate> {
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

