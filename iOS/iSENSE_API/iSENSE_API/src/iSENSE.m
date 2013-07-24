//
//  iSENSE.m
//  iSENSE API
//
//  Created by John Fertitta on 2/23/11.
//  Updated by Jeremy Poulin on 12/05/12.
//  Copyright 2011 UMass Lowell. All rights reserved.
//

#import "iSENSE.h"

static NSString *baseURL = @"http://isense.cs.uml.edu/ws/api.php?";
static iSENSE *_iSENSE = nil;


@implementation iSENSE

// Makes a request to iSENSE and parse the JSONObject it gets back (TODO)
-(NSDictionary *)isenseQuery:(NSString*)target {
    
	NSString *final_target = [target stringByReplacingOccurrencesOfString:@" " withString:@"+"];
	NSLog(@"Sending to %@: %@", baseURL, final_target);
	NSError *requestError = nil;
    
    /* Post request to allow for longer request */
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:baseURL]];
    [request setHTTPMethod:@"POST"];
    
    NSData *data = [final_target dataUsingEncoding:NSUTF8StringEncoding];
    [request setHTTPBody:data];
    NSString *length = [[NSNumber numberWithUnsignedInt:data.length] stringValue];
    [request setValue:length forHTTPHeaderField:@"Content-Length"];
    
    NSURLResponse *serverResponse;
    @try {
        NSData *dataJSON = [NSURLConnection sendSynchronousRequest:request returningResponse:&serverResponse error:&requestError];
        NSDictionary *jsonDictionary = [NSJSONSerialization JSONObjectWithData:dataJSON options:NSJSONReadingMutableContainers error:&requestError];
        NSLog(@"Error Returning Dictionary: %@", requestError);
        return jsonDictionary;
    } @catch (NSException *e) {
        NSLog(@"Server Response Exception: %@", e);
        return nil;
    }
}

+(iSENSE*)getInstance {   
     static dispatch_once_t pred;
     static iSENSE *sharedInstance = nil;
    
     dispatch_once(&pred, ^{
        sharedInstance = [[iSENSE alloc] init];
     });
    
     return sharedInstance;
}

// Called internal to handle requests for new object
+(id)alloc {
	@synchronized([iSENSE class]) {
		NSAssert(_iSENSE == nil, @"Attempted to allocate a second instance of a singleton.");
		_iSENSE = [super alloc];
		return _iSENSE;
	}
	
	return nil;
}

// Called internally to initialize singleton
-(id)init {
	self = [super init];
	if (self != nil) {
        session_key = [[NSString alloc] init];
        username = [[NSString alloc] init];
        uid = [[NSNumber alloc] initWithInt:-1];
        
        username = nil;
        session_key = nil;
        uid = nil;
	}
	
	return self;
}

/* All of the following methods are obsolete */
/*- (id)retain {
 return self;
 }
 
 - (unsigned)retainCount {
 return UINT_MAX; //denotes an object that cannot be released
 }
 
 
 - (id)autorelease {
 return self;
 }
 
 -(oneway void)release {
 }
 
 - (NSZone *)zone {
 return zoneWhereAllocated ;
 }
 */
/* End of obsolete methods. */

// Use this method to access the session key if you are logged in.
- (NSString *) getSessionKey {
	return session_key;
}

// Use this method to access the user id number if you are logged in.
- (NSNumber *) getUID {
	return uid;
}

// Use this method to find out if you are logged in or not.
- (bool) isLoggedIn {
	if (session_key == nil || session_key == (id)[NSNull null]) {
		return FALSE;
    } else {
		return TRUE;
	}
}

// Use this method to access the current username if you are logged in.
- (NSString *) getLoggedInUsername {
	return username;
}

// Use this method to logout of iSENSE(dev)
- (void) logout {
	session_key = nil;
	username = nil;
	uid = [NSNumber numberWithInt:-1];
}

// Use this method to login to iSENSE(dev)
- (bool) login:(NSString *)user with:(NSString *)password {
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=login&username=%@&password=%@", user, password]];
    
    @try {
        session_key = [NSString stringWithString:[[result objectForKey:@"data"] valueForKey:@"session"]];
        uid = [[result objectForKey:@"data"] valueForKey:@"uid"];
	} @catch (NSException *e) {
        NSLog(@"Loggin Result Exception: %@", e);
        return FALSE;
    }
    
    if ([self isLoggedIn]) {
        
        NSString *successfulLoginName = [[NSString alloc] initWithString:user];
		username = successfulLoginName;
        
		return TRUE;
	} else username = nil;
	
	return FALSE;
}

