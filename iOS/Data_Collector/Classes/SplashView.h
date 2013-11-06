//
//  SplashView.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>
#import "ManualViewController.h"
#import "AutomaticViewController.h"
#import <QuartzCore/QuartzCore.h>

@interface SplashView : UIViewController {
	UIImageView *dataCollectorLogo;
	UIButton *automatic;
	UIButton *manual;
}

- (IBAction) loadAutomatic:(id)sender;
- (IBAction) loadManual:(id)sender;
- (void) viewDidAppear:(BOOL)animated;

@property (nonatomic, strong) IBOutlet UIImageView *dataCollectorLogo;
@property (nonatomic, strong) IBOutlet UIButton *automatic;
@property (nonatomic, strong) IBOutlet UIButton *manual;

@end
