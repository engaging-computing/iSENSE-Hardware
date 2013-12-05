//
//  SensorCell.m
//  Data_Collector
//
//  Created by Michael Stowell on 7/2/13.
//
//

#import "SensorCell.h"
#import <iSENSE_API/ISKeys.h>

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
            [compatible setTextColor:UIColorFromHex(0xDE1F22)];
            break;
            
        case AVAILABLE:
            [compatible setText:@"Compatible"];
            [compatible setTextColor:UIColorFromHex(0x5BED18)];
            break;
            
        case AVAIL_CONNECTIVITY:
            [compatible setText:@"Compatible (with connectivity)"];
            [compatible setTextColor:UIColorFromHex(0x5BED18)];
            break;
            
        case AVAIL_WIFI_ONLY:
            [compatible setText:@"Compatible (with WiFi enabled)"];
            [compatible setTextColor:UIColorFromHex(0x5BED18)];
            break;
            
        case NOT_DETECTED:
            [compatible setText:@"Cannot determine compatibility"];
            [compatible setTextColor:UIColorFromHex(0xEDCD18)];
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
