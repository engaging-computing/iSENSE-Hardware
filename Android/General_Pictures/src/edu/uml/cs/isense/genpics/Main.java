package edu.uml.cs.isense.genpics;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.os.PowerManager;
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
import edu.uml.cs.isense.genpics.dialogs.NoConnectivity;
import edu.uml.cs.isense.genpics.dialogs.NoGps;
import edu.uml.cs.isense.genpics.dialogs.ReadyUpload;
import edu.uml.cs.isense.genpics.experiments.ExperimentDialog;
import edu.uml.cs.isense.genpics.objects.Picture;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class Main extends Activity implements LocationListener {
	private static final int CAMERA_PIC_REQUESTED = 101;
	private static final int LOGIN_REQUESTED = 102;
	private static final int NO_GPS_REQUESTED = 103;
	private static final int NO_CONNECTIVITY_REQUESTED = 104;
	private static final int EXPERIMENT_REQUESTED = 105;
	private static final int READY_UPLOAD_REQUESTED = 106;

	private static final int MENU_ITEM_BROWSE = 0;
	private static final int MENU_ITEM_LOGIN = 1;

	private static final int TIMER_LOOP = 1000;

	private LocationManager mLocationManager;
	private PowerManager pm;
	private PowerManager.WakeLock wl;

	private Uri imageUri;

	private Queue<Picture> mQ;
	private static int QUEUE_COUNT = 0;

	RestAPI rapi;

	private static boolean gpsWorking = false;
	private static boolean smartUploading = false;
	private static boolean calledBySmartUp = false;
	private static boolean uploadError = false;
	private static boolean status400 = false;
	public static boolean initialLoginStatus = true;
	private static boolean showGpsDialog = true;

	private Picture uploaderPic = null;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;

		w = new Waffle(mContext);

		mQ = new LinkedList<Picture>();

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
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

		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Full Wake Lock");

		//this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mHandler = new Handler();

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(true);

		name = (EditText) findViewById(R.id.name);

		latLong = (TextView) findViewById(R.id.myLocation);
		queueCount = (TextView) findViewById(R.id.queueCountLabel);
		queueCount.setText("Queue Count: " + QUEUE_COUNT);

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
		menu.add(Menu.NONE, MENU_ITEM_BROWSE, Menu.NONE, "Pick Experiment");
		menu.add(Menu.NONE, MENU_ITEM_LOGIN, Menu.NONE, "Login");
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!useMenu) {
			menu.getItem(0).setEnabled(false);
		} else {
			menu.getItem(0).setEnabled(true);
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
		}

		return false;
	}

	@Override
	protected void onResume() {
		if (!smartUploading && !rapi.isConnectedToInternet()) {
			Intent iNoConnect = new Intent(Main.this, NoConnectivity.class);
			startActivityForResult(iNoConnect, NO_CONNECTIVITY_REQUESTED);
		}
		if (!rapi.isLoggedIn() && !initialLoginStatus)
			attemptLogin();
		if (rapi.isLoggedIn()) {
			if (smartUploading && (QUEUE_COUNT > 0)) {
				Intent iReadyUpload = new Intent(Main.this, ReadyUpload.class);
				startActivityForResult(iReadyUpload, READY_UPLOAD_REQUESTED);
			}
		}

		super.onResume();
	}

	@Override
	protected void onStart() {
		if (!gpsWorking)
			initLocManager();
		if (mTimer == null)
			waitingForGPS();

		super.onStart();
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
			// return new File(imageUri.getPath());
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

			// TODO - queue the picture any time there is an error
			
			boolean useDefaultLocation = false;
			
			final SharedPreferences loginPrefs = new ObscuredSharedPreferences(
					mContext, getSharedPreferences("USER_INFO",
							Context.MODE_PRIVATE));

			boolean success = false;
			if (rapi.isLoggedIn()) {
				success = true;
			} else {
				success = rapi.login(loginPrefs.getString("username", ""),
						loginPrefs.getString("password", ""));
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

			if (!smartUploading || (smartUploading && !calledBySmartUp)) {

				SharedPreferences mPrefs = getSharedPreferences("EID", 0);
				String experimentNum = mPrefs.getString("experiment_id",
						"Error");

				if (experimentNum.equals("Error")) {
					uploadError = true;
					postRunnableWaffleError("No experiment selected to upload pictures to");
					return;
				}

				int sessionId = -1;
				if (address == null || address.size() <= 0) {
					sessionId = rapi.createSession(experimentNum, 
							name.getText().toString(), "" + curTime,
							"", "", "");
				} else {
					sessionId = rapi.createSession(experimentNum, 
							name.getText().toString(), "" + curTime,
							addr, city + ", " + state, country);
				}

				if (sessionId == -1) {
					uploadError = true;
					postRunnableWaffleError("Encountered upload problem: could not create session");
					return;
				}

				JSONArray dataJSON = new JSONArray();
				JSONArray dataRow  = new JSONArray();
				try {
					if (useDefaultLocation) {
						dataRow.put(curTime);
						dataRow.put(DEFAULT_LAT);
						dataRow.put(DEFAULT_LONG);
					} else {
						dataRow.put(curTime);
						dataRow.put(loc.getLatitude());
						dataRow.put(loc.getLongitude());
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				dataJSON.put(dataRow);

				// Experiment Closed Checker
				if (sessionId == -400) {
					status400 = true;
					return;
				} else {
					status400 = false;
					if (!rapi.updateSessionData(sessionId, experimentNum,
							dataJSON)) {
						uploadError = true;
						postRunnableWaffleError("Encountered upload problem: could not add picture");
						return;
					}

					if (!rapi.uploadPictureToSession(picture, experimentNum,
							sessionId, name.getText().toString(),
							"No description provided.")) {
						uploadError = true;
					}
				}
			} else {
				smartUploader(uploaderPic.file, uploaderPic.latitude,
						uploaderPic.longitude, uploaderPic.name,
						uploaderPic.time);
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
				if (smartUploading) {
					if (rapi.isLoggedIn()) {
						calledBySmartUp = false;
						new UploadTask().execute();
					} else
						qsave(picture);
				} else {
					calledBySmartUp = false;
					new UploadTask().execute();	
				}

			}

		} else if (requestCode == EXPERIMENT_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				String eidString = data.getStringExtra("eid");

				experimentLabel.setText(getResources().getString(
						R.string.experiment)
						+ eidString);
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

		} else if (requestCode == NO_CONNECTIVITY_REQUESTED) {
			if (resultCode == RESULT_OK) {
				smartUploading = true;
				waitingForConnectivity();
				if (wl.isHeld())
					wl.acquire();
			} else if (resultCode == RESULT_CANCELED) {
				new ReattemptConnectTask().execute();
			}
		} else if (requestCode == READY_UPLOAD_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (QUEUE_COUNT > 0) {
					uploadPicture();
				}
			}
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		loc = location;
		if ((location.getLatitude() != 0)
				&& (location.getLongitude() != 0)) {
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
				// Do nothing - postRunnableWaffleError takes care of this Waffle
				//w.make("An error occured during upload.", Waffle.LENGTH_LONG,
					//	Waffle.IMAGE_X);
			} else {
				w.make("Upload successful",
						Waffle.LENGTH_LONG, Waffle.IMAGE_CHECK);
			}

			uploadError = false;

			if (QUEUE_COUNT > 0)
				uploadPicture();
			
		}
	}

	// attempt to re-find internet connection
	private class ReattemptConnectTask extends AsyncTask<Void, Integer, Void> {

		private ProgressDialog dia;

		@Override
		protected void onPreExecute() {
			OrientationManager.disableRotation(Main.this);
			
			dia = new ProgressDialog(Main.this);
			dia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dia.setMessage("Re-attempting connection...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			final SharedPreferences mPrefs = new ObscuredSharedPreferences(
					mContext, getSharedPreferences("USER_INFO",
							Context.MODE_PRIVATE));

			if (rapi.isConnectedToInternet())
				rapi.login(mPrefs.getString("username", ""),
						mPrefs.getString("password", ""));
				
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {

			dia.dismiss();
			
			OrientationManager.enableRotation(Main.this);

			if (rapi.isConnectedToInternet())
				w.make("Connectivity found!", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
			else {
				if (!smartUploading) {
					Intent iNoConnect = new Intent(Main.this,
							NoConnectivity.class);
					startActivityForResult(iNoConnect,
							NO_CONNECTIVITY_REQUESTED);
				}
			}
		}
	}

	// repeatedly tries to connect
	private class NotConnectedTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			if (rapi.isConnectedToInternet())
				w.make("Connectivity found!", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
			else {
				if (!smartUploading) {
					Intent iNoConnect = new Intent(Main.this,
							NoConnectivity.class);
					startActivityForResult(iNoConnect,
							NO_CONNECTIVITY_REQUESTED);
				}
			}
		}
	}

	// gets the user's name if not already provided + login to web site
	private void attemptLogin() {

		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				mContext, getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		
		if (mPrefs.getString("username", "").equals("") && mPrefs.getString("password", "").equals("")) {
			return;
		}

		if (rapi.isConnectedToInternet()) {
			boolean success = rapi.login(mPrefs.getString("username", ""),
					mPrefs.getString("password", ""));
			if (!success) {
				w.make("Experiencing wifi difficulties - check your wifi signal.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			} else {
				if (smartUploading && (QUEUE_COUNT > 0)) {
					Intent iReadyUpload = new Intent(Main.this,
							ReadyUpload.class);
					startActivityForResult(iReadyUpload, READY_UPLOAD_REQUESTED);
				}
			}

		} else {
			if (!smartUploading) {
				Intent iNoConnect = new Intent(Main.this, NoConnectivity.class);
				startActivityForResult(iNoConnect, NO_CONNECTIVITY_REQUESTED);
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

	// save picture data in a queue for later upload
	private void qsave(File pictureFile) {
		Picture mPic = new Picture(pictureFile, loc.getLatitude(), loc.getLongitude(), name.getText()
				.toString(), System.currentTimeMillis());
		mQ.add(mPic);
		QUEUE_COUNT++;
		queueCount.setText("Queue Count: " + QUEUE_COUNT);
	}

	// get picture data from the q to upload
	private Picture getPicFromQ() {
		Picture mPic = null;
		try {
			mPic = mQ.remove();
		} catch (NoSuchElementException e) {
		}
		return mPic;
	}

	// upload stuff from the queue
	private void smartUploader(File f, double lat, double lon, String n, long t) {

		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		String experimentNum = mPrefs.getString("experiment_id", "Error");

		if (experimentNum.equals("Error")) {
			uploadError = true;
			return;
		}

		int sessionId;
		if ((sessionId = rapi.createSession(experimentNum, n,
				"No description provided.", "n/a", "Lowell, MA", "")) == -1) {
			uploadError = true;
			return;
		}

		JSONArray dataJSON = new JSONArray();
		try {
			dataJSON.put(t);
			dataJSON.put(lat);
			dataJSON.put(lon);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Experiment Closed Checker
		if (sessionId == -400) {
			status400 = true;
			return;
		} else {
			status400 = false;
			boolean success = rapi.updateSessionData(sessionId, experimentNum,
					dataJSON);
			if (!success) {
				uploadError = true;
				return;
			}

			success = rapi.uploadPictureToSession(f, experimentNum, sessionId,
					n, "No description provided.");
			if (!success)
				uploadError = true;
		}
	}

	// uploads pictures if smartUploading is enabled
	private void uploadPicture() {

		if (QUEUE_COUNT > 0) {
			uploaderPic = getPicFromQ();
			if (uploaderPic != null) {
				QUEUE_COUNT--;
				queueCount.setText("Queue Count: " + QUEUE_COUNT);
				calledBySmartUp = true;
				new UploadTask().execute();
			}
		} else {
			smartUploading = false;
			if (wl.isHeld())
				wl.release();
		}

	}

	@Override
	protected void onStop() {
		mLocationManager.removeUpdates(Main.this);
		gpsWorking = false;
		if (mTimer != null)
			mTimer.cancel();
		if (wl.isHeld())
			wl.release();
		mTimer = null;

		super.onStop();
	}

	// takes care of smart uploading as well as background checking
	private class WaitTenSecondsTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (rapi.isConnectedToInternet()) {
				if (rapi.isLoggedIn()) {
					if (QUEUE_COUNT > 0) {
						Intent iReadyUpload = new Intent(Main.this,
								ReadyUpload.class);
						startActivityForResult(iReadyUpload,
								READY_UPLOAD_REQUESTED);
						waitingForConnectivity();
					}
				}

				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						mContext, getSharedPreferences("USER_INFO",
								Context.MODE_PRIVATE));
				boolean success = rapi.login(mPrefs.getString("username", ""),
						mPrefs.getString("password", ""));
				if (success) {
					w.make("Connectivity found!", Waffle.LENGTH_SHORT,
							Waffle.IMAGE_CHECK);
					if (QUEUE_COUNT > 0) {
						Intent iReadyUpload = new Intent(Main.this,
								ReadyUpload.class);
						startActivityForResult(iReadyUpload,
								READY_UPLOAD_REQUESTED);
					}
				}

			} else {
				if (smartUploading)
					waitingForConnectivity();
				else
					new NotConnectedTask().execute();
			}
			super.onPostExecute(result);
		}
	}

	public void waitingForConnectivity() {
		new WaitTenSecondsTask().execute();
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
							latLong.setText("Lat: " + loc.getLatitude() +
									"\nLong: " + loc.getLongitude());
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

}