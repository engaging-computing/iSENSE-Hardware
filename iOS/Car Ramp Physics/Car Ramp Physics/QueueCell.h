//
//  QueueCell.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 7/2/13.
//
//
#import "CRTableViewCell.h"
#import <UIKit/UIKit.h>
#import <DataSet.h>

@interface QueueCell : UITableViewCell

- (QueueCell *)setupCellWithDataSet:(DataSet *)dataSet;

@property (nonatomic, assign) BOOL isSelected;
@property (nonatomic, readonly, strong) UILabel *textLabel;
@property (nonatomic, readonly, strong) UIImageView *imageView;
@property (nonatomic, readonly, strong) UIImage *renderedMark;
@property (nonatomic, retain) DataSet *dataSet;

@end
