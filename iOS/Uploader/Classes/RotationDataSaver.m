//
//  RotationDataSaver.m
//  Data_Collector
//
//  Created by  on 10/21/13.
//
//

#import "RotationDataSaver.h"

@implementation RotationDataSaver

@synthesize dsName, data, doesHaveName, doesHaveData;

- (id) init {
    if (self = [super init]) {
        dsName = [[NSString alloc] init];
        
        data = [[NSMutableArray alloc] init];
        for (NSInteger i = 0; i < 100; ++i)
            [data addObject:[NSNull null]];
        
        
        doesHaveName = FALSE;
        doesHaveData = FALSE;
    }
    return self;
}

@end
