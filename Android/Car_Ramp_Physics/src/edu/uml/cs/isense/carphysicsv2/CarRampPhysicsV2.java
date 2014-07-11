/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII               iSENSE Car Ramp Physics App                 SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                    By: Michael Stowell                       SSS                **/
/**           III                    and Virinchi Balabhadrapatruni           SSS                 **/
/**           III                    Some Code From: iSENSE Amusement Park      SSS               **/
/**           III                                    App (John Fertita)          SSSSSSSSS        **/
/**           III                    Faculty Advisor:  Fred Martin                      SSS       **/
/**           III                    Group:            ECG,                              SSS      **/
/**           III                                      iSENSE                           SSS       **/
/**      IIIIIIIIIIIII               Property:         UMass Lowell              SSSSSSSSS        **/
/**                                                                                               **/
/***************************************************************************************************/
/***************************************************************************************************/

package edu.uml.cs.isense.carphysicsv2;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import edu.uml.cs.isense.carphysicsv2.dialogs.About;
import edu.uml.cs.isense.carphysicsv2.dialogs.ContributorKeyDialog;
import edu.uml.cs.isense.carphysicsv2.dialogs.Help;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.comm.uploadInfo;
import edu.uml.cs.isense.credentials.ClassroomMode;
import edu.uml.cs.isense.credentials.CredentialManager;
import edu.uml.cs.isense.credentials.EnterName;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.FieldMatching;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class CarRampPhysicsV2 extends Activity implements SensorEventListener,
		LocationListener {

	public static String projectNumber = "-1";
	public static final String DEFAULT_PROJ = "-1";
	public static boolean useDev = false;
	public static boolean promptForName = true;

	public static final String VIS_URL_PROD = "http://isenseproject.org/projects/";
	public static final String VIS_URL_DEV = "http://rsense-dev.cs.uml.edu/projects/";
	public static String baseDataSetUrl = "";
	public static String dataSetUrl = "";

	public static String RECORD_SETTINGS = "RECORD_SETTINGS";

	private Button startStop;
	private TextView values;
	public static Boolean running = false;

	private SensorManager mSensorManager;

	public static Location loc;
	private float accel[];
	private Timer timeTimer;
	private int INTERVAL = 50;

	public DataFieldManager dfm;
	public Fields f;
	public API api;

	private int countdown;

	static String firstName = "";
	static String lastInitial = "";

	public static final int RESULT_GOT_NAME = 1098;
	public static final int UPLOAD_OK_REQUESTED = 90000;
	public static final int LOGIN_STATUS_REQUESTED = 6005;
	public static final int RECORDING_LENGTH_REQUESTED = 4009;
	public static final int PROJECT_REQUESTED = 9000;
	public static final int QUEUE_UPLOAD_REQUESTED = 5000;
	public static final int RESET_REQUESTED = 6003;
	public static final int SAVE_MODE_REQUESTED = 10005;
	public static final String ACCEL_SETTINGS = "ACCEL_SETTINGS";
	private static final int FIELD_MATCHING_REQUESTED = 7498;
	private static final int ALTER_DATA_PROJ_REQUESTED = 6698;

	private boolean timeHasElapsed = false;
	private boolean usedHomeButton = false;

	private MediaPlayer mMediaPlayer;

	private int elapsedMillis = 0;

	DecimalFormat toThou = new DecimalFormat("######0.000");

	int i = 0;
	int len = 0;
	int len2 = 0;
	int length;

	ProgressDialog dia;
	double partialProg = 1.0;

	public static String nameOfDataSet = "";

	static boolean inPausedState = false;
	static boolean useMenu = true;
	static boolean setupDone = false;
	static boolean choiceViaMenu = false;
	static boolean dontToastMeTwice = false;
	static boolean exitAppViaBack = false;
	static boolean dontPromptMeTwice = false;

	private static JSONObject dataToUpload;
	
	private Handler mHandler;
	public static JSONArray dataSet;

	long currentTime;

	public static Context mContext;

	public static TextView loggedInAs;
	private Waffle w;
	public static boolean inApp = false;

	public static UploadQueue uq;

	public static Bundle saved;

	public static Menu menu;
	
	private Switch switchGravity;

    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;


    /* Action Bar */
	private static int actionBarTapCount = 0;



	/* Make sure url is updated when useDev is set. */
	void setUseDev(boolean useDev) {
		api.useDev(useDev);
		if (useDev) {
			baseDataSetUrl = VIS_URL_DEV;
		} else {
			baseDataSetUrl = VIS_URL_PROD;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		saved = savedInstanceState;
		mContext = this;

		api = API.getInstance();
		setUseDev(useDev);

		if (api.getCurrentUser() != null) {
			Runnable r = new Runnable() {
				public void run() {
					api.deleteSession();
					api.useDev(useDev);
				}
			};
			new Thread(r).start();
		}

		// Initialize action bar customization for API >= 11
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			ActionBar bar = getActionBar();

			// make the actionbar clickable
			bar.setDisplayHomeAsUpEnabled(true);
		}

        mNavigationDrawerItemTitles= getResources().getStringArray(R.array.items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_item_row, mNavigationDrawerItemTitles));

//        // Set the list's click listener
//        mDrawerList.setOnItemClickListener(new DrawerItemClickListener(){
//
//        });



        mDrawerToggle = new ActionBarDrawerToggle(
                this,                       /* host Activity */
                mDrawerLayout,              /* DrawerLayout object */
                R.drawable.ic_launcher,     /* nav drawer icon to replace 'Up' caret */
                1,                     /* "open drawer" description */
                0                     /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        f = new Fields();
		uq = new UploadQueue("carrampphysics", mContext, api);
		uq.buildQueueFromFile();

		w = new Waffle(mContext);

		CredentialManager.login(mContext, api);

		mHandler = new Handler();

		startStop = (Button) findViewById(R.id.startStop);

		values = (TextView) findViewById(R.id.values);

		SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH", 0);
		length = countdown = prefs.getInt("length", 10);

		if (savedInstanceState == null) {
			if (firstName.equals("") || lastInitial.equals("")) {
			
				Intent iEnterName = new Intent(this, EnterName.class);
				iEnterName.putExtra(EnterName.PREFERENCES_CLASSROOM_MODE,
						true);
				startActivityForResult(iEnterName, RESULT_GOT_NAME);
				
			}
		}

		SharedPreferences prefs2 = getSharedPreferences("PROJID", 0);
		projectNumber = prefs2.getString("project_id", null);
		if (projectNumber == null) {
			projectNumber = DEFAULT_PROJ;
		}

		if (!Connection.hasConnectivity(mContext)) {
			projectNumber = "-1";
		}

		dfm = new DataFieldManager(Integer.parseInt(projectNumber), api,
				mContext, f);
		dfm.getOrder();

		new DecimalFormat("#,##0.0");

        startStop.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				 
				SharedPreferences prefs = getSharedPreferences("PROJID", 0);
				projectNumber = prefs.getString("project_id", "-1");

                if (projectNumber.equals("-1")) {
                    setUpNoProject();
                }

				//if (!projectNumber.equals("-1")) {
					mMediaPlayer.setLooping(false);
					mMediaPlayer.start();
	
					if (running) {
	
						if (timeHasElapsed) {
	
							getWindow().clearFlags(
									WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	
							setupDone = false;
							timeHasElapsed = false;
							useMenu = true;
							countdown = length;
	
							running = false;
							startStop.setText("Hold to Start");
	
							timeTimer.cancel();
							choiceViaMenu = false;
	
							startStop.setEnabled(true);
							startStop
									.setBackgroundResource(R.drawable.button_rsense);
	
							/*Intent dataIntent = new Intent(mContext,
									DataActivity.class);
							startActivityForResult(dataIntent, UPLOAD_OK_REQUESTED);
							 */
							if (len == 0 || len2 == 0) {
								w.make("There are no data to upload!", Waffle.LENGTH_LONG,
										Waffle.IMAGE_X);
								OrientationManager.enableRotation(CarRampPhysicsV2.this);
							}
							else{
								new AddToQueueTask().execute(); 

								
							}
						} else if (usedHomeButton) {
							setupDone = false;
							timeHasElapsed = false;
							useMenu = true;
							countdown = length;
	
							running = false;
							startStop.setText("Hold to Start");
	
							timeTimer.cancel();
							choiceViaMenu = false;
	
							startStop.setEnabled(true);
							startStop
									.setBackgroundResource(R.drawable.button_rsense);
	
						}
	
					} else {
	
						OrientationManager.disableRotation(CarRampPhysicsV2.this);
						getWindow().addFlags(
								WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	
						startStop.setEnabled(false);
						startStop
								.setBackgroundResource(R.drawable.button_rsense_green);
	
						dataSet = new JSONArray();
						elapsedMillis = 0;
						len = 0;
						len2 = 0;
						i = 0;
						currentTime = getUploadTime(0);
	
						setEnabledFields();
	
//						if (saveMode) {
//							dfm.getOrder();
//						}

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							w.make("Data recording has offset 100 milliseconds due to an error.",
									Waffle.LENGTH_SHORT);
							e.printStackTrace();
						}
	
						useMenu = false;
	
						SharedPreferences prefs2 = getSharedPreferences(
								ACCEL_SETTINGS, 0);
						if (mSensorManager != null) {
							boolean isLinear = prefs2.getBoolean("LINEAR_ACCEL",
									false);
							if (isLinear) {
								mSensorManager.registerListener(
										CarRampPhysicsV2.this,
										mSensorManager
												.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
										SensorManager.SENSOR_DELAY_FASTEST);
							} else {
								mSensorManager.registerListener(
										CarRampPhysicsV2.this,
										mSensorManager
												.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
										SensorManager.SENSOR_DELAY_FASTEST);
							}
						}
	
						running = true;
						startStop.setText("" + countdown);
	
						timeTimer = new Timer();
						timeTimer.scheduleAtFixedRate(new TimerTask() {
	
							public void run() {
	
								elapsedMillis += INTERVAL;
	
								if (i >= (length * (1000 / INTERVAL))) {
	
									timeTimer.cancel();
									timeHasElapsed = true;
	
									mHandler.post(new Runnable() {
										@Override
										public void run() {
											startStop.performLongClick();
										}
									});
	
								} else {
	
									i++;
									len++;
									len2++;
	
									if (i % (1000 / INTERVAL) == 0) {
										mHandler.post(new Runnable() {
											@Override
											public void run() {
												startStop.setText("" + countdown);
											}
										});
										countdown--;
									}
	
									f.timeMillis = currentTime + elapsedMillis;
	
									if (dfm.getOrderList().contains(
											mContext.getString(R.string.accel_x))) {
										f.accel_x = toThou.format(accel[0]);
									}
									if (dfm.getOrderList().contains(
											mContext.getString(R.string.accel_y))) {
										f.accel_y = toThou.format(accel[1]);
									}
									if (dfm.getOrderList().contains(
											mContext.getString(R.string.accel_z))) {
										f.accel_z = toThou.format(accel[2]);
									}
									if (dfm.getOrderList()
											.contains(
													mContext.getString(R.string.accel_total))) {
										f.accel_total = toThou.format(accel[3]);
									}
	
									dataSet.put(dfm.putData());
	
								}
	
							}
						}, 0, INTERVAL);
	
					}
	
					if (android.os.Build.VERSION.SDK_INT >= 11) {
						CarRampPhysicsV2.this.invalidateOptionsMenu();
					}
	
					return running;
				
	
//				} else {
//					w.make("No Project Selected",Waffle.LENGTH_LONG, Waffle.IMAGE_X);
//					return timeHasElapsed;
//				}
			}
		});

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		SharedPreferences prefs3 = getSharedPreferences(ACCEL_SETTINGS, 0);
		if (mSensorManager != null) {
			boolean isLinear = prefs3.getBoolean("LINEAR_ACCEL", false);
			if (CarRampPhysicsV2.getApiLevel() < 14) {
				// If the device isn't on Jelly Bean
				ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton1);
				button.setChecked(isLinear);
				button.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						changeSensors(buttonView);
					}
					
				});
				
			} else {
				switchGravity = (Switch) findViewById(R.id.switch1);
				switchGravity.setChecked(isLinear);
				
				switchGravity.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						changeSensors(buttonView);						
					}
					
				});
				
				
			}
			if (isLinear) {
				mSensorManager
						.registerListener(
								CarRampPhysicsV2.this,
								mSensorManager
										.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
								SensorManager.SENSOR_DELAY_FASTEST);

			} else {
				mSensorManager.registerListener(CarRampPhysicsV2.this,
						mSensorManager
								.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_FASTEST);
			}
			mSensorManager
					.registerListener(CarRampPhysicsV2.this, mSensorManager
							.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
							SensorManager.SENSOR_DELAY_FASTEST);
		}

		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		accel = new float[4];
		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);
		

	}

	@SuppressLint("NewApi")
	public void changeSensors(CompoundButton view) {
		mSensorManager.unregisterListener(CarRampPhysicsV2.this, mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
		mSensorManager.unregisterListener(CarRampPhysicsV2.this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

		boolean on;

		if (CarRampPhysicsV2.getApiLevel() < 14) {
			// If the device isn't on Jelly Bean
			on = ((ToggleButton) view).isChecked();
		} else {
			// the device is on Jelly Bean
			on = ((Switch) view).isChecked();
		}

		// Determine if normal or linear acceleration
		if (on) {
            mSensorManager.registerListener(CarRampPhysicsV2.this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_FASTEST);
		} else {
            mSensorManager.registerListener(CarRampPhysicsV2.this,
                    mSensorManager
                            .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                    SensorManager.SENSOR_DELAY_FASTEST);
		}

		SharedPreferences prefs = getSharedPreferences(ACCEL_SETTINGS, 0);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("LINEAR_ACCEL", on);
		editor.commit();

	}

	private void setEnabledFields() {
		if (dfm.getOrderList().contains(mContext.getString(R.string.accel_x)))
			dfm.enabledFields[Fields.ACCEL_X] = true;
		if (dfm.getOrderList().contains(mContext.getString(R.string.accel_y)))
			dfm.enabledFields[Fields.ACCEL_Y] = true;
		if (dfm.getOrderList().contains(mContext.getString(R.string.accel_z)))
			dfm.enabledFields[Fields.ACCEL_Z] = true;
		if (dfm.getOrderList().contains(
				mContext.getString(R.string.accel_total)))
			dfm.enabledFields[Fields.ACCEL_TOTAL] = true;
		dfm.enabledFields[Fields.TIME] = true;
	}

	long getUploadTime(int millisecond) {

		Calendar c = Calendar.getInstance();

		return (long) (c.getTimeInMillis());

	}

	@Override
	public void onPause() {
		super.onPause();
		if (timeTimer != null)
			timeTimer.cancel();
		inPausedState = true;

	}

	@Override
	public void onStop() {
		super.onStop();
		if (timeTimer != null)
			timeTimer.cancel();
		inPausedState = true;
		mSensorManager.unregisterListener(CarRampPhysicsV2.this, mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));

	}

	@Override
	public void onStart() {
		super.onStart();
		inPausedState = false;
		SharedPreferences prefs3 = getSharedPreferences(ACCEL_SETTINGS, 0);
		boolean isLinear = prefs3.getBoolean("LINEAR_ACCEL", false);
		if (isLinear) {
			mSensorManager.registerListener(CarRampPhysicsV2.this,
					mSensorManager
							.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
					SensorManager.SENSOR_DELAY_FASTEST);
		} else {
			mSensorManager.registerListener(CarRampPhysicsV2.this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_FASTEST);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		inPausedState = false;
		getSharedPreferences(RECORD_SETTINGS, 0);

		if (usedHomeButton && running) {
			setupDone = false;
			timeHasElapsed = false;
			useMenu = true;
			countdown = length;

			running = false;
			startStop.setText("Hold to Start");
			startStop.setEnabled(true);
			startStop.setBackgroundResource(R.drawable.button_rsense);

			timeTimer.cancel();
			choiceViaMenu = false;
			startStop.setEnabled(true);
			dataSet = new JSONArray();
			OrientationManager.enableRotation(CarRampPhysicsV2.this);

			menu.setGroupVisible(0, true);
			useMenu = true;

			w.make("Data recording halted.", Waffle.LENGTH_SHORT,
					Waffle.IMAGE_X);
		}

		if (uq != null)
			uq.buildQueueFromFile();

		SharedPreferences prefs2 = getSharedPreferences("PROJID", 0);
		projectNumber = prefs2.getString("project_id", null);
		if (projectNumber == null) {
				projectNumber = DEFAULT_PROJ;
		}

		if (!Connection.hasConnectivity(mContext)) {
			projectNumber = "-1";
		}

		dfm = new DataFieldManager(Integer.parseInt(projectNumber), api,
				mContext, f);
		dfm.getOrder();

		new DecimalFormat("#,##0.0");

		if (dfm.getOrderList().contains(mContext.getString(R.string.accel_x))) {
			values.setText("X: ");
		}

		if (dfm.getOrderList().contains(mContext.getString(R.string.accel_y))) {
			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_x))) {
				values.setText(values.getText() + " Y: ");
			} else {
				values.setText("Y: ");
			}
		}

		if (dfm.getOrderList().contains(mContext.getString(R.string.accel_z))) {
			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_x))
					|| dfm.getOrderList().contains(
							mContext.getString(R.string.accel_y))) {
				values.setText(values.getText() + " Z: ");
			} else {
				values.setText("Z: ");
			}

		}

		if (dfm.getOrderList().contains(
				mContext.getString(R.string.accel_total))) {

			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_x))
					|| dfm.getOrderList().contains(
							mContext.getString(R.string.accel_y))
					|| dfm.getOrderList().contains(
							mContext.getString(R.string.accel_z))) {
				values.setText(values.getText() + " Magnitude: ");
			} else {
				values.setText("Magnitude: ");
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.setGroupEnabled(0, useMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

		switch (item.getItemId()) {
		case R.id.login:
			startActivityForResult(new Intent(this, CredentialManager.class),
					LOGIN_STATUS_REQUESTED);
			return true;
		case R.id.project_select:
			Intent setup = new Intent(this, Setup.class);
			setup.putExtra("constrictFields", true);
			setup.putExtra("app_name", "CRP");
			startActivityForResult(setup, PROJECT_REQUESTED);
			return true;
		case R.id.upload:
			manageUploadQueue();
			return true;
		case R.id.record_length:
			createSingleInputDialog("Change Recording Length", "",
					RECORDING_LENGTH_REQUESTED);
			return true;
		case R.id.changename:
			Intent iEnterName = new Intent(mContext, EnterName.class);
			SharedPreferences classPrefs = getSharedPreferences(
					ClassroomMode.PREFS_KEY_CLASSROOM_MODE, 0);
			iEnterName.putExtra(EnterName.PREFERENCES_CLASSROOM_MODE,
					classPrefs.getBoolean(
							ClassroomMode.PREFS_BOOLEAN_CLASSROOM_MODE, true));
			startActivityForResult(iEnterName, RESULT_GOT_NAME);
			return true;
		case R.id.reset:
			startActivityForResult(new Intent(this, ResetToDefaults.class),
					RESET_REQUESTED);
			return true;
		case R.id.about_app:
			startActivity(new Intent(this, About.class));
			return true;
		case R.id.helpMenuItem:
			startActivity(new Intent(this, Help.class));
			return true;
		case android.R.id.home:





//			CountDownTimer cdt = null;
//
//			// Give user 10 seconds to switch dev/prod mode
//			if (actionBarTapCount == 0) {
//				cdt = new CountDownTimer(5000, 5000) {
//					public void onTick(long millisUntilFinished) {
//					}
//
//					public void onFinish() {
//						actionBarTapCount = 0;
//					}
//				}.start();
//			}
//
//			String other = (useDev) ? "production" : "dev";
//
//			switch (++actionBarTapCount) {
//			case 5:
//				w.make(getResources().getString(R.string.two_more_taps) + other
//						+ getResources().getString(R.string.mode_type));
//				break;
//			case 6:
//				w.make(getResources().getString(R.string.one_more_tap) + other
//						+ getResources().getString(R.string.mode_type));
//				break;
//			case 7:
//				w.make(getResources().getString(R.string.now_in_mode) + other
//						+ getResources().getString(R.string.mode_type));
//				useDev = !useDev;
//
//				if (cdt != null)
//					cdt.cancel();
//
//				if (api.getCurrentUser() != null) {
//					Runnable r = new Runnable() {
//						public void run() {
//							api.deleteSession();
//							api.useDev(useDev);
//						}
//					};
//					new Thread(r).start();
//				} else
//					setUseDev(useDev);
//
//				actionBarTapCount = 0;
//				break;
//			}

			return true;
		}

		return false;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		DecimalFormat oneDigit = new DecimalFormat("#,##0.0");

		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION
				|| event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			accel[0] = event.values[0];
			accel[1] = event.values[1];
			accel[2] = event.values[2];
			accel[3] = (float) Math.sqrt(Math.pow(accel[0], 2)
					+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2));
			String xPrepend, yPrepend, zPrepend, data = "";

			xPrepend = accel[0] > 0 ? "+" : "";
			yPrepend = accel[1] > 0 ? "+" : "";
			zPrepend = accel[2] > 0 ? "+" : "";

			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_x))) {
				data = "X: " + xPrepend + oneDigit.format(accel[0]);
			}
			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_y))) {
				if (!data.equals("")) {
					data += " , Y: " + yPrepend + oneDigit.format(accel[1]);
				} else {
					data += "Y: " + yPrepend + oneDigit.format(accel[1]);
				}
			}
			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_z))) {
				if (!data.equals("")) {
					data += " , Z: " + zPrepend + oneDigit.format(accel[2]);
				} else {
					data += "Z: " + zPrepend + oneDigit.format(accel[2]);
				}
			}

			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_total))) {

				if (!data.equals("")) {
					data += " , Magnitude: " + oneDigit.format(accel[3]);
				} else {
					data += "Magnitude: " + oneDigit.format(accel[3]);
				}

			}

			values.setText(data);

		}
	}

	@Override
	public void onLocationChanged(Location location) {
		loc = location;
	}

	public static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		dontPromptMeTwice = false;

		if (reqCode == PROJECT_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences prefs = getSharedPreferences("PROJID", 0);
				projectNumber = prefs.getString("project_id", null);
				if (projectNumber == null) {
					projectNumber = DEFAULT_PROJ;
				}
				dfm = new DataFieldManager(Integer.parseInt(projectNumber),
						api, mContext, f);
				dfm.getOrder();

				DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_x))) {
					values.setText("X: " + oneDigit.format(accel[0]));
				}
				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_y))) {
					if (dfm.getOrderList().contains(
							mContext.getString(R.string.accel_x))) {
						values.setText(values.getText() + " Y: "
								+ oneDigit.format(accel[1]));
					} else {
						values.setText("Y: " + oneDigit.format(accel[1]));
					}
				}
				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_z))) {
					if (dfm.getOrderList().contains(
							mContext.getString(R.string.accel_x))
							|| dfm.getOrderList().contains(
									mContext.getString(R.string.accel_y))) {
						values.setText(values.getText() + " Z: "
								+ oneDigit.format(accel[2]));
					} else {
						values.setText("Z: " + oneDigit.format(accel[2]));
					}

				}
				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_total))) {
					accel[3] = (float) Math.sqrt(Math.pow(accel[0], 2)
							+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2));

					if (dfm.getOrderList().contains(
							mContext.getString(R.string.accel_x))
							|| dfm.getOrderList().contains(
									mContext.getString(R.string.accel_y))
							|| dfm.getOrderList().contains(
									mContext.getString(R.string.accel_z))) {
						values.setText(values.getText() + " Magnitude: "
								+ oneDigit.format(accel[3]));
					} else {
						values.setText("Magnitude: "
								+ oneDigit.format(accel[3]));
					}

				}
			}

		} else if (reqCode == QUEUE_UPLOAD_REQUESTED) {
			uq.buildQueueFromFile();

		} /*else if (reqCode == UPLOAD_OK_REQUESTED) {
				if (len == 0 || len2 == 0) {
					w.make("There are no data to upload!", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
					OrientationManager.enableRotation(CarRampPhysicsV2.this);
				}

				else
					new UploadTask().execute();
			 
		}*/ else if (reqCode == LOGIN_STATUS_REQUESTED) {
			if (resultCode == RESULT_OK) {

			}

			dfm = new DataFieldManager(Integer.parseInt(projectNumber), api,
					mContext, f);
			dfm.getOrder();
			DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_x))) {
				values.setText("X: " + oneDigit.format(accel[0]));
			}
			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_y))) {
				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_x))) {
					values.setText(values.getText() + " Y: "
							+ oneDigit.format(accel[1]));
				} else {
					values.setText("Y: " + oneDigit.format(accel[1]));
				}
			}
			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_z))) {
				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_x))
						|| dfm.getOrderList().contains(
								mContext.getString(R.string.accel_y))) {
					values.setText(values.getText() + " Z: "
							+ oneDigit.format(accel[2]));
				} else {
					values.setText("Z: " + oneDigit.format(accel[2]));
				}

			}
			if (dfm.getOrderList().contains(
					mContext.getString(R.string.accel_total))) {
				accel[3] = (float) Math.sqrt(Math.pow(accel[0], 2)
						+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2));

				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_x))
						|| dfm.getOrderList().contains(
								mContext.getString(R.string.accel_y))
						|| dfm.getOrderList().contains(
								mContext.getString(R.string.accel_z))) {
					values.setText(values.getText() + " Magnitude: "
							+ oneDigit.format(accel[3]));
				} else {
					values.setText("Magnitude: " + oneDigit.format(accel[3]));
				}

			}

		} else if (reqCode == RECORDING_LENGTH_REQUESTED) {
			if (resultCode == RESULT_OK) {
				length = Integer.parseInt(data.getStringExtra("input"));
				countdown = length;
				SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH",
						0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("length", length);
				// Below is a math fail
				// if (length <= 25) {
				// INTERVAL = 50;
				// } else {
				// INTERVAL = 2 * length;
				// }
				editor.putInt("Interval", INTERVAL);
				editor.commit();
			}
		} else if (reqCode == RESULT_GOT_NAME) {
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


				} else {
					firstName = namePrefs.getString(
							EnterName.PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME,
							"");
					lastInitial = namePrefs
							.getString(
									EnterName.PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL,
									"");

					
				}

			}
		} else if (reqCode == RESET_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH",
						0);
				countdown = length = prefs.getInt("length", 10);

				CredentialManager.login(this, api);

				SharedPreferences eprefs = getSharedPreferences("PROJID", 0);
				SharedPreferences.Editor editor = eprefs.edit();
				projectNumber = DEFAULT_PROJ;
				editor.putString("project_id", projectNumber);
				editor.commit();
				INTERVAL = 50;

				dfm = new DataFieldManager(Integer.parseInt(projectNumber),
						api, mContext, f);
				dfm.getOrder();

				DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_x))) {
					values.setText("X: " + oneDigit.format(accel[0]));
				}
				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_y))) {
					if (dfm.getOrderList().contains(
							mContext.getString(R.string.accel_x))) {
						values.setText(values.getText() + " Y: "
								+ oneDigit.format(accel[1]));
					} else {
						values.setText("Y: " + oneDigit.format(accel[1]));
					}
				}
				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_z))) {
					if (dfm.getOrderList().contains(
							mContext.getString(R.string.accel_x))
							|| dfm.getOrderList().contains(
									mContext.getString(R.string.accel_y))) {
						values.setText(values.getText() + " Z: "
								+ oneDigit.format(accel[2]));
					} else {
						values.setText("Z: " + oneDigit.format(accel[2]));
					}

				}
				if (dfm.getOrderList().contains(
						mContext.getString(R.string.accel_total))) {
					accel[3] = (float) Math.sqrt(Math.pow(accel[0], 2)
							+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2));

					if (dfm.getOrderList().contains(
							mContext.getString(R.string.accel_x))
							|| dfm.getOrderList().contains(
									mContext.getString(R.string.accel_y))
							|| dfm.getOrderList().contains(
									mContext.getString(R.string.accel_z))) {
						values.setText(values.getText() + " Magnitude: "
								+ oneDigit.format(accel[3]));
					} else {
						values.setText("Magnitude: "
								+ oneDigit.format(accel[3]));
					}

				}
				
				Intent iEnterName = new Intent(mContext, EnterName.class);
				SharedPreferences classPrefs = getSharedPreferences(
						ClassroomMode.PREFS_KEY_CLASSROOM_MODE, 0);
				iEnterName.putExtra(EnterName.PREFERENCES_CLASSROOM_MODE,
						classPrefs.getBoolean(
								ClassroomMode.PREFS_BOOLEAN_CLASSROOM_MODE, true));
				startActivityForResult(iEnterName, RESULT_GOT_NAME);

			}
		} else if (reqCode == FIELD_MATCHING_REQUESTED) {
            if (resultCode == RESULT_OK) {
                if (FieldMatching.acceptedFields.isEmpty()) {
                    Intent iProj = new Intent(mContext, Setup.class);
                    iProj.putExtra("from_where", "main");
                    startActivityForResult(iProj, ALTER_DATA_PROJ_REQUESTED);
                } else if (!FieldMatching.compatible) {
                    Intent iProj = new Intent(mContext, Setup.class);
                    iProj.putExtra("from_where", "main");
                    startActivityForResult(iProj, ALTER_DATA_PROJ_REQUESTED);
                }
            } else if (resultCode == RESULT_CANCELED) {
                Intent iProj = new Intent(mContext, Setup.class);
                iProj.putExtra("from_where", "main");
                startActivityForResult(iProj, ALTER_DATA_PROJ_REQUESTED);
            }
//		} else if (reqCode == SAVE_MODE_REQUESTED) {
//			if (resultCode == RESULT_OK) {
//				saveMode = true;
//
//				CarRampPhysicsV2.projectNumber = "-1";
//				dfm = new DataFieldManager(Integer.parseInt(projectNumber),
//						api, mContext, f);
//				dfm.getOrder();
//				DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
//				if (dfm.getOrderList().contains(
//						mContext.getString(R.string.accel_x))) {
//					values.setText("X: " + oneDigit.format(accel[0]));
//				}
//				if (dfm.getOrderList().contains(
//						mContext.getString(R.string.accel_y))) {
//					if (dfm.getOrderList().contains(
//							mContext.getString(R.string.accel_x))) {
//						values.setText(values.getText() + " Y: "
//						values.setText(values.getText() + " Y: "
//								+ oneDigit.format(accel[1]));
//					} else {
//						values.setText("Y: " + oneDigit.format(accel[1]));
//					}
//				}
//				if (dfm.getOrderList().contains(
//						mContext.getString(R.string.accel_z))) {
//					if (dfm.getOrderList().contains(
//							mContext.getString(R.string.accel_x))
//							|| dfm.getOrderList().contains(
//									mContext.getString(R.string.accel_y))) {
//						values.setText(values.getText() + " Z: "
//								+ oneDigit.format(accel[2]));
//					} else {
//						values.setText("Z: " + oneDigit.format(accel[2]));
//					}
//
//				}
//				if (dfm.getOrderList().contains(
//						mContext.getString(R.string.accel_total))) {
//					accel[3] = (float) Math.sqrt(Math.pow(accel[0], 2)
//							+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2));
//
//					if (dfm.getOrderList().contains(
//							mContext.getString(R.string.accel_x))
//							|| dfm.getOrderList().contains(
//									mContext.getString(R.string.accel_y))
//							|| dfm.getOrderList().contains(
//									mContext.getString(R.string.accel_z))) {
//						values.setText(values.getText() + " Magnitude: "
//								+ oneDigit.format(accel[3]));
//					} else {
//						values.setText("Magnitude: "
//								+ oneDigit.format(accel[3]));
//					}
//
//				}
//			} else {
//				if (!Connection.hasConnectivity(mContext)) {
//					startActivityForResult(new Intent(mContext,
//							SaveModeDialog.class), SAVE_MODE_REQUESTED);
//				} else {
//					saveMode = false;
//				}
//			}
        }
	}

	private Runnable uploader = new Runnable() {

		@SuppressLint("NewApi")
		@Override
		public void run() {

			int dataSetID = -1;
			
			String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());


			nameOfDataSet = firstName + " " + lastInitial;
		
			uploadSuccessful = false;
			QDataSet ds = new QDataSet(nameOfDataSet + " Gravity: " + ((switchGravity.isChecked()) ? "Included" : "Not Included"),
                    currentDateTimeString,
					QDataSet.Type.DATA, dataSet.toString(), null,
					projectNumber, null);

			CarRampPhysicsV2.uq.addDataSetToQueue(ds);
				
			return;

		}

	};
	public boolean uploadSuccessful;

	/**
	 * Upload function specifically for when projID = -1 initially.
	 * 
	 * In this scenario, you'll need to provide an
	 * {@link edu.uml.cs.isense.comm.API API} instance along with an activity
	 * context.
	 * 
	 * @param api
	 *            - An instance of API
	 * @param c
	 *            - The context of the calling activity
	 * 
	 * @return The ID of the data set created on iSENSE, or -1 if the upload
	 *         failed
	 */
	public static int upload(API api, Context c) {
		if (CarRampPhysicsV2.projectNumber.equals("-1"))
			return -1;

		return upload(DataFieldManager.reOrderData(dataSet,
				CarRampPhysicsV2.projectNumber, mContext, null, null));
	}

	/**
	 * Attempts to upload data with the given information passed in through the
	 * QDataSet constructor
	 * 
	 * @return The ID of the data set created on iSENSE, or -1 if the upload
	 *         failed
	 */
	public static int upload(String obj) {

		int dataSetID = -1;

		try {
			JSONArray dataJSON = new JSONArray(obj);
			if (!(dataJSON.isNull(0))) {

				dataToUpload = new JSONObject();
				try {
					dataToUpload.put("data", dataJSON);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				dataToUpload = UploadQueue.getAPI().rowsToCols(dataToUpload);

				System.out.println("JOBJ: " + dataToUpload.toString());

                uploadInfo info = UploadQueue.getAPI().uploadDataSet(
						Integer.parseInt(projectNumber), dataToUpload, nameOfDataSet);
                dataSetID = info.dataSetId;
				System.out.println("Data set ID from Upload is: " + dataSetID);

			}
		} catch (JSONException e) {

		}

		return dataSetID;
	}

	public class UploadTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(mContext);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
			if (dia != null && dia.isShowing())
				dia.dismiss();

			len = 0;
			len2 = 0;

			if (uploadSuccessful) {
				w.make("Data upload successful.", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
			} else {
				if (api.getCurrentUser() != null) {
					w.make("Data saved.", Waffle.LENGTH_LONG, Waffle.IMAGE_CHECK);
				} else {
					Intent i = new Intent(mContext, ContributorKeyDialog.class);
					i.putExtra("ID", Integer.parseInt(CarRampPhysicsV2.projectNumber));
					i.putExtra("data", CarRampPhysicsV2.dataToUpload.toString());
					i.putExtra("name", nameOfDataSet);
					startActivity(i);
				}
			}

			OrientationManager.enableRotation(CarRampPhysicsV2.this);

		}
	}
	
	/**
	 * Uploads data to iSENSE or something.
	 * 
	 * @author jpoulin
	 */
	private class AddToQueueTask extends AsyncTask<String, Void, String> {

		ProgressDialog dia;

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(CarRampPhysicsV2.this);
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

		}
	}



	private void manageUploadQueue() {

		if (!uq.emptyQueue()) {
			Intent i = new Intent().setClass(mContext, QueueLayout.class);
			i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
			startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
		} else {
			w.make("There are no data to upload!", Waffle.LENGTH_LONG,
					Waffle.IMAGE_X);
		}
	}

	public void createMessageDialog(String title, String message, int reqCode) {

		Intent i = new Intent(mContext, MessageDialogTemplate.class);
		i.putExtra("title", title);
		i.putExtra("message", message);

		startActivityForResult(i, reqCode);

	}

	public void createSingleInputDialog(String title, String message,
			int reqCode) {

		Intent i = new Intent(mContext, SingleInputDialogTemplate.class);
		i.putExtra("title", title);
		i.putExtra("message", message);

		startActivityForResult(i, reqCode);

	}

    /**
     *
     */
    private void setUpNoProject() {
        CarRampPhysicsV2.projectNumber = "-1";
        dfm = new DataFieldManager(Integer.parseInt(projectNumber),
                api, mContext, f);
        dfm.getOrder();
        DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
        if (dfm.getOrderList().contains(
                mContext.getString(R.string.accel_x))) {
            values.setText("X: " + oneDigit.format(accel[0]));
        }
        if (dfm.getOrderList().contains(
                mContext.getString(R.string.accel_y))) {
            if (dfm.getOrderList().contains(
                    mContext.getString(R.string.accel_x))) {
                values.setText(values.getText() + " Y: "
                        + oneDigit.format(accel[1]));
            } else {
                values.setText("Y: " + oneDigit.format(accel[1]));
            }
        }
        if (dfm.getOrderList().contains(
                mContext.getString(R.string.accel_z))) {
            if (dfm.getOrderList().contains(
                    mContext.getString(R.string.accel_x))
                    || dfm.getOrderList().contains(
                            mContext.getString(R.string.accel_y))) {
                values.setText(values.getText() + " Z: "
                        + oneDigit.format(accel[2]));
            } else {
                values.setText("Z: " + oneDigit.format(accel[2]));
            }

        }
        if (dfm.getOrderList().contains(
                mContext.getString(R.string.accel_total))) {
            accel[3] = (float) Math.sqrt(Math.pow(accel[0], 2)
                    + Math.pow(accel[1], 2) + Math.pow(accel[2], 2));

            if (dfm.getOrderList().contains(
                    mContext.getString(R.string.accel_x))
                    || dfm.getOrderList().contains(
                            mContext.getString(R.string.accel_y))
                    || dfm.getOrderList().contains(
                            mContext.getString(R.string.accel_z))) {
                values.setText(values.getText() + " Magnitude: "
                        + oneDigit.format(accel[3]));
            } else {
                values.setText("Magnitude: "
                        + oneDigit.format(accel[3]));
            }

        }

    }

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

}