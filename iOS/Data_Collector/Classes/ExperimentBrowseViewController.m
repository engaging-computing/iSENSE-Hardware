//
//  ExperimentBrowseViewController.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 1/28/13.
//
//

#import "ExperimentBrowseViewController.h"

@implementation ExperimentBrowseViewController

@synthesize currentPage, currentQuery, scrollHeight, contentHeight, lastExperimentClicked;

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
    UITextField *searchBarTextField = nil;
    for (UIView *subview in searchBar.subviews) {
        if ([subview isKindOfClass:[UITextField class]]) {
            searchBarTextField = (UITextField *)subview;
            break;
        }
    }
    searchBarTextField.enablesReturnKeyAutomatically = NO;
    
    // Prepare spinner for loading at bottom
    UIView *bottomSpinnerBlock = [[UIView alloc] initWithFrame:CGRectMake(0, self.view.bounds.size.height - 180, 320, 180)];
    [self setCenter:bottomSpinnerBlock];
    [bottomSpinnerBlock addSubview:spinner];
    [self.view addSubview:bottomSpinnerBlock];
    
    // Prepare scrollview
    scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 50, 320, self.view.bounds.size.height - 160)];
    scrollHeight = scrollView.bounds.size.height;
    scrollView.delaysContentTouches = NO;
    scrollView.delegate = self;
    [self.view addSubview:scrollView];
    
    // Prepare Frame
    experimentInfo = [[UIView alloc] initWithFrame:CGRectMake(320, 50, 433, self.view.bounds.size.height - 100)];
    experimentInfo.backgroundColor = [UIColor clearColor];
    experimentInfo.layer.borderWidth = 3;
    experimentInfo.layer.borderColor = [[UIColor whiteColor] CGColor];
    experimentInfo.hidden = YES;
    
    // Prepare rapi
    isenseAPI = [iSENSE getInstance];
    [isenseAPI toggleUseDev:YES];

    
    // Start spinner
    [spinner startAnimating];
    
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
    
    NSString *query = [[NSString alloc] initWithString:searchBar.text];
    
    if ([loadExperimentThread isExecuting])
        [loadExperimentThread cancel];
    
    loadExperimentThread = [[NSThread alloc] initWithTarget:self selector:@selector(updateScrollView:) object:[[ISenseSearch alloc] initWithQuery:query searchType:RECENT page:1 andBuildType:NEW]];
    
    [loadExperimentThread start];

    // Dismiss keyboard.
    [searchBar resignFirstResponder];
}

- (void)searchBarCancelButtonClicked:(UISearchBar *) searchBar {
    NSLog(@"User canceled search");
    // Dismiss keyboard.
    [searchBar resignFirstResponder];
}

- (IBAction)onExperimentButtonClicked:(id)caller {
    [caller switchToDarkImage:TRUE];
    if (lastExperimentClicked && !(caller == lastExperimentClicked)) [lastExperimentClicked switchToDarkImage:FALSE];
    
    lastExperimentClicked = caller;
    
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        // Use extra space for Experiment Data
        experimentInfo.hidden = NO;
        [[experimentInfo subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        
        NSMutableArray *imageArray = [isenseAPI getExperimentImages:lastExperimentClicked->experimentNumber];
        NSLog(@"Image count:%d", imageArray.count);
        if (imageArray.count) {
            Image *firstImage = imageArray[0];
            NSURL *url = [NSURL URLWithString:firstImage.provider_url];
            NSData *data = [NSData dataWithContentsOfURL:url];
            UIImage *image = [UIImage imageWithData:data];
            UIImageView *imageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 433, 433)];
            imageView.image = image;
            imageView.contentMode = UIViewContentModeScaleAspectFit;
            [experimentInfo addSubview:imageView];
        }
        
        [self.view addSubview:experimentInfo];
    }
    
}

// Sets our spinner to the middle of the bottom block.
- (void)setCenter:(UIView *)view {
    spinner = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    [view addSubview:spinner];
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
    if (iSS.buildType == NEW) {
        [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        contentHeight = 0;
    }
    
    NSMutableArray *experiments = [isenseAPI getExperiments:[NSNumber numberWithInt:iSS.page] withLimit:[NSNumber numberWithInt:10] withQuery:iSS.query andSort:[iSS searchTypeToString]];
    currentPage = iSS.page;
    currentQuery = iSS.query;
    int maxHeight = 0;
    
    if (iSS.buildType == APPEND) {
        maxHeight = contentHeight;
        NSLog(@"CONTENT HEIGHT IS %d", contentHeight);
    }
    
    for(Experiment *exp in experiments) {
        NSLog(@"Made a new block at %d", maxHeight);
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
    
    contentHeight = maxHeight;
    
    NSLog(@"ScrollView Content size = %d. ScrollView size = %d", contentHeight, scrollHeight);  
    if (contentHeight <= scrollHeight && experiments.count == 10) {
        iSS.page++;
        iSS.buildType = APPEND;
        [self updateScrollView:iSS];
    }
    
    [spinner stopAnimating];
}

// Check if scrollview has reached bottom
- (void)scrollViewDidScroll:(UIScrollView *)scroller{
    if (scroller.contentOffset.y == scroller.contentSize.height - scroller.frame.size.height) {
        [spinner startAnimating];
        
        if ([loadExperimentThread isExecuting])
            [loadExperimentThread cancel];
        
        loadExperimentThread = [[NSThread alloc] initWithTarget:self selector:@selector(updateScrollView:) object:[[ISenseSearch alloc] initWithQuery:currentQuery searchType:RECENT page:(currentPage + 1) andBuildType:APPEND]];
        
        [loadExperimentThread start];
        
    }
}

@end
