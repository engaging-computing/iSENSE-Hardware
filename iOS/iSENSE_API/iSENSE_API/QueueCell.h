//
//  QueueCell.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 7/2/13.
//  Modified by Mike Stowell
//

#import <UIKit/UIKit.h>
#import "QDataSet.h"

@interface QueueCell : UITableViewCell

- (QueueCell *)setupCellWithDataSet:(QDataSet *)dataSet andKey:(NSNumber *)key;
- (void) toggleChecked;
- (void) setDataSetName:(NSString *)name;
- (NSNumber *)getKey;
- (void) setProjID:(NSString *)projID;
- (void) setDesc:(NSString *)desc;
- (BOOL) dataSetHasInitialProject;
- (void) setFields:(NSMutableArray *)fields;
- (NSMutableArray *) getFields;

@property (nonatomic, assign) IBOutlet UILabel *nameAndDate;
@property (nonatomic, assign) IBOutlet UILabel *dataType;
@property (nonatomic, assign) IBOutlet UILabel *description;
@property (nonatomic, assign) IBOutlet UILabel *projIDLabel;

@property (nonatomic, retain) QDataSet *dataSet;
@property (nonatomic, retain) NSNumber *mKey;

@end
