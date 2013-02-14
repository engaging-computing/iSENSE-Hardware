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


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
    UIView *mainView;
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        // Bound, allocate, and customize the main view
        mainView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 768, 1024 - 44)];
        mainView.backgroundColor = [UIColor blackColor];
        self.view = mainView;
        [mainView release];
        
        // Prepare ExperimentInfo Frame
        experimentInfo = [[UIView alloc] initWithFrame:CGRectMake(320, 50, 433, self.view.bounds.size.height - 100)];
        experimentInfo.backgroundColor = [UIColor clearColor];
        experimentInfo.layer.borderWidth = 3;
        experimentInfo.layer.borderColor = [[UIColor whiteColor] CGColor];
        experimentInfo.hidden = YES;
        [self.view addSubview:experimentInfo];
        experimentInfoSpinner = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
        [self setCenter:experimentInfo forSpinner:experimentInfoSpinner];
        
        // Prepare choose experiment button
        chooseExperiment = [[UIButton alloc] initWithFrame:CGRectMake(20, self.view.bounds.size.height - 160, 433 - 40, 50)];
        chooseExperiment.backgroundColor = [UIColor grayColor];
        [chooseExperiment setTitle:[StringGrabber getString:@"choose_experiment"] forState:UIControlStateNormal];

    } else {
        // Bound, allocate, and customize the main view
        mainView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 480 - 44)];
        mainView.backgroundColor = [UIColor blackColor];
        self.view = mainView;
        [mainView release];
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
    
    // Prepare experimentSpinner for loading at bottom
    UIView *bottomSpinnerBlock = [[UIView alloc] initWithFrame:CGRectMake(0, self.view.bounds.size.height - 60, 320, 40)];
    [self.view addSubview:bottomSpinnerBlock];
    experimentSpinner = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    [bottomSpinnerBlock addSubview:experimentSpinner];
    [self setCenter:bottomSpinnerBlock forSpinner:experimentSpinner];
    [bottomSpinnerBlock release];
    
    // Prepare scrollview
    scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 50, 320, self.view.bounds.size.height - 120)];
    scrollHeight = scrollView.bounds.size.height;
    scrollView.delaysContentTouches = NO;
    scrollView.delegate = self;
    [self.view addSubview:scrollView];
    
    // Prepare rapi
    isenseAPI = [iSENSE getInstance];
    [isenseAPI toggleUseDev:YES];
    
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
    
    if (!(caller == lastExperimentClicked)) {
        if (lastExperimentClicked) {
            [lastExperimentClicked switchToDarkImage:FALSE];
        } else  {
            experimentInfo.hidden = NO;
        }
        
        lastExperimentClicked = caller;
        
        if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
            
            if ([experimentInfoThread isExecuting])
                [experimentInfoThread cancel];
            
            [[experimentInfo subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
            [experimentInfo addSubview:experimentInfoSpinner];
            [experimentInfoSpinner startAnimating];
            
            experimentInfoThread = [[NSThread alloc] initWithTarget:self selector:@selector(loadExperimentInfomationForIPad) object:nil];
            [experimentInfoThread start];
        }
    }
}

// Extra experiment information for loading in experimentInfoThread
- (void) loadExperimentInfomationForIPad {
    
    NSMutableArray *imageArray = [isenseAPI getExperimentImages:lastExperimentClicked->experiment.experiment_id];
    NSLog(@"Image count:%d", imageArray.count);
    if (imageArray.count) {
        // Fetch Images
        Image *firstImage = imageArray[0];
        NSURL *url = [NSURL URLWithString:firstImage.provider_url];
        NSData *data = [NSData dataWithContentsOfURL:url];
        UIImage *image = [UIImage imageWithData:data];
        UIImageView *imageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 110, 433, 433)];
        imageView.image = image;
        imageView.contentMode = UIViewContentModeScaleAspectFit;

        // Add image to experimentInfo
        [experimentInfo addSubview:imageView];
        //[imageView release];
        
    }
    
    // Set experimentTitle
    UILabel *experimentTitle = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, 433 - 40, 100)];
    experimentTitle.backgroundColor = [UIColor clearColor];
    experimentTitle.text = lastExperimentClicked->experiment.name;
    experimentTitle.textAlignment = NSTextAlignmentCenter;
    experimentTitle.textColor = [UIColor whiteColor];
    experimentTitle.numberOfLines = 0;
    experimentTitle.font = [UIFont fontWithName:@"Helvetica" size:24];
    //[experimentTitle release];
    
    // Update experimentInfo
    [experimentInfo addSubview:experimentTitle];
    [experimentInfo addSubview:chooseExperiment];
    
    [experimentInfoSpinner stopAnimating];

}

// Sets our experimentSpinner to the middle of the bottom block.
- (void)setCenter:(UIView *)view forSpinner:(UIActivityIndicatorView *)newSpinner {
    CGSize boundsSize = view.bounds.size;
    CGRect frameToCenter = newSpinner.frame;
    /* Center horizontally */
    if (frameToCenter.size.width < boundsSize.width)
        frameToCenter.origin.x = (boundsSize.width - frameToCenter.size.width) / 2;
    else
        frameToCenter.origin.x = 0;
    
    /* Center vertically */
    if (frameToCenter.size.height < boundsSize.height)
        frameToCenter.origin.y = (boundsSize.height - frameToCenter.size.height) / 2;
    else
        frameToCenter.origin.y = 0;
    
    newSpinner.frame = frameToCenter;
    NSLog(@"The newSpinner is currently at %f, %f.", newSpinner.center.x, newSpinner.center.y);
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
    
    for (Experiment *exp in experiments) {
        [exp retain];
        NSLog(@"%d, %@", exp.retainCount, exp.name);
        NSLog(@"Made a new block at %d", maxHeight);
        ExperimentBlock *block = [[ExperimentBlock alloc] initWithFrame:CGRectMake(0, maxHeight, 310, 50)
                                                             experiment:exp
                                                                 target:self
                                                                 action:@selector(onExperimentButtonClicked:)];
        [scrollView addSubview:block];
        //[block release];
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
    
    [experimentSpinner stopAnimating];
}

// Check if scrollview has reached bottom
- (void)scrollViewDidScroll:(UIScrollView *)scroller{
    if (scroller.contentOffset.y == scroller.contentSize.height - scroller.frame.size.height) {
        [experimentSpinner startAnimating];
        
        if ([loadExperimentThread isExecuting])
            [loadExperimentThread cancel];
        
        loadExperimentThread = [[NSThread alloc] initWithTarget:self selector:@selector(updateScrollView:) object:[[ISenseSearch alloc] initWithQuery:currentQuery searchType:RECENT page:(currentPage + 1) andBuildType:APPEND]];
        
        [loadExperimentThread start];
        
    }
}

- (void) dealloc {
    [super dealloc];
}

@end
