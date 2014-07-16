//
//  MenuTableViewController.m
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 6/10/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import "MenuTableViewController.h"

@interface MenuTableViewController ()

@end

@implementation MenuTableViewController

@synthesize section1,section2,section3,section4, parent,sectionHeaders, frame;


- (id)initWithParentViewController:(ViewController *)parentController {
    self = [super init];
    if (self) {
        parent = parentController;
        self.view.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        self.view.autoresizesSubviews = YES;
        //self.view.contentMode = UIViewContentModeScaleToFill;

    }
    return self;
}

- (void)viewDidLoad
{
    
    // Uncomment the following line to preserve selection between presentations.
     self.clearsSelectionOnViewWillAppear = YES;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    section1 = [[NSMutableArray alloc] initWithObjects:@"Upload", @"Login", nil];
    section2 = [[NSMutableArray alloc] initWithObjects:@"Recording Length", @"Recording Rate", @"Project ID", nil];
    section3 = [[NSMutableArray alloc] initWithObjects:@"About", @"Help", nil];
    section4 = [[NSMutableArray alloc] initWithObjects:@"Reset", nil];
    sectionHeaders = [[NSMutableArray alloc] initWithObjects:@"Upload", @"Recording Settings", @"Information", @"Reset", nil];




}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    
    [[UITableViewHeaderFooterView appearance] setTintColor:UIColorFromHex(0x111155)];
    if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {
       
    }
    return [sectionHeaders objectAtIndex:section];
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
    return 4;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    switch (section) {
        case 0:
            return 2;
        case 1:
            return 3;
        case 2:
            return 2;
        default:
            return 1;
    }
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle  reuseIdentifier:CellIdentifier];
        
    }
    
    switch (indexPath.section) {
        case 0:
            cell.textLabel.text = [section1 objectAtIndex:indexPath.row];
            break;
        case 1:
            cell.textLabel.text = [section2 objectAtIndex:indexPath.row];
            break;
        case 2:
            cell.textLabel.text = [section3 objectAtIndex:indexPath.row];
            break;
        case 3:
            cell.textLabel.text = [section4 objectAtIndex:indexPath.row];
            break;
            
        default:
            cell.textLabel.text = @"Error loading menu";
            break;
    }
    
    cell.contentView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleBottomMargin;
    
    return cell;
}

- (void) viewWillAppear:(BOOL)animated {
    [self.tableView deselectRowAtIndexPath:[self.tableView indexPathForSelectedRow] animated:animated];
    [super viewWillAppear:animated];
}


#pragma mark - Table view delegate

// In a xib-based application, navigation from a table can be handled in -tableView:didSelectRowAtIndexPath:
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    switch (indexPath.section) {
        case 0:
            switch (indexPath.row) {
                case 0:
                    if (parent.dataSaver.dataQueue.count > 0) {
                        //[[SlideNavigationController sharedInstance] popToRootAndSwitchToViewController:queueUploader withCompletion:nil];
                    } else {
                        [[SlideNavigationController sharedInstance] popToRootViewControllerAnimated:YES];
                        [[SlideNavigationController sharedInstance] closeMenuWithCompletion:^{
                            [parent.view makeWaffle:@"No data sets to upload!" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
                        }];
                        
                    }
                    break;
                case 1:
                    [[SlideNavigationController sharedInstance] popToRootViewControllerAnimated:YES];
                    [[SlideNavigationController sharedInstance] closeMenuWithCompletion:^{
                        [parent showCredentialManager];
                    }];
                    break;
            }
            break;
        case 1:
            switch (indexPath.row) {
                case 0: {
                    [[SlideNavigationController sharedInstance] popToRootViewControllerAnimated:YES];
                    [[SlideNavigationController sharedInstance] closeMenuWithCompletion:^{
                        [parent showRecordLengthDialog];
                    }];
                }
                    break;
                case 1: {
                    [[SlideNavigationController sharedInstance] popToRootViewControllerAnimated:YES];
                    [[SlideNavigationController sharedInstance] closeMenuWithCompletion:^{
                        [parent showRecordRateDialog];
                    }];
                }
                    break;
                case 2: {
                    [[SlideNavigationController sharedInstance] popToRootViewControllerAnimated:YES];
                    [[SlideNavigationController sharedInstance] closeMenuWithCompletion:^{
                        [parent showProjectIDDialog];
                    }];
                }
                    break;
            }
            break;
        case 2:
            switch (indexPath.row) {
                case 0: {
                    AboutViewController *about;
                    // Override point for customization after application launch.
                    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
                        about = [[AboutViewController alloc] initWithNibName:@"AboutViewController_iPhone" bundle:nil andStringText:@"about_app_text"];
                    } else {
                        about = [[AboutViewController alloc] initWithNibName:@"AboutViewController_iPad" bundle:nil andStringText:@"about_app_text"];
                    }
                    about.navigationItem.title = @"About";
                    [[SlideNavigationController sharedInstance] popToRootAndSwitchToViewController:about withCompletion:nil];
                }
                    break;
                case 1:
                {
                    AboutViewController *about;
                    // Override point for customization after application launch.
                    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
                        about = [[AboutViewController alloc] initWithNibName:@"AboutViewController_iPhone" bundle:nil andStringText:@"help_app_text"];
                    } else {
                        about = [[AboutViewController alloc] initWithNibName:@"AboutViewController_iPad" bundle:nil andStringText:@"help_app_text"];
                    }
                    about.navigationItem.title = @"Help";
                    [[SlideNavigationController sharedInstance] popToRootAndSwitchToViewController:about withCompletion:nil];
                }
                    break;
            }
            break;
        case 3:
            [[SlideNavigationController sharedInstance] popToRootViewControllerAnimated:YES];
            [[SlideNavigationController sharedInstance] closeMenuWithCompletion:^{
                [parent showResetDialog];
            }];
            break;
    }
}


@end
