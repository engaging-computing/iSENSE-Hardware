//
//  VariablesViewController.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/15/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin

#import "CRTableViewCell.h"
#import <UIKit/UIKit.h>

@interface VariablesViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>


@property(nonatomic) NSMutableArray *dataSource;
@property(nonatomic) NSMutableArray *selectedMarks;
@property(nonatomic) IBOutlet UITableView *table;
@end
