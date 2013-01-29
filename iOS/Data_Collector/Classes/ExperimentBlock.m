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
        self.layer.borderWidth = 2;
        self.layer.borderColor = [UIColor whiteColor].CGColor;
        
        // Center Experiment Information in a Label
        UILabel *experimentNameLabel = [[UILabel alloc] initWithFrame:frame];
        [experimentNameLabel setBackgroundColor:[UIColor clearColor]];
        experimentNameLabel.text = expName;
        experimentNameLabel.textAlignment = NSTextAlignmentCenter;
        experimentNameLabel.textColor = [UIColor whiteColor];
        
        // Add the label to the main view
        [self addSubview:experimentNameLabel];
        
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
