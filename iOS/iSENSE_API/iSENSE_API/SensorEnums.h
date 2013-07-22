//
//  SensorEnums.h
//  iSENSE API
//
//  Created by Mike Stowell on 7/1/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#ifndef iSENSE_API_SensorTypeEnum_h
#define iSENSE_API_SensorTypeEnum_h

typedef enum SensorTypes {
    sGPS = 0, sACCELEROMETER = 1, sAMBIENT_LIGHT = 2, sGYROSCOPE = 3, sPROXIMITY = 4
} SensorTypes;

typedef enum DispatchValues {
    NOT_AVAILABLE = 0, AVAILABLE = 1, AVAIL_CONNECTIVITY = 2, AVAIL_WIFI_ONLY = 3, NOT_DETECTED = 4
} DispatchValues;

#endif
