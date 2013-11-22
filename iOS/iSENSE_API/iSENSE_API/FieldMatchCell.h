//
//  FieldMatchCell.h
//  iSENSE_API
//
//  Created by Michael Stowell on 11/14/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface FieldMatchCell : UITableViewCell

- (FieldMatchCell *)setupCellWithProjField:(NSString *)proj andMatchField:(NSString *)match;
- (void) setProjField:(NSString *)proj;
- (void) setMatchField:(NSString *)match;
- (NSString *) getProjField;
- (NSString *) getMatchField;

@property (nonatomic, assign) IBOutlet UILabel  *fieldProj;
@property (nonatomic, assign) IBOutlet UILabel  *fieldMatch;

@end
