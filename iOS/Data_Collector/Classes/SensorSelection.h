//
//  SensorSelection.h
//  Data_Collector
//
//  Created by Michael Stowell on 7/2/13.
//
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>
#import <CoreMotion/CoreMotion.h>

#import <iSENSE_API/DataFieldManager.h>
#import <iSENSE_API/SensorCompatibility.h>
#import <iSENSE_API/SensorEnums.h>

@interface SensorSelection : UIViewController {
 
    DataFieldManager *dfm;
    NSMutableArray *fieldNames;
    NSMutableArray *compatible;
    NSMutableArray *selectedCells;
    
}

- (IBAction) okOnClick:(id)sender;

@property (nonatomic, retain) IBOutlet UITableView *table;
@property (nonatomic, retain) IBOutlet UIButton *ok;

@property (nonatomic) int fieldNumber;


@end

