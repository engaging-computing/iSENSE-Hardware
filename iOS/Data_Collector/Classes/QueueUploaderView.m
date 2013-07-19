//
//  QueueUploaderView.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 6/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "QueueUploaderView.h"

@implementation QueueUploaderView

@synthesize mTableView, currentIndex, dataSaver, managedObjectContext, iapi, lastClickedCellIndex;

// Initialize the view
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        iapi = [iSENSE getInstance];
    }
    return self;
    
}

// Upload button control
-(IBAction)upload:(id)sender {
    
    NSLog(@"%@", dataSaver.dataQueue.description);
    
    // Words n stuff
    if ([iapi isLoggedIn]) {
        
        // Do zee upload thang
        bool uploadSuccessful = [dataSaver upload];
        if (!uploadSuccessful) NSLog(@"Upload Not Successful");
        
        [self.navigationController popViewControllerAnimated:YES];
        
    } else {
        if ([iapi isConnectedToInternet]) {
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Login"
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Okay", nil];
            message.tag = QUEUE_LOGIN;
			[message setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
            [message show];
            [message release];
        } else {
           
            [self.navigationController popViewControllerAnimated:YES];

        }
    }

}

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"queue_layout-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"queue_layout~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"queue_layout-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"queue_layout~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
}

-(void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:YES];
    
    // Autorotate
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];

}

// Do any additional setup after loading the view.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Managed Object Context for Data_CollectorAppDelegate
    if (managedObjectContext == nil) {
        managedObjectContext = [(Data_CollectorAppDelegate *)[[UIApplication sharedApplication] delegate] managedObjectContext];
    }
    
    // Get dataSaver from the App Delegate
    if (dataSaver == nil) {
        dataSaver = [(Data_CollectorAppDelegate *)[[UIApplication sharedApplication] delegate] dataSaver];
    }
    
    currentIndex = 0;

    // add long press gesture listener to the table
    UILongPressGestureRecognizer *lpgr = [[UILongPressGestureRecognizer alloc]
                                          initWithTarget:self action:@selector(handleLongPressOnTableCell:)];
    lpgr.minimumPressDuration = 0.5;
    lpgr.delegate = self;
    [self.mTableView addGestureRecognizer:lpgr];
    [lpgr release];

    // make table clear
    mTableView.backgroundColor = [UIColor clearColor];
    mTableView.backgroundView = nil; // TODO frogs?
}

- (void) handleLongPressOnTableCell:(UILongPressGestureRecognizer *)gestureRecognizer {
    if (gestureRecognizer.state == UIGestureRecognizerStateBegan) {
        
        CGPoint p = [gestureRecognizer locationInView:self.mTableView];
        
        NSIndexPath *indexPath = [self.mTableView indexPathForRowAtPoint:p];
        if (indexPath != nil) {
            
            lastClickedCellIndex = [indexPath copy];
            NSLog(@"stuff stuff: %@", lastClickedCellIndex);
            QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:indexPath];
            if (cell.isHighlighted) {
                
                NSLog(@"long press on table view at row %d", indexPath.row);
                
                if (![cell dataSetHasInitialExperiment]) {
                    UIActionSheet *popupQuery = [[UIActionSheet alloc]
                                                 initWithTitle:nil
                                                 delegate:self
                                                 cancelButtonTitle:@"Cancel"
                                                 destructiveButtonTitle:@"Delete"
                                                 otherButtonTitles:@"Rename", @"Change Description", @"Select Experiment", nil];
                    popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
                    [popupQuery showInView:self.view];
                    [popupQuery release];
                } else {
                    UIActionSheet *popupQuery = [[UIActionSheet alloc]
                                                 initWithTitle:nil
                                                 delegate:self
                                                 cancelButtonTitle:@"Cancel"
                                                 destructiveButtonTitle:@"Delete"
                                                 otherButtonTitles:@"Rename", @"Change Description", nil];
                    popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
                    [popupQuery showInView:self.view];
                    [popupQuery release];
                }
            }
        }
    }
}

- (void) actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    
    UIAlertView *message;
    QueueCell *cell;

	switch (buttonIndex) {
        case QUEUE_DELETE:

            cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
            [dataSaver removeDataSet:[cell getKey]];
            [self.mTableView reloadData];
            [mTableView reloadData];
            
            break;
            
        case QUEUE_RENAME:
            message = [[UIAlertView alloc] initWithTitle:@"Enter new session name:"
                                                              message:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                                    otherButtonTitles:@"Okay", nil];
            
            message.tag = QUEUE_RENAME;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeDefault;
            [message textFieldAtIndex:0].tag = TAG_QUEUE_RENAME;
            [message textFieldAtIndex:0].delegate = self;
            [message show];
            [message release];
            
            break;
            
        case QUEUE_CHANGE_DESC:
            message = [[UIAlertView alloc] initWithTitle:@"Enter new data set description:"
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Okay", nil];
            
            message.tag = QUEUE_CHANGE_DESC;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeDefault;
            [message textFieldAtIndex:0].tag = TAG_QUEUE_DESC;
            [message textFieldAtIndex:0].delegate = self;
            [message show];
            [message release];
            
            break;
            
        case QUEUE_SELECT_EXP:
            
            cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
            if (![cell dataSetHasInitialExperiment]) {
                
                message = [[UIAlertView alloc] initWithTitle:nil
                                                     message:nil
                                                    delegate:self
                                           cancelButtonTitle:@"Cancel"
                                           otherButtonTitles:@"Enter Experiment #", @"Browse", @"Scan QR Code", nil];
                message.tag = QUEUE_SELECT_EXP;
                [message show];
                [message release];
            }
            
			break;
            
		default:
			break;
	}
	
}

