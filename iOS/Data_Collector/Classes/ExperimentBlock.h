//
//  ExperimentBlock.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 1/25/13.
//
//

#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>

@interface ExperimentBlock : UIView {
    @public
    Experiment *experiment;
    @protected
    UIImageView *background;
    SEL _selector;
    id _target;

}

- (id)initWithFrame:(CGRect)frame experiment:(Experiment*)exp target:(id)target action:(SEL)selector;
- (void)switchToDarkImage:(bool)booleanSwitch;

@end