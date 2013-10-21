//
//  QueueCell.m
//  Data_Collector
//
//  Created by Jeremy Poulin on 7/2/13.
//
//

#import "QueueCell.h"

@implementation QueueCell

@synthesize nameAndDate, dataType, description, eidLabel, dataSet, mKey;



- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (QueueCell *)setupCellWithDataSet:(QDataSet *)ds andKey:(NSNumber *)key {
    self.mKey = key;
    self.nameAndDate.text = ds.name;
    self.description.text = ds.dataDescription;
    self.eidLabel.text = (ds.projID.intValue == -1) ? @"No Proj." : [NSString stringWithFormat:@"%d", ds.projID.intValue];
    
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
    
    dataSet = ds;
    
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

- (void) toggleChecked {
    if (dataSet.uploadable.boolValue == false) {
        [self setCheckedSwitch:true];
        dataSet.uploadable = [NSNumber numberWithBool:true];
    } else {
        [self setCheckedSwitch:false];
        dataSet.uploadable = [NSNumber numberWithBool:false];
    }
}

- (void) setSessionName:(NSString *)name {
    self.nameAndDate.text = name;
    [dataSet setName:name];
}

- (NSNumber *)getKey {
    return mKey;
}

- (void) setExpNum:(NSString *)exp {
    self.eidLabel.text = exp;
    [dataSet setProjID:[NSNumber numberWithInt:[exp intValue]]];
}

- (void) setDesc:(NSString *)desc {
    self.description.text = desc;
    [dataSet setDataDescription:desc];
}

- (BOOL) dataSetHasInitialExperiment {
    NSNumber *initial = [dataSet hasInitialProj];
    return [initial boolValue];
}


@end
