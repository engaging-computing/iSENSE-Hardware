//
//  ExperimentBrowseViewController.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 1/28/13.
//
//

#import <UIKit/UIKit.h>
#import "ExperimentBlock.h"
#import "ISenseSearch.h"

@interface ExperimentBrowseViewController : UIViewController <UISearchBarDelegate, UIScrollViewDelegate> {
    iSENSE *isenseAPI;
    UIScrollView *scrollView;
    UIActivityIndicatorView *spinner;
    NSThread *loadExperimentThread;
    UIView *experimentInfo;
}

- (IBAction)onExperimentButtonClicked:(id)caller;
- (void) updateScrollView:(ISenseSearch *)iSS;

@property (nonatomic, assign) int currentPage;
@property (nonatomic, assign) NSString *currentQuery;
@property (nonatomic, assign) int scrollHeight;
@property (nonatomic, assign) int contentHeight;
@property (nonatomic, assign) ExperimentBlock *lastExperimentClicked;

@end

