//
//  UILongClickButton.m
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 10/30/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "UILongClickButton.h"


@implementation UILongClickButton

- (id)initWithFrame:(CGRect)frame {
    
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code.
        image = nil;
        originalImageCopy = nil;
        recognizer = nil;
        
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame imageView:(UIImageView *)buttonImage target:(id)target action:(SEL)selector {
    self = [self initWithFrame:frame];
    image = buttonImage;
    originalImageCopy = [[UIImageView alloc] initWithImage:image.image];
    
	recognizer = [[UILongPressGestureRecognizer alloc] initWithTarget:target action:selector];
	recognizer.MinimumPressDuration = 0.5;
    recognizer.numberOfTouchesRequired = 1;
    recognizer.allowableMovement = 50;
    
    [recognizer cancelsTouchesInView];
    [self addGestureRecognizer:recognizer];
    
    return self;
}



/*
 // Only override drawRect: if you perform custom drawing.
 // An empty implementation adversely affects performance during animation.
 - (void)drawRect:(CGRect)rect {
 // Drawing code.
 }
 */


- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
   
    // Darken the button
    if (image != nil) {
        image.image = [image.image tintedImageUsingColor:[UIColor colorWithWhite:0.0 alpha:0.3]];
    }
    
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    
    // Reset the button
    if (image != nil) {
        image.image = originalImageCopy.image;
    }
    
    // Enabled the listener
    if (recognizer != nil) {
        recognizer.enabled = YES;
    }
}

- (void)updateImage:(UIImageView *)imageView {
    [originalImageCopy release];
    originalImageCopy = [[UIImageView alloc] initWithImage:imageView.image];
}

- (void)dealloc {
    [recognizer release];
    [originalImageCopy release];
    [super dealloc];
}


@end
