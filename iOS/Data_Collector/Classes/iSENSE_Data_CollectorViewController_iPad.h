//
//  iSENSE_Data_CollectorViewController_iPad.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 11/1/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "UIPicButton.h"
#import <AudioToolbox/AudioToolbox.h>
#import <UIKit/UIKit.h>
#import "RestAPI.h"


@interface iSENSE_Data_CollectorViewController_iPad : UIViewController {
	// Fuctionality
    UIPicButton *containerForMainButton;
    RestAPI *rapi;
    // GUI
    UIImageView *startStopButton;
    UIImageView *mainLogo;
    UILabel *startStopLabel;
    UILabel *loginStatus;	
}

-(IBAction) onStartStopLongClick:(UILongPressGestureRecognizer*)longClickRecognizer;

@property BOOL isRecording;

@end
