//
//  ManualView.h
//  Splash
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>


@interface ManualView : UIViewController {
	
	UIImageView *logo;
	UILabel *loggedInAs;
	UILabel *expNum;
	UIButton *save;
	UIButton *clear;
	UITextField *sessionName;
	UIButton *media;
	UIScrollView *scrollView;
	
}

- (IBAction) saveOnClick:(id)sender;
- (IBAction) clearOnClick:(id)sender;
- (IBAction) mediaOnClick:(id)sender;
- (IBAction) useMenu:(id)sender;

@property (nonatomic, retain) IBOutlet UIImageView *logo;
@property (nonatomic, retain) IBOutlet UILabel *loggedInAs;
@property (nonatomic, retain) IBOutlet UILabel *expNum;
@property (nonatomic, retain) IBOutlet UIButton *save;
@property (nonatomic, retain) IBOutlet UIButton *clear;
@property (nonatomic, retain) IBOutlet UITextField *sessionName;
@property (nonatomic, retain) IBOutlet UIButton *media;
@property (nonatomic, retain) IBOutlet UIScrollView *scrollView;

@end

