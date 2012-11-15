//
//  RestAPI.m
//  iSENSE_Data_Collector
//
//  Created by Jeremy Poulin on 11/6/12.
//  Copyright 2012 __MyCompanyName__. All rights reserved.
//

#import "RestAPI.h"

static NSString *baseURL = @"http://isensedev.cs.uml.edu/ws/api.php";
static RestAPI *instance = nil;

@implementation RestAPI


/*- (id)initWithFrame:(CGRect)frame {
    
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code.
		login_key = -1;
    }
    return self;
}*/

- (BOOL) login:(NSString*)username:(NSString*)password {
	NSString *url = nil;
	url = @"method=login&username=";
	const char *temp;
	temp = [username UTF8String];
	username = [[NSString alloc] initWithUTF8String:temp];
	url = [[url stringByAppendingString:username] retain];
	temp = [password UTF8String];
	password = [[NSString alloc] initWithUTF8String:temp];
	url = [[url stringByAppendingString:@"&password="] retain];
	url = [[url stringByAppendingString:password] retain];
	
	NSString *response = [self makeRequest:url];
	NSLog(@"%@", url);
	
	if (url == nil) return FALSE;
	
	/*		String url = null;
		try {
			url = "method=login&username="
			+ URLEncoder.encode(username, "UTF-8") + "&password="
			+ URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		if (url == null)
			return false;
		
		if (isConnected()) {
			try {
				connection = "NONE";
				String data = makeRequest(url);
				
				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				connection = o.getString("status");
				checkStatus(connection);
				if (connection.equals("600")) {
					Log.e(TAG, "Invalid username or password.");
					return false;
				}
				session_key = o.getJSONObject("data").getString("session");
				uid = o.getJSONObject("data").getInt("uid");
				
				if (isLoggedIn()) {
					this.username = username;
					return true;
				}
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
				connection = "NONE";
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				connection = "NONE";
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				connection = "NONE";
				return false;
			}
			
			return true;
		}
		connection = "NONE";
		return false;
	}*/
	return TRUE;
}

-(NSString *)makeRequest:(NSString*) target {
	target = [target stringByReplacingOccurrencesOfString:@" " withString:@"+"];
	NSLog(@"%@", target);
	
	return @"null";
}

/*
 public String makeRequest(String target) throws Exception {
 
 String output = "{}";
 
 String data = target.replace(" ", "+");
 
 HttpURLConnection conn = (HttpURLConnection) new URL(RestAPI.base_url)
 .openConnection();
 conn.setDoOutput(true);
 conn.setRequestMethod("POST");
 conn.setRequestProperty("Content-Length",
 Integer.toString(data.length()));
 conn.getOutputStream().write(data.getBytes(charEncoding));
 conn.connect();
 conn.getResponseCode();
 
 // Get the status code of the HTTP Request so we can figure out what to
 // do next
 int status = conn.getResponseCode();
 
 switch (status) {
 
 case 200:
 
 // Build Reader and StringBuilder to output to String
 BufferedReader br = new BufferedReader(new InputStreamReader(
 conn.getInputStream()));
 StringBuilder sb = new StringBuilder();
 String line;
 
 // Loop through response to build JSON String
 while ((line = br.readLine()) != null) {
 sb.append(line + "\n");
 }
 
 // Set output from response
 output = sb.toString();
 break;
 
 case 404:
 // Handle 404 page not found
 Log.e(TAG, "Could not find URL! (404 Exception)");
 throw new IOException();
 
 default:
 // Catch all for all other HTTP response codes
 Log.e(TAG, "Returned unhandled error code: " + status);
 throw new IOException();
 }
 
 return output;
 }
 */

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code.
}
*/

// Get the shared instance and create it if necessary.
+ (RestAPI *)getInstance {
    if (instance == nil) {
        instance = [[super allocWithZone:NULL] init];
    }
	
    return instance;
}

- (void)dealloc {
	[super dealloc];
}


@end
