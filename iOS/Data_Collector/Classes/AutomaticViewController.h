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
#import "ExperimentBrowseViewController.h"


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
@property (nonatomic, assign) CMMotionManager *motionManager;
@property (nonatomic, assign) NSMutableArray *dataToBeJSONed;
@property (nonatomic, assign) int expNum;

@end
