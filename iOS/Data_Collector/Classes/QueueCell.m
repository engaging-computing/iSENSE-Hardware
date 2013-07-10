//
//  QueueCell.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 7/2/13.
//
//

#import "QueueCell.h"

@implementation QueueCell

@synthesize nameAndDate, dataType, description;

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

    [self setCheckedSwitch:upload];
    
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        // do stuff
    }
    return self;
}


-(void)setCheckedSwitch:(bool)checked {
    if (checked) {
        self.accessoryType = UITableViewCellAccessoryCheckmark;
    } else {
        self.accessoryType = UITableViewCellAccessoryNone;
    }
}

-(IBAction)setChecked:(UITapGestureRecognizer *)sender {
    if (self.accessoryType == UITableViewCellAccessoryNone) {
        [self setCheckedSwitch:true];
    } else {
        [self setCheckedSwitch:false];
    }
}

@end
