//
//  FieldMatchCell.m
//  iSENSE_API
//
//  Created by Michael Stowell on 11/14/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import "FieldMatchCell.h"

@implementation FieldMatchCell

@synthesize fieldProj, fieldMatch;

- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {}
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
}

- (FieldMatchCell *)setupCellWithProjField:(NSString *)proj andMatchField:(NSString *)match{
    
    [self.fieldProj  setText:proj];
    [self.fieldMatch setText:match];
    
    return self;
}

- (void) setProjField:(NSString *)proj {
    [fieldProj setText:proj];
}

- (void) setMatchField:(NSString *)match {
    [fieldMatch setText:match];
}

- (NSString *) getProjField {
    return fieldProj.text;
}

- (NSString *) getMatchField {
    return fieldMatch.text;
}

@end
