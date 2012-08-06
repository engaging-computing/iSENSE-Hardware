/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII            General Purpose Data Collector App             SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                                                              SSS                **/
/**           III                    By:               Jeremy Poulin,           SSS               **/
/**           III                                      Michael Stowell           SSSSSSSSS        **/
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.collector.objects.DataFieldManager;
import edu.uml.cs.isense.collector.objects.DataSet;
import edu.uml.cs.isense.collector.objects.Fields;
import edu.uml.cs.isense.collector.objects.SensorCompatibility;
import edu.uml.cs.isense.collector.splash.Splash;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.complexdialogs.ChooseSensorDialog;
import edu.uml.cs.isense.complexdialogs.Description;
import edu.uml.cs.isense.complexdialogs.LoginActivity;
import edu.uml.cs.isense.complexdialogs.MediaManager;
import edu.uml.cs.isense.complexdialogs.Setup;
import edu.uml.cs.isense.objects.Experiment;
import edu.uml.cs.isense.simpledialogs.ForceStop;
import edu.uml.cs.isense.simpledialogs.NoGps;
import edu.uml.cs.isense.simpledialogs.NoIsense;
import edu.uml.cs.isense.simpledialogs.RecordingStopped;
import edu.uml.cs.isense.simpledialogs.Summary;
import edu.uml.cs.isense.simpledialogs.UploadChoice;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.sync.SyncTime;
import edu.uml.cs.isense.waffle.Waffle;

/* Experiment 422 on iSENSE and 277 on Dev */

