//
//  VariablesViewController.m
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/15/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin

#import "VariablesViewController.h"

@interface VariablesViewController ()

@end

@implementation VariablesViewController

@synthesize dataSource, selectedMarks;

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"VariablesViewController~landscape_iPad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"VariablesViewController_iPad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"VariablesViewController~landscape_iPhone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"VariablesViewController_iPhone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
    
}

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

- (void)viewDidLoad
{
    [super viewDidLoad];

    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithTitle:@"Done" style:UIBarButtonItemStyleDone target:self action:@selector(done)];
    
    dataSource = [[NSMutableArray alloc] init];
    selectedMarks = [[NSMutableArray alloc] init];
    
    [dataSource addObject:@"X"];
    [dataSource addObject:@"Y"];
    [dataSource addObject:@"Z"];
    [dataSource addObject:@"Magnitude"];
    
    [self loadPrefs];
    
    self.navigationItem.rightBarButtonItem = doneButton;
    
    self.navigationItem.title = @"Record Settings";
    
    
}

- (void) viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self willRotateToInterfaceOrientation:self.interfaceOrientation duration:0];
}

- (void) loadPrefs {
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    BOOL x = [prefs boolForKey:@"X"];
    BOOL y = [prefs boolForKey:@"Y"];
    BOOL z = [prefs boolForKey:@"Z"];
    BOOL mag = [prefs boolForKey:@"Magnitude"];
    
    if (x)
        [selectedMarks addObject:@"X"];
    if (y)
        [selectedMarks addObject:@"Y"];
    if (z)
        [selectedMarks addObject:@"Z"];
    if (mag)
        [selectedMarks addObject:@"Magnitude"];
    
    
}

- (void) done {
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    [prefs setBool:[selectedMarks containsObject:@"X"] forKey:@"X"];
    [prefs setBool:[selectedMarks containsObject:@"Y"] forKey:@"Y"];
    [prefs setBool:[selectedMarks containsObject:@"Z"] forKey:@"Z"];
    [prefs setBool:[selectedMarks containsObject:@"Magnitude"] forKey:@"Magnitude"];
    
    [self.navigationController popViewControllerAnimated:YES];
    
    
    
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
    return 4;
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

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *text = [dataSource objectAtIndex:[indexPath row]];
    
    if ([selectedMarks containsObject:text])// Is selected?
        [selectedMarks removeObject:text];
    else
        [selectedMarks addObject:text];
    
    [tableView reloadRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
}

@end
