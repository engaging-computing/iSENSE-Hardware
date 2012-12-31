//
//  UIPicButton.m
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 10/30/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "UIPicButton.h"



@implementation UIPicButton

@synthesize clickEnabled;

- (id)initWithFrame:(CGRect)frame {
    
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code.
		clickEnabled = TRUE;
    }
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
	clickEnabled = TRUE;
}

- (void)dealloc {
    [super dealloc];
}


@end
