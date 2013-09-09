//
//  QueueCell.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 7/2/13.
//
//

#import <UIKit/UIKit.h>
#import <iSENSE_API/headers/QDataSet.h>

@interface QueueCell : UITableViewCell

- (QueueCell *)setupCellWithDataSet:(QDataSet *)dataSet andKey:(NSNumber *)key;
- (void) toggleChecked;
- (void) setSessionName:(NSString *)name;
- (NSNumber *)getKey;
- (void) setExpNum:(NSString *)exp;
- (void) setDesc:(NSString *)desc;
- (BOOL) dataSetHasInitialExperiment;

@property (nonatomic, assign) IBOutlet UILabel *nameAndDate;
@property (nonatomic, assign) IBOutlet UILabel *dataType;
@property (nonatomic, assign) IBOutlet UILabel *description;
@property (nonatomic, assign) IBOutlet UILabel *eidLabel;

@property (nonatomic, retain) QDataSet *dataSet;
@property (nonatomic, retain) NSNumber *mKey;

@end
