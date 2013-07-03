//
//  SensorCell.m
//  Data_Collector
//
//  Created by Michael Stowell on 7/2/13.
//
//

#import "SensorCell.h"

@implementation SensorCell

@synthesize field, compatible, image;

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
        //enabled = true;
        //self.contentView.backgroundColor = [UIColor lightGrayColor];
    }
    return self;
}


- (SensorCell *) setupCellWith:(NSString *)name andCompatability:(int)compat {
    [field setText:name];
    switch (compat) {
        case NOT_AVAILABLE:
            [compatible setText:@"Not Compatible"];
            [compatible setTextColor:[HexColor colorWithHexString:@"DE1F22"]];
            break;
            
        case AVAILABLE:
            [compatible setText:@"Compatible"];
            [compatible setTextColor:[HexColor colorWithHexString:@"5BED18"]];
            break;
            
        case AVAIL_CONNECTIVITY:
            [compatible setText:@"Compatible (with connectivity)"];
            [compatible setTextColor:[HexColor colorWithHexString:@"5BED18"]];
            break;
            
        case AVAIL_WIFI_ONLY:
            [compatible setText:@"Compatible (with WiFi enabled)"];
            [compatible setTextColor:[HexColor colorWithHexString:@"5BED18"]];
            break;
            
        case NOT_DETECTED:
            [compatible setText:@"Cannot determine compatibility"];
            [compatible setTextColor:[HexColor colorWithHexString:@"EDCD18"]];
            break;
    }
    
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
}

/*- (void) swapLogoEnabled {
    if (!enabled)
        image.image = [UIImage imageNamed:@"waffle_check"];
    else
        image.image = [UIImage imageNamed:@"waffle_x"];
    
    enabled = !enabled;
}*/

- (void) setEnabled:(bool)isEnabled {
    NSLog(@"lots of enable: %d", isEnabled);
    if (isEnabled)
        image.image = [UIImage imageNamed:@"waffle_check"];
    else
        image.image = [UIImage imageNamed:@"waffle_x"];
    
    [image setNeedsDisplay];
}

@end