- (bool) upload:(UIImage *)picture toExperiment:(NSNumber *)exp_id forSession:(NSNumber *)ses_id withName:(NSString *)name andDescription:(NSString *)description {

    if ([self isLoggedIn]) {
        
        // reference: stackoverflow.com/questions/8564833/ios-upload-image-and-text-using-http-post
        
        // fix strings to remove spaces
        name = [name stringByReplacingOccurrencesOfString:@" " withString:@"+"];
        description = [description stringByReplacingOccurrencesOfString:@" " withString:@"+"];
        
        // Dictionary that holds post parameters. You can set your post parameters that your server accepts or programmed to accept.
        NSMutableDictionary* params = [[NSMutableDictionary alloc] init];
        [params setObject:@"uploadImageToSession"  forKey:@"method"];
        [params setObject:session_key              forKey:@"session_key"];
        [params setObject:exp_id                   forKey:@"eid"];
        [params setObject:ses_id                   forKey:@"sid"];
        [params setObject:name                     forKey:@"img_name"];
        [params setObject:description              forKey:@"img_description"];
        
        // the boundary string : a random string, that will not repeat in post data, to separate post data fields.
        NSString *boundaryConstant = @"*****";
        
        // string constant for the post parameter 'image'
        NSString* fileParamConstant = @"image";
        
        // the server url to which the image (or the media) is uploaded.
        NSURL* requestURL = [NSURL URLWithString:baseURL];
        
        // create request
        NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
        [request setCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
        [request setHTTPShouldHandleCookies:NO];
        [request setTimeoutInterval:30];
        [request setHTTPMethod:@"POST"];
        
        // set Content-Type in HTTP header
        NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@", boundaryConstant];
        [request setValue:contentType forHTTPHeaderField: @"Content-Type"];
        
        // post body
        NSMutableData *body = [NSMutableData data];
        
        // add params (all params are strings)
        for (NSString *param in params) {
            [body appendData:[[NSString stringWithFormat:@"--%@\r\n", boundaryConstant] dataUsingEncoding:NSUTF8StringEncoding]];
            [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"\r\n\r\n", param] dataUsingEncoding:NSUTF8StringEncoding]];
            [body appendData:[[NSString stringWithFormat:@"%@\r\n", [params objectForKey:param]] dataUsingEncoding:NSUTF8StringEncoding]];
        }
        
        // add image data
        NSData *imageData = UIImageJPEGRepresentation(picture, 1.0);
        if (imageData) {
            [body appendData:[[NSString stringWithFormat:@"--%@\r\n", boundaryConstant] dataUsingEncoding:NSUTF8StringEncoding]];
            [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"; filename=\"image.jpg\"\r\n", fileParamConstant] dataUsingEncoding:NSUTF8StringEncoding]];
            [body appendData:[@"Content-Type: image/jpeg\r\n\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
            [body appendData:imageData];
            [body appendData:[[NSString stringWithFormat:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding]];
        }
        
        [body appendData:[[NSString stringWithFormat:@"--%@--\r\n", boundaryConstant] dataUsingEncoding:NSUTF8StringEncoding]];
        
        // setting the body of the post to the reqeust
        [request setHTTPBody:body];
        
        // set the content-length
        NSString *postLength = [NSString stringWithFormat:@"%d", [body length]];
        [request setValue:postLength forHTTPHeaderField:@"Content-Length"];
        
        // set URL
        [request setURL:requestURL];
        
        // submit request and receive response
        NSURLResponse *resp = nil;
        NSError *err = nil;
        NSData *response = [NSURLConnection sendSynchronousRequest:request returningResponse:&resp error:&err];
        NSString * respString = [[NSString alloc] initWithData:response encoding:NSUTF8StringEncoding];
        NSLog(@"response: %@", respString);
        
        if ([respString rangeOfString:@"200"].location == NSNotFound)
            return false;
        else
            return true;
        
    }
	
	return false;
}

/*
 * Use this function to access experiments and their data.
 * 
 * Exp_id: The experiment id you want to get data from.
 */
