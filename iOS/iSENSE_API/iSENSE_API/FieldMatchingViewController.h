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

@interface FieldMatchingViewController : UIViewController <UIPickerViewDelegate> {
    // bundle for resource files in the iSENSE_API_Bundle
    NSBundle *isenseBundle;
    
    // to hold FieldEntry objects
    NSMutableArray *entries;
    
    // pickerview for changing fields
    BOOL isShowingPickerView;
    UIPickerView *fieldPickerView;
    int fieldTag;
}

- (id) initWithUserFields:(NSMutableArray *)uf andProjectFields:(NSMutableArray *)pf;

- (IBAction) backOnClick:(id)sender;
- (IBAction) okOnClick:(id)sender;

@property (nonatomic, assign) IBOutlet UITableView *mTableView;
@property (nonatomic, assign) IBOutlet UIButton *back;
@property (nonatomic, assign) IBOutlet UIButton *ok;
@property (nonatomic, strong) NSMutableArray *userFields;
@property (nonatomic, strong) NSMutableArray *projFields;

@end
