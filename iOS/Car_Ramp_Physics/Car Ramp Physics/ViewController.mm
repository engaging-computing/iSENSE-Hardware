//
//  ViewController.m
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/8/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin

#import "ViewController.h"

@interface ViewController ()

@end

@interface DLAVAlertViewController ()

+ (instancetype)sharedController;

- (void)setBackdropColor:(UIColor *)color;

- (void)addAlertView:(DLAVAlertView *)alertView;
- (void)removeAlertView:(DLAVAlertView *)alertView;

@end

@implementation ViewController

@synthesize start, menuButton, vector_status, items, recordLength, countdown, api, running, timeOver, setupDone, dfm, motionmanager, locationManager, recordDataTimer, timer, testLength, projNum, sampleInterval, sessionName,geoCoder,city,country,address,dataToBeJSONed,elapsedTime,recordingRate, project,userName,useDev,passWord,session_num,managedObjectContext,dataSaver,x,y,z,mag,proj_num, loginalert, pickerLength,lengths, lengthField, saveModeEnabled, saveMode, dataToBeOrdered, formatter, mngr,alert, buttonText, countdownLbl, menu, rates, pickerRate, rateField, enterName, name;

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    saver->user = userName;
    saver->pass = passWord;
    
    
    
    if (running) {
        return;
    }
    
    [start removeFromSuperview];
    [buttonText removeFromSuperview];
    
    UIImageView *titleView;
    NSBundle *isenseBundle = [NSBundle bundleWithURL:[[NSBundle mainBundle] URLForResource:@"iSENSE_API_Bundle" withExtension:@"bundle"]];
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"ViewController~landscape_iPad"
                                          owner:self
                                        options:nil];
            //[self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"ViewController_iPad"
                                          owner:self
                                        options:nil];
            //[self viewDidLoad];
        }
        titleView = [[UIImageView alloc] initWithImage:[UIImage imageWithContentsOfFile:[isenseBundle pathForResource:@"navBar_iPhone" ofType:@"png"]]];
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"ViewController~landscape_iPhone"
                                          owner:self
                                        options:nil];
            //[self viewDidLoad];
            titleView = [[UIImageView alloc] initWithImage:[UIImage imageWithContentsOfFile:[isenseBundle pathForResource:@"navBar~landscape_iPhone" ofType:@"png"]]];
            start = [[UIView alloc] initWithFrame:CGRectMake(10, 20, 460, 120)];
            buttonText = [[UILabel alloc] initWithFrame:CGRectMake(181, 47, 98, 21)];
            start.layer.borderColor = [UIColor grayColor].CGColor;
            start.layer.borderWidth = 2;
            start.layer.cornerRadius = 10;
            start.layer.masksToBounds = YES;
            [buttonText setText:@"Hold to Start"];
            [buttonText setTextColor:[UIColor blueColor]];
            [start addSubview:buttonText];
            [self.view addSubview:start];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"ViewController_iPhone"
                                          owner:self
                                        options:nil];
            //[self viewDidLoad];
            titleView = [[UIImageView alloc] initWithImage:[UIImage imageWithContentsOfFile:[isenseBundle pathForResource:@"navBar_iPhone" ofType:@"png"]]];
            start = [[UIView alloc] initWithFrame:CGRectMake(11, 20, 298, 255)];
            buttonText = [[UILabel alloc] initWithFrame:CGRectMake(99, 110, 98, 21)];
            start.layer.borderColor = [UIColor grayColor].CGColor;
            start.layer.borderWidth = 2;
            start.layer.cornerRadius = 10;
            start.layer.masksToBounds = YES;
            [buttonText setText:@"Hold to Start"];
            [buttonText setTextColor:[UIColor blueColor]];
            [start addSubview:buttonText];
            [self.view addSubview:start];
        }
        
    }
    
    UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longPress:)];
    [start setUserInteractionEnabled:YES];
    [start addGestureRecognizer:longPress];
    
    
    self.navigationItem.titleView = titleView;
    
    
}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
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

// returns the number of 'columns' to display.
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 1;
    
}

// returns the # of rows in each component..
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent: (NSInteger)component
{
    return 6;
    
}

-(NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    if ([pickerView isEqual:pickerLength]) {
        return [lengths objectAtIndex:row];
    } else {
        return [rates objectAtIndex:row];
    }
    
    
}
- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    if ([pickerView isEqual:pickerLength]) {
        lengthField.text =  [lengths objectAtIndex:row];
    } else {
        rateField.text = [rates objectAtIndex:row];
    }
    
    
}


