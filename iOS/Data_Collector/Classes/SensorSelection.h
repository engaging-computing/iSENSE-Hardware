//
//  SensorSelection.h
//  Data_Collector
//
//  Created by Michael Stowell on 7/2/13.
//
//

#import <UIKit/UIKit.h>

@interface SensorSelection : UIViewController {
    
}

- (IBAction)okOnClick:(id)sender;

@property (nonatomic, retain) IBOutlet UITableView *table;
@property (nonatomic, retain) IBOutlet UIButton *ok;

@property (nonatomic) int fieldNumber;


@end

