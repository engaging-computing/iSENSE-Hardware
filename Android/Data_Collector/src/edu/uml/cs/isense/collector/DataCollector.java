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
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.dialogs.CanLogin;
import edu.uml.cs.isense.collector.dialogs.CanRecord;
import edu.uml.cs.isense.collector.dialogs.ChooseSensorDialog;
import edu.uml.cs.isense.collector.dialogs.Description;
import edu.uml.cs.isense.collector.dialogs.ForceStop;
import edu.uml.cs.isense.collector.dialogs.LoginActivity;
import edu.uml.cs.isense.collector.dialogs.MediaManager;
import edu.uml.cs.isense.collector.dialogs.NeedConnectivity;
import edu.uml.cs.isense.collector.dialogs.NoGps;
import edu.uml.cs.isense.collector.dialogs.NoIsense;
import edu.uml.cs.isense.collector.dialogs.Summary;
import edu.uml.cs.isense.collector.dialogs.UploadFailSave;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.dfm.SensorCompatibility;
import edu.uml.cs.isense.collector.sync.SyncTime;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.exp.Setup;
import edu.uml.cs.isense.objects.Experiment;
import edu.uml.cs.isense.queue.DataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class DataCollector extends Activity implements SensorEventListener,
		LocationListener {

	/* Constants */

	public static final String activityName = "datacollector";

	// Numerical constants
	static final int INTERVAL = 50;
	static final int TEST_LENGTH = 600;
	private static long srate = INTERVAL;
	private static int recLength = TEST_LENGTH;

	private static final int MENU_ITEM_SETUP = 0;
	private static final int MENU_ITEM_UPLOAD = 1;
	private static final int MENU_ITEM_LOGIN = 2;
	private static final int MENU_ITEM_MEDIA = 3;
	private static final int MENU_ITEM_SYNC = 4;

	public static final int DIALOG_CANCELED = 0;
	public static final int DIALOG_OK = 1;
	public static final int DIALOG_PICTURE = 2;

	public static final int SYNC_TIME_REQUESTED = 1;
	public static final int CHOOSE_SENSORS_REQUESTED = 2;
	public static final int QUEUE_UPLOAD_REQUESTED = 3;
	public static final int SETUP_REQUESTED = 4;
	public static final int LOGIN_REQUESTED = 5;
	public static final int NO_ISENSE_REQUESTED = 6;
	public static final int GPS_REQUESTED = 7;
	public static final int FORCE_STOP_REQUESTED = 8;
	public static final int RECORDING_STOPPED_REQUESTED = 9;
	public static final int DESCRIPTION_REQUESTED = 10;
	public static final int CAN_LOGIN_REQUESTED = 11;
	
	/* UI Objects */

	// TextView
	private static TextView time;

	// EditTexts
	private static EditText sessionName;
	private static EditText sampleInterval;
	private static EditText recordingLength;

	// Buttons
	private static Button startStop;

	// Menu Items
	private Menu mMenu;

	private MenuItem menuSetup;
	private MenuItem menuLogin;
	private MenuItem menuUpload;
	private MenuItem menuSync;
	private MenuItem menuMedia;

	// ProgressDialogs
	private ProgressDialog dia;

	// LinearLayouts
	private LinearLayout mScreen;

	// ImageViews
	private ImageView isenseLogo;


	/* Formatters */

	private final static DecimalFormat toThou = new DecimalFormat("#,###,##0.000");


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
	private static boolean performExpNumCheckOnReturn = false;

	// Strings
	public static String textToSession = "";
	public static String toSendOut = "";
	public static String sdFileName = "";
	public static String nameOfSession = "";


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
	private String sessionDescription = "";

	// Booleans
	private static boolean useMenu = false;
	private static boolean preLoad = false;
	private static boolean beginWrite = true;
	private static boolean choiceViaMenu = false;
	private static boolean status400 = false;
	private static boolean sdCardError = false;
	private static boolean uploadSuccess = false;
	private static boolean showGpsDialog = true;
	private static boolean throughUploadMenuItem = false;


	/* Additional Objects */

	// Built-In
	private Animation rotateInPlace;
	private static Handler mHandler;

	private static File SDFile;
	private static FileWriter gpxwriter;
	private static BufferedWriter out;

	public static JSONArray dataSet;

	public static Context mContext;

	// Custom
	public static RestAPI rapi;
	public static Waffle w;
	public static DataFieldManager dfm;
	public static Fields f;
	public static SensorCompatibility sc;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);

		OrientationManager.disableRotation(DataCollector.this);

		rotateInPlace = AnimationUtils.loadAnimation(this, R.anim.superspinner);
		ImageView spinner = (ImageView) findViewById(R.id.spinner);
		spinner.startAnimation(rotateInPlace);

		// Set main context of application once
		mContext = this;

		new LoadingMainTask().execute();

	}

	// (s)tarts, (u)pdates, and (f)inishes writing the .csv to the SD Card
	// containing "data"
	public static void writeToSDCard(String data, char code) {
		switch (code) {
		case 's':
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss", Locale.US);
			Date dt = new Date();
			String csvDateString = sdf.format(dt);

			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/iSENSE");

			if (!folder.exists()) {
				folder.mkdir();
			}

			SDFile = new File(folder, nameOfSession + "--" + csvDateString
					+ ".csv");
			sdFileName = nameOfSession + " - " + csvDateString;

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
		setContentView(R.layout.data_collector);

		// Initialize everything you're going to need
		initVars();
		initMainUI();

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

		if (uq != null)
			uq.buildQueueFromFile();
		
		// keeps menu buttons disabled while running
		if (running)
			setMenuStatus(false);
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

		menuSetup = menu.getItem(MENU_ITEM_SETUP);
		menuUpload = menu.getItem(MENU_ITEM_UPLOAD);
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
			menu.getItem(3).setEnabled(false);
			menu.getItem(4).setEnabled(false);

		} else {

			menu.getItem(0).setEnabled(true);
			menu.getItem(1).setEnabled(true);
			menu.getItem(2).setEnabled(true);
			menu.getItem(3).setEnabled(true);
			menu.getItem(4).setEnabled(true);

		}
		return true;
	}

	// Handle all menu item selections
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_setup:
			startStop.setEnabled(false);
			if (!rapi.isConnectedToInternet()) {
				w.make("No internet connectivity - searching only cached experiments", 
						Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
			}
			Intent iSetup = new Intent(DataCollector.this, Setup.class);
			iSetup.putExtra("enable_no_exp_button", true);
			startActivityForResult(iSetup, SETUP_REQUESTED);
			return true;
		case R.id.menu_item_login:
			Intent iLogin = new Intent(mContext, LoginActivity.class);
			startActivityForResult(iLogin, LOGIN_REQUESTED);
			return true;
		case R.id.menu_item_upload:
			choiceViaMenu = true;
			uploadSuccess = true;
			throughUploadMenuItem = true;
			manageUploadQueue();
			return true;
		case R.id.menu_item_sync:
			Intent iTime = new Intent(DataCollector.this, SyncTime.class);
			startActivityForResult(iTime, SYNC_TIME_REQUESTED);
			return true;
		case R.id.menu_item_media:
			Intent iMedia = new Intent(DataCollector.this, MediaManager.class);
			iMedia.putExtra("sessionName", sessionName.getText().toString());
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
			if (dfm.enabledFields[Fields.ACCEL_X] || dfm.enabledFields[Fields.ACCEL_Y]
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
			if (dfm.enabledFields[Fields.MAG_X] || dfm.enabledFields[Fields.MAG_Y]
					|| dfm.enabledFields[Fields.MAG_Z] || dfm.enabledFields[Fields.MAG_TOTAL]
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

		} else if (requestCode == CHOOSE_SENSORS_REQUESTED) {
			startStop.setEnabled(true);
			if (resultCode == RESULT_OK) {
				if (ChooseSensorDialog.acceptedFields.isEmpty()) {
					startStop.setEnabled(false);
					Intent iSetup = new Intent(DataCollector.this, Setup.class);
					iSetup.putExtra("enable_no_exp_button", true);
					startActivityForResult(iSetup, SETUP_REQUESTED);
				} else if (!ChooseSensorDialog.compatible) {
					startStop.setEnabled(false);
					Intent iSetup = new Intent(DataCollector.this, Setup.class);
					iSetup.putExtra("enable_no_exp_button", true);
					startActivityForResult(iSetup, SETUP_REQUESTED);
				} else {
					acceptedFields = ChooseSensorDialog.acceptedFields;
					getEnabledFields();
				}
			}

		} else if (requestCode == SETUP_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (data != null) {
					boolean noExp = data.getBooleanExtra("no_exp", false);
					if (noExp == true) {
						setUpDFMWithAllFields();
						startStop.setEnabled(true);
					} else {
						new SensorCheckTask().execute();
					}
				} else {
					new SensorCheckTask().execute();
				}

			} else if (resultCode == RESULT_CANCELED) {

				startStop.setEnabled(true);
			}

		} else if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String returnCode = data.getStringExtra("returnCode");

				if (returnCode.equals("Success")) {

					w.make("Login successful", Waffle.LENGTH_LONG,
							Waffle.IMAGE_CHECK);
				} else if (returnCode.equals("Failed")) {

					Intent i = new Intent(mContext, LoginActivity.class);
					startActivityForResult(i, LOGIN_REQUESTED);
				} else {
					// should never get here
				}
			}
			
			if (performExpNumCheckOnReturn) {
				performExpNumCheckOnReturn = false;
				checkExpNumCredentials();
			}

		} else if (requestCode == NO_ISENSE_REQUESTED) {
			if (!choiceViaMenu)
				showSummary();

		} else if (requestCode == GPS_REQUESTED) {
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

				if ((experimentInput.length() >= 0) && rapi.isLoggedIn()) {
					nameOfSession = sessionName.getText().toString();
					new UploadTask().execute();
				} else if ((experimentInput.length() >= 0)
						&& !rapi.isLoggedIn()) {

					w.make("Not logged in - saving data instead",
							Waffle.IMAGE_WARN);
					new UploadTask().execute();
				} else {
					Intent iNoIsense = new Intent(mContext, NoIsense.class);
					startActivityForResult(iNoIsense, NO_ISENSE_REQUESTED);
				}
			} else if (resultCode == RESULT_CANCELED) {
				nameOfSession = sessionName.getText().toString();
				w.make("Data set deleted", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
			}

		} else if (requestCode == QUEUE_UPLOAD_REQUESTED) {

			boolean success = uq.buildQueueFromFile();
			if (!success) {
				w.make("Could not re-build queue from file!", Waffle.IMAGE_X);
			}

		} else if (requestCode == CAN_LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {
				performExpNumCheckOnReturn = true;
				Intent iLogin = new Intent(mContext, LoginActivity.class);
				startActivityForResult(iLogin, LOGIN_REQUESTED);
			} else if (resultCode == RESULT_CANCELED) {
				checkExpNumCredentials();
			}
		}
	}

	// Calls the rapi primitives for actual uploading
	private Runnable uploader = new Runnable() {

		@Override
		public void run() {
			status400 = false;
			int sessionId = -1;

			String city = "", state = "", country = "", addr = "";
			List<Address> address = null;

			try {
				if (loc != null) {
					address = new Geocoder(DataCollector.this,
							Locale.getDefault()).getFromLocation(
							loc.getLatitude(), loc.getLongitude(), 1);
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
						"", "", "United States");
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
					DataSet ds = new DataSet(DataSet.Type.DATA, nameOfSession,
							description, eid, dataSet.toString(), null,
							sessionId, city, state, country, addr);
					uq.addDataSetToQueue(ds);
				}

				int pic = MediaManager.pictureArray.size();

				while (pic > 0) {
					boolean picSuccess = rapi.uploadPictureToSession(
							MediaManager.pictureArray.get(pic - 1), eid,
							sessionId, nameOfSession, description);

					// Saves pictures for later upload
					if (!picSuccess) {
						DataSet ds = new DataSet(DataSet.Type.PIC,
								nameOfSession, description, eid, null,
								MediaManager.pictureArray.get(pic - 1),
								sessionId, city, state, country, addr);
						uq.addDataSetToQueue(ds);
					}
					pic--;
				}

				MediaManager.pictureArray.clear();
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

			MediaManager.mediaCount = 0;

			sessionName.setText("");
			recordingLength.setText("");
			sampleInterval.setText("");

			nameOfSession = "";

			showSummary();

			if (status400)
				w.make("Your data cannot be uploaded to this experiment.  It has been closed.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			else if (!uploadSuccess) {
				if (rapi.isLoggedIn())
					w.make("Data not uploaded - saved instead",
							Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
				SharedPreferences mPrefs = getSharedPreferences("save_dialog", 0);
				boolean seenDialog = mPrefs.getBoolean("seen_dialog", false);
				if (!seenDialog) {
					Intent iUploadFailSave = new Intent(mContext, UploadFailSave.class);
					startActivity(iUploadFailSave);
					SharedPreferences.Editor mEdit = mPrefs.edit();
					mEdit.putBoolean("seen_dialog", true);
					mEdit.commit();
				}
			} else {
				w.make("Upload success", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
				manageUploadQueue();
			}

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

		time.setText("Time Elapsed: " + min + ":" + sec);
	}

	// Deals with login and UI display
	void login() {
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				DataCollector.mContext,
				DataCollector.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		boolean success = rapi.login(mPrefs.getString("username", ""),
				mPrefs.getString("password", ""));
		if (!success) {
			// This is crazy, so Waffle me maybe?
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

			if (dfm.enabledFields[Fields.ACCEL_X] || dfm.enabledFields[Fields.ACCEL_Y]
					|| dfm.enabledFields[Fields.ACCEL_Z]
					|| dfm.enabledFields[Fields.ACCEL_TOTAL]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager
								.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[Fields.MAG_X] || dfm.enabledFields[Fields.MAG_Y]
					|| dfm.enabledFields[Fields.MAG_Z] || dfm.enabledFields[Fields.MAG_TOTAL]
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

			if (dfm.enabledFields[Fields.PRESSURE] || dfm.enabledFields[Fields.ALTITUDE]) {
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
	
	// TODO - done?
	private void setUpDFMWithAllFields() {		
		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		SharedPreferences.Editor mEdit = mPrefs.edit();
		mEdit.putString("experiment_id", "-1").commit();

		dfm = new DataFieldManager(Integer.parseInt(mPrefs.getString("experiment_id", "-1")), rapi, mContext, f);
		dfm.getOrder();
		
		for (int i = 0; i < Fields.TEMPERATURE_K; i++)
			dfm.enabledFields[i] = true;
		
		String acceptedFields = getResources().getString(R.string.time) + "," +
						getResources().getString(R.string.accel_x) + "," +
						getResources().getString(R.string.accel_y) + "," +
						getResources().getString(R.string.accel_z) + "," +
						getResources().getString(R.string.accel_total) + "," +
						getResources().getString(R.string.latitude) + "," +
						getResources().getString(R.string.longitude) + "," +
						getResources().getString(R.string.magnetic_x) + "," +
						getResources().getString(R.string.magnetic_y) + "," +
						getResources().getString(R.string.magnetic_z) + "," +
						getResources().getString(R.string.magnetic_total) + "," +
						getResources().getString(R.string.heading_deg) + "," +
						getResources().getString(R.string.heading_rad) + "," +
						getResources().getString(R.string.temperature_c) + "," +
						getResources().getString(R.string.pressure) + "," +
						getResources().getString(R.string.altitude) + "," +
						getResources().getString(R.string.luminous_flux) + "," +
						getResources().getString(R.string.temperature_f) + "," +
						getResources().getString(R.string.temperature_k);
	
		mEdit.putString("accepted_fields", acceptedFields).commit();
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

			chooseSensorIntent();

		}
	}
	
	private void chooseSensorIntent() {
		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		String expNum = mPrefs.getString("experiment_id", "");
		Intent i = new Intent(mContext, ChooseSensorDialog.class);
		Experiment e = rapi.getExperiment(Integer.parseInt(expNum));
		i.putExtra("expnum", expNum);
		if (e != null)
			i.putExtra("expname", e.name);
		else
			i.putExtra("expname", "");
		startActivityForResult(i, CHOOSE_SENSORS_REQUESTED);
		
	}

	private void initDfm() {
		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		String experimentInput = mPrefs.getString("experiment_id", "");

		if (experimentInput.equals("-1")) {
			setUpDFMWithAllFields();
		} else {
			dfm = new DataFieldManager(Integer.parseInt(experimentInput), rapi,
					mContext, f);
			dfm.getOrder();

			sc = dfm.checkCompatibility();

			String fields = mPrefs.getString("accepted_fields", "");
			if (fields.equals("")) {
				// launch intent to setup fields
				w.make("Please re-select fields", Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
				chooseSensorIntent();
			} else {
				getFieldsFromPrefsString(fields);
			}

			getEnabledFields();
		}
	}

	private void getFieldsFromPrefsString(String fieldList) {

		String[] fields = fieldList.split(",");
		acceptedFields = new LinkedList<String>();

		for (String f : fields) {
			acceptedFields.add(f);
		}

	}

	private void getEnabledFields() {

		try {
			for (String s : acceptedFields) { if (s.length() != 0) break; }
		} catch (NullPointerException e) {
			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String fields = mPrefs.getString("accepted_fields", "");
			getFieldsFromPrefsString(fields);
		}
		
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
		mScreen = (LinearLayout) findViewById(R.id.mainScreen);
		isenseLogo = (ImageView) findViewById(R.id.ImageViewLogo);
		startStop = (Button) findViewById(R.id.startStop);
		time = (TextView) findViewById(R.id.time);
		sessionName = (EditText) findViewById(R.id.sessionName);
		sampleInterval = (EditText) findViewById(R.id.sampleInterval);
		recordingLength = (EditText) findViewById(R.id.testLength);
	}

	// Variables needed to be initialized for onCreate
	private void initVars() {
		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(true);
		
		performCredentialChecks();

		uq = new UploadQueue("datacollector", mContext, rapi);
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
	
	private void performCredentialChecks() {
		if (rapi.isConnectedToInternet()) {
			final SharedPreferences mPrefs = new ObscuredSharedPreferences(
					DataCollector.mContext,
					DataCollector.mContext.getSharedPreferences("USER_INFO",
							Context.MODE_PRIVATE));

			if (!(mPrefs.getString("username", "").equals(""))) {
				login();
				checkExpNumCredentials();
			} else {
				Intent iCanLogin = new Intent(mContext, CanLogin.class);
				startActivityForResult(iCanLogin, CAN_LOGIN_REQUESTED);
			}
		} else {
			checkExpNumCredentials();
		}
	}
	
	private void checkExpNumCredentials() {
		if (rapi.isConnectedToInternet())
			checkExpNumCredentialsWithConnectivity();
		else
			checkExpNumCredentialsWithoutConnectivity();
	}
	
	private void checkExpNumCredentialsWithConnectivity() {
		SharedPreferences expPrefs = getSharedPreferences("EID", 0);
		if (!(expPrefs.getString("experiment_id", "").equals(""))) {
			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String fields = mPrefs.getString("accepted_fields", "");
			if (!(fields.equals(""))) {
				if (dfm == null) initDfm();
				getEnabledFields();
			} else {
				new SensorCheckTask().execute();
			}
		} else {
			Intent iSetup = new Intent(DataCollector.this, Setup.class);
			iSetup.putExtra("enable_no_exp_button", true);
			startActivityForResult(iSetup, SETUP_REQUESTED);
		}
	}
	
	private void checkExpNumCredentialsWithoutConnectivity() {
		SharedPreferences expPrefs = getSharedPreferences("EID", 0);
		if (!(expPrefs.getString("experiment_id", "").equals(""))) {
			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String fields = mPrefs.getString("accepted_fields", "");
			if (!(fields.equals(""))) {
				if (dfm == null) initDfm();
				getEnabledFields();
				Intent iCanRecord = new Intent(mContext, CanRecord.class);
				startActivity(iCanRecord);
			} else {
				Intent iNeedConnectivity = new Intent(mContext, NeedConnectivity.class);
				startActivity(iNeedConnectivity);
			}
		} else {
			Intent iNeedConnectivity = new Intent(mContext, NeedConnectivity.class);
			startActivity(iNeedConnectivity);
		}
	}

	@Override
	protected void onStart() {
		if(mLocationManager != null)
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

		// Colorize the startStop button and add the huge listener
		startStop.getBackground().setColorFilter(0xFFFF0000,
				PorterDuff.Mode.MULTIPLY);
		setStartStopListener();
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
					startStop.performLongClick();
			}
		});
	}
	
	// Displays description dialog when data is done recording
	public void displayDescription() {
		
		SimpleDateFormat sdf = new SimpleDateFormat(
				"MM-dd-yyyy, HH:mm:ss", Locale.US);
		Date dt = new Date();
		dateString = sdf.format(dt);
		nameOfSession += " - " + dateString;
		
		// absolutely ensure the timer resets to 0
		setTime(0);

		Intent iDescription = new Intent(mContext,
				Description.class);
		startActivityForResult(iDescription,
				DESCRIPTION_REQUESTED);
		
		if (terminateThroughPowerOff) {
			terminateThroughPowerOff = false;
			Intent iForceStop = new Intent(mContext, ForceStop.class);
			startActivity(iForceStop);
		}
	}
	
	// Code for registering sensors and preparing to poll data
	public void setUpSensorsForRecording() {
		initDfm();
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
			if (!f.angle_deg.equals(""))
				f.angle_rad = toThou.format(
						(Double.parseDouble(f.angle_deg) * (Math.PI / 180)));
			else
				f.angle_rad = "";
		if (dfm.enabledFields[Fields.MAG_X])
			f.mag_x = toThou.format(mag[0]);
		if (dfm.enabledFields[Fields.MAG_Y])
			f.mag_y = toThou.format(mag[1]);
		if (dfm.enabledFields[Fields.MAG_Z])
			f.mag_z = toThou.format(mag[2]);
		if (dfm.enabledFields[Fields.MAG_TOTAL])
			f.mag_total = toThou.format(Math.sqrt(Math
					.pow(mag[0], 2)
					+ Math.pow(mag[1], 2)
					+ Math.pow(mag[2], 2)));
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
		
		dataSet.put(dfm.putData());
		data = dfm.writeSdCardLine();
		
		if (beginWrite) {
			String header = dfm.writeHeaderLine();
			writeToSDCard(header, 's');
			writeToSDCard(data, 'u');
		} else {
			writeToSDCard(data, 'u');
		}
	}

	// All the code for the main button!
	public void setStartStopListener() {
		startStop.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				if (!running) {
					SharedPreferences mPrefs = getSharedPreferences("EID", 0);
					boolean numbersReady = true;
					if (!sampleInterval.getText().toString().equals("")) {
						try {
							int sInterval = Integer.parseInt(sampleInterval
									.getText().toString());
							if (sInterval < 50) {
								sampleInterval
										.setError("Enter an interval >= 50 ms");
								numbersReady = false;
							}

						} catch (NumberFormatException nfe) {
							sampleInterval.setError("Enter an interval >= 50 ms");
							numbersReady = false;
						}
					}
					if (!recordingLength.getText().toString().equals("")) {
						try {
							int testLength = Integer.parseInt(recordingLength
									.getText().toString());
							if (testLength < 0) {
								recordingLength
										.setError("Enter a positive test length");
								numbersReady = false;
							}

						} catch (NumberFormatException nfe) {
							recordingLength
									.setError("Enter a positive test length");
							numbersReady = false;
						}
					}

					SharedPreferences expPrefs = getSharedPreferences("EID", 0);
					if (expPrefs.getString("experiment_id", "").equals("")) {						
						if (rapi.isConnectedToInternet()) {

							w.make("Please select an experiment", Waffle.LENGTH_LONG,
									Waffle.IMAGE_WARN);
							Intent iSetup = new Intent(DataCollector.this, Setup.class);
							iSetup.putExtra("enable_no_exp_button", true);
							startActivityForResult(iSetup, SETUP_REQUESTED);
						} else {
							Intent iNeedConnectivity = new Intent(mContext, NeedConnectivity.class);
							startActivity(iNeedConnectivity);
						}

					} else if (mPrefs.getString("accepted_fields", "").equals("")) {

						w.make("Please select fields to record", Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
						chooseSensorIntent();
							 
					} else if (sessionName.getText().toString().equals("")) {
						
						w.make("Please enter a session name first", Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
						sessionName.setError("Enter a session name");

					} else if (!numbersReady) {
						// Not ready to record data yet.  Do nothing.
					} else {

						nameOfSession = sessionName.getText().toString();
						sessionName.setError(null);
						recordingLength.setError(null);
						sampleInterval.setError(null);

						if (!sampleInterval.getText().toString().equals(""))
							srate = Integer.parseInt(sampleInterval.getText()
									.toString());
						else
							srate = INTERVAL;
						
						if (!recordingLength.getText().toString().equals(""))
							recLength = Integer.parseInt(recordingLength.getText()
									.toString());
						else
							recLength = TEST_LENGTH;

						vibrator.vibrate(300);
						mMediaPlayer.setLooping(false);
						mMediaPlayer.start();
					
						// start running task
						running = true;
							
						OrientationManager.disableRotation((Activity) mContext);
							
						getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // TODO - is this needed because of service wakelock?
							
						sessionName.setEnabled(false);
						recordingLength.setEnabled(false);
						sampleInterval.setEnabled(false);
							
						setMenuStatus(false);
							
						startStop.setText(R.string.stopString);
							
						startStop.getBackground().setColorFilter(0xFF00FF00,
									PorterDuff.Mode.MULTIPLY);
						mScreen.setBackgroundResource(R.drawable.background_running);
						isenseLogo.setImageResource(R.drawable.logo_green);
						
						setUpSensorsForRecording();
						
						Intent iService = new Intent(mContext, DataCollectorService.class);
						iService.putExtra(DataCollectorService.SRATE, srate);
						iService.putExtra(DataCollectorService.REC_LENGTH, recLength);
						startService(iService);
					 
						return running;
							
					}
					
					running = false;
					return running;
					
				} else {
					vibrator.vibrate(300);
					mMediaPlayer.setLooping(false);
					mMediaPlayer.start();
					
					running = false;
					
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					
					stopService(new Intent(mContext, DataCollectorService.class));
					
					OrientationManager.enableRotation((Activity) mContext);
					
					sessionName.setEnabled(true);
					recordingLength.setEnabled(true);
					sampleInterval.setEnabled(true);

					writeToSDCard(null, 'f');
					setMenuStatus(true);
					
					startStop.setText(R.string.startString);
					setTime(0);

					startStop.getBackground().setColorFilter(0xFFFF0000,
							PorterDuff.Mode.MULTIPLY);
					mScreen.setBackgroundResource(R.drawable.background);
					isenseLogo.setImageResource(R.drawable.logo_red);

					choiceViaMenu = false;

					if (sdCardError)
						w.make("Could not write file to SD Card",
								Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
					
					displayDescription();
					
					return running;
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
		else
			appendMe = "Filename: \n" + sdFileName;

		Intent iSummary = new Intent(mContext, Summary.class);
		iSummary.putExtra("millis", s_elapsedMillis)
				.putExtra("seconds", s_elapsedSeconds)
				.putExtra("minutes", s_elapsedMinutes)
				.putExtra("append", appendMe).putExtra("date", dateString)
				.putExtra("points", "" + dataPointCount);

		startActivity(iSummary);

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
			long timeStart = System.currentTimeMillis();

			mHandler.post(loadingThread);

			long timeEllapsed = System.currentTimeMillis() - timeStart;
			try {
				if (timeEllapsed < 2000)
					Thread.sleep(2000 - timeEllapsed);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

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

			setContentView(R.layout.data_collector);
			initMainUI();
			assignVars();

			super.onPostExecute(result);
		}

	}

	// allows for menu to be turned off when necessary
	private void setMenuStatus(boolean enabled) {
		useMenu = enabled;

		if (mMenu != null) {
			menuSetup.setEnabled(enabled);
			menuUpload.setEnabled(enabled);
			menuLogin.setEnabled(enabled);
			menuMedia.setEnabled(enabled);
			menuSync.setEnabled(enabled);
			if (enabled) {
				MenuItem item= mMenu.findItem(R.id.menu_item_setup);
		    	item.setVisible(true);
		    	item= mMenu.findItem(R.id.menu_item_upload);
		    	item.setVisible(true);
		    	item= mMenu.findItem(R.id.menu_item_login);
		    	item.setVisible(true);
		    	item= mMenu.findItem(R.id.menu_item_media);
		    	item.setVisible(true);
		    	item= mMenu.findItem(R.id.menu_item_sync);
		    	item.setVisible(true);
		    	super.onPrepareOptionsMenu(mMenu);
			} else {
				MenuItem item= mMenu.findItem(R.id.menu_item_setup);
		    	item.setVisible(false);
		    	item= mMenu.findItem(R.id.menu_item_upload);
		    	item.setVisible(false);
		    	item= mMenu.findItem(R.id.menu_item_login);
		    	item.setVisible(false);
		    	item= mMenu.findItem(R.id.menu_item_media);
		    	item.setVisible(false);
		    	item= mMenu.findItem(R.id.menu_item_sync);
		    	item.setVisible(false);
		    	super.onPrepareOptionsMenu(mMenu);
			}
		}
	}

}