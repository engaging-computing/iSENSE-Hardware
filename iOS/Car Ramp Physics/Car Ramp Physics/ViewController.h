//
//  ViewController.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/8/13.
//  Copyright (c) 2013 ECG. All rights reserved.
//

// waffle constants
#define WAFFLE_LENGTH_SHORT  2.0
#define WAFFLE_LENGTH_LONG   3.5
#define WAFFLE_BOTTOM @"bottom"
#define WAFFLE_TOP @"top"
#define WAFFLE_CENTER @"center"
#define WAFFLE_CHECKMARK @"waffle_check"
#define WAFFLE_RED_X @"waffle_x"
#define WAFFLE_WARNING @"waffle_warn"

#import "RNGridMenu.h"
#import "Waffle.h"
#import "AboutViewController.h"
#import "CODialog.h"
#import "iSENSE_API/headers/iSENSE.h"
#import <UIKit/UIKit.h>

@interface ViewController : UIViewController <RNGridMenuDelegate, UIActionSheetDelegate, UIAlertViewDelegate> {



}

@property(nonatomic) IBOutlet UILabel *vector_status;
@property(nonatomic) IBOutlet UILabel *login_status;
@property(nonatomic) IBOutlet UIButton *start;
@property(nonatomic) IBOutlet UIBarButtonItem *menuButton;
@property(nonatomic) IBOutlet UINavigationBar *navBar;

@property(nonatomic) int recordLength;
@property(nonatomic) int countdown;
@property(nonatomic, retain) CODialog *change_name;
@property (nonatomic, retain) iSENSE *isenseAPI;

@property(nonatomic, retain) NSArray *items;

- (void)longPress:(UILongPressGestureRecognizer*)gesture;
- (IBAction)showMenu:(id)sender;
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex;
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;
- (void) changeName;

@end


