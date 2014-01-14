//
//  PWViewController.m
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 1/9/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import "PWViewController.h"



@interface PWViewController ()

@end



@implementation PWViewController

@synthesize menuButton, project, loginalert, login_status, userName, passWord, api, groupNameField, dataSaver, selectButton, popOver, managedObjectContext, projID, projectIDLbl, picCntLbl, proj_num, saveMode, useDev;

- (void)viewDidLoad
{
    [super viewDidLoad];
	
    [[UINavigationBar appearance] setBackgroundImage:[[UIImage alloc] init] forBarMetrics:UIBarMetricsDefault];
    [[UINavigationBar appearance] setBackgroundColor:UIColorFromHex(0x111155)];
    UIImageView *titleView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"navBar.png"]];
    [[UINavigationBar appearance] setTitleView:titleView];
    self.navigationItem.titleView = titleView;
    UIButton* btton = [UIButton buttonWithType:UIButtonTypeCustom];
    [btton setFrame:CGRectMake(0, 0, 30, 30)];
    [btton addTarget:self action:@selector(callMenu) forControlEvents:UIControlEventTouchUpInside];
    [btton setImage:[UIImage imageNamed:@"menuIcon"] forState:UIControlStateNormal];
    menuButton = [[UIBarButtonItem alloc] initWithCustomView:btton];
    [menuButton setTintColor:UIColorFromHex(0x111155)];
    self.navigationItem.rightBarButtonItem = menuButton;
    [[UIBarButtonItem appearance] setTintColor:UIColorFromHex(0x111155)];
    
    groupNameField.delegate = self;
    
    NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
    projID = [prefs integerForKey:KEY_PROJECT_ID];
    
    if (projID == 0) {
        projID = -1;
    }
    
    projectIDLbl.text = [@"Project: " stringByAppendingString:[NSString stringWithFormat:@"%d", projID]];
    
    api = [API getInstance];
    
    useDev = YES;
    
    [api useDev:useDev];
    
    if (managedObjectContext == nil) {
        managedObjectContext = [(PWAppDelegate *)[[UIApplication sharedApplication] delegate] managedObjectContext];
    }
    
    // DataSaver from Data_CollectorAppDelegate
    if (dataSaver == nil) {
        dataSaver = [(PWAppDelegate *) [[UIApplication sharedApplication] delegate] dataSaver];
        NSLog(@"Datasaver Details: %@", dataSaver.description);
        NSLog(@"Current count = %d", dataSaver.dataQueue.count);
    }

}

-(void)projectViewController:(ProjectBrowseViewController *)controller didFinishChoosingProject:(NSNumber *)proj {
    
    projID = proj.intValue;
    
    if (projID != 0) {

        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        [prefs setInteger:projID forKey:KEY_PROJECT_ID];
        
    }
    
    projectIDLbl.text = [@"Project: " stringByAppendingString:[NSString stringWithFormat:@"%d",projID]];
}


- (void)navigationController:(UINavigationController *)navigationController
      willShowViewController:(UIViewController *)viewController
                    animated:(BOOL)animated {
    
        [[UIApplication sharedApplication] setStatusBarHidden:NO];
        [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleBlackOpaque animated:NO];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    
    return YES;
}

- (void) viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
}

