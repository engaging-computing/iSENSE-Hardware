//
//  AboutViewController.m
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/9/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
#import "AboutViewController.h"

@interface AboutViewController ()

@end

@implementation AboutViewController

@synthesize text;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

// pre-iOS6 rotating options
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

// iOS6 rotating options
- (BOOL)shouldAutorotate {
    return YES;
}

// iOS6 interface orientations
- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"AboutViewController~landscape_iPad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"AboutViewController_iPad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"AboutViewController~landscape_iPhone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"AboutViewController_iPhone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
    
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [text setText:@"Enter your first name and last initial so that you can later identify your session on iSENSE.\n\nPress and hold the Start button, and the app will record 10 seconds of acceleration data. After recording, you may publish your data to isense.cs.uml.edu. \n\nYou are also given an option to view your data. \n\nYou may modify various settings -- explore the menus.  \n\nIf setting up your own experiment: create fields named \"Time\" (time/milliseconds) and any of \"X\", \"Y\", \"Z\", and \"Accel-Magnitude\" (all Acceleration/meters per second squared)." ];
    
    self.navigationItem.title = @"About";
}

- (void) viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [self willRotateToInterfaceOrientation:self.interfaceOrientation duration:0];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
