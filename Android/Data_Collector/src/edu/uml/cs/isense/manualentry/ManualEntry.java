package edu.uml.cs.isense.manualentry;

import java.util.ArrayList;
import java.util.Queue;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.collector.objects.DataSet;
import edu.uml.cs.isense.collector.splash.Splash;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.complexdialogs.ExperimentDialog;
import edu.uml.cs.isense.complexdialogs.LoginActivity;
import edu.uml.cs.isense.objects.ExperimentField;
import edu.uml.cs.isense.shared.QueueUploader;
import edu.uml.cs.isense.simpledialogs.NoGps;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
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

	private static final int FIRST = 1000;
	private static final int LAST = 1001;

	private static boolean showGpsDialog = true;

	private Waffle w;
	private RestAPI rapi;

	private Button uploadData;
	private Button saveData;
	private Button clearData;

	public static Context mContext;

	private TextView loginLabel;
	private TextView experimentLabel;

	private SharedPreferences loginPrefs;
	private SharedPreferences expPrefs;

	private LinearLayout dataFieldEntryList;

	private LocationManager mLocationManager;
	private Location loc;

	public static Queue<DataSet> uploadQueue;
	private static boolean uploadSuccess = true;
	private static boolean throughUploadButton = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manual_entry);

		mContext = this;

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());

		initLocations();

		loginPrefs = new ObscuredSharedPreferences(Splash.mContext,
				Splash.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		expPrefs = getSharedPreferences("EID", 0);

		loginLabel = (TextView) findViewById(R.id.loginLabel);
		loginLabel.setText(getResources().getString(R.string.loggedInAs)
				+ loginPrefs.getString("username", ""));

		experimentLabel = (TextView) findViewById(R.id.experimentLabel);
		experimentLabel.setText(getResources().getString(
				R.string.usingExperiment)
				+ expPrefs.getString(PREFERENCES_EXP_ID, ""));

		uploadData = (Button) findViewById(R.id.manual_upload);
		saveData = (Button) findViewById(R.id.manual_save);
		clearData = (Button) findViewById(R.id.manual_clear);

		uploadData.setOnClickListener(this);
		saveData.setOnClickListener(this);
		clearData.setOnClickListener(this);

		uploadData.setOnClickListener(this);
		saveData.setOnClickListener(this);
		clearData.setOnClickListener(this);

		w = new Waffle(this);

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
			if (exp.equals("")) {
				w.make("Fatal error: Invalid experiment.");
			} else {
				saveFields(exp);
			}
			break;
		case R.id.manual_upload:
			uploadFields();
			break;
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

		}
	}

	private void loadExperimentData(String eidString) {
		if (dataFieldEntryList != null)
			dataFieldEntryList.removeAllViews();

		if (eidString != null) {
			int eid = Integer.parseInt(eidString);
			if (eid != -1) {
				experimentLabel.setText(getResources().getString(
						R.string.usingExperiment)
						+ eid);
				fillDataFieldEntryList(eid);
				rapi.getExperimentFields(eid);
			} else {
				w.make("CRITICAL ERROR", Waffle.IMAGE_X);
			}
		}
	}

	private void fillDataFieldEntryList(int eid) {
		ArrayList<ExperimentField> fieldOrder = rapi.getExperimentFields(eid);
		for (ExperimentField expField : fieldOrder) {
			int firstOrLast = FIRST;
			if (fieldOrder.indexOf(expField) == (fieldOrder.size() - 1)) {
				firstOrLast = LAST;
			}
			if (expField.type_id == expField.GEOSPACIAL) {
				if (expField.unit_id == expField.UNIT_LATITUDE) {
					addDataField(expField, TYPE_LATITUDE, firstOrLast);
				} else {
					addDataField(expField, TYPE_LONGITUDE, firstOrLast);
				}
			} else if (expField.type_id == expField.TIME) {
				addDataField(expField, TYPE_TIME, firstOrLast);
			} else {
				addDataField(expField, TYPE_DEFAULT, firstOrLast);
			}
		}
	}

	private void addDataField(ExperimentField expField, int type, int fol) {
		LinearLayout dataField = (LinearLayout) View.inflate(this,
				R.layout.manualentryfield, null);
		TextView fieldName = (TextView) dataField
				.findViewById(R.id.manual_dataFieldName);
		fieldName.setText(expField.field_name);
		EditText fieldContents = (EditText) dataField
				.findViewById(R.id.manual_dataFieldContents);

		fieldContents.setSingleLine(true);
		if (fol == FIRST)
			fieldContents.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		else
			fieldContents.setImeOptions(EditorInfo.IME_ACTION_DONE);

		if (type != TYPE_DEFAULT) {
			fieldContents.setText("Auto");
			fieldContents.setEnabled(false);

			fieldContents.setClickable(false);
			fieldContents.setCursorVisible(false);
			fieldContents.setFocusable(false);
			fieldContents.setFocusableInTouchMode(false);
			fieldContents.setTextColor(Color.GRAY);

		}
		dataFieldEntryList.addView(dataField);
	}

	private void clearFields() {
		for (int i = 0; i < dataFieldEntryList.getChildCount(); i++) {
			EditText dataFieldContents = (EditText) dataFieldEntryList
					.getChildAt(i).findViewById(R.id.manual_dataFieldContents);
			if (dataFieldContents.isEnabled())
				dataFieldContents.setText("");
		}
	}

	private void saveFields(String eid) {

		String data = getJSONData();
		DataSet ds = new DataSet(DataSet.Type.DATA, "Session Name",
				"Description", eid, data, null, -1, "Lowell", "Massachusetts",
				"USA", "1 University Ave"); // TODO
		uploadQueue.add(ds);

	}

	private void uploadFields() {
		throughUploadButton = true;
		manageUploadQueue();
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

		JSONArray data = new JSONArray();

		for (int i = 0; i < dataFieldEntryList.getChildCount(); i++) {
			EditText dataFieldContents = (EditText) dataFieldEntryList
					.getChildAt(i).findViewById(R.id.manual_dataFieldContents);
			TextView dataFieldName = (EditText) dataFieldEntryList
					.getChildAt(i).findViewById(R.id.manual_dataFieldName);
			String contents = dataFieldContents.getText().toString()
					.toLowerCase();
			String name = dataFieldName.getText().toString().toLowerCase();
			if (contents.contains("auto")) {
				// Need to auto-fill the data
				if (name.contains("latitude")) {
					data.put("" + loc.getLatitude());
				} else if (name.contains("longitude")) {
					data.put("" + loc.getLongitude());
				} else if (name.contains("time")) {
					data.put("" + System.currentTimeMillis());
				} else {
					// Shouldn't have gotten here... we'll insert -1 as a
					// default
					data.put("-1");
				}
			} else {
				data.put(dataFieldContents.getText().toString());
			}
		}

		Log.e("tag", data.toString());
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
		if (uploadSuccess) {
			if (!uploadQueue.isEmpty()) {
				throughUploadButton = false;
				Intent i = new Intent().setClass(mContext, QueueUploader.class);
				i.putExtra(QueueUploader.INTENT_IDENTIFIER, QueueUploader.QUEUE_MANUAL_ENTRY);
				startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
			} else {
				if (throughUploadButton) {
					throughUploadButton = false;
					w.make("There is no data to upload.", Waffle.LENGTH_LONG,
							Waffle.IMAGE_CHECK);
				}
			}
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

}
