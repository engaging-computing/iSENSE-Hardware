//
//  CarRampPhysicsAppDelegate.h
//  iOS Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 6/27/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
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

