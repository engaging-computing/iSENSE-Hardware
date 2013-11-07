//
//  SelectModeViewController.h
//  Uploader
//
//  Created by Michael Stowell on 11/5/13.
//  Copyright (c) 2013 iSENSE. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SelectModeViewController : UIViewController {
    
}

// button click methods
- (IBAction) dataCollectorOnClick:(UIButton *)sender;
- (IBAction) manualEntryOnClick:(UIButton *)sender;

@property (nonatomic, strong) IBOutlet UIButton *dataCollector;
@property (nonatomic, strong) IBOutlet UIButton *manualEntry;

@end
