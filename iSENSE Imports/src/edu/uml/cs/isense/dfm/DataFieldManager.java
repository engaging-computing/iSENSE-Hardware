package edu.uml.cs.isense.dfm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.objects.RProjectField;

public class DataFieldManager extends Application {

	int projID;
	API api;
	Context mContext;

	ArrayList<RProjectField> projFields;
	public LinkedList<String> order;
	Fields f;
	SensorCompatibility sc = new SensorCompatibility();
	public boolean[] enabledFields = { false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false };

	public DataFieldManager(int projID, API api, Context mContext, Fields f) {
		this.projID = projID;
		this.api = api;
		this.order = new LinkedList<String>();
		this.mContext = mContext;
		this.f = f;
	}

	// Static class function strictly for getting the field order of any
	// experiment
	public static LinkedList<String> getOrder(int projID, API api, Context c) {
		DataFieldManager d = new DataFieldManager(projID, api, c, null);
		d.getOrder();
		return d.order;
	}

	public void getOrder() {
		if (!order.isEmpty())
			return;

		if (projID == -1) {
			order.add(mContext.getString(R.string.time));
			order.add(mContext.getString(R.string.accel_x));
			order.add(mContext.getString(R.string.accel_y));
			order.add(mContext.getString(R.string.accel_z));
			order.add(mContext.getString(R.string.accel_total));
			order.add(mContext.getString(R.string.latitude));
			order.add(mContext.getString(R.string.longitude));
			order.add(mContext.getString(R.string.magnetic_x));
			order.add(mContext.getString(R.string.magnetic_y));
			order.add(mContext.getString(R.string.magnetic_z));
			order.add(mContext.getString(R.string.magnetic_total));
			order.add(mContext.getString(R.string.heading_deg));
			order.add(mContext.getString(R.string.heading_rad));
			order.add(mContext.getString(R.string.temperature_c));
			order.add(mContext.getString(R.string.pressure));
			order.add(mContext.getString(R.string.altitude));
			order.add(mContext.getString(R.string.luminous_flux));
			order.add(mContext.getString(R.string.temperature_f));
			order.add(mContext.getString(R.string.temperature_k));
		} else {

			projFields = api.getProjectFields(projID);

			for (RProjectField field : projFields) {
				switch (field.type) {

				// Number
				case RProjectField.TYPE_NUMBER:

					// Temperature
					if (field.name.toLowerCase(Locale.US).contains("temp")) {
						if (field.unit.toLowerCase(Locale.US).contains("c")) {
							order.add(mContext
									.getString(R.string.temperature_c));
						} else if (field.unit.toLowerCase(Locale.US).contains(
								"k")) {
							order.add(mContext
									.getString(R.string.temperature_k));
						} else {
							order.add(mContext
									.getString(R.string.temperature_f));
						}
						break;
					}

					// Potential Altitude
					else if (field.name.toLowerCase(Locale.US).contains(
							"altitude")) {
						order.add(mContext.getString(R.string.altitude));
						break;
					}

					// Light
					else if (field.name.toLowerCase(Locale.US)
							.contains("light")) {
						order.add(mContext.getString(R.string.luminous_flux));
						break;
					}

					// Heading
					else if (field.name.toLowerCase(Locale.US).contains(
							"heading")
							|| field.name.toLowerCase(Locale.US).contains(
									"angle")) {
						if (field.unit.toLowerCase(Locale.US).contains("rad")) {
							order.add(mContext.getString(R.string.heading_rad));
						} else {
							order.add(mContext.getString(R.string.heading_deg));
						}
						break;
					}

					// Numeric/Custom
					else if (field.name.toLowerCase(Locale.US).contains(
							"magnetic")) {
						if (field.name.toLowerCase(Locale.US).contains("x")) {
							order.add(mContext.getString(R.string.magnetic_x));
						} else if (field.name.toLowerCase(Locale.US).contains(
								"y")) {
							order.add(mContext.getString(R.string.magnetic_y));
						} else if (field.name.toLowerCase(Locale.US).contains(
								"z")) {
							order.add(mContext.getString(R.string.magnetic_z));
						} else {
							order.add(mContext
									.getString(R.string.magnetic_total));
						}
						break;
					}

					// Acceleration
					else if (field.name.toLowerCase(Locale.US)
							.contains("accel")) {
						if (field.name.toLowerCase(Locale.US).contains("x")) {
							order.add(mContext.getString(R.string.accel_x));
						} else if (field.name.toLowerCase(Locale.US).contains(
								"y")) {
							order.add(mContext.getString(R.string.accel_y));
						} else if (field.name.toLowerCase(Locale.US).contains(
								"z")) {
							order.add(mContext.getString(R.string.accel_z));
						} else {
							order.add(mContext.getString(R.string.accel_total));
						}
						break;
					}

					// Pressure
					else if (field.name.toLowerCase(Locale.US).contains(
							"pressure")) {
						order.add(mContext.getString(R.string.pressure));
						break;
					}

					else {
						order.add(mContext.getString(R.string.null_string));
						break;
					}

					// Time
				case RProjectField.TYPE_TIMESTAMP:
					order.add(mContext.getString(R.string.time));
					break;

				// Latitude
				case RProjectField.TYPE_LAT:
					order.add(mContext.getString(R.string.latitude));
					break;

				// Longitude
				case RProjectField.TYPE_LON:
					order.add(mContext.getString(R.string.longitude));
					break;

				// No match (Just about every other category)
				default:
					order.add(mContext.getString(R.string.null_string));
					break;

				}

			}
		}

	}

