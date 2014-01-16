//
//  QueueCell.h
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/23/13.
//  Copyright (c) 2013 ECG. All rights reserved.
//

#import <iSENSE_API/headers/QDataSet.h>
#import "DataFieldManager.h"
#import "CRTableViewCell.h"

@interface QueueCell : CRTableViewCell

- (QueueCell *)setupCellWithDataSet:(QDataSet *)dataSet andKey:(NSNumber *)key;
- (void) setSessionName:(NSString *)name;
- (NSNumber *)getKey;
- (void) setExpNum:(NSString *)exp;
- (BOOL) dataSetHasInitialExperiment;

@property (nonatomic, retain) QDataSet *dataSet;
@property (nonatomic, retain) NSNumber *mKey;



@end
