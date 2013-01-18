//
//  SplashView.m
//  Splash
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "SplashView.h"

@implementation SplashView

@synthesize dataCollectorLogo, orb, automatic, manual;

- (void)viewDidLoad {
	[super viewDidLoad];

}

// Is called every time SplashView appears
- (void)viewDidAppear:(BOOL)animated {    
    [super viewDidAppear:animated];
    
    // Spin the orb!
    [self runSpinAnimationOnView:orb duration:1 rotations:1 repeat:FLT_MAX];
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
    [super dealloc];
}

- (IBAction) loadAutomatic:(id)sender {
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        AutomaticViewController_iPad *autoViewIPad = [[AutomaticViewController_iPad alloc] init];
        autoViewIPad.title = @"Automatic";
        [self.navigationController pushViewController:autoViewIPad animated:YES];
        [autoViewIPad release];
    } else {
        AutomaticViewController *autoView = [[AutomaticViewController alloc] init];
        autoView.title = @"Automatic";
        [self.navigationController pushViewController:autoView animated:YES];
        [autoView release];
    }	
	
}

- (IBAction) loadManual:(id)sender {
	ManualViewController *manualViewController = [[ManualViewController alloc] init];
	manualViewController.title = @"Manual";
	[self.navigationController pushViewController:manualViewController animated:YES];
	[manualViewController release];
}

- (void) runSpinAnimationOnView:(UIView*)view duration:(CGFloat)duration rotations:(CGFloat)rotations repeat:(float)repeat {
    CABasicAnimation* rotationAnimation;
    rotationAnimation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.z"];
    rotationAnimation.toValue = [NSNumber numberWithFloat: M_PI * 2.0 /* full rotation */ * rotations * duration ];
    rotationAnimation.duration = duration;
    rotationAnimation.cumulative = YES;
    rotationAnimation.repeatCount = repeat;
    
    [view.layer addAnimation:rotationAnimation forKey:@"rotationAnimation"];
}

@end
