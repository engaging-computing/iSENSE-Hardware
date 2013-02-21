//
//  AboutView.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>


@interface AboutView : UIViewController {
	UITextView *aboutText;
}

@property (nonatomic, retain) IBOutlet UITextView *aboutText;

@end
