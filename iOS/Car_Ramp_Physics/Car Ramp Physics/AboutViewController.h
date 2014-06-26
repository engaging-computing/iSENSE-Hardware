//
//  AboutViewController.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/9/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin

#import <UIKit/UIKit.h>
#import "StringGrabber.h"

@interface AboutViewController : UIViewController


@property(nonatomic) IBOutlet UITextView *text;
@property(nonatomic) NSString *textString;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil andStringText:(NSString *) key;

@end
