//
//  ManualView.m
//  Splash
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "ManualView.h"
#import "Data_CollectorAppDelegate.h"

#define MENU_UPLOAD 0
#define MENU_EXPERIMENT 1
#define MENU_LOGIN 2
#define MENU_CANCEL 3

@implementation ManualView

@synthesize logo, loggedInAs, expNum, save, clear, sessionName, media, scrollView;


 // Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
 - (void)viewDidLoad {
	 [super viewDidLoad];
	 
	 [self.view sendSubviewToBack:scrollView];
	 
	 [self.sessionName addTarget:self
						  action:@selector(textFieldFinished:)
				forControlEvents:UIControlEventEditingDidEndOnExit];
	 sessionName.enablesReturnKeyAutomatically = NO;
	 
	 loggedInAs.text = [StringGrabber getString:@"logged_in_as"];
	 expNum.text = [StringGrabber getString:@"exp_num"];
	 
	 UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];          
	 self.navigationItem.rightBarButtonItem = menuButton;
	 [menuButton release];
	 
 }

- (IBAction)textFieldFinished:(id)sender {}
 

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	[logo release];
	[loggedInAs release];
	[expNum release];
	[save release];
	[clear release];
	[sessionName release];
	[media release];
	[scrollView release];
	[super dealloc];
}

- (IBAction) saveOnClick:(id)sender {
	
}

- (IBAction) clearOnClick:(id)sender {
	sessionName.text = @"";
}

- (IBAction) mediaOnClick:(id)sender {
	[CameraUsage useCamera];
}

- (IBAction) displayMenu:(id)sender {
	UIActionSheet *popupQuery = [[UIActionSheet alloc] 
				  initWithTitle:nil 
			           delegate:self
	          cancelButtonTitle:@"Cancel" 
	     destructiveButtonTitle:nil 
			  otherButtonTitles:@"Upload", @"Experiment", @"Login", nil];
	popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
	[popupQuery showInView:self.view];
	[popupQuery release];
}

-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
	BOOL showMsg = YES;
	UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Menu item clicked:"
													  message:@"Nil_message"
													 delegate:nil
											cancelButtonTitle:@"Okay"
											otherButtonTitles:nil];
	switch (buttonIndex) {
		case MENU_UPLOAD:
			message.message = @"Upload"; //showMsg = NO; [self upload];
			break;
		case MENU_EXPERIMENT:
			message.message = @"Experiment"; //showMsg = NO; [self experiment];
			break;
		case MENU_LOGIN:
			message.message = @"Login"; //showMsg = NO; [self login];
			break;
		case MENU_CANCEL:
			showMsg = NO;
			break;
	}
	
	if (showMsg)
		[message show];
}


// TODO - make this actually restrict character limits
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
	// if (textField = sessionName, e.g.) {
    NSUInteger newLength = [textField.text length] + [string length] - range.length;
    return (newLength > 25) ? NO : YES;
	// }
	// otherwise, return YES
}

- (void) login {

}

- (void) experiment {
	
}

- (void) upload {
	
	
}


@end
