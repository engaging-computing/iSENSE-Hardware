//
//  CarRampPhysicsAppDelegate.h
//  CarRampPhysics
//
//  Created by Virinchi Balabhadrapatruni on 6/27/13.
//  Copyright 2013 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CarRampPhysicsViewController;

@interface CarRampPhysicsAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
    CarRampPhysicsViewController *viewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet CarRampPhysicsViewController *viewController;

@end

