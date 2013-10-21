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

@property (nonatomic, strong) IBOutlet UILabel *nameAndDate;
@property (nonatomic, strong) IBOutlet UILabel *dataType;
@property (nonatomic, strong) IBOutlet UILabel *description;
@property (nonatomic, strong) IBOutlet UILabel *eidLabel;

@property (nonatomic, strong) QDataSet *dataSet;
@property (nonatomic, strong) NSNumber *mKey;

@end
