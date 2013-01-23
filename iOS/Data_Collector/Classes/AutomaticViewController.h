//
//  AutomaticViewController.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 1/10/13.
//
//

#import "UILongClickButton.h"
#import "UIImageTint.h"
#import <AudioToolbox/AudioToolbox.h>
#import <CoreMotion/CoreMotion.h>


@interface AutomaticViewController : UIViewController <UIActionSheetDelegate, UIAlertViewDelegate>  {
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

- (void) login:(NSString *)usernameInput withPassword:(NSString *)password;
- (void) experiment;
- (void) upload;
- (void) getExperiment;

@property (nonatomic) BOOL isRecording;
@property (atomic, assign) CMMotionManager *motionManager;
@property (atomic, assign) NSMutableArray *dataToBeJSONed;

@end
