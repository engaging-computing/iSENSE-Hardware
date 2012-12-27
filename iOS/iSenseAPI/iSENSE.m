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

-(NSDictionary *)isenseQuery:(NSString*)target
{
	NSLog(@"Reached isenseQuery");
	NSMutableString *base_url = [NSMutableString stringWithString:baseURL];
	NSLog(@"%@", base_url);
	[base_url appendString:target];
	NSString *final = [base_url stringByReplacingOccurrencesOfString:@" " withString:@"+"];
	NSLog(@"Sent to iSENSE: %@", final);
	NSError *requestError;
	return [[NSString stringWithContentsOfURL:[NSURL URLWithString:final] encoding:NSUTF8StringEncoding error:&requestError] JSONValue];
}

+(iSENSE*)instance
{
	@synchronized([iSENSE class])
	{
		if (!_iSENSE)
			[[self alloc] init];
		NSLog(@"Initialized iSENSE object.");
		
		return _iSENSE;
	}
	
	return nil;
}

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

-(id)init {
	self = [super init];
	if (self != nil) {
		username = [[NSString alloc] autorelease];
		session_key =[[NSString alloc] autorelease];
		uid = [[NSNumber alloc] autorelease];
	}
	
	return self;
}

- (id)retain {
    return self;
}
- (unsigned)retainCount {
    return UINT_MAX; //denotes an object that cannot be released
}
- (void)release {
    // never release
}
- (id)autorelease {
    return self;
}

- (void)dealloc {
    // Should never be called, but just here for clarity really.
    [username release];
	[session_key release];
	[uid release];
	[_iSENSE release];
    [super dealloc];
}

- (NSString *) getSessionKey {
	return session_key;
}

- (NSNumber *) getUID {
	return uid;
}

- (bool) isLoggedIn {
	if (session_key) {
		if ([session_key isEqualToString:@""]) {
			return FALSE;
		} else {
			return TRUE;
		}
	} else {
		return FALSE;
	}
}

- (NSString *) getLoggedInUsername {
	return username;
}

- (void) logout {
	session_key = NULL;
	username = NULL;
	uid = [NSNumber numberWithInt:-1];
}

- (bool) login:(NSString *)User with:(NSString *)Password {
	NSLog(@"Login starts.");
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=login&username=%@&password=%@", User, Password]];
	NSLog(@"Result Obtained");
	session_key = [[result objectForKey:@"data"] valueForKey:@"session"];
	NSLog(@"session_key = %d.", session_key);
	uid = [[result objectForKey:@"data"] valueForKey:@"uid"];
	if ([self isLoggedIn]) {
		username = User;
		return TRUE;
	}
	
	return FALSE;
}

//- (bool) upload:(NSFile)Picture toExperiment:(NSNumber *)exp_id withName:(NSString *)name andDescirption:(NSString *)description {
//}

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

- (NSNumber *) createSession:(NSString *)name withDescription:(NSString *)description Street:(NSString *)street City:(NSString *)city Country:(NSString *)country toExperiment:(NSNumber *)exp_id {
	NSDictionary *result  = [self isenseQuery:[NSString stringWithFormat:@"method=createSession&session_key=%@&eid=%@&name=%@&description=%@&street=%@&city=%@&country=%@", session_key, exp_id, name, description, street, city, country]];
	NSNumber *sid = [[NSNumber alloc] autorelease];
	
	sid = [[result objectForKey:@"data"] valueForKey:@"sessionId"];
	
	return sid;
}

- (bool) putSessionData:(NSString *)dataJSON forSession:(NSNumber *)session_id inExperiment:(NSNumber *)exp_id {
	NSLog(@"%@", session_id);
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=putSessionData&session_key=%@&sid=%@&eid=%@&data=%@", session_key, session_id, exp_id, dataJSON]];
	NSArray *data = [result objectForKey:@"data"];
	
	if (data) {
		return true;
	}
	
	return false;
}

- (bool) updateSessionData:(NSString *)dataJSON forSession:(NSNumber *)session_id inExperiment:(NSNumber *)exp_id {
	NSDictionary *result = [self isenseQuery:[NSString stringWithFormat:@"method=updateSessionData&session_key=%@&sid=%@&eid=%@&data=%@", session_key, session_id, exp_id, dataJSON]];
	NSArray *data = [result objectForKey:@"data"];
	
	if (data) {
		return true;
	}
	
	return false;
}

- (void) useDev:(BOOL)toggle {
	if (toggle) {
		baseURL = @"http://isensedev.cs.uml.edu/ws/api.php?";
		NSLog(@"Switched to dev.");
	} else {	
		baseURL = @"http://isense.cs.uml.edu/ws/api.php?";
		NSLog(@"Switched to iSENSE.");

	}
}



@end
