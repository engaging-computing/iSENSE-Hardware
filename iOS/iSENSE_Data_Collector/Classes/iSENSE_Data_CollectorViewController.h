//
//  iSENSE_Data_CollectorViewController.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 10/3/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AudioToolbox/AudioToolbox.h>
#import	"UIPicButton.h"

@interface iSENSE_Data_CollectorViewController : UIViewController {
	UIImageView *startStopButton;
	UIImageView *mainLogo;
	UIPicButton *container;
	UILabel *startStopLabel;
	bool	isRecording;
	NSTimer *longClickTimer;
}

-(IBAction) onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer;

@property (nonatomic, retain) UIImageView *startStopButton;
@property (nonatomic, retain) NSTimer	*longClickTimer;
@property (nonatomic, retain) UIImageView *mainLogo;
@property (nonatomic, retain) UIPicButton *container;
@property (nonatomic, retain) UILabel *startStopLabel;

@end

