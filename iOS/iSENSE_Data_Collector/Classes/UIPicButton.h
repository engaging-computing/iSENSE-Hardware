//
//  UIPicButton.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 10/30/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface UIPicButton : UIView {
	BOOL clickEnabled;
}

- (BOOL) getClickEnabled;
- (void) setClickEnabled:(BOOL)enableClick;
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event;

@end
