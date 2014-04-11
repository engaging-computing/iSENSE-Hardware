//
//  CredentialManager.h
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 2/28/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#import <API.h>
#import <StringGrabber.h>
#import <Waffle.h>
#import <DLAVAlertView.h>

@class CredentialManager;
@protocol CredentialManagerDelegate <NSObject>

@required
- (void) didPressLogin:(CredentialManager *)mngr;

@end

@interface CredentialManager : UIViewController <UIAlertViewDelegate, UITextFieldDelegate>{
}

@property (strong, nonatomic) IBOutlet UIImageView *gravatarView;
@property (strong, nonatomic) IBOutlet UILabel *nameLabel;
@property (strong, nonatomic) IBOutlet UIButton *loginoutButton;
@property(nonatomic) UIAlertView *loginalert;
@property (strong, nonatomic) API *api;
@property (nonatomic, weak) id <CredentialManagerDelegate> delegate;


- (IBAction)loginLogout:(id)sender;
- (CredentialManager *) initWithDelegate:(__weak id<CredentialManagerDelegate>) delegateObject;

@end
