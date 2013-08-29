//
//  ProjectBlock.m
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 1/25/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//


#import "ProjectBlock.h"

@implementation ProjectBlock

@synthesize project;

- (id)initWithFrame:(CGRect)frame project:(RProject *)proj target:(id)target action:(SEL)selector {

    self = [super initWithFrame:frame];
    if (self) {
        
        // Initialization code
        project = proj;
        _target = target;
        _selector = selector;
        self.multipleTouchEnabled = false;
        
        // Backround image
        background = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"projectblock_clean.png"]];
        [self addSubview:background];
        
        // Center Project Information in a Label
        UILabel *projectNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(5, 0, frame.size.width - 10, frame.size.height - 25)];
        [projectNameLabel setBackgroundColor:[UIColor clearColor]];
        projectNameLabel.text = proj.name;
        projectNameLabel.textAlignment = NSTextAlignmentCenter;
        projectNameLabel.textColor = [UIColor blackColor];
        projectNameLabel.font = [UIFont systemFontOfSize:14];
        
        UILabel *projectDescriptionLabel = [[UILabel alloc] initWithFrame:CGRectMake(5, 22, frame.size.width - 10, 25)];
        [projectDescriptionLabel setBackgroundColor:[UIColor clearColor]];
        if ([proj.description class] != [NSNull class]) projectDescriptionLabel.text = proj.description;
        else projectDescriptionLabel.text = @"No description provided.";
        projectDescriptionLabel.textAlignment = NSTextAlignmentCenter;
        projectDescriptionLabel.textColor = [UIColor blackColor];
        projectDescriptionLabel.font = [UIFont systemFontOfSize:11];
        projectDescriptionLabel.numberOfLines = 2;
        projectDescriptionLabel.lineBreakMode = NSLineBreakByTruncatingTail;
        
        // Add the label to the main view
        [self addSubview:projectNameLabel];
        [self addSubview:projectDescriptionLabel];
        
    }
    return self;
}

- (void) switchToDarkImage:(bool)booleanSwitch {
    if (booleanSwitch) {
        background.image = [UIImage imageNamed:@"projectblock_dark.png"];
    } else {
        background.image = [UIImage imageNamed:@"projectblock_clean.png"];
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

@end
