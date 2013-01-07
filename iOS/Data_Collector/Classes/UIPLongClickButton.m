//
//  UIPicButton.m
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 10/30/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "UILongClickButton.h"



@implementation UILongClickButton

@synthesize clickEnabled;

- (id)initWithFrame:(CGRect)frame {
    
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code.
		clickEnabled = TRUE;
        button = nil;
        
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame withImageView:(UIImageView *)buttonImage {
    id i = [self initWithFrame:frame];
    button = buttonImage;
    
    return i;
}



/*
 // Only override drawRect: if you perform custom drawing.
 // An empty implementation adversely affects performance during animation.
 - (void)drawRect:(CGRect)rect {
 // Drawing code.
 }
 */

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    NSLog(@"Button press detected");
	clickEnabled = TRUE;
    
    // Darken the button
    if (button != nil) {
        NSLog(@"Updating Image");
        button.image = [button.image tintedImageUsingColor:[UIColor colorWithWhite:0.0 alpha:0.3]];
    }
}

- (void)dealloc {
    [super dealloc];
}


@end
