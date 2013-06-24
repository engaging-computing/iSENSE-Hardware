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

@synthesize sessionName, sampleInterval, testLength, expNumLabel, rememberMe, selectExp, selectLater, ok;

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
    
    sampleInterval.delegate = self;
    testLength.delegate = self;
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    NSString *defaultSesName = [prefs stringForKey:[StringGrabber grabString:@"key_step1_session_name"]];
    NSString *newSesName = ([defaultSesName length] == 0) ? @"" : defaultSesName;
    [sessionName setText:newSesName];
    
    bool remem = [prefs boolForKey:[StringGrabber grabString:@"key_remember_me_check"]];
    if (remem) {
        NSString *defaultSampleInterval = [prefs stringForKey:[StringGrabber grabString:@"key_sample_interval"]];
        NSString *newSampleInterval = ([defaultSampleInterval length] == 0) ? @"" : defaultSampleInterval;
        [sampleInterval setText:newSampleInterval];
        
        NSString *defaultTestLength = [prefs stringForKey:[StringGrabber grabString:@"key_test_length"]];
        NSString *newTestLength = ([defaultTestLength length] == 0) ? @"" : defaultTestLength;
        [testLength setText:newTestLength];
        
        rememberMe.on = true;
    } else {
        rememberMe.on = false;
    }
    
    NSString *defaultExp = [prefs stringForKey:[StringGrabber grabString:@"key_exp_automatic"]];
    if ([defaultExp length] != 0) {
        if ([defaultExp isEqualToString:@"-1"]) {
            selectLater.on = true;
            selectExp.enabled = NO;
            selectExp.alpha = 0.5;
        } else {
            NSString *newExpLabel = [NSString stringWithFormat:@"(currently %@)", defaultExp];
            [expNumLabel setText:[StringGrabber concatenateHardcodedString:@"current_exp_label" with:newExpLabel]];
            selectLater.on = false;
        }
    } else {
        selectLater.on = true;
        selectExp.enabled = NO;
        selectExp.alpha = 0.5;
    }

}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)okOnClick:(UIButton *)okButton {
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    if (rememberMe.on) {
        [prefs setBool:true forKey:[StringGrabber grabString:@"key_remember_me_check"]];
        [prefs setValue:[sampleInterval text] forKey:[StringGrabber grabString:@"key_sample_interval"]];
        [prefs setValue:[testLength text] forKey:[StringGrabber grabString:@"key_test_length"]];
    } else {
       [prefs setBool:false forKey:[StringGrabber grabString:@"key_remember_me_check"]];
    }
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
    [expNumLabel release];
    [rememberMe release];
    [selectExp release];
    [selectLater release];
    [ok release];
    
    [super dealloc];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField{
    [textField resignFirstResponder];
    return YES;
}

@end
