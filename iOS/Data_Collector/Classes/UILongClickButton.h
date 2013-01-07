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
	BOOL clickEnabled;
    UIImageView *button;
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event;
- (id)initWithFrame:(CGRect)frame withImageView:(UIImageView *)buttonImage;

@property (nonatomic) BOOL clickEnabled;

@end
