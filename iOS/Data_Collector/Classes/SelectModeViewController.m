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

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}

// iOS6 enable rotation
- (BOOL)shouldAutorotate {
    return YES;
}

// iOS6 enable rotation
- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

- (void)viewDidLoad {
    
    [super viewDidLoad];
    
    // align text to center of buttons
    [dataCollector.titleLabel setTextAlignment:UITextAlignmentCenter];
    [manualEntry.titleLabel setTextAlignment:UITextAlignmentCenter];
    
    // Do any additional setup after loading the view from its nib.
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    BOOL enableManual = [prefs boolForKey:kENABLE_MANUAL];
    if (!enableManual) {
        [manualEntry setEnabled:FALSE];
        [manualEntry setAlpha:0.5f];
    }

}

- (void)viewWillAppear:(BOOL)animated {
    
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
    
}

- (void)didReceiveMemoryWarning {
    
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
    
}

- (IBAction) dataCollectorOnClick:(UIButton *)sender {
    AutomaticViewController *autoView = [[AutomaticViewController alloc] init];
    autoView.title = @"Automatic";
    [self.navigationController pushViewController:autoView animated:YES];
}

- (IBAction) manualEntryOnClick:(UIButton *)sender {
    ManualViewController *manualViewController = [[ManualViewController alloc] init];
	manualViewController.title = @"Manual";
	[self.navigationController pushViewController:manualViewController animated:YES];
}

@end
