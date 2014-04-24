//
//  ImageManipulator.h
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 2/28/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreGraphics/CoreGraphics.h>
#import <QuartzCore/QuartzCore.h>

@interface ImageManipulator : NSObject {
}
+ (UIImage *)imageWithRoundedCornersSize:(float)cornerRadius usingImage:(UIImage *)original;
+ (UIImage*) roundCorneredImage: (UIImage*) orig radius:(CGFloat) r;
@end
