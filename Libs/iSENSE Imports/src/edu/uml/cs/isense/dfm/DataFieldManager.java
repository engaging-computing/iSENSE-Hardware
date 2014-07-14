package edu.uml.cs.isense.dfm;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.objects.RProjectField;

/**
 * The DataFieldManager class is designed to, as its name implies, manage how
 * data is associated with project fields on the iSENSE website. It provides
 * field matching, organizing and formatting data sets, applying sensor
 * compatibility checks, and writing data sets to a .csv file.
 * 
 * @author iSENSE Android Development Team
 */
public class DataFieldManager extends Application {

	private int projID;
	private API api;
	private Context mContext;

	private ArrayList<RProjectField> projFields;
	private LinkedList<String> order;
	private LinkedList<String> realOrder; // the actual fields in the project,
											// used for .csv file header writing
	private LinkedList<Long> fieldIDs; // IDs for the fields in order, in order
	private Fields f;

	private String CSV_DELIMITER = "-:;_--:-;-;_::-;";

	/**
	 * Boolean array of size 19 containing a list of fields enabled for
	 * recording data. See the {@link edu.uml.cs.isense.dfm.Fields Fields} class
	 * for a list of the constants associated with this boolean array's
	 * respective indices. By default, each field is disabled.
	 * 
	 * To enable a particular field for recording from your class, perform an
	 * operation such as:
	 * 
	 * <pre>
	 * {@code
	 *  myDFMInstance.enabledFields[Fields.ACCEL_X] = true;
	 * }
	 * </pre>
	 */
	public boolean[] enabledFields = { false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false };

	/**
	 * Constructor for the DataFieldManager class.
	 * 
	 * @param projID
	 *            - The ID of the project to be associated with this
	 *            DataFieldManager, or -1 for no associated project.
	 * @param api
	 *            - An instance of the {@link edu.uml.cs.isense.comm.API} class.
	 * @param mContext
	 *            - The context of the class containing the DataFieldManager
	 *            object instance.
	 * @param f
	 *            - An instance of the {@link edu.uml.cs.isense.dfm.Fields}
	 *            class.
	 * @return An instance of DataFieldManager.
	 */
	public DataFieldManager(int projID, API api, Context mContext, Fields f) {
		this.projID = projID;
		this.api = api;
		this.order = new LinkedList<String>();
		this.realOrder = new LinkedList<String>();
		this.fieldIDs = new LinkedList<Long>();
		this.mContext = mContext;
		this.f = f;
	}

	/**
	 * Creates a list, stored in this DataFieldManager instance's "order"
	 * object, of matched fields from the iSENSE project with the instance's
	 * "projID".
	 * 
	 * If no associated project is passed in (-1), the order array contains all
	 * possible fields to be recorded. Otherwise, the order array contains all
	 * fields that could be matched string-wise with the associated project's
	 * fields.
	 * 
	 * NOTE: Ensure you call this method before recording data, or otherwise you
	 * will be given blank data back from the
	 * {@link edu.uml.cs.isense.dfm.DataFieldManager#putData() putData()}
	 * method. Error checking may be added such as:
	 * 
	 * <pre>
	 * {@code
	 *  if (myDFMInstance.getOrderList().size() == 0) 
	 *     myDFMInstance.getOrder();
	 * }
	 * </pre>
	 * 
	 * to prevent such a bug from occurring.
	 * 
	 * Additionally, if you intend on calling this function from within an
	 * AsyncTask, call
	 * {@link edu.uml.cs.isense.dfm.DataFieldManager#getOrderWithExternalAsyncTask()
	 * getOrderWithExternalAsyncTask()} instead.
	 */
	public void getOrder() {
		if (!order.isEmpty())
			return;

		if (projID == -1) {
			order.clear();
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
			// Execute a new task
			new GetOrderTask().execute();
		}
	}

