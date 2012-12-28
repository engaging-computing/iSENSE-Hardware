//
//  GuideView.h
//  Splash
//
//  Created by CS Admin on 12/28/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface GuideView : UIViewController {
	UITextView *guideText;
}

- (NSString *) getString:(NSString *)label;

@property (nonatomic, retain) IBOutlet UITextView *guideText;

@end