- (void) callMenu {
    RNGridMenu *menu;
    
    UIImage *upload = [UIImage imageNamed:@"upload2"];
    UIImage *code = [UIImage imageNamed:@"barcode"];
    UIImage *login = [UIImage imageNamed:@"users"];
    UIImage *about = [UIImage imageNamed:@"info"];
    UIImage *reset = [UIImage imageNamed:@"reset"];
    
    void (^uploadBlock)() = ^() {
        NSLog(@"Upload button pressed");
        
        if (dataSaver.dataQueue.count > 0) {
            QueueUploaderView *queueUploader = [[QueueUploaderView alloc] initWithParentName:@"PWViewController"];
            queueUploader.title = @"Upload saved data";
            [self.navigationController pushViewController:queueUploader animated:YES];
        } else {
            [self.view makeWaffle:@"No data sets to upload!" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
        }
         
        
    };
    void (^codeBlock)() = ^() {
        NSLog(@"project button pressed");
        project = [[UIAlertView alloc] initWithTitle:@"Project ID" message:@"" delegate:self cancelButtonTitle:nil otherButtonTitles: nil];
        [project addButtonWithTitle:@"Enter Project ID"];
        [project addButtonWithTitle:@"Browse"];
        [project addButtonWithTitle:@"QR Code"];
        [project addButtonWithTitle:@"Create New Project"];
        [project addButtonWithTitle:@"Done"];
        [project show];
        
    };
    void (^loginBlock)() = ^() {
        NSLog(@"Login button pressed");
        
        loginalert = [[UIAlertView alloc] initWithTitle:@"Login to iSENSE" message:@"" delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"OK", nil];
        [loginalert setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
        [loginalert textFieldAtIndex:0].delegate = self;
        [loginalert textFieldAtIndex:1].delegate = self;
        [loginalert textFieldAtIndex:0].placeholder = @"Email";
        [loginalert show];
    };
    void (^aboutBlock)() = ^() {
        NSLog(@"About button pressed");
        
        AboutViewController *about;
        // Override point for customization after application launch.
        if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
            about = [[AboutViewController alloc] initWithNibName:@"AboutViewController_iPhone" bundle:nil];
        } else {
            about = [[AboutViewController alloc] initWithNibName:@"AboutViewController_iPad" bundle:nil];
        }
        
        [self.navigationController pushViewController:about animated:YES];
        
    };
    void (^resetBlock)() = ^() {
        NSLog(@"Reset button pressed");
        userName = @"mobile.fake@example.com";
        passWord = @"mobile";
        [self login:userName withPassword:passWord];
        login_status.text = [@"Logged in as: " stringByAppendingString: userName];
    };
    
    RNGridMenuItem *uploadItem = [[RNGridMenuItem alloc] initWithImage:upload title:@"Upload" action:uploadBlock];
    RNGridMenuItem *codeItem = [[RNGridMenuItem alloc] initWithImage:code title:@"Project ID" action:codeBlock];
    RNGridMenuItem *loginItem = [[RNGridMenuItem alloc] initWithImage:login title:@"Login" action:loginBlock];
    RNGridMenuItem *aboutItem = [[RNGridMenuItem alloc] initWithImage:about title:@"About" action:aboutBlock];
    RNGridMenuItem *resetItem = [[RNGridMenuItem alloc] initWithImage:reset title:@"Reset" action:resetBlock];
    
    NSArray *items = [[NSArray alloc] initWithObjects:uploadItem, codeItem, loginItem, aboutItem, resetItem, nil];
    
    menu = [[RNGridMenu alloc] initWithItems:items];
    
    menu.delegate = self;
    
    [menu showInViewController:self center:CGPointMake(self.view.bounds.size.width/2.f, self.view.bounds.size.height/2.f)];
    
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void) takePicture:(id)sender {
    
    if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) {
        
        UIAlertView *myAlertView = [[UIAlertView alloc] initWithTitle:@"Error"
                                                              message:@"Device has no camera"
                                                             delegate:nil
                                                    cancelButtonTitle:@"OK"
                                                    otherButtonTitles: nil];
        
        [myAlertView show];
        return;
        
    }
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    picker.delegate = self;
    picker.allowsEditing = YES;
    picker.sourceType = UIImagePickerControllerSourceTypeCamera;
    
    [self presentViewController:picker animated:YES completion:nil];
}

- (IBAction)selectPhoto:(id)sender {
    
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    picker.delegate = self;
    picker.allowsEditing = YES;
    picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        UIPopoverController *popover = [[UIPopoverController alloc] initWithContentViewController:picker];
        [popover presentPopoverFromRect:self.view.window.bounds inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
        self.popOver = popover;
    } else {
        [self presentViewController:picker animated:YES completion:NULL];
    }
    
    
    
    
}

- (void) QRCode {
    [project dismissWithClickedButtonIndex:2 animated:YES];
    if ([[UIApplication sharedApplication]
         canOpenURL:[NSURL URLWithString:@"pic2shop:"]]) {
        NSURL *urlp2s = [NSURL URLWithString:@"pic2shop://scan?callback=pictures%3A//EAN"];
        [[UIApplication sharedApplication] openURL:urlp2s];
    } else {
        NSURL *urlapp = [NSURL URLWithString:
                         @"http://itunes.com/app/pic2shop"];
        [[UIApplication sharedApplication] openURL:urlapp];
    }
}

