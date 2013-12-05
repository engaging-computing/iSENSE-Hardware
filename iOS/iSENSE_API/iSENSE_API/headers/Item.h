//
//  Item.h
//  iSENSE API
//
//  Created by James Dalphond on 2/23/11.
//  Copyright 2011 UMass Lowell. All rights reserved.
// 
//  Modified by John Fertitta on 3/1/11.
//

#import <Foundation/Foundation.h>

@interface Item : NSObject {

}

/*Properties for getting/setting variables*/
@property (assign) NSMutableArray *experiments;
@property (assign) NSMutableArray *sessions;
@property (assign) NSMutableArray *images;

@end

