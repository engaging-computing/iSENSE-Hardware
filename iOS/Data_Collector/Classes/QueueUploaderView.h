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

#import <AVFoundation/AVFoundation.h>
#import <AVFoundation/AVCaptureDevice.h>

@interface QueueUploaderView : UIViewController <UIGestureRecognizerDelegate, UIActionSheetDelegate> {
    int expNum;
    bool browsing;
}

- (IBAction) upload:(id)sender;

- (void) handleLongPressOnTableCell:(UILongPressGestureRecognizer *)gestureRecognizer;
- (BOOL) handleNewQRCode:(NSURL *)url;

@property (nonatomic, assign) iSENSE *iapi;
@property (nonatomic, retain) DataSaver *dataSaver;
@property (nonatomic, assign) IBOutlet UITableView *mTableView;
@property (assign) int currentIndex;
@property (nonatomic, retain) NSIndexPath *lastClickedCellIndex;
@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;

@end