- (void) projCode {
    [project dismissWithClickedButtonIndex:0 animated:YES];
    proj_num = [[UIAlertView alloc] initWithTitle:@"Enter Project ID" message:@"" delegate:self cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
    [proj_num setAlertViewStyle:UIAlertViewStylePlainTextInput];
    if (useDev) {
        [proj_num textFieldAtIndex:0].text = [NSString stringWithFormat:@"%d",DEV_DEFAULT_PROJ];
    } else {
        [proj_num textFieldAtIndex:0].text = [NSString stringWithFormat:@"%d",PROD_DEFAULT_PROJ];
    }
    
    [proj_num show];
}

- (void) browseproj {
    [project dismissWithClickedButtonIndex:1 animated:YES];
    ProjectBrowseViewController *browse;
    browse = [[ProjectBrowseViewController alloc] init];
    browse.title = @"Browse for Projects";
    browse.delegate = self;
    [self.navigationController pushViewController:browse animated:YES];
    
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    NSString *title = [alertView buttonTitleAtIndex:buttonIndex];
    
    if ([alertView.title isEqualToString:@"Login to iSENSE"]) {
        
        NSString *email = [alertView textFieldAtIndex:0].text;
        NSString *pass = [alertView textFieldAtIndex:1].text;
        [self login:email withPassword:pass];
    } else if ([alertView.title isEqualToString:@"Project ID"]){
        if ([title isEqualToString:@"Enter Project ID"]) {
            [self projCode];
        } else if ([title isEqualToString:@"Browse"]) {
            [self browseproj];
        } else if ([title isEqualToString:@"QR Code"]) {
            [self QRCode];
        } else if ([title isEqualToString:@"Create New Project"]) {
            //[self createProject];
        } else {
            [project dismissWithClickedButtonIndex:3 animated:YES];
        }
    } else if ([alertView.title isEqualToString:@"Enter Project ID"]) {
        projID = [[alertView textFieldAtIndex:0].text intValue];
        if (![API hasConnectivity]) {
            projID = -1;
        } else {
            if (useDev) {
                projID = DEV_DEFAULT_PROJ;
            } else {
                projID = PROD_DEFAULT_PROJ;
            }
        }
        
    } else if ([alertView.title isEqualToString:@"Enter Project Name"]) {
        
        if ([title isEqualToString:@"Create Project"] && [API hasConnectivity] && [api getCurrentUser] != nil) {
            
            NSString *projName = [alertView textFieldAtIndex:0].text;
            
            if ([projName isEqualToString:@""]){
                
                UIAlertView *create = [[UIAlertView alloc] initWithTitle:@"Enter Project Name" message:nil delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"Create Project", nil];
                [create setAlertViewStyle:UIAlertViewStylePlainTextInput];
                [create show];
                [[[[UIApplication sharedApplication] windows] objectAtIndex:1] makeWaffle:@"Project Name Cannot Be Empty" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_RED_X];
            } else {
                
                RProjectField *time = [[RProjectField alloc] init];
                RProjectField *aX = [[RProjectField alloc] init];
                RProjectField *aY = [[RProjectField alloc] init];
                RProjectField *aZ = [[RProjectField alloc] init];
                RProjectField *aT = [[RProjectField alloc] init];
                
                time.name = @"Time";
                time.type = [NSNumber numberWithInt:TYPE_TIMESTAMP];
                NSString *b = @"Accel-";
                aX.type = aY.type = aZ.type = aT.type=  [NSNumber numberWithInt:TYPE_NUMBER];
                aX.name = [b stringByAppendingString:@"X"];
                aY.name = [b stringByAppendingString:@"Y"];
                aZ.name = [b stringByAppendingString:@"Z"];
                aT.name = [b stringByAppendingString:@"Total"];
                aX.unit = aY.unit = aZ.unit = aT.unit = @"m/s^2";
                
                NSMutableArray *fields = [[NSMutableArray alloc] initWithObjects:time,aX,aY,aZ,aT, nil];
                
                projID = [api createProjectWithName:projName andFields:fields];
                
                NSLog(@"projNum:%d", projID);
            }
            
        } else if (![API hasConnectivity]){
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"No connectivity" message:@"A project cannot be created due to a lack of network connection." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
            [message show];
        }
    }
}




- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    
    UIImage *chosenImage = info[UIImagePickerControllerEditedImage];
    //self.imageView.image = chosenImage;
    
    QDataSet *dataSet = [[QDataSet alloc] initWithEntity:[NSEntityDescription entityForName:@"QDataSet" inManagedObjectContext:managedObjectContext] insertIntoManagedObjectContext:managedObjectContext];
    
    UIImage *viewImage = chosenImage;  // --- mine was made from drawing context
    ALAssetsLibrary *library = [[ALAssetsLibrary alloc] init];
    // Request to save the image to camera roll
    
    [library writeImageToSavedPhotosAlbum:[viewImage CGImage] orientation:(ALAssetOrientation)[viewImage imageOrientation] completionBlock:^(NSURL *assetURL, NSError *error){
        if (error) {
            NSLog(@"error");
        } else {
            NSLog(@"url %@", assetURL);
            NSString *imageName = [assetURL lastPathComponent];
            
            NSLog(@"Image Name: %@", imageName);
            
            NSData *imageData = [NSData dataWithData:UIImageJPEGRepresentation(chosenImage, 1)];
            
            dataSet.picturePaths = [[NSArray alloc] initWithObjects:imageData, nil];
            
            [dataSet setName:imageName];
            [dataSet setParentName:@"PWViewController"];
            [dataSet setDataDescription:nil];
            [dataSet setUploadable:[NSNumber numberWithBool:YES]];
            [dataSet setProjID:[NSNumber numberWithInt:projID]];
            [dataSet setHasInitialProj:[NSNumber numberWithBool:TRUE]];
            
            bool success = [dataSaver addDataSet:dataSet];
            
            NSString *s = @"NO";
            
            if (success){
                s = @"YES";
            }
            
            NSLog(@"DataSet added?: %@", s);
            
            NSLog(@"Count: %d", dataSaver.dataQueue.count);
        }
    }];     
    
    
    
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        [self.popOver dismissPopoverAnimated:YES];
    } else {
        [picker dismissViewControllerAnimated:YES completion:nil];
    }
    
    
    
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    
    [picker dismissViewControllerAnimated:YES completion:nil];
    
}

