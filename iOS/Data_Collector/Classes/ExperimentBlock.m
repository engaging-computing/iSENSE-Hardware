//
//  ExperimentBlock.m
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/25/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//


#import "ExperimentBlock.h"

@implementation ExperimentBlock

@synthesize experiment;

- (id)initWithFrame:(CGRect)frame experiment:(Experiment *)exp target:(id)target action:(SEL)selector {

    self = [super initWithFrame:frame];
    if (self) {
        
        // Initialization code
        experiment = [exp retain];
        _target = target;
        _selector = selector;
        self.multipleTouchEnabled = false;
        
        // Backround image
        background = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"experimentblock_clean.png"]];
        [self addSubview:background];
        
        // Center Experiment Information in a Label
        UILabel *experimentNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(5, 0, frame.size.width - 10, frame.size.height - 25)];
        [experimentNameLabel setBackgroundColor:[UIColor clearColor]];
        experimentNameLabel.text = exp.name;
        experimentNameLabel.textAlignment = NSTextAlignmentCenter;
        experimentNameLabel.textColor = [UIColor blackColor];
        experimentNameLabel.font = [UIFont systemFontOfSize:14];
        
        UILabel *experimentDescriptionLabel = [[UILabel alloc] initWithFrame:CGRectMake(5, 22, frame.size.width - 10, 25)];
        [experimentDescriptionLabel setBackgroundColor:[UIColor clearColor]];
        if ([exp.description class] != [NSNull class]) experimentDescriptionLabel.text = exp.description;
        else experimentDescriptionLabel.text = @"No description provided.";
        experimentDescriptionLabel.textAlignment = NSTextAlignmentCenter;
        experimentDescriptionLabel.textColor = [UIColor blackColor];
        experimentDescriptionLabel.font = [UIFont systemFontOfSize:11];
        experimentDescriptionLabel.numberOfLines = 2;
        experimentDescriptionLabel.lineBreakMode = NSLineBreakByTruncatingTail;
        
        // Add the label to the main view
        [self addSubview:experimentNameLabel];
        [self addSubview:experimentDescriptionLabel];
        [experimentDescriptionLabel release];
        [experimentNameLabel release];
        
    }
    return self;
}

- (void) switchToDarkImage:(bool)booleanSwitch {
    if (booleanSwitch) {
        background.image = [UIImage imageNamed:@"experimentblock_dark.png"];
    } else {
        background.image = [UIImage imageNamed:@"experimentblock_clean.png"];
    }
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    [self switchToDarkImage:TRUE];
}

-(void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event {
    [self switchToDarkImage:FALSE];

}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    [_target performSelector:_selector withObject:self];
}

- (void) dealloc {
    [experiment release];
    [super dealloc];
}

@end
