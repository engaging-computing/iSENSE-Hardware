//
//  DataFieldManager.h
//  iOS Data Collector
//
//  Created by Mike Stowell on 2/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <Foundation/Foundation.h>

@interface DataFieldManager : NSObject {


}

- (NSMutableArray *) getFieldOrderOfExperiment:(int)exp;
- (NSMutableArray *) putData;

@end
