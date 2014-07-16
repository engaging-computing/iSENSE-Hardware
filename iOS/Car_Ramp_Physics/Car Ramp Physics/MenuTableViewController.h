//
//  MenuTableViewController.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 6/10/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ViewController.h"
#import <SlideNavigationController.h>
#import "AboutViewController.h"

@interface MenuTableViewController : UITableViewController

@property(nonatomic) NSMutableArray *section1, *section2, *section3, *section4, *sectionHeaders;
@property(nonatomic, strong) ViewController *parent;
@property(nonatomic) CGRect frame;

- (id) initWithParentViewController:(ViewController *)parent;

@end