- (void)viewDidLoad {
    
    [super viewDidLoad];
    
    formatter = [[NSNumberFormatter alloc] init];
    
    [formatter setNumberStyle:NSNumberFormatterDecimalStyle];
    
    [formatter setMaximumFractionDigits:3];
    
    [formatter setRoundingMode: NSNumberFormatterRoundUp];
    
    useDev = TRUE;
    
    api = [API getInstance];
    [api useDev: useDev];
    
    if (saver == nil) {
        saver = new RotationDataSaver;
        saver->hasLogin = false;
        saver->isLoggedIn = false;
        saver->user = [[NSString alloc] init];
        saver->pass = [[NSString alloc] init];
        saver->saveMode = NO;
    }
    
    saveModeEnabled = saver->saveMode;
    
    
    running = NO;
    timeOver = NO;
    setupDone = NO;
    
    motionmanager = [[CMMotionManager alloc] init];
    
    if (saver->hasLogin){
        userName = saver->user;
        passWord = saver->pass;
        [self login:userName withPassword:passWord];
        saver->isLoggedIn = true;
    } else {
        userName = @"";
        passWord = @"";
        saver->isLoggedIn = false;
        
        
    }
    
    
    if (saveModeEnabled) {
        projNum = -1;
    } else {
        NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
        projNum= [prefs integerForKey:KEY_PROJECT_ID];
        
        if (projNum == 0) {
            if (useDev) {
                projNum = DEV_DEFAULT_PROJ;
            } else {
                projNum = PROD_DEFAULT_PROJ;
            }
        }

        
    }
    
    if (alert != nil && ![alert isHidden] && setupDone) {
        [alert dismissWithClickedButtonIndex:0 animated:YES];
        mngr = [[CredentialManager alloc] initWithDelegate:self];
        DLAVAlertViewController *parent = [DLAVAlertViewController sharedController];
        [parent addChildViewController:mngr];
        alert = [[DLAVAlertView alloc] initWithTitle:@"Credential Manager" message:@"" delegate:nil cancelButtonTitle:@"Close" otherButtonTitles:nil];
        [alert setContentView:mngr.view];
        [alert setDismissesOnBackdropTap:YES];
        [alert show];
        
    }
    
    [countdownLbl setTextAlignment:NSTextAlignmentCenter];
    [vector_status setTextAlignment:NSTextAlignmentCenter];
    
    
    
    
    
    
    
    // Managed Object Context for Data_CollectorAppDelegate
    if (managedObjectContext == nil) {
        managedObjectContext = [(AppDelegate *)[[UIApplication sharedApplication] delegate] managedObjectContext];
    }
    
    // DataSaver from Data_CollectorAppDelegate
    if (dataSaver == nil) {
        dataSaver = [(AppDelegate *) [[UIApplication sharedApplication] delegate] dataSaver];
        NSLog(@"Datasaver Details: %@", dataSaver.description);
        NSLog(@"Current count = %d", dataSaver.dataQueue.count);
    }
    
    
    lengths = [[NSMutableArray alloc] initWithObjects:@"1 sec", @"2 sec", @"5 sec", @"10 sec", @"30 sec", @"60 sec", nil];
    rates = [[NSMutableArray alloc] initWithObjects:@"1 Hz", @"5 Hz", @"10 Hz", @"15 Hz", @"25 Hz", @"30 Hz", nil];
    
    pickerLength = [[UIPickerView alloc]init];
    [pickerLength setDataSource:self];
    [pickerLength setDelegate:self];
    
    [pickerLength setShowsSelectionIndicator:YES];
    
    pickerRate = [[UIPickerView alloc]init];
    [pickerRate setDataSource:self];
    [pickerRate setDelegate:self];
    
    [pickerRate setShowsSelectionIndicator:YES];
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    recordLength = countdown = [defaults integerForKey:@"recordLength"];
    
    dfm = [[DataFieldManager alloc] initWithProjID:projNum API:api andFields:nil];
    
    [[UINavigationBar appearance] setBackgroundImage:[[UIImage alloc] init] forBarMetrics:UIBarMetricsDefault];
    [[UINavigationBar appearance] setBackgroundColor:UIColorFromHex(0x111155)];
    UIButton* btton = [UIButton buttonWithType:UIButtonTypeCustom];
    [btton setFrame:CGRectMake(0, 0, 30, 30)];
    [btton addTarget:self action:@selector(showMenu) forControlEvents:UIControlEventTouchUpInside];
    NSBundle *isenseBundle = [NSBundle bundleWithURL:[[NSBundle mainBundle] URLForResource:@"iSENSE_API_Bundle" withExtension:@"bundle"]];
    [btton setImage:[UIImage imageWithContentsOfFile:[isenseBundle pathForResource:@"menuIcon" ofType:@"png"]] forState:UIControlStateNormal];
    menuButton = [[UIBarButtonItem alloc] initWithCustomView:btton];
    [menuButton setTintColor:UIColorFromHex(0x111155)];
    self.navigationItem.rightBarButtonItem = menuButton;
    [[UIBarButtonItem appearance] setTintColor:UIColorFromHex(0x111155)];
    [[UIButton appearance] setBackgroundImage:[self imageWithColor:UIColorFromHex(0x111155)] forState:UIControlStateHighlighted];
    [[UISearchBar appearance] setBackgroundImage:[self imageWithColor:UIColorFromHex(0x111155)]];
    [[UISearchBar appearance] setScopeBarBackgroundImage:[self imageWithColor:UIColorFromHex(0x111155)]];
    
    [self setUpMenu:isenseBundle];
    

    
}

- (void)actionSheet:(UIActionSheet *)popup clickedButtonAtIndex:(NSInteger)buttonIndex {
    //NSLog(@"Index: %@", buttonIndex);
    if ([popup.title isEqualToString:@"Recording Settings"]) {
        if ([[popup buttonTitleAtIndex:buttonIndex] isEqualToString:@"Recording Length"]) {
            NSLog(@"Length");
                    UIAlertView *talert = [[UIAlertView alloc] initWithTitle:@"Enter recording length" message:@"Enter time in seconds." delegate:self cancelButtonTitle:nil otherButtonTitles:@"Done", nil];
                    [talert setAlertViewStyle:UIAlertViewStylePlainTextInput];
                    lengthField = [talert textFieldAtIndex:0];
                    lengthField.inputView = pickerLength;
                    [self setPickerDefault:pickerLength];
                    [talert show];
            
        } else if ([[popup buttonTitleAtIndex:buttonIndex] isEqualToString:@"Recording Rate"]) {

                    NSLog(@"Rate");
                    UIAlertView *talert = [[UIAlertView alloc] initWithTitle:@"Enter recording rate" message:@"Enter rate in Hz." delegate:self cancelButtonTitle:nil otherButtonTitles:@"Done", nil];
                    [talert setAlertViewStyle:UIAlertViewStylePlainTextInput];
                    rateField = [talert textFieldAtIndex:0];
                    rateField.inputView = pickerRate;
                    [self setPickerDefault: pickerRate];
                    [talert show];
            
        }
        
        
    }
}

