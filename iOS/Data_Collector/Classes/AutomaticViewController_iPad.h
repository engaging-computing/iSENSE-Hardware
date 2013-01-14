//
//  AutomaticViewController_iPad.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 11/1/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "UILongClickButton.h"
#import "UIImageTint.h"
#import <AudioToolbox/AudioToolbox.h>
#import <CoreMotion/CoreMotion.h>


@interface AutomaticViewController_iPad : UIViewController <UIActionSheetDelegate>  {
	// Fuctionality
    UILongClickButton *containerForMainButton;
    iSENSE *isenseAPI;
    // GUI
    UIImageView *startStopButton;
    UIImageView *mainLogo;
    UILabel *startStopLabel;
    UILabel *loginStatus;
    UIBarButtonItem *menuButton;
}

-(IBAction) onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer;
-(IBAction) displayMenu:(id)sender;
-(void) updateLoginStatus;
-(CMMotionManager *) recordData;
-(NSMutableArray *) stopRecording:(CMMotionManager *)finalMotionManager;

- (void) login;
- (void) experiment;
- (void) upload;

@property (nonatomic) BOOL isRecording;
@property (atomic, assign) CMMotionManager *motionManager;
@property (atomic, assign) NSMutableArray *dataToBeJSONed;

@end
