//
//  FieldMatchingViewController.m
//  iSENSE_API
//
//  Created by Michael Stowell on 11/14/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "FieldMatchingViewController.h"

@interface FieldMatchingViewController ()

@end

@implementation FieldMatchingViewController

@synthesize userFields;

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [isenseBundle loadNibNamed:@"FieldMatching-landscape~ipad"
                                 owner:self
                               options:nil];
            [self viewDidLoad];
        } else {
            [isenseBundle loadNibNamed:@"FieldMatching~ipad"
                                 owner:self
                               options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [isenseBundle loadNibNamed:@"FieldMatching-landscape~iphone"
                                 owner:self
                               options:nil];
            [self viewDidLoad];
        } else {
            [isenseBundle loadNibNamed:@"FieldMatching~iphone"
                                 owner:self
                               options:nil];
            [self viewDidLoad];
        }
    }
}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

// iOS6 enable rotation
- (BOOL)shouldAutorotate {
    return YES;
}

// iOS6 enable rotation
- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

-(void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:YES];
    
    // Autorotate
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
    }
    return self;
}

- (id) initWithUserFields:(NSMutableArray *)uf {
    self = [super init];
    if (self) {
        userFields = uf;
        isenseBundle = [NSBundle bundleWithURL:[[NSBundle mainBundle] URLForResource:@"iSENSE_API_Bundle" withExtension:@"bundle"]];
    }
    return self;

}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction) backOnClick:(id)sender {
    
}

- (IBAction) okOnClick:(id)sender {
    
}

@end
