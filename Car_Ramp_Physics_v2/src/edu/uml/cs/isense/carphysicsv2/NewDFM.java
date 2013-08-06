package edu.uml.cs.isense.carphysicsv2;

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
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.objects.RProjectField;

public class NewDFM extends Application {

	int eid;
	API rapi;
	Context mContext;

	ArrayList<RProjectField> expFields;
	public LinkedList<String> order;
	JSONArray dataSet;
	Fields f;
	public boolean[] enabledFields = { false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false };

	public NewDFM(int eid, API rapi, Context mContext, Fields f) {
		this.eid = eid;
		this.rapi = rapi;
		this.order = new LinkedList<String>();
		this.mContext = mContext;
		this.dataSet = new JSONArray();
		this.f = f;
		expFields = rapi.getProjectFields(eid);
	}

	// Static class function strictly for getting the field order of any
	// experiment
	public static LinkedList<String> getOrder(int eid, API rapi, Context c) {
		NewDFM d = new NewDFM(eid, rapi, c, null);
		d.getOrder();
		return d.order;
	}

	public void getOrder() {
		if (!order.isEmpty())
			return;

		if (eid == -1) {
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

			expFields = rapi.getProjectFields(eid);

			for (RProjectField field : expFields) {
				switch (field.type) {

				// Time
				case 1:
					order.add(mContext.getString(R.string.time));
					break;

				// Number
				case 2:

					// Acceleration
					if (field.name.toLowerCase(Locale.ENGLISH)
							.contains("accel")) {
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("x")) {
							order.add(mContext.getString(R.string.accel_x));
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("y")) {
							order.add(mContext.getString(R.string.accel_y));
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("z")) {
							order.add(mContext.getString(R.string.accel_z));
						}
						if (field.name.toLowerCase(Locale.ENGLISH).contains(
								"total")
								|| field.name.toLowerCase(Locale.ENGLISH)
										.contains("mag")) {
							order.add(mContext.getString(R.string.accel_total));
						}
					}
					// Magnetic
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"mag")) {
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("x")) {
							order.add(mContext.getString(R.string.magnetic_x));
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("y")) {
							order.add(mContext.getString(R.string.magnetic_y));
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("z")) {
							order.add(mContext.getString(R.string.magnetic_z));
						}
						if (field.name.toLowerCase(Locale.ENGLISH).contains(
								"total")
								|| field.name.toLowerCase(Locale.ENGLISH)
										.contains("mag")) {
							order.add(mContext
									.getString(R.string.magnetic_total));
						}
					}
					// Angle
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"angle")
							|| field.name.toLowerCase(Locale.ENGLISH).contains(
									"deg")) {
						if (field.unit.toLowerCase(Locale.ENGLISH).contains(
								"deg")) {
							order.add(mContext.getString(R.string.heading_deg));
						} else if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("rad")) {
							order.add(mContext.getString(R.string.heading_rad));
						}
					}
					// Temperature
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"temp")) {
						if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("f")) {
							order.add(mContext
									.getString(R.string.temperature_f));
						} else if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("c")) {
							order.add(mContext
									.getString(R.string.temperature_c));
						} else if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("k")) {
							order.add(mContext
									.getString(R.string.temperature_k));
						}
					}
					// Pressure
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"pressure")
							|| field.unit.toLowerCase(Locale.ENGLISH).contains(
									"ppi")
							|| field.unit.toLowerCase(Locale.ENGLISH).contains(
									"psi")) {
						order.add(mContext.getString(R.string.pressure));
					}
					// Altitude
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"alt")) {
						order.add(mContext.getString(R.string.altitude));
					}
					// Luminous Flux
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"flux")) {
						order.add(mContext.getString(R.string.luminous_flux));
					} else {
						order.add(mContext.getString(R.string.null_string));
					}

					break;

				// Text
				case 3:
					Log.i("u is nub", "Nothing happenin' here!");
					break;

				// Latitude
				case 4:
					order.add(mContext.getString(R.string.latitude));
					break;

				// Longitude
				case 5:
					order.add(mContext.getString(R.string.longitude));
					break;

				}

			}
		}

	}

	public JSONObject makeJSONObject() {

		JSONObject thisRow = new JSONObject();

		try {
			for (RProjectField field : expFields) {
				switch (field.type) {

				// Time
				case 1:
					thisRow.put("" + field.field_id, f.timeMillis);
					break;

				// Number
				case 2:

					// Acceleration
					if (field.name.toLowerCase(Locale.ENGLISH)
							.contains("accel")) {
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("x")) {
							thisRow.put("" + field.field_id, f.accel_x);
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("y")) {
							thisRow.put("" + field.field_id, f.accel_y);
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("z")) {
							thisRow.put("" + field.field_id, f.accel_z);
						}
						if (field.name.toLowerCase(Locale.ENGLISH).contains(
								"total")
								|| field.name.toLowerCase(Locale.ENGLISH)
										.contains("mag")) {
							thisRow.put("" + field.field_id, f.accel_z);
						}
					}
					// Magnetic
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"mag")) {
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("x")) {
							thisRow.put("" + field.field_id, f.mag_x);
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("y")) {
							thisRow.put("" + field.field_id, f.mag_y);
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("z")) {
							thisRow.put("" + field.field_id, f.mag_z);
						}
						if (field.name.toLowerCase(Locale.ENGLISH).contains(
								"total")
								|| field.name.toLowerCase(Locale.ENGLISH)
										.contains("mag")) {
							thisRow.put("" + field.field_id, f.mag_total);
						}
					}
					// Angle
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"angle")
							|| field.name.toLowerCase(Locale.ENGLISH).contains(
									"head")) {
						if (field.unit.toLowerCase(Locale.ENGLISH).contains(
								"deg")) {
							thisRow.put("" + field.field_id, f.angle_deg);
						} else if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("rad")) {
							thisRow.put("" + field.field_id, f.angle_rad);
						}
					}
					// Temperature
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"temp")) {
						if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("f")) {
							thisRow.put("" + field.field_id, f.temperature_f);
						} else if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("c")) {
							thisRow.put("" + field.field_id, f.temperature_c);
						} else if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("k")) {
							thisRow.put("" + field.field_id, f.temperature_k);
						}
					}
					// Pressure
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"pressure")
							|| field.unit.toLowerCase(Locale.ENGLISH).contains(
									"ppi")
							|| field.unit.toLowerCase(Locale.ENGLISH).contains(
									"psi")) {
						thisRow.put("" + field.field_id, f.pressure);
					}
					// Altitude
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"alt")) {
						thisRow.put("" + field.field_id, f.altitude);
					}
					// Luminous Flux
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"flux")) {
						thisRow.put("" + field.field_id, f.lux);
					}

					break;

				// Text
				case 3:
					Log.i("u is nub", "Nothing happenin' here!");
					break;

				// Latitude
				case 4:
					order.add(mContext.getString(R.string.latitude));
					break;

				// Longitude
				case 5:
					order.add(mContext.getString(R.string.longitude));
					break;

				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return thisRow;
	}

	public JSONArray putData() {

		JSONArray dataJSON = new JSONArray();

		for (String s : this.order) {
			try {
				if (s.equals(mContext.getString(R.string.accel_x))) {
					dataJSON.put(f.accel_x);
					continue;
				}
				if (s.equals(mContext.getString(R.string.accel_y))) {
					dataJSON.put(f.accel_y);
					continue;
				}
				if (s.equals(mContext.getString(R.string.accel_z))) {
					dataJSON.put(f.accel_z);
					continue;
				}
				if (s.equals(mContext.getString(R.string.accel_total))) {
					dataJSON.put(f.accel_total);
					continue;
				}
				if (s.equals(mContext.getString(R.string.temperature_c))) {
					dataJSON.put(f.temperature_c);
					continue;
				}
				if (s.equals(mContext.getString(R.string.temperature_f))) {
					dataJSON.put(f.temperature_f);
					continue;
				}
				if (s.equals(mContext.getString(R.string.temperature_k))) {
					dataJSON.put(f.temperature_k);
					continue;
				}
				if (s.equals(mContext.getString(R.string.time))) {
					dataJSON.put(f.timeMillis);
					continue;
				}
				if (s.equals(mContext.getString(R.string.luminous_flux))) {
					dataJSON.put(f.lux);
					continue;
				}
				if (s.equals(mContext.getString(R.string.heading_deg))) {
					dataJSON.put(f.angle_deg);
					continue;
				}
				if (s.equals(mContext.getString(R.string.heading_rad))) {
					dataJSON.put(f.angle_rad);
					continue;
				}
				if (s.equals(mContext.getString(R.string.latitude))) {
					dataJSON.put(f.latitude);
					continue;
				}
				if (s.equals(mContext.getString(R.string.longitude))) {
					dataJSON.put(f.longitude);
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_x))) {
					dataJSON.put(f.mag_x);
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_y))) {
					dataJSON.put(f.mag_y);
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_z))) {
					dataJSON.put(f.mag_z);
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_total))) {
					dataJSON.put(f.mag_total);
					continue;
				}
				if (s.equals(mContext.getString(R.string.altitude))) {
					dataJSON.put(f.altitude);
					continue;
				}
				if (s.equals(mContext.getString(R.string.pressure))) {
					dataJSON.put(f.pressure);
					continue;
				}
				dataJSON.put(null);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

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
	public static String reOrderData(JSONArray data, String eid, API rapi,
			Context c) {
		JSONArray row, outData = new JSONArray(), outRow;
		int len = data.length();
		LinkedList<String> fieldOrder = getOrder(Integer.parseInt(eid), rapi, c);

		for (int i = 0; i < len; i++) {
			try {
				row = data.getJSONArray(i);
				outRow = new JSONArray();

				for (String s : fieldOrder) {
					try {
						// Future TODO - I want to get the android
						// R.string.accel_x for e.g. here but I need a context,
						// so find a fix later
						if (s.equals("Accel-X")) {
							outRow.put(row.getString(Fields.ACCEL_X));
							continue;
						}
						if (s.equals("Accel-Y")) {
							outRow.put(row.getString(Fields.ACCEL_Y));
							continue;
						}
						if (s.equals("Accel-Z")) {
							outRow.put(row.getString(Fields.ACCEL_Z));
							continue;
						}
						if (s.equals("Accel-Total")) {
							outRow.put(row.getString(Fields.ACCEL_TOTAL));
							continue;
						}
						if (s.equals("Temperature-C")) {
							outRow.put(row.getString(Fields.TEMPERATURE_C));
							continue;
						}
						if (s.equals("Temperature-F")) {
							outRow.put(row.getString(Fields.TEMPERATURE_F));
							continue;
						}
						if (s.equals("Temperature-K")) {
							outRow.put(row.getString(Fields.TEMPERATURE_K));
							continue;
						}
						if (s.equals("Time")) {
							outRow.put(row.getLong(Fields.TIME));
							continue;
						}
						if (s.equals("Luminous Flux")) {
							outRow.put(row.getString(Fields.LIGHT));
							continue;
						}
						if (s.equals("Heading-Deg")) {
							outRow.put(row.getString(Fields.HEADING_DEG));
							continue;
						}
						if (s.equals("Heading-Rad")) {
							outRow.put(row.getString(Fields.HEADING_RAD));
							continue;
						}
						if (s.equals("Latitude")) {
							outRow.put(row.getDouble(Fields.LATITUDE));
							continue;
						}
						if (s.equals("Longitude")) {
							outRow.put(row.getDouble(Fields.LONGITUDE));
							continue;
						}
						if (s.equals("Magnetic-X")) {
							outRow.put(row.getString(Fields.MAG_X));
							continue;
						}
						if (s.equals("Magnetic-Y")) {
							outRow.put(row.getString(Fields.MAG_Y));
							continue;
						}
						if (s.equals("Magnetic-Z")) {
							outRow.put(row.getString(Fields.MAG_Z));
							continue;
						}
						if (s.equals("Magnetic-Total")) {
							outRow.put(row.getString(Fields.MAG_TOTAL));
							continue;
						}
						if (s.equals("Altitude")) {
							outRow.put(row.getString(Fields.ALTITUDE));
							continue;
						}
						if (s.equals("Pressure")) {
							outRow.put(row.getString(Fields.PRESSURE));
							continue;
						}
						outRow.put(null);
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
