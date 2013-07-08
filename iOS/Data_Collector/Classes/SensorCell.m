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

- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        self.contentView.backgroundColor = [UIColor clearColor];
    }
    return self;
}

- (SensorCell *) setupCellWithName:(NSString *)name compatability:(int)compat andEnabled:(bool)en {
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
    
    if (en)
        image.image = [UIImage imageNamed:@"waffle_check"];
    else
        image.image = [UIImage imageNamed:@"waffle_x"];
    
    //[image setNeedsDisplay];
    
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
}


@end
