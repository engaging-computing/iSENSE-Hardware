package edu.uml.cs.isense.objects;

public class ExperimentField {
	public int field_id;
	public String field_name = "";
	public int type_id;
	public int unit_id;
	public String type_name = "";
	public String unit_name = "";
	public String unit_abbreviation = "";
	
	// New API Fields
	public String experiment_id = "";
}

/**
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