- (void) setUpMenu: (NSBundle*) isenseBundle {
    UIImage *upload = [UIImage imageWithContentsOfFile:[isenseBundle pathForResource:@"upload2" ofType:@"png"]];
    UIImage *settings = [UIImage imageWithContentsOfFile:[isenseBundle pathForResource:@"settings" ofType:@"png"]];;
    UIImage *code = [UIImage imageWithContentsOfFile:[isenseBundle pathForResource:@"barcode" ofType:@"png"]];
    UIImage *login = [UIImage imageWithContentsOfFile:[isenseBundle pathForResource:@"users" ofType:@"png"]];
    UIImage *about = [UIImage imageWithContentsOfFile:[isenseBundle pathForResource:@"info" ofType:@"png"]];
    UIImage *reset = [UIImage imageWithContentsOfFile:[isenseBundle pathForResource:@"reset" ofType:@"png"]];
    
    void (^uploadBlock)() = ^() {
        NSLog(@"Upload button pressed");
        if (dataSaver.dataQueue.count > 0) {
            QueueUploaderView *queueUploader = [[QueueUploaderView alloc] init];
            queueUploader.title = @"Upload saved data";
            menu = nil;
            [self.navigationController pushViewController:queueUploader animated:YES];
        } else {
            [self.view makeWaffle:@"No data sets to upload!" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
            menu = nil;
        }
        
    };
    void (^settingsBlock)() = ^() {
        menu = nil;
        UIActionSheet *settings = [[UIActionSheet alloc] initWithTitle:@"Recording Settings" delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Recording Length", @"Recording Rate", nil];
        settings.tag = 1;
        [settings showInView:self.view];
        
        
    };
    void (^codeBlock)() = ^() {
        NSLog(@"project button pressed");
        project = [[UIAlertView alloc] initWithTitle:@"Project ID" message:@"" delegate:self cancelButtonTitle:nil otherButtonTitles: nil];
        [project addButtonWithTitle:@"Enter Project ID"];
        [project addButtonWithTitle:@"Browse"];
        [project addButtonWithTitle:@"QR Code"];
        [project addButtonWithTitle:@"Create New Project"];
        [project addButtonWithTitle:@"Done"];
        menu = nil;
        [project show];
        
    };
    void (^loginBlock)() = ^() {
        NSLog(@"Login button pressed");
        
        mngr = [[CredentialManager alloc] initWithDelegate:self];
        DLAVAlertViewController *parent = [DLAVAlertViewController sharedController];
        [parent addChildViewController:mngr];
        alert = [[DLAVAlertView alloc] initWithTitle:@"Credential Manager" message:@"" delegate:nil cancelButtonTitle:@"Close" otherButtonTitles:nil];
        [alert setContentView:mngr.view];
        [alert setDismissesOnBackdropTap:YES];
        menu = nil;
        [alert show];
        
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
        menu = nil;
        [self.navigationController pushViewController:about animated:YES];
        
    };
    void (^resetBlock)() = ^() {
        NSLog(@"Reset button pressed");
        countdown = recordLength = 10;
        recordingRate = 30;
        [[API getInstance] deleteSession];
        menu = nil;
        enterName = [[DLAVAlertView alloc] initWithTitle:@"Enter Name" message:@"" delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
        [enterName setAlertViewStyle:DLAVAlertViewStyleLoginAndPasswordInput];
        [enterName textFieldAtIndex:0].placeholder = @"First Name";
        [enterName textFieldAtIndex:1].placeholder = @"Last Initial";
        [enterName textFieldAtIndex:0].delegate = self;
        [enterName textFieldAtIndex:1].delegate = self;
        [enterName textFieldAtIndex:1].secureTextEntry = NO;
        [enterName textFieldAtIndex:0].tag = FIRST_NAME_FIELD;
        [enterName showWithCompletion:^(DLAVAlertView *alertView, NSInteger buttonIndex) {
            name = [NSString stringWithFormat:@"%@ %@.", [alertView textFieldTextAtIndex:0],[alertView textFieldTextAtIndex:1]];
        }];
    };
    
    RNGridMenuItem *uploadItem = [[RNGridMenuItem alloc] initWithImage:upload title:@"Upload" action:uploadBlock];
    RNGridMenuItem *recordSettingsItem = [[RNGridMenuItem alloc] initWithImage:settings title:@"Recording Settings" action:settingsBlock];
    RNGridMenuItem *codeItem = [[RNGridMenuItem alloc] initWithImage:code title:@"Project ID" action:codeBlock];
    RNGridMenuItem *loginItem = [[RNGridMenuItem alloc] initWithImage:login title:@"Login" action:loginBlock];
    RNGridMenuItem *aboutItem = [[RNGridMenuItem alloc] initWithImage:about title:@"About" action:aboutBlock];
    RNGridMenuItem *resetItem = [[RNGridMenuItem alloc] initWithImage:reset title:@"Reset" action:resetBlock];
    
    items = [[NSArray alloc] initWithObjects:uploadItem, recordSettingsItem, codeItem, loginItem, aboutItem, resetItem, nil];
    
    menu = [[RNGridMenu alloc] initWithItems:items];
    
    menu.delegate = self;
}

- (UIImage *)imageWithColor:(UIColor *)color {
    CGRect rect = CGRectMake(0.0f, 0.0f, 1.0f, 1.0f);
    UIGraphicsBeginImageContext(rect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    CGContextSetFillColorWithColor(context, [color CGColor]);
    CGContextFillRect(context, rect);
    
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return image;
}

- (void) didPressLogin:(CredentialManager *)mngr {
    [alert dismissWithClickedButtonIndex:0 animated:YES];
    alert = nil;
    loginalert = [[UIAlertView alloc] initWithTitle:@"Login to iSENSE" message:@"" delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"OK", nil];
    [loginalert setAlertViewStyle:UIAlertViewStyleLoginAndPasswordInput];
    [loginalert textFieldAtIndex:0].delegate = self;
    [loginalert textFieldAtIndex:0].tag = LOGIN_USER;
    [[loginalert textFieldAtIndex:0] becomeFirstResponder];
    [loginalert textFieldAtIndex:1].delegate = self;
    [loginalert textFieldAtIndex:1].tag = LOGIN_PASS;
    [loginalert textFieldAtIndex:0].placeholder = @"Email";
    [loginalert show];
}

- (void) setPickerDefault:(UIPickerView *) picker {
    if ([picker isEqual:pickerLength]) {
            switch (countdown) {
                case 1:
                    [picker selectRow:0 inComponent:0 animated:YES];
                    lengthField.text = [lengths objectAtIndex:0];
                    break;
                case 2:
                    [picker selectRow:1 inComponent:0 animated:YES];
                    lengthField.text = [lengths objectAtIndex:1];
                    break;
                case 5:
                    [picker selectRow:2 inComponent:0 animated:YES];
                    lengthField.text = [lengths objectAtIndex:2];
                    break;
                case 10:
                    [picker selectRow:3 inComponent:0 animated:YES];
                    lengthField.text = [lengths objectAtIndex:3];
                    break;
                case 30:
                    [picker selectRow:4 inComponent:0 animated:YES];
                    lengthField.text = [lengths objectAtIndex:4];
                    break;
                case 60:
                    [picker selectRow:5 inComponent:0 animated:YES];
                    lengthField.text = [lengths objectAtIndex:5];
                    break;
                default:
                    break;
            }
    } else {
            switch (recordingRate) {
                case 1:
                    [picker selectRow:0 inComponent:0 animated:YES];
                    rateField.text = [rates objectAtIndex:0];
                    break;
                case 5:
                    [picker selectRow:1 inComponent:0 animated:YES];
                    rateField.text = [rates objectAtIndex:1];
                    break;
                case 10:
                    [picker selectRow:2 inComponent:0 animated:YES];
                    rateField.text = [rates objectAtIndex:2];
                    break;
                case 15:
                    [picker selectRow:3 inComponent:0 animated:YES];
                    rateField.text = [rates objectAtIndex:3];
                    break;
                case 25:
                    [picker selectRow:4 inComponent:0 animated:YES];
                    rateField.text = [rates objectAtIndex:4];
                    break;
                case 30:
                    [picker selectRow:5 inComponent:0 animated:YES];
                    rateField.text = [rates objectAtIndex:5];
                    break;
                default:
                    break;

            }
        }
    
    [picker reloadComponent:0];
}

- (void) viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    BOOL x1 = [prefs boolForKey:@"X"];
    BOOL y1 = [prefs boolForKey:@"Y"];
    BOOL z1 = [prefs boolForKey:@"Z"];
    BOOL mag1 = [prefs boolForKey:@"Magnitude"];
    
    
    x = x1;
    y = y1;
    z = z1;
    mag = mag1;
    
    if (self.isMovingToParentViewController == YES) {
        
        
        if (![API hasConnectivity]){
            [dfm setEnabledField:x atIndex:fACCEL_X];
            [dfm setEnabledField:y atIndex:fACCEL_Y];
            [dfm setEnabledField:z atIndex:fACCEL_Z];
            [dfm setEnabledField:mag atIndex:fACCEL_TOTAL];
        }
        
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        recordLength = countdown = [defaults integerForKey:@"recordLength"];
        
        [self saveModeDialog];
        enterName = [[DLAVAlertView alloc] initWithTitle:@"Enter Name" message:@"" delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
        [enterName setAlertViewStyle:DLAVAlertViewStyleLoginAndPasswordInput];
        [enterName textFieldAtIndex:0].placeholder = @"First Name";
        [enterName textFieldAtIndex:1].placeholder = @"Last Initial";
        [enterName textFieldAtIndex:0].delegate = self;
        [enterName textFieldAtIndex:1].delegate = self;
        [enterName textFieldAtIndex:1].secureTextEntry = NO;
        [enterName textFieldAtIndex:0].tag = FIRST_NAME_FIELD;
        [enterName showWithCompletion:^(DLAVAlertView *alertView, NSInteger buttonIndex) {
            name = [NSString stringWithFormat:@"%@ %@.", [alertView textFieldTextAtIndex:0],[alertView textFieldTextAtIndex:1]];
        }];
        
    }
    
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
    setupDone = YES;
    
}

- (void) saveModeDialog {
    if (![API hasConnectivity]) {
        saveMode = [[UIAlertView alloc] initWithTitle:@"No Connectivity" message:@"Could not connect to the Internet through either Wi-Fi or mobile service. You will not be able to upload data to iSENSE until either is enabled.\n* Turning on Save Mode will allow data to be saved until Internet is enabled." delegate:self cancelButtonTitle:@"Try Again" otherButtonTitles:@"Save Mode", nil];
        
        [saveMode show];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)longPress:(UILongPressGestureRecognizer*)gesture {
    if (gesture.state == UIGestureRecognizerStateBegan) {
        [start setBackgroundColor:UIColorFromHex(0x111155)];
        [buttonText setBackgroundColor:UIColorFromHex(0x111155)];
        [buttonText setTextColor:[UIColor whiteColor]];
    } else if ( gesture.state == UIGestureRecognizerStateEnded ) {
        NSLog(@"Long Press");
        [start setBackgroundColor:[UIColor clearColor]];
        [buttonText setBackgroundColor:[UIColor clearColor]];
        [buttonText setTextColor:[UIColor blackColor]];
        if (!running) {
            // Get Field Order
            if (![API hasConnectivity]){
                [dfm setEnabledField:x atIndex:fACCEL_X];
                [dfm setEnabledField:y atIndex:fACCEL_Y];
                [dfm setEnabledField:z atIndex:fACCEL_Z];
                [dfm setEnabledField:mag atIndex:fACCEL_TOTAL];
            } else {
                [self getEnabledFields];
            }
            // Record Data
            running = YES;
            [start setUserInteractionEnabled:NO];
            [buttonText setText:@"Recording..."];
            [buttonText setTextColor:[UIColor greenColor]];
            [self recordData];
        }
        
        NSString *path = [NSString stringWithFormat:@"%@%@", [[NSBundle mainBundle] resourcePath], @"/button-37.wav"];
        SystemSoundID soundID;
        NSURL *filePath = [NSURL fileURLWithPath:path isDirectory:NO];
        CFURLRef url = (__bridge CFURLRef)filePath;
        AudioServicesCreateSystemSoundID(url, &soundID);
        AudioServicesPlaySystemSound(soundID);
        
    }
}

// Record the data and return the NSMutable array to be JSONed
- (void) recordData {
    
    //[start setTitle:[NSString stringWithFormat:@"%d", countdown] forState:UIControlStateNormal];
    // Get the recording rate
    float rate = 0.02;
    sampleInterval = 15;
    if (sampleInterval > 0) rate = sampleInterval / 1000;
    
    elapsedTime = 0;
    recordingRate = rate * 1000;
    
    NSLog(@"rate: %d", recordingRate);
    
    // Set the accelerometer update interval to reccomended sample interval, and start updates
    motionmanager.accelerometerUpdateInterval = rate;
    motionmanager.magnetometerUpdateInterval = rate;
    motionmanager.gyroUpdateInterval = rate;
    if (motionmanager.accelerometerAvailable) [motionmanager startAccelerometerUpdates];
    if (motionmanager.magnetometerAvailable) [motionmanager startMagnetometerUpdates];
    if (motionmanager.gyroAvailable) [motionmanager startGyroUpdates];
    
    // New JSON array to hold data
    dataToBeJSONed = [[NSMutableArray alloc] init];
    dataToBeOrdered = [[NSMutableArray alloc] init];
    [dfm enableAllFields];
    
    // Start the new timers TODO - put them on dispatch?
    recordDataTimer = [NSTimer scheduledTimerWithTimeInterval:rate target:self selector:@selector(buildRowOfData) userInfo:nil repeats:YES];
    timer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(updateElapsedTime) userInfo:nil repeats:YES];
}


- (void) updateElapsedTime {
    
    if (!running) {
        
    }
    
    
    
    dispatch_queue_t queue = dispatch_queue_create("automatic_update_elapsed_time", NULL);
    dispatch_async(queue, ^{
        elapsedTime += 1;
        
        int seconds = elapsedTime % 60;
        
        NSString *secondsStr;
        if (seconds < 10)
            secondsStr = [NSString stringWithFormat:@"0%d", seconds];
        else
            secondsStr = [NSString stringWithFormat:@"%d", seconds];
        
        int dataPoints = (1000 / recordingRate) * elapsedTime;
        
        NSLog(@"points: %d", dataPoints);
        
        if (countdown >=  1) {
            dispatch_async(dispatch_get_main_queue(), ^{
                NSString *countdownStr = [NSString stringWithFormat:@"%d", countdown--];
                [countdownLbl setText:[NSString stringWithFormat:@"Time Elapsed: %@", countdownStr]];
                
            });
        }
        
        if (countdown <= 0) {
            
            [self stopRecording:motionmanager];
        }
        
        
        
        
    });
    
}


// Fill dataToBeOrdered with a row of data
- (void) buildRowOfData {
    
    if (!running || recordDataTimer == nil) {
        
        [recordDataTimer invalidate];
        recordDataTimer = nil;
        
    } else {
        
        dispatch_queue_t queue = dispatch_queue_create("automatic_record_data", NULL);
        dispatch_async(queue, ^{
            
            Fields *fieldsRow = [[Fields alloc] init];
            
            NSString *vector = @"";
            
            // Fill a new row of data starting with time
            double time = [[NSDate date] timeIntervalSince1970];
            if ([dfm enabledFieldAtIndex:fTIME_MILLIS])
                fieldsRow.time_millis = [NSNumber numberWithDouble:time * 1000];
            
            
            // acceleration in meters per second squared
            if ([dfm enabledFieldAtIndex:fACCEL_X]) {
                fieldsRow.accel_x = [NSNumber numberWithDouble:[motionmanager.accelerometerData acceleration].x * 9.80665];
                vector = [vector stringByAppendingString:@"X: "];
                vector = [vector stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_x]];
            } if ([dfm enabledFieldAtIndex:fACCEL_Y]) {
                fieldsRow.accel_y = [NSNumber numberWithDouble:[motionmanager.accelerometerData acceleration].y * 9.80665];
                if ([vector length] == 0) {
                    vector = [vector stringByAppendingString:@"Y: "];
                } else {
                    vector = [vector stringByAppendingString:@"\nY: "];
                }
                vector = [vector stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_y]];
            } if ([dfm enabledFieldAtIndex:fACCEL_Z]) {
                fieldsRow.accel_z = [NSNumber numberWithDouble:[motionmanager.accelerometerData acceleration].z * 9.80665];
                if ([vector length] == 0) {
                    vector = [vector stringByAppendingString:@"Z: "];
                } else {
                    vector = [vector stringByAppendingString:@"\nZ: "];
                }
                vector = [vector stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_z]];
            } if ([dfm enabledFieldAtIndex:fACCEL_TOTAL]) {
                fieldsRow.accel_total = [NSNumber numberWithDouble:
                                         sqrt(pow(fieldsRow.accel_x.doubleValue, 2)
                                              + pow(fieldsRow.accel_y.doubleValue, 2)
                                              + pow(fieldsRow.accel_z.doubleValue, 2))];
                
                if ([vector length] == 0) {
                    vector = [vector stringByAppendingString:@"Total: "];
                } else {
                    vector = [vector stringByAppendingString:@"\nTotal: "];
                }
                vector = [vector stringByAppendingString:[formatter stringFromNumber:fieldsRow.accel_total]];
                
            }
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [vector_status setText:vector];
            });
            
            
            // update data object
            if (dataToBeOrdered == nil)
                dataToBeOrdered = [[NSMutableArray alloc] init];
            
            //[dataToBeOrdered addObject:fieldsRow];
            
            if (dataToBeOrdered != nil) {
                [dfm setFields:fieldsRow];
                
                [dataToBeOrdered addObject:[dfm putData]];
                
            }
            
        });
    }
    
}

