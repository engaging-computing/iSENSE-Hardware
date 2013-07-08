//
//  QueueCell.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 7/2/13.
//
//

#import "QueueCell.h"

@implementation QueueCell

//- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
//    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
//    if (self) {
//        // Initialization code
//    }
//    return self;
//}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (QueueCell *)setupCellName:(NSString *)nameAndDate andDataType:(NSString *)type andDescription:(NSString *)description andUploadable:(bool)uploadable {
    QueueCell *cell = [[QueueCell alloc] init];
    
    NSLog(@"Name is: %@", nameAndDate);
    
    cell.nameAndDate.text = nameAndDate;
    cell.dataType.text = type;
    cell.description.text = description;
    
    return cell;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        // do stuff
    }
    return self;
}


@end
