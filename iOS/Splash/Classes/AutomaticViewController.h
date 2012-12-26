//
//  AutomaticViewController.h
//  Splash
//
//  Created by Mike S. on 12/4/12.
//  Advisor - Fred Martin
//  Copyright 2012 ECG. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface AutomaticViewController : UIViewController {
	UIWindow *window;
	UIViewController *secondView;
	
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet UIViewController *secondView;

@end
