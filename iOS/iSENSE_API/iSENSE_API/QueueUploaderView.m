//
//  QueueUploaderView.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 6/26/13.
//  Modified by Mike Stowell
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "QueueUploaderView.h"

@implementation QueueUploaderView

@synthesize api, mTableView, currentIndex, dataSaver, managedObjectContext, lastClickedCellIndex, parent, limitedTempQueue;

// Initialize the view - TODO this shouldn't ever be used, right?  do we need it?
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        api = [API getInstance];
    }
    return self;
}

-(id)initWithParentName:(NSString *)parentName {
    self = [super init];
    if (self) {
        api = [API getInstance];
        parent = parentName;
        isenseBundle = [NSBundle bundleWithURL:[[NSBundle mainBundle] URLForResource:@"iSENSE_API_Bundle" withExtension:@"bundle"]];
    }
    return self;
}

// Upload button control
-(IBAction)upload:(id)sender {

    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    [prefs setBool:TRUE forKey:KEY_ATTEMPTED_UPLOAD];
    
    if ([api getCurrentUser] != nil) {
        
        bool uploadSuccessful = [dataSaver upload:parent];
        if (!uploadSuccessful) NSLog(@"Upload Not Successful");
        
        [self.navigationController popViewControllerAnimated:YES];
        
    } else {
        NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
        NSString *user = [prefs objectForKey:KEY_USERNAME];
        NSString *pass = [prefs objectForKey:KEY_PASSWORD];
        
        if ([API hasConnectivity]) {
            
            if (user == nil || pass == nil) {
                
                UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Login"
                                                                  message:nil
                                                                 delegate:self
                                                        cancelButtonTitle:@"Cancel"
                                                        otherButtonTitles:@"Okay", nil];
                message.tag = QUEUE_LOGIN;
                [message setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
                [message show];
            } else {
                [self loginAndUploadWithUsername:user withPassword:pass];
            }
            
        } else {
            
            [self.navigationController popViewControllerAnimated:YES];
            
        }
    }
    
}

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [isenseBundle loadNibNamed:@"queue_layout-landscape~ipad"
                                          owner:self
                                        options:nil];
            //[self viewDidLoad];
        } else {
            [isenseBundle loadNibNamed:@"queue_layout~ipad"
                                          owner:self
                                        options:nil];
            //[self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [isenseBundle loadNibNamed:@"queue_layout-landscape~iphone"
                                          owner:self
                                        options:nil];
            //[self viewDidLoad];
        } else {
            [isenseBundle loadNibNamed:@"queue_layout~iphone"
                                          owner:self
                                        options:nil];
            //[self viewDidLoad];
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
    
    // Note: if an app crashes while loading the QueueUploaderView, please check to ensure
    // the application's delegate has implemented a managedObjectContext and dataSaver
    // variable, named exactly like that.
    
    // Managed Object Context for App Delegate (where id = the application delegate)
    if (managedObjectContext == nil) {
        managedObjectContext = [(id)[[UIApplication sharedApplication] delegate] managedObjectContext];
    }
    
    // Get dataSaver from the App Delegate
    if (dataSaver == nil) {
        dataSaver = [(id)[[UIApplication sharedApplication] delegate] dataSaver];
    }
    
    currentIndex = 0;
    
    // add long press gesture listener to the table
    UILongPressGestureRecognizer *lpgr = [[UILongPressGestureRecognizer alloc]
                                          initWithTarget:self action:@selector(handleLongPressOnTableCell:)];
    lpgr.minimumPressDuration = 0.5;
    lpgr.delegate = self;
    [self.mTableView addGestureRecognizer:lpgr];
    
    // make table clear
    mTableView.backgroundColor = [UIColor clearColor];
    mTableView.backgroundView = nil;
    
    // Initialize My Limited Queue
    limitedTempQueue = [[NSMutableDictionary alloc] init];
    
    NSArray *keys = [dataSaver.dataQueue allKeys];
    for (int i = 0; i < keys.count; i++) {
        QDataSet *tmp = [dataSaver.dataQueue objectForKey:keys[i]];
        if ([tmp.parentName isEqualToString:parent]) {
            [limitedTempQueue setObject:tmp forKey:keys[i]];
        } else {
            // shouldn't get here: if user wants to remove garbage data sets,
            // he/she should first call by dataSetCountWithParentName: before
            // loading the QueueUploaderView.m.  Cleaning can't be done here or
            // else data sets with a new project can be treated as garbage, thus
            // changing a data set's project kills it completely.  And that's bad.
        }
    }
    
    // set that we haven't tried uploading anything yet
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    [prefs setBool:FALSE forKey:KEY_ATTEMPTED_UPLOAD];
    
}

