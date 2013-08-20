/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII            General Purpose Data Collector App             SSSSSSSSS        **/
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.dialogs.CanLogin;
import edu.uml.cs.isense.collector.dialogs.Description;
import edu.uml.cs.isense.collector.dialogs.ForceStop;
import edu.uml.cs.isense.collector.dialogs.LoginActivity;
import edu.uml.cs.isense.collector.dialogs.MediaManager;
import edu.uml.cs.isense.collector.dialogs.NeedConnectivity;
import edu.uml.cs.isense.collector.dialogs.NoGps;
import edu.uml.cs.isense.collector.dialogs.Step1Setup;
import edu.uml.cs.isense.collector.dialogs.Summary;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.dfm.SensorCompatibility;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.sync.SyncTime;
import edu.uml.cs.isense.waffle.Waffle;

public class DataCollector extends Activity implements SensorEventListener,
		LocationListener {

	/* Constants */

	// String constants
	public static final String activityName = "datacollector";
	public static final String STEP_1_DATASET_NAME = "dataset_name";
	public static final String STEP_1_SAMPLE_INTERVAL = "sample_interval";
	public static final String STEP_1_TEST_LENGTH = "test_length";

	// Numerical constants
	public static final int S_INTERVAL = 50;
	public static final int TEST_LENGTH = 600;

	private static final int MENU_ITEM_LOGIN = 0;
	private static final int MENU_ITEM_MEDIA = 1;
	private static final int MENU_ITEM_SYNC = 2;

	public static final int DIALOG_CANCELED = 0;
	public static final int DIALOG_OK = 1;
	public static final int DIALOG_PICTURE = 2;

	public static final int SYNC_TIME_REQUESTED = 1;
	public static final int QUEUE_UPLOAD_REQUESTED = 3;
	public static final int LOGIN_REQUESTED = 5;
	public static final int GPS_REQUESTED = 7;
	public static final int FORCE_STOP_REQUESTED = 8;
	public static final int RECORDING_STOPPED_REQUESTED = 9;
	public static final int DESCRIPTION_REQUESTED = 10;
	public static final int CAN_LOGIN_REQUESTED = 11;
	public static final int STEP_1_SETUP_REQUESTED = 12;

	/* UI Objects */

	// Buttons
	private static Button step1;
	private static Button step2;
	private static Button step3;

	// Menu Items
	private Menu mMenu;

	private MenuItem menuLogin;
	private MenuItem menuSync;
	private MenuItem menuMedia;

	// ProgressDialogs
	private ProgressDialog dia;

	// ImageView
	private ImageView isenseLogo;

	/* Formatters */

	private final static DecimalFormat toThou = new DecimalFormat("######0.000");

	/* GeoSpacial and Sensor Components */

	// GeoSpacial Components
	private LocationManager mLocationManager;
	private static Location loc;

	// Sensor Components
	private SensorManager mSensorManager;

	/* Data Recording Specific Components */

	// Recording status trigger
	private static Boolean running = false;

	// Start/stop vibrator and beeper
	private Vibrator vibrator;
	private MediaPlayer mMediaPlayer;

	/* Data Variables */

	// Implemented into data variables
	private static String data;
	private static int dataPointCount = 0;

	// Recording Credentials
	private static String dataSetName;
	private static long sampleInterval;
	private static long recordingLength;
	private static int currentProjID;

	// Raw data variables
	private static String temperature = "";
	private static String pressure = "";
	private static String light = "";

	private float rawAccel[];
	private float rawMag[];
	private static float accel[];
	private static float mag[];
	private static float orientation[];

	/* Publicized Variables */

	// Lists and Queues
	public static LinkedList<String> acceptedFields;
	public static UploadQueue uq;

	// Booleans
	public static boolean inPausedState = false;
	public static boolean terminateThroughPowerOff = false;
	public static boolean manageUploadQueueAfterLogin = false;

	// Strings
	public static String textToDataSet = "";
	public static String toSendOut = "";
	public static String sdFileName = "";

	/* Additional Private Variables */

	// Integers
	private int elapsedMinutes = 0;
	private int elapsedSeconds = 0;
	private static int elapsedMillis = 0;
	private static int totalMillis = 0;

	private static int rotation = 0;

	// Longs
	private static long currentTime = 0;
	private long timeOffset = 0;

	// Strings
	private String dateString;
	private String s_elapsedMinutes;
	private String s_elapsedSeconds;
	private String s_elapsedMillis;
	private String dataSetDescription = "";

	// Booleans
	private static boolean useMenu = false;
	private static boolean preLoad = false;
	private static boolean beginWrite = true;
	private static boolean sdCardError = false;
	private static boolean showGpsDialog = true;
	private static boolean throughUploadMenuItem = false;
	private static boolean writeCSVFile = true;

	/* Additional Objects */

	// Built-In
	private Animation rotateInPlace;
	private static Handler mHandler;

	private static File SDFile;
	private static FileWriter gpxwriter;
	private static BufferedWriter out;
	private static Context mContext;
	
	public static JSONArray dataSet;

	// Custom
	public static API api;
	public static Waffle w;
	public static DataFieldManager dfm;
	public static Fields f;
	public static SensorCompatibility sc;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);

		OrientationManager.disableRotation(DataCollector.this);

		mHandler = new Handler();
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				rotateInPlace = AnimationUtils.loadAnimation(
						DataCollector.this, R.anim.superspinner);
				ImageView spinner = (ImageView) findViewById(R.id.spinner);
				spinner.startAnimation(rotateInPlace);
			}
		});

		// Set main context of application once
		mContext = this;

		// Action bar customization for API >= 14
		setActionBarNormal();

		// Load the main UI
		new LoadingMainTask().execute();

	}

	// (s)tarts, (u)pdates, and (f)inishes writing the .csv to the SD Card
	// containing "data"
	public static void writeToSDCard(String data, char code) {
		switch (code) {
		case 's':
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss",
					Locale.US);
			Date dt = new Date();
			String csvDateString = sdf.format(dt);

			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/iSENSE");

			if (!folder.exists()) {
				folder.mkdir();
			}

			SDFile = new File(folder, dataSetName + "--" + csvDateString
					+ ".csv");
			sdFileName = dataSetName + " - " + csvDateString;

			try {
				gpxwriter = new FileWriter(SDFile);
				out = new BufferedWriter(gpxwriter);
				out.write(data);
				beginWrite = false;
			} catch (IOException e) {
				sdCardError = true;
			} catch (Exception e) {
				sdCardError = true;
			}

			break;

		case 'u':
			try {
				out.append(data);
			} catch (IOException e) {
				if (running)
					sdCardError = true;

			} catch (Exception e) {
				if (running)
					sdCardError = true;
			}

			break;

		case 'f':
			try {
				if (out != null)
					out.close();
				if (gpxwriter != null)
					gpxwriter.close();
			} catch (IOException e) {
				sdCardError = true;
			}

			break;

		default:
			sdCardError = true;
			break;
		}
	}

	// Activity paused
	@Override
	public void onPause() {
		super.onPause();

		inPausedState = true;

	}

	// Activity stopped
	@Override
	public void onStop() {
		super.onStop();
		if (!running) {
			if (mLocationManager != null)
				mLocationManager.removeUpdates(DataCollector.this);

			if (mSensorManager != null)
				mSensorManager.unregisterListener(DataCollector.this);
		}

		inPausedState = true;

	}

	// Called when activity rotates
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.automatic_concept);

		// Re-initialize everything we're going to need
		reInitVars();
		reInitMainUI();

		// Assign everything to respective variables
		assignVars();
	}

	// Activity resuming
	@Override
	public void onResume() {
		super.onResume();

		// Will call the login dialogue if necessary
		// and update UI
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				DataCollector.mContext,
				DataCollector.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		if (!(mPrefs.getString("username", "").equals("")) && !inPausedState)
			login();

		inPausedState = false;

		// if (uq != null)
		// uq.buildQueueFromFile();

		// keeps menu buttons disabled while running
		if (running)
			setMenuStatus(false);
		else if (step3 != null)
			if (uq != null)
				if (uq.emptyQueue())
					disableStep3();
				else
					enableStep3();
	}

	// Overridden to prevent user from exiting app unless back button is pressed
	// twice
	@Override
	public void onBackPressed() {

		if (!w.isDisplaying) {
			if (running)
				w.make("Cannot exit via BACK while recording data; use HOME instead.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			else
				w.make("Double press \"Back\" to exit.");

		} else if (w.canPerformTask && !running) {
			super.onBackPressed();
		}
	}

	// Create menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);

		mMenu = menu;

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		menuLogin = menu.getItem(MENU_ITEM_LOGIN);
		menuMedia = menu.getItem(MENU_ITEM_MEDIA);
		menuSync = menu.getItem(MENU_ITEM_SYNC);

		if (preLoad)
			setMenuStatus(false);

		return true;

	}

	// Prepare the menu (enable/disable properly)
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!preLoad)
			setMenuStatus(true);
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

	// Handle all menu item selections
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_login:
			Intent iLogin = new Intent(mContext, LoginActivity.class);
			startActivityForResult(iLogin, LOGIN_REQUESTED);
			return true;
		case R.id.menu_item_sync:
			Intent iTime = new Intent(DataCollector.this, SyncTime.class);
			startActivityForResult(iTime, SYNC_TIME_REQUESTED);
			return true;
		case R.id.menu_item_media:
			Intent iMedia = new Intent(DataCollector.this, MediaManager.class);
			iMedia.putExtra("dataSetName", dataSetName);
			startActivity(iMedia);
			return true;
		}
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (dfm.enabledFields[Fields.ACCEL_X]
					|| dfm.enabledFields[Fields.ACCEL_Y]
					|| dfm.enabledFields[Fields.ACCEL_Z]
					|| dfm.enabledFields[Fields.ACCEL_TOTAL]) {

				rawAccel = event.values.clone();

				switch (rotation) {
				case 90:
					// x = -y && y = x
					accel[0] = -event.values[1];
					accel[1] = event.values[0];
					break;
				case 180:
					// x = -x && y = -y
					accel[0] = -event.values[0];
					accel[1] = -event.values[1];
					break;
				case 270:
					// x = y && y = -x
					accel[0] = event.values[1];
					accel[1] = -event.values[0];
					break;
				case 0:
				default:
					accel[0] = event.values[0];
					accel[1] = event.values[1];
				}

				accel[2] = event.values[2];
				accel[3] = (float) Math.sqrt((float) (Math.pow(accel[0], 2)
						+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2)));
			}

		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			if (dfm.enabledFields[Fields.MAG_X]
					|| dfm.enabledFields[Fields.MAG_Y]
					|| dfm.enabledFields[Fields.MAG_Z]
					|| dfm.enabledFields[Fields.MAG_TOTAL]
					|| dfm.enabledFields[Fields.HEADING_DEG]
					|| dfm.enabledFields[Fields.HEADING_RAD]) {

				rawMag = event.values.clone();

				switch (rotation) {
				case 90:
					// x = -y && y = x
					mag[0] = -event.values[1];
					mag[1] = event.values[0];
					break;
				case 180:
					// x = -x && y = -y
					mag[0] = -event.values[0];
					mag[1] = -event.values[1];
					break;
				case 270:
					// x = y && y = -x
					mag[0] = event.values[1];
					mag[1] = -event.values[0];
					break;
				case 0:
				default:
					mag[0] = event.values[0];
					mag[1] = event.values[1];
				}

				mag[2] = event.values[2];

				float rotation[] = new float[9];

				if (SensorManager.getRotationMatrix(rotation, null, rawAccel,
						rawMag)) {
					orientation = new float[3];
					SensorManager.getOrientation(rotation, orientation);
				}
			}

		} else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
			if (dfm.enabledFields[Fields.TEMPERATURE_C]
					|| dfm.enabledFields[Fields.TEMPERATURE_F]
					|| dfm.enabledFields[Fields.TEMPERATURE_K])
				temperature = toThou.format(event.values[0]);
		} else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
			if (dfm.enabledFields[Fields.PRESSURE])
				pressure = toThou.format(event.values[0]);
		} else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			if (dfm.enabledFields[Fields.LIGHT])
				light = toThou.format(event.values[0]);
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

	// Performs tasks after returning to main UI from previous activities
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SYNC_TIME_REQUESTED) {
			if (resultCode == RESULT_OK) {
				timeOffset = data.getExtras().getLong("offset");
				SharedPreferences mPrefs = getSharedPreferences("time_offset",
						0);
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putLong("timeOffset", timeOffset);
				mEditor.commit();
			}

		} else if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String returnCode = data.getStringExtra("returnCode");

				if (returnCode.equals("Success")) {

					w.make("Login successful", Waffle.LENGTH_LONG,
							Waffle.IMAGE_CHECK);

					if (manageUploadQueueAfterLogin) {
						manageUploadQueueAfterLogin = false;
						manageUploadQueue();
					}

				} else if (returnCode.equals("Failed")) {

					Intent i = new Intent(mContext, LoginActivity.class);
					startActivityForResult(i, LOGIN_REQUESTED);
				} else {
					// should never get here
				}

			} else if (resultCode == RESULT_CANCELED) {
				if (manageUploadQueueAfterLogin) {
					manageUploadQueueAfterLogin = false;
					manageUploadQueue();
				}
			}

		} else if (requestCode == GPS_REQUESTED) {
			showGpsDialog = true;
			if (resultCode == RESULT_OK) {
				startActivity(new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}

		} else if (requestCode == FORCE_STOP_REQUESTED) {
			step2.performLongClick();

		} else if (requestCode == RECORDING_STOPPED_REQUESTED) {
			Intent iDescription = new Intent(mContext, Description.class);
			startActivityForResult(iDescription, DESCRIPTION_REQUESTED);

		} else if (requestCode == DESCRIPTION_REQUESTED) {

			step3.setText(getResources().getString(R.string.step3));
			disableStep2();

			if (resultCode == RESULT_OK) {
				dataSetDescription = data.getStringExtra("description");
				new SaveDataTask().execute();

			} else if (resultCode == RESULT_CANCELED) {
				w.make("Data set deleted", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
				OrientationManager.enableRotation((Activity) mContext);
			}

		} else if (requestCode == QUEUE_UPLOAD_REQUESTED) {

			boolean success = uq.buildQueueFromFile();
			if (!success) {
				w.make("Could not re-build queue from file!", Waffle.IMAGE_X);
			}

		} else if (requestCode == STEP_1_SETUP_REQUESTED) {
			if (resultCode == RESULT_OK) {

				if (data != null) {
					dataSetName = data.getStringExtra(STEP_1_DATASET_NAME);
					sampleInterval = data.getLongExtra(STEP_1_SAMPLE_INTERVAL,
							Step1Setup.S_INTERVAL);
					recordingLength = data.getLongExtra(STEP_1_TEST_LENGTH,
							Step1Setup.TEST_LENGTH);
					enableStep2();
					new GetDfmOrderTask().execute();
					// initDfm();
				}
			}
		} else if (requestCode == CAN_LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {
				Intent iLogin = new Intent(mContext, LoginActivity.class);
				startActivityForResult(iLogin, LOGIN_REQUESTED);
			} else if (resultCode == RESULT_CANCELED) {
				if (manageUploadQueueAfterLogin) {
					manageUploadQueueAfterLogin = false;
					manageUploadQueue();
				}
			}
		}
	}

	// Calls the rapi primitives for actual uploading
	private Runnable uploader = new Runnable() {

		@Override
		public void run() {

			// Prepare description for data set
			String description;
			if (dataSetDescription.equals(""))
				description = "Automated Submission Through Android Data Collector App";
			else
				description = dataSetDescription;

			SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
			String projID = mPrefs.getString("project_id", "");

			// Reset the description
			dataSetDescription = "";

			// Start to upload media and data
			int pic = MediaManager.pictureArray.size();

			// Check for media
			if (pic != 0) {

				// Associates latest picture with the current dataSet, then
				// associates rest
				// to project
				boolean firstSave = true;

				while (pic > 0) {

					// First run through, save data with the picture
					if (firstSave) {
						QDataSet ds = new QDataSet(QDataSet.Type.BOTH,
								dataSetName, description, projID,
								dataSet.toString(),
								MediaManager.pictureArray.get(pic - 1));
						uq.addDataSetToQueue(ds);
						firstSave = false;

						// Next set of runs, save the remaining pictures
					} else {
						QDataSet dsp = new QDataSet(QDataSet.Type.PIC,
								dataSetName, description, projID, null,
								MediaManager.pictureArray.get(pic - 1));
						uq.addDataSetToQueue(dsp);
					}

					pic--;
				}

				// When finished, clear out the media array
				MediaManager.pictureArray.clear();

				// Else if no pictures, just save data
			} else {
				QDataSet ds = new QDataSet(QDataSet.Type.DATA, dataSetName,
						description, projID, dataSet.toString(), null);
				uq.addDataSetToQueue(ds);
			}

		}

	};

	// Control task for uploading data
	private class SaveDataTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

			OrientationManager.disableRotation(DataCollector.this);

			dia = new ProgressDialog(DataCollector.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while your data are saved...");
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
			dia.setMessage("Done");
			dia.cancel();

			OrientationManager.enableRotation(DataCollector.this);

			MediaManager.mediaCount = 0;

			dataSetName = "";
			recordingLength = TEST_LENGTH;
			sampleInterval = S_INTERVAL;

			showSummary();

			w.make("Your data set has been saved", Waffle.LENGTH_SHORT,
					Waffle.IMAGE_CHECK);
		}

	}

	// Updates time on main UI
	public static void setTime(int seconds) {
		int min = seconds / 60;
		int secInt = seconds % 60;

		String sec = "";
		if (secInt <= 9)
			sec = "0" + secInt;
		else
			sec = "" + secInt;

		step3.setText("Time Elapsed: " + min + ":" + sec
				+ "\nData Point Count: " + dataPointCount);
	}

	// Deals with login and UI display
	void login() {
		new LoginTask().execute();
	}

	// Attempts to login with current user information
	private class LoginTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			final SharedPreferences mPrefs = new ObscuredSharedPreferences(
					DataCollector.mContext,
					DataCollector.mContext.getSharedPreferences("USER_INFO",
							Context.MODE_PRIVATE));

			boolean success = api.createSession(
					mPrefs.getString("username", ""),
					mPrefs.getString("password", ""));
			return success;
		}

	}

	// Gets the milliseconds since Epoch
	private long getUploadTime() {
		Calendar c = Calendar.getInstance();
		SharedPreferences mPrefs = getSharedPreferences("time_offset", 0);
		timeOffset = mPrefs.getLong("timeOffset", 0);

		return (((long) c.getTimeInMillis()) + timeOffset);
	}

	// Registers Sensors
	@SuppressLint("InlinedApi")
	private void registerSensors() {

		if (mSensorManager != null) {

			if (dfm == null)
				initDfm();

			if (dfm.enabledFields[Fields.ACCEL_X]
					|| dfm.enabledFields[Fields.ACCEL_Y]
					|| dfm.enabledFields[Fields.ACCEL_Z]
					|| dfm.enabledFields[Fields.ACCEL_TOTAL]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager
								.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[Fields.MAG_X]
					|| dfm.enabledFields[Fields.MAG_Y]
					|| dfm.enabledFields[Fields.MAG_Z]
					|| dfm.enabledFields[Fields.MAG_TOTAL]
					|| dfm.enabledFields[Fields.HEADING_DEG]
					|| dfm.enabledFields[Fields.HEADING_RAD]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager
								.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[Fields.TEMPERATURE_C]
					|| dfm.enabledFields[Fields.TEMPERATURE_F]
					|| dfm.enabledFields[Fields.TEMPERATURE_K]
					|| dfm.enabledFields[Fields.ALTITUDE]) {
				mSensorManager
						.registerListener(
								DataCollector.this,
								mSensorManager
										.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
								SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[Fields.PRESSURE]
					|| dfm.enabledFields[Fields.ALTITUDE]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[Fields.LIGHT]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

		}
	}

	// Calculates Altitude from Temperature and Pressure if Possible
	private static String calcAltitude() {
		if ((dfm.enabledFields[Fields.TEMPERATURE_C]
				|| dfm.enabledFields[Fields.TEMPERATURE_F] || dfm.enabledFields[Fields.TEMPERATURE_K])
				&& dfm.enabledFields[Fields.PRESSURE]) {

			if (pressure.equals("") || temperature.equals("")) {
				return "";
			} else {
				double temp = Math.pow(1013.25 / Double.parseDouble(pressure),
						(1 / 5.257));
				temp *= (Double.parseDouble(temperature) + 273.15);
				temp /= 0.0065;

				String altitude = toThou.format(temp);
				return altitude;
			}

		} else
			return "";
	}

	private void setUpDFMWithAllFields() {
		SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
		SharedPreferences.Editor mEdit = mPrefs.edit();
		mEdit.putString("project_id", "-1").commit();

		dfm = new DataFieldManager(Integer.parseInt(mPrefs.getString(
				"project_id", "-1")), api, mContext, f);
		dfm.getOrder();

		for (int i = 0; i < Fields.TEMPERATURE_K; i++)
			dfm.enabledFields[i] = true;

		String acceptedFields = getResources().getString(R.string.time) + ","
				+ getResources().getString(R.string.accel_x) + ","
				+ getResources().getString(R.string.accel_y) + ","
				+ getResources().getString(R.string.accel_z) + ","
				+ getResources().getString(R.string.accel_total) + ","
				+ getResources().getString(R.string.latitude) + ","
				+ getResources().getString(R.string.longitude) + ","
				+ getResources().getString(R.string.magnetic_x) + ","
				+ getResources().getString(R.string.magnetic_y) + ","
				+ getResources().getString(R.string.magnetic_z) + ","
				+ getResources().getString(R.string.magnetic_total) + ","
				+ getResources().getString(R.string.heading_deg) + ","
				+ getResources().getString(R.string.heading_rad) + ","
				+ getResources().getString(R.string.temperature_c) + ","
				+ getResources().getString(R.string.pressure) + ","
				+ getResources().getString(R.string.altitude) + ","
				+ getResources().getString(R.string.luminous_flux) + ","
				+ getResources().getString(R.string.temperature_f) + ","
				+ getResources().getString(R.string.temperature_k);

		mEdit.putString("accepted_fields", acceptedFields).commit();
	}

	private void initDfm() {

		SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
		String projectInput = mPrefs.getString("project_id", "");

		if (projectInput.equals("-1")) {
			setUpDFMWithAllFields();
		} else {
			dfm = new DataFieldManager(Integer.parseInt(projectInput), api,
					mContext, f);
			dfm.getOrder();

			sc = dfm.checkCompatibility();

			String fields = mPrefs.getString("accepted_fields", "");
			getFieldsFromPrefsString(fields);
			getEnabledFields();

		}
	}

	private void initDfmWithExternalAsyncTask() {

		SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
		String projectInput = mPrefs.getString("project_id", "");

		if (projectInput.equals("-1")) {
			setUpDFMWithAllFields();
		} else {
			dfm = new DataFieldManager(Integer.parseInt(projectInput), api,
					mContext, f);
			dfm.getOrderWithExternalAsyncTask();

			sc = dfm.checkCompatibility();

			String fields = mPrefs.getString("accepted_fields", "");
			getFieldsFromPrefsString(fields);
			getEnabledFields();

		}
	}

	// (currently 2 of these methods exist - one also in step1setup)
	private void getEnabledFields() {

		try {
			for (String s : acceptedFields) {
				if (s.length() != 0)
					break;
			}
		} catch (NullPointerException e) {
			SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
			String fields = mPrefs.getString("accepted_fields", "");
			getFieldsFromPrefsString(fields);
		}

		for (String s : acceptedFields) {
			if (s.equals(getString(R.string.time)))
				dfm.enabledFields[Fields.TIME] = true;

			else if (s.equals(getString(R.string.accel_x)))
				dfm.enabledFields[Fields.ACCEL_X] = true;

			else if (s.equals(getString(R.string.accel_y)))
				dfm.enabledFields[Fields.ACCEL_Y] = true;

			else if (s.equals(getString(R.string.accel_z)))
				dfm.enabledFields[Fields.ACCEL_Z] = true;

			else if (s.equals(getString(R.string.accel_total)))
				dfm.enabledFields[Fields.ACCEL_TOTAL] = true;

			else if (s.equals(getString(R.string.latitude)))
				dfm.enabledFields[Fields.LATITUDE] = true;

			else if (s.equals(getString(R.string.longitude)))
				dfm.enabledFields[Fields.LONGITUDE] = true;

			else if (s.equals(getString(R.string.magnetic_x)))
				dfm.enabledFields[Fields.MAG_X] = true;

			else if (s.equals(getString(R.string.magnetic_y)))
				dfm.enabledFields[Fields.MAG_Y] = true;

			else if (s.equals(getString(R.string.magnetic_z)))
				dfm.enabledFields[Fields.MAG_Z] = true;

			else if (s.equals(getString(R.string.magnetic_total)))
				dfm.enabledFields[Fields.MAG_TOTAL] = true;

			else if (s.equals(getString(R.string.heading_deg)))
				dfm.enabledFields[Fields.HEADING_DEG] = true;

			else if (s.equals(getString(R.string.heading_rad)))
				dfm.enabledFields[Fields.HEADING_RAD] = true;

			else if (s.equals(getString(R.string.temperature_c)))
				dfm.enabledFields[Fields.TEMPERATURE_C] = true;

			else if (s.equals(getString(R.string.temperature_f)))
				dfm.enabledFields[Fields.TEMPERATURE_F] = true;

			else if (s.equals(getString(R.string.temperature_k)))
				dfm.enabledFields[Fields.TEMPERATURE_K] = true;

			else if (s.equals(getString(R.string.pressure)))
				dfm.enabledFields[Fields.PRESSURE] = true;

			else if (s.equals(getString(R.string.altitude)))
				dfm.enabledFields[Fields.ALTITUDE] = true;

			else if (s.equals(getString(R.string.luminous_flux)))
				dfm.enabledFields[Fields.LIGHT] = true;

		}
	}

	private void getFieldsFromPrefsString(String fieldList) {

		String[] fields = fieldList.split(",");
		acceptedFields = new LinkedList<String>();

		for (String f : fields) {
			acceptedFields.add(f);
		}
	}

	// Prompts the user to upload the rest of their content
	// upon successful upload of data
	private void manageUploadQueue() {

		if (!uq.emptyQueue()) {
			throughUploadMenuItem = false;
			Intent i = new Intent().setClass(mContext, QueueLayout.class);
			i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
			startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
		} else {
			if (throughUploadMenuItem) {
				throughUploadMenuItem = false;
				w.make("There is no data to upload", Waffle.LENGTH_LONG,
						Waffle.IMAGE_CHECK);
			}
		}

	}

	// UI variables initialized for onCreate
	private void initMainUI() {
		isenseLogo = (ImageView) findViewById(R.id.isenseLogo);
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			isenseLogo.setVisibility(View.GONE);
		}

		step1 = (Button) findViewById(R.id.auto_step1);
		step1.setText(getResources().getString(R.string.step1));
		step2 = (Button) findViewById(R.id.auto_step2);
		step2.setText(getResources().getString(R.string.step2));
		disableStep2();
		step3 = (Button) findViewById(R.id.auto_step3);
		step3.setText(getResources().getString(R.string.step3));
		if (uq != null)
			if (uq.emptyQueue())
				disableStep3();
			else
				enableStep3();
	}

	// UI variables to re-initialize onConfigurationChange
	private void reInitMainUI() {
		isenseLogo = (ImageView) findViewById(R.id.isenseLogo);
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			isenseLogo.setVisibility(View.GONE);
		}

		step1 = (Button) findViewById(R.id.auto_step1);
		step1.setText(getResources().getString(R.string.step1));
		step2 = (Button) findViewById(R.id.auto_step2);
		step2.setText(getResources().getString(R.string.step2));
		if (dataSetName.equals(""))
			disableStep2();
		step3 = (Button) findViewById(R.id.auto_step3);
		step3.setText(getResources().getString(R.string.step3));
		if (uq != null)
			if (uq.emptyQueue())
				disableStep3();
			else
				enableStep3();
	}

	// Variables needed to be initialized for onCreate
	private void initVars() {
		api = API.getInstance(getApplicationContext());
		api.useDev(true);

		uq = new UploadQueue("datacollector", mContext, api);
		uq.buildQueueFromFile();

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		dataSetName = "";
		sampleInterval = S_INTERVAL;
		recordingLength = TEST_LENGTH;

		w = new Waffle(this);
		f = new Fields();

		accel = new float[4];
		orientation = new float[3];
		rawAccel = new float[3];
		rawMag = new float[3];
		mag = new float[3];

		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);

		initLocations();
	}

	// Variables to re-initialize for onConfigurationChange
	private void reInitVars() {
		api = API.getInstance(getApplicationContext());
		api.useDev(true);

		uq = new UploadQueue("datacollector", mContext, api);
		uq.buildQueueFromFile();

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		w = new Waffle(this);
		f = new Fields();

		accel = new float[4];
		orientation = new float[3];
		rawAccel = new float[3];
		rawMag = new float[3];
		mag = new float[3];

		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);

		initLocations();
	}

	@Override
	protected void onStart() {
		if (mLocationManager != null)
			initLocations();
		super.onStart();
	}

	// Everything that needs to be assigned in onCreate()
	private void assignVars() {
		// Set all the login info
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				DataCollector.mContext,
				DataCollector.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		if (!(mPrefs.getString("username", "").equals("")))
			login();

		// Add listener
		setStepButtonListeners();
	}

	// Allows for GPS to be recorded
	public void initLocations() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0,
					DataCollector.this);
		} else {
			if (showGpsDialog) {
				Intent iNoGps = new Intent(mContext, NoGps.class);
				startActivityForResult(iNoGps, GPS_REQUESTED);
				showGpsDialog = false;
			}
		}

		loc = new Location(mLocationManager.getBestProvider(c, true));
	}

	// Simulates a stop if the service finishes
	public static void serviceHasStopped() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (running)
					step2.performLongClick();
			}
		});
	}

	// Displays description dialog when data is done recording
	public void displayDescription() {

		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy, HH:mm:ss",
				Locale.US);
		Date dt = new Date();
		dateString = sdf.format(dt);
		dataSetName += " - " + dateString;

		// absolutely ensure the timer resets to 0
		setTime(0);

		Intent iDescription = new Intent(mContext, Description.class);
		startActivityForResult(iDescription, DESCRIPTION_REQUESTED);

		if (terminateThroughPowerOff) {
			terminateThroughPowerOff = false;
			Intent iForceStop = new Intent(mContext, ForceStop.class);
			startActivity(iForceStop);
		}
	}

	// Code for registering sensors and preparing to poll data
	public void setUpSensorsForRecording() {

		// initDfm();
		registerSensors();

		rotation = getRotation(mContext);
		dataSet = new JSONArray();
		elapsedMillis = 0;
		totalMillis = 0;
		dataPointCount = 0;
		beginWrite = true;
		sdCardError = false;

		currentTime = getUploadTime();
	}

	// Code for polling sensors for data periodically - called by service
	public static void pollForData() {
		dataPointCount++;
		elapsedMillis += sampleInterval;
		totalMillis = elapsedMillis;

		if (dfm.enabledFields[Fields.ACCEL_X])
			f.accel_x = toThou.format(accel[0]);
		if (dfm.enabledFields[Fields.ACCEL_Y])
			f.accel_y = toThou.format(accel[1]);
		if (dfm.enabledFields[Fields.ACCEL_Z])
			f.accel_z = toThou.format(accel[2]);
		if (dfm.enabledFields[Fields.ACCEL_TOTAL])
			f.accel_total = toThou.format(accel[3]);
		if (dfm.enabledFields[Fields.LATITUDE])
			f.latitude = loc.getLatitude();
		if (dfm.enabledFields[Fields.LONGITUDE])
			f.longitude = loc.getLongitude();
		if (dfm.enabledFields[Fields.HEADING_DEG])
			f.angle_deg = toThou.format(orientation[0]);
		if (dfm.enabledFields[Fields.HEADING_RAD])
			if (!f.angle_deg.equals(""))
				f.angle_rad = toThou
						.format((Double.parseDouble(f.angle_deg) * (Math.PI / 180)));
			else
				f.angle_rad = "";
		if (dfm.enabledFields[Fields.MAG_X])
			f.mag_x = toThou.format(mag[0]);
		if (dfm.enabledFields[Fields.MAG_Y])
			f.mag_y = toThou.format(mag[1]);
		if (dfm.enabledFields[Fields.MAG_Z])
			f.mag_z = toThou.format(mag[2]);
		if (dfm.enabledFields[Fields.MAG_TOTAL])
			f.mag_total = toThou.format(Math.sqrt(Math.pow(mag[0], 2)
					+ Math.pow(mag[1], 2) + Math.pow(mag[2], 2)));
		if (dfm.enabledFields[Fields.TIME])
			f.timeMillis = currentTime + elapsedMillis;
		if (dfm.enabledFields[Fields.TEMPERATURE_C])
			f.temperature_c = temperature;
		if (dfm.enabledFields[Fields.TEMPERATURE_F])
			if (!temperature.equals(""))
				f.temperature_f = ""
						+ ((Double.parseDouble(temperature) * 1.8) + 32);
			else
				f.temperature_f = "";
		if (dfm.enabledFields[Fields.TEMPERATURE_K])
			if (!temperature.equals(""))
				f.temperature_k = ""
						+ (Double.parseDouble(temperature) + 273.15);
			else
				f.temperature_k = "";
		if (dfm.enabledFields[Fields.PRESSURE])
			f.pressure = pressure;
		if (dfm.enabledFields[Fields.ALTITUDE])
			f.altitude = calcAltitude();
		if (dfm.enabledFields[Fields.LIGHT])
			f.lux = light;

		if (currentProjID != -1)
			dataSet.put(dfm.putData());
		else
			dataSet.put(dfm.putDataForNoProjectID());

		data = dfm.writeSdCardLine();

		if (writeCSVFile) {
			if (beginWrite) {
				String header = dfm.writeHeaderLine();
				writeToSDCard(header, 's');
				writeToSDCard(data, 'u');
			} else {
				writeToSDCard(data, 'u');
			}
		}
	}

	// All the code for the main button!
	public void setStepButtonListeners() {
		step1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iSetup = new Intent(mContext, Step1Setup.class);
				startActivityForResult(iSetup, STEP_1_SETUP_REQUESTED);
			}
		});

		step2.setOnLongClickListener(new OnLongClickListener() {

			@SuppressLint("NewApi")
			@Override
			public boolean onLongClick(View arg0) {
				if (!running) {
					if (dataSetName.equals("")
							|| ((1000 / sampleInterval) * recordingLength) > Step1Setup.MAX_DATA_POINTS) {
						w.make("Some data not found - please setup again",
								Waffle.LENGTH_LONG, Waffle.IMAGE_X);
						Intent iSetup = new Intent(mContext, Step1Setup.class);
						startActivityForResult(iSetup, STEP_1_SETUP_REQUESTED);
					} else {

						setUpRecordingDescription();

						// start running task
						running = true;

						OrientationManager.disableRotation((Activity) mContext);

						SharedPreferences mPrefs = getSharedPreferences(
								"PROJID", 0);
						String projectInput = mPrefs
								.getString("project_id", "");
						if (projectInput.equals("-1"))
							writeCSVFile = false;
						else
							writeCSVFile = true;
						currentProjID = Integer.parseInt(projectInput);

						getWindow().addFlags(
								WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

						setMenuStatus(false);

						step2.setText(R.string.stopString);
						step2.setTextColor(Color.parseColor("#008800"));

						setUpSensorsForRecording();

						vibrator.vibrate(300);
						mMediaPlayer.setLooping(false);
						mMediaPlayer.start();

						isenseLogo
								.setImageResource(R.drawable.rsense_logo_recording);
						isenseLogo.setBackgroundColor(Color
								.parseColor("#003300"));
						setActionBarRecording();

						if (android.os.Build.VERSION.SDK_INT >= 11) {
							final LinearLayout ll = (LinearLayout) findViewById(R.id.automatic_bright_flash);
							ll.setAlpha(1.0f);
							AlphaAnimation flash = new AlphaAnimation(1.0f,
									0.0f);
							flash.setDuration(500);
							flash.setAnimationListener(new AnimationListener() {
								@SuppressLint("NewApi")
								@Override
								public void onAnimationEnd(Animation animation) {
									ll.setAlpha(0.0f);
								}

								@Override
								public void onAnimationRepeat(
										Animation animation) {
								}

								@Override
								public void onAnimationStart(Animation animation) {
								}
							});
							ll.startAnimation(flash);
						}

						Intent iService = new Intent(mContext,
								DataCollectorService.class);
						iService.putExtra(DataCollectorService.SRATE,
								sampleInterval);
						iService.putExtra(DataCollectorService.REC_LENGTH,
								recordingLength);
						startService(iService);

						return running;

					}

					running = false;
					return running;

				} else {
					vibrator.vibrate(300);
					mMediaPlayer.setLooping(false);
					mMediaPlayer.start();

					stopService(new Intent(mContext, DataCollectorService.class));

					isenseLogo.setImageResource(R.drawable.rsense_logo);
					isenseLogo.setBackgroundColor(Color.parseColor("#000033"));
					setActionBarNormal();

					step1.setVisibility(View.VISIBLE);
					step2.setTextColor(Color.parseColor("#0066FF"));
					step3.setVisibility(View.VISIBLE);

					running = false;

					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

					OrientationManager.enableRotation((Activity) mContext);

					if (writeCSVFile)
						writeToSDCard(null, 'f');

					setMenuStatus(true);

					step2.setText(R.string.step2);
					setTime(0);

					if (writeCSVFile && sdCardError)
						w.make("Could not write file to SD Card",
								Waffle.LENGTH_SHORT, Waffle.IMAGE_X);

					displayDescription();

					bringBackStep1And3();

					return running;
				}

			}

		});

		step3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						DataCollector.mContext, DataCollector.mContext
								.getSharedPreferences("USER_INFO",
										Context.MODE_PRIVATE));

				if ((mPrefs.getString("username", "").equals(""))) {
					if (api.hasConnectivity()) {
						manageUploadQueueAfterLogin = true;
						Intent iCanLogin = new Intent(mContext, CanLogin.class);
						startActivityForResult(iCanLogin, CAN_LOGIN_REQUESTED);
					} else {
						manageUploadQueue();
						Intent iNeedConnectivity = new Intent(mContext,
								NeedConnectivity.class);
						startActivity(iNeedConnectivity);
					}
				} else {
					manageUploadQueue();
				}
			}
		});
	}

	// Used to adjust for sensor data
	public int getRotation(Context context) {
		@SuppressWarnings("deprecation")
		final int rotation = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
				.getOrientation();

		switch (rotation) {

		case Surface.ROTATION_0:
			return 0;

		case Surface.ROTATION_90:
			return 90;

		case Surface.ROTATION_180:
			return 180;

		default:
			return 270;

		}
	}

	private void showSummary() {

		elapsedMillis = totalMillis;
		elapsedSeconds = elapsedMillis / 1000;
		elapsedMillis %= 1000;
		elapsedMinutes = elapsedSeconds / 60;
		elapsedSeconds %= 60;

		if (elapsedSeconds < 10) {
			s_elapsedSeconds = "0" + elapsedSeconds;
		} else {
			s_elapsedSeconds = "" + elapsedSeconds;
		}

		if (elapsedMillis < 10) {
			s_elapsedMillis = "00" + elapsedMillis;
		} else if (elapsedMillis < 100) {
			s_elapsedMillis = "0" + elapsedMillis;
		} else {
			s_elapsedMillis = "" + elapsedMillis;
		}

		if (elapsedMinutes < 10) {
			s_elapsedMinutes = "0" + elapsedMinutes;
		} else {
			s_elapsedMinutes = "" + elapsedMinutes;
		}

		String appendMe = "";
		if (sdCardError)
			appendMe = "File not written to SD Card.";
		else {
			if (sdFileName.equals(""))
				appendMe = "";
			else
				appendMe = "Filename: \n" + sdFileName;
		}

		Intent iSummary = new Intent(mContext, Summary.class);
		iSummary.putExtra("millis", s_elapsedMillis)
				.putExtra("seconds", s_elapsedSeconds)
				.putExtra("minutes", s_elapsedMinutes)
				.putExtra("append", appendMe)
				.putExtra("date", dateString)
				.putExtra("points", "" + dataPointCount);

		startActivity(iSummary);

		// Reset the sdFileName
		sdFileName = "";
	}

	// Loads the main screen
	private class LoadingMainTask extends AsyncTask<Void, Integer, Void> {
		Runnable loadingThread;

		@Override
		protected void onPreExecute() {
			preLoad = true;
			inPausedState = true;
			mHandler = new Handler();
			loadingThread = new Runnable() {

				@Override
				public void run() {
					// Initializes everything we can
					initVars();
				}

			};

			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			mHandler.post(loadingThread);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			inPausedState = false;
			OrientationManager.enableRotation(DataCollector.this);

			if (mMenu != null) {
				onPrepareOptionsMenu(mMenu);
				setMenuStatus(true);
			}
			preLoad = false;

			setContentView(R.layout.automatic_concept);
			initMainUI();
			assignVars();

			super.onPostExecute(result);
		}

	}

	// Get DFM order
	private class GetDfmOrderTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			OrientationManager.disableRotation(DataCollector.this);

			dia = new ProgressDialog(DataCollector.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Loading...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(Void... params) {

			initDfmWithExternalAsyncTask();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dia.setMessage("Done");
			if (dia != null && dia.isShowing())
				dia.cancel();

			OrientationManager.enableRotation(DataCollector.this);
			
//			if (dfm.getOrderList.size() == 0)
//				TODO - API error checking for this case

			super.onPostExecute(result);
		}

	}

	// allows for menu to be turned off when necessary
	private void setMenuStatus(boolean enabled) {
		useMenu = enabled;

		if (mMenu != null) {
			menuLogin.setEnabled(enabled);
			menuMedia.setEnabled(enabled);
			menuSync.setEnabled(enabled);

			// if (enabled) {
			// MenuItem item = mMenu.findItem(R.id.menu_item_login);
			// item.setVisible(true);
			// item = mMenu.findItem(R.id.menu_item_media);
			// item.setVisible(true);
			// item = mMenu.findItem(R.id.menu_item_sync);
			// item.setVisible(true);
			// super.onPrepareOptionsMenu(mMenu);
			// } else {
			// MenuItem item = mMenu.findItem(R.id.menu_item_login);
			// item.setVisible(false);
			// item = mMenu.findItem(R.id.menu_item_media);
			// item.setVisible(false);
			// item = mMenu.findItem(R.id.menu_item_sync);
			// item.setVisible(false);
			// super.onPrepareOptionsMenu(mMenu);
			// }

		}
	}

	private void enableStep1() {
		step1.setEnabled(true);
		step1.setTextColor(Color.parseColor("#0066FF"));
	}

	private void disableStep2() {
		step2.setEnabled(false);
		step2.setTextColor(Color.parseColor("#666666"));
	}

	private void enableStep2() {
		step2.setEnabled(true);
		step2.setTextColor(Color.parseColor("#0066FF"));
	}

	private void disableStep3() {
		step3.setEnabled(false);
		step3.setTextColor(Color.parseColor("#666666"));
	}

	private void enableStep3() {
		step3.setEnabled(true);
		step3.setTextColor(Color.parseColor("#0066FF"));
	}

	private void setUpRecordingDescription() {
		step1.setEnabled(false);
		step1.setBackgroundColor(Color.TRANSPARENT);
		step1.setTextColor(Color.parseColor("#555555"));
		step1.setText("Recording data for \"" + dataSetName
				+ "\" at a sample interval of " + sampleInterval + " ms for "
				+ recordingLength + " sec.");

		step3.setEnabled(false);
		step3.setBackgroundColor(Color.TRANSPARENT);
		step3.setTextColor(Color.parseColor("#555555"));
		step3.setText("Time Elapsed: 0:00\nData Point Count: 0");
	}

	private void bringBackStep1And3() {
		enableStep1();
		disableStep2();

		// turn step 3 back, but don't enable it
		step3.setBackgroundResource(R.drawable.button_rsense);
		step3.setTextColor(Color.parseColor("#0066FF"));

		step1.setBackgroundResource(R.drawable.button_rsense);
		step1.setText(getResources().getString(R.string.step1));
		step3.setText(getResources().getString(R.string.step3));
	}

	@SuppressLint("NewApi")
	private void setActionBarNormal() {
		// Action bar customization for API >= 14
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			ActionBar bar = getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#111133")));
			bar.setIcon(getResources()
					.getDrawable(R.drawable.rsense_logo_right));
			bar.setDisplayShowTitleEnabled(false);
			int actionBarTitleId = Resources.getSystem().getIdentifier(
					"action_bar_title", "id", "android");
			if (actionBarTitleId > 0) {
				TextView title = (TextView) findViewById(actionBarTitleId);
				if (title != null) {
					title.setTextColor(Color.WHITE);
					title.setTextSize(24.0f);
				}
			}
		}
	}

	@SuppressLint("NewApi")
	private void setActionBarRecording() {
		// Action bar customization for API >= 14
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			ActionBar bar = getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#07420E")));
			bar.setIcon(getResources().getDrawable(
					R.drawable.rsense_logo_recording_right));
			bar.setDisplayShowTitleEnabled(false);
			int actionBarTitleId = Resources.getSystem().getIdentifier(
					"action_bar_title", "id", "android");
			if (actionBarTitleId > 0) {
				TextView title = (TextView) findViewById(actionBarTitleId);
				if (title != null) {
					title.setTextColor(Color.WHITE);
					title.setTextSize(24.0f);
				}
			}
		}
	}

}