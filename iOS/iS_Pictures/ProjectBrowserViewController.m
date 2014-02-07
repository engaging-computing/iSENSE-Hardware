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

@synthesize bar, table, cell_count, isUpdating, projects, currentPage, currentQuery, delegate;

- (id)initWithDelegate: (id<ProjectBrowserDelegate>) delegateObject
{
    self = [super init];
    
    delegate =  delegateObject;

    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    isenseAPI = [API getInstance];
    cell_count = 10;
    isUpdating = false;
    projects = [[NSMutableArray alloc] init];
    ISenseSearch *search = [[ISenseSearch alloc] init];
    table.tableHeaderView = bar;
    [self updateProjects:search];
    table.dataSource = self;
    table.delegate = self;
    
    // Do any additional setup after loading the view from its nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)scrollViewDidScroll: (UIScrollView*)scroll {
    // UITableView only moves in one direction, y axis
    if (!isUpdating) {
        NSInteger currentOffset = scroll.contentOffset.y;
        NSInteger maximumOffset = scroll.contentSize.height - scroll.frame.size.height;
        
        // Change 10.0 to adjust the distance from bottom
        if (maximumOffset - currentOffset <= 10.0) {
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
	
	return cell_count + 1;
	
}

-(void)updateProjects:(ISenseSearch *)iSS {
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        dispatch_async(dispatch_get_main_queue(), ^{
            
            [projects addObjectsFromArray:[isenseAPI getProjectsAtPage:iSS.page withPageLimit:iSS.perPage withFilter:CREATED_AT_DESC andQuery:iSS.query]];
            
            NSLog(@"Hello");
            
            currentPage = iSS.page;
            currentQuery = iSS.query;
            
            
        });
        
    });
    double delayInSeconds = 4.0;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        [table reloadData];
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
		
	} else {
        int row = indexPath.row;
        
        RProject *proj = [projects objectAtIndex:row];
        
        UITableViewCell *cell = [[ProjectCell alloc] initWithProject:proj];
        
        
        
        return cell;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (indexPath.row == cell_count) return;
    else {
        [self.delegate didFinishChoosingProject:self withID:[(RProject *)[projects objectAtIndex:indexPath.row] project_id].intValue];
        [self.navigationController popViewControllerAnimated:YES];
    }
    
    
    
}


@end
