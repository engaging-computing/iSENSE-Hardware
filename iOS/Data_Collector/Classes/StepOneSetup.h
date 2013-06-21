//
//  StepOneSetup.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 06/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>

@interface StepOneSetup : UIViewController {
    
}

@property (nonatomic, retain) IBOutlet UITextField *sessionName;
@property (nonatomic, retain) IBOutlet UITextField *sampleInterval;
@property (nonatomic, retain) IBOutlet UITextField *testLength;
@property (nonatomic, retain) IBOutlet UISwitch    *rememberMe;
@property (nonatomic, retain) IBOutlet UIButton    *selectExp;
@property (nonatomic, retain) IBOutlet UISwitch    *selectLater;
@property (nonatomic, retain) IBOutlet UIButton    *cancel;
@property (nonatomic, retain) IBOutlet UIButton    *ok;

@end


