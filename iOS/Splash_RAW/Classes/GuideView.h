//
//  GuideView.h
//  Splash
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>


@interface GuideView : UIViewController {
	UITextView *guideText;
}

- (NSString *) getString:(NSString *)label;

@property (nonatomic, retain) IBOutlet UITextView *guideText;

@end
