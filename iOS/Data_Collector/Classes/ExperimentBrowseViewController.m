//
//  ExperimentBrowseViewController.m
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/28/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "ExperimentBrowseViewController.h"

@implementation ExperimentBrowseViewController

@synthesize currentPage, currentQuery, scrollHeight, contentHeight, lastExperimentClicked;

#define SPINNER_HEIGHT 25

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
    UIView *mainView;
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        
        // Bound, allocate, and customize the main view
        mainView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 768, 1024 - NAVIGATION_CONTROLLER_HEIGHT)];
        mainView.backgroundColor = [HexColor colorWithHexString:@"EEEEEE"];
        self.view = mainView;
        
        // Prepare ExperimentInfo Frame
        experimentInfo = [[UIView alloc] initWithFrame:CGRectMake(325, 42, 433, self.view.bounds.size.height - 44)];
        experimentInfo.backgroundColor = [UIColor clearColor];
        experimentInfo.layer.borderWidth = 3;
        experimentInfo.layer.borderColor = [[UIColor blackColor] CGColor];
        experimentInfo.hidden = YES;
        [self.view addSubview:experimentInfo];
        experimentInfoSpinner = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
        [self setCenter:experimentInfo forSpinner:experimentInfoSpinner];
        
        // Prepare choose experiment button
        chooseExperiment = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        chooseExperiment.frame = CGRectMake(20, self.view.bounds.size.height - 170, experimentInfo.frame.size.width - 40, 100);
        [chooseExperiment setTitleColor:[HexColor colorWithHexString:@"5C93DB"] forState:UIControlStateNormal];
        [chooseExperiment setTitle:[StringGrabber grabString:@"choose_project"] forState:UIControlStateNormal];
        [chooseExperiment addTarget:self action:@selector(experimentChosen) forControlEvents:UIControlEventTouchUpInside];

    } else {
        // Bound, allocate, and customize the main view
        mainView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 480 - NAVIGATION_CONTROLLER_HEIGHT)];
        mainView.backgroundColor = [UIColor whiteColor];
        self.view = mainView;
    }
    
    // Prepare search bar
    searchBar = [[UISearchBar alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, 40)];
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
    
    // Prepare scrollview
    scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 42, 320, self.view.bounds.size.height - 44)];
    scrollHeight = scrollView.bounds.size.height;
    scrollView.delaysContentTouches = NO;
    scrollView.delegate = self;
    [self.view addSubview:scrollView];
    
    // Prepare experimentSpinner for loading at bottom
    bottomSpinnerBlock = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 25)];
    experimentSpinner = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    [bottomSpinnerBlock addSubview:experimentSpinner];
    [self setCenter:bottomSpinnerBlock forSpinner:experimentSpinner];
    [scrollView addSubview:bottomSpinnerBlock];
    
    // Prepare rapi
    isenseAPI = [iSENSE getInstance];
    [isenseAPI toggleUseDev:YES];
    
    // Load the first 10 experiments. (And more if screen size is large.)
    ISenseSearch *newSearch = [[ISenseSearch alloc] init];
    [self updateScrollView:newSearch];
    
}

// Is called every time ExperimentBrowser appears
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    // UpdateExperimentNumber status
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}

// Enables rotation in iOS6
- (BOOL)shouldAutorotate {
    return YES;
}

// Enables rotation in iOS6
- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