// Log into iSENSE
- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    
    // __block BOOL success;
    // __block RPerson *curUser;
    
    UIAlertView *spinnerDialog = [self getDispatchDialogWithMessage:@"Logging in..."];
    [spinnerDialog show];
    
    dispatch_queue_t queue = dispatch_queue_create("dispatch_queue_t_dialog", NULL);
    dispatch_async(queue, ^{
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            BOOL success = [api createSessionWithUsername:usernameInput andPassword:passwordInput];
            if (success) {
                [self.view makeWaffle:[NSString stringWithFormat:@"Login as %@ successful", usernameInput]
                             duration:WAFFLE_LENGTH_SHORT
                            position:WAFFLE_BOTTOM
                               image:WAFFLE_CHECKMARK];
                
                NSLog(@"Login as %@ successful", usernameInput);
                
                // save the username and password in prefs
                NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
                [prefs setObject:usernameInput forKey:[StringGrabber grabString:@"key_username"]];
                [prefs setObject:passwordInput forKey:[StringGrabber grabString:@"key_password"]];
                [prefs synchronize];
                
                
                RPerson *curUser = [api getCurrentUser];
                
                NSString *loginstat = [@"Logged in as: " stringByAppendingString:usernameInput];
                
                [login_status setText:loginstat];
                userName = usernameInput;
                passWord = passwordInput;
            } else {
                [self.view makeWaffle:@"Login failed"
                             duration:WAFFLE_LENGTH_SHORT
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_RED_X];
            }
            [spinnerDialog dismissWithClickedButtonIndex:0 animated:YES];
            
        });
        
    });
    
}

// Default dispatch_async dialog with custom spinner
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



@end
