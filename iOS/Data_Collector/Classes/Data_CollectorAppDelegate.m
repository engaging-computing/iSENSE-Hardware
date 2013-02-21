//
//  Data_CollectorAppDelegate.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "Data_CollectorAppDelegate.h"

@implementation Data_CollectorAppDelegate

@synthesize window, navControl, about, guide;


#pragma mark -
#pragma mark Application lifecycle

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
    
    // Override point for customization after application launch.
    self.window.rootViewController = self.navControl;
	
    [self.window makeKeyAndVisible];
    
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
}


- (void)applicationWillTerminate:(UIApplication *)application {
}


#pragma mark -
#pragma mark Memory management

- (void)applicationDidReceiveMemoryWarning:(UIApplication *)application {
}


- (void)dealloc {
    [window release];
	[navControl release];
    [super dealloc];
}

#pragma mark -
#pragma mark Custom Functions

- (IBAction) showAbout:(id)sender {
	AboutView *aboutView = [[AboutView alloc] init];
	aboutView.title = @"About";
	[self.navControl pushViewController:aboutView animated:YES];
	[aboutView release];
}

- (IBAction) showGuide:(id)sender {
	GuideView *guideView = [[GuideView alloc] init];
	guideView.title = @"Guide";
	[self.navControl pushViewController:guideView animated:YES];
	[guideView release];
}

@end