- (void) alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet.tag == QUEUE_LOGIN) {
        
        if (buttonIndex != OPTION_CANCELED) {
            NSString *usernameInput = [[actionSheet textFieldAtIndex:0] text];
            NSString *passwordInput = [[actionSheet textFieldAtIndex:1] text];
            [self login:usernameInput withPassword:passwordInput];
        }
    } else if (actionSheet.tag == QUEUE_RENAME) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            NSString *newSessionName = [[actionSheet textFieldAtIndex:0] text];
            QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
            [cell setSessionName:newSessionName];
        }
    } else if (actionSheet.tag == QUEUE_SELECT_EXP) {
        if (buttonIndex == OPTION_ENTER_EXPERIMENT_NUMBER) {
            
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Enter Experiment #:"
                                                              message:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                                    otherButtonTitles:@"Okay", nil];
            
            message.tag = EXPERIMENT_MANUAL_ENTRY;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeNumberPad;
            [message textFieldAtIndex:0].tag = TAG_QUEUE_EXP;
            [message textFieldAtIndex:0].delegate = self;
            [message show];
            [message release];
            
        } else if (buttonIndex == OPTION_BROWSE_EXPERIMENTS) {
            
            ExperimentBrowseViewController *browseView = [[ExperimentBrowseViewController alloc] init];
            browseView.title = @"Browse for Experiments";
            browseView.chosenExperiment = &expNum;
            browsing = true;
            [self.navigationController pushViewController:browseView animated:YES];
            [browseView release];
            
        } else if (buttonIndex == OPTION_SCAN_QR_CODE) {
            
            if([[AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo] supportsAVCaptureSessionPreset:AVCaptureSessionPresetMedium]){
                
                if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"pic2shop:"]]) {
                    NSURL *urlp2s = [NSURL URLWithString:@"pic2shop://scan?callback=DataCollector%3A//EAN"];
                    Data_CollectorAppDelegate *dcad = (Data_CollectorAppDelegate*)[[UIApplication sharedApplication] delegate];
                    [dcad setLastController:self];
                    [dcad setReturnToClass:DELELGATE_KEY_QUEUE];
                    [[UIApplication sharedApplication] openURL:urlp2s];
                } else {
                    NSURL *urlapp = [NSURL URLWithString:@"http://itunes.com/app/pic2shop"];
                    [[UIApplication sharedApplication] openURL:urlapp];
                }
                
            } else {
                
                UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Your device does not have a camera that supports QR Code scanning."
                                                                  message:nil
                                                                 delegate:self
                                                        cancelButtonTitle:@"Cancel"
                                                        otherButtonTitles:nil];
                
                [message setAlertViewStyle:UIAlertViewStyleDefault];
                [message show];
                [message release];
                
            }
            
        }
    } else if (actionSheet.tag == EXPERIMENT_MANUAL_ENTRY) {
        
        if (buttonIndex != OPTION_CANCELED) {
                        
            NSString *expNumString = [[actionSheet textFieldAtIndex:0] text];
            QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
            [cell setExpNum:expNumString];
        }
        
    } else if (actionSheet.tag == QUEUE_CHANGE_DESC) {
        
        if (buttonIndex != OPTION_CANCELED) {
            NSString *newDescription = [[actionSheet textFieldAtIndex:0] text];
            QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
            [cell setDesc:newDescription];
        }
    }
}

- (BOOL) handleNewQRCode:(NSURL *)url {
    
    NSArray *arr = [[url absoluteString] componentsSeparatedByString:@"="];
    NSString *exp = arr[2];
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    [prefs setValue:exp forKeyPath:[StringGrabber grabString:@"key_exp_manual"]];
    
    QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
    [cell setExpNum:exp];
    
    return YES;
}

// Dispose of any resources that can be recreated.
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}

// iOS6 enable rotation
- (BOOL)shouldAutorotate {
    return YES;
}

// iOS6 enable rotation
- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

// There is a single column in this table
- (NSInteger *)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

// There are as many rows as there are DataSets
- (NSInteger *)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (dataSaver == nil) NSLog(@"Why am I nil?");
    return dataSaver.count;
}

