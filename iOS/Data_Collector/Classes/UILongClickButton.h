//
//  UIPicButton.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 10/30/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIImageTint.h"



@interface UILongClickButton : UIView {
    UIImageView *image;
    UIImageView *originalImageCopy;
    UILongPressGestureRecognizer *recognizer;
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event;
- (id)initWithFrame:(CGRect)frame imageView:(UIImageView *)buttonImage target:(id)target action:(SEL)selector;
- (void)updateImage:(UIImageView *)imageView;

@end
