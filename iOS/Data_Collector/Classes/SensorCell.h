//
//  SensorCell.h
//  Data_Collector
//
//  Created by Michael Stowell on 7/2/13.
//
//

#import <UIKit/UIKit.h>

@interface SensorCell : UITableViewCell

- (SensorCell *) setupCellWith:(NSString *)name;
- (void) swapLogoEnabled;

@property (nonatomic, retain) IBOutlet UILabel      *field;
@property (nonatomic, retain) IBOutlet UILabel      *compatible;
@property (nonatomic, retain) IBOutlet UIImageView  *image;
@property (nonatomic) bool enabled;

@end
