//
//  AboutViewController.m
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/9/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
#import "AboutViewController.h"

@interface AboutViewController ()

@end

@implementation AboutViewController

@synthesize text;

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
    [text setText:@"Enter your first name and last initial so that you can later identify your session on iSENSE.\n\nPress and hold the Start button, and the app will record 10 seconds of acceleration data. After recording, you may publish your data to isenseproject.org. \n\nYou are also given an option to view your data. \n\nYou may modify various settings -- explore the menus.  \n\nIf setting up your own experiment: create fields named \"Time\" (time/milliseconds) and any of \"X\", \"Y\", \"Z\", and \"Accel-Magnitude\" (all Acceleration/meters per second squared)." ];
    
    self.navigationItem.title = @"About";
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
