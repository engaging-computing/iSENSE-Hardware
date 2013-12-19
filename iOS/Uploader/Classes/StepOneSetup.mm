//
//  StepOneSetup.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 06/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "StepOneSetup.h"

@implementation StepOneSetup

@synthesize sessionName, sampleInterval, testLength, projNumLabel, rememberMe, selectProj, selectLater, ok;

-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"StepOneSetup-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"StepOneSetup~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"StepOneSetup-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"StepOneSetup~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        displaySensorSelectFromBrowse = false;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    api = [API getInstance];
    
    sessionName.delegate = self;
    sessionName.tag = TAG_STEP1_DATA_SET_NAME;
    
    sampleInterval.delegate = self;
    sampleInterval.keyboardType = UIKeyboardTypeNumberPad;
    sampleInterval.tag = TAG_STEP1_SAMPLE_INTERVAL;
    UIToolbar* SITool = [[UIToolbar alloc]initWithFrame:CGRectMake(0, 0, 320, 40)];
    SITool.barStyle = UIBarStyleBlackTranslucent;
    SITool.items = [NSArray arrayWithObjects:
                    [[UIBarButtonItem alloc]initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil],
                           [[UIBarButtonItem alloc]initWithTitle:@"Done" style:UIBarButtonItemStyleDone target:self action:@selector(doneEditingSampleInterval)],
                           nil];
    [SITool sizeToFit];
    sampleInterval.inputAccessoryView = SITool;
    
    testLength.delegate = self;
    testLength.keyboardType = UIKeyboardTypeNumberPad;
    testLength.tag = TAG_STEP1_TEST_LENGTH;
    UIToolbar* TLTool = [[UIToolbar alloc]initWithFrame:CGRectMake(0, 0, 320, 40)];
    TLTool.barStyle = UIBarStyleBlackTranslucent;
    TLTool.items = [NSArray arrayWithObjects:
                    [[UIBarButtonItem alloc]initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil],
                    [[UIBarButtonItem alloc]initWithTitle:@"Done" style:UIBarButtonItemStyleDone target:self action:@selector(doneEditingTestLength)],
                    nil];
    [TLTool sizeToFit];
    testLength.inputAccessoryView = TLTool;
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    // Indicate that we're not done setting up yet and that sensors haven't been selected
    [prefs setBool:false forKey:[StringGrabber grabString:@"key_setup_complete"]];
    [prefs setBool:false forKey:@"sensor_done"];
    sensorsSelected = false;
    
    NSString *defaultSesName = [prefs stringForKey:[StringGrabber grabString:@"key_step1_data_set_name"]];
    NSString *newSesName = ([defaultSesName length] == 0) ? @"" : defaultSesName;
    [sessionName setText:newSesName];
    
    bool remem = [prefs boolForKey:[StringGrabber grabString:@"key_remember_me_check"]];
    bool returnWithPrefs = [prefs boolForKey:@"return_with_prefs"];
    if (remem || returnWithPrefs) {
        NSString *defaultSampleInterval = [prefs stringForKey:[StringGrabber grabString:@"key_sample_interval"]];
        NSString *newSampleInterval = ([defaultSampleInterval length] == 0) ? @"" : defaultSampleInterval;
        [sampleInterval setText:newSampleInterval];
        
        NSString *defaultTestLength = [prefs stringForKey:[StringGrabber grabString:@"key_test_length"]];
        NSString *newTestLength = ([defaultTestLength length] == 0) ? @"" : defaultTestLength;
        [testLength setText:newTestLength];
        
        rememberMe.on = true;
    }
    
    if (!remem) {
        rememberMe.on = false;
    }
    
    
    int projID = [prefs integerForKey:kPROJECT_ID_DC];
    if (projID > 0) {
        // use global proj and set it to local
        NSString *newProjLabel = [NSString stringWithFormat:@" (currently %d)", projID];
        [projNumLabel setText:[StringGrabber concatenateHardcodedString:@"current_proj_label" with:newProjLabel]];
        selectLater.on = false;
        [prefs setValue:[NSString stringWithFormat:@"%d", projID] forKey:[StringGrabber grabString:@"key_proj_automatic"]];
    } else if (projID == -1) {
        // global proj was "select a project later" from the Welcome screen
        selectLater.on = true;
        selectProj.enabled = NO;
        selectProj.alpha = 0.5;
    } else {
        // search for local proj
        NSString *defaultProj = [prefs stringForKey:[StringGrabber grabString:@"key_proj_automatic"]];
        if ([defaultProj length] != 0) {
            if ([defaultProj isEqualToString:@"-1"]) {
                selectLater.on = true;
                selectProj.enabled = NO;
                selectProj.alpha = 0.5;
            } else {
                NSString *newProjLabel = [NSString stringWithFormat:@" (currently %@)", defaultProj];
                [projNumLabel setText:[StringGrabber concatenateHardcodedString:@"current_proj_label" with:newProjLabel]];
                selectLater.on = false;
            }
        } else {
            if (defaultProj == NULL) {
                selectLater.on = false;
                selectProj.enabled = YES;
                selectProj.alpha = 1.0;
            } else {
                selectLater.on = true;
                selectProj.enabled = NO;
                selectProj.alpha = 0.5;
            }
        }
    }
    
    returnFields = [[NSMutableArray alloc] init];
    NSString *proj = [prefs stringForKey:[StringGrabber grabString:@"key_proj_automatic"]];
    if ([proj intValue] > 0) {
        NSMutableArray *fields = [self getFieldsFromPrefsForProj:[proj intValue]];
        if (fields != nil && [fields count] != 0) {
            [returnFields addObjectsFromArray:fields];
        }
    }
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (IBAction)okOnClick:(UIButton *)okButton {
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    bool ready = true;
    
    if ([[sessionName text] length] == 0) {
        [self.view makeWaffle:@"Please enter a data set name first"
                    duration:WAFFLE_LENGTH_LONG
                    position:WAFFLE_BOTTOM
                       image:WAFFLE_WARNING];
        ready = false;
    }
    
    int sInt;
    if ([[sampleInterval text] length] == 0) {
        sInt = S_INTERVAL;
    } else {
        sInt = [[sampleInterval text] integerValue];
    }
    
    if (sInt < S_INTERVAL) {
        if (ready == true)
            [self.view makeWaffle:[NSString stringWithFormat:@"Please enter a sample interval >= %d ms", S_INTERVAL]
                        duration:WAFFLE_LENGTH_LONG
                        position:WAFFLE_BOTTOM
                           image:WAFFLE_WARNING];
        ready = false;
    }
    
    int tLen;
    if ([[testLength text] length] == 0) {
        tLen = TEST_LENGTH;
    } else {
        tLen = [[testLength text] integerValue];
    }
    
    if (tLen * (1000/sInt) > MAX_DATA_POINTS) {
        if (ready == true)
            [self.view makeWaffle:[NSString stringWithFormat:@"Please enter a test length <= %d s", MAX_DATA_POINTS/(1000/sInt)]
                        duration:WAFFLE_LENGTH_LONG
                        position:WAFFLE_BOTTOM
                           image:WAFFLE_WARNING];
        ready = false;
    }
    
    if (!selectLater.on) {
        NSString *projID = [prefs stringForKey:[StringGrabber grabString:@"key_proj_automatic"]]; // TODO or check global proj?
        
        if (projID == NULL || [projID isEqualToString:@""] || [projID isEqualToString:@"-1"]) {
            if (ready == true)
                [self.view makeWaffle:@"Please select a project"
                            duration:WAFFLE_LENGTH_LONG
                            position:WAFFLE_BOTTOM
                               image:WAFFLE_WARNING];
            ready = false;
        } else if ([returnFields count] == 0) {
            if (ready == true) {
                [self.view makeWaffle:@"Please select fields for this project"
                             duration:WAFFLE_LENGTH_LONG
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_WARNING];
                
                projNumInteger = [projID intValue];
                [self launchFieldMatchingViewControllerFromBrowse:FALSE];
                
                return;
            }
        }
        
    }
    
    if (ready) {
        [prefs setValue:[sessionName text] forKey:[StringGrabber grabString:@"key_step1_data_set_name"]];
        
        if (selectLater.on)
            [prefs setValue:@"-1" forKey:[StringGrabber grabString:@"key_proj_automatic"]];
        
        if (rememberMe.on)
            [prefs setBool:true forKey:[StringGrabber grabString:@"key_remember_me_check"]];
            
        else
            [prefs setBool:false forKey:[StringGrabber grabString:@"key_remember_me_check"]];
            
        if ([[sampleInterval text] length] == 0)
            [prefs setValue:[NSString stringWithFormat:@"%d", S_INTERVAL] forKey:[StringGrabber grabString:@"key_sample_interval"]];
        else
            [prefs setValue:[sampleInterval text] forKey:[StringGrabber grabString:@"key_sample_interval"]];
        
        if ([[testLength text] length] == 0)
            [prefs setValue:[NSString stringWithFormat:@"%d", TEST_LENGTH] forKey:[StringGrabber grabString:@"key_test_length"]];
        else
            [prefs setValue:[testLength text] forKey:[StringGrabber grabString:@"key_test_length"]];
        
        // Indicate that we're done setting up and return
        [prefs setBool:true forKey:[StringGrabber grabString:@"key_setup_complete"]];
        [self.navigationController popViewControllerAnimated:YES];
    }
    
    
}

