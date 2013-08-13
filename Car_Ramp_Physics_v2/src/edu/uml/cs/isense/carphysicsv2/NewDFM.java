package edu.uml.cs.isense.carphysicsv2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.objects.RProjectField;

public class NewDFM extends Application {

	int projID;
	API rapi;
	Context mContext;

	ArrayList<RProjectField> projFields;
	public LinkedList<String> order;
	JSONArray dataSet;
	Fields f;
	public boolean[] enabledFields = { false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false };

	public NewDFM(int projID, API rapi, Context mContext, Fields f) {
		this.projID = projID;
		this.rapi = rapi;
		this.order = new LinkedList<String>();
		this.mContext = mContext;
		this.dataSet = new JSONArray();
		this.f = f;
		this.projFields = new ArrayList<RProjectField>();
		if (rapi.hasConnectivity())
			new getFieldsTask().execute();
		else { // TODO - this else statement OK?
			projID = -1;
			getOrder();
		}
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

			if (projFields == null) {
				new RetrieveFieldsFromGetOrderTask().execute();
			}

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
					else if (field.name.toLowerCase(Locale.US)
							.contains("pressure")) {
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
	public JSONObject makeJSONObject() {

		JSONObject thisRow = new JSONObject();

		try {
			System.out.println("Fields:" + projFields);
			for (int i = 0; i < projFields.size(); i++) {
				RProjectField field = projFields.get(i);
				System.out.println("Type: " + field.type);
				switch (field.type) {

				// Time
				case 1:
					thisRow.put(Integer.toString(i), "u " + f.timeMillis);
					break;

				// Number
				case 2:

					// Acceleration
					if (field.name.toLowerCase(Locale.ENGLISH)
							.contains("accel")) {
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("x")) {
							thisRow.put(Integer.toString(i), f.accel_x);
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("y")) {
							thisRow.put(Integer.toString(i), f.accel_y);
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("z")) {
							thisRow.put(Integer.toString(i), f.accel_z);
						}
						if (field.name.toLowerCase(Locale.ENGLISH).contains(
								"total")
								|| field.name.toLowerCase(Locale.ENGLISH)
										.contains("mag")) {
							thisRow.put(Integer.toString(i), f.accel_z);
						}
					}
					// Magnetic
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"mag")) {
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("x")) {
							thisRow.put(Integer.toString(i), f.mag_x);
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("y")) {
							thisRow.put(Integer.toString(i), f.mag_y);
						}
						if (field.name.toLowerCase(Locale.ENGLISH)
								.contains("z")) {
							thisRow.put(Integer.toString(i), f.mag_z);
						}
						if (field.name.toLowerCase(Locale.ENGLISH).contains(
								"total")
								|| field.name.toLowerCase(Locale.ENGLISH)
										.contains("mag")) {
							thisRow.put(Integer.toString(i), f.mag_total);
						}
					}
					// Angle
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"angle")
							|| field.name.toLowerCase(Locale.ENGLISH).contains(
									"head")) {
						if (field.unit.toLowerCase(Locale.ENGLISH).contains(
								"deg")) {
							thisRow.put(Integer.toString(i), f.angle_deg);
						} else if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("rad")) {
							thisRow.put(Integer.toString(i), f.angle_rad);
						}
					}
					// Temperature
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"temp")) {
						if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("f")) {
							thisRow.put(Integer.toString(i), f.temperature_f);
						} else if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("c")) {
							thisRow.put(Integer.toString(i), f.temperature_c);
						} else if (field.unit.toLowerCase(Locale.ENGLISH)
								.contains("k")) {
							thisRow.put(Integer.toString(i), f.temperature_k);
						}
					}
					// Pressure
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"pressure")
							|| field.unit.toLowerCase(Locale.ENGLISH).contains(
									"ppi")
							|| field.unit.toLowerCase(Locale.ENGLISH).contains(
									"psi")) {
						thisRow.put(Integer.toString(i), f.pressure);
					}
					// Altitude
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"alt")) {
						thisRow.put(Integer.toString(i), f.altitude);
					}
					// Luminous Flux
					else if (field.name.toLowerCase(Locale.ENGLISH).contains(
							"flux")) {
						thisRow.put(Integer.toString(i), f.lux);
					}

					break;

				// Text
				case 3:
					Log.i("u is nub", "Nothing happenin' here!");
					break;

				// Latitude
				case 4:
					thisRow.put(Integer.toString(i), f.latitude);
					break;

				// Longitude
				case 5:
					thisRow.put(Integer.toString(i), f.longitude);
					break;

				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		System.out.println("Data:" + thisRow);
		return thisRow;
	}

	public JSONArray makeJSONArray() {

		JSONArray dataJSON = new JSONArray();
		
		System.out.println("Order:" + order.toString());

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
							outRow.put(j + "", row.getLong(Fields.TIME));
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

		return outData.toString();
	}

	public void setContext(Context c) {
		this.mContext = c;
	}

	public class getFieldsTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(Void... voids) {

			projFields = rapi.getProjectFields(projID);
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {
			getOrder();

		}

	}
	
	public class RetrieveFieldsFromGetOrderTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(Void... voids) {

			projFields = rapi.getProjectFields(projID);
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {

		}

	}

}