- (Experiment *) getExperiment:(NSNumber *)exp_id {
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=getExperiment&experiment=%@", exp_id]];
	NSArray *data = [result objectForKey:@"data"];
	Experiment *e = [Experiment new];
	
	if (data) {
		[e setExperiment_id:[data valueForKey:@"experiment_id"]];
		[e setOwner_id:[data valueForKey:@"owner_id"]];
		[e setName:[data valueForKey:@"name"]];
		[e setDescription:[data valueForKey:@"description"]];
		[e setTimecreated:[data valueForKey:@"timecreated"]];
		[e setTimemodified:[data valueForKey:@"timemodeified"]];
		[e setDefault_read:[data valueForKey:@"deafault_read"]];
		[e setDefault_join:[data valueForKey:@"deafault_join"]];
		[e setFeatured:[data valueForKey:@"featured"]];
		[e setRating:[data valueForKey:@"rating"]];
		[e setRating_votes:[data valueForKey:@"rating_votes"]];
		[e setHidden:[data valueForKey:@"hidden"]];
        [e setActivity:[data valueForKey:@"activity"]];
        [e setActivity_for:[data valueForKey:@"activity_for"]];
        [e setReq_name:[data valueForKey:@"req_name"]];
        [e setReq_procedure:[data valueForKey:@"req_procedure"]];
        [e setReq_location:[data valueForKey:@"req_location"]];
        [e setName_prefix:[data valueForKey:@"name_prefix"]];
        [e setLocation:[data valueForKey:@"location"]];
        [e setClosed:[data valueForKey:@"closed"]];
        [e setExp_image:[data valueForKey:@"exp_image"]];
        [e setRecommended:[data valueForKey:@"recommended"]];
        [e setDefault_vis:[data valueForKey:@"default_vis"]];
		[e setFirstname:[data valueForKey:@"firstname"]];
        [e setLastname:[data valueForKey:@"lastname"]];
        [e setSrate:[data valueForKey:@"srate"]];
	}
	
	return e;
    
}

// Use this method to access the data belonging to a given session.
- (NSMutableArray *) sessionData:(NSString *)sessions {
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=sessiondata&sessions=%@", sessions]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *sessiondata = [NSMutableArray new];
	
	if (data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			SessionData *s = [SessionData new];
			[s setRawJSON:object];
			[s setDataJSON:[object objectForKey:@"data"]];
			[s setMetaDataJSON:[object objectForKey:@"meta"]];
			[s setFieldsJSON:[object objectForKey:@"fields"]];
			[s setFieldData:[NSMutableArray new]];
			
			int fieldCount = [[s FieldsJSON] count];
			
			for (int i = 0; i < fieldCount; i++) {
				NSMutableArray *temp = [NSMutableArray new];
				NSEnumerator *d = [[s DataJSON] objectEnumerator];
				id row;
				while (row = [d nextObject]) {
					[temp addObject:[row objectAtIndex:i]];
				}
				[[s fieldData] addObject:temp];
			}
            
			[sessiondata addObject:s];
		}
	}
	
	return sessiondata;
}

// Use this method to go through iSENSE users.
- (NSMutableArray *) getPeople:(NSNumber *)fromPage withPageSize:(NSNumber *)limit withAction:(NSString *)action andQuery:(NSString *)query {
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=getPeople&page=%@&limit=%@&action=%@&query=%@", fromPage, limit, action, query]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *people = [NSMutableArray new];
	
	if(data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			Person *p = [Person new];
			
			[p setUser_id:[object objectForKey:@"user_id"]];
			[p setFirstname:[object objectForKey:@"firstname"]];
			[p setLastname:[object objectForKey:@"lastname"]];
			[p setConfirmed:[object objectForKey:@"confirmed"]];
			[p setEmail:[object objectForKey:@"email"]];
			[p setIcq:[object objectForKey:@"icq"]];
			[p setSkype:[object objectForKey:@"skype"]];
			[p setYahoo:[object objectForKey:@"yahoo"]];
			[p setAim:[object objectForKey:@"aim"]];
			[p setMsn:[object objectForKey:@"msn"]];
			[p setInstitution:[object objectForKey:@"institution"]];
			[p setDepartment:[object objectForKey:@"department"]];
			[p setStreet:[object objectForKey:@"stree"]];
			[p setCity:[object objectForKey:@"city"]];
			[p setCountry:[object objectForKey:@"country"]];
			[p setLongitude:[object objectForKey:@"longitude"]];
			[p setLatitude:[object objectForKey:@"latitude"]];
			[p setLanguage:[object objectForKey:@"language"]];
			[p setFirstaccess:[object objectForKey:@"firstaccess"]];
			[p setLastaccess:[object objectForKey:@"lastaccess"]];
			[p setLastlogin:[object objectForKey:@"lastlogin"]];
			[p setPicture:[object objectForKey:@"picture"]];
			[p setUrl:[object objectForKey:@"url"]];
			[p setExperiment_count:[object objectForKey:@"experiment_count"]];
			[p setSession_count:[object objectForKey:@"session_count"]];
			
			[people addObject:p];
		}
	}
	
	return people;
}

