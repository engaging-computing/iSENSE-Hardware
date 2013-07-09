//
//  QueueCell.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 7/2/13.
//
//

#import "QueueCell.h"

@implementation QueueCell

@synthesize nameAndDate, dataType, description, uploadable;

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

- (QueueCell *)setupCellName:(NSString *)name andDataType:(NSString *)type andDescription:(NSString *)desc andUploadable:(bool)upload {
      
    self.nameAndDate.text = name;
    self.dataType.text = type;
    self.description.text = desc;
    
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        // do stuff
    }
    return self;
}


@end
