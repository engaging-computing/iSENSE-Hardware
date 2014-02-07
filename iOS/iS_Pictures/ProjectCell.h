//
//  ProjectCell.h
//  iS Pictures
//
//  Created by Virinchi Balabhadrapatruni on 2/4/14.
//  Copyright (c) 2014 ECG. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RProject.h>

@interface ProjectCell : UITableViewCell

- (id) initWithProject:(RProject *) project;

@property int projID;

@end
