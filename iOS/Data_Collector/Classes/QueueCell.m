//
//  QueueCell.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 7/2/13.
//
//

#import "QueueCell.h"

@implementation QueueCell

@synthesize nameAndDate, dataType, description, dataSet;



- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (QueueCell *)setupCellWithDataSet:(DataSet *)ds {
      
    self.nameAndDate.text = ds.name;
    self.description.text = ds.dataDescription;
    
    NSString *tmpDataType;
    if (ds.picturePaths == nil) {
        if (ds.data == nil) {
            tmpDataType = @"Error";
        } else {
            tmpDataType = @"Data";
        }
    } else {
        if (ds.data == nil) {
            tmpDataType = @"Picture";
        } else {
            tmpDataType = @"Data";
        }
    }
    
    self.dataType.text = tmpDataType;
    [self setCheckedSwitch:ds.uploadable.boolValue];
    
    dataSet = [ds retain];
    
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        self.contentView.backgroundColor = [UIColor clearColor];
    }
    return self;
}


-(void)setCheckedSwitch:(bool)checked {
    if (checked) {
        self.accessoryType = UITableViewCellAccessoryCheckmark;
    } else {
        self.accessoryType = UITableViewCellAccessoryNone;
    }
}

-(IBAction)setChecked:(UITapGestureRecognizer *)sender {
    if (dataSet.uploadable.boolValue == false) {
        [self setCheckedSwitch:true];
        dataSet.uploadable = [[NSNumber alloc] initWithBool:true];
    } else {
        [self setCheckedSwitch:false];
        dataSet.uploadable = [[NSNumber alloc] initWithBool:false];
    }
}

-(void)dealloc {
    [super dealloc];
    [dataSet release];
}

@end
