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

/* new methods */
- (id) initWithProjID:(int)projectID API:(API *)isenseAPI andFields:(Fields *)fields;
+ (NSMutableArray *) getOrderForProjID:(int)projectID API:(API *)isenseAPI;
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
- (NSMutableDictionary *) putData;
- (NSMutableArray *) putDataForNoProjectID;
+ (NSMutableArray *) reOrderData:(NSMutableArray *)data forProjectID:(int)projectID API:(API *)isenseAPI andFieldOrder:(NSMutableArray *)fieldOrder;
- (void) setOrder:(NSMutableArray *)newOrderFields;

/* old methods */
- (void) setEnabledField:(bool)value atIndex:(int)index;
- (bool) enabledFieldAtIndex:(int)index;

/* old properties */
@property (nonatomic, retain) NSMutableArray *order;

/* new properties */
@property (nonatomic, retain) NSMutableArray *realOrder;

@end