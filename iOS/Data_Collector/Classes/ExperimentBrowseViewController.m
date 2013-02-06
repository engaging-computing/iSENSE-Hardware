//
//  ExperimentBrowseViewController.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 1/28/13.
//
//

#import "ExperimentBrowseViewController.h"

@implementation ExperimentBrowseViewController

@synthesize currentPage;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
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
    
    // Prepare search bar
    UISearchBar *searchBar = [[UISearchBar alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, 40)];
    searchBar.delegate = self;
    [self.view addSubview:searchBar];
    
    // Prepare spinner for loading at bottom
    UIView *bottomSpinnerBlock = [[UIView alloc] initWithFrame:CGRectMake(0, self.view.bounds.size.height - 150, self.view.bounds.size.width, 150)];
    [self setCenter:bottomSpinnerBlock];
    [bottomSpinnerBlock addSubview:spinner];
    [self.view addSubview:bottomSpinnerBlock];
    
    // Prepare scrollview
    scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 50, 320, self.view.bounds.size.height - 150)];
    scrollView.delaysContentTouches = NO;
    [self.view addSubview:scrollView];
    
    // Prepare rapi
    isenseAPI = [iSENSE getInstance];
    
    // Load the first 10 experiments.
   loadExperimentThread = [[NSThread alloc] initWithTarget:self selector:@selector(updateScrollView:) object:[[ISenseSearch alloc] init]];
    [loadExperimentThread start];
        
}

- (void)viewDidLoad {
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


/* Search bar methods */
- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar {
    [self handleSearch:searchBar];
}

- (void)searchBarTextDidEndEditing:(UISearchBar *)searchBar {
    //[self handleSearch:searchBar];
}

- (void)handleSearch:(UISearchBar *)searchBar {
    NSLog(@"User searched for %@", searchBar.text);
    
    if ([loadExperimentThread isExecuting])
        [loadExperimentThread cancel];
    
    loadExperimentThread = [[NSThread alloc] initWithTarget:self selector:@selector(updateScrollView:) object:[[ISenseSearch alloc] initWithQuery:searchBar.text searchType:RECENT page:1 andBuildType:APPEND]];
    
    [loadExperimentThread start];

        
    [searchBar resignFirstResponder]; // if you want the keyboard to go away
}

- (void)searchBarCancelButtonClicked:(UISearchBar *) searchBar {
    NSLog(@"User canceled search");
    [searchBar resignFirstResponder]; // if you want the keyboard to go away
}

- (IBAction)onExperimentButtonClicked:(id)caller {
    [caller switchToDarkImage:false];
}

// Sets our spinner to the middle of the bottom block.
- (void)setCenter:(UIView *)view {
    spinner = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
    [view addSubview:spinner];
 //   [spinner startAnimating];
    CGSize boundsSize = view.bounds.size;
    CGRect frameToCenter = spinner.frame;
    // center horizontally
    if (frameToCenter.size.width < boundsSize.width)
        frameToCenter.origin.x = (boundsSize.width - frameToCenter.size.width) / 2;
    else
        frameToCenter.origin.x = 0;
    
    // center vertically
    if (frameToCenter.size.height < boundsSize.height)
        frameToCenter.origin.y = (boundsSize.height - frameToCenter.size.height) / 2;
    else
        frameToCenter.origin.y = 0;
    
    spinner.frame = frameToCenter;
    NSLog(@"The spinner is currently at %f, %f.", spinner.center.x, spinner.center.y);
}

// Update scrollView by appending or making a new search on a separate thread.
- (void) updateScrollView:(ISenseSearch *)iSS {
    [spinner startAnimating];
    
    if (iSS.buildType == NEW)
        [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    NSMutableArray *experiments = [isenseAPI getExperiments:[NSNumber numberWithInt:iSS.page] withLimit:[NSNumber numberWithInt:10] withQuery:iSS.query andSort:[iSS searchTypeToString]];
    int maxHeight = 0;
    
    if (iSS.buildType == APPEND)
        maxHeight = scrollView.contentSize.height;
    
    for(Experiment *exp in experiments) {
        ExperimentBlock *block = [[ExperimentBlock alloc] initWithFrame:CGRectMake(0, maxHeight, 310, 50)
                                                         experimentName:exp.name
                                                       experimentNumber:[exp.experiment_id integerValue]
                                                                 target:self
                                                                 action:@selector(onExperimentButtonClicked:)];
        [scrollView addSubview:block];
         maxHeight += 60;
    }
    
    CGSize scrollableSize = CGSizeMake(320, maxHeight);
    [scrollView setContentSize:scrollableSize];

    [spinner stopAnimating];
}

@end
