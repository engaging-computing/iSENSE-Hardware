package edu.uml.cs.isense.amusement.objects;

public class SensorCompatibility {

	public enum SensorTypes {
		ACCELEROMETER(0), MAGNETIC_FIELD(1), AMBIENT_TEMPERATURE(2), PRESSURE(3), LIGHT(
				4), ORIENTATION(5);

		private int sensorType;

		SensorTypes(int sensorType) {
			this.setSensorType(sensorType);
		}

		public int getSensorType() {
			return sensorType;
		}

		public void setSensorType(int sensorType) {
			this.sensorType = sensorType;
		}

	}

	public boolean[] compatible = { false, false, false, false, false, false };

	public int[][] compatDispatch = {
			// 0 = false, 1 = true
			{ 1, 1, 0, 0, 1, 1 }, // Row for api level 8
			{ 1, 1, 0, 1, 1, 1 }, // Row for api level 9 - 13
			{ 1, 1, 1, 1, 1, 1 }, // Row for api level 14 +
	/*
	 * Columns: 0 - Accelerometer, 1 - Magnetic Field, 2 - Ambient Temperature 3
	 * - Pressure, 4 - Light, 5 - Orientation
	 */
	};

	public SensorCompatibility() {

	}

	public boolean isCompatible(SensorTypes sensorType) {
		return compatible[(sensorType.getSensorType())];
	}

}
