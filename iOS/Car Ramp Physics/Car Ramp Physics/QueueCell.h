//
//  QueueCell.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/23/13.
//  Copyright (c) 2013 ECG. All rights reserved.
//

#import "DataSet.h"
#import "DataFieldManager.h"
#import "CRTableViewCell.h"

@interface QueueCell : CRTableViewCell

- (QueueCell *)setupCellWithDataSet:(DataSet *)dataSet andKey:(NSNumber *)key;
- (void) setSessionName:(NSString *)name;
- (NSNumber *)getKey;
- (void) setExpNum:(NSString *)exp;
- (BOOL) dataSetHasInitialExperiment;

@property (nonatomic, retain) DataSet *dataSet;
@property (nonatomic, retain) NSNumber *mKey;



@end
