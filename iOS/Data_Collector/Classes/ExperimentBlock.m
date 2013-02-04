//
//  ExperimentBlock.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 1/25/13.
//
//

#import "ExperimentBlock.h"

@implementation ExperimentBlock

- (id)initWithFrame:(CGRect)frame experimentName:(NSString *)expName experimentNumber:(NSInteger)expNum target:(id)target action:(SEL)selector {
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        experimentName = [[NSString alloc] initWithString:expName];
        experimentNumber = [[NSNumber alloc] initWithUnsignedInteger:expNum];
        _target = target;
        _selector = selector;
        self.multipleTouchEnabled = false;
        
        // Backround image
        background = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"button_experiment_clean.png"]];
        [self addSubview:background];
        
        // Center Experiment Information in a Label
        UILabel *experimentNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 10, frame.size.width - 10, frame.size.height - 10)];
        [experimentNameLabel setBackgroundColor:[UIColor clearColor]];
        experimentNameLabel.text = expName;
        experimentNameLabel.textAlignment = NSTextAlignmentCenter;
        experimentNameLabel.textColor = [UIColor blackColor];
        
        // Add the label to the main view
        [self addSubview:experimentNameLabel];
        
        // Set the listener for the experiment button
        UITapGestureRecognizer *pressRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(buttonClicked)];
        [self addGestureRecognizer:pressRecognizer];
        
    }
    return self;
}

- (void) switchToDarkImage:(bool)booleanSwitch {
    if (booleanSwitch) {
        background.image = [UIImage imageNamed:@"button_experiment_dark.png"];
    } else {
        background.image = [UIImage imageNamed:@"button_experiment_clean.png"];
    }
}

- (void) buttonClicked {
    [_target performSelector:_selector withObject:self];
}

- (void) touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    NSLog(@"buttonClicked -- start");

    [self switchToDarkImage:true];
}
/*
- (void) touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    NSLog(@"buttonClicked -- released");

    [self switchToDarkImage:false];
} */

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
