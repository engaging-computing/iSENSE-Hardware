//
//  ExperimentBlock.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 1/25/13.
//
//

#import "ExperimentBlock.h"

@implementation ExperimentBlock

- (id)initWithFrame:(CGRect)frame experimentName:(NSString *)expName experimentNumber:(NSInteger)expNum {
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        experimentName = [[NSString alloc] initWithString:expName];
        experimentNumber = [[NSNumber alloc] initWithUnsignedInteger:expNum];
        
        // Backround black and white stroke
        [self setBackgroundColor:[UIColor blackColor]];
        [self.layer ];
        self.layer.borderWith = 2;
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
