//
//  ProjectBrowserViewController.h
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 1/31/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <ISenseSearch.h>
#import <RProject.h>
#import <API.h>
#import "ProjectCell.h"

@class ProjectBrowserViewController;
@protocol ProjectBrowserDelegate <NSObject>

@required
- (void) didFinishChoosingProject:(ProjectBrowserViewController *) browser withID: (int) project_id;

@end

@interface ProjectBrowserViewController : UITableViewController <UITableViewDelegate, UITableViewDataSource, UISearchBarDelegate, UITextFieldDelegate> {
    
    API *isenseAPI;
    __weak id <ProjectBrowserDelegate> delegate;

    
    
}

@property IBOutlet UISearchBar *bar;
@property IBOutlet UITableView *table;
@property int cell_count;
@property BOOL isUpdating;
@property NSMutableArray *projects;
@property NSMutableArray *projectsFiltered;
@property (nonatomic, assign) int currentPage;
@property (nonatomic, retain) NSString *currentQuery;
@property (nonatomic, weak) id <ProjectBrowserDelegate> delegate;
@property UIAlertView *spinnerDialog;

- (id)initWithDelegate: (__weak id<ProjectBrowserDelegate>) delegateObject;


@end
