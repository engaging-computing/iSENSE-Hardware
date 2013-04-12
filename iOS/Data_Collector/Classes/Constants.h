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

// manual scrollview drawing constants
#define SCROLLVIEW_Y_OFFSET     50
#define SCROLLVIEW_OBJ_INCR     30
#define SCROLLVIEW_LABEL_HEIGHT 20
#define SCROLLVIEW_TEXT_HEIGHT  35
#define UI_FIELDNAME            0
#define UI_FIELDCONTENTS        1
#define IPAD_WIDTH_PORTRAIT     725
#define IPAD_WIDTH_LANDSCAPE    980
#define IPHONE_WIDTH_PORTRAIT   280
#define IPHONE_WIDTH_LANDSCAPE  415

// manual scrollview oddity patches
#define PORTRAIT_BOTTOM_CUT         40
#define LANDSCAPE_BOTTOM_CUT_IPAD   30
#define LANDSCAPE_BOTTOM_CUT_IPHONE 80
#define TOP_ELEMENT_ADJUSTMENT      30
#define START_Y_PORTRAIT_IPAD       20
#define START_Y_PORTRAIT_IPHONE     50
#define START_Y_LANDSCAPE_IPAD      5
#define START_Y_LANDSCAPE_IPHONE    0

// tags for types of UITextFields
#define TAG_TEXT    100
#define TAG_NUMERIC 101

// Nav Controller Height
#define NAVIGATION_CONTROLLER_HEIGHT 64


#endif
