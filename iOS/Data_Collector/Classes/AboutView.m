//
//  AboutView.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "AboutView.h"

@implementation AboutView

@synthesize aboutText;

- (void)viewDidLoad {
    [super viewDidLoad];
	aboutText.text = [StringGrabber grabString:@"about_text"];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void)viewDidUnload {
    [super viewDidUnload];
}



@end
