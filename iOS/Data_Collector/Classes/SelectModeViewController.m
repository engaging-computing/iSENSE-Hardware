//
//  SelectModeViewController.m
//  Uploader
//
//  Created by Michael Stowell on 11/5/13.
//  Copyright (c) 2013 iSENSE. All rights reserved.
//

#import "SelectModeViewController.h"

@interface SelectModeViewController ()
@end

@implementation SelectModeViewController

@synthesize dataCollector, manualEntry;

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"SelectMode-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"SelectMode~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"SelectMode-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"SelectMode~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
}

- (void)viewDidLoad {
    
    [super viewDidLoad];
    
    // align text to center of buttons
    [dataCollector.titleLabel setTextAlignment:UITextAlignmentCenter];
    [manualEntry.titleLabel setTextAlignment:UITextAlignmentCenter];
    
    // Do any additional setup after loading the view from its nib.
    
}

- (void)didReceiveMemoryWarning {
    
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
    
}

- (IBAction) dataCollectorOnClick:(UIButton *)sender {
    
}

- (IBAction) manualEntryOnClick:(UIButton *)sender {
    
}

@end
