//
//  Constants.h
//  Data_Walk
//
//  Created by Michael Stowell on 8/22/13.
//  Copyright (c) 2013 iSENSE. All rights reserved.
//

#ifndef Data_Walk_Constants_h
#define Data_Walk_Constants_h

// iSENSE constants
#define kUSE_DEV            true
#define kDEFAULT_PROJECT    11
#define kDEFAULT_USER       @"mobile"
#define kDEFAULT_PASS       @"mobile"
#define kDEFAULT_NAME       @"Mobile U."
#define kBASE_VIS_URL       @"http://beta.isenseproject.org/projects/"

// other recording constants
#define kDEFAULT_REC_INTERVAL   10

// tags for UI items
#define kTAG_LABEL_LATITUDE         500
#define kTAG_LABEL_LONGITUDE        501
#define kTAG_BUTTON_RECORD          502
#define kTAG_BUTTON_INTERVAL        503
#define kTAG_TEXTFIELD_NAME         504
#define kTAG_BUTTON_LOGGED_IN       505
#define kTAG_BUTTON_UPLOAD          506
#define kTAG_BUTTON_PROJECT         507

// tags for alert view/text fields in the alert view
#define kTAG_PROJECT_SELECTION      600
#define kENTER_PROJ_TEXTFIELD       601
#define kENTER_USER_TEXTFIELD       602
#define kENTER_PASS_TEXTFIELD       603
#define kTAG_LOGIN_DIALOG           604
#define kTAG_RESET_ARE_YOU_SURE     605

// options for project action sheet
#define kOPTION_CANCELED            0
#define kOPTION_ENTER_PROJECT       1
#define kOPTION_BROWSE_PROJECTS     2
#define kOPTION_SCAN_PROJECT_QR     3

// hex color
#define UIColorFromHex(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

#endif
