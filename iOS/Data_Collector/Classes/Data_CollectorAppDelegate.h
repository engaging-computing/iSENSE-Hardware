//
//  Data_CollectorAppDelegate.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>
#import "AboutView.h"
#import "GuideView.h"
#import <iSENSE_API/headers/DataSaver.h>

#import "ManualViewController.h"
#import "StepOneSetup.h"


@interface Data_CollectorAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
	UINavigationController *navControl;
	UIBarButtonItem *about;
	UIBarButtonItem *guide;
    
    // for QR codes
    UIViewController *lastController;
    int returnToClass;
}

- (IBAction) showAbout:(id)sender;
- (IBAction) showGuide:(id)sender;
- (void) setLastController:(UIViewController *)uivc;
- (void) setReturnToClass:(int)ret;

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet UINavigationController *navControl;
@property (nonatomic, retain) IBOutlet UIBarButtonItem *about;
@property (nonatomic, retain) IBOutlet UIBarButtonItem *guide;

// DataSaver
@property (nonatomic, retain) IBOutlet DataSaver *dataSaver;

// Core Data Stuffs
@property (nonatomic, retain, readonly) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, retain, readonly) NSManagedObjectModel *managedObjectModel;
@property (nonatomic, retain, readonly) NSPersistentStoreCoordinator *persistentStoreCoordinator;

@end

