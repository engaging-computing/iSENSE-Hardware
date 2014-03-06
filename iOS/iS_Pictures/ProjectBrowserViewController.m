//
//  ProjectBrowserViewController.m
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 1/31/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import "ProjectBrowserViewController.h"

@interface ProjectBrowserViewController ()

@end

@implementation ProjectBrowserViewController

@synthesize bar, table, cell_count, isUpdating, projects, currentPage, currentQuery, delegate, spinnerDialog, projectsFiltered;

// pre-iOS6 rotating options
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

// iOS6 rotating options
- (BOOL)shouldAutorotate {
    return YES;
}

// iOS6 interface orientations
- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"ProjectBrowserViewController~landscape_iPad"
                                          owner:self
                                        options:nil];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"ProjectBrowserViewController_iPad"
                                          owner:self
                                        options:nil];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"ProjectBrowserViewController~landscape_iPhone"
                                          owner:self
                                        options:nil];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"ProjectBrowserViewController_iPhone"
                                          owner:self
                                        options:nil];
            
            
        }
    }
    
    
}


- (id)initWithDelegate: (__weak id<ProjectBrowserDelegate>) delegateObject
{
    self = [super init];
    
    if (self) {
    
        delegate =  delegateObject;
        isenseAPI = [API getInstance];
        cell_count = 10;
        isUpdating = false;
        
        
    }

    return self;
}

- (void) viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [self willRotateToInterfaceOrientation:self.interfaceOrientation duration:0];
    ISenseSearch *search = [[ISenseSearch alloc] init];
    self.tableView.tableHeaderView = bar;
    bar.delegate = self;
    
    [bar setShowsScopeBar:false];
    [bar setShowsCancelButton:false animated:true];
    [bar sizeToFit];
    
    spinnerDialog = [self getDispatchDialogWithMessage:@"Loading..."];
    [spinnerDialog show];
    
    NSLog(@"Boo");
    [self updateProjects:search];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    
    

    
    // Do any additional setup after loading the view from its nib.
}

- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar {
    [searchBar setShowsCancelButton:true animated:true];
    [searchBar sizeToFit];
}
/*
- (void)textFieldDidEndEditing:(UITextField *)textField {
    if ([textField.text isEqualToString:@""]) {
        [bar setShowsScopeBar:false];
    } else {
        [bar setShowsScopeBar:true];
    }
}
*/
- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

// Default dispatch_async dialog with custom spinner
- (UIAlertView *) getDispatchDialogWithMessage:(NSString *)dString {
    UIAlertView *message = [[UIAlertView alloc] initWithTitle:dString
                                                      message:nil
                                                     delegate:self
                                            cancelButtonTitle:nil
                                            otherButtonTitles:nil];
    UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    spinner.center = CGPointMake(139.5, 75.5);
    [message addSubview:spinner];
    [spinner startAnimating];
    return message;
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scroll willDecelerate:(BOOL)decelerate {
    // UITableView only moves in one direction, y axis
    
    
    if (!isUpdating) {
        NSInteger currentOffset = scroll.contentOffset.y;
        NSInteger maximumOffset = scroll.contentSize.height - scroll.frame.size.height;
        
        // Change 10.0 to adjust the distance from bottom
        if (maximumOffset - currentOffset <= 10) {
            isUpdating = true;
            [self update];
        }
    }
}

- (void) update{
    cell_count += 10;
    ISenseSearch *newSearch = [[ISenseSearch alloc] initWithQuery:currentQuery page:(currentPage + 1) itemsPerPage:10 andBuildType:APPEND];
    [self updateProjects:newSearch];
    isUpdating = false;
}

// There is a single column in this table
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

// There are as many rows as there are projects
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
	
	if (![currentQuery isEqualToString:@""]) {
        if ([projectsFiltered count] == 0) {
            return 1;
        } else {
           return [projectsFiltered count]; 
        }
        
    } else {
        return cell_count + 1;
    }
	
}

