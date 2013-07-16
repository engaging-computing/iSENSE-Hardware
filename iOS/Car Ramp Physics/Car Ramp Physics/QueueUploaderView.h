//
//  QueueUploaderView.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 6/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AppDelegate.h"
#import "QueueCell.h"
#import <DataSaver.h>

@interface QueueUploaderView : UIViewController {
    
}

-(IBAction)upload:(id)sender;

@property (nonatomic, retain) DataSaver *dataSaver;
@property (nonatomic, assign) IBOutlet UITableView *mTableView;
@property (assign) int currentIndex;
@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;
@property(nonatomic) NSMutableArray *selectedMarks;
@property(nonatomic) NSMutableArray *dataSource;

@end
