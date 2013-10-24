//
//  HexColor.h
//  iSENSE_API
//
//  Created by Michael Stowell on 10/24/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#ifndef iSENSE_API_HexColor_h
#define iSENSE_API_HexColor_h

// hex color
#define UIColorFromHex(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

#endif
