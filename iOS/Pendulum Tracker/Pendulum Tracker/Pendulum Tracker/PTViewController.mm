//
//  PTViewController.m
//  Pendulum Tracker
//
//  Created by Virinchi Balabhadrapatruni on 8/28/13.
//  Copyright (c) 2013 ECG. All rights reserved.
//

#import "PTViewController.h"
#import <AVFoundation/AVCaptureSession.h>
#import <AVFoundation/AVCaptureVideoPreviewLayer.h>
#import <AVFoundation/AVCaptureDevice.h>
#import <AVFoundation/AVCaptureInput.h>
#import <AVFoundation/AVFoundation.h>

@interface PTViewController ()

@end

@implementation PTViewController

@synthesize cameraView, menuButtons, videoCamera;

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    self.videoCamera = [[CvVideoCamera alloc] initWithParentView:cameraView];
    self.videoCamera.defaultAVCaptureDevicePosition = AVCaptureDevicePositionBack;
    self.videoCamera.defaultAVCaptureSessionPreset = AVCaptureSessionPreset352x288;
    self.videoCamera.defaultAVCaptureVideoOrientation = AVCaptureVideoOrientationLandscapeRight;
    self.videoCamera.defaultFPS = 30;
    self.videoCamera.grayscaleMode = YES;
    self.videoCamera.delegate = self;
    [self.videoCamera start];
    AVCaptureSession *session = [self.videoCamera captureSession];
	session.sessionPreset = AVCaptureSessionPresetMedium;
    
    NSLog(@"NULLFROGS");
	
	AVCaptureVideoPreviewLayer *previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:session];
    CGRect screenRect = cameraView.bounds;
    previewLayer.frame = screenRect; // Assume you want the preview layer to fill the view.
    [cameraView.layer addSublayer:previewLayer];
    
    NSLog(@"NULLTOADS");

	
	AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
	
	NSError *error = nil;
	AVCaptureDeviceInput *input = [AVCaptureDeviceInput deviceInputWithDevice:device error:&error];
	if (!input) {
		// Handle the error appropriately.
		NSLog(@"ERROR: trying to open camera: %@", error);
	}
    
    NSLog(@"NULLTOADFROGS");
    
	//[session addInput:input];
    
    NSLog(@"NULLFROGTOADS");
	
	[session startRunning];
    
    NSLog(@"NULLFROGTOADFROGS");
    
    lbl1.textColor = [UIColor redColor];
    lbl2.textColor = [UIColor redColor];
    
    NSLog(@"NULLNULL");
    
    UIBarButtonItem *instruct = [[UIBarButtonItem alloc]
                               initWithTitle:@"Instructions"
                               style:UIBarButtonItemStylePlain
                               target:self
                               action:@selector(instructions)];
    
    UIBarButtonItem *start = [[UIBarButtonItem alloc]
                               initWithTitle:@"Start"
                               style:UIBarButtonItemStylePlain
                               target:self
                               action:@selector(startStop)];
    
    menuButtons = [[NSMutableArray alloc] initWithObjects:start,instruct, nil];
    self.navigationItem.rightBarButtonItems = menuButtons;
    self.navigationItem.title = @"iSENSE Pendulum Tracker";
    
    NSLog(@"NULLBULL");
    
    NSString *faceCascadePath = [[NSBundle mainBundle] pathForResource:kFaceCascadeName
                                                               ofType:@"xml"];
    
    
    
#ifdef __cplusplus
    if(!face_cascade.load([faceCascadePath UTF8String])) {
        NSLog(@"Could not load face classifier!");
    }
#endif
    
    
}

#pragma mark - Protocol CvVideoCameraDelegate

#ifdef __cplusplus
- (void)processImage:(cv::Mat&)image;
{
    // Do some OpenCV stuff with the image
    cv::vector<cv::Rect> faces;
    cv::Mat frame_gray;
    
    cvtColor(image, frame_gray, CV_BGRA2GRAY);
    equalizeHist(frame_gray, frame_gray);
    
    face_cascade.detectMultiScale(frame_gray, faces, 1.1, 2, 0 | CV_HAAR_SCALE_IMAGE, cv::Size(100, 100));
    
    for(unsigned int i = 0; i < faces.size(); ++i) {
        rectangle(image, cv::Point(faces[i].x, faces[i].y),
                  cv::Point(faces[i].x + faces[i].width, faces[i].y + faces[i].height),
                  cv::Scalar(0,255,255));
    }

}
#endif

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    if (interfaceOrientation==UIInterfaceOrientationLandscapeLeft || interfaceOrientation==UIInterfaceOrientationLandscapeRight)
        return YES;
    
    return NO;
}

- (void) startStop {
    
}

- (void) instructions {
    UIAlertView *instructAlert = [[UIAlertView alloc] initWithTitle:@"Instructions" message:[StringGrabber grabString:@"instructions"]  delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
    [instructAlert show];
}

@end
