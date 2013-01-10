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

@interface AutomaticViewController : UIViewController <UIActionSheetDelegate> {
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

- (void) login;
- (void) experiment;
- (void) upload;

-(IBAction) onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer;
-(IBAction) displayMenu:(id)sender;
-(void) updateLoginStatus;

@property (nonatomic) BOOL isRecording;

@end
