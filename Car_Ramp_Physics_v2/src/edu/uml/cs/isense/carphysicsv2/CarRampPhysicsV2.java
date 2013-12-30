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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.credentials.EnterName;
import edu.uml.cs.isense.credentials.Login;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class CarRampPhysicsV2 extends Activity implements SensorEventListener,
		LocationListener {

	public static String projectNumber = "12";
	public static final String DEFAULT_PROJ_PROD = "12";
	public static final String DEFAULT_PROJ_DEV = "3";
	private static final String DEFAULT_USER = "mobile";
	public static boolean useDev = true;

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

	private boolean timeHasElapsed = false;
	private boolean usedHomeButton = false;
	public static boolean saveMode = false;

	private MediaPlayer mMediaPlayer;

	private int elapsedMillis = 0;

	DecimalFormat toThou = new DecimalFormat("######0.000");

	ArrayList<Double> accelerX;
	ArrayList<Double> accelerY;
	ArrayList<Double> accelerZ;
	ArrayList<Double> acceler;

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

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		saved = savedInstanceState;
		mContext = this;

		api = API.getInstance();
		api.useDev(useDev);
		if (useDev) {
			baseDataSetUrl = VIS_URL_DEV;
		} else {
			baseDataSetUrl = VIS_URL_PROD;
		}

		accelerX = new ArrayList<Double>();
		accelerY = new ArrayList<Double>();
		accelerZ = new ArrayList<Double>();
		acceler = new ArrayList<Double>();

		f = new Fields();
		uq = new UploadQueue("carrampphysics", mContext, api);
		uq.buildQueueFromFile();

		w = new Waffle(mContext);

		// Save the default login info
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				CarRampPhysicsV2.mContext,
				CarRampPhysicsV2.mContext.getSharedPreferences(Login.PREFERENCES_KEY_OBSCURRED_USER_INFO,
						Context.MODE_PRIVATE));
		if (mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "").equals("")) {
			SharedPreferences.Editor mEdit = mPrefs.edit();
			mEdit.putString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, DEFAULT_USER).commit();
			mEdit.putString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, DEFAULT_USER).commit();
		}

		mHandler = new Handler();

		startStop = (Button) findViewById(R.id.startStop);

		values = (TextView) findViewById(R.id.values);

		SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH", 0);
		length = countdown = prefs.getInt("length", 10);

		if (savedInstanceState == null) {
			if (firstName.equals("") || lastInitial.equals("")) {
				if (!dontPromptMeTwice) {
					startActivityForResult(
							new Intent(mContext, EnterName.class),
							RESULT_GOT_NAME);
				}
			}
		}

		if (!Connection.hasConnectivity(mContext) && !saveMode) {
			startActivityForResult(new Intent(mContext, SaveModeDialog.class),
					SAVE_MODE_REQUESTED);
		}

		new OnCreateLoginTask().execute();

		loggedInAs = (TextView) findViewById(R.id.loginStatus);
		if (api.getCurrentUser() != null) {
			loggedInAs.setText(getResources().getString(R.string.logged_in_as)
					+ " " + mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "") + ", Name: "
					+ firstName + " " + lastInitial);
		} else {
			loggedInAs.setText(getResources().getString(R.string.not_logged_in)
					+ ", Name: " + firstName + " " + lastInitial);
		}
		SharedPreferences prefs2 = getSharedPreferences("PROJID", 0);
		projectNumber = prefs2.getString("project_id", null);
		if (projectNumber == null) {
			if (useDev) {
				projectNumber = DEFAULT_PROJ_DEV;
			} else {
				projectNumber = DEFAULT_PROJ_PROD;
			}
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

		startStop.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				mMediaPlayer.setLooping(false);
				mMediaPlayer.start();

				if (!Connection.hasConnectivity(mContext) && !saveMode) {
					startActivityForResult(new Intent(mContext,
							SaveModeDialog.class), SAVE_MODE_REQUESTED);
					return false;
				}

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

						Intent dataIntent = new Intent(mContext,
								DataActivity.class);
						startActivityForResult(dataIntent, UPLOAD_OK_REQUESTED);

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

					if (saveMode) {
						dfm.getOrder();
					}

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

								dataSet.put(dfm.putDataForNoProjectID());

							}

						}
					}, 0, INTERVAL);

				}

				CarRampPhysicsV2.this.invalidateOptionsMenu();

				return running;

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
			} else {
				Switch button = (Switch) findViewById(R.id.switch1);
				button.setChecked(isLinear);
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
	public void onToggleClicked(View view) {

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
					mSensorManager
							.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
					SensorManager.SENSOR_DELAY_FASTEST);
		} else {
			mSensorManager.registerListener(CarRampPhysicsV2.this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
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

	public void onUserLeaveHint() {
		super.onUserLeaveHint();
		usedHomeButton = true;
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
			if (useDev) {
				projectNumber = DEFAULT_PROJ_DEV;
			} else {
				projectNumber = DEFAULT_PROJ_PROD;
			}
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
	public void onBackPressed() {
		if (!dontToastMeTwice) {
			if (running)
				w.make(

				"Cannot exit via BACK while recording data; use HOME instead.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
			else
				w.make("Press back again to exit.", Waffle.LENGTH_SHORT);
			new NoToastTwiceTask().execute();
		} else if (exitAppViaBack && !running) {
			setupDone = false;
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		CarRampPhysicsV2.menu = menu;
		menu.setGroupEnabled(0, useMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about_app:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case R.id.login:
			startActivityForResult(
					new Intent(this, Login.class),
					LOGIN_STATUS_REQUESTED);
			return true;
		case R.id.project_select:
			Intent setup = new Intent(this, Setup.class);
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
			startActivityForResult(new Intent(this, EnterName.class),
					RESULT_GOT_NAME);
			return true;
		case R.id.reset:
			startActivityForResult(new Intent(this, ResetToDefaults.class),
					RESET_REQUESTED);
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
			/*
			 * accelerX.add(Double.valueOf(accel[0]));
			 * accelerY.add(Double.valueOf(accel[1]));
			 * accelerZ.add(Double.valueOf(accel[2]));
			 * 
			 * ArrayList<Double> velocityX = new ArrayList<Double>();
			 * ArrayList<Double> velocityY = new ArrayList<Double>();
			 * ArrayList<Double> velocityZ = new ArrayList<Double>();
			 * ArrayList<Double> velocity = new ArrayList<Double>();
			 * velocityX.add(Double.valueOf(0));
			 * velocityY.add(Double.valueOf(0));
			 * velocityZ.add(Double.valueOf(0)); double interval = 0.05; for
			 * (int i = 1; i < accelerX.size(); i++) {
			 * velocityX.add(Double.valueOf((accelerX.get(i) + accelerX .get(i -
			 * 1)) / 2 * interval + velocityX.get(i - 1)));
			 * velocityY.add(Double.valueOf((accelerY.get(i) + accelerY .get(i -
			 * 1)) / 2 * interval + velocityY.get(i - 1)));
			 * velocityZ.add(Double.valueOf((accelerZ.get(i) + accelerZ .get(i -
			 * 1)) / 2 * interval + velocityZ.get(i - 1))); }
			 * 
			 * for (int i = 0; i < velocityX.size(); i++) { velocity.add(Math
			 * .sqrt((velocityX.get(i).doubleValue() * velocityX
			 * .get(i).doubleValue()) + (velocityY.get(i).doubleValue() *
			 * velocityY .get(i).doubleValue()) +
			 * (velocityZ.get(i).doubleValue() * velocityZ
			 * .get(i).doubleValue()))); }
			 * 
			 * double avgUpTill = 0;
			 * 
			 * for (int i = 0 ; i<velocity.size(); i++) avgUpTill +=
			 * velocity.get(i).doubleValue();
			 * 
			 * avgUpTill /= velocity.size();
			 * 
			 * values.setText(data + " Velocity: " +
			 * oneDigit.format(avgUpTill));
			 */
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
					if (useDev) {
						projectNumber = DEFAULT_PROJ_DEV;
					} else {
						projectNumber = DEFAULT_PROJ_PROD;
					}
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

		} else if (reqCode == UPLOAD_OK_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (len == 0 || len2 == 0) {
					w.make("There are no data to upload!", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
					OrientationManager.enableRotation(CarRampPhysicsV2.this);
				}

				else
					new UploadTask().execute();
			} else {
				w.make("Data set discarded", Waffle.LENGTH_LONG,
						Waffle.IMAGE_WARN);
				OrientationManager.enableRotation(CarRampPhysicsV2.this);
			}
		} else if (reqCode == LOGIN_STATUS_REQUESTED) {
			if (resultCode == RESULT_OK) {
				
				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						mContext,
						mContext.getSharedPreferences(Login.PREFERENCES_KEY_OBSCURRED_USER_INFO,
								Context.MODE_PRIVATE));
				String loginName = mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
						"");
				
				if (loggedInAs == null)
					loggedInAs = (TextView) findViewById(R.id.loginStatus);
				if (api.getCurrentUser() != null) {
					loggedInAs.setText(getResources().getString(
							R.string.logged_in_as)
							+ " "
							+ loginName
							+ ", Name: " + firstName + " " + lastInitial);
				} else {
					loggedInAs.setText(getResources().getString(
							R.string.not_logged_in)
							+ ", Name: " + firstName + " " + lastInitial);
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
				
				w.make("Login successful", Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
				
			} else if (resultCode == Login.RESULT_ERROR) {
				
				startActivityForResult(new Intent(mContext, Login.class), LOGIN_STATUS_REQUESTED);
				
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
				
				if (!inApp)
					inApp = true;

				SharedPreferences namePrefs = getSharedPreferences(EnterName.PREFERENCES_KEY_USER_INFO, MODE_PRIVATE);
				SharedPreferences loginPrefs = new ObscuredSharedPreferences(
						CarRampPhysicsV2.mContext,
						CarRampPhysicsV2.mContext.getSharedPreferences(
								Login.PREFERENCES_KEY_OBSCURRED_USER_INFO, Context.MODE_PRIVATE));
				
				if (namePrefs.getBoolean(EnterName.PREFERENCES_USER_INFO_SUBKEY_USE_ACCOUNT_NAME, true)) {
					RPerson user = api.getCurrentUser();
					
					firstName = user.name;
					lastInitial = "";
					
					loggedInAs.setText(getResources().getString(
							R.string.logged_in_as)
							+ " "
							+ loginPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "")
							+ ", Name: " + firstName);
					
				} else {
					firstName = namePrefs.getString(EnterName.PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME, "");
					lastInitial = namePrefs.getString(EnterName.PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL, "");
					
					loggedInAs.setText(getResources().getString(
							R.string.logged_in_as)
							+ " "
							+ loginPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "")
							+ ", Name: " + firstName + " " + lastInitial);
				}
				
			} else {
				if (!inApp)
					finish();
			}
		} else if (reqCode == RESET_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH",
						0);
				countdown = length = prefs.getInt("length", 10);
				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						CarRampPhysicsV2.mContext,
						CarRampPhysicsV2.mContext.getSharedPreferences(
								Login.PREFERENCES_KEY_OBSCURRED_USER_INFO, Context.MODE_PRIVATE));
				SharedPreferences.Editor mOSPEdit = mPrefs.edit();
				mOSPEdit.putString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, DEFAULT_USER).commit();
				mOSPEdit.putString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, DEFAULT_USER).commit();

				new OnCreateLoginTask().execute();
				if (api.getCurrentUser() != null) {
					loggedInAs.setText(getResources().getString(
							R.string.logged_in_as)
							+ " "
							+ mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "")
							+ ", Name: " + firstName + " " + lastInitial);
				} else {
					loggedInAs.setText(getResources().getString(
							R.string.not_logged_in)
							+ ", Name: " + firstName + " " + lastInitial);
				}

				SharedPreferences eprefs = getSharedPreferences("PROJID", 0);
				SharedPreferences.Editor editor = eprefs.edit();
				if (useDev) {
					projectNumber = DEFAULT_PROJ_DEV;
				} else {
					projectNumber = DEFAULT_PROJ_PROD;
				}
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

			}
		} else if (reqCode == SAVE_MODE_REQUESTED) {
			if (resultCode == RESULT_OK) {
				saveMode = true;

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
			} else {
				if (!Connection.hasConnectivity(mContext)) {
					startActivityForResult(new Intent(mContext,
							SaveModeDialog.class), SAVE_MODE_REQUESTED);
				} else {
					saveMode = false;
				}
			}
		}
	}

	private Runnable uploader = new Runnable() {

		@Override
		public void run() {

			int dataSetID = -1;

			nameOfDataSet = firstName + " " + lastInitial;

			if (Connection.hasConnectivity(mContext)) {

				dataSetID = CarRampPhysicsV2.upload(api, mContext);
				
				if (dataSetID != -1) {
					dataSetUrl = baseDataSetUrl + projectNumber
							+ "/data_sets/" + dataSetID + "?embed=true";

					uploadSuccessful = true;
				} else {
					uploadSuccessful = false;
					QDataSet ds = new QDataSet(nameOfDataSet, "Data uploaded from Android Car Ramp Physics",
							QDataSet.Type.DATA, dataSet.toString(), null, projectNumber, null);

					CarRampPhysicsV2.uq.addDataSetToQueue(ds);
				}
			} else {

				uploadSuccessful = false;
				QDataSet ds = new QDataSet(nameOfDataSet, "Data uploaded from Android Car Ramp Physics",
						QDataSet.Type.DATA, dataSet.toString(), null, projectNumber, null);

				CarRampPhysicsV2.uq.addDataSetToQueue(ds);

				return;
			}

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
				
				JSONObject jobj = new JSONObject();
				try {
					jobj.put("data", dataJSON);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jobj = UploadQueue.getAPI().rowsToCols(jobj);

				System.out.println("JOBJ: " + jobj.toString());

				dataSetID = UploadQueue.getAPI()
						.jsonDataUpload(Integer.parseInt(projectNumber),
								jobj, nameOfDataSet);
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
			if (dia != null && dia.isShowing())
				dia.dismiss();

			len = 0;
			len2 = 0;

			if (uploadSuccessful) {
				w.make("Data upload successful.", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
				startActivity(new Intent(CarRampPhysicsV2.this, ViewData.class));
			} else {
				w.make("Data saved.", Waffle.LENGTH_LONG, Waffle.IMAGE_CHECK);
			}

			OrientationManager.enableRotation(CarRampPhysicsV2.this);

		}
	}

	private class NoToastTwiceTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected void onPreExecute() {
			dontToastMeTwice = true;
			exitAppViaBack = true;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			try {
				Thread.sleep(1500);
				exitAppViaBack = false;
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				exitAppViaBack = false;
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			dontToastMeTwice = false;
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

	public class LoginTask extends AsyncTask<Void, Integer, Void> {

		boolean success;

		@Override
		protected Void doInBackground(Void... arg0) {
			final SharedPreferences mPrefs = new ObscuredSharedPreferences(
					CarRampPhysicsV2.mContext,
					CarRampPhysicsV2.mContext.getSharedPreferences(Login.PREFERENCES_KEY_OBSCURRED_USER_INFO,
							Context.MODE_PRIVATE));
			success = api.createSession(mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, ""),
					mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, ""));

			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			if (success) {
				w.make("Login Successful", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);

			} else {
				if (Connection.hasConnectivity(mContext))
					w.make("Login failed!", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
			}
		}

	}

	public class OnCreateLoginTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			if (Connection.hasConnectivity(mContext)) {
				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						CarRampPhysicsV2.mContext,
						CarRampPhysicsV2.mContext.getSharedPreferences(
								Login.PREFERENCES_KEY_OBSCURRED_USER_INFO, Context.MODE_PRIVATE));
				api.createSession(mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, ""),
						mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, ""));
			}
			return null;
		}

	}

}