//
//  Data_CollectorAppDelegate.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 12/28/12.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>
#import <iSENSE_API/headers/DataSaver.h>
#import "Constants.h"

@interface Data_CollectorAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
	UINavigationController *navControl;
    
    // for QR codes
    UIViewController *lastController;
    int returnToClass;
}

- (void) setLastController:(UIViewController *)uivc;
- (void) setReturnToClass:(int)ret;

@property (nonatomic, strong) IBOutlet UIWindow *window;
@property (nonatomic, strong) IBOutlet UINavigationController *navControl;

// DataSaver
@property (nonatomic, strong) IBOutlet DataSaver *dataSaver;

// Core Data Stuffs
@property (nonatomic, strong, readonly) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong, readonly) NSManagedObjectModel *managedObjectModel;
@property (nonatomic, strong, readonly) NSPersistentStoreCoordinator *persistentStoreCoordinator;

@end