- (void) handleLongPressOnTableCell:(UILongPressGestureRecognizer *)gestureRecognizer {
    if (gestureRecognizer.state == UIGestureRecognizerStateBegan) {
        
        CGPoint p = [gestureRecognizer locationInView:self.mTableView];
        
        NSIndexPath *indexPath = [self.mTableView indexPathForRowAtPoint:p];
        if (indexPath != nil) {
            
            lastClickedCellIndex = [indexPath copy];
            QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:indexPath];
            if (cell.isHighlighted) {
                
                if (![cell dataSetHasInitialProject]) {
                    UIActionSheet *popupQuery = [[UIActionSheet alloc]
                                                 initWithTitle:nil
                                                 delegate:self
                                                 cancelButtonTitle:@"Cancel"
                                                 destructiveButtonTitle:@"Delete"
                                                 otherButtonTitles:@"Rename", @"Change Description", @"Select Project", nil];
                    popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
                    [popupQuery showInView:self.view];
                } else {
                    UIActionSheet *popupQuery = [[UIActionSheet alloc]
                                                 initWithTitle:nil
                                                 delegate:self
                                                 cancelButtonTitle:@"Cancel"
                                                 destructiveButtonTitle:@"Delete"
                                                 otherButtonTitles:@"Rename", @"Change Description", nil];
                    popupQuery.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
                    [popupQuery showInView:self.view];
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
            [limitedTempQueue removeObjectForKey:[cell getKey]];
            [dataSaver removeDataSet:[cell getKey]];
            [self.mTableView reloadData];
            [mTableView reloadData];
            
            break;
            
        case QUEUE_RENAME:
            message = [[UIAlertView alloc] initWithTitle:@"Enter new data set name:"
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
            
            break;
            
        case QUEUE_SELECT_PROJ:
            
            cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
            if (![cell dataSetHasInitialProject]) {
                
                message = [[UIAlertView alloc] initWithTitle:nil
                                                     message:nil
                                                    delegate:self
                                           cancelButtonTitle:@"Cancel"
                                           otherButtonTitles:@"Enter Project #", @"Browse Projects", nil];
                message.tag = QUEUE_SELECT_PROJ;
                [message show];
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
            [self loginAndUploadWithUsername:usernameInput withPassword:passwordInput];
        }
        
    } else if (actionSheet.tag == QUEUE_RENAME) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            NSString *newDataSetName = [[actionSheet textFieldAtIndex:0] text];
            QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
            [cell setDataSetName:newDataSetName];
        }
        
    } else if (actionSheet.tag == QUEUE_SELECT_PROJ) {
        
        if (buttonIndex == OPTION_ENTER_PROJECT) {
            
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Enter Project #:"
                                                              message:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                                    otherButtonTitles:@"Okay", nil];
            
            message.tag = PROJECT_MANUAL_ENTRY;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeNumberPad;
            [message textFieldAtIndex:0].tag = TAG_QUEUE_PROJ;
            [message textFieldAtIndex:0].delegate = self;
            [message show];
            
        } else if (buttonIndex == OPTION_BROWSE_PROJECTS) {
            
            ProjectBrowseViewController *browseView = [[ProjectBrowseViewController alloc] init];
            browseView.title = @"Browse Projects";
            browseView.delegate = self;
            [self.navigationController pushViewController:browseView animated:YES];
            
        }
        
    } else if (actionSheet.tag == PROJECT_MANUAL_ENTRY) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            NSString *projIDString = [[actionSheet textFieldAtIndex:0] text];
            projID = [projIDString intValue];
            
//            QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
//            [cell setProjID:projIDString];
//            [dataSaver editDataSetWithKey:cell.mKey andChangeProjIDTo:[NSNumber numberWithInt:projID]];
            
            NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
            [prefs setInteger:projID forKey:KEY_PROJECT_ID];
            
            [self launchFieldMatchingViewControllerFromBrowse:FALSE];
        }
        
    } else if (actionSheet.tag == QUEUE_CHANGE_DESC) {
        
        if (buttonIndex != OPTION_CANCELED) {
            NSString *newDescription = [[actionSheet textFieldAtIndex:0] text];
            QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
            [cell setDesc:newDescription];
            [dataSaver editDataSetWithKey:cell.mKey andChangeDescription:newDescription];
        }
        
    }
}

-(void)projectViewController:(ProjectBrowseViewController *)controller didFinishChoosingProject:(NSNumber *)project {

    projID = project.intValue;
    
    if (projID != 0) {
//        QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
//        
//        [cell setProjID:[NSString stringWithFormat:@"%d", projID]];
//        [cell.dataSet setProjID:[NSNumber numberWithInt:projID]];
//        [dataSaver editDataSetWithKey:cell.mKey andChangeProjIDTo:project];
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        [prefs setInteger:projID forKey:KEY_PROJECT_ID];
        
        [self launchFieldMatchingViewControllerFromBrowse:TRUE];
    }
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
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

// There are as many rows as there are DataSets
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return limitedTempQueue.count;
}

