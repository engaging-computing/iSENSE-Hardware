//
//  PWViewController.m
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 1/9/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import "PWViewController.h"



@interface PWViewController ()

@end



@implementation PWViewController

@synthesize menuButton;

- (void)viewDidLoad
{
    [super viewDidLoad];
	
    [[UINavigationBar appearance] setBackgroundImage:[[UIImage alloc] init] forBarMetrics:UIBarMetricsDefault];
    [[UINavigationBar appearance] setBackgroundColor:UIColorFromHex(0x111155)];
    UIImageView *titleView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"navBar.png"]];
    [[UINavigationBar appearance] setTitleView:titleView];
    self.navigationItem.titleView = titleView;
    UIButton* btton = [UIButton buttonWithType:UIButtonTypeCustom];
    [btton setFrame:CGRectMake(0, 0, 30, 30)];
    [btton addTarget:self action:@selector(callMenu) forControlEvents:UIControlEventTouchUpInside];
    [btton setImage:[UIImage imageNamed:@"menuIcon"] forState:UIControlStateNormal];
    menuButton = [[UIBarButtonItem alloc] initWithCustomView:btton];
    [menuButton setTintColor:UIColorFromHex(0x111155)];
    self.navigationItem.rightBarButtonItem = menuButton;

}

- (void) viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
}

- (void) callMenu {
    
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
