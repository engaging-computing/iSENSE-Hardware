package edu.uml.cs.isense.collector;

public class SensorCompatibility {

	public enum SensorTypes {
		ACCELEROMETER, MAGNETIC_FIELD, AMBIENT_TEMPERATURE,
	    PRESSURE, LIGHT, ORIENTATION
	}
	
	public boolean[] compatible = {
			false, false, false, false, false
	};
	
	public SensorTypes[] sensors = {
			SensorTypes.ACCELEROMETER, SensorTypes.MAGNETIC_FIELD,
			SensorTypes.AMBIENT_TEMPERATURE, SensorTypes.PRESSURE,
			SensorTypes.LIGHT, SensorTypes.ORIENTATION
	};
	
	public SensorCompatibility() {	
	}
	
}
