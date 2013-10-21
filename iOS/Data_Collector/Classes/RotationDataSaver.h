//
//  RotationDataSaver.h
//  Data_Collector
//
//  Created by  on 10/21/13.
//
//

#import <Foundation/Foundation.h>

@interface RotationDataSaver : NSObject

@property (nonatomic, strong) NSString *sesName;
@property (nonatomic, strong) NSMutableArray *data;
@property (nonatomic, assign) bool doesHaveName;
@property (nonatomic, assign) bool doesHaveData;

@end
