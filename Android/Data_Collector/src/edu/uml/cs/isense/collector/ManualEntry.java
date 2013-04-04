/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII            General Purpose Manual Data Entry App          SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                                                              SSS                **/
/**           III                    By:               Michael Stowell,         SSS               **/
/**           III                                      Jeremy Poulin,            SSSSSSSSS        **/
/**           III                    Faculty Advisor:  Fred Martin                      SSS       **/
/**           III                                                                        SSS      **/
/**           III                    Group:            ECG / iSENSE                     SSS       **/
/**      IIIIIIIIIIIII               Property:         UMass Lowell             SSSSSSSSSS        **/
/**                                                                                               **/
/***************************************************************************************************/
/***************************************************************************************************/

package edu.uml.cs.isense.collector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.dialogs.ExperimentDialog;
import edu.uml.cs.isense.collector.dialogs.LoginActivity;
import edu.uml.cs.isense.collector.dialogs.MediaManager;
import edu.uml.cs.isense.collector.dialogs.NoGps;
import edu.uml.cs.isense.collector.splash.Splash;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;
import edu.uml.cs.isense.queue.DataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class ManualEntry extends Activity implements OnClickListener,
		LocationListener {

	public static final String activityName = "manualentry";
	private static final String PREFERENCES_EXP_ID = "experiment_id";

	private static final int TYPE_DEFAULT = 1;
	private static final int TYPE_LATITUDE = 2;
	private static final int TYPE_LONGITUDE = 3;
	private static final int TYPE_TIME = 4;

	private static final int LOGIN_REQUESTED = 100;
	private static final int EXPERIMENT_REQUESTED = 101;
	private static final int NO_GPS_REQUESTED = 102;
	private static final int QUEUE_UPLOAD_REQUESTED = 103;
	private static final int MEDIA_REQUESTED = 104;

	private static boolean showGpsDialog = true;

	private Waffle w;
	private RestAPI rapi;

	private Button saveData;
	private Button clearData;
	private ImageButton mediaButton;

	public static Context mContext;

	private TextView loginLabel;
	private TextView experimentLabel;

	private SharedPreferences loginPrefs;
	private SharedPreferences expPrefs;

	private LinearLayout dataFieldEntryList;

	private LocationManager mLocationManager;
	private Location loc;

	public static UploadQueue uq;
	private static boolean throughUploadButton = false;

	private ArrayList<ExperimentField> fieldOrder;

	private EditText sessionName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manual_entry);

		mContext = this;

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(true);
		
		initLocations();

		loginPrefs = new ObscuredSharedPreferences(Splash.mContext,
				Splash.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		expPrefs = getSharedPreferences("EID", 0);

		loginLabel = (TextView) findViewById(R.id.loginLabel);
		loginLabel.setText(getResources().getString(R.string.loggedInAs)
				+ loginPrefs.getString("username", ""));

		experimentLabel = (TextView) findViewById(R.id.experimentLabel);
		//experimentLabel.setText(getResources().getString(
		//		R.string.usingExperiment)
		//		+ expPrefs.getString(PREFERENCES_EXP_ID, ""));

		sessionName = (EditText) findViewById(R.id.manual_session_name);

		saveData = (Button) findViewById(R.id.manual_save);
		clearData = (Button) findViewById(R.id.manual_clear);
		mediaButton = (ImageButton) findViewById(R.id.manual_media_button);

		saveData.setOnClickListener(this);
		clearData.setOnClickListener(this);
		mediaButton.setOnClickListener(this);

		w = new Waffle(this);
		uq = new UploadQueue("manualentry", mContext, rapi);

		dataFieldEntryList = (LinearLayout) findViewById(R.id.field_view);

		String exp = expPrefs.getString(PREFERENCES_EXP_ID, "");
		if (exp.equals("")) {
			Intent iGetExpId = new Intent(this, ExperimentDialog.class);
			startActivityForResult(iGetExpId, EXPERIMENT_REQUESTED);
		} else {
			loadExperimentData(exp);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.manual_clear:
			clearFields();
			break;
		case R.id.manual_save:
			String exp = expPrefs.getString(PREFERENCES_EXP_ID, "");

			// Clear the setError if the user has finally entered a session name
			if (sessionName.getText().toString().length() != 0)
				sessionName.setError(null);

			if (exp.equals("")) {
				w.make("Invalid or no selected experiment.");
			} else if (sessionName.getText().toString().length() == 0) {
				sessionName.setError("Enter a session name");
			} else {
				new SaveDataTask().execute();
			}
			break;
		case R.id.manual_media_button:
			if (sessionName.getText().toString().length() != 0) {
				sessionName.setError(null);
				Intent iMedia = new Intent(mContext, MediaManager.class);
				iMedia.putExtra("sessionName", sessionName.getText().toString());
				startActivityForResult(iMedia, MEDIA_REQUESTED);
			} else {
				sessionName.setError("Enter a session name first");
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String returnCode = data.getStringExtra("returnCode");

				if (returnCode.equals("Success")) {

					String loginName = loginPrefs.getString("username", "");
					if (loginName.length() >= 18)
						loginName = loginName.substring(0, 18) + "...";
					loginLabel.setText(getResources().getString(
							R.string.loggedInAs)
							+ loginName);
					w.make("Login successful", Waffle.LENGTH_LONG,
							Waffle.IMAGE_CHECK);
				} else if (returnCode.equals("Failed")) {
					Intent i = new Intent(mContext, LoginActivity.class);
					startActivityForResult(i, LOGIN_REQUESTED);
				} else {
					// should never get here
				}
			}
		} else if (requestCode == EXPERIMENT_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String eidString = data.getStringExtra("eid");
				loadExperimentData(eidString);
			} else {
				// they may not have fields on screen now
			}
		} else if (requestCode == NO_GPS_REQUESTED) {
			showGpsDialog = true;
			if (resultCode == RESULT_OK) {
				startActivity(new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}

		} else if (requestCode == MEDIA_REQUESTED) {
			if (resultCode == RESULT_OK) {
				new PrepareQueue().execute();
			}
		} else if (requestCode == QUEUE_UPLOAD_REQUESTED) {

			boolean success = uq.buildQueueFromFile();
			if (!success) {
				w.make("Could not re-build queue from file!", Waffle.IMAGE_X);
			}

		}
	}

	private void loadExperimentData(String eidString) {

		if (eidString != null) {
			new LoadExperimentFieldsTask().execute();
		}
	}

	private void fillDataFieldEntryList(int eid) {
		
		if (fieldOrder.size() == 0) {
			w.make("Cannot retrieve experiment fields with no internet connection", Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			return;
		}
		
		if (dataFieldEntryList != null)
			dataFieldEntryList.removeAllViews();
		else
			return;
		
		experimentLabel.setText(getResources().getString(
				R.string.usingExperiment)
				+ eid);

		for (ExperimentField expField : fieldOrder) {

			if (expField.type_id == expField.GEOSPACIAL) {
				if (expField.unit_id == expField.UNIT_LATITUDE) {
					addDataField(expField, TYPE_LATITUDE);
				} else {
					addDataField(expField, TYPE_LONGITUDE);
				}
			} else if (expField.type_id == expField.TIME) {
				addDataField(expField, TYPE_TIME);
			} else {
				addDataField(expField, TYPE_DEFAULT);
			}
		}

		checkLastImeOptions();

	}

	private void addDataField(ExperimentField expField, int type) {
		LinearLayout dataField = (LinearLayout) View.inflate(this,
				R.layout.manualentryfield, null);
		TextView fieldName = (TextView) dataField
				.findViewById(R.id.manual_dataFieldName);
		fieldName.setText(expField.field_name);
		EditText fieldContents = (EditText) dataField
				.findViewById(R.id.manual_dataFieldContents);

		fieldContents.setSingleLine(true);
		fieldContents.setImeOptions(EditorInfo.IME_ACTION_NEXT);

		if (type != TYPE_DEFAULT) {
			fieldContents.setText("Auto");
			fieldContents.setEnabled(false);

			fieldContents.setClickable(false);
			fieldContents.setCursorVisible(false);
			fieldContents.setFocusable(false);
			fieldContents.setFocusableInTouchMode(false);
			fieldContents.setTextColor(Color.GRAY);
		}

		if (expField.type_id == expField.TEXT) {
			// keyboard to text
			fieldContents.setInputType(InputType.TYPE_CLASS_TEXT);
			fieldContents
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							60) });
			fieldContents.setKeyListener(DigitsKeyListener
					.getInstance(getResources().getString(
							R.string.digits_restriction)));
		} else {
			// keyboard to nums
			fieldContents.setInputType(InputType.TYPE_CLASS_PHONE);
			fieldContents
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							20) });
			fieldContents.setKeyListener(DigitsKeyListener
					.getInstance(getResources().getString(
							R.string.numbers_restriction)));

		}

		dataFieldEntryList.addView(dataField);
	}

	private void checkLastImeOptions() {
		for (int i = (dataFieldEntryList.getChildCount() - 1); i >= 0; i--) {

			EditText dataFieldContents = (EditText) dataFieldEntryList
					.getChildAt(i).findViewById(R.id.manual_dataFieldContents);

			if (dataFieldContents.getText().toString().toLowerCase(Locale.US)
					.contains("auto"))
				continue;
			else {
				dataFieldContents.setImeOptions(EditorInfo.IME_ACTION_DONE);
				break;
			}
		}
	}

	private void clearFields() {
		for (int i = 0; i < dataFieldEntryList.getChildCount(); i++) {
			EditText dataFieldContents = (EditText) dataFieldEntryList
					.getChildAt(i).findViewById(R.id.manual_dataFieldContents);
			if (dataFieldContents.isEnabled())
				dataFieldContents.setText("");
		}
		sessionName.setText("");
	}

	private void uploadFields() {
		throughUploadButton = true;
		if (!rapi.isLoggedIn()) {
			boolean success = false;
			if (loginPrefs.getString("username", "").equals(""))
				success = false;
			else
				success = rapi.login(loginPrefs.getString("username", ""),
						loginPrefs.getString("password", ""));

			if (success)
				manageUploadQueue();
			else
				w.make("Not logged in - if you think you are logged in, please try again.", Waffle.LENGTH_LONG, Waffle.IMAGE_X);
		} else {
			manageUploadQueue();
		}

	}

	// Overridden to prevent user from exiting app unless back button is pressed
	// twice
	@Override
	public void onBackPressed() {

		if (!w.isDisplaying)
			w.make("Double press \"Back\" to exit.", Waffle.LENGTH_SHORT,
					Waffle.IMAGE_CHECK);
		else if (w.canPerformTask)
			super.onBackPressed();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_manual, menu);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_manual_upload:
			uploadFields();

			return true;

		case R.id.menu_item_manual_experiment:
			Intent iExperiment = new Intent(mContext, ExperimentDialog.class);
			startActivityForResult(iExperiment, EXPERIMENT_REQUESTED);

			return true;

		case R.id.menu_item_manual_login:
			Intent iLogin = new Intent(mContext, LoginActivity.class);
			startActivityForResult(iLogin, LOGIN_REQUESTED);

			return true;

		}
		return false;

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLocationManager != null)
			mLocationManager.removeUpdates(ManualEntry.this);

	}

	@Override
	public void onStop() {
		super.onStop();
		if (mLocationManager != null)
			mLocationManager.removeUpdates(ManualEntry.this);
	}

	private String getJSONData() {

		JSONArray row = new JSONArray();

		for (int i = 0; i < dataFieldEntryList.getChildCount(); i++) {
			EditText dataFieldContents = (EditText) dataFieldEntryList
					.getChildAt(i).findViewById(R.id.manual_dataFieldContents);
			TextView dataFieldName = (TextView) dataFieldEntryList
					.getChildAt(i).findViewById(R.id.manual_dataFieldName);
			String contents = dataFieldContents.getText().toString()
					.toLowerCase(Locale.US);
			String name = dataFieldName.getText().toString().toLowerCase(Locale.US);
			if (contents.contains("auto")) {
				// Need to auto-fill the data
				if (name.contains("latitude")) {
					row.put("" + loc.getLatitude());
				} else if (name.contains("longitude")) {
					row.put("" + loc.getLongitude());
				} else if (name.contains("time")) {
					row.put("" + System.currentTimeMillis());
				} else {
					// Shouldn't have gotten here... we'll insert -1 as a
					// default
					row.put("-1");
				}
			} else {
				if (dataFieldContents.getText().toString().length() != 0)
					row.put(dataFieldContents.getText().toString());
				else
					// if no data, put a space as a place holder
					row.put(" ");
			}
		}

		JSONArray data = new JSONArray();
		data.put(row);

		return data.toString();
	}

	// Allows for GPS to be recorded
	public void initLocations() {

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0,
					ManualEntry.this);
		} else {
			if (showGpsDialog) {
				Intent iNoGps = new Intent(mContext, NoGps.class);
				startActivityForResult(iNoGps, NO_GPS_REQUESTED);
				showGpsDialog = false;
			}
		}

		loc = new Location(mLocationManager.getBestProvider(c, true));
	}

	// Prompts the user to upload the rest of their content
	// upon successful upload of data
	private void manageUploadQueue() {

		if (!uq.emptyQueue()) {
			throughUploadButton = false;
			Intent i = new Intent().setClass(mContext, QueueLayout.class);
			i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
			startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
		} else {
			if (throughUploadButton) {
				throughUploadButton = false;
				w.make("There is no data to upload.", Waffle.LENGTH_LONG,
						Waffle.IMAGE_CHECK);
			}
		}

	}

	private class LoadExperimentFieldsTask extends
			AsyncTask<Void, Integer, Void> {
		ProgressDialog dia;
		private boolean error = false;

		@Override
		protected void onPreExecute() {

			OrientationManager.disableRotation(ManualEntry.this);

			dia = new ProgressDialog(ManualEntry.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Loading data fields...");
			dia.setCancelable(false);
			dia.show();

			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			int eid = Integer.parseInt(expPrefs.getString(PREFERENCES_EXP_ID,
					"-1"));
			if (eid != -1) {
				fieldOrder = rapi.getExperimentFields(eid);
			} else {
				// problem!
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (!error) {
				String eid = expPrefs.getString(PREFERENCES_EXP_ID, "-1");

				try {
					fillDataFieldEntryList(Integer.parseInt(eid));
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}

				dia.dismiss();
				OrientationManager.enableRotation(ManualEntry.this);
			}

			super.onPostExecute(result);
		}

	}

	private class SaveDataTask extends AsyncTask<Void, Integer, Void> {

		ProgressDialog dia;
		String city = "", state = "", country = "", addr = "";
		String eid = expPrefs.getString(PREFERENCES_EXP_ID, "");
		DataSet ds;

		@Override
		protected void onPreExecute() {

			OrientationManager.disableRotation(ManualEntry.this);

			dia = new ProgressDialog(ManualEntry.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Saving data...");
			dia.setCancelable(false);
			dia.show();

			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				List<Address> address = new Geocoder(ManualEntry.this,
						Locale.getDefault()).getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
				if (address.size() > 0) {
					city = address.get(0).getLocality();
					state = address.get(0).getAdminArea();
					country = address.get(0).getCountryName();
					addr = address.get(0).getAddressLine(0);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			String data = getJSONData();

			String uploadTime = makeThisDatePretty(System.currentTimeMillis());

			ds = new DataSet(DataSet.Type.DATA, sessionName.getText()
					.toString(), uploadTime, eid, data, null, -1, city, state,
					country, addr);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (ds != null) {
				uq.addDataSetToQueue(ds);
				w.make("Saved data successfully.", Waffle.IMAGE_CHECK);

			} else {
				w.make("Fatal error in saving data!!!", Waffle.IMAGE_X);
			}

			dia.dismiss();
			OrientationManager.enableRotation(ManualEntry.this);
			MediaManager.mediaCount = 0;

			super.onPostExecute(result);
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		loc = location;
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (uq != null)
			uq.buildQueueFromFile();
	}

	private String makeThisDatePretty(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS, MM/dd/yy", Locale.US);
		return sdf.format(time);
	}

	class PrepareQueue extends AsyncTask<Void, Integer, Void> {
		ProgressDialog dia;

		@Override
		protected void onPreExecute() {

			OrientationManager.disableRotation(ManualEntry.this);

			dia = new ProgressDialog(ManualEntry.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Preparing pictures...");
			dia.setCancelable(false);
			dia.show();

			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			String city = "", state = "", country = "", addr = "";
			try {
				List<Address> address = new Geocoder(ManualEntry.this,
						Locale.getDefault()).getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
				if (address.size() > 0) {
					city = address.get(0).getLocality();
					state = address.get(0).getAdminArea();
					country = address.get(0).getCountryName();
					addr = address.get(0).getAddressLine(0);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			String uploadTime = makeThisDatePretty(System.currentTimeMillis());
			String name = (sessionName.getText().toString().equals("")) ? "(No name provided)"
					: sessionName.getText().toString();

			String eid = expPrefs.getString(PREFERENCES_EXP_ID, null);
			if (eid != null) {
				for (File picture : MediaManager.pictureArray) {
					DataSet picDS = new DataSet(DataSet.Type.PIC, name,
							uploadTime, eid, null, picture,
							DataSet.NO_SESSION_DEFINED, city, state, country,
							addr);
					uq.addDataSetToQueue(picDS);

				}

			}
			MediaManager.pictureArray.clear();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dia.dismiss();
			OrientationManager.enableRotation(ManualEntry.this);
			super.onPostExecute(result);
		}
	}

}
