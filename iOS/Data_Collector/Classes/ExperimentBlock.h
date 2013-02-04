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
    NSString *experimentName;
    NSNumber *experimentNumber;
    UIImageView *background;
    SEL _selector;
    id _target;

}

- (id)initWithFrame:(CGRect)frame experimentName:(NSString *)expName experimentNumber:(NSInteger)expNum target:(id)target action:(SEL)selector;
- (void)switchToDarkImage:(bool)booleanSwitch;
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event;
//- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event;

@end