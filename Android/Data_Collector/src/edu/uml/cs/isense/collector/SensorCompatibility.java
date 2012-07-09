package edu.uml.cs.isense.collector;

public class SensorCompatibility {

	public enum SensorTypes {
		ACCELEROMETER, MAGNETIC_FIELD, AMBIENT_TEMPERATURE,
	    PRESSURE, LIGHT, ORIENTATION
	}
	
	public boolean[] compatible = {
			false, false, false, false, false, false
	};
	
	public SensorTypes[] sensors = {
			SensorTypes.ACCELEROMETER, SensorTypes.MAGNETIC_FIELD,
			SensorTypes.AMBIENT_TEMPERATURE, SensorTypes.PRESSURE,
			SensorTypes.LIGHT, SensorTypes.ORIENTATION
	};
	
	public int[][] compatDispatch = {
			// 0 = false, 1 = true
			{1, 1, 0, 0, 1, 1},   // Row for api level 8
			{1, 1, 0, 1, 1, 1},   // Row for api level 9 - 13
			{1, 1, 1, 1, 1, 1},   // Row for api level 14 +
			/* Columns:
			 * 0 - Accelerometer, 1 - Magnetic Field, 2 - Ambient Temperature
			 * 3 - Pressure,      4 - Light,          5 - Orientation 
			 */
	};
	
	public SensorCompatibility() {	
	}
	
}
