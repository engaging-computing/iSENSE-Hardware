//
//  iSENSE_Data_CollectorViewController_iPad.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 11/1/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "UIPicButton.h"
#import <AudioToolbox/AudioToolbox.h>
#import <UIKit/UIKit.h>
#import "iSENSE.h"


@interface iSENSE_Data_CollectorViewController_iPad : UIViewController {
<<<<<<< HEAD
	UIImageView *startStopButton;
	UIImageView *mainLogo;
	UIPicButton *container;
	UILabel *startStopLabel;
	UILabel *loginStatus;
	bool	isRecording;
	NSTimer *longClickTimer;
	iSENSE *iSENSEAPI;
=======
	// Fuctionality
    UIPicButton *containerForMainButton;
    RestAPI *rapi;
    // GUI
    UIImageView *startStopButton;
    UIImageView *mainLogo;
    UILabel *startStopLabel;
    UILabel *loginStatus;
	
>>>>>>> master
}

-(IBAction) onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer;

<<<<<<< HEAD
@property (nonatomic, retain) UIImageView *startStopButton;
@property (nonatomic, retain) NSTimer	*longClickTimer;
@property (nonatomic, retain) UIImageView *mainLogo;
@property (nonatomic, retain) UIPicButton *container;
@property (nonatomic, retain) UILabel *startStopLabel;
@property (nonatomic, retain) UILabel *loginStatus;
@property (nonatomic, retain) iSENSE *iSENSEAPI;
=======
@property BOOL isRecording;
>>>>>>> master

@end
