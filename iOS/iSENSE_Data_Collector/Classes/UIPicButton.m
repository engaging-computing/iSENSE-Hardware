//
//  UIPicButton.m
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 10/30/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "UIPicButton.h"


@implementation UIPicButton


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
	NSLog(@"Begin ClickEnabled = %s", clickEnabled ? "true" : "false");
}

- (void) setClickEnabled:(BOOL)enableClick {
	clickEnabled = enableClick;
}

- (BOOL)getClickEnabled {
	return clickEnabled;
}

- (void)dealloc {
    [super dealloc];
}


@end
