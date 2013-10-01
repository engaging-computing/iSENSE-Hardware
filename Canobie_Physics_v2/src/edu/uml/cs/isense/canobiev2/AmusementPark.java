/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII            General Purpose Amusement Park Appication      SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                    Original Creator: John Fertita            SSS                **/
/**           III                    Optimized By:     Jeremy Poulin,           SSS               **/
/**           III                                      Michael Stowell           SSSSSSSSS        **/
/**           III                    Faculty Advisor:  Fred Martin                      SSS       **/
/**           III                    Special Thanks:   Don Rhine                         SSS      **/
/**           III                    Group:            ECG, iSENSE                      SSS       **/
/**      IIIIIIIIIIIII               Property:         UMass Lowell              SSSSSSSSS        **/
/**                                                                                               **/
/***************************************************************************************************/
/***************************************************************************************************/

package edu.uml.cs.isense.canobiev2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.dfm.SensorCompatibility;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.sync.SyncTime;
import edu.uml.cs.isense.waffle.Waffle;

public class AmusementPark extends Activity implements SensorEventListener,
		LocationListener {

	/* Default Constants */
	private final String DEFAULT_USERNAME = "mobile";
	private final String DEFAULT_PASSWORD = "mobile";

	/* UI Handles */
	private EditText experimentInput;
	private Spinner rides;
	private TextView rideName;
	private TextView time;
	private TextView values;
	private Button startStop;

	/* Managers and Their Variables */
	public static DataFieldManager dfm;
	public static SensorCompatibility sc;
	private SensorManager mSensorManager;
	private LocationManager mLocationManager;
	private Location loc;
	private Timer recordingTimer;
	private UploadQueue uq;
	private Vibrator vibrator;

	LinkedList<String> acceptedFields;
	Fields f;

	/* Work Flow Variables */
	private boolean isRunning = false;

	/* Recording Constants */
	private final int SAMPLE_INTERVAL = 50;

	/* Recording Variables */
	private String rideNameString = "NOT SET";
	private float rawAccel[];
	private float rawMag[];
	private float accel[];
	private float orientation[];
	private float mag[];
	private String temperature = "";
	private String pressure = "";
	private String light = "";
	private long srate = SAMPLE_INTERVAL;

	private final int MENU_ITEM_SETUP = 0;
	private final int MENU_ITEM_LOGIN = 1;
	private final int MENU_ITEM_UPLOAD = 2;
	private final int MENU_ITEM_TIME = 3;
	private final int MENU_ITEM_MEDIA = 4;

	private final int QUEUE_UPLOAD_REQUESTED = 1;
	private final int EXPERIMENT_CODE = 2;
	private final int CHOOSE_SENSORS_REQUESTED = 3;
	private final int SYNC_TIME_REQUESTED = 4;

	private String dataToBeWrittenToFile;

	private MediaPlayer mMediaPlayer;

	public static ArrayList<File> pictures;
	public static ArrayList<File> videos;

	private int elapsedMinutes = 0;
	private int elapsedSeconds = 0;
	private int elapsedMillis = 0;
	private int totalMillis = 0;
	private int dataPointCount = 0;

	// Used with Sync Time
	private long currentTime = 0;
	private long timeOffset = 0;

	private String dateString, s_elapsedSeconds, s_elapsedMillis,
			s_elapsedMinutes;

	/* Important Objects */
	private API api;
	public static Context mContext;
	private Waffle w;

	DecimalFormat toThou = new DecimalFormat("######0.000");

	String nameOfSession = "";
	static String partialSessionName = "";

	public static boolean inPausedState = false;

	private static boolean useMenu = true;
	private static boolean setupDone = false;
	private static boolean status400 = false;
	private static boolean uploadSuccess = false;

	public static String textToSession = "";
	public static String toSendOut = "";
	private static String stNumber = "1";

	public static JSONArray dataSet;

	public static ArrayList<File> pictureArray = new ArrayList<File>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;

		// Initialize everything you're going to need
		initVars();

		// Main Layout Button for Recording Data
		startStop.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				if (!setupDone || rideName.getText().toString() == null) {

					startStop.setEnabled(false);
					w.make("You must setup before recording data.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);

					// TODO Launch setup

					isRunning = false;
					return isRunning;

				} else {

					// Vibrate and Beep
					vibrator.vibrate(300);
					mMediaPlayer.setLooping(false);
					mMediaPlayer.start();

					// Stop the recording
					if (isRunning) {
						isRunning = false;
						setupDone = false;
						useMenu = true;

						// Unregister sensors to save battery
						mSensorManager.unregisterListener(AmusementPark.this);

						// Update the main button
						enableMainButton(false);

						// Reset main UI
						time.setText(getResources().getString(
								R.string.timeElapsed));
						rideName.setText("Ride/St#: NOT SET");

						// Cancel the recording timer
						recordingTimer.cancel();

						return isRunning;

						// Start the recording
					} else {
						isRunning = true;

						// Check to see if a valid project was chosen. If not,
						// enable all fields for recording.
						SharedPreferences mPrefs = getSharedPreferences(
								"PROJID", MODE_PRIVATE);
						if (mPrefs.getString("project_id", "").equals("-1")) {
							enableAllFields();
						}

						// Create a file so that we can write results to the
						// sdCard
						prepWriteToSDCard(new Date());

						registerSensors();

						dataSet = new JSONArray();
						elapsedMillis = 0;
						totalMillis = 0;

						dataPointCount = 0;

						currentTime = getUploadTime();

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							w.make("Data recording interrupted! Time values may be inconsistent.",
									Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
							e.printStackTrace();
						}

						useMenu = false;

						if (mSensorManager != null) {
							mSensorManager
									.registerListener(
											AmusementPark.this,
											mSensorManager
													.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
											SensorManager.SENSOR_DELAY_FASTEST);
							mSensorManager
									.registerListener(
											AmusementPark.this,
											mSensorManager
													.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
											SensorManager.SENSOR_DELAY_FASTEST);
						}

						dataToBeWrittenToFile = "Accel-X, Accel-Y, Accel-Z, Accel-Total, Mag-X, Mag-Y, Mag-Z, Mag-Total"
								+ "Latitude, Longitude, Time\n";
						startStop.setText(getResources().getString(
								R.string.stopString));
						startStop
								.setBackgroundResource(R.drawable.button_rsense_stop);
						startStop.setTextColor(Color.parseColor("#FFFFFF"));

						recordingTimer = new Timer();
						recordingTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {
								recordData();
							}
						}, 0, srate);
					}

					return isRunning;

				}
			}

		});

	}

	private void enableMainButton(boolean enable) {
		if (enable) {

		} else {
			startStop.setText(getResources().getString(R.string.startString));
			startStop.setBackgroundResource(R.drawable.button_rsense);
			startStop.setTextColor(Color.parseColor("#0066FF"));
		}
	}

	/**
	 * Returns a nicely formatted date.
	 * 
	 * @param date
	 *            Date you wish to convert
	 * @return The date in string form: MM/dd/yyyy, HH:mm:ss
	 */
	String getNiceDateString(Date date) {

		SimpleDateFormat niceFormat = new SimpleDateFormat(
				"MM/dd/yyyy, HH:mm:ss", Locale.US);

		return niceFormat.format(date);
	}

	/**
	 * Prepares a file for writing date to the sdCard.
	 * 
	 * @param date
	 *            Time stamp for the file name
	 * @return Newly created file on sdCard
	 */
	private File prepWriteToSDCard(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss",
				Locale.US);

		dateString = sdf.format(date);

		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}

		String sdFileName = rides.getSelectedItem() + "-" + stNumber + "-"
				+ dateString + ".csv";
		File sdFile = new File(folder, sdFileName);

		return sdFile;

	}

	/**
	 * Attempts to write out a string to a file.
	 * 
	 * @param data
	 *            The data in string form
	 * @param sdFile
	 *            The file to write the data to
	 * @return True if success, false if failed with exception.
	 */
	private boolean writeToSDCard(String data, File sdFile) {
		try {
			// Prepare the writers
			FileWriter gpxwriter = new FileWriter(sdFile);
			BufferedWriter out = new BufferedWriter(gpxwriter);

			// Write out the data
			out.write(data);

			// Close the output stream
			if (out != null)
				out.close();
			if (gpxwriter != null)
				gpxwriter.close();

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public void onPause() {
		super.onPause();

		/* Stop the current sensors to save battery. */
		mLocationManager.removeUpdates(AmusementPark.this);
		mSensorManager.unregisterListener(AmusementPark.this);

		/* Cancel the recording timer */
		if (recordingTimer != null)
			recordingTimer.cancel();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Will call the login dialogue if no user data is found and update UI
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				AmusementPark.mContext,
				AmusementPark.mContext.getSharedPreferences("USER_INFO",
						MODE_PRIVATE));
		if (!(mPrefs.getString("username", "").equals("")))
			login();

		// Rebuilds the upload queue
		if (uq != null)
			uq.buildQueueFromFile();

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!useMenu) {
			menu.getItem(MENU_ITEM_SETUP).setEnabled(false);
			menu.getItem(MENU_ITEM_LOGIN).setEnabled(false);
			menu.getItem(MENU_ITEM_UPLOAD).setEnabled(false);
			menu.getItem(MENU_ITEM_TIME).setEnabled(false);
			menu.getItem(MENU_ITEM_MEDIA).setEnabled(false);
		} else {
			menu.getItem(MENU_ITEM_SETUP).setEnabled(true);
			menu.getItem(MENU_ITEM_LOGIN).setEnabled(true);
			menu.getItem(MENU_ITEM_UPLOAD).setEnabled(true);
			menu.getItem(MENU_ITEM_TIME).setEnabled(true);
			menu.getItem(MENU_ITEM_MEDIA).setEnabled(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SETUP:
			startStop.setEnabled(false);
			return true;
		case MENU_ITEM_LOGIN:
			login();
			return true;
		case MENU_ITEM_UPLOAD:
			manageUploadQueue();
			return true;
		case MENU_ITEM_TIME:
			Intent iTime = new Intent(AmusementPark.this, SyncTime.class);
			startActivityForResult(iTime, SYNC_TIME_REQUESTED);
			return true;
		case MENU_ITEM_MEDIA:
			Intent iMedia = new Intent(AmusementPark.this, MediaManager.class);
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
		DecimalFormat threeDigit = new DecimalFormat("#,##0.000");
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (dfm.enabledFields[Fields.ACCEL_X]
					|| dfm.enabledFields[Fields.ACCEL_Y]
					|| dfm.enabledFields[Fields.ACCEL_Z]
					|| dfm.enabledFields[Fields.ACCEL_TOTAL]) {

				rawAccel = event.values.clone();
				accel[0] = event.values[0];
				accel[1] = event.values[1];
				accel[2] = event.values[2];

				String xPrepend = accel[0] > 0 ? "+" : "";
				String yPrepend = accel[1] > 0 ? "+" : "";
				String zPrepend = accel[2] > 0 ? "+" : "";

				if (!isRunning) {
					values.setText("X: " + xPrepend
							+ threeDigit.format(accel[0]) + "\nY: " + yPrepend
							+ threeDigit.format(accel[1]) + "\nZ: " + zPrepend
							+ threeDigit.format(accel[2]));
				}

				accel[3] = (float) Math.sqrt((float) ((Math.pow(accel[0], 2)
						+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2))));

			}

		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			if (dfm.enabledFields[Fields.MAG_X]
					|| dfm.enabledFields[Fields.MAG_Y]
					|| dfm.enabledFields[Fields.MAG_Z]
					|| dfm.enabledFields[Fields.MAG_TOTAL]
					|| dfm.enabledFields[Fields.HEADING_DEG]
					|| dfm.enabledFields[Fields.HEADING_RAD]) {

				rawMag = event.values.clone();

				mag[0] = event.values[0];
				mag[1] = event.values[1];
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

		if (requestCode == EXPERIMENT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				int eid = data.getExtras().getInt(
						"edu.uml.cs.isense.experiments.exp_id");
				experimentInput.setText("" + eid);
			}
		} else if (requestCode == SYNC_TIME_REQUESTED) {
			if (resultCode == RESULT_OK) {
				timeOffset = data.getExtras().getLong("offset");
				SharedPreferences mPrefs = getSharedPreferences("time_offset",
						0);
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putLong("timeOffset", timeOffset);
				mEditor.commit();
			}
		} else if (requestCode == QUEUE_UPLOAD_REQUESTED) {
			boolean success = uq.buildQueueFromFile();
			if (!success) {
				w.make("Could not re-build queue from file!", Waffle.IMAGE_X);
			}
		} else if (requestCode == CHOOSE_SENSORS_REQUESTED) {
			startStop.setEnabled(true);
			/* TODO ProjectCreate.acceptedFields */
			// acceptedFields = .acceptedFields;
			getEnabledFields();
		}

	}

	// Assists with differentiating between displays for dialogues
	private int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	// Calls the rapi primitives for actual uploading
	private Runnable uploader = new Runnable() {

		@Override
		public void run() {
			status400 = false;
			int sessionId = -1;

			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy, HH:mm:ss",
					Locale.US);
			Date dt = new Date();
			String dateString = sdf.format(dt);

			String description = "Automated Submission Through Android Canobie Physics App";

			SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
			String eid = mPrefs.getString("project_id", "");

			api.createSession(DEFAULT_USERNAME, DEFAULT_PASSWORD);

			// createSession Success Check
			if (sessionId == -1) {
				uploadSuccess = false;
			} else {
				uploadSuccess = true;
			}

			// Experiment Closed Checker
			if (sessionId == -400) {
				status400 = true;
			} else {
				status400 = false;
				if (uploadSuccess)
					// TODO uploadSuccess = api
					// .putSessionData(sessionId, eid, dataSet);

					// Saves data for later upload
					if (!uploadSuccess) {
						QDataSet ds = new QDataSet(QDataSet.Type.DATA,
								nameOfSession + " - " + dateString,
								description, eid, dataSet.toString(), null);
						uq.addDataSetToQueue(ds);
					}

				pictureArray.clear();
			}

		}

	};

	// Control task for uploading data
	private class UploadTask extends AsyncTask<String, Void, String> {

		ProgressDialog dia;

		@Override
		protected String doInBackground(String... strings) {

			uploader.run();

			return strings[0];

		}

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(AmusementPark.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while your data and media are uploaded to iSENSE...");
			dia.setCancelable(false);
			dia.show();

		}

		@Override
		protected void onPostExecute(String sdFileName) {

			dia.setMessage("Done");
			dia.dismiss();

			MediaManager.mediaCount = 0;

			if (status400)
				w.make("Your data cannot be uploaded to this experiment.  It has been closed.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			else if (!uploadSuccess) {
				w.make("Data was not uploaded - saved instead",
						Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
			} else {
				w.make("Upload Success", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
				manageUploadQueue();
			}

			Date date = new Date();
			showSummary(date, sdFileName);

		}
	}

	public void setTime(int seconds) {
		int min = seconds / 60;
		int secInt = seconds % 60;

		String sec = "";
		if (secInt <= 9)
			sec = "0" + secInt;
		else
			sec = "" + secInt;

		time.setText("Time Elapsed: " + min + ":" + sec);
	}

	// Everything needed to be initialized for onCreate
	private void initVars() {

		api = API.getInstance(this);
		api.useDev(true);
		api.createSession(DEFAULT_USERNAME, DEFAULT_PASSWORD);

		// Stores default username and password in Encrypted Shared Preferences
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				AmusementPark.mContext,
				AmusementPark.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		final SharedPreferences.Editor mEdit = mPrefs.edit();
		mEdit.putString("username", DEFAULT_USERNAME);
		mEdit.putString("password", DEFAULT_PASSWORD);
		mEdit.commit();

		uq = new UploadQueue("canobielake", mContext, api);

		pictures = new ArrayList<File>();
		videos = new ArrayList<File>();

		startStop = (Button) findViewById(R.id.startStop);

		values = (TextView) findViewById(R.id.values);
		time = (TextView) findViewById(R.id.time);
		rideName = (TextView) findViewById(R.id.ridename);

		rideName.setText("Ride/St#: " + rideNameString);

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

		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0,
					AmusementPark.this);
		} else {
			// TODO Indicate lack of GPS
		}

		loc = new Location(mLocationManager.getBestProvider(c, true));

		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);

	}

	// Deals with login and UI display
	void login() {
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				AmusementPark.mContext,
				AmusementPark.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		// TODO
		api.createSession(mPrefs.getString("username", ""),
				mPrefs.getString("password", ""));
	}

	// Gets the milliseconds since Epoch
	private long getUploadTime() {
		Calendar c = Calendar.getInstance();
		SharedPreferences mPrefs = getSharedPreferences("time_offset", 0);
		timeOffset = mPrefs.getLong("timeOffset", 0);

		return (((long) c.getTimeInMillis()) + timeOffset);
	}

	// Task for checking sensor availability along with enabling/disabling
	private class SensorCheckTask extends AsyncTask<Void, Integer, Void> {

		ProgressDialog dia;

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(AmusementPark.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Gathering experiment fields...");
			dia.setCancelable(false);
			dia.show();

		}

		@Override
		protected Void doInBackground(Void... voids) {

			SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
			String eidInput = mPrefs.getString("project_id", "");

			dfm = new DataFieldManager(Integer.parseInt(eidInput), api,
					mContext, f);
			dfm.getOrder();

			sc = dfm.checkCompatibility();

			publishProgress(100);
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {
			dia.setMessage("Done");
			dia.cancel();

			Intent i = new Intent(mContext, SensorCompatibility.class);
			startActivityForResult(i, CHOOSE_SENSORS_REQUESTED);

		}
	}

	private void getEnabledFields() {
		for (String s : acceptedFields) {
			if (s.equals(getString(R.string.time)))
				dfm.enabledFields[Fields.TIME] = true;
			if (s.equals(getString(R.string.accel_x)))
				dfm.enabledFields[Fields.ACCEL_X] = true;
			if (s.equals(getString(R.string.accel_y)))
				dfm.enabledFields[Fields.ACCEL_Y] = true;
			if (s.equals(getString(R.string.accel_z)))
				dfm.enabledFields[Fields.ACCEL_Z] = true;
			if (s.equals(getString(R.string.accel_total)))
				dfm.enabledFields[Fields.ACCEL_TOTAL] = true;
			if (s.equals(getString(R.string.latitude)))
				dfm.enabledFields[Fields.LATITUDE] = true;
			if (s.equals(getString(R.string.longitude)))
				dfm.enabledFields[Fields.LONGITUDE] = true;
			if (s.equals(getString(R.string.magnetic_x)))
				dfm.enabledFields[Fields.MAG_X] = true;
			if (s.equals(getString(R.string.magnetic_y)))
				dfm.enabledFields[Fields.MAG_Y] = true;
			if (s.equals(getString(R.string.magnetic_z)))
				dfm.enabledFields[Fields.MAG_Z] = true;
			if (s.equals(getString(R.string.magnetic_total)))
				dfm.enabledFields[Fields.MAG_TOTAL] = true;
			if (s.equals(getString(R.string.heading_deg)))
				dfm.enabledFields[Fields.HEADING_DEG] = true;
			if (s.equals(getString(R.string.heading_rad)))
				dfm.enabledFields[Fields.HEADING_RAD] = true;
			if (s.equals(getString(R.string.temperature_c)))
				dfm.enabledFields[Fields.TEMPERATURE_C] = true;
			if (s.equals(getString(R.string.temperature_f)))
				dfm.enabledFields[Fields.TEMPERATURE_F] = true;
			if (s.equals(getString(R.string.temperature_k)))
				dfm.enabledFields[Fields.TEMPERATURE_K] = true;
			if (s.equals(getString(R.string.pressure)))
				dfm.enabledFields[Fields.PRESSURE] = true;
			if (s.equals(getString(R.string.altitude)))
				dfm.enabledFields[Fields.ALTITUDE] = true;
			if (s.equals(getString(R.string.luminous_flux)))
				dfm.enabledFields[Fields.LIGHT] = true;
		}
	}

	private void enableAllFields() {

		dfm = new DataFieldManager(-1, api, mContext, f);
		dfm.getOrder();
		dfm.enableAllFields();

	}

	// Prompts the user to upload the rest of their content
	// upon successful upload of data
	private void manageUploadQueue() {
		if (!uq.emptyQueue()) {
			Intent i = new Intent().setClass(mContext, QueueLayout.class);
			i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
			startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
		} else {
			w.make("No data to upload.", Waffle.IMAGE_CHECK);
		}
	}

	private void showSummary(Date date, String sdFileName) {

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

		Intent iSummary = new Intent(mContext, Summary.class);
		iSummary.putExtra("millis", s_elapsedMillis)
				.putExtra("seconds", s_elapsedSeconds)
				.putExtra("minutes", s_elapsedMinutes)
				.putExtra("append", "Filename: \n" + sdFileName)
				.putExtra("date", getNiceDateString(date))
				.putExtra("points", "" + dataPointCount);

		startActivity(iSummary);

	}

	// Registers Sensors
	@SuppressLint("InlinedApi")
	private void registerSensors() {
		if (mSensorManager != null && setupDone && dfm != null) {

			if (dfm.enabledFields[Fields.ACCEL_X]
					|| dfm.enabledFields[Fields.ACCEL_Y]
					|| dfm.enabledFields[Fields.ACCEL_Z]
					|| dfm.enabledFields[Fields.ACCEL_TOTAL]) {
				mSensorManager.registerListener(AmusementPark.this,
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
				mSensorManager.registerListener(AmusementPark.this,
						mSensorManager
								.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[Fields.TEMPERATURE_C]
					|| dfm.enabledFields[Fields.TEMPERATURE_F]
					|| dfm.enabledFields[Fields.TEMPERATURE_K]
					|| dfm.enabledFields[Fields.ALTITUDE]) {
				if (getApiLevel() >= 14) {
					mSensorManager
							.registerListener(
									AmusementPark.this,
									mSensorManager
											.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
									SensorManager.SENSOR_DELAY_FASTEST);
				}
			}

			if (dfm.enabledFields[Fields.PRESSURE]
					|| dfm.enabledFields[Fields.ALTITUDE]) {
				mSensorManager.registerListener(AmusementPark.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[Fields.LIGHT]) {
				mSensorManager.registerListener(AmusementPark.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

		}
	}

	private void recordData() {
		dataPointCount++;
		elapsedMillis += srate;
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
			f.angle_rad = ""
					+ (Double.parseDouble(f.angle_deg) * (Math.PI / 180));
		if (dfm.enabledFields[Fields.MAG_X])
			f.mag_x = "" + mag[0];
		if (dfm.enabledFields[Fields.MAG_Y])
			f.mag_y = "" + mag[1];
		if (dfm.enabledFields[Fields.MAG_Z])
			f.mag_z = "" + mag[2];
		if (dfm.enabledFields[Fields.MAG_TOTAL])
			f.mag_total = ""
					+ Math.sqrt(Math.pow(Double.parseDouble(f.mag_x), 2)
							+ Math.pow(Double.parseDouble(f.mag_y), 2)
							+ Math.pow(Double.parseDouble(f.mag_z), 2));
		if (dfm.enabledFields[Fields.TIME])
			f.timeMillis = currentTime + elapsedMillis;
		if (dfm.enabledFields[Fields.TEMPERATURE_C])
			f.temperature_c = temperature;
		if (dfm.enabledFields[Fields.TEMPERATURE_F])
			if (temperature.equals(""))
				f.temperature_f = temperature;
			else
				f.temperature_f = ""
						+ ((Double.parseDouble(temperature) * 1.8) + 32);
		if (dfm.enabledFields[Fields.TEMPERATURE_K])
			if (temperature.equals(""))
				f.temperature_k = temperature;
			else
				f.temperature_k = ""
						+ (Double.parseDouble(temperature) + 273.15);
		if (dfm.enabledFields[Fields.PRESSURE])
			f.pressure = pressure;
		if (dfm.enabledFields[Fields.ALTITUDE])
			f.altitude = "" + loc.getAltitude();
		if (dfm.enabledFields[Fields.LIGHT])
			f.lux = light;

		dataSet.put(dfm.putData());
		dataToBeWrittenToFile = dfm.writeSdCardLine();
	}

}