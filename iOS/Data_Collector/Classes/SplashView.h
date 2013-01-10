//
//  SplashView.h
//  Splash
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>
#import "ManualView.h"
#import "AutomaticViewController.h"
#import "AutomaticViewController_iPad.h"
#import <QuartzCore/QuartzCore.h>

@interface SplashView : UIViewController {
	UIImageView *dataCollectorLogo;
	UIImageView *orb;
	UIButton *automatic;
	UIButton *manual;
}

- (IBAction) loadAutomatic:(id)sender;
- (IBAction) loadManual:(id)sender;
- (void) runSpinAnimationOnView:(UIView*)view duration:(CGFloat)duration rotations:(CGFloat)rotations repeat:(float)repeat; 

@property (nonatomic, retain) IBOutlet UIImageView *dataCollectorLogo;
@property (nonatomic, retain) IBOutlet UIImageView *orb;
@property (nonatomic, retain) IBOutlet UIButton *automatic;
@property (nonatomic, retain) IBOutlet UIButton *manual;

@end