- (IBAction)projectOnClick:(UIButton *)projButton {
    UIAlertView *message = [[UIAlertView alloc] initWithTitle:nil
                                         message:nil
                                        delegate:self
                               cancelButtonTitle:@"Cancel"
                               otherButtonTitles:@"Enter Project #", @"Browse", nil];
    message.tag = MENU_PROJECT;
    [message show];
}

- (IBAction)selectLaterToggled:(UISwitch *)switcher {
    if (switcher.on) {
        selectProj.enabled = NO;
        selectProj.alpha = 0.5;
        
        [projNumLabel setText:[StringGrabber grabString:@"current_proj_label"]];
    } else {
        selectProj.enabled = YES;
        selectProj.alpha = 1.0;
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        NSString *curExp = [prefs valueForKey:[StringGrabber grabString:@"key_proj_automatic"]];
        if ([curExp length] != 0 && [curExp integerValue] != -1) {
            NSString *newExpLabel = [NSString stringWithFormat:@" (currently %@)", curExp];
            [projNumLabel setText:[StringGrabber concatenateHardcodedString:@"current_proj_label" with:newExpLabel]];
        }
    }
}

- (IBAction)rememberMeToggled:(UISwitch *)switcher {}

- (void) alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet.tag == MENU_PROJECT){
        
        if (buttonIndex == OPTION_ENTER_PROJECT_NUMBER) {
            
            UIAlertView *message = [[UIAlertView alloc] initWithTitle:@"Enter Experiment #:"
                                                              message:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                                    otherButtonTitles:@"Okay", nil];
            
            message.tag = PROJ_MANUAL;
            [message setAlertViewStyle:UIAlertViewStylePlainTextInput];
            [message textFieldAtIndex:0].keyboardType = UIKeyboardTypeNumberPad;
            [message textFieldAtIndex:0].tag = TAG_STEPONE_PROJ;
            [message textFieldAtIndex:0].delegate = self;
            [message show];
            
        } else if (buttonIndex == OPTION_BROWSE_PROJECTS) {
            
            [self rememberPrefs];
            
            ProjectBrowseViewController *browseView = [[ProjectBrowseViewController alloc] init];
            browseView.title = @"Browse Projects";
            browseView.delegate = self;
            [self.navigationController pushViewController:browseView animated:YES];
            
        }
        
    } else if (actionSheet.tag == PROJ_MANUAL) {
        
        if (buttonIndex != OPTION_CANCELED) {
            
            NSString *projNum = [[actionSheet textFieldAtIndex:0] text];
            projNumInteger = [projNum integerValue];
           
            NSString *newProjLabel = [NSString stringWithFormat:@" (currently %@)", projNum];
            [projNumLabel setText:[StringGrabber concatenateHardcodedString:@"current_proj_label" with:newProjLabel]];
            
            NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
            [prefs setValue:projNum forKey:[StringGrabber grabString:@"key_proj_automatic"]];
            [prefs setInteger:0 forKey:kPROJECT_ID_DC]; // reset the global proj
            
            [self rememberPrefs];
            
            [self launchFieldMatchingViewControllerFromBrowse:FALSE];
        }
        
    } 
}

