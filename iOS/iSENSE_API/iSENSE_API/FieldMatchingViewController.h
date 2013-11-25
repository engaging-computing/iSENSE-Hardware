//
//  FieldMatchingViewController.h
//  iSENSE_API
//
//  Created by Michael Stowell on 11/14/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FieldMatchCell.h"
#import "FieldEntry.h"
#import "Fields.h"

#define kFIELD_MATCHED_ARRAY @"field_matched_array"

@interface FieldMatchingViewController : UIViewController <UIPickerViewDelegate, UIAlertViewDelegate> {
    // bundle for resource files in the iSENSE_API_Bundle
    NSBundle *isenseBundle;
    
    // to hold FieldEntry objects
    NSMutableArray *entries;
    
    // last clicked cell
    NSIndexPath *lastClickedCellIndex;
    
    // pickerview for changing fields
    BOOL isShowingPickerView;
    UIPickerView *fieldPickerView;
    int fieldTag;
    NSString *fieldName;
    UITextField *alertText;
}

- (id) initWithMatchedFields:(NSMutableArray *)mf andProjectFields:(NSMutableArray *)pf;

- (IBAction) backOnClick:(id)sender;
- (IBAction) okOnClick:(id)sender;

@property (nonatomic, assign) IBOutlet UITableView *mTableView;
@property (nonatomic, assign) IBOutlet UIButton *back;
@property (nonatomic, assign) IBOutlet UIButton *ok;
@property (nonatomic, strong) NSMutableArray *matchFields;
@property (nonatomic, strong) NSMutableArray *projFields;

@end
