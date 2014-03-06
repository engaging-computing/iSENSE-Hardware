//
//  CredentialManager.h
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 2/28/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>

@interface CredentialManager : UIViewController {
    IBOutlet UIView *temp;
}

@property (strong, nonatomic) IBOutlet UIView *userDisplayView;
@property (strong, nonatomic) IBOutlet UIImageView *gravatarView;
@property (strong, nonatomic) IBOutlet UILabel *nameLabel;
@property (strong, nonatomic) IBOutlet UIButton *loginoutButton;
@property (strong, nonatomic) IBOutlet UIButton *manageContributorKeysButton;

@end
