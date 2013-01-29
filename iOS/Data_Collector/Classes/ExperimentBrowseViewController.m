//
//  ExperimentBrowseViewController.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 1/28/13.
//
//

#import "ExperimentBrowseViewController.h"

@interface ExperimentBrowseViewController ()

@end

@implementation ExperimentBrowseViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        // Bound, allocate, and customize the main view
        self.view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 768, 1024)];
        self.view.backgroundColor = [UIColor blackColor];
    } else {
        // Bound, allocate, and customize the main view
        self.view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 480)];
        self.view.backgroundColor = [UIColor blackColor];
    }
    
    //iSENSE *isenseAPI = [iSENSE getInstance];
    ExperimentBlock *block = [[ExperimentBlock alloc] initWithFrame:CGRectMake(0, 0, 150, 50) experimentName:@"Experiment Name" experimentNumber:132];
    
    [self.view addSubview:block];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
