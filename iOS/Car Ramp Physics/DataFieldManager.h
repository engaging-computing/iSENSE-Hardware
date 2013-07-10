//
//  DataFieldManager.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 2/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <Foundation/Foundation.h>
#import "Fields.h"

@interface DataFieldManager : NSObject {
    
    bool enabledFields[22];
}

- (NSMutableArray *) getFieldOrderOfExperiment:(int)exp;
- (NSMutableArray *) orderDataFromFields:(Fields *)f;
- (void) setEnabledField:(bool)value atIndex:(int)index;
- (bool) enabledFieldAtIndex:(int)index;

@property (nonatomic, retain) NSMutableArray *order;
@property (nonatomic, retain) NSMutableArray *data;

@end
