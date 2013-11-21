//
//  FieldMatchingViewController.m
//  iSENSE_API
//
//  Created by Michael Stowell on 11/14/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "FieldMatchingViewController.h"

@interface FieldMatchingViewController ()

@end

@implementation FieldMatchingViewController

@synthesize userFields, projFields, mTableView;

// displays the correct xib based on orientation and device type - called automatically upon view controller entry
-(void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if([UIDevice currentDevice].userInterfaceIdiom==UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [isenseBundle loadNibNamed:@"FieldMatching-landscape~ipad"
                                 owner:self
                               options:nil];
            [self viewDidLoad];
        } else {
            [isenseBundle loadNibNamed:@"FieldMatching~ipad"
                                 owner:self
                               options:nil];
            [self viewDidLoad];
        }
    } else {
        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
            [isenseBundle loadNibNamed:@"FieldMatching-landscape~iphone"
                                 owner:self
                               options:nil];
            [self viewDidLoad];
        } else {
            [isenseBundle loadNibNamed:@"FieldMatching~iphone"
                                 owner:self
                               options:nil];
            [self viewDidLoad];
        }
    }
}

// Allows the device to rotate as necessary.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

// iOS6 enable rotation
- (BOOL)shouldAutorotate {
    return YES;
}

// iOS6 enable rotation
- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

-(void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:YES];
    
    // Autorotate
    [self willRotateToInterfaceOrientation:(self.interfaceOrientation) duration:0];
}

- (id) initWithUserFields:(NSMutableArray *)uf andProjectFields:(NSMutableArray *)pf {
    self = [super init];
    if (self) {
        userFields = uf;
        projFields = pf;
        isenseBundle = [NSBundle bundleWithURL:[[NSBundle mainBundle] URLForResource:@"iSENSE_API_Bundle" withExtension:@"bundle"]];
        entries = [[NSMutableArray alloc] init];
        
        for (int i = 0; i < [uf count]; i++) {
            FieldEntry *fe = [[FieldEntry alloc] init];
            fe->uField = [uf objectAtIndex:i];
            fe->mField = [pf objectAtIndex:i];
            [entries addObject:fe];
        }
        NSLog(@"initialized");
    }
    return self;

}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.

    mTableView.backgroundColor = [UIColor clearColor];
    mTableView.backgroundView = nil;
    
    fieldPickerView = [[UIPickerView alloc] init];
    fieldPickerView.delegate = self;
    fieldPickerView.showsSelectionIndicator = YES;
    
    NSLog(@"loaded");
    
    [self.navigationItem setHidesBackButton:YES];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction) backOnClick:(id)sender {
    // send back nil to show that the user cancelled
    [[NSNotificationCenter defaultCenter] postNotificationName:kFIELD_MATCHED_ARRAY object:nil];
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction) okOnClick:(id)sender {
    // send back the field matched array
    NSMutableArray *fieldMatch = [[NSMutableArray alloc] init];
    for (FieldEntry *fe in entries)
        [fieldMatch addObject:fe->mField];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:kFIELD_MATCHED_ARRAY object:fieldMatch];
    [self.navigationController popViewControllerAnimated:YES];
}

// There is a single column in this table
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

// How many rows in this table view (the length of the array of fields)
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSLog(@"FieldMatch: %d fields", [userFields count]);
    return [userFields count];
}

// Initialize a single object in the table
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSLog(@"makin cells");
    static NSString *cellIndetifier = @"FieldMatchCellIdentifier";
    FieldMatchCell *cell = (FieldMatchCell *)[tableView dequeueReusableCellWithIdentifier:cellIndetifier];
    if (cell == nil) {
        UIViewController *tmpVC = [[UIViewController alloc] initWithNibName:@"FieldMatchCell" bundle:isenseBundle];
        cell = (FieldMatchCell *) tmpVC.view;
    }
    NSLog(@"      in prep...");
    FieldEntry *entry = [entries objectAtIndex:indexPath.row];
    [cell setupCellWithName:entry->uField andMatch:entry->mField];
    NSLog(@"      prepped!");
    return cell;
}

// If a table cell is clicked
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView reloadData];
    FieldMatchCell *cell = (FieldMatchCell *)[tableView cellForRowAtIndexPath:indexPath];
    lastClickedCellIndex = [indexPath copy];
    
    [NSThread sleepForTimeInterval:0.05];
    [cell setBackgroundColor:[UIColor clearColor]];
    
    [self selectNewFieldForCellAtIndex:indexPath.row];
}