// Handles object resizing on rotation
- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        
        if(toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight) {
            self.view.frame = CGRectMake(0, 0, 1024, 768 - NAVIGATION_CONTROLLER_HEIGHT);
            experimentInfo.frame = CGRectMake(325, 42, self.view.bounds.size.width - 330, self.view.bounds.size.height - 44);
            [self setCenter:experimentInfo forSpinner:experimentInfoSpinner];
            chooseExperiment.frame = CGRectMake(20, self.view.bounds.size.height - 145, experimentInfo.frame.size.width - 40, 75);
            searchBar.frame = CGRectMake(0, 0, self.view.bounds.size.width, 40);
            scrollView.frame = CGRectMake(0, 42, 320, self.view.bounds.size.height - 44);
            if (additionalInfo) additionalInfo.frame = CGRectMake(20, 365, experimentInfo.frame.size.width - 40, experimentInfo.frame.size.height - 475);
            if (imageView) imageView.frame = CGRectMake(15, 100, experimentInfo.frame.size.width - 30, 250);
            if (experimentTitle) experimentTitle.frame = CGRectMake(20, 0, experimentInfo.frame.size.width - 40, 100);
        } else {
            self.view.frame = CGRectMake(0, 0, 768, 1024 - NAVIGATION_CONTROLLER_HEIGHT);
            experimentInfo.frame = CGRectMake(325, 42, 433, self.view.bounds.size.height - 44);
            [self setCenter:experimentInfo forSpinner:experimentInfoSpinner];
            chooseExperiment.frame = CGRectMake(20, self.view.bounds.size.height - 170, experimentInfo.frame.size.width - 40, 100);
            searchBar.frame = CGRectMake(0, 0, self.view.bounds.size.width, 40);
            scrollView.frame = CGRectMake(0, 42, 320, self.view.bounds.size.height - 44);
            if (additionalInfo) additionalInfo.frame = CGRectMake(20, 450, experimentInfo.frame.size.width - 40, experimentInfo.frame.size.height - 400);
            if (imageView) imageView.frame = CGRectMake(15, 100, experimentInfo.frame.size.width - 30, 300);
            if (experimentTitle) experimentTitle.frame = CGRectMake(20, 0, experimentInfo.frame.size.width - 40, 100);
        }

    } else {
        
        if(toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight) {
            self.view.frame = CGRectMake(0, 0, 480, 320 - NAVIGATION_CONTROLLER_HEIGHT);
            searchBar.frame = CGRectMake(0, 0, self.view.bounds.size.width, 40);
            scrollView.frame = CGRectMake(80, 42, self.view.bounds.size.width - 160, self.view.bounds.size.height - 44);
        } else {
            self.view.frame = CGRectMake(0, 0, 320, 480 - NAVIGATION_CONTROLLER_HEIGHT);
            searchBar.frame = CGRectMake(0, 0, self.view.bounds.size.width, 40);
            scrollView.frame = CGRectMake(0, 42, 320, self.view.bounds.size.height - 44);
        }
    }
}

- (void)experimentChosen {
    *_chosenExperiment = lastExperimentClicked.experiment.experiment_id.intValue;
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [experimentSpinner startAnimating];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


/* Search bar methods */
- (void)searchBarSearchButtonClicked:(UISearchBar *)search {
    [self handleSearch:search];
}

- (void)handleSearch:(UISearchBar *)search {
    NSString *query = [search.text copy];
   
    ISenseSearch *newSearch = [[ISenseSearch alloc] initWithQuery:query searchType:RECENT page:1 andBuildType:NEW];
    [self updateScrollView:newSearch];
    
    
    [scrollView addSubview:bottomSpinnerBlock];
    [experimentSpinner startAnimating];
    
    // Dismiss keyboard.
    [search resignFirstResponder];
}

- (void)searchBarCancelButtonClicked:(UISearchBar *) search {
    // Dismiss keyboard.
    [search resignFirstResponder];
}

- (void)onExperimentButtonClicked:(id)caller {
    
    if (!(caller == lastExperimentClicked)) {
        if (lastExperimentClicked) {
            [lastExperimentClicked switchToDarkImage:FALSE];
        } else  {
            experimentInfo.hidden = NO;
        }
        
        lastExperimentClicked = caller;
        
        if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
            
            [[experimentInfo subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
            [experimentInfo addSubview:experimentInfoSpinner];
            [experimentInfoSpinner startAnimating];
            
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                [self loadExperimentInfomationForIPad];
            });
        } else {
            [self experimentChosen];
        }
    }
}

