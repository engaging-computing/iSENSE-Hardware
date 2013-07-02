//
//  QueueUploaderView.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 6/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Data_CollectorAppDelegate.h"
#import "QueueCell.h"
#import <iSENSE_API/headers/DataSaver.h>

@interface QueueUploaderView : UIViewController {
    
    DataSaver *dataSaver;
    
}

@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, assign) IBOutlet UITableView *mTableView;
@property (assign) int currentIndex;

@end
