//
//  SensorCell.h
//  Data_Collector
//
//  Created by Michael Stowell on 7/2/13.
//
//

#import <UIKit/UIKit.h>
#import <iSENSE_API/SensorEnums.h>

@interface SensorCell : UITableViewCell

- (SensorCell *) setupCellWithName:(NSString *)name compatability:(int)compat andEnabled:(bool)en;

@property (nonatomic, retain) IBOutlet UILabel      *field;
@property (nonatomic, retain) IBOutlet UILabel      *compatible;
@property (nonatomic, retain) IBOutlet UIImageView  *image;

@end
