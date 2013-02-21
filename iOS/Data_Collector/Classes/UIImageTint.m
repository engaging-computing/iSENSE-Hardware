//
//  UIImageTint.m
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/7/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "UIImageTint.h"

@implementation UIImage (Tint)

- (UIImage *)tintedImageUsingColor:(UIColor *)tintColor {
    UIGraphicsBeginImageContext(self.size);
    CGRect drawRect = CGRectMake(0, 0, self.size.width, self.size.height);
    [self drawInRect:drawRect];
    [tintColor set];
    UIRectFillUsingBlendMode(drawRect, kCGBlendModeSourceAtop);
    UIImage *tintedImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return tintedImage;
}

@end