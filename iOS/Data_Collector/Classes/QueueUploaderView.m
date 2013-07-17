//
//  QueueUploaderView.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 6/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "QueueUploaderView.h"

@implementation QueueUploaderView

@synthesize mTableView, currentIndex, dataSaver, managedObjectContext, iapi;

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
        if (!uploadSuccessful) NSLog(@"Too bad 4 you");
        
        [self.navigationController popViewControllerAnimated:YES];
        
    } else {
        if ([iapi isConnectedToInternet]) {
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Login"
                                                 message:nil
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Okay", nil];
            message.tag = MENU_LOGIN;
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
}

- (void) handleLongPressOnTableCell:(UILongPressGestureRecognizer *)gestureRecognizer {
    if (gestureRecognizer.state == UIGestureRecognizerStateBegan) {
        
        CGPoint p = [gestureRecognizer locationInView:self.mTableView];
        
        NSIndexPath *indexPath = [self.mTableView indexPathForRowAtPoint:p];
        if (indexPath == nil) {
            NSLog(@"long press on table view but not on a row");
        } else {
            UITableViewCell *cell = [self.mTableView cellForRowAtIndexPath:indexPath];
            if (cell.isHighlighted) {
                NSLog(@"long press on table view at section %d row %d", indexPath.section, indexPath.row);
            }
        }
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
    [cell setupCellWithDataSet:tmp];
    
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

- (void) alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet.tag == MENU_LOGIN) {
        
        if (buttonIndex != OPTION_CANCELED) {
            NSString *usernameInput = [[actionSheet textFieldAtIndex:0] text];
            NSString *passwordInput = [[actionSheet textFieldAtIndex:1] text];
            [self login:usernameInput withPassword:passwordInput];
        }
    }
}

// TODO - this method isn't be fired... why? It fires in SensorSelection and its EXACTLY THE SAME
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSLog(@"log plz");
    [tableView reloadData];
    QueueCell *cell = (QueueCell *)[tableView cellForRowAtIndexPath:indexPath];
    [cell setBackgroundColor:[UIColor lightGrayColor]];
    [NSThread sleepForTimeInterval:0.08];
    [cell setBackgroundColor:[UIColor clearColor]];
}


@end
