package edu.uml.cs.isense.dfm;

// NOTE: if you change the type of any of the data fields below,
// be sure to update DataFieldManager's reOrderData function!
public class Fields {
	// Field data
	public String accel_x, accel_y, accel_z, accel_total;
	public String temperature_c, temperature_f, temperature_k;
	public long   timeMillis;
	public String lux;
	public String angle_deg, angle_rad;
	public double latitude, longitude;
	public String mag_x, mag_y, mag_z, mag_total;
	public String altitude;
	public String pressure;
	
	public Fields() {
	}
	
	// Constants
	public static final int TIME = 0;
	public static final int ACCEL_X = 1;
	public static final int ACCEL_Y = 2;
	public static final int ACCEL_Z = 3;
	public static final int ACCEL_TOTAL = 4;
	public static final int LATITUDE = 5;
	public static final int LONGITUDE = 6;
	public static final int MAG_X = 7;
	public static final int MAG_Y = 8;
	public static final int MAG_Z = 9;
	public static final int MAG_TOTAL = 10;
	public static final int HEADING_DEG = 11;
	public static final int HEADING_RAD = 12;
	public static final int TEMPERATURE_C = 13;
	public static final int PRESSURE = 14;
	public static final int ALTITUDE = 15;
	public static final int LIGHT = 16;
	public static final int TEMPERATURE_F = 17;
	public static final int TEMPERATURE_K = 18;
}
