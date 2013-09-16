//
//  PTViewController.h
//  Pendulum Tracker
//
//  Created by Virinchi Balabhadrapatruni on 8/28/13.
//  Copyright (c) 2013 ECG. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <opencv2/opencv.hpp>
#import <opencv2/highgui/cap_ios.h>
#import "StringGrabber.h"
#import "Constants.h"




@interface PTViewController : UIViewController <UINavigationControllerDelegate, CvVideoCameraDelegate> {
    IBOutlet UILabel *lbl1, *lbl2;
#ifdef __cplusplus
    cv::CascadeClassifier face_cascade;
#endif
}

@property(nonatomic) IBOutlet UIView *cameraView;
@property(nonatomic) NSMutableArray *menuButtons;
@property(nonatomic,retain) CvVideoCamera *videoCamera;


@end
