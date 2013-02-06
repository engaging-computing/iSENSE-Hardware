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

@interface ExperimentBrowseViewController : UIViewController <UISearchBarDelegate> {
    iSENSE *isenseAPI;
    UIScrollView *scrollView;
    UIActivityIndicatorView *spinner;
    NSThread *loadExperimentThread;
}

- (IBAction)onExperimentButtonClicked:(id)caller;
- (void) updateScrollView:(ISenseSearch *)iSS;

@property (nonatomic, assign) int currentPage;


@end

