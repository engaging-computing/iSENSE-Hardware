//
//  FieldMatchCell.h
//  iSENSE_API
//
//  Created by Michael Stowell on 11/14/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface FieldMatchCell : UITableViewCell

- (FieldMatchCell *)setupCellWithName:(NSString *)name andMatch:(NSString *)match;
- (void) setName:(NSString *)name;
- (void) setMatch:(NSString *)match;
- (NSString *) getName;
- (NSString *) getMatch;

@property (nonatomic, assign) IBOutlet UILabel  *fieldName;
@property (nonatomic, assign) IBOutlet UIButton *fieldMatch;

@end
