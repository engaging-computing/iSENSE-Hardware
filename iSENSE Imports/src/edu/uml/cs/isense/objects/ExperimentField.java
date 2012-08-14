package edu.uml.cs.isense.objects;

/**
 * Class that includes information about a particular experiment field on iSENSE.
 * 
 * @author iSENSE Android Development Team
 */
public class ExperimentField {
	public int field_id;
	public String field_name = "";
	public int type_id;
	public int unit_id;
	public String type_name = "";
	public String unit_name = "";
	public String unit_abbreviation = "";
	public String experiment_id = "";
	
	// type_id as-is in the database
	public final int TEMPERATURE = 1;
	public final int LENGTH = 2;
	public final int DISTANCE = 3;
	public final int FORCE = 4;
	public final int VOLUME = 5;
	public final int MASS = 6;
	public final int TIME = 7;
	public final int LUMINOUS_FLUX = 8;
	public final int LUMINOUS_INTENSITY = 9;
	public final int ANGLE = 10;
	public final int ELECTRIC_POTENTIAL = 11;
	public final int ELECTRIC_CURRENT = 12;
	public final int POWER = 13;
	public final int ELECTRIC_CHARGE = 14;
	public final int SPEED = 15;
	public final int BOOLEAN = 16;
	public final int PERCENTAGE = 17;
	public final int ANALOG = 18;
	public final int GEOSPACIAL = 19;
	public final int RATE = 20;
	public final int NUMERIC = 21;
	public final int CUSTOM = 22;
	public final int SALINITY = 23;
	public final int PH_LEVEL = 24;
	public final int ACCELERATION = 25;
	public final int UV = 26;
	public final int PRESSURE = 27;
	public final int HUMIDITY = 28;
	public final int LIGHT = 29;
	public final int DISSOLVED_OXYGEN = 30;
	public final int ANEMOMETER = 31;
	public final int TURBIDITY = 32;
	public final int FLOW_RATE = 33;
	public final int MOTOR_MONITOR = 34;
	public final int CONDUCTIVITY = 35;
	public final int CONCENTRATION = 36;
	public final int TEXT = 37;
}

/*
type_id as-is in the database

+---------+--------------------+
| type_id | name               |
+---------+--------------------+
|       1 | Temperature        |
|       2 | Length             |
|       3 | Distance           |
|       4 | Force              |
|       5 | Volume             |
|       6 | Mass               |
|       7 | Time               |
|       8 | Luminous Flux      |
|       9 | Luminous Intensity |
|      10 | Angle              |
|      11 | Electric Potential |
|      12 | Electric Current   |
|      13 | Power              |
|      14 | Electric Charge    |
|      15 | Speed              |
|      16 | Boolean            |
|      17 | Percentage         |
|      18 | Analog             |
|      19 | Geospacial         |
|      20 | Rate               |
|      21 | Numeric            |
|      22 | Custom             |
|      23 | Salinity           |
|      24 | pH Level           |
|      25 | Acceleration       |
|      26 | UV                 |
|      27 | Pressure           |
|      28 | Humidity           |
|      29 | Light              |
|      30 | Dissolved Oxygen   |
|      31 | Anemometer         |
|      32 | Turbidity          |
|      33 | Flow Rate          |
|      34 | Motor Monitor      |
|      35 | Conductivity       |
|      36 | Concentration      |
|      37 | Text               |
+---------+--------------------+

*/