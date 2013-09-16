//
//  QueueUploaderView.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 6/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DWAppDelegate.h"
#import "QueueCell.h"
#import <iSENSE_API/headers/DataSaver.h>
#import <iSENSE_API/API.h>

#import "QueueConstants.h"
#import "ProjectBrowseViewController.h"
#import <iSENSE_API/Waffle.h>

#import <AVFoundation/AVFoundation.h>
#import <AVFoundation/AVCaptureDevice.h>

@interface QueueUploaderView : UIViewController <UIGestureRecognizerDelegate, UIActionSheetDelegate, UITextFieldDelegate, ProjectBrowseViewControllerDelegate> {
    int projID;
}

- (IBAction) upload:(id)sender;

- (void) handleLongPressOnTableCell:(UILongPressGestureRecognizer *)gestureRecognizer;
- (BOOL) handleNewQRCode:(NSURL *)url;
- (id)   initWithParentName:(NSString *)parentName;

@property (nonatomic, assign) API *api;
@property (nonatomic, copy)   NSString *parent;
@property (nonatomic, strong) DataSaver *dataSaver;
@property (nonatomic, assign) IBOutlet UITableView *mTableView;
@property (assign)            int currentIndex;
@property (nonatomic, strong) NSIndexPath *lastClickedCellIndex;
@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong) NSMutableDictionary *limitedTempQueue;

@end