- (void) selectNewFieldForCellAtIndex:(int)i {
    if (isShowingPickerView && fieldPickerView != nil) {
        [fieldPickerView removeFromSuperview];
        isShowingPickerView = NO;
        NSString *newFieldMatch;
        
        // Set button text according to the selected recording interval
        switch (fieldTag) {
            case 1:
                newFieldMatch = sACCEL_X;
                break;
            case 2:
                newFieldMatch = sACCEL_Y;
                break;
            case 3:
                newFieldMatch = sACCEL_Z;
                break;
            case 4:
                newFieldMatch = sACCEL_TOTAL;
                break;
            case 5:
                newFieldMatch = sTEMPERATURE_C;
                break;
            case 6:
                newFieldMatch = sTEMPERATURE_F;
                break;
            case 7:
                newFieldMatch = sTEMPERATURE_K;
                break;
            case 8:
                newFieldMatch = sTIME_MILLIS;
                break;
            case 9:
                newFieldMatch = sLUX;
                break;
            case 10:
                newFieldMatch = sANGLE_DEG;
                break;
            case 11:
                newFieldMatch = sANGLE_RAD;
                break;
            case 12:
                newFieldMatch = sLATITUDE;
                break;
            case 13:
                newFieldMatch = sLONGITUDE;
                break;
            case 14:
                newFieldMatch = sMAG_X;
                break;
            case 15:
                newFieldMatch = sMAG_Y;
                break;
            case 16:
                newFieldMatch = sMAG_Z;
                break;
            case 17:
                newFieldMatch = sMAG_TOTAL;
                break;
            case 18:
                newFieldMatch = sALTITUDE;
                break;
            case 19:
                newFieldMatch = sPRESSURE;
                break;
            case 20:
                newFieldMatch = sGYRO_X;
                break;
            case 21:
                newFieldMatch = sGYRO_Y;
                break;
            case 22:
                newFieldMatch = sGYRO_Z;
                break;
            
        }
        
        NSLog(@"New field: %@", newFieldMatch);
        // update cell and re-draw table: TODO
        
    } else {
        
        fieldTag = i;
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                        message:nil
                                                       delegate:self
                                              cancelButtonTitle:nil
                                              otherButtonTitles:@"Done", nil];
        [alert setAlertViewStyle:UIAlertViewStylePlainTextInput];
        alertText = [alert textFieldAtIndex:0];
        alertText.inputView = fieldPickerView;
        [alert show];
    }
}

- (void) alertView:(UIAlertView *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    // update the arrays of data and re-draw the scrollview, then field matching is done
    FieldEntry *fe = [entries objectAtIndex:fieldTag];
    fe->mField = fieldName;
    [entries setObject:fe atIndexedSubscript:fieldTag];
    
    // re-draw the field cell
    FieldMatchCell *cell = (FieldMatchCell *) [self.mTableView cellForRowAtIndexPath:lastClickedCellIndex];
    [cell setMatch:fieldName];
}


// Called every time the recording interval selector stops on a new row
- (void)pickerView:(UIPickerView *)pickerView didSelectRow: (NSInteger)row inComponent:(NSInteger)component {
    // Handle the selection
    NSString *title = @"";
    switch (row) {
        case 0:title  = @"";            break;  case 1:title  = sACCEL_X;       break;  case 2:title  = sACCEL_Y; break;
        case 3:title  = sACCEL_Z;       break;  case 4:title  = sACCEL_TOTAL;   break;  case 5:title  = sTEMPERATURE_C; break;
        case 6:title  = sTEMPERATURE_F; break;  case 7:title  = sTEMPERATURE_K; break;  case 8:title  = sTIME_MILLIS; break;
        case 9:title  = sLUX;           break;  case 10:title = sANGLE_DEG;     break;  case 11:title = sANGLE_RAD; break;
        case 12:title = sLATITUDE;      break;  case 13:title = sLONGITUDE;     break;  case 14:title = sMAG_X; break;
        case 15:title = sMAG_Y;         break;  case 16:title = sMAG_Z;         break;  case 17:title = sMAG_TOTAL; break;
        case 18:title = sALTITUDE;      break;  case 19:title = sPRESSURE;      break;  case 20:title = sGYRO_X; break;
        case 21:title = sGYRO_Y;        break;  case 22:title = sGYRO_Z;        break;
    }
    [alertText setText:title];
    fieldName = title;
}

// Tells the picker how many rows are available for a given component - we have 7 recording interval options
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return 23;
}

// Tells the picker how many components it will have - 1, since we only want to display a single interval per row
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

// Assigns the picker a title for each row - a "Return to previous" selection, and the 6 other intervals
- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    NSString *title;
    switch (row) {
        case 0:
            title = @"";
            return title;
        case 1:
            title = sACCEL_X;
            return title;
        case 2:
            title = sACCEL_Y;
            return title;
        case 3:
            title = sACCEL_Z;
            return title;
        case 4:
            title = sACCEL_TOTAL;
            return title;
        case 5:
            title = sTEMPERATURE_C;
            return title;
        case 6:
            title = sTEMPERATURE_F;
            return title;
        case 7:
            title = sTEMPERATURE_K;
            return title;
        case 8:
            title = sTIME_MILLIS;
            return title;
        case 9:
            title = sLUX;
            return title;
        case 10:
            title = sANGLE_DEG;
            return title;
        case 11:
            title = sANGLE_RAD;
            return title;
        case 12:
            title = sLATITUDE;
            return title;
        case 13:
            title = sLONGITUDE;
            return title;
        case 14:
            title = sMAG_X;
            return title;
        case 15:
            title = sMAG_Y;
            return title;
        case 16:
            title = sMAG_Z;
            return title;
        case 17:
            title = sMAG_TOTAL;
            return title;
        case 18:
            title = sALTITUDE;
            return title;
        case 19:
            title = sPRESSURE;
            return title;
        case 20:
            title = sGYRO_X;
            return title;
        case 21:
            title = sGYRO_Y;
            return title;
        case 22:
            title = sGYRO_Z;
            return title;
    }
    return title;
}

// Tells the picker the width of each row for a given component
- (CGFloat)pickerView:(UIPickerView *)pickerView widthForComponent:(NSInteger)component {
    int sectionWidth = 300;
    return sectionWidth;
}

@end
