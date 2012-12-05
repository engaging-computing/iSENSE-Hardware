//
//  SplashAppDelegate.m
//  Splash
//
//  Created by CS Admin on 12/4/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "SplashAppDelegate.h"
#import "Math.h"

#define DEGREES_TO_RADIANS(angle) (angle / 180.0 * M_PI)

@implementation SplashAppDelegate

@synthesize window, tbc, orb, aboutText, guideText, automatic, manual;


#pragma mark -
#pragma mark Application lifecycle

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
    
    [window addSubview:[tbc view]];
	
	aboutText.text = [self getString:@"about_text"];
	guideText.text = [self getString:@"guide_text"];
	
    [self.window makeKeyAndVisible];
	
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
    /*
     Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
     Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
     */
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    /*
     Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
     If your application supports background execution, called instead of applicationWillTerminate: when the user quits.
     */
}


- (void)applicationWillEnterForeground:(UIApplication *)application {}


- (void)applicationDidBecomeActive:(UIApplication *)application {

	[self rotateImage:orb duration:1.5
				curve:UIViewAnimationCurveEaseIn degrees:180];
	
}


- (void)applicationWillTerminate:(UIApplication *)application {}


#pragma mark -
#pragma mark Memory management

- (void)applicationDidReceiveMemoryWarning:(UIApplication *)application {}


- (void)dealloc {
	
    [window release];
	[tbc release];
	[orb release];
	[aboutText release];
	[guideText release];
	[automatic release];
	[manual release];
    [super dealloc];
}


- (void)rotateImage:(UIImageView *)image duration:(NSTimeInterval)duration 
			  curve:(int)curve degrees:(CGFloat)degrees {
	
	// Setup the animation
	[UIView beginAnimations:nil context:NULL];
	[UIView setAnimationDuration:duration];
	[UIView setAnimationCurve:curve];
	[UIView setAnimationBeginsFromCurrentState:YES];
	[UIView setAnimationRepeatCount:1e100f];
	
	// The transform matrix
	CGAffineTransform transform = 
	CGAffineTransformMakeRotation(DEGREES_TO_RADIANS(degrees));
	image.transform = transform;
	
	// Commit the changes
	[UIView commitAnimations];
}

- (NSString *) getString:(NSString *)label {
	
	NSString *fname = [[NSBundle mainBundle] pathForResource:@"Strings" ofType:@"strings"];
	NSDictionary *d = [NSDictionary dictionaryWithContentsOfFile:fname];
	NSString *loc = [d objectForKey:label];
	return loc;
}


@end
