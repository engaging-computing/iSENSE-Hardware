//
//  Constants.h
//  iOS Data Collector
//
//  Created by Mike Stowell and Jeremy Poulin on 2/28/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#ifndef Constants_h
#define Constants_h

// constants for dialogs
#define MENU_EXPERIMENT               0
#define MENU_LOGIN                    1
#define EXPERIMENT_MANUAL_ENTRY       2
#define CLEAR_FIELDS_DIALOG           3
#define MENU_UPLOAD                   4
#define DESCRIPTION_AUTOMATIC         5

// options for action sheet
#define OPTION_CANCELED                0
#define OPTION_ENTER_EXPERIMENT_NUMBER 1
#define OPTION_BROWSE_EXPERIMENTS      2
#define OPTION_SCAN_QR_CODE            3

// types of text field data
#define TYPE_DEFAULT   0
#define TYPE_LATITUDE  1
#define TYPE_LONGITUDE 2
#define TYPE_TIME      3

// ipad and iphone dimensions
#define IPAD_WIDTH_PORTRAIT     725
#define IPAD_WIDTH_LANDSCAPE    980
#define IPHONE_WIDTH_PORTRAIT   280
#define IPHONE_WIDTH_LANDSCAPE  415

// nav controller height
#define NAVIGATION_CONTROLLER_HEIGHT 64

// data recording constants
#define S_INTERVAL      125
#define TEST_LENGTH     600
#define MAX_DATA_POINTS (1000/S_INTERVAL) * TEST_LENGTH

// constants for QueueUploaderView's actionSheet
#define QUEUE_DELETE        0
#define QUEUE_RENAME        1
#define QUEUE_SELECT_EXP    2
#define QUEUE_LOGIN         500

// other character restriction text field tags
#define TAG_QUEUE_RENAME    700
#define TAG_QUEUE_DESC      701
#define TAG_QUEUE_EXP       702
#define TAG_STEPONE_EXP     703
#define TAG_AUTO_LOGIN      704
#define TAG_MANUAL_LOGIN    705
#define TAG_MANUAL_EXP      706

#define DEV_VIS_URL @"http://isensedev.cs.uml.edu/highvis.php?sessions="
#define PROD_VIS_URL @"http://isenseproject.org/highvis.php?sessions="

#define DEV_DEFAULT_EXP 596
#define PROD_DEFAULT_EXP 409

#define FIRST_NAME_FIELD 9001
#define LAST_NAME_FIELD 9002
#define LOGIN_USER 9003
#define LOGIN_PASS 9004

#define FIRST_TIME_NAME 9005
#define ENTER_NAME 9006

#define ACCEPTABLE_CHARACTERS @"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz -_.,01234567879()@"


#endif
