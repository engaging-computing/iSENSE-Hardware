//
//  ViewController.m
//  PictureTest
//
//  Created by Jeremy Poulin on 9/11/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    _api = [API getInstance];
    [_api useDev:TRUE];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(BOOL)startMediaBrowserFromViewController:(UIViewController*)controller
                             usingDelegate: (id <UIImagePickerControllerDelegate,
                                             UINavigationControllerDelegate>) delegate {
    
    if (([UIImagePickerController isSourceTypeAvailable:
          UIImagePickerControllerSourceTypeSavedPhotosAlbum] == NO)
        || (delegate == nil)
        || (controller == nil))
        return NO;
    
    UIImagePickerController *mediaUI = [[UIImagePickerController alloc] init];
    mediaUI.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
    
    // Displays saved pictures and movies, if both are available, from the
    // Camera Roll album.
    mediaUI.mediaTypes = [UIImagePickerController availableMediaTypesForSourceType:
                          UIImagePickerControllerSourceTypeSavedPhotosAlbum];
    
    // Hides the controls for moving & scaling pictures, or for
    // trimming movies. To instead show the controls, use YES.
    mediaUI.allowsEditing = NO;
    mediaUI.delegate = delegate;
    
    [controller presentViewController:mediaUI animated:YES completion:nil];
    
    return YES;
    
}

-(IBAction)browsePictures:(id)sender {
    [self startMediaBrowserFromViewController:self usingDelegate:self];
}

-(IBAction)upload:(id)sender {
    
}

-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    
    NSString *mediaType = [info objectForKey: UIImagePickerControllerMediaType];
    UIImage *originalImage, *editedImage, *imageToUse;
    
    // Handle a still image picked from a photo album
    if (CFStringCompare ((CFStringRef) mediaType, kUTTypeImage, 0) == kCFCompareEqualTo) {
        
        editedImage = (UIImage *) [info objectForKey:
                                   UIImagePickerControllerEditedImage];
        
        originalImage = (UIImage *) [info objectForKey:
                                     UIImagePickerControllerOriginalImage];
        
        if (editedImage) {
            imageToUse = editedImage;
        } else {
            imageToUse = originalImage;
        }
                
        // Do something with imageToUse
        NSURL *path = [info objectForKey:UIImagePickerControllerReferenceURL];
        
        NSString *imageName = [path lastPathComponent];
        
        NSData *imageFile = UIImageJPEGRepresentation(imageToUse, 1);
        NSLog(@"Original path %@", path.description);
        //[_api createSessionWithUsername:@"test" andPassword:@"test"];
        [_api uploadProjectMediaWithId:24 withFile:imageFile andName:imageName];
    }
    
    // Handle a movied picked from a photo album
    if (CFStringCompare ((CFStringRef) mediaType, kUTTypeMovie, 0)== kCFCompareEqualTo) {
       // NSString *moviePath = [[info objectForKey:UIImagePickerControllerMediaURL] path];
        
        // Do something with the picked movie available at moviePath
    }
    
    [[picker parentViewController] dismissViewControllerAnimated:YES completion:nil];
    
}

@end
