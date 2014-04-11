//
//  CredentialManager.m
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 2/28/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import "CredentialManager.h"

@interface CredentialManager ()

@end

@implementation CredentialManager

@synthesize loginoutButton, nameLabel, gravatarView,  api, loginalert, delegate;

- (CredentialManager *)init
{
    
    self = [super init];
    
    if (self) {
        
        self.view.autoresizesSubviews = NO;
        
        api = [API getInstance];
        
        self.view.clipsToBounds = YES;
        
        [self loadUserInfo];
        
        
    }
    
    return self;
    
    
    
    
}

- (CredentialManager *) initWithDelegate:(__weak id<CredentialManagerDelegate>) delegateObject {
    self = [self init];
    self.delegate = delegateObject;
    return self;
    
}

- (void) loadUserInfo {
    if ([api getCurrentUser] == nil) {
        [gravatarView setImage:[UIImage imageNamed:@"default_user.png"]];
        [loginoutButton setTitle:@"Login" forState:UIControlStateNormal];
        [nameLabel setText:@"Not Logged In"];
    } else {
        NSURL *imageURL = [NSURL URLWithString:[[api getCurrentUser] gravatar]];
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
            NSData *imageData = [NSData dataWithContentsOfURL:imageURL];
            
            dispatch_async(dispatch_get_main_queue(), ^{
                // Update the UI
                [gravatarView setImage:[UIImage imageWithData:imageData]];
            });
        });
        [loginoutButton setTitle:@"Logout" forState:UIControlStateNormal];
        [nameLabel setText:[[[api getCurrentUser] name] stringByAppendingString:@"."]];
    }

}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    NSString *title = [alertView buttonTitleAtIndex:buttonIndex];
    
    if ([alertView.title isEqualToString:@"Login to iSENSE"] && [title isEqualToString:@"OK"]) {
        
        NSString *email = [alertView textFieldAtIndex:0].text;
        NSString *pass = [alertView textFieldAtIndex:1].text;
        [self login:email withPassword:pass];
    }
}


- (IBAction)loginLogout:(id)sender {
    NSLog(@"Got button press");
    if ([api getCurrentUser] == nil) {
        NSLog(@"Login dialog");
        [self.delegate didPressLogin:self];

    } else {
        [api deleteSession];
        NSLog(@"Deleted session");
        [self loadUserInfo];
    }
    
}

// Log into iSENSE
- (void) login:(NSString *)usernameInput withPassword:(NSString *)passwordInput {
    
    // __block BOOL success;
    // __block RPerson *curUser;
    
    UIAlertView *spinnerDialog = [self getDispatchDialogWithMessage:@"Logging in..."];
    [spinnerDialog show];
    
    dispatch_queue_t queue = dispatch_queue_create("dispatch_queue_t_dialog", NULL);
    dispatch_async(queue, ^{
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            if ([api createSessionWithEmail:usernameInput andPassword:passwordInput] != nil) {
                [self.view makeWaffle:[NSString stringWithFormat:@"Login as %@ successful", usernameInput]
                             duration:WAFFLE_LENGTH_SHORT
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_CHECKMARK];
                
                NSLog(@"Login as %@ successful", usernameInput);
                
                // save the username and password in prefs
                NSUserDefaults * prefs = [NSUserDefaults standardUserDefaults];
                [prefs setObject:usernameInput forKey:[StringGrabber grabString:@"key_username"]];
                [prefs setObject:passwordInput forKey:[StringGrabber grabString:@"key_password"]];
                [prefs synchronize];
                [self loadUserInfo];
                
            } else {
                [self.view makeWaffle:@"Login failed"
                             duration:WAFFLE_LENGTH_SHORT
                             position:WAFFLE_BOTTOM
                                image:WAFFLE_RED_X];
            }
            [spinnerDialog dismissWithClickedButtonIndex:0 animated:YES];
            
        });
        
    });
    
}

// Default dispatch_async dialog with custom spinner
- (UIAlertView *) getDispatchDialogWithMessage:(NSString *)dString {
    UIAlertView *message = [[UIAlertView alloc] initWithTitle:dString
                                                      message:nil
                                                     delegate:self
                                            cancelButtonTitle:nil
                                            otherButtonTitles:nil];
    UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    spinner.center = CGPointMake(139.5, 75.5);
    [message addSubview:spinner];
    [spinner startAnimating];
    return message;
}



@end
