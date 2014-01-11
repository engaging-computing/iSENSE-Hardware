//
//  PWViewController.h
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 1/9/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <iSENSE_API/API.h>
#import <iSENSE_API/ISKeys.h>

@interface PWViewController : UIViewController

@property(nonatomic) IBOutlet UITextField *groupNameField;
@property(nonatomic) IBOutlet UILabel *projectIDLbl, *picCntLbl, *login_status;
@property(nonatomic) IBOutlet UIButton *takeButton;
@property(nonatomic) IBOutlet UIBarButtonItem *menuButton;

- (void) callMenu;

@end
