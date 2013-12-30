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

@synthesize continueWithProj, selectProjLater, welcomeText;

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
    [continueWithProj.titleLabel setTextAlignment:UITextAlignmentCenter];
    [selectProjLater.titleLabel setTextAlignment:UITextAlignmentCenter];
    
    // init API
    api = [API getInstance];
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    useDev = [prefs boolForKey:kUSE_DEV];
    [api useDev:useDev];
    
    // will write false to useDev for initial run of app (which is ok because by default we want production)
    [prefs setBool:useDev forKey:kUSE_DEV];
    [prefs synchronize];
    
    // add gesture listener to the label (shhh this is a dev secret)
    UITapGestureRecognizer *devGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(switchDevAndProduction:)];
    [welcomeText setUserInteractionEnabled:YES];
    [welcomeText addGestureRecognizer:devGesture];
    
}

- (void)viewWillAppear:(BOOL)animated {
    
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
    
    NSLog(@"P: %d", projNum);
    
    if (projNum > 0) {
        SelectModeViewController *smvc = [[SelectModeViewController alloc] init];
        smvc.title = @"Select Mode";
        
        NSMutableArray *controllers = [NSMutableArray arrayWithArray:self.navigationController.viewControllers];
        [controllers addObject:smvc];
        
        [self.navigationController setViewControllers:controllers animated:YES];
        
    }
    
    projNum = 0;
    
}

- (void)didReceiveMemoryWarning {
    
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
    
}

- (void) switchDevAndProduction:(id)sender {
    taps++;
    NSString *otherMode = (useDev) ? @"production" : @"dev";
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    switch (taps) {
        case 12:
            [self.view makeWaffle:[NSString stringWithFormat:@"Two more taps to enter %@ mode", otherMode]];
            break;
        case 13:
            [self.view makeWaffle:[NSString stringWithFormat:@"One more tap to enter %@ mode", otherMode]];
            break;
        case 14:
            [self.view makeWaffle:[NSString stringWithFormat:@"Now in %@ mode", otherMode]];
            useDev = !useDev;
            [prefs setBool:useDev forKey:kUSE_DEV];
            [prefs synchronize];
            if ([api getCurrentUser] != nil) {
                dispatch_queue_t queue = dispatch_queue_create("welcome_vc_logging_out", NULL);
                dispatch_async(queue, ^{
                    [api deleteSession];
                    [api useDev:useDev];
                    NSLog(@"Using dev? %d", useDev);
                });
            } else
                [api useDev:useDev];
            taps = 0;
            break;
        default:
            break;
    }
    
}

- (IBAction) continueWithProjOnClick:(UIButton *)sender {
    if (![API hasConnectivity]) {
        [self.view makeWaffle:@"Requires internet connectivity" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_WARNING];
    } else {
        UIAlertView *message = [[UIAlertView alloc] initWithTitle:nil
                                                          message:nil
                                                         delegate:self
                                                cancelButtonTitle:@"Cancel"
                                                otherButtonTitles:@"Enter Project #", @"Browse", nil];
        message.tag = MENU_PROJECT;
        [message show];
    }
}

- (IBAction) selectProjLaterOnClick:(UIButton *)sender {
    [self setGlobalProjAndEnableManual:-1 andEnable:FALSE isFromBrowse:FALSE];
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
            
        }
        
    } else if (actionSheet.tag == PROJ_MANUAL) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            NSString *projID = [[actionSheet textFieldAtIndex:0] text];
            
            if ([projID intValue] <= 0) {
                [self.view makeWaffle:@"Invalid project #" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
            } else {
                [self setGlobalProjAndEnableManual:[projID intValue] andEnable:TRUE isFromBrowse:FALSE];
            }
            
        }
        
    }
}

- (void) projectViewController:(ProjectBrowseViewController *)controller didFinishChoosingProject:(NSNumber *)project {
    
    NSLog(@"returning from browse");
    
    if ([project intValue] <= 0) {
        [self.view makeWaffle:@"Invalid project #" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
    } else {
        [self setGlobalProjAndEnableManual:[project intValue] andEnable:TRUE isFromBrowse:TRUE];
        projNum = [project intValue];
    }
    
}

- (void) setGlobalProjAndEnableManual:(int)projID andEnable:(BOOL)enable isFromBrowse:(BOOL)ifb {
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    if (projID <= 0)
        projID = -1;
    
    [prefs setInteger:projID forKey:kPROJECT_ID];
    [prefs setInteger:projID forKey:kPROJECT_ID_DC];
    [prefs setInteger:projID forKey:kPROJECT_ID_MANUAL];
    [prefs setBool:enable forKey:kENABLE_MANUAL];
    
    if (!ifb) {
        SelectModeViewController *smvc = [[SelectModeViewController alloc] init];
        smvc.title = @"Select Mode";
      
        [self.navigationController pushViewController:smvc animated:YES];
    }
}

@end