- (void) launchFieldMatchingViewControllerFromBrowse:(bool) fromBrowse {
    // get the fields to field match
    DataFieldManager *dfm = [[DataFieldManager alloc] initWithProjID:projNumInteger API:api andFields:nil];
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
                //[self performSelector:@selector(presentFieldMatchingViewControllerAfterDelay:) withObject:fmvc afterDelay:1.0];
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

- (void) presentFieldMatchingViewControllerAfterDelay:(id)sender {
    FieldMatchingViewController *fmvc = (FieldMatchingViewController *) sender;
    NSMutableArray *controllers = [NSMutableArray arrayWithArray:self.navigationController.viewControllers];
    [controllers addObject:fmvc];
    [self.navigationController setViewControllers:controllers animated:YES];
    
}

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

- (void) retrieveFieldMatchedArray:(NSNotification *)obj {
    NSMutableArray *fieldMatch =  (NSMutableArray *)[obj object];
    if (fieldMatch != nil) {
        // user pressed okay button
        returnFields = [[NSMutableArray alloc] init];
        [returnFields addObjectsFromArray:fieldMatch];
        [self writeFieldsToPrefsForProj:projNumInteger];
    }
    // else user canceled
}

- (void) writeFieldsToPrefsForProj:(int)projID {
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    NSString *prefKey = [NSString stringWithFormat:@"%@%d", kFIELD_PREF_STRING, projID];
    NSLog(@"WRITING %@", prefKey);
    if (returnFields == nil || [returnFields count] == 0)
        return;
    
    [prefs setObject:returnFields forKey:prefKey];
}

- (NSMutableArray *) getFieldsFromPrefsForProj:(int)projID {
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    NSString *prefKey = [NSString stringWithFormat:@"%@%d", kFIELD_PREF_STRING, projID];
    NSLog(@"READING %@", prefKey);
    NSMutableArray *fields = [prefs objectForKey:prefKey];
    if (fields == nil || [fields count] == 0)
        return nil;
    
    return fields;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    // If true, then we're coming back from another ViewController
    if (self.isMovingToParentViewController == NO) {
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        bool backFromSensors = [prefs boolForKey:@"sensor_done"];
        
        if (backFromSensors) {
            sensorsSelected = true;
            
            // Set the sensor_done key back to false again
            [prefs setBool:false forKey:@"sensor_done"];
        }
    }
    
}


- (BOOL) textFieldShouldReturn:(UITextField *)textField{
    [textField resignFirstResponder];
    return YES;
}

- (void) viewDidAppear:(BOOL)animated {
    
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
    
    if (displaySensorSelectFromBrowse) {
        displaySensorSelectFromBrowse = false;
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        [prefs setValue:[sessionName text] forKey:[StringGrabber grabString:@"key_step1_data_set_name"]];
    }
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
            
        case TAG_STEP1_DATA_SET_NAME:
            if (![self containsAcceptedCharacters:string])
                return NO;
            
            return (newLength > 52) ? NO : YES;
            
        case TAG_STEP1_SAMPLE_INTERVAL:
            if (![self containsAcceptedDigits:string])
                return NO;
            
            return (newLength > 15) ? NO : YES;
            
        case TAG_STEP1_TEST_LENGTH:
            if (![self containsAcceptedDigits:string])
                return NO;

            return (newLength > 10) ? NO : YES;
            
        case TAG_STEPONE_PROJ:
            if (![self containsAcceptedDigits:string])
                return NO;
            
            return (newLength > 6) ? NO : YES;
            
        default:
            return YES;
    }
}

