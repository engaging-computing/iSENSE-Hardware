//
//  SplashAppDelegate.h
//  Splash
//
//  Created by CS Admin on 11/21/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@class SplashViewController;

@interface SplashAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
    SplashViewController *viewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet SplashViewController *viewController;

@end

