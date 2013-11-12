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
    api = [API getInstance];
    
}

- (void)didReceiveMemoryWarning {
    
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
    
}

- (IBAction) continueWithProjOnClick:(UIButton *)sender {
    if (![API hasConnectivity]) {
        [self.view makeWaffle:@"Requires internet connectivity" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_WARNING];
    } else {
        UIAlertView *message = [[UIAlertView alloc] initWithTitle:nil
                                                          message:nil
                                                         delegate:self
                                                cancelButtonTitle:@"Cancel"
                                                otherButtonTitles:@"Enter Project #", @"Browse", @"Scan QR Code", nil];
        message.tag = MENU_PROJECT;
        [message show];
    }
}

- (IBAction) createNewProjOnClick:(UIButton *)sender {
    // TODO
    [self.view makeWaffle:@"To be implemented in a future release" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_WARNING];
}

- (IBAction) selectProjLaterOnClick:(UIButton *)sender {
    [self setGlobalProjAndEnableManual:-1 andEnable:FALSE];
}

- (void) alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet.tag == MENU_PROJECT){
        
        if (buttonIndex == OPTION_ENTER_PROJECT_NUMBER) {
            
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Enter Project #:"
                                                              message:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                                    otherButtonTitles:@"Okay", nil];
            
            message.tag = PROJ_MANUAL;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeNumberPad;
            [message textFieldAtIndex:0].tag = TAG_STEPONE_PROJ;
            [message textFieldAtIndex:0].delegate = self;
            [message show];
            
        } else if (buttonIndex == OPTION_BROWSE_PROJECTS) {
            
            ProjectBrowseViewController *browseView = [[ProjectBrowseViewController alloc] init];
            browseView.title = @"Browse Projects";
            browseView.delegate = self;
            [self.navigationController pushViewController:browseView animated:YES];
            
        } else if (buttonIndex == OPTION_SCAN_QR_CODE) {
//            if([[AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo] supportsAVCaptureSessionPreset:AVCaptureSessionPresetMedium]){
//                
//                if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"pic2shop:"]]) {
//                    NSURL *urlp2s = [NSURL URLWithString:@"pic2shop://scan?callback=DataCollector%3A//EAN"];
//                    Data_CollectorAppDelegate *dcad = (Data_CollectorAppDelegate*)[[UIApplication sharedApplication] delegate];
//                    [dcad setLastController:self];
//                    [dcad setReturnToClass:DELEGATE_KEY_AUTOMATIC];
//                    [[UIApplication sharedApplication] openURL:urlp2s];
//                } else {
//                    NSURL *urlapp = [NSURL URLWithString:@"http://itunes.com/app/pic2shop"];
//                    [[UIApplication sharedApplication] openURL:urlapp];
//                }
//                
//            } else {
//                
//                UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Your device does not have a camera that supports QR Code scanning."
//                                                                  message:nil
//                                                                 delegate:self
//                                                        cancelButtonTitle:@"Cancel"
//                                                        otherButtonTitles:nil];
//                
//                [message setAlertViewStyle:UIAlertViewStyleDefault];
//                [message show];
//                
//            }
        }
        
    } else if (actionSheet.tag == PROJ_MANUAL) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            NSString *projNum = [[actionSheet textFieldAtIndex:0] text];
            
            if ([projNum intValue] <= 0) {
                [self.view makeWaffle:@"Invalid project #" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
            } else {
                [self setGlobalProjAndEnableManual:[projNum intValue] andEnable:TRUE];
            }
            
        }
        
    }
}

- (void) projectViewController:(ProjectBrowseViewController *)controller didFinishChoosingProject:(NSNumber *)project {

    if ([project intValue] <= 0) {
        [self.view makeWaffle:@"Invalid project #" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
    } else {
        [self setGlobalProjAndEnableManual:[project intValue] andEnable:TRUE];
    }

}

- (void) setGlobalProjAndEnableManual:(int)projID andEnable:(BOOL)enable {
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    if (projID > 0) {
        [prefs setInteger:projID forKey:kPROJECT_ID];
        [prefs setInteger:projID forKey:kPROJECT_ID_DC];
        [prefs setInteger:projID forKey:kPROJECT_ID_MANUAL];
    }
    
    [prefs setBool:enable forKey:kENABLE_MANUAL];
    
    SelectModeViewController *smvc = [[SelectModeViewController alloc] init];
    smvc.title = @"Select Mode";
    [self.navigationController pushViewController:smvc animated:YES];
    
}


@end
