//
//  WelcomeViewController.m
//  Uploader
//
//  Created by Michael Stowell on 11/5/13.
//  Copyright (c) 2013 iSENSE. All rights reserved.
//

#import "WelcomeViewController.h"

@interface WelcomeViewController ()
@end

@implementation WelcomeViewController

@synthesize continueWithProj, createNewProj, selectProjLater;

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"Welcome-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"Welcome~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"Welcome-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"Welcome~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
}

- (void)viewDidLoad {
    
    [super viewDidLoad];
    
    // align text to center of buttons
    [continueWithProj.titleLabel setTextAlignment:UITextAlignmentCenter];
    [createNewProj.titleLabel setTextAlignment:UITextAlignmentCenter];
    [selectProjLater.titleLabel setTextAlignment:UITextAlignmentCenter];
    
    // Do any additional setup after loading the view from its nib.
    
}

- (void)didReceiveMemoryWarning {
    
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
    
}

- (IBAction) continueWithProjOnClick:(UIButton *)sender {
    
}

- (IBAction) createNewProjOnClick:(UIButton *)sender {
    
}

- (IBAction) selectProjLaterOnClick:(UIButton *)sender {
    
}

@end
