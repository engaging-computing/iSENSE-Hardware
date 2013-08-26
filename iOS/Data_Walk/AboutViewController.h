//
//  AboutViewController.h
//  Data_Walk
//
//  Created by Michael Stowell on 8/26/13.
//  Copyright (c) 2013 iSENSE. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AboutViewController : UIViewController {
	UITextView *aboutText;
}

@property (nonatomic, retain) IBOutlet UITextView *aboutText;

@end