// This inits locations
- (void) initLocations {
    if (!locationManager) {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
        locationManager.distanceFilter = kCLDistanceFilterNone;
        locationManager.desiredAccuracy = kCLLocationAccuracyBest;
        [locationManager startUpdatingLocation];
        geoCoder = [[CLGeocoder alloc] init];
    }
}

// Stops the recording and returns the actual data recorded :)
-(void) stopRecording:(CMMotionManager *)finalMotionManager {
    
    // Stop Timers
    [timer invalidate];
    [recordDataTimer invalidate];
    [menuButton setEnabled:YES];
    
    // Stop Sensors
    if (finalMotionManager.accelerometerActive) [finalMotionManager stopAccelerometerUpdates];
    if (finalMotionManager.gyroActive) [finalMotionManager stopGyroUpdates];
    if (finalMotionManager.magnetometerActive) [finalMotionManager stopMagnetometerUpdates];
    
    // Stop Recording
    running = NO;
    [vector_status setText:@""];
    countdown = recordLength;
    dispatch_async(dispatch_get_main_queue(), ^{
        [buttonText setText:@"Hold to Start"];
        [buttonText setTextColor:[UIColor blueColor]];
        [start setUserInteractionEnabled:YES];
        [countdownLbl setText:@""];
        
    });
    
    NSString *path = [NSString stringWithFormat:@"%@%@", [[NSBundle mainBundle] resourcePath], @"/beep.wav"];
    SystemSoundID soundID;
    NSURL *filePath = [NSURL fileURLWithPath:path isDirectory:NO];
    CFURLRef url = (__bridge CFURLRef)filePath;
    AudioServicesCreateSystemSoundID(url, &soundID);
    AudioServicesPlaySystemSound(soundID);
    
    dispatch_async(dispatch_get_main_queue(), ^{
        
        UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Publish to iSENSE?"
                                                          message:nil
                                                         delegate:self
                                                cancelButtonTitle:@"Discard"
                                                otherButtonTitles:@"Publish", nil];
        
        message.delegate = self;
        [message show];
    });
    
}