// Use this method to retrieve specific user data.
- (Item *) getProfile:(NSNumber *)user_id {
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=getUserProfile&user=%@", user_id]];
	NSArray *data = [result objectForKey:@"data"];
	
	Item *i = [Item new];
	
	if (data) {
		NSArray *experiments = [data valueForKey:@"experiments"];
		
		NSArray *sessions = [data valueForKey:@"sessions"];
		
		NSArray *images = [data valueForKey:@"images"];
		
		NSEnumerator *e = [experiments objectEnumerator];
		id exp;
		[i setExperiments:[NSMutableArray new]];
		while (exp = [e nextObject]) {
			Experiment *experiment = [Experiment new];
			[experiment setExperiment_id:[exp objectForKey:@"experiment_id"]];
			[experiment setOwner_id:[exp objectForKey:@"owner_id"]];
			[experiment setName:[exp objectForKey:@"name"]];
			[experiment setDescription:[exp objectForKey:@"description"]];
			[experiment setTimecreated:[exp objectForKey:@"tiemcreated"]];
			[experiment setTimemodified:[exp objectForKey:@"timemmodified"]];
			[experiment setHidden:[exp objectForKey:@"hidden"]];
			
			[[i experiments] addObject:experiment];
		}
		
		e = [sessions objectEnumerator];
		id ses;
		[i setSessions:[NSMutableArray new]];
		while (ses = [e nextObject]) {
			Session *session = [Session new];
			[session setSession_id:[ses objectForKey:@"session_id"]];
			[session setExperiment_id:[ses objectForKey:@"experiment_id"]];
			[session setName:[ses objectForKey:@"name"]];
			[session setDescription:[ses objectForKey:@"description"]];
			[session setLatitude:[ses objectForKey:@"latitude"]];
			[session setLongitude:[ses objectForKey:@"longitude"]];
			[session setTimecreated:[ses objectForKey:@"timeobj"]];
			[session setTimemodified:[ses objectForKey:@"timemodified"]];
			
			[[i sessions] addObject:session];
		}
		
		e = [images objectEnumerator];
		id img;
		[i setImages:[NSMutableArray new]];
		while (img = [e nextObject]) {
			Image *image = [Image new];
			[image setTitle:[img objectForKey:@"title"]];
			[image setExperiment_id:[img objectForKey:@"experiment_id"]];
			[image setPicture_id:[img objectForKey:@"picture_id"]];
			[image setDescription:[img objectForKey:@"description"]];
			[image setProvider_url:[img objectForKey:@"provider_url"]];
			[image setProvider_id:[img objectForKey:@"provider_id"]];
			[image setTimecreated:[img objectForKey:@"timecreated"]];
			
			[[i images] addObject:image];
		}
	}
	
	return i;
	
}

/*
 * Returns an array of experiment objects.
 *
 * Limit: How many results do you want returned. Defaults to 10.
 * Page: Depends on limit.  Assuming a limit of 10,  page 2 would yield results 11-20.
 * Query: Use this to search for experiments by keyword.
 * Sort: Sort results like on iSENSE.  Accepts "recent", "rating", "activity", and "popularity".
 */
