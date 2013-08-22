//
//  DWDetailViewController.h
//  Data_Walk
//
//  Created by Michael Stowell on 8/22/13.
//  Copyright (c) 2013 iSENSE. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DWDetailViewController : UIViewController <UISplitViewControllerDelegate>

@property (strong, nonatomic) id detailItem;

@property (weak, nonatomic) IBOutlet UILabel *detailDescriptionLabel;
@end
