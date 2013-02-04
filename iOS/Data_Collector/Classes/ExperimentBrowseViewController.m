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
    
    UISearchBar *searchBar = [[UISearchBar alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, 40)];
    searchBar.delegate = self;
    [self.view addSubview:searchBar];
    
    scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 50, 320, self.view.bounds.size.height)];
    scrollView.delaysContentTouches = NO;
    [self.view addSubview:scrollView];
                                                             
    
    isenseAPI = [iSENSE getInstance];

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


/* Search bar methods */
- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar {
    [self handleSearch:searchBar];
}

- (void)searchBarTextDidEndEditing:(UISearchBar *)searchBar {
    [self handleSearch:searchBar];
}

- (void)handleSearch:(UISearchBar *)searchBar {
    NSLog(@"User searched for %@", searchBar.text);
    [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    NSMutableArray *experiments = [isenseAPI getExperiments:[NSNumber numberWithInt:1] withLimit:[NSNumber numberWithInt:25] withQuery:searchBar.text andSort:@"Recent"];
    int i = 0, maxHeight = 0;
    for(Experiment *exp in experiments) {
        
        ExperimentBlock *block = [[ExperimentBlock alloc] initWithFrame:CGRectMake(0, i*60, 310, 50)
                                                        experimentName:exp.name
                                                        experimentNumber:[exp.experiment_id integerValue]
                                                        target:self
                                                        action:@selector(onExperimentButtonClicked:)];
        [scrollView addSubview:block];
        i++;
        maxHeight += 60;
    }
    
    CGSize scrollableSize = CGSizeMake(320, maxHeight);
    [scrollView setContentSize:scrollableSize];

    [searchBar resignFirstResponder]; // if you want the keyboard to go away
}

- (void)searchBarCancelButtonClicked:(UISearchBar *) searchBar {
    NSLog(@"User canceled search");
    [searchBar resignFirstResponder]; // if you want the keyboard to go away
}

- (IBAction)onExperimentButtonClicked:(id)caller {
    [caller switchToDarkImage:false];
}

@end
