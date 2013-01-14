//
//  iSENSE.m
//  isenseAPI
//
//  Created by John Fertitta on 2/23/11.
//  Updated by Jeremy Poulin on 12/05/12.
//  Copyright 2011 UMass Lowell. All rights reserved.
//

#import "iSENSE.h"

static NSString *baseURL = @"http://isense.cs.uml.edu/ws/api.php?";
static iSENSE* _iSENSE = nil;

@implementation iSENSE

// Makes a request to iSENSE and parse the JSONObject it gets back
-(NSDictionary *)isenseQuery:(NSString*)target
{
	NSMutableString *base_url = [NSMutableString stringWithString:baseURL];
	[base_url appendString:target];
	NSString *final = [base_url stringByReplacingOccurrencesOfString:@" " withString:@"+"];
	NSLog(@"Sent to iSENSE: %@", final);
	NSError *requestError = nil;
	NSData *dataJSON = [NSData dataWithContentsOfURL:[NSURL URLWithString:final] options:NSDataReadingMappedIfSafe error:&requestError];
    NSLog(@"Error During Communication: %@", requestError);
    NSLog(@"Server Response: %@", [NSString stringWithUTF8String:[dataJSON bytes]]);
    NSDictionary *jsonDictionary = [[NSDictionary alloc] autorelease];
    jsonDictionary = [NSJSONSerialization JSONObjectWithData:dataJSON options:NSJSONReadingMutableContainers error:&requestError];
    NSLog(@"Error Returning Dictionary: %@", requestError);
    return jsonDictionary;
}

+(iSENSE*)getInstance
{
	@synchronized([iSENSE class])
	{
		if (!_iSENSE)
			[[self alloc] init];
	
		return _iSENSE;
	}
	
	return nil;
}

