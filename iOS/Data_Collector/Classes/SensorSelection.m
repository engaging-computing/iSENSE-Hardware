//
//  SensorSelection.m
//  Data_Collector
//
//  Created by Michael Stowell on 7/2/13.
//
//

#import "SensorSelection.h"
#import "SensorCell.h"

@interface SensorSelection ()

@end

@implementation SensorSelection

@synthesize table, ok, fieldNumber;

-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"SensorSelection-landscape~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"SensorSelection~ipad"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [[NSBundle mainBundle] loadNibNamed:@"SensorSelection-landscape~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        } else {
            [[NSBundle mainBundle] loadNibNamed:@"SensorSelection~iphone"
                                          owner:self
                                        options:nil];
            [self viewDidLoad];
        }
    }
}


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {}
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
	
    // TODO - setup what to add in the tableview
    fieldNumber = 10;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidAppear:(BOOL)animated {
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
}

- (void) dealloc {
    [table release];
    [ok release];
    [super dealloc];
}

- (IBAction)okOnClick:(id)sender {
    //[self tableView:table cellForRowAtIndexPath:0];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return fieldNumber;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellIdentifier = @"SensorCellIdentifier";

    SensorCell *cell = (SensorCell *)[tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    
    if (cell == nil) {
        UIViewController *tempController = [[UIViewController alloc] initWithNibName:@"SensorCell~iphone" bundle:nil];
        cell = (SensorCell *)tempController.view;
        [tempController release];
    }
    
    // Configure the cells
    switch (indexPath.row) {
        case 0:
            [cell setupCellWith:@"FIRST!!!"];
            break;
        default:
            [cell setupCellWith:@"Rest!!!!!!!"];
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView reloadData];
    SensorCell *cell = (SensorCell *)[tableView cellForRowAtIndexPath:indexPath];
    [cell setBackgroundColor:[UIColor lightGrayColor]];
    [NSThread sleepForTimeInterval:0.12];
    [cell setBackgroundColor:[UIColor clearColor]];
    [cell swapLogoEnabled];
}

@end
