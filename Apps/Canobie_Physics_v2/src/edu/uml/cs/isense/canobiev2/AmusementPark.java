/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII            General Purpose Amusement Park Application      SSSSSSSSS       **/
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

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.credentials.CredentialManager;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.waffle.Waffle;
import android.app.ActionBar;


public class AmusementPark extends Activity implements SensorEventListener,
		LocationListener {

	/* Default Constants */
	private final String ACTIVITY_NAME = "canobielake";
	private final String TIME_OFFSET_PREFS_ID = "time_offset";
	private final String TIME_OFFSET_KEY = "timeOffset";

	/* UI Handles */
	public static EditText experimentInput;
	public static TextView rideName;
	private TextView time;
	private TextView values;
	private Button startStop;
	private ToggleButton gravity;
	public static EditText dataset; 
	public static EditText project;
	public static CheckBox projectLater;
	public static EditText sampleRate;
	public static EditText studentNumber;
	public static CheckBox isCanobie;
	
	/* Recording Constants */
	private final int SAMPLE_INTERVAL = 200;
	private final int DATA_POINT_LIMIT = 3000;
	
	/*Values obtained from Configuration*/
	public static int projectNum = -1;
	public static String dataName = "";
	public static String rate = "200";
	public static String rideNameString = "NOT SET";
	public static String stNumber = "1";
	public static Boolean projectLaterChecked = false;
	public static Boolean canobieChecked = true;
	public static int spinnerid = 0;

	
	/* Managers and Their Variables */
	public static DataFieldManager dfm;
	private SensorManager mSensorManager;
	private LocationManager mLocationManager;
	private LocationManager mRoughLocManager;
	private Location loc;
	private Timer recordingTimer;
	public static UploadQueue uq;
	private Vibrator vibrator;

	/* Other Important Objects */
	private LinkedList<String> acceptedFields;
	private Fields f;
	private String dataToBeWrittenToFile;
	private MediaPlayer mMediaPlayer;
	private API api;
	public static Context mContext;
	private Waffle w;

	/* Work Flow Variables */
	private boolean isRunning = false;
	private static boolean useMenu = true;
	public static boolean setupDone = false;

	

	/* Recording Variables */
	private float rawAccel[];
	private float rawMag[];
	private float accel[];
	private float orientation[];
	private float mag[];
	private String temperature = "";
	private String pressure = "";
	private String light = "";
	private long srate = SAMPLE_INTERVAL;
	private boolean includeGravity = true;

	/* Menu Items */
	private final int MENU_ITEM_SETUP = 0;
	private final int MENU_ITEM_LOGIN = 1;
	private final int MENU_ITEM_UPLOAD = 2;
	//private final int MENU_ITEM_TIME = 3;
	//private final int MENU_ITEM_MEDIA = 4;
	// TODO Rajia
	private final int MENU_ITEM_ABOUT = 3;
	private final int MENU_ITEM_HELP = 4;
	
	/* Action Bar */
	private static int actionBarTapCount = 0;
	private static boolean useDev = false;

	/* Start Activity Codes*/
	private final int QUEUE_UPLOAD_REQUESTED = 1;
	private final int CHOOSE_SENSORS_REQUESTED = 3;
	private final int SYNC_TIME_REQUESTED = 4;
	private final int SETUP_REQUESTED = 5;
	private final int LOGIN_REQUESTED = 6;
	
	private int dataPointCount = 0;
	private int elapsedSecs;

	/* Used with Sync Time */
	private long timeOffset = 0;	
	
	/* Used to set time elapsed */
	private String sec = "";
	private int min = 0;

	public static JSONArray dataSet;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Think pointer to this activity
		mContext = this;

		// Initialize everything you're going to need
		initVars();

		enableAllFields();
		
		// Main Layout Button for Recording Data
		startStop.setOnLongClickListener(new OnLongClickListener() {

			@SuppressLint("NewApi")
			@Override
			public boolean onLongClick(View arg0) {
				if ((!setupDone)) {
					w.make("You must setup before recording data.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);

					isRunning = false;
					return isRunning;

				} else {

					// Vibrate and Beep
					vibrator.vibrate(300);
					mMediaPlayer.setLooping(false);
					mMediaPlayer.start();

					

					// Stop the recording and reset UI if running
					if (isRunning) {
						isRunning = false;
						useMenu = true;	
						if (android.os.Build.VERSION.SDK_INT >= 11)
							invalidateOptionsMenu();

						// Unregister sensors to save battery
						mSensorManager.unregisterListener(AmusementPark.this);

						// Update the main button
						enableMainButton(false);

						// Reset main UI
						time.setText(getResources().getString(
								R.string.timeElapsed));
						values.setText(R.string.xyz);
								
						//enable gravity togglebutton
						gravity.setEnabled(true);
						
						// Cancel the recording timer
						recordingTimer.cancel();
						
						//Add data to Queue to be uploaded
						new AddToQueueTask().execute(); 
						

						return isRunning;

						// Start the recording
					} else {
						srate = Integer.parseInt(rate);
						
						
						//new SensorCheckTask().execute();
						
						isRunning = true;
						useMenu = false;			
						
						//disable gravity togglebutton
						gravity.setEnabled(false);
						
						//disable menu
						if (android.os.Build.VERSION.SDK_INT >= 11)
							invalidateOptionsMenu();

						// Check to see if a valid project was chosen. If not,
						// (projectNum is -1) enable all fields for recording.
						
						if (projectNum == -1) {
							enableAllFields();
						}

						// Create a file so that we can write results to the
						// sdCard
						prepWriteToSDCard(new Date());

						//enable sensors needed
						registerSensors();

						dataSet = new JSONArray();
						elapsedSecs = 0;
						dataPointCount = 0;

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							w.make("Data recording interrupted! Time values may be inconsistent.",
									Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
							e.printStackTrace();
						}

						if (mSensorManager != null) {
							if (gravity.isChecked() == true) {
								mSensorManager
								.registerListener(
										AmusementPark.this,
										mSensorManager
												.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
										SensorManager.SENSOR_DELAY_FASTEST);
							} else {
								mSensorManager
								.registerListener(
										AmusementPark.this,
										mSensorManager
												.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
										SensorManager.SENSOR_DELAY_FASTEST);
							}
							
							mSensorManager
									.registerListener(
											AmusementPark.this,
											mSensorManager
													.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
											SensorManager.SENSOR_DELAY_FASTEST);
						}

						dataToBeWrittenToFile = "Accel-X, Accel-Y, Accel-Z, Accel-Total, Mag-X, Mag-Y, Mag-Z, Mag-Total"
								+ "Latitude, Longitude, Time\n";
						startStop.setText(getResources().getString(R.string.stopString));
						startStop.setBackgroundResource(R.drawable.button_rsense_green);						
						
						new TimeElapsedTask().execute();
						
						initDfm();
						recordingTimer = new Timer();
						recordingTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {
								if(dataPointCount > DATA_POINT_LIMIT - 2) {
									AmusementPark.this.runOnUiThread(new Runnable(){
									    public void run(){
											startStop.performLongClick();
									    }
									});
								}
								recordData();
							}
						}, srate, srate);
					}
					
					
					
					return isRunning;
				}
			}

		});
		
		gravity.setOnCheckedChangeListener(new OnCheckedChangeListener () {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				includeGravity = gravity.isChecked();				
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

		String dateString = sdf.format(date);

		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}
		
		String sdFileName = rideNameString + "-" + stNumber + "-"
				+ dateString + ".csv";
		File sdFile = new File(folder, sdFileName);

		return sdFile;

	}

	@Override
	public void onPause() {
		super.onPause();

		// Stop the current sensors to save battery.
		mLocationManager.removeUpdates(AmusementPark.this);
		mSensorManager.unregisterListener(AmusementPark.this);
		
		//Stop recording
		if (isRunning) {
			startStop.performLongClick();
		}


	}

	@Override
	public void onResume() {
		super.onResume();

		// Silently logs in the user to iSENSE
		CredentialManager.login(mContext, api);

		// Rebuilds the upload queue
		if (uq != null)
			uq.buildQueueFromFile();
		
		
		initLocManager();
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		initLocManager();

	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	

	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.setGroupEnabled(0, useMenu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.MENU_ITEM_SETUP:
			Intent iSetup = new Intent(AmusementPark.this, Configuration.class);
			startActivityForResult(iSetup, SETUP_REQUESTED);
			return true;
		//*************Rajia ******************	
		case R.id.MENU_ITEM_UPLOAD:
			manageUploadQueue();
			return true;
		case R.id.MENU_ITEM_ABOUT:
			// Shows the about dialog
			startActivity(new Intent(this, About.class));
			return true;
			
		case R.id.MENU_ITEM_HELP:
			// Shows the about dialog
			startActivity(new Intent(this, Help.class));
			return true;
		//***************************************	
			
		case R.id.MENU_ITEM_LOGIN:
			startActivityForResult(new Intent(getApplicationContext(),
					CredentialManager.class), LOGIN_REQUESTED);
			return true;
		
//		case R.id.MENU_ITEM_TIME:
//			startActivityForResult(new Intent(getApplicationContext(),
//					SyncTime.class), SYNC_TIME_REQUESTED);
//			return true;
//		case R.id.MENU_ITEM_MEDIA:
//			if ((!setupDone)) {
//				w.make("You must setup before using Media Manager.",
//						Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
//			} else {
//				Intent iMedia = new Intent(AmusementPark.this, MediaManager.class);
//				startActivity(iMedia);
//			}
//			return true;
		case android.R.id.home:
			CountDownTimer cdt = null;

			// Give user 10 seconds to switch dev/prod mode
			if (actionBarTapCount == 0) {
				cdt = new CountDownTimer(5000, 5000) {
					public void onTick(long millisUntilFinished) {
					}

					public void onFinish() {
						actionBarTapCount = 0;
					}
				}.start();
			}

			String other = (useDev) ? "production" : "dev";

			switch (++actionBarTapCount) {
			case 5:
				w.make(getResources().getString(R.string.two_more_taps) + other
						+ getResources().getString(R.string.mode_type));
				break;
			case 6:
				w.make(getResources().getString(R.string.one_more_tap) + other
						+ getResources().getString(R.string.mode_type));
				break;
			case 7:
				w.make(getResources().getString(R.string.now_in_mode) + other
						+ getResources().getString(R.string.mode_type));
				useDev = !useDev;
				
				api.useDev(useDev);

				if (cdt != null)
					cdt.cancel();

				CredentialManager.login(this, api);
				actionBarTapCount = 0;
				
				
				
				break;
			}

			return true;
		
		default:
			return false;
		}
	}
	
	
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	/**
	 * Stores some data into our global objects as quickly as we get new points.
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		DecimalFormat toThou = new DecimalFormat("######0.000");
		//DecimalFormat threeDigit = new DecimalFormat("#,##0.000");
		DecimalFormat oneDigit = new DecimalFormat("#,#00.0");

		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION ||
				event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
			
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

				if (isRunning) {
					values.setText("Ax: " + xPrepend
							+ oneDigit.format(accel[0]) + " " + "m/s^2" + "\nAy: " + yPrepend
							+ oneDigit.format(accel[1]) + " " + "m/s^2" + "\nAz: " + zPrepend
							+ oneDigit.format(accel[2]) + " " + "m/s^2");
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

		if (requestCode == SYNC_TIME_REQUESTED) {
			if (resultCode == RESULT_OK) {
				timeOffset = data.getExtras().getLong("offset");
				SharedPreferences mPrefs = getSharedPreferences(
						TIME_OFFSET_PREFS_ID, 0);
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putLong(TIME_OFFSET_KEY, timeOffset);
				mEditor.commit();
			}
		} else if (requestCode == QUEUE_UPLOAD_REQUESTED) {
			boolean success = uq.buildQueueFromFile();
			if (!success) {
				w.make("Could not re-build queue from file!", Waffle.IMAGE_X);
			}
		} else if (requestCode == CHOOSE_SENSORS_REQUESTED) {
			startStop.setEnabled(true);
			dfm.setEnabledFields(acceptedFields);
			
		} else if (requestCode == SETUP_REQUESTED) {
				rideName.setText("Ride: " + rideNameString);
				
				SharedPreferences mPrefs = getSharedPreferences(
						Setup.PROJ_PREFS_ID, 0);
				final SharedPreferences.Editor mEdit = mPrefs.edit();
				mEdit.putString(Setup.PROJECT_ID, Integer.toString(projectNum));
				mEdit.commit();
				
				
		} else if (requestCode == LOGIN_REQUESTED) {
			
			
		}

	}

	// Assists with differentiating between displays for dialogues
	private int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	// Calls the api primitives for actual uploading
	private Runnable uploader = new Runnable() {

		@Override
		public void run() {

			// Create name from time stamp
			String name = dataName;

			// Retrieve project id
			SharedPreferences mPrefs = getSharedPreferences(Setup.PROJ_PREFS_ID, 0);
			String projId = mPrefs.getString(Setup.PROJECT_ID, "");
			
			Log.e("DATASET", dataSet.toString());
			
			Date date = new Date();
			
			// Saves data to queue for later upload
			QDataSet ds = new QDataSet(name + " Ride: " + rideNameString + " Gravity: " + ((includeGravity) ? "Included" : "Not Included"), "Time: " + getNiceDateString(date) 
					+ "\n" + "Number of Data Points: " + dataPointCount, QDataSet.Type.DATA,
					dataSet.toString(), null, projId, null);
			
			uq.addDataSetToQueue(ds);
		}

	};

	/**
	 * Uploads data to iSENSE or something.
	 * 
	 * @author jpoulin
	 */
	private class AddToQueueTask extends AsyncTask<String, Void, String> {

		ProgressDialog dia;

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(AmusementPark.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while your data and media saved to Queue");
			dia.setCancelable(false);
			dia.show();

		}
		
		@Override
		protected String doInBackground(String... strings) {
			uploader.run();
			return null; //strings[0];
		}

		@Override
		protected void onPostExecute(String sdFileName) {

			dia.setMessage("Done");
			dia.dismiss();
			
			w.make("Data Saved to Queue", Waffle.LENGTH_SHORT,
					Waffle.IMAGE_CHECK);
			manageUploadQueue();
			

			Date date = new Date();
			showSummary(date, sdFileName);

		}
	}

	/**
	 * Writes the passed in time to the main screen.
	 * 
	 * @param seconds
	 */ 
	public void setTime(int seconds) {
		elapsedSecs = seconds;
		
		min = seconds / 60;
		int secInt = seconds % 60;

		sec = "";
		if (secInt <= 9)
			sec = "0" + secInt;
		else
			sec = "" + secInt;
		

		runOnUiThread(new Runnable() {
		    public void run() {
		    	time.setText("Time Elapsed: " + min + ":" + sec);
		    }
		});

	}
	
	private class TimeElapsedTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... voids) {
			for(int time = 0; isRunning; time++ ) {
				setTime(time);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			
		}
	}
	
	
	

	/**
	 * Everything needed to be initialized for onCreate in one helpful function.
	 */
	@SuppressLint("NewApi")
	private void initVars() {

		api = API.getInstance();
		api.useDev(useDev);
		
		// Initialize action bar customization for API >= 11
				if (android.os.Build.VERSION.SDK_INT >= 11) {
					ActionBar bar = getActionBar();

					// make the actionbar clickable
					bar.setDisplayHomeAsUpEnabled(true);
				}

		// Login to iSENSE
		CredentialManager.login(this, api);

		// Create a new upload queue
		uq = new UploadQueue(ACTIVITY_NAME, mContext, api);
		
		// OMG a button!
		startStop = (Button) findViewById(R.id.startStop);
		
		//Toggle button for gravity
		gravity = (ToggleButton) findViewById(R.id.tbGravity);
		gravity.setChecked(includeGravity);
		
		// Have some TVs. TextViews I mean.
		values = (TextView) findViewById(R.id.values);
		time = (TextView) findViewById(R.id.time);
		rideName = (TextView) findViewById(R.id.ridename);
		rideName.setText("Ride: " + rideNameString);

		// Start some managers
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Waffles ares UI messages, and fields are used in recording
		w = new Waffle(this);
		f = new Fields();

		// These are arrays that store values we may record. Who knows, maybe we'll use them.
		accel = new float[4];
		orientation = new float[3];
		rawAccel = new float[3];
		rawMag = new float[3];
		mag = new float[3];
		
		// Fire up the GPS chip (not literally)
		initLocManager();

		

		// Most important feature. Makes the button beep.
		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);
	}

	

	/**
	 * Set all dfm's fields to enabled. 
	 */
	private void enableAllFields() {
		setUpDFMWithAllFields();
	}
	
	//set up GPS
	private void initLocManager() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mRoughLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				&& mRoughLocManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0, AmusementPark.this);
			mRoughLocManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, AmusementPark.this);
		} else {
			
		}
		
		// This is the location we will get back from GPS
		loc = new Location(mLocationManager.getBestProvider(c, true));
		
	}
	
	

	private void initDfm() {

		SharedPreferences mPrefs = getSharedPreferences(Setup.PROJ_PREFS_ID, 0);
		String projectInput = mPrefs.getString(Setup.PROJECT_ID, "");

		if (projectInput.equals("-1")) {
			setUpDFMWithAllFields();
		} else {
			dfm = new DataFieldManager(Integer.parseInt(projectInput), api,
					mContext, f);
			dfm.getOrder();

			String fields = mPrefs.getString("accepted_fields", "");
			getFieldsFromPrefsString(fields);
			getEnabledFields();

		}
	}
	
	private void setUpDFMWithAllFields() {
		SharedPreferences mPrefs = getSharedPreferences(Setup.PROJ_PREFS_ID, 0);
		SharedPreferences.Editor mEdit = mPrefs.edit();
		mEdit.putString(Setup.PROJECT_ID, "-1").commit();

		dfm = new DataFieldManager(Integer.parseInt(mPrefs.getString(
				Setup.PROJECT_ID, "-1")), api, mContext, f);
		dfm.getOrder();

		dfm.enableAllFields();

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
	

	/**
	 * Prompts the user to upload the rest of their content
	 * upon successful upload of data.
	 */
	private void manageUploadQueue() {
		if (!uq.emptyQueue()) {
			Intent i = new Intent().setClass(mContext, QueueLayout.class);
			i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
			startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
		} else {
			w.make("No data to upload!", Waffle.IMAGE_X);
		}
	}
	
	/**
	 * Turns elapsedMillis into readable strings.
	 * 
	 * @author jpoulin
	 */
	private class ElapsedTime {
		private String elapsedSeconds;
		private String elapsedMinutes;
		
		/**
		 * Everybody likes strings.
		 * 
		 * @param seconds
		 */
		
		
		ElapsedTime(int seconds) {
			int minutes;
			
			minutes = seconds / 60;
			seconds %= 60;
			
			if (seconds < 10) {
				elapsedSeconds = "0" + seconds;
			} else {
				elapsedSeconds = "" + seconds;
			}


			if (minutes < 10) {
				elapsedMinutes = "0" + minutes;
			} else {
				elapsedMinutes = "" + minutes;
			}		
		}
		
	}

	/**
	 * Makes a summary dialog.
	 * @param date Time of upload
	 * @param sdFileName Name of the written csv
	 */
	private void showSummary(Date date, String sdFileName) {
		ElapsedTime time = new ElapsedTime(elapsedSecs);
		
		Intent iSummary = new Intent(mContext, Summary.class);
		iSummary.putExtra("seconds", time.elapsedSeconds)
				.putExtra("minutes", time.elapsedMinutes)
				.putExtra("date", getNiceDateString(date))
				.putExtra("points", "" + dataPointCount);

		startActivity(iSummary);

	}

	/**
	 * Turns on only the sensors you need to record data.
	 */
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

	/**
	 * Records a data point, puts it into the dataSet object, and writes it to a csv string.
	 */
	private void recordData() {
		 dataPointCount++;
         DecimalFormat toThou = new DecimalFormat("######0.000");

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
                 f.mag_x = "" + mag[0];
         if (dfm.enabledFields[Fields.MAG_Y])
                 f.mag_y = "" + mag[1];
         if (dfm.enabledFields[Fields.MAG_Z])
                 f.mag_z = "" + mag[2];
         if (dfm.enabledFields[Fields.MAG_TOTAL])
                 f.mag_total = "" + Math.sqrt(Math.pow(Double.parseDouble(f.mag_x), 2)
		                                 + Math.pow(Double.parseDouble(f.mag_y), 2)
		                                 + Math.pow(Double.parseDouble(f.mag_z), 2));
         if (dfm.enabledFields[Fields.TIME])
                 f.timeMillis = System.currentTimeMillis();         
         if (dfm.enabledFields[Fields.TEMPERATURE_C])
                 f.temperature_c = temperature;
         if (dfm.enabledFields[Fields.TEMPERATURE_F])
                 if (temperature.equals(""))
                         f.temperature_f = temperature;
                 else
                         f.temperature_f = "" + ((Double.parseDouble(temperature) * 1.8) + 32);
         if (dfm.enabledFields[Fields.TEMPERATURE_K])
                 if (temperature.equals(""))
                         f.temperature_k = temperature;
                 else
                         f.temperature_k = "" + (Double.parseDouble(temperature) + 273.15);
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
