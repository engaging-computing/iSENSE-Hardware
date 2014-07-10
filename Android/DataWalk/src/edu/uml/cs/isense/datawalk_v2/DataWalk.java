package edu.uml.cs.isense.datawalk_v2;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.credentials.ClassroomMode;
import edu.uml.cs.isense.credentials.CredentialManager;
import edu.uml.cs.isense.credentials.EnterName;
import edu.uml.cs.isense.datawalk_v2.dialogs.DataRateDialog;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProject;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * Behaves as the main driver for the iSENSE Data Walk App. Coordinates and
 * records Geo-location data.
 * 
 * @author Rajia
 */
public class DataWalk extends Activity implements LocationListener,
		SensorEventListener, Listener {

	/* UI Related Globals */
	private TextView elapsedTimeTV;
	private TextView pointsUploadedTV;
	private TextView latitudeTV;
	private TextView longitudeTV;
	private TextView distanceTV;
	private TextView velocityTV;
	private Button rcrdIntervalB;
	private Button projNumB;
	private Button nameB;
	private Button startStopB;
	private Button uploadB;
	private RelativeLayout nameAndLoginRL;
	private LinearLayout recordingExtrasLL;

	/* Manager Controlling Globals */
	private LocationManager mLocationManager;
	private Vibrator vibrator;
	private MediaPlayer mMediaPlayer;
	private API api;
	public static UploadQueue uq;
	private SensorManager mSensorManager;
	private Location loc;
	private Location prevLoc;
	private Location firstLoc;
	private Timer recordTimer;
	private Timer gpsTimer;
	private Waffle w;

	/* iSENSE API Globals and Constants */
	private final String DEFAULT_PROJECT = "13";
	private final String DEFAULT_PROJECT_DEV = "13";

	private int actionBarTapCount = 0;
	public static boolean useDev = false;
	public static String projectID = "13";

	private String loginName = "";
	private String loginPass = "";
	private String projectURL = "";
	private String dataSetName = "";
	private int dataSetID = -1;

	/* Project Preferences */
	private static final String PROJ_PREFS_KEY = "proj_prefs_key";
	private static final String PROJ_ID_PRODUCTION = "proj_id_production";
	private static final String PROJ_ID_DEV = "proj_id_dev";

	/* Manage Work Flow Between Activities */
	public static Context mContext;
	public static String firstName = "";
	public static String lastInitial = "";

	public static final String INTERVAL_PREFS_KEY = "INTERVALID";
	public static final String INTERVAL_VALUE_KEY = "interval_val";

	/* Manage Work Flow Within DataWalk.java */
	private boolean gpsWorking = true;
	private boolean useMenu = true;

	/* Recording Globals */
	private float accel[];
	private JSONArray dataSet;

	/* Dialog Identity Constants */
	private final int DIALOG_VIEW_DATA = 2;
	private final int DIALOG_NO_GPS = 3;
	private final int DIALOG_FORCE_STOP = 4;
	private final int QUEUE_UPLOAD_REQUESTED = 5;
	private final int RESET_REQUESTED = 6;
	private final int NAME_REQUESTED = 7;
	private final int PROJECT_REQUESTED = 8;
	private final int LOGIN_ISENSE_REQUESTED = 9;

	/* Timer Related Globals and Constants */
	private final int TIMER_LOOP = 1000;
	private static final int DEFAULT_INTERVAL = 10000;
	public static int mInterval = DEFAULT_INTERVAL;
	private int elapsedMillis = 0;
	private int dataPointCount = 0;
	private int timerTick = 0;
	private int gpsWaitingCounter = 0;

	/* Distance and Velocity */
	float distance = 0;
	float velocity = 0;
	float deltaTime = 0;
	boolean bFirstPoint = true;
	float totalDistance = 0;

    Intent service;
    BroadcastReceiver receiver;

	@SuppressLint("NewApi")
	/**
	 * Called when the application is created for the first time.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Save current context
		mContext = this;

        //initialize intent for service
        service = new Intent(mContext, Datawalk_Service.class);

		// Initialize action bar customization for API >= 11
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			ActionBar bar = getActionBar();
			// make the actionbar clickable
			bar.setDisplayHomeAsUpEnabled(true);
		}

		// Initialize all the managers.
		initManagers();

		// Initialize main UI elements
		initialize();

        /* update UI with data passed back from service */
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra("VELOCITY")) {
                    String s = intent.getStringExtra("VELOCITY");
                    velocityTV.setText(s);

                } else if (intent.hasExtra("TIME")) {
                    String s = intent.getStringExtra("TIME");
                    elapsedTimeTV.setText(s);

                } else if (intent.hasExtra("DISTANCE")) {
                    String s = intent.getStringExtra("DISTANCE");
                    distanceTV.setText(s);

                } else if (intent.hasExtra("POINTS")) {
                    String s = intent.getStringExtra("POINTS");
                    pointsUploadedTV.setText(s);
                }

            }
        };

		// Gets first name and last initial the first time
		if (firstName.equals("") || lastInitial.equals("")) {
			SharedPreferences classPrefs = getSharedPreferences(
					ClassroomMode.PREFS_KEY_CLASSROOM_MODE, MODE_PRIVATE);
			SharedPreferences namePrefs = getSharedPreferences(
					EnterName.PREFERENCES_KEY_USER_INFO, MODE_PRIVATE);
			boolean classroomMode = classPrefs.getBoolean(
					ClassroomMode.PREFS_BOOLEAN_CLASSROOM_MODE, true);

			if (!classroomMode) {
				if (namePrefs
						.getBoolean(
								EnterName.PREFERENCES_USER_INFO_SUBKEY_USE_ACCOUNT_NAME,
								true)
						&& Connection.hasConnectivity(mContext)) {
					RPerson user = api.getCurrentUser();
					if (user != null) {
						firstName = user.name;
						lastInitial = "";

						nameB.setText(firstName);
					}

				} else {
					firstName = namePrefs.getString(
							EnterName.PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME,
							"");
					lastInitial = namePrefs
							.getString(
									EnterName.PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL,
									"");

					if (firstName.length() == 0) {
						Intent iEnterName = new Intent(this, EnterName.class);
						iEnterName.putExtra(
								EnterName.PREFERENCES_CLASSROOM_MODE,
								classroomMode);
						startActivityForResult(iEnterName, NAME_REQUESTED);
					} else {
						nameB.setText(firstName + " " + lastInitial);
					}
				}
			} else {
                if (!EnterName.isOpen) {
                    Intent iEnterName = new Intent(this, EnterName.class);
                    iEnterName.putExtra(EnterName.PREFERENCES_CLASSROOM_MODE,
                            classroomMode);
                    startActivityForResult(iEnterName, NAME_REQUESTED);
                }
			}

		} else {
			nameB.setText(firstName + " " + lastInitial);
		}
		/* Starts the code for the main button. */
		startStopB.setOnLongClickListener(new OnLongClickListener() {

			@SuppressLint("NewApi")
			@Override
			public boolean onLongClick(View arg0) {

				// Vibrate and beep
				vibrator.vibrate(300);
				mMediaPlayer.setLooping(false);
				mMediaPlayer.start();

                if (Datawalk_Service.running) {
                    setLayoutNotRecording();
                    stopService(service);
                } else if (!Datawalk_Service.running && gpsWorking &&
                        (loc.getLatitude() != 0 && loc.getLongitude() != 0)  ) {
                    setLayoutRecording();
                    startService(service);
                } else if (!gpsWorking || (loc.getLatitude() != 0 && loc.getLongitude() != 0) ) {
                    w.make("No GPS Signal", Waffle.LENGTH_LONG, Waffle.IMAGE_X);
                }

				return Datawalk_Service.running;

			}

		});

		// Additional initializations that are dependent on whether or not we're
		// on dev
		onCreateInit();
        if (Datawalk_Service.running) {
            setLayoutRecording();
        } else {
            setLayoutNotRecording();

        }

	}// ends onCreate

	protected void onCreateInit() {
		// Set the initial default projectID in preferences
		SharedPreferences prefs = getSharedPreferences(PROJ_PREFS_KEY,
				Context.MODE_PRIVATE);
		if (useDev)
			projectID = prefs.getString(PROJ_ID_DEV, DEFAULT_PROJECT_DEV);
		else
			projectID = prefs.getString(PROJ_ID_PRODUCTION, DEFAULT_PROJECT);

		projNumB.setText("Project: " + projectID);
	}

	private void initialize() {
        //TODO
        CredentialManager.login(this, api);

        // Initialize main UI elements
		nameAndLoginRL = (RelativeLayout) findViewById(R.id.rl_nameandlogin);
		recordingExtrasLL = (LinearLayout) findViewById(R.id.ll_recordingextras);
		startStopB = (Button) findViewById(R.id.b_startstop);
		projNumB = (Button) findViewById(R.id.b_project);
		uploadB = (Button) findViewById(R.id.b_upload);
		nameB = (Button) findViewById(R.id.b_name);
		rcrdIntervalB = (Button) findViewById(R.id.b_rcrdinterval);
		elapsedTimeTV = (TextView) findViewById(R.id.tv_elapsedtime);
		pointsUploadedTV = (TextView) findViewById(R.id.tv_pointcount);
		latitudeTV = (TextView) findViewById(R.id.tv_longitude);
		longitudeTV = (TextView) findViewById(R.id.tv_latitude);
		distanceTV = (TextView) findViewById(R.id.tv_distance);
		velocityTV = (TextView) findViewById(R.id.tv_velocity);
		pointsUploadedTV.setText("Points Recorded: " + dataPointCount);
		elapsedTimeTV.setText("Time Elapsed: " + timerTick + " seconds");
		


		rcrdIntervalB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Launches the data recording interval picker
				startActivity(new Intent(mContext, DataRateDialog.class));
			}

		});

		projNumB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Allows the user to pick a project to upload to
				Intent setup = new Intent(mContext, Setup.class);
				startActivityForResult(setup, PROJECT_REQUESTED);
			}

		});

		uploadB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Launched the upload queue dialog
				manageUploadQueue();
			}

		});



		nameB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Launch the dialog that allows users to enter his/her
				// firstname
				// and last initial
				Intent iEnterName = new Intent(mContext, EnterName.class);
				SharedPreferences classPrefs = getSharedPreferences(
						ClassroomMode.PREFS_KEY_CLASSROOM_MODE, 0);
				iEnterName.putExtra(EnterName.PREFERENCES_CLASSROOM_MODE,
						classPrefs.getBoolean(
								ClassroomMode.PREFS_BOOLEAN_CLASSROOM_MODE,
								true));
				startActivityForResult(iEnterName, NAME_REQUESTED);
			}

		});

	}


    /**
     * Set layout up for not recording
     */
    void setLayoutNotRecording() {
        // Swap the layouts below the recording button
        nameAndLoginRL.setVisibility(View.VISIBLE);
        recordingExtrasLL.setVisibility(View.GONE);

        // No longer recording so set menu flag to enabled
        useMenu = true;
        if (android.os.Build.VERSION.SDK_INT >= 11)
            invalidateOptionsMenu();

        // Enabled the Recording Interval Button
        rcrdIntervalB.setEnabled(true);

        // Reset the text on the main button
        startStopB.setText(getString(R.string.start_prompt));
    }

    /**
     * Set layout up for recording
     */
    void setLayoutRecording() {
        //Swap the layouts below the recording button
        nameAndLoginRL.setVisibility(View.GONE);
        recordingExtrasLL.setVisibility(View.VISIBLE);

        // Recording so set menu flag to disabled
        useMenu = false;
        if (android.os.Build.VERSION.SDK_INT >= 11)
            invalidateOptionsMenu();

        // Disable the Recording Interval Button
        rcrdIntervalB.setEnabled(false);

        // Change the text on the main button
		startStopB.setText(getString(R.string.stop_prompt));

        // Reset the main UI text boxes
        nameB.setText(firstName + " " + lastInitial);
        pointsUploadedTV.setText("Points Recorded: " + "0");
        elapsedTimeTV.setText("Time Elapsed:" + " 0 seconds");
    }

	/**
	 * Is called every time this activity is paused. For example, whenever a new
	 * dialog is launched.
	 */
	@Override
	public void onPause() {
		super.onPause();

		// Stop recording
		if (recordTimer != null)
			recordTimer.cancel();
		recordTimer = null;

		// Stop updating GPS
		if (gpsTimer != null)
			gpsTimer.cancel();
		gpsTimer = null;
	}

	/**
	 * Called whenever this application is left, like when the user switches
	 * apps using the task manager.
	 */
	@Override
	public void onStop() {
		super.onStop();

		// Stops the GPS and accelerometer when the application is not
		// recording.
		if (!Datawalk_Service.running) {
			if (mLocationManager != null)
				mLocationManager.removeUpdates(DataWalk.this);

			if (mSensorManager != null)
				mSensorManager.unregisterListener(DataWalk.this);
		}

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

    }


    @Override
    protected void onDestroy() {
        if(isFinishing() && Datawalk_Service.running) {
            setLayoutNotRecording();
            stopService(service);
        }

        super.onDestroy();
    }

    /**
	 * Called whenever this activity is called from within the application, like
	 * from the login dialog.
	 */
	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume();

		// We are going to do a series of tasks depending on whether or not we
		// have connectivity
		// if we have connectivity: user can change ProjectNumber, set it
		// originally to the user's last choice, and automatically login
		if (Connection.hasConnectivity(this)) {
			if (android.os.Build.VERSION.SDK_INT >= 11)
				invalidateOptionsMenu();
		}

		// Get the last know recording interval
		mInterval = Integer.parseInt(getSharedPreferences(INTERVAL_PREFS_KEY,
				Context.MODE_PRIVATE).getString(INTERVAL_VALUE_KEY,
				DEFAULT_INTERVAL + ""));

		// Rebuild the upload queue
		if (uq != null)
			uq.buildQueueFromFile();

		// Restart the GPS counter
		if (mLocationManager != null)
			initLocationManager();
		if (gpsTimer == null)
			waitingForGPS();

		// Update the text in the text boxes on the main UI
		if (projectID == "-1") {
			projNumB.setText(getResources().getString(R.string.project_num));
		} else {
			projNumB.setText("Project: " + projectID);
		}
		if (mInterval == 1000) {
			rcrdIntervalB.setText("1 second");
		} else if (mInterval == 60000) {
			rcrdIntervalB.setText("1 minute");
		} else {
			rcrdIntervalB.setText(mInterval / 1000 + " seconds");
		}

	}// ends onResume

	@Override
	protected void onStart() {
		// Log in automatically
		CredentialManager.login(this, api);

        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(Datawalk_Service.DATAWALK_RESULT));

        super.onStart();
	}

    /**
	 * Sets the project Id to the one the user specified.
	 */
	private void setProjectIDFromSetupClass() {
		// get the projectID from Setup
		SharedPreferences setupPrefs = getSharedPreferences(
				Setup.PROJ_PREFS_ID, Context.MODE_PRIVATE);
		if (useDev)
			projectID = setupPrefs.getString(Setup.PROJECT_ID,
					DEFAULT_PROJECT_DEV);
		else
			projectID = setupPrefs.getString(Setup.PROJECT_ID, DEFAULT_PROJECT);
	}

	/**
	 * sets the project ID to local prefs
	 */
	private void setProjectIDForLocalPrefs() {
		// set the ID to our local prefs
		SharedPreferences localPrefs = getSharedPreferences(PROJ_PREFS_KEY,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor mEdit = localPrefs.edit();
		if (useDev)
			mEdit.putString(PROJ_ID_DEV, projectID);
		else
			mEdit.putString(PROJ_ID_PRODUCTION, projectID);
		mEdit.commit();
	}

	/**
	 * Receives location updates from the location manager.
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (location.getLatitude() != 0 && location.getLongitude() != 0) {
			loc = location;
			gpsWorking = true;
		} else {
			// Rajia will that fix the random velocity problem
			prevLoc.set(loc);
			gpsWorking = false;
			gpsWaitingCounter = 0;
		}
	}

	/**
	 * Location manager not receiving updates anymore.
	 */
	@Override
	public void onProviderDisabled(String provider) {
		gpsWorking = false;
		gpsWaitingCounter = 0;
	}

	/**
	 * Location manager starts receiving updates.
	 */
	@Override
	public void onProviderEnabled(String provider) {
	}

	/**
	 * Called when the provider status changes.
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	/**
	 * Used to find out what version of Android you are using.
	 * 
	 * @return API level of current device
	 */
	public static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	/**
	 * Called to create the menu from your menu.xml file.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the layout from menu.xml
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * Turns the action bar menu on and off.
	 * 
	 * @return Whether or not the menu was prepared successfully.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (!useMenu) {
			menu.getItem(0).setEnabled(false);
			menu.getItem(1).setEnabled(false);
			menu.getItem(2).setEnabled(false);
			menu.getItem(3).setEnabled(false);
		} else {
			menu.getItem(0).setEnabled(true);
			menu.getItem(1).setEnabled(true);
			menu.getItem(2).setEnabled(true);
			menu.getItem(3).setEnabled(true);
		}
		return true;
	}

	/**
	 * Tries to login then launches the upload queue uploading activity.
	 */
	private void manageUploadQueue() {
		// If the queue isn't empty, launch the activity. Otherwise tell the
		// user the queue is empty.
		if (!uq.emptyQueue()) {
			Intent i = new Intent().setClass(mContext, QueueLayout.class);
			i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
			startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
		} else {
			w.make("No Data to Upload.", Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
		}
	}

	/**
	 * Sets up the locations manager so that it request GPS permission if
	 * necessary and gets only the most accurate points.
	 */
	private void initLocationManager() {
		// Set the criteria to points with fine accuracy
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

        boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// Start the GPS listener
        mLocationManager.addGpsStatusListener(this);

        // Check if GPS is enabled. If not, direct user to their settings.
        mLocationManager.requestLocationUpdates(
                mLocationManager.getBestProvider(criteria, true), 0, 0,
                DataWalk.this);

		// Save new GPS points in our loc variable
		loc = new Location(mLocationManager.getBestProvider(criteria, true));
		// Rajia
		prevLoc = loc;
		firstLoc = loc;
	}

	/**
	 * Starts a timer that displays gps points when they are found and the
	 * waiting for gps loop when they are not.
	 */
	private void waitingForGPS() {

		// Creates the new timer to update the main UI every second
		gpsTimer = new Timer();
		gpsTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						// Show the GPS coordinate on the main UI, else continue
						// with our loop.
						if (gpsWorking) {
							latitudeTV.setText(getResources().getString(
									R.string.latitude)
									+ " " + loc.getLatitude());
							longitudeTV.setText(getResources().getString(
									R.string.longitude)
									+ " " + loc.getLongitude());
						} else {
							switch (gpsWaitingCounter % 5) {
							case (0):
								latitudeTV.setText(R.string.latitude);
								longitudeTV.setText(R.string.longitude);
								break;
							default:
								String latitude = (String) latitudeTV.getText();
								String longitude = (String) longitudeTV
										.getText();
								latitudeTV.setText(latitude + " .");
								longitudeTV.setText(longitude + " .");
								break;
							}
							gpsWaitingCounter++;
						}
					}
				});
			}
		}, 0, TIMER_LOOP);
	}

	/**
	 * Initializes managers.
	 */
	private void initManagers() {

	        // Waffles
        w = new Waffle(mContext);

		// iSENSE API
		api = API.getInstance();
		api.useDev(useDev);

		// Upload Queue
		uq = new UploadQueue("data_walk", mContext, api);
		uq.buildQueueFromFile();

		// Vibrator for Long Click
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// GPS
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		initLocationManager();
		waitingForGPS();

		// Sensors
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		// Beep sound
		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);

	}

	/**
	 * Catches the returns from other activities back to DataWalk.java.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// If the user hits yes, launch a web page to view their data on iSENSE,
		// else do nothing.
		if (requestCode == DIALOG_VIEW_DATA) {

			if (resultCode == RESULT_OK) {
				projectURL += dataSetID + "?embed=true";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(projectURL));
				startActivity(i);
			}

			// If a new project has been selected, check to see if it is
			// actually valid.
		} else if (requestCode == PROJECT_REQUESTED) {

			if (Connection.hasConnectivity(mContext)) {
				if (resultCode == RESULT_OK) {
					setProjectIDFromSetupClass();
					new GetProjectTask().execute();
				}
			} else {
				// There is no Internet so we cannot pull fields from
				// iSENSE.
				// IMPORTANT -- Inform the user of this situation and act
				// appropriately
			}

			// If the user hit yes, bring them to GPS settings.
		} else if (requestCode == DIALOG_NO_GPS) {
			if (resultCode == RESULT_OK) {
				startActivity(new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}

			// The user left the app while it was running, so press the main
			// button to stop recording.
		} else if (requestCode == DIALOG_FORCE_STOP) {
			if (resultCode == RESULT_OK) {
				startStopB.performLongClick();
			}

			// If the user uploaded data, offer to show the data on iSENSE
		} else if (requestCode == QUEUE_UPLOAD_REQUESTED) {
			uq.buildQueueFromFile();
			if (resultCode == RESULT_OK) {
				if (data != null) {
					dataSetID = data.getIntExtra(
							QueueLayout.LAST_UPLOADED_DATA_SET_ID, -1);
				}
			}

			// Return of EnterNameActivity
		} else if (requestCode == NAME_REQUESTED) {

			if (resultCode == RESULT_OK) {
				SharedPreferences namePrefs = getSharedPreferences(
						EnterName.PREFERENCES_KEY_USER_INFO, MODE_PRIVATE);

				if (namePrefs
						.getBoolean(
								EnterName.PREFERENCES_USER_INFO_SUBKEY_USE_ACCOUNT_NAME,
								true)) {
					RPerson user = api.getCurrentUser();

					firstName = user.name;
					lastInitial = "";

					nameB.setText(firstName);

				} else {
					firstName = namePrefs.getString(
							EnterName.PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME,
							"");
					lastInitial = namePrefs
							.getString(
									EnterName.PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL,
									"");

					nameB.setText(firstName + " " + lastInitial);
				}

			} else {
				if (firstName.equals("") || lastInitial.equals("")) {
					Intent iEnterName = new Intent(this, EnterName.class);
					SharedPreferences classPrefs = getSharedPreferences(
							ClassroomMode.PREFS_KEY_CLASSROOM_MODE, 0);
					iEnterName.putExtra(EnterName.PREFERENCES_CLASSROOM_MODE,
							classPrefs.getBoolean(
									ClassroomMode.PREFS_BOOLEAN_CLASSROOM_MODE,
									true));
					startActivityForResult(iEnterName, NAME_REQUESTED);
					w.make("You must enter your name before starting to record data.",
							Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
				}
			}

			// Resets iSENSE and recording variables to their defaults.
		} else if (requestCode == RESET_REQUESTED) {
                Log.e("HERE","HERE");
				// Set variables to default
				mInterval = 10000;
				firstName = "";
				lastInitial = "";

                SharedPreferences mPrefs = mContext
                        .getSharedPreferences(EnterName.PREFERENCES_KEY_USER_INFO,
                                MODE_PRIVATE);
                SharedPreferences.Editor nameEdit = mPrefs.edit();

                nameEdit.putString(
                        EnterName.PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME,
                        firstName).commit();
                nameEdit.putString(
                        EnterName.PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL,
                        lastInitial)
                        .commit();


				rcrdIntervalB.setText("10 seconds");
                SharedPreferences sp = getSharedPreferences(
                        DataWalk.INTERVAL_PREFS_KEY, Context.MODE_PRIVATE);


                SharedPreferences.Editor editor = sp.edit();
                editor.putString(DataWalk.INTERVAL_VALUE_KEY, "10000" )
                        .commit();

				if (useDev)
					projectID = DEFAULT_PROJECT_DEV;
				else
					projectID = DEFAULT_PROJECT;

				// Set the project ID in preferences back to its default value
				SharedPreferences prefs = getSharedPreferences(PROJ_PREFS_KEY,
						Context.MODE_PRIVATE);
				SharedPreferences.Editor mEdit = prefs.edit();
				mEdit.putString(PROJ_ID_PRODUCTION, DEFAULT_PROJECT);
				mEdit.putString(PROJ_ID_DEV, DEFAULT_PROJECT_DEV);
				mEdit.commit();

				
				// Tell the user his settings are back to default
				w.make("Settings have been reset to default.",
						Waffle.LENGTH_SHORT);

				// Launch the EnterNameActivity
				Intent iEnterName = new Intent(this, EnterName.class);
				SharedPreferences classPrefs = getSharedPreferences(
						ClassroomMode.PREFS_KEY_CLASSROOM_MODE, 0);
				iEnterName.putExtra(EnterName.PREFERENCES_CLASSROOM_MODE,
						classPrefs.getBoolean(
								ClassroomMode.PREFS_BOOLEAN_CLASSROOM_MODE,
								true));
				startActivityForResult(iEnterName, NAME_REQUESTED);

			// Catches return from LoginIsense.java
		} else if (requestCode == LOGIN_ISENSE_REQUESTED) {

		}

	}// End of onActivityResult

	/**
	 * Is called when the accuracy of the accelerometer sensor changes.
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	/**
	 * Is called whenever a new point is received from the accelerometer.
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		// x acceleration in accel[0]
		// y acceleration in accel[1]
		// z acceleration in accel[2]
		// magnitude of total acceleration in accel[3]
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accel[0] = event.values[0];
			accel[1] = event.values[1];
			accel[2] = event.values[2];
			accel[3] = (float) Math.sqrt((float) (Math.pow(accel[0], 2)
					+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2)));
		}
	}

	/**
	 * Performs the code for whenever a menu button is clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.reset:
			// Launch the dialog asking if the user is sure he/she wants to
			// reset to default settings
			Intent i = new Intent(mContext, Reset.class);
			startActivityForResult(i, RESET_REQUESTED);
			return true;

		case R.id.About:
			// Shows the about dialog
			startActivity(new Intent(this, About.class));
			return true;

		case R.id.help:
			// Shows the help dialog
			startActivity(new Intent(this, Help.class));
			return true;

		case R.id.classroom:
			// Shows the classroom settings dialog
			startActivity(new Intent(this, ClassroomMode.class));
			return true;

        case R.id.Login:
            startActivityForResult(new Intent(getApplicationContext(),
            CredentialManager.class), LOGIN_ISENSE_REQUESTED);

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

				if (cdt != null)
					cdt.cancel();

				if (api.getCurrentUser() != null) {
					Runnable r = new Runnable() {
						public void run() {
							api.deleteSession();
							api.useDev(useDev);
						}
					};
					new Thread(r).start();
				} else
					api.useDev(useDev);

				actionBarTapCount = 0;
				break;
			}

			return true;
		}

		return false;

	}// Ends on options item selected

	/**
	 * Checks to see if the last entered project number is valid.
	 * 
	 * @author Rajia
	 */
	public class GetProjectTask extends AsyncTask<Void, Integer, Void> {

		RProject proj;

		/**
		 * Tries to get the project from iSENSE.
		 */
		@Override
		protected Void doInBackground(Void... arg0) {

			// Get the project from iSENSE
			proj = api.getProject(Integer.parseInt(projectID));

			return null;
		}

		/**
		 * Called once you've finished trying to get the project from iSENSE.
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// If the project is invalid, tell the user and launch the project
			// picking dialog.
			if (proj.name == null || proj.name.equals("")) {
				w.make("Project Number Invalid! Please enter a new one.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);

				startActivityForResult(new Intent(mContext, Setup.class),
						PROJECT_REQUESTED);
			} else {
				setProjectIDFromSetupClass();
				setProjectIDForLocalPrefs();
			}
		}

	}

	/**
	 * Tracks the current GPS status. Is called when the GPS is started,
	 * stopped, and receives updates.
	 */
	@Override
	public void onGpsStatusChanged(int event) {

		// Count the current number of satellites in contact with the GPS
		GpsStatus status = mLocationManager.getGpsStatus(null);
		int count = 0;
		Iterable<GpsSatellite> sats = status.getSatellites();
		for (Iterator<GpsSatellite> i = sats.iterator(); i.hasNext(); i.next()) {
			count++;
		}

		// If there are 3 or fewer satellites that we are connected, we've
		// probably lost GPS a lock
		if (count < 4) {
			if (gpsWorking == true) {
				w.make("Weak GPS signal.", Waffle.LENGTH_LONG,
						Waffle.IMAGE_WARN);
				gpsWaitingCounter = 0;
			}
			gpsWorking = false;
			prevLoc.set(loc);
		}
	}



	// formats numbers to 2 decimal points
	double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}

}