public class DataCollector extends Activity implements SensorEventListener,
		LocationListener {

	static String TAG = "DataCollector";

	private static TextView session;
	private static TextView time;

	private Button startStop;
	private Boolean running = false;
	private Vibrator vibrator;
	private TextView loginInfo;
	private SensorManager mSensorManager;
	private LocationManager mLocationManager;
	private LocationManager mRoughLocManager;
	private Location loc;
	private Location roughLoc;
	private Timer timeTimer;
	private Timer timeElapsedTimer;
	private Animation rotateInPlace;

	private float rawAccel[];
	private float rawMag[];
	private float accel[];
	private float mag[];
	private float orientation[];
	private String temperature = "";
	private String pressure = "";
	private String light = "";

	private static final int INTERVAL = 200;
	private static long srate = INTERVAL;

	private static final int MENU_ITEM_SETUP = 0;
	private static final int MENU_ITEM_LOGIN = 1;
	private static final int MENU_ITEM_UPLOAD = 2;
	private static final int MENU_ITEM_TIME = 3;
	private static final int MENU_ITEM_MEDIA = 4;

	public static final int DIALOG_CANCELED = 0;
	public static final int DIALOG_OK = 1;
	public static final int DIALOG_PICTURE = 2;

	public static final int SYNC_TIME_REQUESTED = 1;
	public static final int CHOOSE_SENSORS_REQUESTED = 2;
	public static final int QUEUE_UPLOAD_REQUESTED = 3;
	public static final int SETUP_REQUESTED = 4;
	public static final int LOGIN_REQUESTED = 5;
	public static final int UPLOAD_CHOICE_REQUESTED = 6;
	public static final int NO_ISENSE_REQUESTED = 7;
	public static final int NO_GPS_REQUESTED = 8;
	public static final int FORCE_STOP_REQUESTED = 9;
	public static final int RECORDING_STOPPED_REQUESTED = 10;
	public static final int DESCRIPTION_REQUESTED = 11;
	public static final int SPLASH_REQUESTED = 12;

	private static final int TIME = 0;
	private static final int ACCEL_X = 1;
	private static final int ACCEL_Y = 2;
	private static final int ACCEL_Z = 3;
	private static final int ACCEL_TOTAL = 4;
	private static final int LATITUDE = 5;
	private static final int LONGITUDE = 6;
	private static final int MAG_X = 7;
	private static final int MAG_Y = 8;
	private static final int MAG_Z = 9;
	private static final int MAG_TOTAL = 10;
	private static final int HEADING_DEG = 11;
	private static final int HEADING_RAD = 12;
	private static final int TEMPERATURE_C = 13;
	private static final int PRESSURE = 14;
	private static final int ALTITUDE = 15;
	private static final int LIGHT = 16;

	private static final int TEMPERATURE_F = 17;
	private static final int TEMPERATURE_K = 18;

	private String data;

	private MediaPlayer mMediaPlayer;

	private int elapsedMinutes = 0;
	private int elapsedSeconds = 0;
	private int elapsedMillis = 0;
	private int totalMillis = 0;
	private int dataPointCount = 0;
	private int iCount = 0;
	private int len = 0;
	private int len2 = 0;
	private int secondsElapsed = 0;

	private long currentTime = 0;
	private long timeOffset = 0;

	private String dateString, niceDateString, s_elapsedSeconds,
			s_elapsedMillis, s_elapsedMinutes;
	private String sessionDescription = "";

	public static RestAPI rapi;
	static Waffle w;
	public static DataFieldManager dfm;
	Fields f;
	public static SensorCompatibility sc;
	LinkedList<String> acceptedFields;
	public static Queue<DataSet> uploadQueue;

	DecimalFormat toThou = new DecimalFormat("#,###,##0.000");

	ProgressDialog dia;
	double partialProg = 1.0;

	private static String nameOfSession = "";
	public static String partialSessionName = "";

	public static boolean inPausedState = false;

	public static int mwidth = 1;
	private static int rotation = 0;

	private static boolean useMenu = true;
	private static boolean beginWrite = true;
	private static boolean setupDone = false;
	private static boolean choiceViaMenu = false;
	private static boolean successLogin = false;
	private static boolean status400 = false;
	private static boolean sdCardError = false;
	private static boolean uploadSuccess = false;
	private static boolean showGpsDialog = true;
	private static boolean alreadySaved = false;
	
	private static Handler mHandler;
	private boolean throughHandler = false;

	File SDFile;
	FileWriter gpxwriter;
	BufferedWriter out;

	public static String textToSession = "";
	public static String toSendOut = "";
	public static String sdFileName = "";

	public static JSONArray dataSet;
	public static Context mContext;

	public static ArrayList<File> pictureArray = new ArrayList<File>();
	public static ArrayList<File> pictures = new ArrayList<File>();
	public static ArrayList<File> videos = new ArrayList<File>();

	private LinearLayout mScreen;
	private ImageView isenseLogo;

	private SharedPreferences eulaPrefs;
	private String eulaKey;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);

		OrientationManager.disableRotation(DataCollector.this);
		useMenu = false;

		rotateInPlace = AnimationUtils.loadAnimation(this, R.anim.superspinner);
		ImageView spinner = (ImageView) findViewById(R.id.spinner);
		spinner.startAnimation(rotateInPlace);

		// Set main context of application once
		mContext = this;

		new LoadingMainTask().execute();

		// This block useful for if onBackPressed - retains some things from
		// previous session
		if (running) {
			Intent iForceStop = new Intent(mContext, ForceStop.class);
			startActivityForResult(iForceStop, FORCE_STOP_REQUESTED);
		}

	}

	// (s)tarts, (u)pdates, and (f)inishes writing the .csv to the SD Card
	// containing "data"
	public void writeToSDCard(String data, char code) {
		switch (code) {
		case 's':
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
			SimpleDateFormat niceFormat = new SimpleDateFormat(
					"MM/dd/yyyy, HH:mm:ss");
			Date dt = new Date();

			dateString = sdf.format(dt);
			niceDateString = niceFormat.format(dt);

			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/iSENSE");

			if (!folder.exists()) {
				folder.mkdir();
			}

			SDFile = new File(folder, partialSessionName + " - " + dateString
					+ ".csv");
			sdFileName = partialSessionName + " - " + dateString;

			try {
				gpxwriter = new FileWriter(SDFile);
				out = new BufferedWriter(gpxwriter);
				out.write(data);
				beginWrite = false;
			} catch (IOException e) {
				sdCardError = true;
			}

			break;

		case 'u':
			try {
				out.append(data);
			} catch (IOException e) {
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

	@Override
	public void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(DataCollector.this);
		mRoughLocManager.removeUpdates(DataCollector.this);
		mSensorManager.unregisterListener(DataCollector.this);
		if (timeTimer != null)
			timeTimer.cancel();
		if (timeElapsedTimer != null)
			timeElapsedTimer.cancel();
		inPausedState = true;
	}

	@Override
	public void onStop() {
		super.onStop();
		mLocationManager.removeUpdates(DataCollector.this);
		mRoughLocManager.removeUpdates(DataCollector.this);
		mSensorManager.unregisterListener(DataCollector.this);
		if (timeTimer != null)
			timeTimer.cancel();
		if (timeElapsedTimer != null)
			timeElapsedTimer.cancel();
		inPausedState = true;

		// stores uploadQueue in uploadqueue.ser (on SD card) and saves Q_COUNT
		storeQueue();
	}

	@Override
	public void onStart() {
		super.onStart();

		// rebuilds uploadQueue from saved info
		getUploadQueue();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.main);

		// Initialize everything you're going to need
		initVars();

		// Assign everything to respective variables
		assignVars();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (running) {
			Intent iForceStop = new Intent(mContext, ForceStop.class);
			startActivityForResult(iForceStop, FORCE_STOP_REQUESTED);
		}
		// Will call the login dialogue if necessary
		// and update UI
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				DataCollector.mContext,
				DataCollector.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		if (!(mPrefs.getString("username", "").equals("")) && !inPausedState)
			login();

		inPausedState = false;
	}

	// Overridden to prevent user from exiting app unless back button is pressed
	// twice
	@Override
	public void onBackPressed() {
		if (!w.dontToastMeTwice) {
			if (running)
				w.make("Cannot exit via BACK while recording data; use HOME instead.",
						Toast.LENGTH_LONG, "x");
			else
				w.make("Double press \"Back\" to exit.", Toast.LENGTH_SHORT,
						"check");

		} else if (w.exitAppViaBack && !running) {
			setupDone = false;
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ITEM_SETUP, Menu.NONE, "Setup").setIcon(
				R.drawable.ic_menu_settings);
		menu.add(Menu.NONE, MENU_ITEM_LOGIN, Menu.NONE, "Login").setIcon(
				R.drawable.ic_menu_login);
		menu.add(Menu.NONE, MENU_ITEM_UPLOAD, Menu.NONE, "Upload").setIcon(
				R.drawable.ic_menu_upload);
		menu.add(Menu.NONE, MENU_ITEM_TIME, Menu.NONE, "Sync Time").setIcon(
				R.drawable.ic_menu_synctime);
		menu.add(Menu.NONE, MENU_ITEM_MEDIA, Menu.NONE, "Media").setIcon(
				R.drawable.ic_menu_media);
		return true;
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
			new SetupTask().execute();
			return true;
		case MENU_ITEM_LOGIN:
			Intent iLogin = new Intent(mContext, LoginActivity.class);
			startActivityForResult(iLogin, LOGIN_REQUESTED);
			return true;
		case MENU_ITEM_UPLOAD:
			choiceViaMenu = true;
			Intent iUpload = new Intent(mContext, UploadChoice.class);
			startActivityForResult(iUpload, UPLOAD_CHOICE_REQUESTED);

			// Gets the previous unuploaded sessions
			uploadSuccess = true;
			manageUploadQueue();
			return true;
		case MENU_ITEM_TIME:
			Intent iTime = new Intent(DataCollector.this, SyncTime.class);
			startActivityForResult(iTime, SYNC_TIME_REQUESTED);
			return true;
		case MENU_ITEM_MEDIA:
			Intent iMedia = new Intent(DataCollector.this, MediaManager.class);
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
			if (dfm.enabledFields[ACCEL_X] || dfm.enabledFields[ACCEL_Y]
					|| dfm.enabledFields[ACCEL_Z]
					|| dfm.enabledFields[ACCEL_TOTAL]) {

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
				accel[3] = FloatMath.sqrt((float) (Math.pow(accel[0], 2)
						+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2)));
			}

		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			if (dfm.enabledFields[MAG_X] || dfm.enabledFields[MAG_Y]
					|| dfm.enabledFields[MAG_Z] || dfm.enabledFields[MAG_TOTAL]
					|| dfm.enabledFields[HEADING_DEG]
					|| dfm.enabledFields[HEADING_RAD]) {

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
			if (dfm.enabledFields[TEMPERATURE_C]
					|| dfm.enabledFields[TEMPERATURE_F]
					|| dfm.enabledFields[TEMPERATURE_K])
				temperature = toThou.format(event.values[0]);
		} else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
			if (dfm.enabledFields[PRESSURE])
				pressure = toThou.format(event.values[0]);
		} else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			if (dfm.enabledFields[LIGHT])
				light = toThou.format(event.values[0]);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		loc = location;
		roughLoc = location;
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

		} else if (requestCode == CHOOSE_SENSORS_REQUESTED) {
			startStop.setEnabled(true);
			if (resultCode == RESULT_OK) {
				if (ChooseSensorDialog.acceptedFields.isEmpty()) {
					startStop.setEnabled(false);
					new SetupTask().execute();
				} else if (!ChooseSensorDialog.compatible) {
					startStop.setEnabled(false);
					new SetupTask().execute();
				} else {
					acceptedFields = ChooseSensorDialog.acceptedFields;
					getEnabledFields();
				}
			} else if (resultCode == RESULT_CANCELED) {
				setupDone = false;
			}

		} else if (requestCode == SETUP_REQUESTED) {
			if (resultCode == RESULT_OK) {

				setupDone = true;

				partialSessionName = nameOfSession = data
						.getStringExtra("sessionName");

				srate = data.getIntExtra("srate", INTERVAL);

				String showSessionName;
				if (partialSessionName.length() > 15) {
					showSessionName = partialSessionName.substring(0, 15)
							+ "...";
				} else {
					showSessionName = partialSessionName;
				}

				session.setText("Session Name: " + showSessionName);

				new SensorCheckTask().execute();

			} else if (resultCode == RESULT_CANCELED) {
				setupDone = false;
				startStop.setEnabled(true);
			}

		} else if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String returnCode = data.getStringExtra("returnCode");

				if (returnCode.equals("Success")) {
					final SharedPreferences mPrefs = new ObscuredSharedPreferences(
							DataCollector.mContext,
							DataCollector.mContext.getSharedPreferences(
									"USER_INFO", Context.MODE_PRIVATE));
					String loginName = mPrefs.getString("username", "");
					if (loginName.length() >= 18)
						loginName = loginName.substring(0, 18) + "...";
					loginInfo.setText("Username: " + loginName);
					successLogin = true;
					w.make("Login successful", Toast.LENGTH_LONG, "check");
				} else if (returnCode.equals("Failed")) {
					successLogin = false;
					Intent i = new Intent(mContext, LoginActivity.class);
					startActivityForResult(i, LOGIN_REQUESTED);
				} else {
					// Should never get here
				}

			}

		} else if (requestCode == UPLOAD_CHOICE_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (len == 0 || len2 == 0)
					w.make("There is no data to upload!", Toast.LENGTH_LONG,
							"x");
				else {

					SharedPreferences mPrefs = getSharedPreferences("EID", 0);
					String experimentInput = mPrefs.getString("experiment_id",
							"");

					if (successLogin && (experimentInput.length() > 0)) {
						Intent iDescription = new Intent(mContext,
								Description.class);
						startActivityForResult(iDescription,
								DESCRIPTION_REQUESTED);
					} else {
						Intent iNoIsense = new Intent(mContext, NoIsense.class);
						startActivityForResult(iNoIsense, NO_ISENSE_REQUESTED);
					}
				}
			} else if (resultCode == RESULT_CANCELED) {
				if (!choiceViaMenu)
					showSummary();
			}

		} else if (requestCode == NO_ISENSE_REQUESTED) {
			if (!choiceViaMenu)
				showSummary();

		} else if (requestCode == NO_GPS_REQUESTED) {
			showGpsDialog = true;
			if (resultCode == RESULT_OK) {
				startActivity(new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}

		} else if (requestCode == FORCE_STOP_REQUESTED) {
			startStop.performLongClick();

		} else if (requestCode == RECORDING_STOPPED_REQUESTED) {
			Intent iDescription = new Intent(mContext, Description.class);
			startActivityForResult(iDescription, DESCRIPTION_REQUESTED);

		} else if (requestCode == DESCRIPTION_REQUESTED) {
			if (resultCode == RESULT_OK) {
				sessionDescription = data.getStringExtra("description");

				SharedPreferences mPrefs = getSharedPreferences("EID", 0);
				String experimentInput = mPrefs.getString("experiment_id", "");

				if ((experimentInput.length() >= 0) && successLogin) {
					new UploadTask().execute();
				} else if ((experimentInput.length() >= 0) && !successLogin) {
					Intent iNoIsense = new Intent(mContext, NoIsense.class);
					startActivityForResult(iNoIsense, NO_ISENSE_REQUESTED);
					if (!alreadySaved) saveOnUploadQueue();
				} else {
					Intent iNoIsense = new Intent(mContext, NoIsense.class);
					startActivityForResult(iNoIsense, NO_ISENSE_REQUESTED);
				}
			} else if (resultCode == RESULT_CANCELED) {
				w.make("Data not uploaded.", Toast.LENGTH_SHORT, "x");
				showSummary();
			}

		} else if (requestCode == SPLASH_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences.Editor editor = eulaPrefs.edit();
				editor.putBoolean(eulaKey, true);
				editor.commit();
			} else {
				((Activity) mContext).finish();
			}

		} else if (requestCode == QUEUE_UPLOAD_REQUESTED) {
			if (resultCode == RESULT_OK) {
				getUploadQueue();
			}
		}


	}

	// Saves dataSet on queue when you aren't logged in
	private void saveOnUploadQueue() {
		// Session Id
		int sessionId = -1;

		// Date
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy, HH:mm:ss");
		Date dt = new Date();
		String dateString = sdf.format(dt);

		// Location
		List<Address> address = null;
		String city = "", state = "", country = "", addr = "";

		try {
			if (roughLoc != null) {
				address = new Geocoder(DataCollector.this, Locale.getDefault())
						.getFromLocation(roughLoc.getLatitude(),
								roughLoc.getLongitude(), 1);
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

		// Session Description
		String description;
		if (sessionDescription.equals(""))
			description = "Automated Submission Through Android Data Collection App";
		else
			description = sessionDescription;

		// Experiment Id
		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		String eid = mPrefs.getString("experiment_id", "");

		// Chucks all the info into the queue
		DataSet ds = new DataSet(DataSet.Type.DATA, nameOfSession + " - "
				+ dateString, description, eid, dataSet.toString(), null,
				sessionId, city, state, country, addr);
		uploadQueue.add(ds);

		// Saves pictures for later upload
		int pic = pictureArray.size();
		while (pic > 0) {
			DataSet dsPic = new DataSet(DataSet.Type.PIC, nameOfSession + " - "
					+ dateString, description, eid, null,
					pictureArray.get(pic - 1), sessionId, city, state, country,
					addr);
			uploadQueue.add(dsPic);
			pic--;
		}
		
		alreadySaved = true;
	}

	// Calls the rapi primitives for actual uploading
	private Runnable uploader = new Runnable() {

		@Override
		public void run() {
			status400 = false;
			int sessionId = -1;
			String city = "", state = "", country = "", addr = "";
			List<Address> address = null;

			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy, HH:mm:ss");
			Date dt = new Date();
			String dateString = sdf.format(dt);

			try {
				if (roughLoc != null) {
					address = new Geocoder(DataCollector.this,
							Locale.getDefault()).getFromLocation(
							roughLoc.getLatitude(), roughLoc.getLongitude(), 1);
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

			String description;
			if (sessionDescription.equals(""))
				description = "Automated Submission Through Android Data Collection App";
			else
				description = sessionDescription;

			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String eid = mPrefs.getString("experiment_id", "");

			if (address == null || address.size() <= 0) {
				sessionId = rapi.createSession(eid, nameOfSession, description,
						"N/A", "N/A", "United States");
			} else {
				sessionId = rapi.createSession(eid, nameOfSession, description,
						addr, city + ", " + state, country);
			}

			sessionDescription = "";

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
					uploadSuccess = rapi
							.putSessionData(sessionId, eid, dataSet);

				// Saves data for later upload
				if (!uploadSuccess) {
					DataSet ds = new DataSet(DataSet.Type.DATA, nameOfSession
							+ " - " + dateString, description, eid,
							dataSet.toString(), null, sessionId, city, state,
							country, addr);
					uploadQueue.add(ds);
				}

				int pic = pictureArray.size();

				while (pic > 0) {
					boolean picSuccess = rapi.uploadPictureToSession(
							pictureArray.get(pic - 1), eid, sessionId,
							nameOfSession, description);

					// Saves pictures for later upload
					if (!picSuccess) {
						DataSet ds = new DataSet(DataSet.Type.PIC,
								nameOfSession + " - " + dateString,
								description, eid, null,
								pictureArray.get(pic - 1), sessionId, city,
								state, country, addr);
						uploadQueue.add(ds);
					}
					pic--;
				}

				pictureArray.clear();
			}

		}

	};

	// Control task for uploading data
	private class UploadTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			OrientationManager.disableRotation(DataCollector.this);
			dia = new ProgressDialog(DataCollector.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while your data are uploaded to iSENSE...");
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

			len = 0;
			len2 = 0;
			MediaManager.mediaCount = 0;
			session.setText(getString(R.string.session));
			nameOfSession = "";

			showSummary();

			if (status400)
				w.make("Your data cannot be uploaded to this experiment.  It has been closed.",
						Toast.LENGTH_LONG, "x");
			else if (!uploadSuccess) {
				w.make("An error occured during upload.  Please check internet connectivity.",
						Toast.LENGTH_LONG, "x");
			} else {
				w.make("Upload Success", Toast.LENGTH_SHORT, "check");
				manageUploadQueue();
			}

		}

	}

	// Control task for uploading data
	private class SetupTask extends AsyncTask<Void, Integer, Void> {

		Intent iSetup;

		@Override
		protected void onPreExecute() {
			OrientationManager.disableRotation(DataCollector.this);
			dia = new ProgressDialog(DataCollector.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Loading experiment data...");
			dia.setCancelable(false);
			dia.show();

		}

		@Override
		protected Void doInBackground(Void... voids) {

			SharedPreferences mPrefs = getSharedPreferences("EID", 0);

			String eid = mPrefs.getString("experiment_id", "-1");
			Experiment e = rapi.getExperiment(Integer.parseInt(eid));

			iSetup = new Intent(DataCollector.this, Setup.class);

			if (e != null) {
				iSetup.putExtra("experiment_id", "" + e.experiment_id);
				iSetup.putExtra("srate", "" + e.srate);
			} else {
				iSetup.putExtra("experiment_id", "");
				iSetup.putExtra("srate", "");
			}
			publishProgress(100);
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {
			dia.setMessage("Done");
			dia.cancel();

			OrientationManager.enableRotation(DataCollector.this);

			startActivityForResult(iSetup, SETUP_REQUESTED);

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

	// Takes care of everything to do with EULA
	private void displaySplash() {

		PackageInfo versionInfo = getPackageInfo();

		// The eulaKey changes every time you increment the version number in
		// the AndroidManifest.xml
		eulaKey = "eula_" + versionInfo.versionCode;

		if (eulaPrefs == null)
			eulaPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		boolean hasBeenShown = eulaPrefs.getBoolean(eulaKey, false);

		if (hasBeenShown == false) {
			Intent iSplash = new Intent(mContext, Splash.class);
			startActivityForResult(iSplash, SPLASH_REQUESTED);
		}

	}

	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}

	// Deals with login and UI display
	void login() {
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				DataCollector.mContext,
				DataCollector.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		boolean success = rapi.login(mPrefs.getString("username", ""),
				mPrefs.getString("password", ""));
		if (success) {
			String loginName = mPrefs.getString("username", "");
			if (loginName.length() >= 18)
				loginName = loginName.substring(0, 18) + "...";
			loginInfo.setText("Username: " + loginName);
			// loginInfo.setTextColor(Color.GREEN);
			successLogin = true;
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
	private void registerSensors() {
		if (mSensorManager != null && setupDone && dfm != null) {

			if (dfm.enabledFields[ACCEL_X] || dfm.enabledFields[ACCEL_Y]
					|| dfm.enabledFields[ACCEL_Z]
					|| dfm.enabledFields[ACCEL_TOTAL]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager
								.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[MAG_X] || dfm.enabledFields[MAG_Y]
					|| dfm.enabledFields[MAG_Z] || dfm.enabledFields[MAG_TOTAL]
					|| dfm.enabledFields[HEADING_DEG]
					|| dfm.enabledFields[HEADING_RAD]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager
								.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[TEMPERATURE_C]
					|| dfm.enabledFields[TEMPERATURE_F]
					|| dfm.enabledFields[TEMPERATURE_K]
					|| dfm.enabledFields[ALTITUDE]) {
				mSensorManager
						.registerListener(
								DataCollector.this,
								mSensorManager
										.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
								SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[PRESSURE] || dfm.enabledFields[ALTITUDE]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[LIGHT]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

		}
	}

	// Calculates Altitude from Temperature and Pressure if Possible
	private String calcAltitude() {
		if ((dfm.enabledFields[TEMPERATURE_C]
				|| dfm.enabledFields[TEMPERATURE_F] || dfm.enabledFields[TEMPERATURE_K])
				&& dfm.enabledFields[PRESSURE]) {
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

	// Task for checking sensor availability along with enabling/disabling
	private class SensorCheckTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			OrientationManager.disableRotation(DataCollector.this);

			dia = new ProgressDialog(DataCollector.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Gathering experiment fields...");
			dia.setCancelable(false);
			dia.show();

		}

		@Override
		protected Void doInBackground(Void... voids) {

			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String experimentInput = mPrefs.getString("experiment_id", "");

			dfm = new DataFieldManager(Integer.parseInt(experimentInput), rapi,
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

			OrientationManager.enableRotation(DataCollector.this);

			Intent i = new Intent(mContext, ChooseSensorDialog.class);
			startActivityForResult(i, CHOOSE_SENSORS_REQUESTED);

		}
	}

	private void getEnabledFields() {
		for (String s : acceptedFields) {
			if (s.equals(getString(R.string.time)))
				dfm.enabledFields[TIME] = true;
			if (s.equals(getString(R.string.accel_x)))
				dfm.enabledFields[ACCEL_X] = true;
			if (s.equals(getString(R.string.accel_y)))
				dfm.enabledFields[ACCEL_Y] = true;
			if (s.equals(getString(R.string.accel_z)))
				dfm.enabledFields[ACCEL_Z] = true;
			if (s.equals(getString(R.string.accel_total)))
				dfm.enabledFields[ACCEL_TOTAL] = true;
			if (s.equals(getString(R.string.latitude)))
				dfm.enabledFields[LATITUDE] = true;
			if (s.equals(getString(R.string.longitude)))
				dfm.enabledFields[LONGITUDE] = true;
			if (s.equals(getString(R.string.magnetic_x)))
				dfm.enabledFields[MAG_X] = true;
			if (s.equals(getString(R.string.magnetic_y)))
				dfm.enabledFields[MAG_Y] = true;
			if (s.equals(getString(R.string.magnetic_z)))
				dfm.enabledFields[MAG_Z] = true;
			if (s.equals(getString(R.string.magnetic_total)))
				dfm.enabledFields[MAG_TOTAL] = true;
			if (s.equals(getString(R.string.heading_deg)))
				dfm.enabledFields[HEADING_DEG] = true;
			if (s.equals(getString(R.string.heading_rad)))
				dfm.enabledFields[HEADING_RAD] = true;
			if (s.equals(getString(R.string.temperature_c)))
				dfm.enabledFields[TEMPERATURE_C] = true;
			if (s.equals(getString(R.string.temperature_f)))
				dfm.enabledFields[TEMPERATURE_F] = true;
			if (s.equals(getString(R.string.temperature_k)))
				dfm.enabledFields[TEMPERATURE_K] = true;
			if (s.equals(getString(R.string.pressure)))
				dfm.enabledFields[PRESSURE] = true;
			if (s.equals(getString(R.string.altitude)))
				dfm.enabledFields[ALTITUDE] = true;
			if (s.equals(getString(R.string.luminous_flux)))
				dfm.enabledFields[LIGHT] = true;
		}
	}

	// Prompts the user to upload the rest of their content
	// upon successful upload of data
	private void manageUploadQueue() {
		if (uploadSuccess) {
			if (!uploadQueue.isEmpty()) {
				Intent i = new Intent().setClass(mContext, QueueUploader.class);
				startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
			} else
				Log.w("uploadQueue", "empty queue");
		}
	}

	// Everything needed to be initialized for onCreate
	@SuppressWarnings("deprecation")
	private void initVars() {

		mScreen = (LinearLayout) findViewById(R.id.mainScreen);
		isenseLogo = (ImageView) findViewById(R.id.ImageViewLogo);

		Display deviceDisplay = getWindowManager().getDefaultDisplay();
		mwidth = deviceDisplay.getWidth();

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());

		startStop = (Button) findViewById(R.id.startStop);

		session = (TextView) findViewById(R.id.sessionName);

		time = (TextView) findViewById(R.id.time);

		loginInfo = (TextView) findViewById(R.id.loginInfo);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mRoughLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

	// Everything that needs to be assigned in onCreate()
	private void assignVars() {
		// Set all the login info
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				DataCollector.mContext,
				DataCollector.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		String loginName = mPrefs.getString("username", "");
		if (loginName.equals(""))
			loginInfo.setText(R.string.notLoggedIn);
		else {
			loginInfo.setText("Username: " + loginName);
		}

		if (!(mPrefs.getString("username", "").equals("")))
			login();

		// Set session name
		if (nameOfSession.equals(""))
			session.setText(getString(R.string.session));
		else
			session.setText("Session Name: " + nameOfSession);

		// Colorize the startStop button and add the huge listener
		startStop.getBackground().setColorFilter(0xFFFF0000,
				PorterDuff.Mode.MULTIPLY);
		setStartStopListener();
	}

	public void initLocations() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				&& mRoughLocManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0,
					DataCollector.this);
			mRoughLocManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, DataCollector.this);
		} else {
			if (showGpsDialog) {
				Intent iNoGps = new Intent(mContext, NoGps.class);
				startActivityForResult(iNoGps, NO_GPS_REQUESTED);
				showGpsDialog = false;
			}
		}

		loc = new Location(mLocationManager.getBestProvider(c, true));
	}

	public void setStartStopListener() {
		startStop.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				if (!setupDone) {

					w.make("You must setup before recording data.",
							Toast.LENGTH_LONG, "x");

					startStop.setEnabled(false);
					new SetupTask().execute();

				} else {

					vibrator.vibrate(300);
					mMediaPlayer.setLooping(false);
					mMediaPlayer.start();

					if (running) {
						OrientationManager.enableRotation((Activity) mContext);

						writeToSDCard(null, 'f');
						setupDone = false;
						useMenu = true;
						alreadySaved = false;

						mSensorManager.unregisterListener(DataCollector.this);
						running = false;
						startStop.setText(R.string.startString);
						time.setText(R.string.timeElapsed);

						timeTimer.cancel();
						timeElapsedTimer.cancel();

						startStop.getBackground().setColorFilter(0xFFFF0000,
								PorterDuff.Mode.MULTIPLY);
						mScreen.setBackgroundResource(R.drawable.background);
						isenseLogo.setImageResource(R.drawable.logo_red);

						choiceViaMenu = false;

						if (sdCardError)
							w.make("Could not write file to SD Card.",
									Toast.LENGTH_SHORT, "x");

						if (throughHandler) {
							Intent iRecordingStopped = new Intent(mContext,
									RecordingStopped.class);
							startActivityForResult(iRecordingStopped,
									RECORDING_STOPPED_REQUESTED);
						} else {
							Intent iDescription = new Intent(mContext,
									Description.class);
							startActivityForResult(iDescription,
									DESCRIPTION_REQUESTED);
						}

					} else {

						registerSensors();
						OrientationManager.disableRotation((Activity) mContext);

						rotation = getRotation(mContext);
						dataSet = new JSONArray();
						secondsElapsed = 0;
						elapsedMillis = 0;
						totalMillis = 0;
						len = 0;
						len2 = 0;
						dataPointCount = 0;
						iCount = 0;
						beginWrite = true;
						sdCardError = false;

						currentTime = getUploadTime();

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							w.make("Data recording interrupted! Time values may be inconsistent.",
									Toast.LENGTH_SHORT, "x");
							e.printStackTrace();
						}

						useMenu = false;

						data = "X Acceleration, Y Acceleration, Z Acceleration, Acceleration, "
								+ "Latitude, Longitude, Heading, Magnetic X, Magnetic Y, Magnetic Z, Time\n";
						running = true;
						startStop.setText(R.string.stopString);

						timeElapsedTimer = new Timer();
						timeElapsedTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										setTime(secondsElapsed++);
									}
								});
							}
						}, 0, 1000);

						timeTimer = new Timer();
						timeTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {

								dataPointCount++;
								elapsedMillis += srate;
								totalMillis = elapsedMillis;

								if (iCount >= 3000) {

									timeTimer.cancel();
									dataPointCount--;

									mHandler.post(new Runnable() {
										@Override
										public void run() {
											throughHandler = true;
											startStop.performLongClick();
										}
									});

								} else {

									iCount++;
									len++;
									len2++;

									if (dfm.enabledFields[ACCEL_X])
										f.accel_x = toThou.format(accel[0]);
									if (dfm.enabledFields[ACCEL_Y])
										f.accel_y = toThou.format(accel[1]);
									if (dfm.enabledFields[ACCEL_Z])
										f.accel_z = toThou.format(accel[2]);
									if (dfm.enabledFields[ACCEL_TOTAL])
										f.accel_total = toThou.format(accel[3]);
									if (dfm.enabledFields[LATITUDE])
										f.latitude = loc.getLatitude();
									if (dfm.enabledFields[LONGITUDE])
										f.longitude = loc.getLongitude();
									if (dfm.enabledFields[HEADING_DEG])
										f.angle_deg = toThou
												.format(orientation[0]);
									if (dfm.enabledFields[HEADING_RAD])
										f.angle_rad = ""
												+ (Double
														.parseDouble(f.angle_deg) * (Math.PI / 180));
									if (dfm.enabledFields[MAG_X])
										f.mag_x = mag[0];
									if (dfm.enabledFields[MAG_Y])
										f.mag_y = mag[1];
									if (dfm.enabledFields[MAG_Z])
										f.mag_z = mag[2];
									if (dfm.enabledFields[MAG_TOTAL])
										f.mag_total = Math.sqrt(Math.pow(
												f.mag_x, 2)
												+ Math.pow(f.mag_y, 2)
												+ Math.pow(f.mag_z, 2));
									if (dfm.enabledFields[TIME])
										f.timeMillis = currentTime
												+ elapsedMillis;
									if (dfm.enabledFields[TEMPERATURE_C])
										f.temperature_c = temperature;
									if (dfm.enabledFields[TEMPERATURE_F])
										f.temperature_f = ""
												+ ((Double
														.parseDouble(temperature) * 1.8) + 32);
									if (dfm.enabledFields[TEMPERATURE_K])
										f.temperature_k = ""
												+ (Double
														.parseDouble(temperature) + 273.15);
									if (dfm.enabledFields[PRESSURE])
										f.pressure = pressure;
									if (dfm.enabledFields[ALTITUDE])
										f.altitude = calcAltitude();
									if (dfm.enabledFields[LIGHT])
										f.lux = light;

									dataSet.put(dfm.putData());
									data = dfm.writeSdCardLine();

									if (beginWrite) {
										writeToSDCard(data, 's');
									} else {
										writeToSDCard(data, 'u');
									}

								}

							}
						}, 0, srate);
						startStop.getBackground().setColorFilter(0xFF00FF00,
								PorterDuff.Mode.MULTIPLY);
						mScreen.setBackgroundResource(R.drawable.background_running);
						isenseLogo.setImageResource(R.drawable.logo_green);
					}
					return running;

				}
				running = false;
				return running;
			}

		});
	}

	// get shared Q_COUNT
	public static SharedPreferences getSharedPreferences(Context ctxt) {
		return ctxt.getSharedPreferences("Q_COUNT", MODE_PRIVATE);
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
		else
			appendMe = "Filename: \n" + sdFileName;

		Intent iSummary = new Intent(mContext, Summary.class);
		iSummary.putExtra("millis", s_elapsedMillis)
				.putExtra("seconds", s_elapsedSeconds)
				.putExtra("minutes", s_elapsedMinutes)
				.putExtra("append", appendMe).putExtra("date", niceDateString)
				.putExtra("points", "" + dataPointCount);

		startActivity(iSummary);

	}

	// Rebuilds uploadQueue from Q_COUNT and uploadqueue.ser
	public static void getUploadQueue() {

		uploadQueue = new LinkedList<DataSet>();

		// Makes sure there is an iSENSE folder
		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");
		if (!folder.exists())
			folder.mkdir();

		// Gets Q_COUNT back from Shared Prefs
		final SharedPreferences mPrefs = getSharedPreferences(mContext);
		int Q_COUNT = mPrefs.getInt("Q_COUNT", 0);

		try {
			// Deserialize the file as a whole
			File file = new File(folder.getAbsolutePath() + "/uploadqueue.ser");
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					file));
			// Deserialize the objects one by one
			for (int i = 0; i < Q_COUNT; i++) {
				DataSet dataSet = (DataSet) in.readObject();
				uploadQueue.add(dataSet);
			}
			in.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Saves Q_COUNT and uploadQueue into memory for later use
	public static void storeQueue() {

		// Save Q_COUNT in SharedPrefs
		final SharedPreferences mPrefs = getSharedPreferences(mContext);
		final SharedPreferences.Editor mPrefsEditor = mPrefs.edit();
		int Q_COUNT = uploadQueue.size();
		mPrefsEditor.putInt("Q_COUNT", Q_COUNT);
		mPrefsEditor.commit();

		// writes uploadqueue.ser
		File uploadQueueFile = new File(
				Environment.getExternalStorageDirectory() + "/iSENSE"
						+ "/uploadqueue.ser");
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(uploadQueueFile));

			// Serializes DataSets from uploadQueue into uploadqueue.ser
			while (Q_COUNT > 0) {
				DataSet ds = uploadQueue.remove();
				out.writeObject(ds);
				Q_COUNT--;
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Loads the main screen
	private class LoadingMainTask extends AsyncTask<Void, Integer, Void> {
		Runnable loadingScreen;

		@Override
		protected void onPreExecute() {
			inPausedState = true;
			mHandler = new Handler();
			loadingScreen = new Runnable() {

				@Override
				public void run() {

					// Display the Splash Screen
					displaySplash();

					setContentView(R.layout.main);

					// Initialize everything you're going to need
					initVars();

					// Assign everything to respective variables
					assignVars();

				}
			};
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mHandler.post(loadingScreen);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			inPausedState = false;
			OrientationManager.enableRotation(DataCollector.this);
			useMenu = true;
			super.onPostExecute(result);
		}

	}

}