//
//  PWAppDelegate.h
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 1/9/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <DataSaver.h>

@class PWViewController;

@interface PWAppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;

@property (strong, nonatomic) PWViewController *viewController;

@property (nonatomic, strong) IBOutlet DataSaver *dataSaver;

// Core Data Stuffs
@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong) NSManagedObjectModel *managedObjectModel;
@property (nonatomic, strong) NSPersistentStoreCoordinator *persistentStoreCoordinator;

@end
