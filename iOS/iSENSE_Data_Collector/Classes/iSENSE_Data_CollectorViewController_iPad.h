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
#import "RestAPI.h"


@interface iSENSE_Data_CollectorViewController_iPad : UIViewController {
	UIImageView *startStopButton;
	UIImageView *mainLogo;
	UIPicButton *container;
	UILabel *startStopLabel;
	UILabel *loginStatus;
	bool	isRecording;
	NSTimer *longClickTimer;
	RestAPI *rapi;
}

-(IBAction) onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer;

@property (nonatomic, retain) UIImageView *startStopButton;
@property (nonatomic, retain) NSTimer	*longClickTimer;
@property (nonatomic, retain) UIImageView *mainLogo;
@property (nonatomic, retain) UIPicButton *container;
@property (nonatomic, retain) UILabel *startStopLabel;
@property (nonatomic, retain) UILabel *loginStatus;
@property (nonatomic, retain) RestAPI *rapi;

@end
