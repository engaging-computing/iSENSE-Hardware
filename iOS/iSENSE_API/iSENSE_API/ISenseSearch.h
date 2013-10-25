//
//  ISenseSearch.h
//  iOS Data Collector
//
//  Created by Jeremy Poulin on 2/5/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <Foundation/Foundation.h>

typedef enum BuildType { NEW = 0, APPEND = 1 } BuildType;

@interface ISenseSearch : NSObject {
}

@property (nonatomic, retain) NSString *query;
@property (nonatomic, assign) BuildType buildType;
@property (nonatomic, assign) int page;
@property (nonatomic, assign) int perPage;

- (id) init;
- (id) initWithQuery:(NSString *)q page:(int)pageNumber itemsPerPage:(int)itemsPerPage andBuildType:(BuildType)bt;

@end