-(void) stopRecordingWithoutPublishing:(CMMotionManager *)finalMotionManager {
    
    // Stop Timers
    [timer invalidate];
    [recordDataTimer invalidate];
    [menuButton setEnabled:YES];
    
    // Stop Sensors
    if (finalMotionManager.accelerometerActive) [finalMotionManager stopAccelerometerUpdates];
    if (finalMotionManager.gyroActive) [finalMotionManager stopGyroUpdates];
    if (finalMotionManager.magnetometerActive) [finalMotionManager stopMagnetometerUpdates];
    
    // Stop Recording
    running = NO;
    [vector_status setText:@""];
    countdown = recordLength;
    dispatch_async(dispatch_get_main_queue(), ^{
        [buttonText setText:@"Hold to Start"];
        [buttonText setTextColor:[UIColor blueColor]];
        [start setUserInteractionEnabled:YES];
        [countdownLbl setText:@""];
        
    });
    
    NSString *name = [api getCurrentUser].name;
    name = [name stringByAppendingString:@". "];
    
}


// Enabled fields check
- (void) getEnabledFields {
    
    // if proj# = -1 then enable all, else enable some
    if (projNum == -1) {
        
        for (int i = 0; i < [[dfm order] count]; i++) {
            [dfm setEnabledField:YES atIndex:i];
        }
        
    } else {
        
        int i = 0;
        
        for (NSString *s in [dfm order]) {
            if ([s isEqualToString:[StringGrabber grabField:@"accel_x"]]) {
                [dfm setEnabledField:YES atIndex:fACCEL_X];
                
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"accel_y"]]) {
                [dfm setEnabledField:YES atIndex:fACCEL_Y];
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"accel_z"]]) {
                [dfm setEnabledField:YES atIndex:fACCEL_Z];
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"accel_total"]]) {
                [dfm setEnabledField:YES atIndex:fACCEL_TOTAL];
            }
            else if ([s isEqualToString:[StringGrabber grabField:@"time"]]) {
                [dfm setEnabledField:YES atIndex:fTIME_MILLIS];
            }
            
            ++i;
        }
    }
}