// Initialize a single object in the table
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellIndetifier = @"QueueCellIdentifier";
    QueueCell *cell = (QueueCell *)[tableView dequeueReusableCellWithIdentifier:cellIndetifier];
    if (cell == nil) {
        UIViewController *tmpVC = [[UIViewController alloc] initWithNibName:@"QueueCell" bundle:isenseBundle];
        cell = (QueueCell *) tmpVC.view;
    }
    
    NSArray *keys = [limitedTempQueue allKeys];
    QDataSet *tmp = [limitedTempQueue objectForKey:keys[indexPath.row]];
    [cell setupCellWithDataSet:tmp andKey:keys[indexPath.row]];
    
    return cell;
}

// Log you into to iSENSE using the iSENSE API and uploads data
- (void) loginAndUploadWithUsername:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Logging in..."];
    [message show];
    
    dispatch_queue_t queue = dispatch_queue_create("dispatch_queue_in_queue_uploader_view", NULL);
    dispatch_async(queue, ^{
        dispatch_async(dispatch_get_main_queue(), ^{
            BOOL success = [api createSessionWithUsername:usernameInput andPassword:passwordInput];
            if (success) {
                
                // save the username and password in prefs
                NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
                [prefs setObject:usernameInput forKey:KEY_USERNAME];
                [prefs setObject:passwordInput forKey:KEY_PASSWORD];
                [prefs synchronize];
                
                [message setTitle:@"Uploading data sets..."];
                
            } else {
                [self.view makeWaffle:@"Login Failed"
                             duration:WAFFLE_LENGTH_SHORT
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_RED_X];
                [message dismissWithClickedButtonIndex:0 animated:YES];
                return;
            }
            
            if ([api getCurrentUser] != nil) {
                [dataSaver upload:parent];
            }
            
            [message dismissWithClickedButtonIndex:0 animated:YES];
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

    return message;
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
    [[NSCharacterSet characterSetWithCharactersInString:kACCEPTED_CHARS] invertedSet];
    
    return ([mString rangeOfCharacterFromSet:unwantedCharacters].location == NSNotFound) ? YES : NO;
}

- (BOOL) containsAcceptedDigits:(NSString *)mString {
    NSCharacterSet *unwantedCharacters =
    [[NSCharacterSet characterSetWithCharactersInString:kACCEPTED_DIGITS] invertedSet];
    
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
            
        case TAG_QUEUE_PROJ:
            if (![self containsAcceptedDigits:string])
                return NO;
            
            return (newLength > 6) ? NO : YES;
            
        default:
            return YES;
    }
}

- (void) launchFieldMatchingViewControllerFromBrowse:(bool)fromBrowse {
    // get the fields to field match
    DataFieldManager *dfm = [[DataFieldManager alloc] initWithProjID:projID API:api andFields:nil];
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Loading fields..."];
    [message show];
    
    dispatch_queue_t queue = dispatch_queue_create("step_1_setup_loading_project_fields", NULL);
    dispatch_async(queue, ^{
        [dfm getOrder];
        dispatch_async(dispatch_get_main_queue(), ^{
            // set an observer for the field matched array caught from FieldMatching
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(retrieveFieldMatchedArray:) name:kFIELD_MATCHED_ARRAY object:nil];
            
            // launch the field matching dialog
            FieldMatchingViewController *fmvc = [[FieldMatchingViewController alloc] initWithMatchedFields:[dfm getOrderList] andProjectFields:[dfm getRealOrder]];
            fmvc.title = @"Field Matching";
            
            if (fromBrowse) {
                double delayInSeconds = 0.1;
                dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, delayInSeconds * NSEC_PER_SEC);
                dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                    [self.navigationController pushViewController:fmvc animated:YES];
                });
            } else
                [self.navigationController pushViewController:fmvc animated:YES];
            
            if (fromBrowse) [NSThread sleepForTimeInterval:1.0];
            [message dismissWithClickedButtonIndex:nil animated:YES];
            
        });
    });
}

- (void) retrieveFieldMatchedArray:(NSNotification *)obj {
    NSMutableArray *fieldMatch =  (NSMutableArray *)[obj object];
    if (fieldMatch != nil) {
        // user pressed okay button - set the cell's project and fields
        QueueCell *cell = (QueueCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
        
        [cell setProjID:[NSString stringWithFormat:@"%d", projID]];
        [cell.dataSet setProjID:[NSNumber numberWithInt:projID]];
        [dataSaver editDataSetWithKey:cell.mKey andChangeProjIDTo:[NSNumber numberWithInt:projID]];
        
        [cell setFields:fieldMatch];
        [cell.dataSet setFields:fieldMatch];
        [dataSaver editDataSetWithKey:cell.mKey andChangeFieldsTo:fieldMatch];
        
    }
    // else user canceled
}

@end
