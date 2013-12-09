//
//  ProjectBrowseViewController.m
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/28/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import "ProjectBrowseViewController.h"

@implementation ProjectBrowseViewController

@synthesize currentPage, currentQuery, scrollHeight, contentHeight, lastProjectClicked;

#define SPINNER_HEIGHT 25
#define NAVIGATION_CONTROLLER_HEIGHT 75
#define ITEMS_PER_PAGE 15

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
    UIView *mainView;
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        
        // Bound, allocate, and customize the main view
        mainView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 768, 1024 - NAVIGATION_CONTROLLER_HEIGHT)];
        mainView.backgroundColor = UIColorFromHex(0xEEEEEE);
        self.view = mainView;
        
        // Prepare ProjectInfo Frame
        projectInfo = [[UIView alloc] initWithFrame:CGRectMake(325, 42, 433, self.view.bounds.size.height - 44)];
        projectInfo.backgroundColor = [UIColor clearColor];
        projectInfo.layer.borderWidth = 3;
        projectInfo.layer.borderColor = [[UIColor blackColor] CGColor];
        projectInfo.hidden = YES;
        [self.view addSubview:projectInfo];
        projectInfoSpinner = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
        [self setCenter:projectInfo forSpinner:projectInfoSpinner];
        
        // Prepare choose Project button
        chooseProject = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        chooseProject.frame = CGRectMake(20, self.view.bounds.size.height - 170, projectInfo.frame.size.width - 40, 100);
        [chooseProject setTitleColor:UIColorFromHex(0x5C93DB)forState:UIControlStateNormal];
        [chooseProject setTitle:kCHOOSE_PROJECT forState:UIControlStateNormal];
        //[chooseProject addTarget:self action:@selector(projectChosen) forControlEvents:UIControlEventTouchUpInside];

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
    
    // Prepare ProjectSpinner for loading at bottom
    bottomSpinnerBlock = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 25)];
    projectSpinner = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    [bottomSpinnerBlock addSubview:projectSpinner];
    [self setCenter:bottomSpinnerBlock forSpinner:projectSpinner];
    [scrollView addSubview:bottomSpinnerBlock];
    
    // Prepare api
    isenseAPI = [API getInstance];
    
    // Load the first 10 Projects. (And more if screen size is large.)
    ISenseSearch *newSearch = [[ISenseSearch alloc] init];
    [self updateScrollView:newSearch];
    
}

