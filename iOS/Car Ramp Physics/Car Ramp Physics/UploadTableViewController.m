//
//  UploadTableViewController.m
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/13/13.
//  Copyright (c) 2013 ECG. All rights reserved.
//

#import "UploadTableViewController.h"

@interface UploadTableViewController ()

@end

@implementation UploadTableViewController

@synthesize dataSource, selectedMarks, saver;

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    UIBarButtonItem *uploadButton = [[UIBarButtonItem alloc] initWithTitle:@"Upload" style:UIBarButtonItemStyleDone target:self action:@selector(uploadSaved)];
    
    dataSource = [[NSMutableArray alloc] init];
    selectedMarks = [[NSMutableArray alloc] init];
    
    int count = saver.count;
    
    
    while (count) {
        // get the next dataset
        int headKey = saver.dataQueue.allKeys[0];
        DataSet *current = [saver removeDataSet:headKey];
        [saver.dataQueue enqueue:current withKey:headKey];
    
        NSString *cellString = [current.name stringByAppendingString:[@" " stringByAppendingString:current.description]];
        
        [dataSource addObject:cellString];
        
        count--;

    }
    
    UIToolbar* toolbar = [[UIToolbar alloc] initWithFrame:CGRectMake(-20, 0.0f, 130.0f, 44.01f)];
    [toolbar setBarStyle:UIBarStyleBlack];
    NSArray* buttons = [NSArray arrayWithObjects:self.editButtonItem, uploadButton, nil];
    [toolbar setItems:buttons animated:YES];
    
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:toolbar];
    
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [self.dataSource count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CRTableViewCellIdentifier = @"cellIdentifier";
    
    // init the CRTableViewCell
    CRTableViewCell *cell = (CRTableViewCell *)[tableView dequeueReusableCellWithIdentifier:CRTableViewCellIdentifier];
    
    if (cell == nil) {
        cell = [[CRTableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CRTableViewCellIdentifier];
    }
    
    // Check if the cell is currently selected (marked)
    NSString *text = [dataSource objectAtIndex:[indexPath row]];
    cell.isSelected = [selectedMarks containsObject:text] ? YES : NO;
    cell.textLabel.text = text;
    
    return cell;
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *text = [dataSource objectAtIndex:[indexPath row]];
    
    if ([selectedMarks containsObject:text])// Is selected?
        [selectedMarks removeObject:text];
    else
        [selectedMarks addObject:text];
    
    [tableView reloadRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    // If row is deleted, remove it from the list.
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        [dataSource removeObjectAtIndex:indexPath.row];
        [tableView reloadData];
    }
}

- (void) uploadSaved {
    
}

@end
