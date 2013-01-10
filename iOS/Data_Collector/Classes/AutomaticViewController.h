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

@interface AutomaticViewController : UIViewController  {
	// Fuctionality
    UILongClickButton *containerForMainButton;
    iSENSE *isenseAPI;
    // GUI
    UIImageView *startStopButton;
    UIImageView *mainLogo;
    UILabel *startStopLabel;
    UILabel *loginStatus;
}

-(IBAction) onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer;
-(void) updateLoginStatus;

@property (nonatomic) BOOL isRecording;

@end
