//
//  QueueUploaderView.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 6/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AppDelegate.h"
#import "Constants.h"
#import "QueueCell.h"
#import "ProjectBrowseViewController.h"
#import "Waffle.h"
#import "StringGrabber.h"
#import <iSENSE_API/DataFieldManager.h>
#import <DataSaver.h>

@interface QueueUploaderView : UIViewController <UITextFieldDelegate, UIActionSheetDelegate, UIAlertViewDelegate, UIGestureRecognizerDelegate> {
    
}

-(IBAction)upload:(id)sender;

@property (nonatomic, retain) DataSaver *dataSaver;
@property (nonatomic, assign) IBOutlet UITableView *mTableView;
@property (assign) int currentIndex;
@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;
@property (nonatomic) NSMutableArray *selectedMarks;
@property (nonatomic) NSMutableArray *dataSource;
@property (nonatomic, assign) API *iapi;
@property (nonatomic, retain) UIBarButtonItem *edit;
@property (nonatomic, retain) NSIndexPath *lastClickedCellIndex;
@property (nonatomic, retain) IBOutlet UIButton *uploadButton;

@end