- (void) browseproj {
    [project dismissWithClickedButtonIndex:1 animated:YES];
    ProjectBrowserViewController *browser = [[ProjectBrowserViewController alloc] initWithDelegate:self];
    [self.navigationController pushViewController:browser animated:YES];
    
}

- (void) didFinishChoosingProject:(ProjectBrowserViewController *) browser withID: (int) project_id {
    projNum = project_id;
    NSLog(@"ID = %d", projNum);
    dfm = [[DataFieldManager alloc] initWithProjID:projNum API:api andFields:nil];
    [self launchFieldMatchingViewControllerFromBrowse:TRUE];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    
    // ADD: get the decode results
    id<NSFastEnumeration> results =
    [info objectForKey: ZBarReaderControllerResults];
    ZBarSymbol *symbol = nil;
    for(symbol in results)
        // EXAMPLE: just grab the first barcode
        break;
    
    // EXAMPLE: do something useful with the barcode data
    NSLog(@"QR Data: %@", symbol.data);
    
    NSRange range = [symbol.data rangeOfString:@"projects"];
    
    NSMutableCharacterSet *_slashes = [NSMutableCharacterSet characterSetWithCharactersInString:@"/"];
    
    NSString *proj = [[symbol.data substringFromIndex:NSMaxRange(range)] stringByTrimmingCharactersInSet:_slashes];
    
    projNum= [proj intValue];
    
    NSLog(@"ExpNum: %d", projNum);
    
    // ADD: dismiss the controller (NB dismiss from the *reader*!)
    [picker dismissModalViewControllerAnimated: YES];
    if (projNum != 0) {
        
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        [prefs setInteger:projNum forKey:KEY_PROJECT_ID];
        
    }
    
    dfm = [[DataFieldManager alloc] initWithProjID:projNum API:api andFields:nil];
    
    [self.view makeWaffle:[NSString stringWithFormat:@"Project Number: %d", projNum] duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:@"" image:WAFFLE_CHECKMARK];
    
    [self launchFieldMatchingViewControllerFromBrowse:FALSE];
    
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    
    [picker dismissViewControllerAnimated:YES completion:nil];
    
}