- (NSMutableArray *) getExperiments:(NSNumber *)fromPage withLimit:(NSNumber *)limit withQuery:(NSString *)query andSort:(NSString *)sort {
    
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=getExperiments&page=%@&limit=%@&query=%@&sort=%@", fromPage, limit, query, sort]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *experiments = [NSMutableArray new];
    
	if(data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			NSDictionary *meta = [object objectForKey:@"meta"];
            NSDictionary *obj = (NSDictionary *)object;
			Experiment *exp = [Experiment new];
			
			[exp setExperiment_id:[meta objectForKey:@"experiment_id"]];
			[exp setOwner_id:[meta objectForKey:@"owner_id"]];
			[exp setName:[meta objectForKey:@"name"]];
			[exp setDescription:[meta objectForKey:@"description"]];
			[exp setTimecreated:[meta objectForKey:@"timecreated"]];
			[exp setTimemodified:[meta objectForKey:@"timemodeified"]];
			[exp setDefault_read:[meta objectForKey:@"deafault_read"]];
			[exp setDefault_join:[meta objectForKey:@"deafault_join"]];
			[exp setFeatured:[meta objectForKey:@"featured"]];
			[exp setRating:[meta objectForKey:@"rating"]];
			[exp setRating_votes:[meta objectForKey:@"rating_votes"]];
			[exp setHidden:[meta objectForKey:@"hidden"]];
			[exp setFirstname:[meta objectForKey:@"firstname"]];
            [exp setActivity:[meta objectForKey:@"activity"]];
            [exp setActivity_for:[meta objectForKey:@"activity_for"]];
            [exp setReq_name:[meta objectForKey:@"req_name"]];
            [exp setReq_procedure:[meta objectForKey:@"req_procedure"]];
            [exp setReq_location:[meta objectForKey:@"req_location"]];
            [exp setName_prefix:[meta objectForKey:@"name_prefix"]];
            [exp setLocation:[meta objectForKey:@"location"]];
            [exp setClosed:[meta objectForKey:@"closed"]];
            [exp setExp_image:[meta objectForKey:@"exp_image"]];
            [exp setRecommended:[meta objectForKey:@"recommended"]];
            [exp setSrate:[meta objectForKey:@"srate"]];
            [exp setDefault_vis:[meta objectForKey:@"default_vis"]];
            [exp setRating_comp:[meta objectForKey:@"rating_comp"]];
            [exp setTags:[obj objectForKey:@"tags"]];
            [exp setRelevancy:[obj objectForKey:@"relevancy"]];
            [exp setContrib_count:[obj objectForKey:@"contrib_count"]];
            [exp setSession_count:[obj objectForKey:@"session_count"]];
            
            [experiments addObject:exp];
		}
	}
	
	return experiments;
	
}

// Use this method to retrieve images associated with an experiment (may be deprecated).
- (NSMutableArray *) getExperimentImages:(NSNumber *)exp_id {
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=getExperimentImages&experiment=%@", exp_id]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *images = [NSMutableArray new];
    if (data && data != (id)[NSNull null]) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			Image *image = [Image new];
			
			[image setTitle:[object objectForKey:@"title"]];
			[image setExperiment_id:[object objectForKey:@"experiment_id"]];
			[image setPicture_id:[object objectForKey:@"picture_id"]];
			[image setDescription:[object objectForKey:@"description"]];
			[image setProvider_url:[object objectForKey:@"provider_url"]];
			[image setProvider_id:[object objectForKey:@"provider_id"]];
			[image setTimecreated:[object objectForKey:@"timecreated"]];
			
			[images addObject:image];
		}
	}
	
	return images;
}

// Use this method to retrieve videos associated with an experiment (may be deprecated).
- (NSMutableArray *) getExperimentVideos:(NSNumber *)exp_id {
	return [NSMutableArray new];
}

- (NSMutableArray *) getExperimentTags:(NSNumber *)exp_id {
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=getExperimentTags&experiment=%@", exp_id]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *tags = [NSMutableArray new];
	
	if (data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			[tags addObject:[object objectForKey:@"tag"]];
		}
	}
	
	return tags;
}

/*
 * Use this method to get the experiment fields associated with an expiriment.
 * Please note that we don't store the experiment id belonging to the field, as we already know which experiment it belongs to.
 *
 * Exp_id: The experiment id whose fields who want.
 */
- (NSMutableArray *) getExperimentFields:(NSNumber *)exp_id {
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=getExperimentFields&experiment=%@", exp_id]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *fields = [NSMutableArray new];
	
	if (data) {
        NSEnumerator *e;
        @try {
            e = [data objectEnumerator];
        }
        @catch (NSException *exception) {
            NSLog(@"Empty array returned");
            return fields;
        }

		id object;
		while (object = [e nextObject]) {
			ExperimentField *expField = [ExperimentField new];
			
			[expField setField_id:[object objectForKey:@"field_id"]];
			[expField setField_name:[object objectForKey:@"field_name"]];
			[expField setType_id:[object objectForKey:@"type_id"]];
			[expField setType_name:[object objectForKey:@"type_name"]];
			[expField setUnit_abbreviation:[object objectForKey:@"unit_abbreviation"]];
			[expField setUnit_id:[object objectForKey:@"unit_id"]];
			[expField setUnit_name:[object objectForKey:@"unit_name"]];
			
			[fields addObject:expField];
		}
	}
	
	return fields;
}

