//
//  WelcomeViewController.h
//  Uploader
//
//  Created by Michael Stowell on 11/5/13.
//  Copyright (c) 2013 iSENSE. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface WelcomeViewController : UIViewController {
    
}

// button click methods
- (IBAction) continueWithProjOnClick:(UIButton *)sender;
- (IBAction) createNewProjOnClick:(UIButton *)sender;
- (IBAction) selectProjLaterOnClick:(UIButton *)sender;

@property (nonatomic, strong) IBOutlet UIButton *continueWithProj;
@property (nonatomic, strong) IBOutlet UIButton *createNewProj;
@property (nonatomic, strong) IBOutlet UIButton *selectProjLater;

@end