// Initialize a single object in the table
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *cellIndetifier = @"QueueCellIdentifier";
    QueueCell *cell = (QueueCell *)[tableView dequeueReusableCellWithIdentifier:cellIndetifier];
    if (cell == nil) {
        UIViewController *tmpVC = [[UIViewController alloc] initWithNibName:@"QueueCell" bundle:nil];
        cell = (QueueCell *) tmpVC.view;
        [tmpVC release];
    }
    
    NSArray *keys = [dataSaver.dataQueue allKeys];  
    DataSet *tmp = [[dataSaver.dataQueue objectForKey:keys[indexPath.row]] retain];
    NSLog(@"DataSet data: %@", tmp.data);// @Mike
    [cell setupCellWithDataSet:tmp andKey:keys[indexPath.row]];
    
    if (browsing == true && indexPath.row == lastClickedCellIndex.row) {
        browsing = false;
        NSString *expNumString = [NSString stringWithFormat:@"%d", expNum];
        NSLog(@"new exp from browsing is: %@", expNumString);
        if (expNum != 0)
            [cell setExpNum:expNumString];
    }
    
    return cell;
}

// Log you into to iSENSE using the iSENSE API
- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Logging in..."];
    [message show];
    
    dispatch_queue_t queue = dispatch_queue_create("automatic_login_from_login_function", NULL);
    dispatch_async(queue, ^{
        BOOL success = [iapi login:usernameInput with:passwordInput];
        dispatch_async(dispatch_get_main_queue(), ^{
            if (success) {
                [self.view makeWaffle:@"Login Successful!"
                             duration:WAFFLE_LENGTH_SHORT
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_CHECKMARK];
                
                // save the username and password in prefs
                NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
                [prefs setObject:usernameInput forKey:[StringGrabber grabString:@"key_username"]];
                [prefs setObject:passwordInput forKey:[StringGrabber grabString:@"key_password"]];
                [prefs synchronize];
                
            } else {
                [self.view makeWaffle:@"Login Failed!"
                             duration:WAFFLE_LENGTH_SHORT
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_RED_X];
            }
            [message dismissWithClickedButtonIndex:nil animated:YES];
            
            if ([iapi isLoggedIn]) {
                // Do zee upload thang
                bool uploadSuccessful = [dataSaver upload];
                if (!uploadSuccessful) NSLog(@"Too bad 4 you");
            }
            
            [self.navigationController popViewControllerAnimated:YES];

        });
    });
    
}

// This is for the loading spinner when the app starts automatic mode
- (UIAlertView *) getDispatchDialogWithMessage:(NSString *)dString {
    UIAlertView *message = [[UIAlertView alloc] initWithTitle:dString
                                                      message:nil
                                                     delegate:self
                                            cancelButtonTitle:nil
                                            otherButtonTitles:nil];
    UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    spinner.center = CGPointMake(139.5, 75.5);
    [message addSubview:spinner];
    [spinner startAnimating];
    [spinner release];
    return [message autorelease];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView reloadData];
    QueueCell *cell = (QueueCell *)[tableView cellForRowAtIndexPath:indexPath];
    
    [NSThread sleepForTimeInterval:0.07];
    [cell setBackgroundColor:[UIColor clearColor]];
    
    [cell toggleChecked];
}

- (BOOL) textFieldShouldReturn:(UITextField *)textField{
    [textField resignFirstResponder];
    return YES;
}

- (BOOL) containsAcceptedCharacters:(NSString *)mString {
    NSCharacterSet *unwantedCharacters =
    [[NSCharacterSet characterSetWithCharactersInString:
      [StringGrabber grabString:@"accepted_chars"]] invertedSet];
    
    return ([mString rangeOfCharacterFromSet:unwantedCharacters].location == NSNotFound) ? YES : NO;
}

- (BOOL) containsAcceptedDigits:(NSString *)mString {
    NSCharacterSet *unwantedCharacters =
    [[NSCharacterSet characterSetWithCharactersInString:
      [StringGrabber grabString:@"accepted_digits"]] invertedSet];
    
    return ([mString rangeOfCharacterFromSet:unwantedCharacters].location == NSNotFound) ? YES : NO;
}

- (BOOL) textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    
    NSUInteger newLength = [textField.text length] + [string length] - range.length;
    
    switch (textField.tag) {
            
        case TAG_QUEUE_RENAME:
            if (![self containsAcceptedCharacters:string])
                return NO;
            
            return (newLength > 30) ? NO : YES;
            
        case TAG_QUEUE_DESC:
            if (![self containsAcceptedCharacters:string])
                return NO;
            
            return (newLength > 255) ? NO : YES;
            
        case TAG_QUEUE_EXP:
            if (![self containsAcceptedDigits:string])
                return NO;
            
            return (newLength > 6) ? NO : YES;
            
        default:
            return YES;
    }
}

@end
