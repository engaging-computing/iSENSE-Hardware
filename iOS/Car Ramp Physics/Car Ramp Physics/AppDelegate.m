//
//  AppDelegate.m
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/8/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin

#import "AppDelegate.h"
#import <iSENSE_API/Fields.h>
#import "ViewController.h"

@implementation AppDelegate

@synthesize  dataSaver, managedObjectContext,managedObjectModel,persistentStoreCoordinator;

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    // Override point for customization after application launch.
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        self.viewController = [[ViewController alloc] initWithNibName:@"ViewController_iPhone" bundle:nil];
    } else {
        self.viewController = [[ViewController alloc] initWithNibName:@"ViewController_iPad" bundle:nil];
    }    
    UINavigationController *navigation = [[UINavigationController alloc] initWithRootViewController:self.viewController];
    self.viewController.navigationItem.rightBarButtonItem = self.viewController.menuButton;
    navigation.navigationBar.barStyle = UIBarStyleBlackOpaque;
    self.window.rootViewController = navigation;
    self.viewController.setupDone = YES;
    [self.window makeKeyAndVisible];
    return YES;
}

- (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url
{
    NSLog(@"got barcode: %@", url);
    
    NSArray *arr = [[url absoluteString] componentsSeparatedByString:@"="];
    
    NSString *exp = arr[2];
    
    int expNum = [exp intValue];
    
    NSLog(@"ExpNum: %d", expNum);
    
    self.viewController.expNum = expNum;
    
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    if (self.viewController.running) {
        [self.viewController stopRecordingWithoutPublishing:self.viewController.motionmanager];
        [self.viewController.view setNeedsLayout];
        [self.viewController.view setNeedsDisplay];
        [self.viewController.view makeWaffle:@"Data recording halted" duration:WAFFLE_LENGTH_SHORT position:WAFFLE_BOTTOM image:WAFFLE_RED_X];
    }
}

- (void)applicationWillTerminate:(UIApplication *)application {
    
    NSError *error = nil;
    if (managedObjectContext != nil) {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error]) {
            /*
             Replace this implementation with code to handle the error appropriately.
             
             abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. If it is not possible to recover from the error, display an alert panel that instructs the user to quit the application by pressing the Home button.
             */
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        }
    }
    
    
}

#pragma mark -
#pragma mark Core Data stack

/**
 Returns the managed object context for the application.
 If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
 
- (NSManagedObjectContext *) managedObjectContext {
	
    if (managedObjectContext != nil) {
        return managedObjectContext;
    }
	
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil) {
        managedObjectContext = [[NSManagedObjectContext alloc] init];
        [managedObjectContext setPersistentStoreCoordinator: coordinator];
    }
    
    [self fetchDataSets];
    
    return managedObjectContext;
}


/**
 * Returns the managed object model for the application.
 *If the model doesn't already exist, it is created by merging all of the models found in the application bundle.
 
- (NSManagedObjectModel *)managedObjectModel {
    if (!managedObjectModel) {
        
        NSString *staticLibraryBundlePath = [[NSBundle mainBundle] pathForResource:@"iSENSE_API_Bundle" ofType:@"bundle"];
        NSURL *staticLibraryMOMURL = [[NSBundle bundleWithPath:staticLibraryBundlePath] URLForResource:@"DataSetModel" withExtension:@"momd"];
        managedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:staticLibraryMOMURL];
        NSLog(@"%@", managedObjectModel.entities.description);
        if (!managedObjectModel) {
            NSLog(@"Problem");
            abort();
        }
    }
    
    return managedObjectModel;
}


/**
 Returns the persistent store coordinator for the application.
 If the coordinator doesn't already exist, it is created and the application's store added to it.
 
- (NSPersistentStoreCoordinator *)persistentStoreCoordinator {
	
    if (persistentStoreCoordinator != nil) {
        return persistentStoreCoordinator;
    }
	
    NSURL *storeUrl = [NSURL fileURLWithPath: [[self applicationDocumentsDirectory] stringByAppendingPathComponent: @"CarPhysics.sqlite"]];
	
	NSError *error = nil;
    persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
    
    NSDictionary *options = [NSDictionary dictionaryWithObjectsAndKeys:
                             
                             [NSNumber numberWithBool:YES], NSMigratePersistentStoresAutomaticallyOption,
                             
                             [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption, nil];
    
    if (![persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeUrl options:options error:&error]) {
		/*
		 Replace this implementation with code to handle the error appropriately.
		 
		 abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. If it is not possible to recover from the error, display an alert panel that instructs the user to quit the application by pressing the Home button.
		 
		 Typical reasons for an error here include:
		 * The persistent store is not accessible
		 * The schema for the persistent store is incompatible with current managed object model
		 Check the error message to determine what the actual problem was.
		 
		NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
		abort();
    }
	
    return persistentStoreCoordinator;
}


#pragma mark -
#pragma mark Application's Documents directory

/**
 Returns the path to the application's Documents directory.
 
- (NSString *)applicationDocumentsDirectory {
	return [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
}


#pragma mark -
#pragma mark Memory management

- (void)applicationDidReceiveMemoryWarning:(UIApplication *)application {
}


#pragma mark -
#pragma mark Custom Functions

// Get the dataSets from the queue :D
- (void) fetchDataSets {
    
    
    // Fetch the old DataSets
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    NSEntityDescription *dataSetEntity = [NSEntityDescription entityForName:@"DataSet" inManagedObjectContext:managedObjectContext];
    if (dataSetEntity) {
        [request setEntity:dataSetEntity];
        
        // Actually make the request
        NSError *error = nil;
        NSMutableArray *mutableFetchResults = [[managedObjectContext executeFetchRequest:request error:&error] mutableCopy];
        if (mutableFetchResults == nil) {
            // dats a problem
        } else {
            NSLog(@"Description: %@, %d", mutableFetchResults.description, mutableFetchResults.count);
        }
        
        dataSaver = [[DataSaver alloc] initWithContext:managedObjectContext];
        
        // fill dataSaver's DataSet Queue
        for (int i = 0; i < mutableFetchResults.count; i++) {
            [dataSaver addDataSetFromCoreData:mutableFetchResults[i]];
        }
        
    }
}
*/
@end
