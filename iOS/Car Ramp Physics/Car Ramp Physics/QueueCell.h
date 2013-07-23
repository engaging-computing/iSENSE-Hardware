//
//  QueueCell.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/23/13.
//  Copyright (c) 2013 ECG. All rights reserved.
//

#import "DataSet.h"
#import "CRTableViewCell.h"

@interface QueueCell : CRTableViewCell

- (QueueCell *)setupCellWithDataSet:(DataSet *)dataSet andKey:(NSNumber *)key;
- (void) toggleChecked;
- (void) setSessionName:(NSString *)name;
- (NSNumber *)getKey;
- (void) setExpNum:(NSString *)exp;

@property (nonatomic, retain) DataSet *dataSet;
@property (nonatomic, retain) NSNumber *mKey;



@end