- (void) doneEditingSampleInterval {
    [sampleInterval resignFirstResponder];
}

- (void) doneEditingTestLength {
    [testLength resignFirstResponder];
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

- (void) rememberPrefs {
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    [prefs setValue:[sessionName text] forKey:[StringGrabber grabString:@"key_step1_data_set_name"]];
    
    if (rememberMe.on)
        [prefs setBool:true forKey:[StringGrabber grabString:@"key_remember_me_check"]];
    
    else
        [prefs setBool:false forKey:[StringGrabber grabString:@"key_remember_me_check"]];
    
    if ([[sampleInterval text] length] == 0)
        [prefs setValue:[NSString stringWithFormat:@"%d", S_INTERVAL] forKey:[StringGrabber grabString:@"key_sample_interval"]];
    else
        [prefs setValue:[sampleInterval text] forKey:[StringGrabber grabString:@"key_sample_interval"]];
    
    if ([[testLength text] length] == 0)
        [prefs setValue:[NSString stringWithFormat:@"%d", TEST_LENGTH] forKey:[StringGrabber grabString:@"key_test_length"]];
    else
        [prefs setValue:[testLength text] forKey:[StringGrabber grabString:@"key_test_length"]];
    
    [prefs setBool:true forKey:@"return_with_prefs"];
}

-(void)projectViewController:(ProjectBrowseViewController *)controller didFinishChoosingProject:(NSNumber *)project {
    
    projNumInteger = project.intValue;
    
    if (projNumInteger != 0) {
        NSString *newProjLabel = [NSString stringWithFormat:@" (currently %d)", projNumInteger];
        [projNumLabel setText:[StringGrabber concatenateHardcodedString:@"current_proj_label" with:newProjLabel]];
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        NSString *projNumString = [NSString stringWithFormat:@"%d", projNumInteger];
        [prefs setValue:projNumString forKey:[StringGrabber grabString:@"key_proj_automatic"]];
        [prefs setInteger:0 forKey:kPROJECT_ID_DC]; // reset global proj
        
        displaySensorSelectFromBrowse = true;
        
        [self launchFieldMatchingViewControllerFromBrowse:TRUE];
    }
}

@end
