//
//  StepOneSetup.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 06/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "StepOneSetup.h"

//@interface StepOneSetup ()

//@end

@implementation StepOneSetup

@synthesize sessionName, sampleInterval, testLength, rememberMe, selectExp, selectLater, ok;

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"StepOneSetup-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"StepOneSetup~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"StepOneSetup-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"StepOneSetup~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {}
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    iapi = [iSENSE getInstance];
    [iapi toggleUseDev:YES];
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    NSString *defaultSesName = [prefs stringForKey:[StringGrabber grabString:@"key_step1_session_name"]];
    NSString *newSesName = ([defaultSesName length] != 0) ? @"" : defaultSesName;
    [sessionName setText:newSesName];
    
    // TODO - rememberMe check

    
    rememberMe.on = false;
    selectLater.on = false;
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)okOnClick:(UIButton *)okButton {
    
}

- (IBAction)experimentOnClick:(UIButton *)expButton {
    
}

- (IBAction)selectLaterToggled:(UISwitch *)switcher {
    if (switcher.on) {
        selectExp.enabled = NO;
        selectExp.alpha = 0.5;
    } else {
        selectExp.enabled = YES;
        selectExp.alpha = 1.0;
    }
}

- (IBAction)rememberMeToggled:(UISwitch *)switcher {
    
}

- (void) dealloc {
 
    [sessionName release];
    [sampleInterval release];
    [testLength release];
    [rememberMe release];
    [selectExp release];
    [selectLater release];
    [ok release];
    
    [super dealloc];
}

@end