// Is called every time ProjectBrowser appears
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    // UpdateProjectNumber status
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
            projectInfo.frame = CGRectMake(325, 42, self.view.bounds.size.width - 330, self.view.bounds.size.height - 44);
            [self setCenter:projectInfo forSpinner:projectInfoSpinner];
            chooseProject.frame = CGRectMake(20, self.view.bounds.size.height - 145, projectInfo.frame.size.width - 40, 75);
            searchBar.frame = CGRectMake(0, 0, self.view.bounds.size.width, 40);
            scrollView.frame = CGRectMake(0, 42, 320, self.view.bounds.size.height - 44);
            if (additionalInfo) additionalInfo.frame = CGRectMake(20, 365, projectInfo.frame.size.width - 40, projectInfo.frame.size.height - 475);
            if (imageView) imageView.frame = CGRectMake(15, 100, projectInfo.frame.size.width - 30, 250);
            if (projectTitle) projectTitle.frame = CGRectMake(20, 0, projectInfo.frame.size.width - 40, 100);
        } else {
            self.view.frame = CGRectMake(0, 0, 768, 1024 - NAVIGATION_CONTROLLER_HEIGHT);
            projectInfo.frame = CGRectMake(325, 42, 433, self.view.bounds.size.height - 44);
            [self setCenter:projectInfo forSpinner:projectInfoSpinner];
            chooseProject.frame = CGRectMake(20, self.view.bounds.size.height - 170, projectInfo.frame.size.width - 40, 100);
            searchBar.frame = CGRectMake(0, 0, self.view.bounds.size.width, 40);
            scrollView.frame = CGRectMake(0, 42, 320, self.view.bounds.size.height - 44);
            if (additionalInfo) additionalInfo.frame = CGRectMake(20, 450, projectInfo.frame.size.width - 40, projectInfo.frame.size.height - 400);
            if (imageView) imageView.frame = CGRectMake(15, 100, projectInfo.frame.size.width - 30, 300);
            if (projectTitle) projectTitle.frame = CGRectMake(20, 0, projectInfo.frame.size.width - 40, 100);
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

- (void)projectChosen {
    NSLog(@"%@", lastProjectClicked.project.owner_name);
    [self.delegate projectViewController:self didFinishChoosingProject:lastProjectClicked.project.project_id];
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [projectSpinner startAnimating];
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
   
    ISenseSearch *newSearch = [[ISenseSearch alloc] initWithQuery:query page:1 itemsPerPage:ITEMS_PER_PAGE andBuildType:NEW];
    [self updateScrollView:newSearch];
       
    [scrollView addSubview:bottomSpinnerBlock];
    [projectSpinner startAnimating];
    
    // Dismiss keyboard.
    [search resignFirstResponder];
}

- (void)searchBarCancelButtonClicked:(UISearchBar *) search {
    // Dismiss keyboard.
    [search resignFirstResponder];
}

- (void)onProjectButtonClicked:(id)caller {
    
    if (!(caller == lastProjectClicked)) {
        if (lastProjectClicked) {
            [lastProjectClicked switchToDarkImage:FALSE];
        } else  {
            projectInfo.hidden = NO;
        }
        
        lastProjectClicked = caller;
        
        if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
            
            [[projectInfo subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
            [projectInfo addSubview:projectInfoSpinner];
            [projectInfoSpinner startAnimating];
            
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                [self loadProjectInfomationForIPad];
            });
        } else {
            [self projectChosen];
        }
    }
}

// Extra Project information for loading in background.
- (void)loadProjectInfomationForIPad {
    
    //NSMutableArray *imageArray = [isenseAPI getProjectWithId:lastProjectClicked.project.project_id].;
    
    if (self.interfaceOrientation == UIInterfaceOrientationPortrait) {
        imageView = [[UIImageView alloc] initWithFrame:CGRectMake(15, 100, projectInfo.frame.size.width - 30, 300)];
    } else {
        imageView = [[UIImageView alloc] initWithFrame:CGRectMake(15, 100, projectInfo.frame.size.width - 30, 250)];
    }
    
//    if (imageArray.count) {
//                
//        // Fetch Images
//        Image *firstImage = imageArray[0];
//        NSURL *url = [NSURL URLWithString:firstImage.provider_url];
//        NSData *data = [NSData dataWithContentsOfURL:url];
//        UIImage *image = [UIImage imageWithData:data];
//        imageView.image = image;
//        imageView.contentMode = UIViewContentModeScaleAspectFit;
//        
//        // Add image to ProjectInfo
//        dispatch_async(dispatch_get_main_queue(), ^{
//            [projectInfo addSubview:imageView];
//        });
//    } else {
        imageView.image = [UIImage imageNamed:@"novis_photo.png"];
        imageView.contentMode = UIViewContentModeScaleAspectFit;
        
        // Add image to ProjectInfo
        dispatch_async(dispatch_get_main_queue(), ^{
            [projectInfo addSubview:imageView];
        });

//    }
        
    dispatch_async(dispatch_get_main_queue(), ^{
        
        // Set ProjectTitle
        projectTitle = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, projectInfo.frame.size.width - 40, 100)];
        projectTitle.backgroundColor = [UIColor clearColor];
        projectTitle.text = lastProjectClicked.project.name;
        projectTitle.textAlignment = NSTextAlignmentCenter;
        projectTitle.textColor = [UIColor blackColor];
        projectTitle.numberOfLines = 0;
        projectTitle.font = [UIFont fontWithName:@"Helvetica" size:30];
        
        // Set additional information
        if (self.interfaceOrientation == UIInterfaceOrientationPortrait) {
            additionalInfo = [[UITextView alloc] initWithFrame:CGRectMake(20, 450, projectInfo.frame.size.width - 40, projectInfo.frame.size.height - 400)];
        } else {
            additionalInfo = [[UITextView alloc] initWithFrame:CGRectMake(20, 365, projectInfo.frame.size.width - 40, projectInfo.frame.size.height - 475)];
        }
        
        additionalInfo.text = [NSString stringWithFormat:@"Created by: %@\nLast Modified: %@\n\nURL: %@", lastProjectClicked.project.name, lastProjectClicked.project.timecreated, lastProjectClicked.project.url];
        additionalInfo.textAlignment = NSTextAlignmentCenter;
        additionalInfo.font = [UIFont fontWithName:@"Arial" size:18];
        additionalInfo.textColor = [UIColor blackColor];
        additionalInfo.backgroundColor = [UIColor clearColor];
        additionalInfo.editable = FALSE;
                
        // Update ProjectInfo
        [projectInfo addSubview:additionalInfo];
        [projectInfo addSubview:projectTitle];
        [projectInfo addSubview:chooseProject];
    
        [projectInfoSpinner stopAnimating];
    });
    
}