// Use this method to retrieve sessions associated with an experiment (may be deprecated).
- (NSMutableArray *) getSessions:(NSNumber *)exp_id {
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=getSessions&experiment=%@", exp_id]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *sessions = [NSMutableArray new];
	
	if (data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			Session *ses = [Session new];
			
			[ses setSession_id:[object objectForKey:@"session_id"]];
			[ses setOwner_id:[object objectForKey:@"object_id"]];
			[ses setExperiment_id:[object objectForKey:@"object_id"]];
			[ses setName:[object objectForKey:@"name"]];
			[ses setDescription:[object objectForKey:@"description"]];
			[ses setStreet:[object objectForKey:@"street"]];
			[ses setCity:[object objectForKey:@"city"]];
			[ses setCountry:[object objectForKey:@"country"]];
			[ses setLatitude:[object objectForKey:@"latitude"]];
			[ses setLongitude:[object objectForKey:@"longitude"]];
			[ses setTimecreated:[object objectForKey:@"timecreated"]];
			[ses setTimemodified:[object objectForKey:@"timemodified"]];
			[ses setDebug_data:[object objectForKey:@"debug_data"]];
			[ses setFirstname:[object objectForKey:@"firstname"]];
			[ses setLastname:[object objectForKey:@"lastname"]];
			
			[sessions addObject:ses];
		}
	}
    
	return sessions;
}

// Use this method to create a session and receive its session number (may be deprecated).
// The API specification actually contains 'state' as opposed to 'country,' but historically we've always used country
- (NSNumber *) createSession:(NSString *)name withDescription:(NSString *)description Street:(NSString *)street City:(NSString *)city Country:(NSString *)country toExperiment:(NSNumber *)exp_id {
    if ([self isLoggedIn]) {
        NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=createSession&session_key=%@&eid=%@&name=%@&description=%@&street=%@&city=%@&country=%@", session_key, exp_id, name, description, street, city, country]];
        
        NSNumberFormatter *f = [[NSNumberFormatter alloc] init];
        [f setNumberStyle:NSNumberFormatterDecimalStyle];
        NSNumber *sid = [f numberFromString:[[result objectForKey:@"data"] valueForKey:@"sessionId"]];

        return sid;
    }
	return NULL;
}

// Use this method to add data to a session (may be deprecated).
- (bool) putSessionData:(NSData *)dataJSON forSession:(NSNumber *)session_id inExperiment:(NSNumber *)exp_id {
    if ([self isLoggedIn]) {
        
        NSString *dataAsString = [[NSString alloc] initWithData:dataJSON encoding:NSUTF8StringEncoding];
        NSLog(@"Session Key: = %@", session_key);
        NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=putSessionData&session_key=%@&sid=%@&eid=%@&data=%@", session_key, session_id, exp_id, dataAsString]];
        NSArray *data = [result objectForKey:@"data"];
        
        if (data) {
            return true;
        }
    }
	
	return false;
}

// Use this method to add data to a session (may be deprecated). Duplicated of putSessionData.
- (bool) updateSessionData:(NSString *)dataJSON forSession:(NSNumber *)session_id inExperiment:(NSNumber *)exp_id {
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=updateSessionData&session_key=%@&sid=%@&eid=%@&data=%@", session_key, session_id, exp_id, dataJSON]];
	NSArray *data = [result objectForKey:@"data"];
	
	if (data) {
		return true;
	}
	
	return false;
}

// Use this method to toggle between using iSENSE and iSENSE dev
- (void) toggleUseDev:(BOOL)toggle {
	if (toggle) {
		baseURL = @"http://isensedev.cs.uml.edu/ws/api.php?";
	} else {
		baseURL = @"http://isense.cs.uml.edu/ws/api.php?";
	}
}

// Use this method to determine if internet connectivity is available
- (bool) isConnectedToInternet {
    NSString *URLString = [NSString stringWithContentsOfURL:[NSURL URLWithString:@"http://isenseproject.org"] encoding:NSASCIIStringEncoding error:nil];
    return ( URLString != NULL ) ? true : false;
}

@end