- (void) QRCode {
    [project dismissWithClickedButtonIndex:2 animated:YES];
    ZBarReaderViewController *reader = [ZBarReaderViewController new];
    reader.readerDelegate = self;
    reader.supportedOrientationsMask = ZBarOrientationMaskAll;
    
    ZBarImageScanner *scanner = reader.scanner;
    // TODO: (optional) additional reader configuration here
    
    // EXAMPLE: disable rarely used I2/5 to improve performance
    [scanner setSymbology: ZBAR_I25
                   config: ZBAR_CFG_ENABLE
                       to: 0];
    
    // present and release the controller
    [self presentModalViewController: reader
                            animated: YES];}

- (void) projCode {
    [project dismissWithClickedButtonIndex:0 animated:YES];
    proj_num = [[UIAlertView alloc] initWithTitle:@"Enter Project ID" message:@"" delegate:self cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
    [proj_num setAlertViewStyle:UIAlertViewStylePlainTextInput];
    [proj_num textFieldAtIndex:0].text = [NSString stringWithFormat:@"%d",projNum];
    [proj_num show];
}

// Save a data set so you don't have to upload it immediately
- (void) saveDataSetWithDescription:(NSString *)description {
    
    bool uploadable = false;
    [menuButton setEnabled:YES];
    
    if (![API hasConnectivity])
        projNum = -1;
    
    if (projNum > 1) uploadable = true;
    
    NSLog(@"Bla");
    QDataSet *ds = [[QDataSet alloc] initWithEntity:[NSEntityDescription entityForName:@"QDataSet" inManagedObjectContext:managedObjectContext] insertIntoManagedObjectContext:managedObjectContext];
    [ds setName:sessionName];
    [ds setDataDescription:description];
    [ds setProjID:[NSNumber numberWithInt:projNum]];
    [ds setData:dataToBeJSONed];
    [ds setPicturePaths:nil];
    [ds setUploadable:[NSNumber numberWithBool:uploadable]];
    [ds setParentName:@"CarRampPhysics"];
    NSLog(@"Bla2");
    // Add the new data set to the queue
    [dataSaver addDataSet:ds];
    NSLog(@"There are %d dataSets in the dataSaver.", dataSaver.dataQueue.count);
    
    
}

- (bool) uploadData:(NSString *) description {
    
    if ([API hasConnectivity]) {
        
        
        
        
        sessionName = [NSString stringWithFormat:@"%@.", [api getCurrentUser].name];
        if ([api getCurrentUser] == nil){
            sessionName = @"";
        }
        
        UIAlertView *message = [self getDispatchDialogWithMessage:@"Uploading to iSENSE..."];
        [message show];
        
        dispatch_queue_t queue = dispatch_queue_create("uploading_data", NULL);
        dispatch_async(queue, ^{
            dataToBeJSONed = [DataFieldManager reOrderData:dataToBeOrdered forProjectID:projNum withFieldOrder:[dfm getOrderList] andFieldIDs:[dfm getFieldIDs]];
            NSLog(@"REORDER SUCCESSFUL: %@", dataToBeJSONed);
            NSMutableDictionary *data = [[NSMutableDictionary alloc] init];
            [data setObject:dataToBeJSONed forKey:@"data"];
            data = [[api rowsToCols:data] mutableCopy];
            
            __block int ds_id;
            
            if ([api getCurrentUser] == nil) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    DLAVAlertView *contribKeyAlert = [[DLAVAlertView alloc] initWithTitle:@"Enter Contributor Key" message:@"" delegate:nil cancelButtonTitle:@"Cancel" otherButtonTitles:@"Upload", nil];
                    [contribKeyAlert setAlertViewStyle:DLAVAlertViewStyleLoginAndPasswordInput];
                    [contribKeyAlert textFieldAtIndex:0].placeholder = @"Contributor Name";
                    [contribKeyAlert textFieldAtIndex:1].placeholder = @"Contributor Key";
                    [contribKeyAlert textFieldAtIndex:0].text = name;
                
                    [contribKeyAlert showWithCompletion:^(DLAVAlertView *alertView, NSInteger buttonIndex) {
                        if ([[alertView buttonTitleAtIndex:buttonIndex] isEqualToString:@"Upload"]) {
                            ds_id = [api uploadDataWithId:projNum withData:data withContributorKey:[contribKeyAlert textFieldTextAtIndex:1] as:@"" andName:[contribKeyAlert textFieldTextAtIndex:0]];
                        }
                    }];
                });
                
            } else {
                ds_id = [api uploadDataWithId:projNum withData:data andName:sessionName];
            }

            dispatch_async(dispatch_get_main_queue(), ^{
                [message dismissWithClickedButtonIndex:nil animated:YES];
                if (ds_id == -1) {
                    [self.view makeWaffle:@"Unable to upload" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_RED_X];
                } else {
                    [self.view makeWaffle:@"Upload successful" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_CHECKMARK];
                    session_num = [[NSNumber alloc] initWithInt:ds_id];
                    
                }
                
                
            });
        });
        
    } else {
        [self saveDataSetWithDescription:sessionName];
    }
    return true;
    
}

