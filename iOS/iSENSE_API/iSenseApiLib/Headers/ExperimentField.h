//
//  ExperimentField.h
//  isenseAPI
//
//  Created by James Dalphond on 2/23/11.
//  Copyright 2011 UMass Lowell. All rights reserved.
//
//  Modified by John Fertitta on 3/1/11.
//

#import <Foundation/Foundation.h>

// type_id as-is in the database
#define TEMPERATURE 1
#define LENGTH 2
#define DISTANCE 3
#define FORCE 4
#define VOLUME 5
#define MASS 6
#define TIME 7
#define LUMINOUS_FLUX 8
#define LUMINOUS_INTENSITY 9
#define ANGLE 10
#define ELECTRIC_POTENTIAL 11
#define ELECTRIC_CURRENT 12
#define POWER 13
#define ELECTRIC_CHARGE 14
#define SPEED 15
#define BOOLEAN 16
#define PERCENTAGE 17
#define ANALOG 18
#define GEOSPACIAL 19
#define RATE 20
#define NUMERIC 21
#define CUSTOM 22
#define SALINITY 23
#define PH_LEVEL 24
#define ACCELERATION 25
#define UV 26
#define PRESSURE 27
#define HUMIDITY 28
#define LIGHT 29
#define DISSOLVED_OXYGEN 30
#define ANEMOMETER 31
#define TURBIDITY 32
#define FLOW_RATE 33
#define MOTOR_MONITOR 34
#define CONDUCTIVITY 35
#define CONCENTRATION 36
#define TEXT 37

// unit_id as-is in the database
#define UNIT_KELVIN 1
#define UNIT_CELSIUS 2
#define UNIT_FAHRENHEIT 3
#define UNIT_KILOMETER 4
#define UNIT_METER 5
#define UNIT_CENTIMETER 6
#define UNIT_MILLIMETER 7
#define UNIT_NANOMETER 8
#define UNIT_MILE 9
#define UNIT_YARD 10
#define UNIT_FOOT 11
#define UNIT_INCH 12
#define UNIT_POUND 13
#define UNIT_NEWTON 14
#define UNIT_LITER 15
#define UNIT_MILLILITER 16
#define UNIT_GALLON 17
#define UNIT_QUART 18
#define UNIT_KILOGRAM 19
#define UNIT_GRAM 20
#define UNIT_MILLIGRAM 21
#define UNIT_UNIX_TIME 22
#define UNIT_JULIAN_YEAR 23
#define UNIT_DAY 24
#define UNIT_SECOND 25
#define UNIT_HOUR 26
#define UNIT_MINUTE 27
#define UNIT_MILLISECOND 28
#define UNIT_NANOSECOND 29
#define UNIT_LUMEN 30
#define UNIT_CANDELA 31
#define UNIT_DEGREE 32
#define UNIT_RADIAN 33
#define UNIT_VOLT 34
#define UNIT_AMPERE 35
#define UNIT_MILLIAMP 36
#define UNIT_WATT 37
#define UNIT_MILLIWATT 38
#define UNIT_KILOWATT 39
#define UNIT_MEGAWATT 40
#define UNIT_GIGAWATT 41
#define UNIT_COULOMB 42
#define UNIT_MICROCOULOMB 43
#define UNIT_NANOCOULOMB 44
#define UNIT_METERS_PER_SECOND 45
#define UNIT_KILOMETERS_PER_SECOND 46
#define UNIT_KILOMETERS_PER_HOUR 46
#define UNIT_FEET_PER_SECOND 47
#define UNIT_MILES_PER_HOUR 48
#define UNIT_PICOBOARD_LIGHT 49
#define UNIT_PICOBOARD_SOUND 50
#define UNIT_PICOBOARD_BUTTON 51
#define UNIT_SUNSPOT_LIGHT 52
#define UNIT_TOGGLE_SWITCH 53
#define UNIT_PICOBOARD_SLIDER 54
#define UNIT_SUNSPOT_BUTTON 55
#define UNIT_ANALOG 56
#define UNIT_LATITUDE 57
#define UNIT_LONGITUDE 58
#define UNIT_PER_SECOND 59
#define UNIT_PER_MINUTE 60
#define UNIT_PER_HOUR 61
#define UNIT_PER_DAY 62
#define UNIT_PER_WEEK 63
#define UNIT_PER_MONTH 64
#define UNIT_PER_YEAR 65
#define UNIT_NUMBER 66
#define UNIT_CUSTOM 67
#define UNIT_LUX 68
#define UNIT_SALINITY 69
#define UNIT_PH_LEVEL 70
#define UNIT_METERS_PER_SECOND_SQUARED 71
#define UNIT_UVA 72
#define UNIT_UVB 73
#define UNIT_ATMOSPHERES 74
#define UNIT_PASCALS 75
#define UNIT_RELATIVE_HUMIDITY 76
#define UNIT_PARTS_PER_MILLION 77
#define UNIT_NEPHELOMETRIC_TURBIDITY_UNITS 78
#define UNIT_SIEMENS 79
#define UNIT_PARTS_PER_MILLION2 80
#define UNIT_TEXT 81


@interface ExperimentField : NSObject
{
	NSNumber *field_id;
	NSNumber *type_id;
	NSNumber *unit_id;
	
	NSString *field_name;
	NSString *type_name;
	NSString *unit_abbreviation;
	NSString *unit_name;
}

/*Properties for setting/getting variables*/
@property (assign) NSNumber *field_id;
@property (assign) NSNumber *type_id;
@property (assign) NSNumber *unit_id;

@property (assign) NSString *field_name;
@property (assign) NSString *type_name;
@property (assign) NSString *unit_abbreviation;
@property (assign) NSString *unit_name;

@end

