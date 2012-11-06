package edu.uml.cs.isense.genpics;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.genpics.dialogs.LoginActivity;
import edu.uml.cs.isense.genpics.dialogs.NoGps;
import edu.uml.cs.isense.genpics.experiments.ExperimentDialog;
import edu.uml.cs.isense.genpics.objects.DataFieldManager;
import edu.uml.cs.isense.genpics.objects.Fields;
import edu.uml.cs.isense.queue.DataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class Main extends Activity implements LocationListener {
	private static final int CAMERA_PIC_REQUESTED = 101;
	private static final int LOGIN_REQUESTED = 102;
	private static final int NO_GPS_REQUESTED = 103;
	private static final int EXPERIMENT_REQUESTED = 104;
	private static final int QUEUE_UPLOAD_REQUESTED = 105;

	private static final int MENU_ITEM_BROWSE = 0;
	private static final int MENU_ITEM_LOGIN = 1;
	private static final int MENU_ITEM_UPLOAD = 2;

	private static final int TIMER_LOOP = 1000;

	private LocationManager mLocationManager;

	private Uri imageUri;

	public static RestAPI rapi;
	public static UploadQueue uq;
	public static final String activityName = "genpicsmain";

	private static boolean gpsWorking = false;
	private static boolean uploadError = false;
	private static boolean status400 = false;
	public static boolean initialLoginStatus = true;
	private static boolean showGpsDialog = true;

	private EditText name;
	private TextView experimentLabel;
	private Vibrator vibrator;
	private Timer mTimer = null;
	private Handler mHandler;
	private TextView latLong;
	private TextView queueCount;
	private static final double DEFAULT_LAT = 42.6404;
	private static final double DEFAULT_LONG = -71.3533;
	private long curTime;
	private static int waitingCounter = 0;

	public static Context mContext;

	private Waffle w;
	private File picture;
	public static Button takePicture;
	static boolean useMenu = true;
	
	private ProgressDialog dia;
	private Location loc;
	private DataFieldManager dfm;
	private Fields f;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;

		w = new Waffle(mContext);
		
		f = new Fields();

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(true);

		uq = new UploadQueue("generalpictures", mContext, rapi);
		
		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		if (mPrefs.getString("experiment_id", "").equals("")) {
			if (dfm == null) {
				dfm = new DataFieldManager(
						Integer.parseInt(mPrefs.getString("experiment_id", "-1")),
						rapi, mContext, f);
				dfm.getOrder();
			}
		}

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		final SharedPreferences loginPrefs = new ObscuredSharedPreferences(
				mContext, getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		if (loginPrefs.getString("username", "").equals("")) {
			startActivityForResult(new Intent(getApplicationContext(),
					LoginActivity.class), LOGIN_REQUESTED);
		}

		experimentLabel = (TextView) findViewById(R.id.ExperimentLabel);
		experimentLabel.setText(getResources().getString(R.string.experiment)
				+ mPrefs.getString("experiment_id", "None Set"));

		// this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mHandler = new Handler();

		name = (EditText) findViewById(R.id.name);

		latLong = (TextView) findViewById(R.id.myLocation);
		queueCount = (TextView) findViewById(R.id.queueCountLabel);

		takePicture = (Button) findViewById(R.id.takePicture);
		takePicture.getBackground().setColorFilter(0xFFFFFF33,
				PorterDuff.Mode.MULTIPLY);
		takePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (name.getText().length() == 0) {
					name.setError("Enter a name");
					return;
				} else {
					name.setError(null);
				}

				SharedPreferences mPrefs = getSharedPreferences("EID", 0);
				String experimentNum = mPrefs.getString("experiment_id",
						"Error");

				if (experimentNum.equals("Error")) {
					w.make("Please select an experiment first.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_X);
					return;
				}

				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {

					ContentValues values = new ContentValues();

					imageUri = getContentResolver().insert(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							values);

					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
					intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

					OrientationManager.disableRotation(Main.this);
					startActivityForResult(intent, CAMERA_PIC_REQUESTED);
				} else {
					w.make("Cannot write to external storage.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_X);
				}

			}

		});

	}

	@Override
	public void onBackPressed() {
		if (!w.isDisplaying) {
			w.make("Double press \"Back\" to exit.", Waffle.LENGTH_SHORT,
					Waffle.IMAGE_CHECK);
		} else if (w.canPerformTask)
			super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ITEM_BROWSE, Menu.NONE, "Experiment");
		menu.add(Menu.NONE, MENU_ITEM_LOGIN, Menu.NONE, "Login");
		menu.add(Menu.NONE, MENU_ITEM_UPLOAD, Menu.NONE, "Upload");
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!useMenu) {
			menu.getItem(0).setEnabled(false);
			menu.getItem(1).setEnabled(false);
			menu.getItem(2).setEnabled(false);
		} else {
			menu.getItem(0).setEnabled(true);
			menu.getItem(1).setEnabled(true);
			menu.getItem(2).setEnabled(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_BROWSE:

			Intent iExperiment = new Intent(getApplicationContext(),
					ExperimentDialog.class);
			startActivityForResult(iExperiment, EXPERIMENT_REQUESTED);

			return true;

		case MENU_ITEM_LOGIN:
			startActivityForResult(new Intent(getApplicationContext(),
					LoginActivity.class), LOGIN_REQUESTED);
			return true;

		case MENU_ITEM_UPLOAD:
			manageUploadQueue();
			return true;
		}

		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!rapi.isLoggedIn())
			attemptLogin();

		// Rebuilds uploadQueue from saved info
		uq.buildQueueFromFile();
		queueCount.setText("Queue Count: " + uq.queueSize());

		// Check to see if now is the right time to try to upload information.
		// manageUploadQueue();
	}

	private void manageUploadQueue() {

		if (!(rapi.isConnectedToInternet())) {
			w.make("Must be connected to the internet to upload.", Waffle.IMAGE_X);
			return;
		}

		if (!(rapi.isLoggedIn())) {
			w.make("Must be logged in to upload.", Waffle.IMAGE_X);
			return;
		}

		if (uq.emptyQueue()) {
			w.make("No data to upload.", Waffle.IMAGE_CHECK);
			return;
		}

		Intent i = new Intent().setClass(mContext, QueueLayout.class);
		i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
		startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);

	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!gpsWorking)
			initLocManager();
		if (mTimer == null)
			waitingForGPS();
	}

	private static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	@SuppressLint("NewApi")
	public static File convertImageUriToFile(Uri imageUri) {

		int apiLevel = getApiLevel();
		if (apiLevel >= 11) {

			String[] proj = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media._ID,
					MediaStore.Images.ImageColumns.ORIENTATION };
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;

			CursorLoader cursorLoader = new CursorLoader(mContext, imageUri,
					proj, selection, selectionArgs, sortOrder);

			Cursor cursor = cursorLoader.loadInBackground();

			int file_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			int orientation_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			if (cursor.moveToFirst()) {
				@SuppressWarnings("unused")
				String orientation = cursor.getString(orientation_ColumnIndex);
				return new File(cursor.getString(file_ColumnIndex));
			}
			return null;

		} else {

			Cursor cursor = null;
			try {
				String[] proj = { MediaStore.Images.Media.DATA,
						MediaStore.Images.Media._ID,
						MediaStore.Images.ImageColumns.ORIENTATION };
				ContentResolver cr = mContext.getContentResolver();
				cursor = cr.query(imageUri, proj, // Which columns
													// to return
						null, // WHERE clause; which rows to return (all rows)
						null, // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)
				int file_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				int orientation_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
				if (cursor.moveToFirst()) {
					@SuppressWarnings("unused")
					String orientation = cursor
							.getString(orientation_ColumnIndex);
					return new File(cursor.getString(file_ColumnIndex));
				}
				return null;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
	}

	private void postRunnableWaffleError(final String message) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				w.make(message, Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			}
		});
	}

	private Runnable uploader = new Runnable() {
		@Override
		public void run() {

			boolean useDefaultLocation = false;

			final SharedPreferences loginPrefs = new ObscuredSharedPreferences(
					mContext, getSharedPreferences("USER_INFO",
							Context.MODE_PRIVATE));

			boolean success = false;
			
			if (rapi.isConnectedToInternet()) {
				if (rapi.isLoggedIn()) {
					success = true;
				} else {
					success = rapi.login(loginPrefs.getString("username", ""),
							loginPrefs.getString("password", ""));
				}
			} else {
				success = true;
			}

			if (!success) {
				uploadError = true;
				postRunnableWaffleError("Must be logged in to upload pictures");
				return;
			}

			if ((loc.getLatitude() == 0) || (loc.getLongitude() == 0))
				useDefaultLocation = true;
			else
				useDefaultLocation = false;

			// Location
			List<Address> address = null;
			String city = "", state = "", country = "", addr = "";

			try {
				if (loc != null) {

					address = new Geocoder(Main.this, Locale.getDefault())
							.getFromLocation(loc.getLatitude(),
									loc.getLongitude(), 1);

					if (address.size() > 0) {
						city = address.get(0).getLocality();
						state = address.get(0).getAdminArea();
						country = address.get(0).getCountryName();
						addr = address.get(0).getAddressLine(0);

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String experimentNum = mPrefs.getString("experiment_id", "Error");

			if (experimentNum.equals("Error")) {
				uploadError = true;
				postRunnableWaffleError("No experiment selected to upload pictures to");
				return;
			}

			JSONArray dataJSON = new JSONArray();
			JSONArray dataRow = new JSONArray();
			if (useDefaultLocation) {
				f.time = curTime;
				f.lat = DEFAULT_LAT;
				f.lon = DEFAULT_LONG;
				dataRow = dfm.putData();
				//dataRow.put(curTime);
				//dataRow.put(DEFAULT_LAT);
				//dataRow.put(DEFAULT_LONG);
			} else {
				f.time = curTime;
				f.lat = loc.getLatitude();
				f.lon = loc.getLongitude();
				dataRow = dfm.putData();
				//dataRow.put(curTime);
				//dataRow.put(loc.getLatitude());
				//dataRow.put(loc.getLongitude());
			}
			dataJSON.put(dataRow);

			int sessionId = -1;
			if (address == null || address.size() <= 0) {
				sessionId = rapi.createSession(experimentNum, name.getText()
						.toString(), makeThisDatePretty(curTime), "", "", "");
			} else {
				sessionId = rapi.createSession(experimentNum, name.getText()
						.toString(), makeThisDatePretty(curTime), addr, city
						+ ", " + state, country);
			}

			if (sessionId == -1) {
				uploadError = true;
				postRunnableWaffleError("Encountered upload problem: could not create session");
				// Add new DataSet to Queue
				DataSet ds = new DataSet(DataSet.Type.BOTH, name.getText()
						.toString(), makeThisDatePretty(curTime),
						experimentNum, dataJSON.toString(), picture, sessionId,
						city, state, country, addr);
				uq.addDataSetToQueue(ds);
				return;
			}

			// Experiment Closed Checker
			if (sessionId == -400) {
				status400 = true;
				return;
			} else {
				status400 = false;
				if (!rapi.updateSessionData(sessionId, experimentNum, dataJSON)) {
					uploadError = true;
					postRunnableWaffleError("Encountered upload problem: could not add picture");
					// Add new DataSet to Queue
					DataSet ds = new DataSet(DataSet.Type.DATA, name.getText()
							.toString(), makeThisDatePretty(curTime),
							experimentNum, dataJSON.toString(), null,
							sessionId, city, state, country, addr);
					uq.addDataSetToQueue(ds);
					return;
				}

				if (!rapi.uploadPictureToSession(picture, experimentNum,
						sessionId, name.getText().toString(),
						"No description provided.")) {
					uploadError = true;
					// Add new DataSet to Queue
					DataSet ds = new DataSet(DataSet.Type.PIC, name.getText()
							.toString(), makeThisDatePretty(curTime),
							experimentNum, null, picture, sessionId, city,
							state, country, addr);
					uq.addDataSetToQueue(ds);
				}
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CAMERA_PIC_REQUESTED) {
			if (resultCode == RESULT_OK) {
				curTime = System.currentTimeMillis();
				picture = convertImageUriToFile(imageUri);

				takePicture.setEnabled(true);

				uq.buildQueueFromFile();
				queueCount.setText("Queue Count: " + uq.queueSize());

				new UploadTask().execute();

			}

		} else if (requestCode == EXPERIMENT_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				String eidString = data.getStringExtra("eid");

				experimentLabel.setText(getResources().getString(
						R.string.experiment)
						+ eidString);
				
				//SharedPreferences mPrefs = getSharedPreferences("EID", 0);
				//SharedPreferences.Editor mEdit = mPrefs.edit();
				//mEdit.putInt("experiment_id", Integer.parseInt(eidString)).commit();
				
				dfm = new DataFieldManager(Integer.parseInt(eidString), rapi, mContext, f);
				dfm.getOrder();
			}
		} else if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences mPrefs = getSharedPreferences("LOGIN", 0);
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putBoolean("logged_in", true);
				mEditor.commit();
			}
		} else if (requestCode == NO_GPS_REQUESTED) {
			showGpsDialog = true;
			if (resultCode == RESULT_OK) {
				startActivity(new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		loc = location;
		if ((location.getLatitude() != 0) && (location.getLongitude() != 0)) {
			if (gpsWorking == false) {
				vibrator.vibrate(100);
			}
			gpsWorking = true;
		} else {
			gpsWorking = false;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		gpsWorking = false;
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	private class UploadTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			OrientationManager.disableRotation(Main.this);

			dia = new ProgressDialog(Main.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Uploading Picture...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {

			uploader.run();
			publishProgress(100);

			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {

			dia.cancel();

			OrientationManager.enableRotation(Main.this);

			if (status400) {
				w.make("Your data cannot be uploaded to this experiment.  It has been closed.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			} else if (uploadError) {
				// Do nothing - postRunnableWaffleError takes care of this
				// Waffle
			} else {
				w.make("Upload successful", Waffle.LENGTH_LONG,
						Waffle.IMAGE_CHECK);
			}

			queueCount.setText("Queue Count: " + uq.queueSize());
			uq.buildQueueFromFile();

			uploadError = false;
		}
	}

	// gets the user's name if not already provided + login to web site
	private void attemptLogin() {

		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				mContext, getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		if (mPrefs.getString("username", "").equals("")
				&& mPrefs.getString("password", "").equals("")) {
			return;
		}

		if (rapi.isConnectedToInternet()) {
			boolean success = rapi.login(mPrefs.getString("username", ""),
					mPrefs.getString("password", ""));
			if (!success) {
				w.make("Experiencing wifi difficulties - check your wifi signal.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			}
		}
	}

	// initialize location listener to get a point
	private void initLocManager() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		loc = new Location(mLocationManager.getBestProvider(c, true));
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0, Main.this);
			new Location(mLocationManager.getBestProvider(c, true));
		} else {
			if (showGpsDialog) {
				Intent iNoGps = new Intent(mContext, NoGps.class);
				startActivityForResult(iNoGps, NO_GPS_REQUESTED);
				showGpsDialog = false;
			}
		}

	}

	@Override
	protected void onStop() {
		mLocationManager.removeUpdates(Main.this);
		gpsWorking = false;
		if (mTimer != null)
			mTimer.cancel();
		mTimer = null;

		super.onStop();
	}

	private void waitingForGPS() {
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {

						if (gpsWorking)
							latLong.setText("Lat: " + loc.getLatitude()
									+ "\nLong: " + loc.getLongitude());
						else {
							switch (waitingCounter % 5) {
							case (0):
								latLong.setText(R.string.noLocation0);
								break;
							case (1):
								latLong.setText(R.string.noLocation1);
								break;
							case (2):
								latLong.setText(R.string.noLocation2);
								break;
							case (3):
								latLong.setText(R.string.noLocation3);
								break;
							case (4):
								latLong.setText(R.string.noLocation4);
								break;
							}
							waitingCounter++;
						}
					}
				});
			}
		}, 0, TIMER_LOOP);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private String makeThisDatePretty(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS, MM/dd/yy");
		return sdf.format(time);
	}

}