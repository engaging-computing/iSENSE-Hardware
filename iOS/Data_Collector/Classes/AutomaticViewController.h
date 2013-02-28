//
//  AutomaticViewController.h
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/10/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "UILongClickButton.h"
#import "UIImageTint.h"
#import <AudioToolbox/AudioToolbox.h>
#import <CoreMotion/CoreMotion.h>
#import "ExperimentBrowseViewController.h"
#import "AutomaticConstants.h"


@interface AutomaticViewController : UIViewController <UIActionSheetDelegate, UIAlertViewDelegate>  {
	// Fuctionality
    UILongClickButton *containerForMainButton;
    iSENSE *isenseAPI;
    // GUI
    UIImageView *startStopButton;
    UIImageView *mainLogo;
    UILabel *startStopLabel;
    UILabel *loginStatus;
    UILabel *expNumStatus;
    UIBarButtonItem *menuButton;
    UILabel *elapsedTimeView;
}

-(IBAction) onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer;
-(IBAction) displayMenu:(id)sender;
-(void) updateLoginStatus;
-(CMMotionManager *) recordData;
-(NSMutableArray *) stopRecording:(CMMotionManager *)finalMotionManager;

- (void) login:(NSString *)usernameInput withPassword:(NSString *)password;
- (void) experiment;
- (void) upload;
- (void) getExperiments;

@property (nonatomic) BOOL isRecording;
@property (nonatomic) int elapsedTime;
@property (nonatomic, assign) NSTimer *timer;
@property (nonatomic, assign) CMMotionManager *motionManager;
@property (nonatomic, assign) NSMutableArray *dataToBeJSONed;
@property (nonatomic, assign) int expNum;

@end
