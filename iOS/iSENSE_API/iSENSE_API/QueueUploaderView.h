//
//  QueueUploaderView.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 6/26/13.
//  Modified by Mike Stowell
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "DataSaver.h"
#import "API.h"
#import "ProjectBrowseViewController.h"
#import "Waffle.h"
#import "ISKeys.h"

#import "QueueConstants.h"
#import "QueueCell.h"

#import "DataFieldManager.h"
#import "FieldMatchingViewController.h"

#import <AVFoundation/AVFoundation.h>
#import <AVFoundation/AVCaptureDevice.h>

#define KEY_ATTEMPTED_UPLOAD    @"key_attempted_upload"

// Parent name constants: add a new one for each app
#define PARENT_AUTOMATIC    @"Automatic"
#define PARENT_MANUAL       @"Manual"
#define PARENT_DATA_WALK    @"DataWalk"
#define PARENT_CAR_RAMP     @"CarRampPhysics"
#define PARENT_CANOBIE      @"CanobiePhysics"

@interface QueueUploaderView : UIViewController <UIGestureRecognizerDelegate, UIActionSheetDelegate, UITextFieldDelegate, ProjectBrowseViewControllerDelegate> {
    int projID;
    
    // bundle for resource files in the iSENSE_API_Bundle
    NSBundle *isenseBundle;
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
