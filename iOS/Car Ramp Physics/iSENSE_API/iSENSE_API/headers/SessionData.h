//
//  SessionData.h
//  isenseAPI
//
//  Created by James Dalphond on 2/23/11.
//  Copyright 2011 UMass Lowell. All rights reserved.
//
//  Modified by John Fertitta on 3/1/11.
//

#import <Foundation/Foundation.h>

@interface SessionData : NSObject{

}

/*Properties for setting/getting variables*/

@property (assign) NSDictionary *RawJSON;
@property (assign) NSArray *DataJSON;
@property (assign) NSArray *FieldsJSON;
@property (assign) NSArray *MetaDataJSON;
@property (assign) NSMutableArray *fieldData;	

@end