// Extra experiment information for loading in background.
- (void)loadExperimentInfomationForIPad {
    
    NSMutableArray *imageArray = [isenseAPI getExperimentImages:lastExperimentClicked.experiment.experiment_id];
    
    if (self.interfaceOrientation == UIInterfaceOrientationPortrait) {
        imageView = [[UIImageView alloc] initWithFrame:CGRectMake(15, 100, experimentInfo.frame.size.width - 30, 300)];
    } else {
        imageView = [[UIImageView alloc] initWithFrame:CGRectMake(15, 100, experimentInfo.frame.size.width - 30, 250)];
    }
    
    if (imageArray.count) {
                
        // Fetch Images
        Image *firstImage = imageArray[0];
        NSURL *url = [NSURL URLWithString:firstImage.provider_url];
        NSData *data = [NSData dataWithContentsOfURL:url];
        UIImage *image = [UIImage imageWithData:data];
        imageView.image = image;
        imageView.contentMode = UIViewContentModeScaleAspectFit;
        
        // Add image to experimentInfo
        dispatch_async(dispatch_get_main_queue(), ^{
            [experimentInfo addSubview:imageView];
        });
    } else {
        imageView.image = [UIImage imageNamed:@"novis_photo.png"];
        imageView.contentMode = UIViewContentModeScaleAspectFit;
        
        // Add image to experimentInfo
        dispatch_async(dispatch_get_main_queue(), ^{
            [experimentInfo addSubview:imageView];
        });

    }
        
    dispatch_async(dispatch_get_main_queue(), ^{
        
        // Set experimentTitle
        experimentTitle = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, experimentInfo.frame.size.width - 40, 100)];
        experimentTitle.backgroundColor = [UIColor clearColor];
        experimentTitle.text = lastExperimentClicked.experiment.name;
        experimentTitle.textAlignment = NSTextAlignmentCenter;
        experimentTitle.textColor = [UIColor blackColor];
        experimentTitle.numberOfLines = 0;
        experimentTitle.font = [UIFont fontWithName:@"Helvetica" size:30];
        
        // Set additional information
        if (self.interfaceOrientation == UIInterfaceOrientationPortrait) {
            additionalInfo = [[UITextView alloc] initWithFrame:CGRectMake(20, 450, experimentInfo.frame.size.width - 40, experimentInfo.frame.size.height - 400)];
        } else {
            additionalInfo = [[UITextView alloc] initWithFrame:CGRectMake(20, 365, experimentInfo.frame.size.width - 40, experimentInfo.frame.size.height - 475)];
        }
        
        additionalInfo.text = [NSString stringWithFormat:@"Created by: %@\nNumber of Sessions: %@\nLast Modified: %@\n\nDescription: %@", lastExperimentClicked.experiment.firstname, lastExperimentClicked.experiment.session_count, lastExperimentClicked.experiment.timecreated, lastExperimentClicked.experiment.description];
        additionalInfo.textAlignment = NSTextAlignmentCenter;
        additionalInfo.font = [UIFont fontWithName:@"Arial" size:18];
        additionalInfo.textColor = [UIColor blackColor];
        additionalInfo.backgroundColor = [UIColor clearColor];
        additionalInfo.editable = FALSE;
                
        // Update experimentInfo
        [experimentInfo addSubview:additionalInfo];
        [experimentInfo addSubview:experimentTitle];
        [experimentInfo addSubview:chooseExperiment];   

        // Release subviews
    
        [experimentInfoSpinner stopAnimating];
    });
    
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
}

// Update scrollView by appending or making a new search in background.
- (void) updateScrollView:(ISenseSearch *)iSS {
        
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        
        NSMutableArray *experiments = [isenseAPI getExperiments:[NSNumber numberWithInt:iSS.page]
                                                       withLimit:[NSNumber numberWithInt:15]
                                                       withQuery:iSS.query
                                                         andSort:[iSS searchTypeToString]];
                        
        dispatch_async(dispatch_get_main_queue(), ^{
            // remove the spinner
            [experimentSpinner stopAnimating];
            [experimentSpinner removeFromSuperview];

            currentPage = iSS.page;
            currentQuery = iSS.query;
            
            int maxHeight = 0;
            
            if (iSS.buildType == NEW) {
                [[scrollView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
                contentHeight = 0;
            }
            
            if (iSS.buildType == APPEND) {
                maxHeight = contentHeight - SPINNER_HEIGHT;
            }
            
            for (Experiment *exp in experiments) {
               
                ExperimentBlock *block = [[ExperimentBlock alloc] initWithFrame:CGRectMake(0, maxHeight, 310, 50)
                                                                     experiment:exp
                                                                         target:self
                                                                         action:@selector(onExperimentButtonClicked:)];                
                [scrollView addSubview:block];
                maxHeight += 54; // adds 4 pixels of padding
            }
            
            if (experiments.count == 0) {
                UILabel *noExperimentsFound = [[UILabel alloc] initWithFrame:CGRectMake(5, 0, 310, 20)];
                noExperimentsFound.text = @"No experiments found.";
                [scrollView addSubview:noExperimentsFound];
                maxHeight += 20;
            }
            
            // Hopefully adds a spinner to the bottom of the view
            bottomSpinnerBlock.frame = CGRectMake(0, maxHeight, 320, 25);
            [scrollView addSubview:bottomSpinnerBlock];
            [bottomSpinnerBlock addSubview:experimentSpinner];
            [self setCenter:bottomSpinnerBlock forSpinner:experimentSpinner];
            maxHeight += SPINNER_HEIGHT;
            
            CGSize scrollableSize = CGSizeMake(320, maxHeight);
            [scrollView setContentSize:scrollableSize];
            
            contentHeight = maxHeight;
            
            if (contentHeight <= scrollHeight && experiments.count == 15) {
                iSS.page++;
                iSS.buildType = APPEND;
                [experimentSpinner startAnimating];
                [self updateScrollView:iSS];
            }
    
    
        });
        
    });
    
}

// Check if scrollview has reached bottom
- (void)scrollViewDidScroll:(UIScrollView *)scroller{
    if (scroller.contentOffset.y == scroller.contentSize.height - scroller.frame.size.height) {
        [experimentSpinner startAnimating];
        
        ISenseSearch *newSearch = [[ISenseSearch alloc] initWithQuery:currentQuery searchType:RECENT page:(currentPage + 1) andBuildType:APPEND];
        [self updateScrollView:newSearch];
        
    }
}


@end