// Called internal to handle requests for new object
+(id)alloc
{
	@synchronized([iSENSE class])
	{
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
		username = [[NSString alloc] autorelease];
		session_key =[[NSString alloc] autorelease];
		uid = [[NSNumber alloc] autorelease];
        
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
 
 - (void)dealloc {
 // Should never be called, but just here for clarity really.
 [username release];
 [session_key release];
 [uid release];
 [_iSENSE release];
 [super dealloc];
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
    NSLog(@"Session Key: %@", session_key);
	if (session_key == nil) {
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
   	
    session_key = [[result objectForKey:@"data"] valueForKey:@"session"];
	uid = [[result objectForKey:@"data"] valueForKey:@"uid"];
	
    if ([self isLoggedIn]) {
		username = user;
		return TRUE;
	}
	
	return FALSE;
}

// To be completed
//- (bool) upload:(NSFile)Picture toExperiment:(NSNumber *)exp_id withName:(NSString *)name andDescirption:(NSString *)description {
//}

// Use this function to access experiments and their data.
- (Experiment *) getExperiment:(NSNumber *)exp_id {
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=getExperiment&experiment=%@", exp_id]];
	NSArray *data = [result objectForKey:@"data"];
	Experiment *e = [[Experiment new] autorelease];
	
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
		[e setFirstname:[data valueForKey:@"firstname"]];
		[e setLastname:[data valueForKey:@"lastname"]];
	}
	
	return e;
    
}

// Use this method to access the data belonging to a given session.
- (NSMutableArray *) sessionData:(NSString *)sessions {
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=sessiondata&sessions=%@", sessions]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *sessiondata = [[NSMutableArray new] autorelease];
	
	if (data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			SessionData *s = [[SessionData new] autorelease];
			[s setRawJSON:object];
			[s setDataJSON:[object objectForKey:@"data"]];
			[s setMetaDataJSON:[object objectForKey:@"meta"]];
			[s setFieldsJSON:[object objectForKey:@"fields"]];
			[s setFieldData:[[NSMutableArray new] autorelease]];
			
			int fieldCount = [[s FieldsJSON] count];
			
			for (int i = 0; i < fieldCount; i++) {
				NSMutableArray *temp = [[NSMutableArray new] autorelease];
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
	
	NSMutableArray *people = [[NSMutableArray new] autorelease];
	
	if(data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			Person *p = [[Person new] autorelease];
			
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
	
	Item *i = [[Item new] autorelease];
	
	if (data) {
		NSArray *experiments = [data valueForKey:@"experiments"];
		
		NSArray *sessions = [data valueForKey:@"sessions"];
		
		NSArray *images = [data valueForKey:@"images"];
		
		NSEnumerator *e = [experiments objectEnumerator];
		id exp;
		[i setExperiments:[[NSMutableArray new] autorelease]];
		while (exp = [e nextObject]) {
			Experiment *experiment = [[Experiment new] autorelease];
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
		[i setSessions:[[NSMutableArray new] autorelease]];
		while (ses = [e nextObject]) {
			Session *session = [[Session new] autorelease];
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
		[i setImages:[[NSMutableArray new] autorelease]];
		while (img = [e nextObject]) {
			Image *image = [[Image new] autorelease];
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

// Use this method to retrieve experiments (may be deprecated).
- (NSMutableArray *) getExperiments:(NSNumber *)fromPage withPageSize:(NSNumber *)limit withAction:(NSString *)action andQuery:(NSString *)query {
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=getExperiments&page=%@&limit=%@&action=%@&query=%@", fromPage, limit, action, query]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *experiments = [[NSMutableArray new] autorelease];
    
	if(data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			NSDictionary *meta = [object objectForKey:@"meta"];
			Experiment *exp = [[Experiment new] autorelease];
			
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
			[exp setLastname:[meta objectForKey:@"lastname"]];
			
			[experiments addObject:exp];
		}
	}
	
	return experiments;
	
}

// Use this method to retrieve images associated with an experiment (may be deprecated).
- (NSMutableArray *) getExperimentImages:(NSNumber *)exp_id {
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=getExperimentImages&experiment=%@", exp_id]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *images = [[NSMutableArray new] autorelease];
	
	if (data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			Image *image = [[Image new] autorelease];
			
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
	return [[NSMutableArray new] autorelease];
}

- (NSMutableArray *) getExperimentTags:(NSNumber *)exp_id {
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=getExperimentTags&experiment=%@", exp_id]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *tags = [[NSMutableArray new] autorelease];
	
	if (data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			[tags addObject:[object objectForKey:@"tag"]];
		}
	}
	
	return tags;
}

// Use this method to get the experiment fields associated with an expiriment
- (NSMutableArray *) getExperimentFields:(NSNumber *)exp_id {
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=getExperimentFields&experiment=%@", exp_id]];
	NSArray *data = [result objectForKey:@"data"];
	
	NSMutableArray *fields = [[NSMutableArray new] autorelease];
	
	if (data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			ExperimentField *expField = [[ExperimentField new] autorelease];
			
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
	
	NSMutableArray *sessions = [[NSMutableArray new] autorelease];
	
	if (data) {
		NSEnumerator *e = [data objectEnumerator];
		id object;
		while (object = [e nextObject]) {
			Session *ses = [[Session new] autorelease];
			
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
        NSNumber *sid = [[NSNumber alloc] autorelease];
	
        sid = [[result objectForKey:@"data"] valueForKey:@"sessionId"];
            return sid;
    }
	return NULL;
}

// Use this method to add data to a session (may be deprecated).
- (bool) putSessionData:(NSString *)dataJSON forSession:(NSNumber *)session_id inExperiment:(NSNumber *)exp_id {
	NSLog(@"%@", session_id);
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=putSessionData&session_key=%@&sid=%@&eid=%@&data=%@", session_key, session_id, exp_id, dataJSON]];
	NSArray *data = [result objectForKey:@"data"];
	
	if (data) {
		return true;
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



@end
