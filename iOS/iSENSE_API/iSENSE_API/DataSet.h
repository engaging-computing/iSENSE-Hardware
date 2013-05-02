//
//  DataSet.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 4/26/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#ifndef __iSENSE_API__DataSet__
#define __iSENSE_API__DataSet__

#import <Foundation/Foundation.h>


@interface DataSet : NSObject {
}

- (id) initWithName:(NSString *)name;
- (id) initWithName:(NSString *)name andDescription:(NSString *)description;
- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid;
- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data;
- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths;
- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths andSessionId:(int)sid;
- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths andSessionId:(int)sid andCity:(NSString *)city;
- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths andSessionId:(int)sid andCity:(NSString *)city andCountry:(NSString *)country;
- (id) initWithName:(NSString *)name andDescription:(NSString *)description andEID:(int)eid andData:(NSArray *)data andPicturePaths:(NSArray *)picturePaths andSessionId:(int)sid andCity:(NSString *)city andCountry:(NSString *)country andAddress:(NSString *)address;


@property (nonatomic, copy) NSString *name_;
@property (nonatomic, copy) NSString *description_;
@property (nonatomic, assign) int eid_;
@property (nonatomic, retain) NSArray *data_;
@property (nonatomic, retain) NSArray *picturePaths_;
@property (nonatomic, assign) int sid_;
@property (nonatomic, copy) NSString *city_;
@property (nonatomic, copy) NSString *country_;
@property (nonatomic, copy) NSString *address_;
@property (nonatomic, assign) BOOL uploadable_;

@end

#endif /* defined(__iSENSE_API__Dataset__) */
