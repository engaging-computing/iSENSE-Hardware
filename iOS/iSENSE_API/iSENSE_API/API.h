//
//  API.h
//  iSENSE_API
//
//  Created by Jeremy Poulin on 8/21/13.
//  Copyright (c) 2013 Engaging Computing Group, UML. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface API : NSObject {
	private:
		NSString baseURL;
}

/* getInstance */
+(void)initialize;

/* Switch For Dev */
-(void)useDev:(BOOL)useDev;

/* Gets an Authentication Key */
-(BOOL)createSessionWithUsername:(NSString *)username andPassword:(NSString *)password;

/* Require an Authentication Key */

@end
