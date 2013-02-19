//
//  ISenseSearch.h
//  Data_Collector
//
//  Created by Jeremy Poulin on 2/5/13.
//
//

#import <Foundation/Foundation.h>

typedef enum SearchType { RECENT = 0, RATING = 1, ACTIVITY = 2, POPULARITY = 3 } SearchType;
typedef enum BuildType { NEW = 0, APPEND = 1 } BuildType;

@interface ISenseSearch : NSObject {
}

@property (nonatomic, assign) SearchType searchType;
@property (nonatomic, retain) NSString *query;
@property (nonatomic, assign) BuildType buildType;
@property (nonatomic, assign) int page;

- (id) init;
- (id) initWithQuery:(NSString *)q searchType:(SearchType)st page:(int)p andBuildType:(BuildType)bt;
- (NSString *)searchTypeToString;

@end
