//
//  SensorCell.m
//  Data_Collector
//
//  Created by Michael Stowell on 7/2/13.
//
//

#import "SensorCell.h"

@implementation SensorCell

@synthesize field, compatible, image, enabled;

/*- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {}
    return self;
}*/


- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        // init magic here
        self.contentView.backgroundColor = [UIColor clearColor];
        enabled = true;
        //self.contentView.backgroundColor = [UIColor lightGrayColor];
    }
    return self;
}


- (SensorCell *) setupCellWith:(NSString *)name {
    [field setText:name];
    // more magic here
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
}

- (void) swapLogoEnabled {
    if (!enabled)
        image.image = [UIImage imageNamed:@"waffle_check"];
    else
        image.image = [UIImage imageNamed:@"waffle_x"];
    
    enabled = !enabled;
}

@end
