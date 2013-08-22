//
//  DWMasterViewController.m
//  Data_Walk
//
//  Created by Michael Stowell on 8/22/13.
//  Copyright (c) 2013 iSENSE. All rights reserved.
//

#import "MainViewController.h"
#import "Constants.h"

@implementation MainViewController

@synthesize menu;
@synthesize name, timeElapsed, pointsRecorded, loggedInAs, projNumber, dataRecordedEvery, latitudeLabel, longitudeLabel;
@synthesize locationManager, isRecording;

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"MainLayout-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"MainLayout~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"MainLayout-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"MainLayout~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
    
}
							
- (void)viewDidLoad {
    // Initial super call
    [super viewDidLoad];
    
    // Establish menu bar
	menu = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStyleBordered target:self action:@selector(onMenuClick)];
    self.navigationItem.rightBarButtonItem = menu;
    self.navigationItem.title = @"iSENSE Data Walk";
    
    UIView *myView = [[UIView alloc] initWithFrame: CGRectMake(0, 0, 300, 30)];
    UILabel *title = [[UILabel alloc] initWithFrame: CGRectMake(45, 2, 300, 30)];
    
    title.text = NSLocalizedString(@"iSENSE Data Walk", nil);
    [title setTextColor:[UIColor colorWithRed:50/255.0f green:50/255.0f blue:50/255.0f alpha:1]];
    [title setFont:[UIFont boldSystemFontOfSize:20.0]];
    
    [title setBackgroundColor:[UIColor clearColor]];
    UIImage *image = [UIImage imageNamed:@"datawalk_logo.png"];
    UIImageView *myImageView = [[UIImageView alloc] initWithImage:image];
    
    myImageView.frame = CGRectMake(0, -5, 40, 40);
    
    [myView addSubview:title];
    [myView setBackgroundColor:[UIColor  clearColor]];
    [myView addSubview:myImageView];
    self.navigationItem.titleView = myView;
    
    self.navigationController.navigationBar.tintColor = [UIColor colorWithRed:200/255.0f green:200/255.0f blue:200/255.0f alpha:1];
    self.navigationItem.rightBarButtonItem.tintColor = [UIColor colorWithRed:100/255.0f green:100/255.0f blue:100/255.0f alpha:1];
    
    // Setup UILabels with constant tags
    name.tag                = kTAG_LABEL_NAME;
    timeElapsed.tag         = kTAG_LABEL_TIME_ELAPSED;
    pointsRecorded.tag      = kTAG_LABEL_POINTS_REC;
    loggedInAs.tag          = kTAG_LABEL_LOGGED_IN;
    projNumber.tag          = kTAG_LABEL_PROJ_NUM;
    dataRecordedEvery.tag   = kTAG_LABEL_DATA_REC;
    latitudeLabel.tag       = kTAG_LABEL_LATITUDE;
    longitudeLabel.tag      = kTAG_LABEL_LONGITUDE;
    
    // Initialize other variables
    isRecording = NO;
    
}

// Is called every time MainViewController is about to appear
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
}

// Is called every time MainViewController appears
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    // Autorotate
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
}

- (void) onMenuClick {
    NSLog(@"Menu item clicked.");
}

//- (void) displayMenu {
//	UIActionSheet *popupQuery = [[UIActionSheet alloc]
//                                 initWithTitle:nil
//                                 delegate:self
//                                 cancelButtonTitle:@"Cancel"
//                                 destructiveButtonTitle:nil
//                                 otherButtonTitles:@"Login", @"Media", nil];
//	popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
//	[popupQuery showInView:self.view];
//}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return (isRecording) ? NO : YES;
}

// iOS6 enable rotation
- (BOOL)shouldAutorotate {
    return (isRecording) ? NO : YES;
}

// iOS6 enable rotation
- (NSUInteger)supportedInterfaceOrientations {
    if (isRecording) {
        if (self.interfaceOrientation == UIInterfaceOrientationPortrait) {
            return UIInterfaceOrientationMaskPortrait;
        } else if (self.interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown) {
            return UIInterfaceOrientationMaskPortraitUpsideDown;
        } else if (self.interfaceOrientation == UIInterfaceOrientationLandscapeLeft) {
            return UIInterfaceOrientationMaskLandscapeLeft;
        } else {
            return UIInterfaceOrientationMaskLandscapeRight;
        }
    } else
        return UIInterfaceOrientationMaskAll;
}


- (void) actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
}

- (void) alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
}

- (void) initLocations {
//    if (!locationManager) {
//        locationManager = [[CLLocationManager alloc] init];
//        locationManager.delegate = self;
//        locationManager.distanceFilter = kCLDistanceFilterNone;
//        locationManager.desiredAccuracy = kCLLocationAccuracyBest;
//        [locationManager startUpdatingLocation];
//        geoCoder = [[CLGeocoder alloc] init];
//    }
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
}

- (BOOL) textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    return YES;
}

- (BOOL) containsAcceptedCharacters:(NSString *)mString {
    return YES;
}



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

@end
