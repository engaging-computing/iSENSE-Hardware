//
//  QueueCell.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 7/2/13.
//
//

#import <UIKit/UIKit.h>
#import <DataSet.h>

@interface QueueCell : UITableViewCell

- (QueueCell *)setupCellWithDataSet:(DataSet *)dataSet;
- (IBAction)setChecked:(UITapGestureRecognizer *)sender;

@property (nonatomic, assign) IBOutlet UILabel *nameAndDate;
@property (nonatomic, assign) IBOutlet UILabel *dataType;
@property (nonatomic, assign) IBOutlet UILabel *description;
@property (nonatomic, retain) IBOutlet UITapGestureRecognizer *onClickRecognizer;
@property (nonatomic, retain) DataSet *dataSet;

@end
