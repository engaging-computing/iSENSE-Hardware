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
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    int exp = [[prefs stringForKey:[StringGrabber grabString:@"key_exp_automatic"]] integerValue];
    
    dfm = [[DataFieldManager alloc] init];
    [dfm getFieldOrderOfExperiment:exp];
    fieldNumber = [[dfm order] count];
    
    fieldNames = [[NSMutableArray alloc] init];
    selectedCells = [[NSMutableArray alloc] init];
    
    for (NSString *s in [dfm order]) {
        [fieldNames addObject:s];
        [selectedCells addObject:[NSNumber numberWithInt:1]];
    }
    
    compatible = [[NSMutableArray alloc] init];
    
    SensorCompatibility *sc = [[SensorCompatibility alloc] init];
    int gps     = [sc getCompatibilityForSensorType:sGPS];
    int accel   = [sc getCompatibilityForSensorType:sACCELEROMETER];
    int light   = [sc getCompatibilityForSensorType:sAMBIENT_LIGHT];
    int gyro    = [sc getCompatibilityForSensorType:sGYROSCOPE];
    //int proxi   = [sc getCompatibilityForSensorType:sPROXIMITY];
    
    for (NSString *s in fieldNames) {
        // Fields determined from the SensorCompatibility object
        if ([s isEqualToString:[StringGrabber grabField:@"latitude"]] || [s isEqualToString:[StringGrabber grabField:@"longitude"]]) {
            [compatible addObject:[NSNumber numberWithInt:gps]];
        } else if ([[s lowercaseString] rangeOfString:@"accel"].location != NSNotFound) {
            [compatible addObject:[NSNumber numberWithInt:accel]];
        } else if ([s isEqualToString:[StringGrabber grabField:@"luminous_flux"]]) {
            [compatible addObject:[NSNumber numberWithInt:light]];
        } else if ([[s lowercaseString] rangeOfString:@"gyro"].location != NSNotFound) {
            [compatible addObject:[NSNumber numberWithInt:gyro]];
        } else if ([s isEqualToString:[StringGrabber grabField:@"altitude"]]) {
            // altitude can be calculated as long as the GPS is enabled
            [compatible addObject:[NSNumber numberWithInt:gps]];
        }
        
        // Fields determined elsewise
        else if ([s isEqualToString:[StringGrabber grabField:@"time"]]) {
            [compatible addObject:[NSNumber numberWithInt:AVAILABLE]];
        } else if ([[s lowercaseString] rangeOfString:@"heading"].location != NSNotFound) {
            [compatible addObject:([CLLocationManager headingAvailable] ? [NSNumber numberWithInt:AVAILABLE] : [NSNumber numberWithInt:NOT_AVAILABLE])];
        } else if ([[s lowercaseString] rangeOfString:@"magnet"].location != NSNotFound) {
            [compatible addObject:([CLLocationManager headingAvailable] ? [NSNumber numberWithInt:AVAILABLE] : [NSNumber numberWithInt:NOT_AVAILABLE])];
        } else if ([s isEqualToString:[StringGrabber grabField:@"pressure"]]) {
            // it appears no iOS device ships with a barometer - current apps calculate air pressure using gps to get location and getting weather conditions at that ground location
            [compatible addObject:[NSNumber numberWithInt:NOT_AVAILABLE]];
        } else if ([[s lowercaseString] rangeOfString:@"temper"].location != NSNotFound) {
            // there is currently no ambient temperature sensor available, only internal temperature sensors
            [compatible addObject:[NSNumber numberWithInt:NOT_AVAILABLE]];
        } else {
            [compatible addObject:[NSNumber numberWithInt:NOT_AVAILABLE]];
        }
    }
    
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
        UIViewController *tempController;
        if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
            tempController = [[UIViewController alloc] initWithNibName:@"SensorCell~ipad" bundle:nil];
        } else {
            tempController = [[UIViewController alloc] initWithNibName:@"SensorCell~iphone" bundle:nil]; 
        }
        
        cell = (SensorCell *)tempController.view;
        [tempController release];
    }

    [cell setupCellWith:[fieldNames objectAtIndex:indexPath.row] andCompatability:[[compatible objectAtIndex:indexPath.row] integerValue]];
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView reloadData];
    SensorCell *cell = (SensorCell *)[tableView cellForRowAtIndexPath:indexPath];
    [cell setBackgroundColor:[UIColor lightGrayColor]];
    [NSThread sleepForTimeInterval:0.08];
    [cell setBackgroundColor:[UIColor clearColor]];
    
    NSLog(@"@@@@@@@@@@@@@@@@@@");
    NSNumber *newVal = [selectedCells objectAtIndex:indexPath.row];
    [selectedCells replaceObjectAtIndex:indexPath.row withObject:([newVal integerValue] == 1) ? [NSNumber numberWithInt:0] : [NSNumber numberWithInt:1]];
    for (NSNumber *n in selectedCells) {
        [cell setEnabled:([n integerValue] == 1) ? true : false];
    }
    //[cell swapLogoEnabled];
}

@end
