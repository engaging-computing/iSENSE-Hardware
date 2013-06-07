//
//  DataSet.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 6/7/13.
//  Copyright (c) 2013 Jeremy Poulin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface DataSet : NSManagedObject

@property (nonatomic, retain) NSString * address;
@property (nonatomic, retain) NSString * city;
@property (nonatomic, retain) NSString * country;
@property (nonatomic, retain) id data;
@property (nonatomic, retain) NSString * dataDescription;
@property (nonatomic, retain) NSNumber * eid;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) id picturePaths;
@property (nonatomic, retain) NSNumber * sid;
@property (nonatomic, retain) NSNumber * uploadable;

@end
