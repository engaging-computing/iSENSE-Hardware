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

@synthesize bar, table;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    table.tableHeaderView = bar;
    // Do any additional setup after loading the view from its nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

// There is a single column in this table
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}
/*
// There are as many rows as there are DataSets
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
	
	// Return 1 section which will be used to display the giant blank UITableViewCell as defined
	// in the tableView:cellForRowAtIndexPath: method below
	if ( == 0){
		
		return 1;
		
	} else if ([self.searchResults count] < self.webServiceDataModel.total) {
        
		// Add an object to the end of the array for the "Load more..." table cell.
		return [self.searchResults count] + 1;
        
	}
	// Return the number of rows as there are in the searchResults array.
	return [self.searchResults count];
	
}
**/
// Initialize a single object in the table
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
}

@end
