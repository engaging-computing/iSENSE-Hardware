package edu.uml.cs.isense.collector;

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

	public SensorCompatibility() {

	}

	public boolean isCompatible(SensorTypes sensorType) {
		return compatible[(sensorType.getSensorType())];
	}

}
