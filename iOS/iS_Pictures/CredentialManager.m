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

@synthesize userDisplayView, loginoutButton, nameLabel, gravatarView, manageContributorKeysButton;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    gravatarView.layer.cornerRadius = 51;
    gravatarView.layer.masksToBounds = YES;
    gravatarView.layer.opaque = NO;
    
    [gravatarView setImage:[UIImage imageNamed:@"river_walk_57x57.png"]];
    [temp setFrame:CGRectMake(2, 4, temp.bounds.size.width, temp.bounds.size.height)];
    
    [userDisplayView addSubview:temp];
    [temp addSubview:gravatarView];
    
    self.navigationItem.title = @"Credential Manager";
    
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
