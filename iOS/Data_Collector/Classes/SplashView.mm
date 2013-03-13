//
//  SplashView.mm
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "SplashView.h"

@implementation SplashView

@synthesize dataCollectorLogo, automatic, manual;

- (void)viewDidLoad {
	[super viewDidLoad];
    
}

// Called every time SplashView appears
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void)viewDidUnload {
    [super viewDidUnload];
}


- (void)dealloc {
    [super dealloc];
}

- (IBAction) loadAutomatic:(id)sender {
    AutomaticViewController *autoView = [[AutomaticViewController alloc] init];
    autoView.title = @"Automatic";
    [self.navigationController pushViewController:autoView animated:YES];
    [autoView release];
}

- (IBAction) loadManual:(id)sender {
	ManualViewController *manualViewController = [[ManualViewController alloc] init];
	manualViewController.title = @"Manual";
	[self.navigationController pushViewController:manualViewController animated:YES];
	[manualViewController release];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

// iOS6
- (BOOL)shouldAutorotate {
    return YES;
}

// iOS6
- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:[NSString stringWithFormat:@"%@-landscape-ipad", NSStringFromClass([self class])]
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:[NSString stringWithFormat:@"%@-iPad", NSStringFromClass([self class])]
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:[NSString stringWithFormat:@"%@-landscape", NSStringFromClass([self class])]
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:[NSString stringWithFormat:@"%@", NSStringFromClass([self class])]
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
}


@end
