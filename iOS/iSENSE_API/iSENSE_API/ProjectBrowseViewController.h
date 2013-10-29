//
//  ProjectBrowseViewController.h
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/28/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <UIKit/UIKit.h>
#import <iSENSE_API/API.h>
#import <iSENSE_API/ISKeys.h>

#import "ProjectBlock.h"
#import "ISenseSearch.h"

#define kCHOOSE_PROJECT @"This is my project."

@class ProjectBrowseViewController;

@protocol ProjectBrowseViewControllerDelegate <NSObject>
@required
-(void)projectViewController:(ProjectBrowseViewController *)controller didFinishChoosingProject:(NSNumber *)project;
@end

@interface ProjectBrowseViewController : UIViewController <UISearchBarDelegate, UIScrollViewDelegate> {
    API *isenseAPI;
    UIScrollView *scrollView;
    UIActivityIndicatorView *projectSpinner;
    UIActivityIndicatorView *projectInfoSpinner;
    NSThread *loadProjectThread;
    NSThread *projectInfoThread;
    UIView *projectInfo;
    UIButton *chooseProject;
    UISearchBar *searchBar;
    UITextView *additionalInfo;
    UIImageView *imageView;
    UILabel *projectTitle;
    UIView *bottomSpinnerBlock;
}

- (void) onProjectButtonClicked:(id)caller;
- (void) updateScrollView:(ISenseSearch *)iSS;
- (void) loadProjectInfomationForIPad;

@property (nonatomic, assign) int currentPage;
@property (nonatomic, retain) NSString *currentQuery;
@property (nonatomic, assign) int scrollHeight;
@property (nonatomic, assign) int contentHeight;
@property (nonatomic, retain) ProjectBlock *lastProjectClicked;
@property (nonatomic, unsafe_unretained) id <ProjectBrowseViewControllerDelegate> delegate;

@end




