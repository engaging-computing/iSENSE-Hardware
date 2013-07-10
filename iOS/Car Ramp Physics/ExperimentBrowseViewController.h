//
//  ExperimentBrowseViewController.h
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/28/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>
#import "ExperimentBlock.h"
#import "ISenseSearch.h"
#import "StringGrabber.h"
#import "Constants.h"
#import "iSENSE.h"

@interface ExperimentBrowseViewController : UIViewController <UISearchBarDelegate, UIScrollViewDelegate> {
    iSENSE *isenseAPI;
    UIScrollView *scrollView;
    UIActivityIndicatorView *experimentSpinner;
    UIActivityIndicatorView *experimentInfoSpinner;
    NSThread *loadExperimentThread;
    NSThread *experimentInfoThread;
    UIView *experimentInfo;
    UIButton *chooseExperiment;
    UISearchBar *searchBar;
    UITextView *additionalInfo;
    UIImageView *imageView;
    UILabel *experimentTitle;
    UIView *bottomSpinnerBlock;
}

- (IBAction) onExperimentButtonClicked:(id)caller;
- (void) updateScrollView:(ISenseSearch *)iSS;
- (void) loadExperimentInfomationForIPad;

@property (nonatomic, assign) int currentPage;
@property (nonatomic, retain) NSString *currentQuery;
@property (nonatomic, assign) int scrollHeight;
@property (nonatomic, assign) int *chosenExperiment;
@property (nonatomic, assign) int contentHeight;
@property (nonatomic, retain) ExperimentBlock *lastExperimentClicked;

@end

