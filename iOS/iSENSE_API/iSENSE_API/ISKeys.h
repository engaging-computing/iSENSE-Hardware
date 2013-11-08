//
//  ISKeys.h
//  iSENSE_API
//
//  Created by Michael Stowell on 10/24/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#ifndef iSENSE_API_ISKeys_h
#define iSENSE_API_ISKeys_h

// debug symbol
#define IS_DEBUG

// hex color
#define UIColorFromHex(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

// accepted characters
#define kACCEPTED_DIGITS  @"1234567890"
#define kACCEPTED_NUMBERS @"1234567890.-"
#define kACCEPTED_CHARS   @"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz -_.,:01234567879()[]@"

// login fields
#define KEY_USERNAME            @"key_username"
#define KEY_PASSWORD            @"key_password"

// project identifier
#define KEY_PROJECT_ID          @"project_id"


#endif
