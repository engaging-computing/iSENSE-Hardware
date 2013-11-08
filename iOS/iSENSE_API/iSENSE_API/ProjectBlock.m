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
        isenseBundle = [NSBundle bundleWithURL:[[NSBundle mainBundle] URLForResource:@"iSENSE_API_Bundle" withExtension:@"bundle"]];
        self.multipleTouchEnabled = false;
        
        // Backround image
        NSString *cleanImagePath = [isenseBundle pathForResource:@"projectblock_clean" ofType:@"png"];
        background = [[UIImageView alloc] initWithImage:[UIImage imageWithContentsOfFile:cleanImagePath]];
        [self addSubview:background];
        
        // Center Project Information in a Label
        UILabel *projectNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(5, 0, frame.size.width - 10, frame.size.height - 25)];
        [projectNameLabel setBackgroundColor:[UIColor clearColor]];
        projectNameLabel.text = proj.name;
        projectNameLabel.textAlignment = NSTextAlignmentCenter;
        projectNameLabel.textColor = [UIColor blackColor];
        projectNameLabel.font = [UIFont systemFontOfSize:14];
        
        UILabel *projectCreatorLabel = [[UILabel alloc] initWithFrame:CGRectMake(5, 22, frame.size.width - 10, 25)];
        [projectCreatorLabel setBackgroundColor:[UIColor clearColor]];
        if ([proj.owner_name class] != [NSNull class]) projectCreatorLabel.text = [NSString stringWithFormat:@"Created by: %@", proj.owner_name];
        else projectCreatorLabel.text = @"Unknown creator.";
        projectCreatorLabel.textAlignment = NSTextAlignmentCenter;
        projectCreatorLabel.textColor = [UIColor blackColor];
        projectCreatorLabel.font = [UIFont systemFontOfSize:11];
        projectCreatorLabel.numberOfLines = 2;
        projectCreatorLabel.lineBreakMode = NSLineBreakByTruncatingTail;
        
        // Add the label to the main view
        [self addSubview:projectNameLabel];
        [self addSubview:projectCreatorLabel];
        
    }
    return self;
}

- (void) switchToDarkImage:(bool)booleanSwitch {
    if (booleanSwitch) {
        NSString *darkImagePath = [isenseBundle pathForResource:@"projectblock_dark" ofType:@"png"];
        background.image = [UIImage imageWithContentsOfFile:darkImagePath];
    } else {
        NSString *cleanImagePath = [isenseBundle pathForResource:@"projectblock_clean" ofType:@"png"];
        background.image = [UIImage imageWithContentsOfFile:cleanImagePath];
    }
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    [self switchToDarkImage:TRUE];
}

-(void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event {
    [self switchToDarkImage:FALSE];
    
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    #pragma clang diagnostic push
    #pragma clang diagnostic ignored "-Warc-performSelector-leaks"
    [_target performSelector:_selector withObject:self];
    #pragma clang diagnostic pop
}

@end