	public JSONObject putData() {

		JSONObject dataJSON = new JSONObject();

		for (int i = 0; i < order.size(); i++) {
			String s = order.get(i);

			try {
				if (s.equals(mContext.getString(R.string.accel_x))) {
					if (enabledFields[Fields.ACCEL_X])
						dataJSON.put("" + i, f.accel_x);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.accel_y))) {
					if (enabledFields[Fields.ACCEL_Y])
						dataJSON.put("" + i, f.accel_y);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.accel_z))) {
					if (enabledFields[Fields.ACCEL_Z])
						dataJSON.put("" + i, f.accel_z);
					continue;
				}
				if (s.equals(mContext.getString(R.string.accel_total))) {
					if (enabledFields[Fields.ACCEL_TOTAL])
						dataJSON.put("" + i, f.accel_total);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.temperature_c))) {
					if (enabledFields[Fields.TEMPERATURE_C])
						dataJSON.put("" + i, f.temperature_c);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.temperature_f))) {
					if (enabledFields[Fields.TEMPERATURE_F])
						dataJSON.put("" + i, f.temperature_f);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.temperature_k))) {
					if (enabledFields[Fields.TEMPERATURE_K])
						dataJSON.put("" + i, f.temperature_k);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.time))) {
					if (enabledFields[Fields.TIME])
						dataJSON.put("" + i, "u " + f.timeMillis);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.luminous_flux))) {
					if (enabledFields[Fields.LIGHT])
						dataJSON.put("" + i, f.lux);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.heading_deg))) {
					if (enabledFields[Fields.HEADING_DEG])
						dataJSON.put("" + i, f.angle_deg);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.heading_rad))) {
					if (enabledFields[Fields.HEADING_RAD])
						dataJSON.put("" + i, f.angle_rad);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.latitude))) {
					if (enabledFields[Fields.LATITUDE])
						dataJSON.put("" + i, f.latitude);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.longitude))) {
					if (enabledFields[Fields.LONGITUDE])
						dataJSON.put("" + i, f.longitude);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_x))) {
					if (enabledFields[Fields.MAG_X])
						dataJSON.put("" + i, f.mag_x);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_y))) {
					if (enabledFields[Fields.MAG_Y])
						dataJSON.put("" + i, f.mag_y);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_z))) {
					if (enabledFields[Fields.MAG_Z])
						dataJSON.put("" + i, f.mag_z);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_total))) {
					if (enabledFields[Fields.MAG_TOTAL])
						dataJSON.put("" + i, f.mag_total);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.altitude))) {
					if (enabledFields[Fields.ALTITUDE])

						dataJSON.put("" + i, f.altitude);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				if (s.equals(mContext.getString(R.string.pressure))) {
					if (enabledFields[Fields.PRESSURE])
						dataJSON.put("" + i, f.pressure);
					else
						dataJSON.put("" + i, "");
					continue;
				}
				dataJSON.put("" + i, "");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		Log.d("Jeremy", dataJSON.toString());

		return dataJSON;

	}

	public String writeSdCardLine() {

		StringBuilder b = new StringBuilder();
		boolean start = true;
		boolean firstLineWritten = false;

		for (String s : this.order) {

			if (s.equals(mContext.getString(R.string.accel_x))) {
				firstLineWritten = true;
				if (start)
					b.append(f.accel_x);
				else
					b.append(", ").append(f.accel_x);

			} else if (s.equals(mContext.getString(R.string.accel_y))) {
				firstLineWritten = true;
				if (start)
					b.append(f.accel_y);
				else
					b.append(", ").append(f.accel_y);

			} else if (s.equals(mContext.getString(R.string.accel_z))) {
				firstLineWritten = true;
				if (start)
					b.append(f.accel_z);
				else
					b.append(", ").append(f.accel_z);

			} else if (s.equals(mContext.getString(R.string.accel_total))) {
				firstLineWritten = true;
				if (start)
					b.append(f.accel_total);
				else
					b.append(", ").append(f.accel_total);

			} else if (s.equals(mContext.getString(R.string.temperature_c))) {
				firstLineWritten = true;
				if (start)
					b.append(f.temperature_c);
				else
					b.append(", ").append(f.temperature_c);

			} else if (s.equals(mContext.getString(R.string.temperature_f))) {
				firstLineWritten = true;
				if (start)
					b.append(f.temperature_f);
				else
					b.append(", ").append(f.temperature_f);

			} else if (s.equals(mContext.getString(R.string.temperature_k))) {
				firstLineWritten = true;
				if (start)
					b.append(f.temperature_k);
				else
					b.append(", ").append(f.temperature_k);

			} else if (s.equals(mContext.getString(R.string.time))) {
				firstLineWritten = true;
				if (start)
					b.append(f.timeMillis);
				else
					b.append(", ").append(f.timeMillis);

			} else if (s.equals(mContext.getString(R.string.luminous_flux))) {
				firstLineWritten = true;
				if (start)
					b.append(f.lux);
				else
					b.append(", ").append(f.lux);

			} else if (s.equals(mContext.getString(R.string.heading_deg))) {
				firstLineWritten = true;
				if (start)
					b.append(f.angle_deg);
				else
					b.append(", ").append(f.angle_deg);

			} else if (s.equals(mContext.getString(R.string.heading_rad))) {
				firstLineWritten = true;
				if (start)
					b.append(f.angle_rad);
				else
					b.append(", ").append(f.angle_rad);

			} else if (s.equals(mContext.getString(R.string.latitude))) {
				firstLineWritten = true;
				if (start)
					b.append(f.latitude);
				else
					b.append(", ").append(f.latitude);

			} else if (s.equals(mContext.getString(R.string.longitude))) {
				firstLineWritten = true;
				if (start)
					b.append(f.longitude);
				else
					b.append(", ").append(f.longitude);

			} else if (s.equals(mContext.getString(R.string.magnetic_x))) {
				firstLineWritten = true;
				if (start)
					b.append(f.mag_x);
				else
					b.append(", ").append(f.mag_x);

			} else if (s.equals(mContext.getString(R.string.magnetic_y))) {
				firstLineWritten = true;
				if (start)
					b.append(f.mag_y);
				else
					b.append(", ").append(f.mag_y);

			} else if (s.equals(mContext.getString(R.string.magnetic_z))) {
				firstLineWritten = true;
				if (start)
					b.append(f.mag_z);
				else
					b.append(", ").append(f.mag_z);

			} else if (s.equals(mContext.getString(R.string.magnetic_total))) {
				firstLineWritten = true;
				if (start)
					b.append(f.mag_total);
				else
					b.append(", ").append(f.mag_total);

			} else if (s.equals(mContext.getString(R.string.altitude))) {
				firstLineWritten = true;
				if (start)
					b.append(f.altitude);
				else
					b.append(", ").append(f.altitude);

			} else if (s.equals(mContext.getString(R.string.pressure))) {
				firstLineWritten = true;
				if (start)
					b.append(f.pressure);
				else
					b.append(", ").append(f.pressure);

			} else {
				firstLineWritten = true;
				if (start)
					b.append("");
				else
					b.append(", ").append("");
			}

			if (firstLineWritten)
				start = false;

		}

		b.append("\n");

		return b.toString();
	}

	public SensorCompatibility checkCompatibility() {

		int apiLevel = android.os.Build.VERSION.SDK_INT;
		int apiVal = 0;
		int[][] dispatch = sc.compatDispatch;

		if (apiLevel <= 8)
			apiVal = 0;
		if (apiLevel > 8 && apiLevel < 14)
			apiVal = 1;
		if (apiLevel > 14)
			apiVal = 2;

		for (int i = 0; i <= 5; i++) {
			int temp = dispatch[apiVal][i];
			if (temp == 1)
				sc.compatible[i] = true;
		}

		return sc;
	}

	public String writeHeaderLine() {
		StringBuilder b = new StringBuilder();
		boolean start = true;

		for (String unitName : this.order) {
			if (start)
				b.append(unitName);
			else
				b.append(", ").append(unitName);

			start = false;
		}

		b.append("\n");

		return b.toString();
	}

	// For use if a clump of data was recorded and needs to be cut down and
	// re-ordered
	public static String reOrderData(JSONArray data, String projID, API api,
			Context c) {
		JSONArray row, outData = new JSONArray();
		JSONObject outRow;
		int len = data.length();
		LinkedList<String> fieldOrder = getOrder(Integer.parseInt(projID), api,
				c);

		for (int i = 0; i < len; i++) {
			try {
				row = data.getJSONArray(i);
				outRow = new JSONObject();

				for (int j = 0; j < fieldOrder.size(); j++) {
					String s = fieldOrder.get(j);
					try {
						// Future TODO - I want to get the android
						// R.string.accel_x for e.g. here but I need a context,
						// so find a fix later
						if (s.equals("Accel-X")) {
							outRow.put(j + "", row.getString(Fields.ACCEL_X));
							continue;
						}
						if (s.equals("Accel-Y")) {
							outRow.put(j + "", row.getString(Fields.ACCEL_Y));
							continue;
						}
						if (s.equals("Accel-Z")) {
							outRow.put(j + "", row.getString(Fields.ACCEL_Z));
							continue;
						}
						if (s.equals("Accel-Total")) {
							outRow.put(j + "",
									row.getString(Fields.ACCEL_TOTAL));
							continue;
						}
						if (s.equals("Temperature-C")) {
							outRow.put(j + "",
									row.getString(Fields.TEMPERATURE_C));
							continue;
						}
						if (s.equals("Temperature-F")) {
							outRow.put(j + "",
									row.getString(Fields.TEMPERATURE_F));
							continue;
						}
						if (s.equals("Temperature-K")) {
							outRow.put(j + "",
									row.getString(Fields.TEMPERATURE_K));
							continue;
						}
						if (s.equals("Time")) {
							outRow.put(j + "", "u " + row.getLong(Fields.TIME));
							continue;
						}
						if (s.equals("Luminous Flux")) {
							outRow.put(j + "", row.getString(Fields.LIGHT));
							continue;
						}
						if (s.equals("Heading-Deg")) {
							outRow.put(j + "",
									row.getString(Fields.HEADING_DEG));
							continue;
						}
						if (s.equals("Heading-Rad")) {
							outRow.put(j + "",
									row.getString(Fields.HEADING_RAD));
							continue;
						}
						if (s.equals("Latitude")) {
							outRow.put(j + "", row.getDouble(Fields.LATITUDE));
							continue;
						}
						if (s.equals("Longitude")) {
							outRow.put(j + "", row.getDouble(Fields.LONGITUDE));
							continue;
						}
						if (s.equals("Magnetic-X")) {
							outRow.put(j + "", row.getString(Fields.MAG_X));
							continue;
						}
						if (s.equals("Magnetic-Y")) {
							outRow.put(j + "", row.getString(Fields.MAG_Y));
							continue;
						}
						if (s.equals("Magnetic-Z")) {
							outRow.put(j + "", row.getString(Fields.MAG_Z));
							continue;
						}
						if (s.equals("Magnetic-Total")) {
							outRow.put(j + "", row.getString(Fields.MAG_TOTAL));
							continue;
						}
						if (s.equals("Altitude")) {
							outRow.put(j + "", row.getString(Fields.ALTITUDE));
							continue;
						}
						if (s.equals("Pressure")) {
							outRow.put(j + "", row.getString(Fields.PRESSURE));
							continue;
						}
						outRow.put(j + "", null);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				outData.put(outRow);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// TODO: backup plan - if nothing was re-ordered, just hand back the
		// data as-is?

		return outData.toString();
	}

	public void setContext(Context c) {
		this.mContext = c;
	}

}
