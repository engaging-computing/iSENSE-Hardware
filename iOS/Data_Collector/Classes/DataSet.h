//
//  DataSet.h
//  Data_Collector
//
//  Created by Michael Stowell on 2/5/13.
//
//

#import <Foundation/Foundation.h>

typedef enum Type {
    DATA, PICTURE, BOTH
} Type;

@interface DataSet : NSObject {
    
    Type type;
    NSString *name;
    NSString *desc;
    NSString *eid;
    
    BOOL readyForUpload;
    
    NSString *data; // json in string format?
    //FILE *picture; // honestly have no idea what picture needs to be
    int sid;
    NSString *city;
    NSString *state;
    NSString *country;
    NSString *addr;
    
}

@property (nonatomic) Type type;
@property (nonatomic, retain) NSString *name;
@property (nonatomic, retain) NSString *desc;
@property (nonatomic, retain) NSString *eid;
@property (nonatomic) BOOL readyForUpload;
@property (nonatomic, retain) NSString *data;
//@property (nonatomic/*, retain*/) FILE *picture;
@property (nonatomic) int sid;
@property (nonatomic, retain) NSString *city;
@property (nonatomic, retain) NSString *state;
@property (nonatomic, retain) NSString *country;
@property (nonatomic, retain) NSString *addr;


@end