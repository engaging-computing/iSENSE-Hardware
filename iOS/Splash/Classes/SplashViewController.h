//
//  SplashViewController.h
//  Splash
//
//  Created by CS Admin on 11/21/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SplashViewController : UIViewController {

	UIButton *automatic;
	UIButton *manual;
	UIImage *isenseLogo;
	UIImage *orb;
	
}

// properties
@property (nonatomic, retain) IBOutlet UIButton *automatic;
@property (nonatomic, retain) IBOutlet UIButton *manual;
@property (nonatomic, retain) IBOutlet UIImage *isenseLogo;
@property (nonatomic, retain) IBOutlet UIImage *orb;

// method prototypes

@end