// Sets our ProjectSpinner to the middle of the bottom block.
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
-(void)updateScrollView:(ISenseSearch *)iSS {
        
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        dispatch_async(dispatch_get_main_queue(), ^{
            
            NSArray *projects = [isenseAPI getProjectsAtPage:iSS.page withPageLimit:iSS.perPage withFilter:CREATED_AT_DESC andQuery:iSS.query];
            
            // remove the spinner
            [projectSpinner stopAnimating];
            [projectSpinner removeFromSuperview];

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
            
            for (RProject *proj in projects) {
                              
                ProjectBlock *block = [[ProjectBlock alloc] initWithFrame:CGRectMake(0, maxHeight, 310, 50)
                                                                     project:proj
                                                                         target:self
                                                                         action:@selector(onProjectButtonClicked:)];                
                [scrollView addSubview:block];
                maxHeight += 54; // adds 4 pixels of padding
            }
            
            if (projects.count == 0 && iSS.buildType == NEW) {
                UILabel *noProjectsFound = [[UILabel alloc] initWithFrame:CGRectMake(5, 0, 310, 20)];
                noProjectsFound.text = @"No Projects found.";
                [scrollView addSubview:noProjectsFound];
                maxHeight += 20;
            }
            
            // Hopefully adds a spinner to the bottom of the view
            bottomSpinnerBlock.frame = CGRectMake(0, maxHeight, 320, 25);
            [scrollView addSubview:bottomSpinnerBlock];
            [bottomSpinnerBlock addSubview:projectSpinner];
            [self setCenter:bottomSpinnerBlock forSpinner:projectSpinner];
            maxHeight += SPINNER_HEIGHT;
            
            CGSize scrollableSize = CGSizeMake(320, maxHeight);
            [scrollView setContentSize:scrollableSize];
            
            contentHeight = maxHeight;
            
            if (contentHeight <= scrollHeight && projects.count == 15) {
                iSS.page++;
                iSS.buildType = APPEND;
                [projectSpinner startAnimating];
                [self updateScrollView:iSS];
            }
       
        });
        
    });
    
}

// Check if scrollview has reached bottom
- (void)scrollViewDidScroll:(UIScrollView *)scroller{
    if (scroller.contentOffset.y == scroller.contentSize.height - scroller.frame.size.height) {
        [projectSpinner startAnimating];
        
        ISenseSearch *newSearch = [[ISenseSearch alloc] initWithQuery:currentQuery page:(currentPage + 1) itemsPerPage:ITEMS_PER_PAGE andBuildType:APPEND];
        [self updateScrollView:newSearch];
        
    }
}

@end
