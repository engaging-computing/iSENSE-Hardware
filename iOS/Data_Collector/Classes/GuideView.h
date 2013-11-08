//
//  GuideView.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>


@interface GuideView : UIViewController {
	UITextView *guideText;
}

@property (nonatomic, strong) IBOutlet UITextView *guideText;

@end
