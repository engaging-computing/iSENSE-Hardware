//
//  ViewController.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/8/13.
//  Copyright (c) 2013 ECG. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController <UIActionSheetDelegate> {



}

@property(nonatomic) IBOutlet UILabel *vector_status;
@property(nonatomic) IBOutlet UILabel *login_status;
@property(nonatomic) IBOutlet UIButton *start;
@property(nonatomic) IBOutlet UIBarButtonItem *menuButton;

- (void)longPress:(UILongPressGestureRecognizer*)gesture;
- (IBAction)showMenu:(id)sender;
- (void) actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex;

@end