	/**
	 * Use this function instead of getOrder() if and only if you are calling
	 * this function in an AsyncTask.
	 * 
	 * See the {@link edu.uml.cs.isense.dfm.DataFieldManager#getOrder()
	 * getOrder()} function for more details.
	 */
	public void getOrderWithExternalAsyncTask() {
		if (!order.isEmpty())
			return;

		if (projID == -1) {
			order.clear();
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
			// Function is being called within an AsyncTask already, so
			// no need to create a new task for the API call
			projFields = api.getProjectFields(projID);
			getProjectFieldOrder();

		}
	}

	/**
	 * Sets the DataFieldManager's order array based not on project field
	 * matching but rather the field's returned from from user field matching.
	 * 
	 * @param input
	 *            A field list built from the FieldMatching dialog.
	 */
	public void setOrder(String input) {
		this.order = new LinkedList<String>();

		String[] fields = input.split(",");

		for (String s : fields) {
			order.add(s);
		}
	}

	/**
	 * Creates a row of data from the Fields object this class instance
	 * contains. This function performs no field matching and assumes you are
	 * only calling it with the intention of saving it in the data saver.
	 * 
	 * @return The row of data in the form of a JSONArray that is to be
	 *         re-organized at upload time.
	 */
	public JSONArray putData() {

		JSONArray dataJSON = new JSONArray();

		try {
			if (enabledFields[Fields.TIME])
				dataJSON.put("u " + f.timeMillis);
			else
				dataJSON.put("");

			if (enabledFields[Fields.ACCEL_X] && f.accel_x != null) 
				dataJSON.put(f.accel_x);
				else
				dataJSON.put("");

			if (enabledFields[Fields.ACCEL_Y] && f.accel_y != null)
				dataJSON.put(f.accel_y);
			else
				dataJSON.put("");

			if (enabledFields[Fields.ACCEL_Z] && f.accel_z != null)
				dataJSON.put(f.accel_z);
			else
				dataJSON.put("");

			if (enabledFields[Fields.ACCEL_TOTAL] && f.accel_total != null)
				dataJSON.put(f.accel_total);
			else
				dataJSON.put("");

			if (enabledFields[Fields.LATITUDE])
				dataJSON.put(f.latitude);
			else
				dataJSON.put("");

			if (enabledFields[Fields.LONGITUDE])
				dataJSON.put(f.longitude);
			else
				dataJSON.put("");

			if (enabledFields[Fields.MAG_X] && f.mag_x != null)
				dataJSON.put(f.mag_x);
			else
				dataJSON.put("");

			if (enabledFields[Fields.MAG_Y] && f.mag_y != null)
				dataJSON.put(f.mag_y);
			else
				dataJSON.put("");

			if (enabledFields[Fields.MAG_Z] && f.mag_z != null)
				dataJSON.put(f.mag_z);
			else
				dataJSON.put("");

			if (enabledFields[Fields.MAG_TOTAL] && f.mag_total != null)
				dataJSON.put(f.mag_total);
			else
				dataJSON.put("");

			if (enabledFields[Fields.HEADING_DEG] && f.angle_deg != null)
				dataJSON.put(f.angle_deg);
			else
				dataJSON.put("");

			if (enabledFields[Fields.HEADING_RAD] && f.angle_rad != null)
				dataJSON.put(f.angle_rad);
			else
				dataJSON.put("");

			if (enabledFields[Fields.TEMPERATURE_C] && f.temperature_c != null)
				dataJSON.put(f.temperature_c);
			else
				dataJSON.put("");

			if (enabledFields[Fields.PRESSURE] && f.pressure != null)
				dataJSON.put(f.pressure);
			else
				dataJSON.put("");

			if (enabledFields[Fields.ALTITUDE] && f.altitude != null)

				dataJSON.put(f.altitude);
			else
				dataJSON.put("");

			if (enabledFields[Fields.LIGHT] && f.lux != null)
				dataJSON.put(f.lux);
			else
				dataJSON.put("");

			if (enabledFields[Fields.TEMPERATURE_F] && f.temperature_f != null)
				dataJSON.put(f.temperature_f);
			else
				dataJSON.put("");

			if (enabledFields[Fields.TEMPERATURE_K] && f.temperature_k != null)
				dataJSON.put(f.temperature_k);
			else
				dataJSON.put("");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		System.out.println("Data line: " + dataJSON.toString());

		return dataJSON;

	}

	/**
	 * Writes a single line of data in .csv format. Data is pulled from this
	 * class instance's Fields object.
	 * 
	 * NOTE: Only call this method if you are recording with an associated
	 * project. You will be returned a blank string otherwise.
	 * 
	 * Also ensure that your .csv file begins with the String returned by
	 * {@link edu.uml.cs.isense.dfm.DataFieldManager#writeHeaderLine()
	 * writeHeaderLine()}.
	 * 
	 * @return A single line of data in .csv format in the form of a String, or
	 *         a blank string if there is no associated project.
	 */
	public String writeSdCardLine() {

		StringBuilder b = new StringBuilder();
		boolean start = true;
		boolean firstLineWritten = false;

		if (projID == -1)
			return "";

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
					b.append("u " + f.timeMillis);
				else
					b.append(", ").append("u " + f.timeMillis);

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

	/**
	 * Writes the first line in a .csv file for the project you are recording
	 * data for. Data can then by appended to this by calling
	 * {@link edu.uml.cs.isense.dfm.DataFieldManager#writeSdCardLine()
	 * writeSdCardLine()}.
	 * 
	 * NOTE: Only call this method if you are recording with an associated
	 * project. You will be returned a blank string otherwise.
	 * 
	 * @return A single header line in .csv format in the form of a String, or a
	 *         blank string if there is no associated project.
	 */
	public String writeHeaderLine() {
		StringBuilder b = new StringBuilder();
		boolean start = true;

		if (projID == -1)
			return "";

		for (String unitName : this.realOrder) {

			if (start)
				b.append(unitName);
			else
				b.append(", ").append(unitName);

			start = false;
		}

		b.append("\n");

		return b.toString();
	}

	/**
	 * Use this method only if data was recorded with no associated project AND
	 * you intend to create a {@link edu.uml.cs.isense.queue.QDataSet QDataSet}
	 * object with an associated project before adding the QDataSet to an
	 * {@link edu.uml.cs.isense.queue.UploadQueue UploadQueue} object.
	 * 
	 * This method will be called internally if you pass -1 as your project ID
	 * to the QDataSet object, and thus you do not need to call it.
	 * 
	 * 
	 * @param data
	 *            - A JSONArray of JSONArray objects returned from the
	 *            {@link edu.uml.cs.isense.dfm.DataFieldManager#putData()
	 *            putData()} method.
	 * @param projID
	 *            - The project ID which the data will be re-ordered to match.
	 * @param c
	 *            - The context of the Activity calling this function
	 * @param fieldOrder
	 *            - The list of fields matched using the FieldMatching class, or
	 *            null if FieldMatching wasn't used.
	 * @param fieldIDs
	 *            - The list of field IDs, in order, of the project to reorder
	 *            the data for (or null if you do not have them).
	 * @return A JSONObject.toString() formatted properly for upload to iSENSE.
	 * 
	 */
	public static String reOrderData(JSONArray data, String projID, Context c,
			LinkedList<String> fieldOrder, LinkedList<Long> fieldIDs) {
		API api = API.getInstance();

		JSONArray row, outData = new JSONArray();
		JSONObject outRow;
		int len = data.length();

		// if the field order is null, set up the fieldOrder/fieldIDs.
		// otherwise, just get fieldIDs
		if (fieldOrder == null || fieldOrder.size() == 0) {
			DataFieldManager d = new DataFieldManager(Integer.parseInt(projID),
					api, c, null);
			d.getOrderWithExternalAsyncTask();
			fieldOrder = d.getOrderList();
			fieldIDs = d.getFieldIDs();
		} else if (fieldIDs == null || fieldIDs.size() == 0) {
			DataFieldManager d = new DataFieldManager(Integer.parseInt(projID),
					api, c, null);
			d.getOrderWithExternalAsyncTask();
			fieldIDs = d.getFieldIDs();
		}

		Activity a = (Activity) c;

		for (int i = 0; i < len; i++) {
			try {
				row = data.getJSONArray(i);
				outRow = new JSONObject();

				for (int j = 0; j < fieldOrder.size(); j++) {
					String s = fieldOrder.get(j);
					Long id = fieldIDs.get(j);

					try {
						// Compare against hard-coded strings
						if (s.equals(a.getResources().getString(
								R.string.accel_x))) {
							outRow.put(id + "", row.getString(Fields.ACCEL_X));
							Log.e("DFM",s + " " + a.getResources().getString(
									R.string.accel_x)); 
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.accel_y))) {
							outRow.put(id + "", row.getString(Fields.ACCEL_Y));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.accel_z))) {
							outRow.put(id + "", row.getString(Fields.ACCEL_Z));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.accel_total))) {
							outRow.put(id + "",
									row.getString(Fields.ACCEL_TOTAL));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.temperature_c))) {
							outRow.put(id + "",
									row.getString(Fields.TEMPERATURE_C));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.temperature_f))) {
							outRow.put(id + "",
									row.getString(Fields.TEMPERATURE_F));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.temperature_k))) {
							outRow.put(id + "",
									row.getString(Fields.TEMPERATURE_K));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.time))) {
							outRow.put(id + "", row.getString(Fields.TIME));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.luminous_flux))) {
							outRow.put(id + "", row.getString(Fields.LIGHT));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.heading_deg))) {
							outRow.put(id + "",
									row.getString(Fields.HEADING_DEG));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.heading_rad))) {
							outRow.put(id + "",
									row.getString(Fields.HEADING_RAD));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.latitude))) {
							outRow.put(id + "", row.getDouble(Fields.LATITUDE));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.longitude))) {
							outRow.put(id + "", row.getDouble(Fields.LONGITUDE));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.magnetic_x))) {
							outRow.put(id + "", row.getString(Fields.MAG_X));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.magnetic_y))) {
							outRow.put(id + "", row.getString(Fields.MAG_Y));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.magnetic_z))) {
							outRow.put(id + "", row.getString(Fields.MAG_Z));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.magnetic_total))) {
							outRow.put(id + "", row.getString(Fields.MAG_TOTAL));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.altitude))) {
							outRow.put(id + "", row.getString(Fields.ALTITUDE));
							continue;
						} else if (s.equals(a.getResources().getString(
								R.string.pressure))) {
							outRow.put(id + "", row.getString(Fields.PRESSURE));
							continue;
						} else {
							Log.e("DFM", s + " " + a.getResources().getString(
									R.string.time));
							outRow.put(id + "", "");
						}
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

	/**
	 * Set the context for this instance of DataFieldManager.
	 * 
	 * @param c
	 *            - The new context of this DataFieldManager instance.
	 */
	public void setContext(Context c) {
		this.mContext = c;
	}

	// Task for checking sensor availability along with enabling/disabling
	private class GetOrderTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			projFields = api.getProjectFields(projID);
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			getProjectFieldOrder();
		}
	}

	/**
	 * Writes the fields for the project out to SharedPreferences, designed to
	 * aid the writing of .csv files using
	 * {@link edu.uml.cs.isense.dfm.DataFieldManager#getProjectFieldsAndSetCSVOrder()
	 * getProjectFieldsAndSetCSVOrder} when prepared to write a .csv file.
	 * 
	 */
	public void writeProjectFields() {

		SharedPreferences mPrefs = this.mContext.getSharedPreferences(
				"CSV_ORDER", 0);
		SharedPreferences.Editor mEdit = mPrefs.edit();

		StringBuilder sb = new StringBuilder();
		boolean start = true;

		for (String s : this.realOrder) {
			if (start)
				sb.append(s);
			else
				sb.append(CSV_DELIMITER).append(s);

			start = false;
		}

		String out = sb.toString();
		mEdit.putString("csv_order", out).commit();

	}

	/**
	 * Retrieve the fields written to SharedPreferences from
	 * {@link edu.uml.cs.isense.dfm.DataFieldManager#writeProjectFields()
	 * writeProjectFields()}, primarily designed for writing the header line in
	 * a .csv file.
	 * 
	 */
	public void getProjectFieldsAndSetCSVOrder() {

		SharedPreferences mPrefs = this.mContext.getSharedPreferences(
				"CSV_ORDER", 0);

		String in = mPrefs.getString("csv_order", "");
		if (in.equals(""))
			return;

		String[] parts = in.split(CSV_DELIMITER);
		this.realOrder.clear();

		for (String s : parts) {
			this.realOrder.add(s);
		}

	}

	private void getProjectFieldOrder() {
		this.order = new LinkedList<String>();
		this.realOrder = new LinkedList<String>();
		this.fieldIDs = new LinkedList<Long>();

		for (RProjectField field : projFields) {

			realOrder.add(field.name);
			fieldIDs.add(field.field_id);

			switch (field.type) {

			// Number
			case RProjectField.TYPE_NUMBER:

				// Temperature
				if (field.name.toLowerCase(Locale.US).contains("temp")) {
					if (field.unit.toLowerCase(Locale.US).contains("c")) {
						order.add(mContext.getString(R.string.temperature_c));
					} else if (field.unit.toLowerCase(Locale.US).contains("k")) {
						order.add(mContext.getString(R.string.temperature_k));
					} else {
						order.add(mContext.getString(R.string.temperature_f));
					}
					break;
				}

				// Potential Altitude
				else if (field.name.toLowerCase(Locale.US).contains("altitude")) {
					order.add(mContext.getString(R.string.altitude));
					break;
				}

				// Light
				else if (field.name.toLowerCase(Locale.US).contains("light")) {
					order.add(mContext.getString(R.string.luminous_flux));
					break;
				}

				// Heading
				else if (field.name.toLowerCase(Locale.US).contains("heading")
						|| field.name.toLowerCase(Locale.US).contains("angle")) {
					if (field.unit.toLowerCase(Locale.US).contains("rad")) {
						order.add(mContext.getString(R.string.heading_rad));
					} else {
						order.add(mContext.getString(R.string.heading_deg));
					}
					break;
				}

				// Numeric/Custom
				else if (field.name.toLowerCase(Locale.US).contains("magnetic")) {
					if (field.name.toLowerCase(Locale.US).contains("x")) {
						order.add(mContext.getString(R.string.magnetic_x));
					} else if (field.name.toLowerCase(Locale.US).contains("y")) {
						order.add(mContext.getString(R.string.magnetic_y));
					} else if (field.name.toLowerCase(Locale.US).contains("z")) {
						order.add(mContext.getString(R.string.magnetic_z));
					} else {
						order.add(mContext.getString(R.string.magnetic_total));
					}
					break;
				}

				// Acceleration
				else if (field.name
						.toLowerCase(Locale.US)
						.matches(
								"(^(((x|y|z){1}[^x|y|z]*acc[^x|y|z]*)|[^x|y|z]*(accel))$)"
										+ "|(^((acc[^x|y|z]*(x|y|z){1})|(accel[^x|y|z]*))$)")) {
					if (field.name.toLowerCase(Locale.US).contains("x")) {
						order.add(mContext.getString(R.string.accel_x));
					} else if (field.name.toLowerCase(Locale.US).contains("y")) {
						order.add(mContext.getString(R.string.accel_y));
					} else if (field.name.toLowerCase(Locale.US).contains("z")) {
						order.add(mContext.getString(R.string.accel_z));
					} else {
						order.add(mContext.getString(R.string.accel_total));
					}
					break;
				}

				// Pressure
				else if (field.name.toLowerCase(Locale.US).contains("pressure")) {
					order.add(mContext.getString(R.string.pressure));
					break;
				}

				else {
					order.add(mContext.getString(R.string.null_string)
							+ field.name);
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
				order.add(mContext.getString(R.string.null_string) + field.name);
				break;

			}

		}

	}

	/**
	 * Getter for the project ID this DataFieldManager instance operates on.
	 * 
	 * @return The current project ID.
	 */
	public int getProjID() {
		return this.projID;
	}

	/**
	 * Setter for the project ID this DataFieldManager instance operates on.
	 * 
	 * NOTE: If you call this function, be sure you call
	 * {@link edu.uml.cs.isense.dfm.DataFieldManager#getOrder()} to update which
	 * fields this DataFieldManager instance records for.
	 * 
	 * @param projectID
	 *            - The new project ID for DataFieldManager to operate with.
	 */
	public void setProjID(int projectID) {
		this.projID = projectID;
	}

	/**
	 * Getter for the project fields associated with the project ID passed in to
	 * this instance of DataFieldManager.
	 * 
	 * @return An ArrayList of {@link edu.uml.cs.isense.objects.RProjectField
	 *         RProjectField} containing the fields from the associated iSENSE
	 *         project.
	 */
	public ArrayList<RProjectField> getProjectFields() {
		return this.projFields;
	}

	/**
	 * Getter for the list of matched fields that this instance of
	 * DataFieldManager will record data for.
	 * 
	 * @return The list of matched project fields from the associated project ID
	 */
	public LinkedList<String> getOrderList() {
		return this.order;
	}

	/**
	 * Getter for the list of actual project fields.
	 * 
	 * @return The list of project fields from the associated project ID
	 */
	public LinkedList<String> getRealOrderList() {
		return this.realOrder;
	}

	/**
	 * Converts order into a String[]
	 * 
	 * @return order in the form of a String[]
	 */
	public String[] convertLinkedListToStringArray(LinkedList<String> ll) {

		String[] sa = new String[ll.size()];
		int i = 0;

		for (String s : ll)
			sa[i++] = s;

		return sa;
	}

	/**
	 * Converts a String[] back into a LinkedList of Strings
	 * 
	 * @param sa
	 *            - the String[] to convert
	 * @return sa in the form of a LinkedList of Strings
	 */
	public static LinkedList<String> convertStringArrayToLinkedList(String[] sa) {

		LinkedList<String> lls = new LinkedList<String>();

		for (String s : sa)
			lls.add(s);

		return lls;
	}

	/**
	 * Getter for the Fields object associated with this instance of
	 * DataFieldManager
	 * 
	 * @return The Fields object associated with this instance of
	 *         DataFieldManager
	 */
	public Fields getFields() {
		return this.f;
	}

	/**
	 * Setter for the Fields object associated with this instance of
	 * DataFieldManager
	 * 
	 * @param fields
	 *            - The Fields object associated with this instance of
	 *            DataFieldManager
	 */
	public void setFields(Fields fields) {
		this.f = fields;
	}

	/**
	 * Getter for the list of field IDs in the order list.
	 * 
	 * @return The IDs for the fields, in order, of the project associated with
	 *         this DataFieldManager instance.
	 */
	public LinkedList<Long> getFieldIDs() {
		return this.fieldIDs;
	}

	private ArrayList<Integer> getFieldTypes() {

		ArrayList<Integer> fieldTypes = new ArrayList<Integer>();

		for (RProjectField field : this.projFields) {
			fieldTypes.add(field.type);
		}

		return fieldTypes;

	}

	/*
	 * Checks if project contains a timestamp
	 */

	public boolean projectContainsTimeStamp() {
		ArrayList<Integer> fields = this.getFieldTypes();

		for (Integer i : fields) {
			if (i.intValue() == RProjectField.TYPE_TIMESTAMP) {
				return true;
			}
		}

		return false;
	}

	/*
	 * Checks if project contains a location (latitude and longitude)
	 */

	public boolean projectContainsLocation() {
		ArrayList<Integer> fields = this.getFieldTypes();

		for (Integer i : fields) {
			if (i.intValue() == RProjectField.TYPE_LAT
					|| i.intValue() == RProjectField.TYPE_LON) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Enables all fields for recording data
	 */
	public void enableAllFields() {
		enabledFields[Fields.TIME] = true;
		enabledFields[Fields.ACCEL_X] = true;
		enabledFields[Fields.ACCEL_Y] = true;
		enabledFields[Fields.ACCEL_Z] = true;
		enabledFields[Fields.ACCEL_TOTAL] = true;
		enabledFields[Fields.LATITUDE] = true;
		enabledFields[Fields.LONGITUDE] = true;
		enabledFields[Fields.MAG_X] = true;
		enabledFields[Fields.MAG_Y] = true;
		enabledFields[Fields.MAG_Z] = true;
		enabledFields[Fields.MAG_TOTAL] = true;
		enabledFields[Fields.HEADING_DEG] = true;
		enabledFields[Fields.HEADING_RAD] = true;
		enabledFields[Fields.TEMPERATURE_C] = true;
		enabledFields[Fields.TEMPERATURE_F] = true;
		enabledFields[Fields.TEMPERATURE_K] = true;
		enabledFields[Fields.PRESSURE] = true;
		enabledFields[Fields.ALTITUDE] = true;
		enabledFields[Fields.LIGHT] = true;
	}

	/**
	 * Set the enabled fields from the acceptedFields parameter.
	 * 
	 * @param acceptedFields
	 *            LinkedList of field strings
	 */
	public void setEnabledFields(LinkedList<String> acceptedFields) {

		for (String s : acceptedFields) {
			if (s.equals(getString(R.string.time)))
				enabledFields[Fields.TIME] = true;
			if (s.equals(getString(R.string.accel_x)))
				enabledFields[Fields.ACCEL_X] = true;
			if (s.equals(getString(R.string.accel_y)))
				enabledFields[Fields.ACCEL_Y] = true;
			if (s.equals(getString(R.string.accel_z)))
				enabledFields[Fields.ACCEL_Z] = true;
			if (s.equals(getString(R.string.accel_total)))
				enabledFields[Fields.ACCEL_TOTAL] = true;
			if (s.equals(getString(R.string.latitude)))
				enabledFields[Fields.LATITUDE] = true;
			if (s.equals(getString(R.string.longitude)))
				enabledFields[Fields.LONGITUDE] = true;
			if (s.equals(getString(R.string.magnetic_x)))
				enabledFields[Fields.MAG_X] = true;
			if (s.equals(getString(R.string.magnetic_y)))
				enabledFields[Fields.MAG_Y] = true;
			if (s.equals(getString(R.string.magnetic_z)))
				enabledFields[Fields.MAG_Z] = true;
			if (s.equals(getString(R.string.magnetic_total)))
				enabledFields[Fields.MAG_TOTAL] = true;
			if (s.equals(getString(R.string.heading_deg)))
				enabledFields[Fields.HEADING_DEG] = true;
			if (s.equals(getString(R.string.heading_rad)))
				enabledFields[Fields.HEADING_RAD] = true;
			if (s.equals(getString(R.string.temperature_c)))
				enabledFields[Fields.TEMPERATURE_C] = true;
			if (s.equals(getString(R.string.temperature_f)))
				enabledFields[Fields.TEMPERATURE_F] = true;
			if (s.equals(getString(R.string.temperature_k)))
				enabledFields[Fields.TEMPERATURE_K] = true;
			if (s.equals(getString(R.string.pressure)))
				enabledFields[Fields.PRESSURE] = true;
			if (s.equals(getString(R.string.altitude)))
				enabledFields[Fields.ALTITUDE] = true;
			if (s.equals(getString(R.string.luminous_flux)))
				enabledFields[Fields.LIGHT] = true;
		}
	}

	/**
	 * Converts a JSONArray of JSONArray data into a JSONArray of JSONObject
	 * data where each JSONObject is the internal JSONArray element of the old
	 * data, keyed with the project's field IDs.
	 * 
	 * @param oldData
	 *            - JSONArray of JSONArray data to be converted
	 * @return A JSONArray of JSONObjects, ready for upload to the associated
	 *         project.
	 */
	public JSONArray convertInternalDataToJSONObject(JSONArray oldData) {

		getOrderWithExternalAsyncTask();
		JSONArray newData = new JSONArray();

		for (int i = 0; i < oldData.length(); i++) {
			try {
				JSONArray oldRow = oldData.getJSONArray(i);
				JSONObject newRow = new JSONObject();
				for (int j = 0; j < this.fieldIDs.size(); j++) {
					String data = oldRow.getString(j);
					newRow.put(fieldIDs.get(j) + "", data);
				}
				newData.put(newRow);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		return newData;
	}

}
