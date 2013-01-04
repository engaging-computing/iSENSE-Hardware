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

@implementation ManualView

@synthesize logo, loggedInAsLabel, expNumLabel, save, clear, sessionNameInput, media, scrollView;
@synthesize sessionName, username;


 // Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
 - (void)viewDidLoad {
	 [super viewDidLoad];
	 
	 [self.view sendSubviewToBack:scrollView];
	 
	 [self.sessionNameInput addTarget:self
						  action:@selector(textFieldFinished:)
				forControlEvents:UIControlEventEditingDidEndOnExit];
	 sessionNameInput.enablesReturnKeyAutomatically = NO;
	 
	 UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(displayMenu:)];          
	 self.navigationItem.rightBarButtonItem = menuButton;
	 [menuButton release];
	 
	 // stable     ^
	 // ----------------------------------------------------------------------------------------------
	 // needs work v
	 
	 //iapi = [iSENSE getInstance];
	 //[iapi toggleUseDev:YES];
	 
	 [self initLocations];
	 
	 //* get exp. # from prefs
	 
	 //* get login info from prefs/rapi
	 
	 loggedInAsLabel.text = [StringGrabber concatenateWithHardcodedString:@"logged_in_as" :@"_"];
	 expNumLabel.text = [StringGrabber concatenateWithHardcodedString:@"exp_num" :@"_"];
	 
	 //* if exp. # is null, launch the dialog for choosing exp. num
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
	[loggedInAsLabel release];
	[expNumLabel release];
	[save release];
	[clear release];
	[sessionNameInput release];
	[media release];
	[scrollView release];
	
	[sessionName release];
	[username release];
	
	[super dealloc];
}

- (IBAction) saveOnClick:(id)sender {
	//* if exp is null, toast
	//* else if sessionName's length = 0, .setError
	//* else SavaDataTask
}

- (IBAction) clearOnClick:(id)sender {
	sessionNameInput.text = @"";
	//* loop through UITextFields and clear them out
}

- (IBAction) mediaOnClick:(id)sender {
	//* useCamera iff sessionNameInput.length != 0
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
			message.message = @"Upload"; showMsg = NO; [self upload];
			break;
		case MENU_EXPERIMENT:
			message.message = @"Experiment"; showMsg = NO; [self experiment];
			break;
		case MENU_LOGIN:
			message.message = @"Login"; showMsg = NO; [self login];
			//[message setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput]; <- implemented later
			break;
		default:
			showMsg = NO;
			break;
	}
	
	if (showMsg)
		[message show];
	
	// eh?? -v
	[message release];
}

// TODO - make this actually restrict character limits
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
	if (textField = sessionNameInput) {
		NSUInteger newLength = [textField.text length] + [string length] - range.length;
		return (newLength > 25) ? NO : YES;
	}
	return YES;
}

- (void) login {
	UIImage *check = [[UIImage alloc] init];
	check = [UIImage imageNamed:@"bluecheck"];
	[self.view makeToast:@"Login!"
				duration:2.0
				position:@"bottom"
				   image:check];
	
	//* present dialog with login credentials
	//[isenseAPI login:@"sor" with:@"sor"];
	
}

- (void) experiment {
	[self.view makeToast:@"Experiment!"
				duration:2.0
				position:@"bottom"];
}

- (void) upload {
	[self.view makeToast:@"Upload!"
				duration:2.0
				position:@"bottom"];
	
}

// TODO gets data from the exp. #
- (void) getDataFromExpNumber {
	
}

// TODO allows for GPS to be recorded
- (void) initLocations {
	
}

// TODO
- (void) fillDataFieldEntryList:(int)eid {
	/*
	 for (ExperimentField expField : fieldOrder) {
	 
		if (expField.type_id == expField.GEOSPACIAL) {
			if (expField.unit_id == expField.UNIT_LATITUDE) {
				addDataField(expField, TYPE_LATITUDE);
			} else {
				addDataField(expField, TYPE_LONGITUDE);
			}
		} else if (expField.type_id == expField.TIME) {
			addDataField(expField, TYPE_TIME);
		} else {
			addDataField(expField, TYPE_DEFAULT);
		}
	 }
	 
	 checkLastImeOptions();
	 */
}

// TODO
- (void) addDataField:(NSString *)expField andType:(int)type {
	/*
	LinearLayout dataField = (LinearLayout) View.inflate(this,
														 R.layout.manualentryfield, null);
	TextView fieldName = (TextView) dataField
	.findViewById(R.id.manual_dataFieldName);
	fieldName.setText(expField.field_name);
	EditText fieldContents = (EditText) dataField
	.findViewById(R.id.manual_dataFieldContents);
	
	fieldContents.setSingleLine(true);
	fieldContents.setImeOptions(EditorInfo.IME_ACTION_NEXT);
	
	if (type != TYPE_DEFAULT) {
		fieldContents.setText("Auto");
		fieldContents.setEnabled(false);
		
		fieldContents.setClickable(false);
		fieldContents.setCursorVisible(false);
		fieldContents.setFocusable(false);
		fieldContents.setFocusableInTouchMode(false);
		fieldContents.setTextColor(Color.GRAY);
	}
	
	if (expField.type_id == expField.TEXT) {
		// keyboard to text
		fieldContents.setInputType(InputType.TYPE_CLASS_TEXT);
		fieldContents
		.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
																	 60) });
		fieldContents.setKeyListener(DigitsKeyListener
									 .getInstance(getResources().getString(
																		   R.string.digits_restriction)));
	} else {
		// keyboard to nums
		fieldContents.setInputType(InputType.TYPE_CLASS_PHONE);
		fieldContents
		.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
																	 20) });
		fieldContents.setKeyListener(DigitsKeyListener
									 .getInstance(getResources().getString(
																		   R.string.numbers_restriction)));
		
	}
	
	dataFieldEntryList.addView(dataField);	
	 */
}

@end