-(void)updateProjects:(ISenseSearch *)iSS {
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        dispatch_async(dispatch_get_main_queue(), ^{
            
            if (projects == nil) {
                NSLog(@"World");
                projects = [[NSMutableArray alloc] init];
                projectsFiltered = [[NSMutableArray alloc] init];
                self.tableView.dataSource = self;
                self.tableView.delegate = self;
            }
            
            if (![iSS.query isEqualToString:@""]){
                [projectsFiltered addObjectsFromArray:[isenseAPI getProjectsAtPage:iSS.page withPageLimit:iSS.perPage withFilter:CREATED_AT_DESC andQuery:iSS.query]];
            } else {
               [projects addObjectsFromArray:[isenseAPI getProjectsAtPage:iSS.page withPageLimit:iSS.perPage withFilter:CREATED_AT_DESC andQuery:iSS.query]];
            }
            
            
            
            NSLog(@"Hello");
            
            currentPage = iSS.page;
            currentQuery = iSS.query;
            
            
        });
        
    });
    double delayInSeconds = 4.0;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        [self.tableView reloadData];
        [spinnerDialog dismissWithClickedButtonIndex:0 animated:YES];
    });
    
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
	
    if (indexPath.row == cell_count){ // Special Case 2
		
		UITableViewCell *cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:@"NoReuse"];
        cell.textLabel.text = @"Loading...";
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
        UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc]
                                             initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
        
        // Spacer is a 1x1 transparent png
        UIImage *spacer = [UIImage imageNamed:@"spacer"];
        
        UIGraphicsBeginImageContext(spinner.frame.size);
        
        [spacer drawInRect:CGRectMake(0,0,spinner.frame.size.width,spinner.frame.size.height)];
        UIImage* resizedSpacer = UIGraphicsGetImageFromCurrentImageContext();
        
        UIGraphicsEndImageContext();
        cell.imageView.image = resizedSpacer;
        [cell.imageView addSubview:spinner];
        [spinner startAnimating];
        
        return cell;
		
	} else if (![currentQuery isEqualToString:@""] && [projectsFiltered count] == 0) {
        UITableViewCell *cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"NoProjFoundCell"];
        cell.textLabel.text= @"No Projects Found";
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
        return cell;
        
    } else {
        int row = indexPath.row;
        
        RProject *proj;
        if (![currentQuery isEqualToString:@""]) {
            proj = [projectsFiltered objectAtIndex:row];
        } else {
            proj = [projects objectAtIndex:row];
        }
        
        UITableViewCell *cell = [[ProjectCell alloc] initWithProject:proj];
        
        
        
        return cell;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (![bar.text isEqualToString:@""]) {
        if ([projectsFiltered count] == 0) return;
        else {
            [self.delegate didFinishChoosingProject:self withID:[(RProject *)[projectsFiltered objectAtIndex:indexPath.row] project_id].intValue];
            [self.navigationController popViewControllerAnimated:YES];
        }
    } else {
        if (indexPath.row == cell_count) return;
        else {
            [self.delegate didFinishChoosingProject:self withID:[(RProject *)[projects objectAtIndex:indexPath.row] project_id].intValue];
            [self.navigationController popViewControllerAnimated:YES];
        }
    }
    
    
    
    
    
}

/* Search bar methods */
- (void)searchBarSearchButtonClicked:(UISearchBar *)search {
    spinnerDialog = [self getDispatchDialogWithMessage:@"Loading..."];
    [spinnerDialog show];
    [projectsFiltered removeAllObjects];
    [self handleSearch:search];
}

- (void)handleSearch:(UISearchBar *)search {
    NSString *query = [search.text copy];
    
    ISenseSearch *newSearch = [[ISenseSearch alloc] initWithQuery:query page:1 itemsPerPage:10 andBuildType:NEW];
    [self updateProjects:newSearch];
    
    // Dismiss keyboard.
    [search resignFirstResponder];
}

- (void)searchBarCancelButtonClicked:(UISearchBar *) search {
    // Dismiss keyboard.
    [search setText:@""];
    [search resignFirstResponder];
    
    spinnerDialog = [self getDispatchDialogWithMessage:@"Loading..."];
    [spinnerDialog show];
    
    ISenseSearch *newSearch = [[ISenseSearch alloc] initWithQuery:@"" page:1 itemsPerPage:10 andBuildType:NEW];
    [self updateProjects:newSearch];
    [search setShowsCancelButton:false animated:true];
    [search sizeToFit];

}


@end
