//
//  SplashView.h
//  Splash
//
//  Created by CS Admin on 12/28/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AutomaticView.h"

@interface SplashView : UIViewController {
	UIImageView *dataCollectorLogo;
	UIImageView *orb;
	UIButton *automatic;
	UIButton *manual;
}

- (IBAction) loadAutomatic:(id)sender;
- (void) rotateImage:(UIImageView *)image duration:(NSTimeInterval)duration 
			  curve:(int)curve degrees:(CGFloat)degrees;

@property (nonatomic, retain) IBOutlet UIImageView *dataCollectorLogo;
@property (nonatomic, retain) IBOutlet UIImageView *orb;
@property (nonatomic, retain) IBOutlet UIButton *automatic;
@property (nonatomic, retain) IBOutlet UIButton *manual;

@end
