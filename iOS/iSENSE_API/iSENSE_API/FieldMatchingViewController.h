//
//  FieldMatchingViewController.h
//  iSENSE_API
//
//  Created by Michael Stowell on 11/14/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface FieldMatchingViewController : UIViewController {
    // bundle for resource files in the iSENSE_API_Bundle
    NSBundle *isenseBundle;
}

- (id) initWithUserFields:(NSMutableArray *)uf;

- (IBAction) backOnClick:(id)sender;
- (IBAction) okOnClick:(id)sender;

@property (nonatomic, assign) IBOutlet UITableView *mTableView;
@property (nonatomic, assign) IBOutlet UIButton *back;
@property (nonatomic, assign) IBOutlet UIButton *ok;
@property (nonatomic, strong) NSMutableArray *userFields;

@end
