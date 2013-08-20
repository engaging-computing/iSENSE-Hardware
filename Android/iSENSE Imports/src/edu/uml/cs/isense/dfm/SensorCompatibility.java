package edu.uml.cs.isense.dfm;

/**
 * This class is used to determine which sensors are typically/potentially
 * available on the user's device.
 * 
 * @author iSENSE Android Development Team
 *
 */
public class SensorCompatibility {

	/**
	 * Enumeration type that contains the 6 different register-able
	 * hardware sensor managers.
	 * 
	 */
	public enum SensorTypes {
		ACCELEROMETER(0), MAGNETIC_FIELD(1), AMBIENT_TEMPERATURE(2), PRESSURE(3), LIGHT(
				4), ORIENTATION(5);

		private int sensorType;

		private SensorTypes(int sensorType) {
			this.setSensorType(sensorType);
		}

		private int getSensorType() {
			return sensorType;
		}

		private void setSensorType(int sensorType) {
			this.sensorType = sensorType;
		}

	}

    // Row for each of the sensors
	protected boolean[] compatible = { false, false, false, false, false, false };

	protected int[][] compatDispatch = {
			// 0 = false, 1 = true
			{ 1, 1, 0, 0, 1, 1 }, // Row for api level 8
			{ 1, 1, 0, 1, 1, 1 }, // Row for api level 9 - 13
			{ 1, 1, 1, 1, 1, 1 }, // Row for api level 14 +
	
//	  Columns: 0 - Accelerometer 
//	           1 - Magnetic Field
//	           2 - Ambient Temperature 
//	           3 - Pressure
//	           4 - Light 
//	           5 - Orientation
//	 
			
	};

	/**
	 * Default constructor for the SensorCompatibility object.
	 */
	public SensorCompatibility() {
	}

	/**
	 * Determines if the passed in sensor type is compatible with the user's device.
	 * 
	 * @param sensorType
	 * 		- The type of hardware sensor you would like to check for compatibility.
	 * @return
	 * 		@true  If the sensor type is compatible with the user's device.
	 *      @false Otherwise
	 */
	public boolean isCompatible(SensorTypes sensorType) {
		return compatible[(sensorType.getSensorType())];
	}

}
