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
#import "API.h"

@interface DataFieldManager : NSObject {
    bool enabledFields[22];
    
    int projID;
    API *api;
    Fields *f;
    
    NSMutableArray *projFields;
}

/* old methods */
- (id) initWithProjID:(int)projectID API:(API *)isenseAPI andFields:(Fields *)fields;
- (void) getOrder;
- (void) getProjectFieldOrder;
- (int) getProjID;
- (void) setProjID:(int)projectID;
- (NSMutableArray *) getProjectFields;
- (NSMutableArray *) getOrderList;
- (NSMutableArray *) getRealOrder;
- (Fields *) getFields;
- (void) setFields:(Fields *)fields;
- (void) enableAllFields;
- (void) setEnabledFields:(NSMutableArray *)acceptedFields;
- (void) setOrder:(NSMutableArray *)newOrderFields;
- (void) setEnabledField:(bool)value atIndex:(int)index;
- (bool) enabledFieldAtIndex:(int)index;

/* new methods */
- (NSMutableArray *) putData;
+ (NSMutableArray *) reOrderData:(NSMutableArray *)data forProjectID:(int)projectID withFieldOrder:(NSMutableArray *)fieldOrder andFieldIDs:(NSMutableArray *)ids;
- (NSMutableArray *) getFieldIDs;

/* old properties */
@property (nonatomic, retain) NSMutableArray *order;
@property (nonatomic, retain) NSMutableArray *realOrder;

/* new properties */
@property (nonatomic, retain) NSMutableArray *fieldIDs;

@end