//
//  UIPicButton.h
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 10/30/12.
//  Copyright 2012 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>



@interface UIPicButton : UIView {
	BOOL clickEnabled;
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event;

@property BOOL clickEnabled;

@end
