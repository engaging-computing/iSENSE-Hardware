//
//  RestAPIConnectionDelegate.m
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 11/16/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "RestAPIConnectionDelegate.h"


@implementation RestAPIConnectionDelegate : NSURLConnection

@synthesize data;

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
	[self.data setLength:0];
	NSLog(@"%@", response.URL);
	NSHTTPURLResponse* httpResponse = (NSHTTPURLResponse*)response;
	int code = [httpResponse statusCode];
	NSLog(@"%d", code);
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)d {
    [self.data appendData:d];
	NSString *responseText = [[NSString alloc] initWithData:d encoding:NSUTF8StringEncoding];
	NSLog(@"Receieved: %@", responseText);
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error {
    [[[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"")
                                 message:[error localizedDescription]
                                delegate:nil
                       cancelButtonTitle:NSLocalizedString(@"OK", @"") 
                       otherButtonTitles:nil] autorelease] show];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
    NSString *responseText = [[NSString alloc] initWithData:self.data encoding:NSUTF8StringEncoding];
	
    NSLog(@"Response: %@", responseText);
	
    [responseText release];
}

// Handle basic authentication challenge if needed
- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge {
    NSString *username = @"sor";
    NSString *password = @"sor";
	
    NSURLCredential *credential = [NSURLCredential credentialWithUser:username
                                                             password:password
                                                          persistence:NSURLCredentialPersistenceForSession];
    [[challenge sender] useCredential:credential forAuthenticationChallenge:challenge];
}

@end
