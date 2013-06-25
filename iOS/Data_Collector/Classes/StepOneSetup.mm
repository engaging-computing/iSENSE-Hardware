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
    
    sessionName.delegate = self;
    sampleInterval.delegate = self;
    testLength.delegate = self;
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    // Indicate that we're not done setting up yet
    [prefs setBool:false forKey:[StringGrabber grabString:@"key_setup_complete"]];
    
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
            NSString *newExpLabel = [NSString stringWithFormat:@" (currently %@)", defaultExp];
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
}

- (IBAction)okOnClick:(UIButton *)okButton {
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    bool ready = true;
    
    if ([[sessionName text] length] == 0) {
        [self.view makeToast:@"Please enter a session name first"
                    duration:TOAST_LENGTH_LONG
                    position:TOAST_BOTTOM
                       image:TOAST_RED_X];
        ready = false;
    }
    
    int sInt;
    if ([[sampleInterval text] length] == 0) {
        sInt = S_INTERVAL;
    } else {
        sInt = [[sampleInterval text] integerValue];
    }
    
    if (sInt < S_INTERVAL) {
        [self.view makeToast:[NSString stringWithFormat:@"Please enter a sample interval >= %d ms", S_INTERVAL]
                    duration:TOAST_LENGTH_LONG
                    position:TOAST_BOTTOM
                       image:TOAST_RED_X];
        ready = false;
    }
    
    int tLen;
    if ([[testLength text] length] == 0) {
        tLen = TEST_LENGTH;
    } else {
        tLen = [[testLength text] integerValue];
    }
    
    if (tLen * (1000/sInt) > MAX_DATA_POINTS) {
        [self.view makeToast:[NSString stringWithFormat:@"Please enter a test length <= %d s", MAX_DATA_POINTS/(1000/sInt)]
                    duration:TOAST_LENGTH_LONG
                    position:TOAST_BOTTOM
                       image:TOAST_RED_X];
        ready = false;
    }
    
    if (!selectLater.on) {
        NSString *eid = [prefs stringForKey:[StringGrabber grabString:@"key_exp_automatic"]];
        // TODO - fields?
        if ([eid isEqualToString:@""] || [eid isEqualToString:@"-1"]) {
            [self.view makeToast:@"Please select an experiment"
                        duration:TOAST_LENGTH_LONG
                        position:TOAST_BOTTOM
                           image:TOAST_RED_X];
            ready = false;
        }
    }
    
    if (ready) {
        [prefs setValue:[sessionName text] forKey:[StringGrabber grabString:@"key_step1_session_name"]];
        
        if (selectLater.on) {
            [prefs setValue:@"-1" forKey:[StringGrabber grabString:@"key_exp_automatic"]];
        }
        
        if (rememberMe.on) {
            [prefs setBool:true forKey:[StringGrabber grabString:@"key_remember_me_check"]];
            [prefs setValue:[sampleInterval text] forKey:[StringGrabber grabString:@"key_sample_interval"]];
            [prefs setValue:[testLength text]     forKey:[StringGrabber grabString:@"key_test_length"]];
        } else {
            [prefs setBool:false forKey:[StringGrabber grabString:@"key_remember_me_check"]];
            [prefs setValue:[NSString stringWithFormat:@"%d", S_INTERVAL]  forKey:[StringGrabber grabString:@"key_sample_interval"]];
            [prefs setValue:[NSString stringWithFormat:@"%d", TEST_LENGTH] forKey:[StringGrabber grabString:@"key_test_length"]];
        }
        
        // Indicate that we're done setting up and return
        [prefs setBool:true forKey:[StringGrabber grabString:@"key_setup_complete"]];
        [self.navigationController popViewControllerAnimated:YES];
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
