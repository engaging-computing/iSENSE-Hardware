//
//  ViewController.m
//  AccelTest
//
//  Created by Jeremy Poulin on 6/19/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

@synthesize motionManager;

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    motionManager = [[CMMotionManager alloc] init];
    if (motionManager.accelerometerAvailable) {
        [motionManager startAccelerometerUpdates];
    } else {
        NSLog(@"Accelerometer is not available right now!");
    }
    
    [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(printXValue) userInfo:nil repeats:YES];
    
}

- (void) printXValue {
    NSLog(@"Current x value: %f", [motionManager.accelerometerData acceleration].x);

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
