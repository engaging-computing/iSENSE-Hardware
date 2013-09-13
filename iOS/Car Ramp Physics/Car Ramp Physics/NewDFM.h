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
#import <iSENSE_API/API.h>
#import "StringGrabber.h"

@interface NewDFM : NSObject {
    
    bool enabledFields[22];
}

- (NSMutableArray *) getFieldOrderOfProject:(int)projID;
- (NSMutableDictionary *) putDataFromFields:(Fields *)f;
- (NSMutableArray *) putDataForNoProjectIDFromFields:(Fields *)f;
- (void) setEnabledField:(bool)value atIndex:(int)index;
- (bool) enabledFieldAtIndex:(int)index;
- (void) addAllFieldsToOrder;
- (id) reOrderData:(NSMutableArray*)oldData forProjectID:(int)projID;

@property (nonatomic, retain) NSMutableArray *order;
@property (nonatomic, retain) NSMutableArray *data;

@end