- (void)showMenu {
    
    if (menu != nil) {
        menu = nil;
    }
    
    NSBundle *isenseBundle = [NSBundle bundleWithURL:[[NSBundle mainBundle] URLForResource:@"iSENSE_API_Bundle" withExtension:@"bundle"]];
    [self setUpMenu:isenseBundle];
    
    if (![menu isVisible]) {
       [menu showInViewController:self center:CGPointMake(self.view.bounds.size.width/2.f, self.view.bounds.size.height/2.f)];
    } else {
        [menu dismissAnimated:YES];
        menu = nil;
    }
    
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    
    NSCharacterSet *cs = [[NSCharacterSet characterSetWithCharactersInString:ACCEPTABLE_CHARACTERS] invertedSet];
    NSUInteger newLength = [textField.text length] + [string length] - range.length;
    if ([string rangeOfCharacterFromSet:cs].location == NSNotFound) {
        if (textField.tag == FIRST_NAME_FIELD || textField.tag == LOGIN_PASS) {
            return (newLength > 20) ? NO : YES;
        } else if (textField.tag == LOGIN_USER) {
            return (newLength > 100) ? NO : YES;
        }else {
            return (newLength > 1) ? NO : YES;
        }
    } else {
        return NO;
    }
    
    
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    NSString *title = [alertView buttonTitleAtIndex:buttonIndex];
    
    if ([alertView.title isEqualToString:@"Enter recording length"]) {
        
        if([title isEqualToString:@"Done"])
        {
            UITextField *length = [alertView textFieldAtIndex:0];
            
            NSArray *lolcats = [length.text componentsSeparatedByString:@" "];
            recordLength = countdown = [lolcats[0] intValue];
            NSLog(@"Length is %d", recordLength);
            
            NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
            [defaults setInteger:recordLength forKey:@"recordLength"];
            
        }
    } else if ([alertView.title isEqualToString:@"Enter recording rate"]) {
        
        if([title isEqualToString:@"Done"])
        {
            UITextField *length = [alertView textFieldAtIndex:0];
            
            NSArray *lolcats = [length.text componentsSeparatedByString:@" "];
            sampleInterval = recordingRate = [lolcats[0] floatValue];
            NSLog(@"Sample Interval is %f", sampleInterval);
            
        }
    } else if ([alertView.title isEqualToString:@"Login to iSENSE"]) {
        [self login:[alertView textFieldAtIndex:0].text withPassword:[alertView textFieldAtIndex:1].text];
        userName = [alertView textFieldAtIndex:0].text;
        passWord = [alertView textFieldAtIndex:1].text;
    } else if ([alertView.title isEqualToString:@"Publish to iSENSE?"]) {
        if ([title isEqualToString:@"Discard"]) {
            [self.view makeWaffle:@"Data discarded!" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM title:nil image:WAFFLE_RED_X];
        } else {
            [self uploadData: @""];
        }
    } else if ([alertView.title isEqualToString:@"Project ID"]){
        if ([title isEqualToString:@"Enter Project ID"]) {
            [self projCode];
        } else if ([title isEqualToString:@"Browse"]) {
            [self browseproj];
        } else if ([title isEqualToString:@"QR Code"]) {
            [self QRCode];
        } else if ([title isEqualToString:@"Create New Project"]) {
            [self createProject];
        } else {
            [project dismissWithClickedButtonIndex:3 animated:YES];
        }
    } else if ([alertView.title isEqualToString:@"Enter Project ID"]) {
        projNum = [[alertView textFieldAtIndex:0].text intValue];
        NSLog(@"ID = %d", projNum);
        dfm = [[DataFieldManager alloc] initWithProjID:projNum API:api andFields:nil];
        [self launchFieldMatchingViewControllerFromBrowse:FALSE];
    } else if ([alertView.title isEqualToString:@"No Connectivity"]) {
        if ([title isEqualToString:@"Try Again"]){
            [self saveModeDialog];
        } else {
            saveModeEnabled = YES;
            saver->saveMode = YES;
            projNum = -1;
            [self.view makeWaffle:@"Save Mode Enabled" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_CHECKMARK];
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
                
                UIAlertView *spinnerDialog = [self getDispatchDialogWithMessage:@"Creating Project..."];
                [spinnerDialog show];
                
                dispatch_queue_t queue = dispatch_queue_create("dispatch_queue_t_dialog", NULL);
                dispatch_async(queue, ^{
                    
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
                    
                    projNum = [api createProjectWithName:projName andFields:fields];
                    
                    NSLog(@"projNum:%d", projNum);
                    
                    dispatch_async(dispatch_get_main_queue(), ^{
                        
                        
                        if (projNum != -1) {
                            NSLog(@"Damn");
                            [self.view makeWaffle:[NSString stringWithFormat:@"Project #%d successfully created!", projNum]
                                         duration:WAFFLE_LENGTH_SHORT
                                         position:WAFFLE_BOTTOM
                                            image:WAFFLE_CHECKMARK];
                            
                            NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
                            [prefs setInteger:projNum forKey:KEY_PROJECT_ID];
                        } else {
                            [self.view makeWaffle:@"Project creation was unsuccessful."
                                         duration:WAFFLE_LENGTH_SHORT
                                         position:WAFFLE_BOTTOM
                                            image:WAFFLE_RED_X];
                        }
                        [spinnerDialog dismissWithClickedButtonIndex:0 animated:YES];
                        
                    });
                });
                
                
            }
            
        } else if (![API hasConnectivity]){
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"No connectivity" message:@"A project cannot be created due to a lack of network connection." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
            [message show];
        }
    }
}

- (void) createProject {
    
    UIAlertView *create = [[UIAlertView alloc] initWithTitle:@"Enter Project Name" message:nil delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"Create Project", nil];
    [create setAlertViewStyle:UIAlertViewStylePlainTextInput];
    [create show];
    
    
    
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
            
            RPerson *success = [api createSessionWithEmail:usernameInput andPassword:passwordInput];
            if (success != nil) {
                NSLog(@"Damn");
                [self.view makeWaffle:[NSString stringWithFormat:@"Login as %@ successful", usernameInput]
                             duration:WAFFLE_LENGTH_SHORT
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_CHECKMARK];
                
                // save the username and password in prefs
                NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
                [prefs setObject:usernameInput forKey:[StringGrabber grabString:@"key_username"]];
                [prefs setObject:passwordInput forKey:[StringGrabber grabString:@"key_password"]];
                [prefs synchronize];
            
                userName = usernameInput;
                passWord = passwordInput;
                saver->hasLogin = TRUE;
                saver->isLoggedIn = true;
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



- (void) launchFieldMatchingViewControllerFromBrowse:(bool)fromBrowse {
    // get the fields to field match
    UIAlertView *message = [self getDispatchDialogWithMessage:@"Loading fields..."];
    [message show];
    
    dispatch_queue_t queue = dispatch_queue_create("loading_project_fields", NULL);
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
        
    }
    // else user canceled
}



@end
