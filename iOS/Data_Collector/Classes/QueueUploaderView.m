//
//  QueueUploaderView.m
//  iSENSE_API
//
//  Created by Jeremy Poulin on 6/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "QueueUploaderView.h"

@implementation QueueUploaderView

@synthesize mTableView, currentIndex, dataSaver;

// Initialize the view where the 
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:@"queue_layout~iphone" bundle:nibBundleOrNil];
    if (self) {
    }
    return self;
    
}

// Upload button control
-(IBAction)upload:(id)sender {
    [dataSaver upload];
}

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"queue_layout-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"queue_layout~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"queue_layout-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"queue_layout~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
}

// Do any additional setup after loading the view.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Get dataSaver from the App Delegate
    if (dataSaver == nil) {
        dataSaver = [(Data_CollectorAppDelegate *)[[UIApplication sharedApplication] delegate] dataSaver];
    }
            
    currentIndex = 0;

}

// Dispose of any resources that can be recreated.
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
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

// There is a single column in this table
- (NSInteger *)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

// There are as many rows as there are DataSets
- (NSInteger *)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return dataSaver.count;
}

// Initialize a single object in the table
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *cellIndetifier = @"QueueCellIdentifier";
    QueueCell *cell = (QueueCell *)[tableView dequeueReusableCellWithIdentifier:cellIndetifier];
    if (cell == nil) {
        UIViewController *tmp = [[UIViewController alloc] initWithNibName:@"QueueCell" bundle:nil];
        cell = (QueueCell *) tmp.view;
        
        [tmp release];
    }
    
    DataSet *tmp = [[dataSaver removeDataSet:NO_KEY] retain];
    [cell setupCellName:tmp.name andDataType:@"Data" andDescription:tmp.dataDescription andUploadable:true];
    [dataSaver addDataSet:tmp];
    [tmp release];
    
    return cell;
}




@end
