//
//  GuideView.m
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "GuideView.h"


@implementation GuideView

@synthesize guideText;


- (void)viewDidLoad {
    [super viewDidLoad];
	guideText.text = [StringGrabber grabString:@"guide_text"];
	
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void)viewDidUnload {
    [super viewDidUnload];
}


- (void)dealloc {
    [super dealloc];
}

@end