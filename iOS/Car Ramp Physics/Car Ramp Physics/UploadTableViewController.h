//
//  UploadTableViewController.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/13/13.
//  Copyright (c) 2013 ECG. All rights reserved.
//
#import "CRTableViewCell.h"
#import "ViewController.h"
#import "DataSaver.h"
#import "DataSet.h"
#import <UIKit/UIKit.h>

@interface UploadTableViewController : UITableViewController

@property(nonatomic) NSMutableArray *dataSource;
@property(nonatomic) NSMutableArray *selectedMarks;
@property(nonatomic, retain) DataSaver *saver;

@end
