//
//  ExperimentBrowseViewController.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 1/28/13.
//
//

#import <UIKit/UIKit.h>
#import "ExperimentBlock.h"

@interface ExperimentBrowseViewController : UIViewController <UISearchBarDelegate> {
    iSENSE *isenseAPI;
    UIScrollView *scrollView;
    UIActivityIndicatorView *spinner;
}

- (IBAction)onExperimentButtonClicked:(id)caller;


@end
