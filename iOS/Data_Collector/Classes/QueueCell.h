//
//  QueueCell.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 7/2/13.
//
//

#import <UIKit/UIKit.h>

@interface QueueCell : UITableViewCell

- (QueueCell *)setupCellName:(NSString *)nameAndDate andDataType:(NSString *)type andDescription:(NSString *)description andUploadable:(bool)uploadable;

@property (nonatomic, assign) IBOutlet UILabel *nameAndDate;
@property (nonatomic, assign) IBOutlet UILabel *dataType;
@property (nonatomic, assign) IBOutlet UILabel *description;
@property (nonatomic, assign) IBOutlet UISwitch *uploadable;

@end
