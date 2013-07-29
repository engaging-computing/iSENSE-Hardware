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

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.exp.Setup;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class CarRampPhysicsV2 extends Activity implements SensorEventListener,
		LocationListener {

	public static String experimentNumber = "409";
	public static String defaultExp = "409";
	private static String userName = "sor";
	private static String password = "sor";

	public static final String baseSessionUrl_Prod = "http://isenseproject.org/highvis.php?sessions=";
	public static final String baseSessionUrl_Dev = "http://isensedev.cs.uml.edu/highvis.php?sessions=";
	public static String baseSessionUrl = "";
	public static String sessionUrl = "";

	private Button startStop;
	private TextView values;
	public static Boolean running = false;

	private SensorManager mSensorManager;
	private LocationManager mLocationManager;

	public static Location loc;
	private float accel[];
	private float orientation[];
	private Timer timeTimer;
	private float rawAccel[];
	private float rawMag[];

	private int INTERVAL = 50;

	static final public int DIALOG_CANCELED = 0;
	static final public int DIALOG_OK = 1;
	static final public int DIALOG_PICTURE = 2;

	static final public int CAMERA_PIC_REQUESTED = 1;
	static final public int CAMERA_VID_REQUESTED = 2;

	public DataFieldManager dfm;
	public Fields f;

	private int countdown;

	static String firstName = "";
	static String lastInitial = "";
	private int resultGotName;

	private boolean timeHasElapsed = false;
	private boolean usedHomeButton = false;
	public static boolean appTimedOut = false;
	public static boolean useDev = false;

	private MediaPlayer mMediaPlayer;

	private int elapsedMillis = 0;

	private String dateString;
	RestAPI rapi;

	private boolean x = false, y = false, z = false, mag = false;

	DecimalFormat toThou = new DecimalFormat("#,###,##0.000");

	int i = 0;
	int len = 0;
	int len2 = 0;
	int length;

	ProgressDialog dia;
	double partialProg = 1.0;

	String nameOfSession = "";

	static int mediaCount = 0;
	static boolean inPausedState = false;
	static boolean toastSuccess = false;
	static boolean useMenu = true;
	public static boolean setupDone = false;
	static boolean choiceViaMenu = false;
	static boolean dontToastMeTwice = false;
	static boolean exitAppViaBack = false;
	static boolean backWasPressed = false;
	static boolean nameSuccess = false;
	static boolean dontPromptMeTwice = false;

	private Handler mHandler;

	public static String textToSession = "";
	public static String toSendOut = "";
	public static String experimentId = "";
	public static JSONArray dataSet;

	static int mheight = 1;
	static int mwidth = 1;
	long currentTime;

	public static Context mContext;

	public static TextView loggedInAs;
	private Waffle w;
	public static boolean inApp = false;

	public static UploadQueue uq;

	public static final int UPLOAD_OK_REQUESTED = 90000;

	public static Bundle saved;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		saved = savedInstanceState;
		mContext = this;

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(useDev);
		if (useDev) {
			baseSessionUrl = baseSessionUrl_Dev;
		} else {
			baseSessionUrl = baseSessionUrl_Prod;
		}

		f = new Fields();
		dfm = new DataFieldManager(Integer.parseInt(experimentNumber), rapi,
				mContext, f);
		dfm.getOrder();
		uq = new UploadQueue("carrampphysics", mContext, rapi);
		uq.buildQueueFromFile();

		dateString = "";

		w = new Waffle(mContext);

		mHandler = new Handler();

		startStop = (Button) findViewById(R.id.startStop);

		values = (TextView) findViewById(R.id.values);

		SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH", 0);
		length = countdown = prefs.getInt("length", 10);

		loggedInAs = (TextView) findViewById(R.id.loginStatus);

		boolean success = rapi.login(userName, password);

		if (success) {
			loggedInAs.setText(getResources().getString(R.string.logged_in_as)
					+ userName + " Name: " + firstName + " " + lastInitial);
		} else {
			if (rapi.isConnectedToInternet())
				w.make("Login Error", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
		}

		SharedPreferences prefs2 = getSharedPreferences("EID", 0);
		experimentNumber = prefs2.getString("experiment_id", null);
		if (experimentNumber == null)
			experimentNumber = defaultExp;
		dfm = new DataFieldManager(Integer.parseInt(experimentNumber), rapi,
				mContext, f);
		dfm.getOrder();

		startStop.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				mMediaPlayer.setLooping(false);
				mMediaPlayer.start();

				if (running) {

					if (timeHasElapsed) {
						OrientationManager
								.enableRotation(CarRampPhysicsV2.this);
						setupDone = false;
						timeHasElapsed = false;
						useMenu = true;
						countdown = length;

						running = false;
						startStop.setText("Hold to Start");

						timeTimer.cancel();
						startStop.getBackground().clearColorFilter();
						choiceViaMenu = false;

						if (!appTimedOut)
							try {
								Intent dataIntent = new Intent(mContext,
										DataActivity.class);

								startActivityForResult(dataIntent,
										UPLOAD_OK_REQUESTED);
							} catch (Exception e) {

							}

						else
							w.make("Your app has timed out, you may not upload data any longer.",
									Waffle.LENGTH_LONG);

					} else if (usedHomeButton) {
						setupDone = false;
						timeHasElapsed = false;
						useMenu = true;
						countdown = length;

						running = false;
						startStop.setText("Hold to Start");

						timeTimer.cancel();
						startStop.getBackground().clearColorFilter();
						choiceViaMenu = false;
						startStop.setEnabled(true);
					}

					startStop.setEnabled(true);
				} else {

					OrientationManager.disableRotation(CarRampPhysicsV2.this);
					startStop.setEnabled(false);
					dataSet = new JSONArray();
					elapsedMillis = 0;
					len = 0;
					len2 = 0;
					i = 0;
					currentTime = getUploadTime(0);

					if (mLocationManager
							.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
						mLocationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER, 0, 0,
								CarRampPhysicsV2.this);

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						w.make("Data recording has offset 100 milliseconds due to an error.",
								Waffle.LENGTH_SHORT);
						e.printStackTrace();
					}

					useMenu = true;

					if (mSensorManager != null) {
						mSensorManager
								.registerListener(
										CarRampPhysicsV2.this,
										mSensorManager
												.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
										SensorManager.SENSOR_DELAY_FASTEST);
						mSensorManager
								.registerListener(
										CarRampPhysicsV2.this,
										mSensorManager
												.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
										SensorManager.SENSOR_DELAY_FASTEST);
					}

					running = true;
					startStop.setText("" + countdown);

					timeTimer = new Timer();
					timeTimer.scheduleAtFixedRate(new TimerTask() {

						public void run() {

							elapsedMillis += INTERVAL;

							if (i >= (length * 20)) {

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

								if (i % 20 == 0) {
									mHandler.post(new Runnable() {
										@Override
										public void run() {
											startStop.setText("" + countdown);
										}
									});
									countdown--;
								}

								f.timeMillis = currentTime + elapsedMillis;
								Log.d("fantastag", "time added");
								SharedPreferences prefs = getSharedPreferences(
										RecordSettings.RECORD_SETTINGS, 0);

								x = prefs.getBoolean("X", true);
								y = prefs.getBoolean("Y", true);
								z = prefs.getBoolean("Z", true);
								mag = prefs.getBoolean("Magnitude", mag);
								if (x) {
									f.accel_x = toThou.format(accel[0]);
									Log.d("fantastag", "X added");
								}
								if (y) {
									f.accel_y = toThou.format(accel[1]);
									Log.d("fantastag", "Y added");
								}
								if (z) {
									f.accel_z = toThou.format(accel[2]);
									Log.d("fantastag", "Z added");
								}
								if (mag) {
									f.accel_total = toThou.format(accel[3]);
									Log.d("fantastag", "Magnitude¯ added");
								}

								dataSet.put(dfm.putData());

							}

						}
					}, 0, INTERVAL);
					startStop.getBackground().setColorFilter(0xFF00FF00,
							PorterDuff.Mode.MULTIPLY);
				}

				return running;

			}

		});

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (mSensorManager != null) {
			mSensorManager.registerListener(CarRampPhysicsV2.this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_FASTEST);
			mSensorManager
					.registerListener(CarRampPhysicsV2.this, mSensorManager
							.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
							SensorManager.SENSOR_DELAY_FASTEST);
		}

		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		accel = new float[4];
		orientation = new float[3];
		rawAccel = new float[3];
		rawMag = new float[3];

		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);

		if (rapi.isConnectedToInternet()) {
			success = rapi.login(userName, password);
			if (!success) {
				if (rapi.connection == "600") {
					w.make("Connection timed out.", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
					appTimedOut = true;
				} else {

				}
			}

		} else {
			w.make(

			"You are not connected to the Internet. Data saving enabled",
					Waffle.LENGTH_LONG, Waffle.IMAGE_CHECK);
		}

		if (savedInstanceState == null) {
			if (firstName.equals("") || lastInitial.equals("")) {
				if (!dontPromptMeTwice) {
					startActivityForResult(new Intent(mContext,
							EnterNameActivity.class), resultGotName);
				}
			}
		}

	}

	long getUploadTime(int millisecond) {

		Calendar c = Calendar.getInstance();

		return (long) (c.getTimeInMillis());

	}

	@Override
	public void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(CarRampPhysicsV2.this);
		if (timeTimer != null)
			timeTimer.cancel();
		inPausedState = true;

	}

	@Override
	public void onStop() {
		super.onStop();
		mLocationManager.removeUpdates(CarRampPhysicsV2.this);
		if (timeTimer != null)
			timeTimer.cancel();
		inPausedState = true;

	}

	public void onUserLeaveHint() {
		super.onUserLeaveHint();
		usedHomeButton = true;
	}

	@Override
	public void onStart() {
		super.onStart();
		inPausedState = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		inPausedState = false;
		SharedPreferences prefs = getSharedPreferences(
				RecordSettings.RECORD_SETTINGS, 0);

		x = prefs.getBoolean("X", x);
		y = prefs.getBoolean("Y", y);
		z = prefs.getBoolean("Z", z);
		mag = prefs.getBoolean("Magnitude", mag);

		String dataLabel = "";

		if (x) {
			dataLabel += "X: ";
		}
		if (y) {
			if (x) {
				dataLabel += " , Y: ";
			} else
				dataLabel += "Y: ";
		}
		if (z) {
			if (x || y) {
				dataLabel += " , Z: ";
			} else
				dataLabel += "Z: ";
		}

		if (mag) {
			if (x || y || z) {
				dataLabel += " , Magnitude: ";
			} else
				dataLabel += "Magnitude: ";
		}

		values.setText(dataLabel);

		if (usedHomeButton && running) {
			setupDone = false;
			timeHasElapsed = false;
			useMenu = true;
			countdown = length;

			running = false;
			startStop.setText("Hold to Start");

			timeTimer.cancel();
			startStop.getBackground().clearColorFilter();
			choiceViaMenu = false;
			startStop.setEnabled(true);
			dataSet = new JSONArray();
			
			w.make("Data recording halted.", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
		}

		if (uq != null)
			uq.buildQueueFromFile();

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

	public static final int LOGIN_STATUS_REQUESTED = 6005;
	public static final int RECORDING_LENGTH_REQUESTED = 4009;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about_app:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case R.id.login:
			startActivityForResult(new Intent(this, CarRampLoginActivity.class),LOGIN_STATUS_REQUESTED);
			return true;
		case R.id.record_settings:
			startActivity(new Intent(this, RecordSettings.class));
			return true;
		case R.id.experiment_select:
			Intent setup = new Intent(this, Setup.class);
			startActivityForResult(setup, EXPERIMENT_REQUESTED);
			return true;
		case R.id.upload:
			manageUploadQueue();
			return true;
		case R.id.record_length:
			createSingleInputDialog("Change Recording Length",
					"Input new recording length in seconds",
					RECORDING_LENGTH_REQUESTED);
			return true;
		case R.id.changename:
			startActivityForResult(new Intent(this, EnterNameActivity.class),
					resultGotName);
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

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			rawAccel = event.values.clone();
			accel[0] = event.values[0];
			accel[1] = event.values[1];
			accel[2] = event.values[2];

			String xPrepend, yPrepend, zPrepend, data = "";

			xPrepend = accel[0] > 0 ? "+" : "";
			yPrepend = accel[1] > 0 ? "+" : "";
			zPrepend = accel[2] > 0 ? "+" : "";

			if (x) {
				data = "X: " + xPrepend + oneDigit.format(accel[0]);
			}
			if (y) {
				if (!data.equals("")) {
					data += " , Y: " + yPrepend + oneDigit.format(accel[1]);
				} else {
					data += "Y: " + yPrepend + oneDigit.format(accel[1]);
				}
			}
			if (z) {
				if (!data.equals("")) {
					data += " , Z: " + zPrepend + oneDigit.format(accel[2]);
				} else {
					data += "Z: " + zPrepend + oneDigit.format(accel[2]);
				}
			}

			if (mag) {
				accel[3] = (float) Math.sqrt(Math.pow(accel[0], 2)
						+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2));

				if (!data.equals("")) {
					data += " , Magnitude: " + oneDigit.format(accel[3]);
				} else {
					data += "Magnitude: " + oneDigit.format(accel[3]);
				}

			}
			values.setText(data);

		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			rawMag = event.values.clone();

			float rotation[] = new float[9];

			if (SensorManager.getRotationMatrix(rotation, null, rawAccel,
					rawMag)) {
				orientation = new float[3];
				SensorManager.getOrientation(rotation, orientation);
			}

		}
	}

	@Override
	public void onLocationChanged(Location location) {
		loc = location;
	}

	public static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	public static final int EXPERIMENT_REQUESTED = 9000;
	public static final int QUEUE_UPLOAD_REQUESTED = 5000;
	public static final int RESET_REQUESTED = 6003;

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		dontPromptMeTwice = false;

		if (reqCode == EXPERIMENT_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences prefs = getSharedPreferences("EID", 0);
				experimentNumber = prefs.getString("experiment_id", null);
				if (experimentNumber == null)
					experimentNumber = defaultExp;
				dfm = new DataFieldManager(Integer.parseInt(experimentNumber),
						rapi, mContext, f);
				dfm.getOrder();
			}
		} else if (reqCode == QUEUE_UPLOAD_REQUESTED) {
			uq.buildQueueFromFile();

		} else if (reqCode == UPLOAD_OK_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (len == 0 || len2 == 0)
					w.make("There are no data to upload!", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);

				else
					new UploadTask().execute();
			} else {
				w.make("Data thrown away!", Waffle.LENGTH_LONG,
						Waffle.IMAGE_CHECK);
			}
		} else if (reqCode == LOGIN_STATUS_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (loggedInAs == null)
					loggedInAs = (TextView) findViewById(R.id.loginStatus);
				loggedInAs.setText(getResources().getString(
						R.string.logged_in_as)
						+ data.getStringExtra("username")
						+ " Name: "
						+ firstName + " " + lastInitial);
			}
		} else if (reqCode == RECORDING_LENGTH_REQUESTED) {
			if (resultCode == RESULT_OK) {
				length = Integer.parseInt(data.getStringExtra("input"));
				countdown = length;
				SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH",
						0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("length", length);
				if (length <= 25) {
					INTERVAL = 50;
				} else {
					INTERVAL = 2 * length;
				}
				editor.putInt("Interval", INTERVAL);
				editor.commit();
			}
		} else if (reqCode == resultGotName) {
			if (resultCode == RESULT_OK) {
				if (!inApp)
					inApp = true;
				loggedInAs.setText(getResources().getString(
						R.string.logged_in_as)
						+ "sor" + " Name: " + firstName + " " + lastInitial);
			} else {
				if (!inApp)
					finish();
			}
		} else if (reqCode == RESET_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH",
						0);
				countdown = length = prefs.getInt("length", 10);
				boolean success = rapi.login("sor", "sor");

				if (success) {
					loggedInAs
							.setText(getResources().getString(
									R.string.logged_in_as)
									+ "sor"
									+ " Name: "
									+ firstName
									+ " "
									+ lastInitial);
				} else {
					if (rapi.isConnectedToInternet())
						w.make("Login Error", Waffle.LENGTH_SHORT,
								Waffle.IMAGE_X);
				}

				SharedPreferences eprefs = getSharedPreferences("EID", 0);
				SharedPreferences.Editor editor = eprefs.edit();
				experimentNumber = defaultExp;
				editor.putString("experiment_id", experimentNumber);
				editor.commit();
				INTERVAL = 50;
				x = z = mag = false;
				y = true;
				values.setText("Y: " + accel[1]);
				Log.d("fantastag", "resetti");

			}
		}
	}

	private Runnable uploader = new Runnable() {

		@Override
		public void run() {

			int sessionId = -1;
			String city = "", state = "", country = "";
			List<Address> address = null;
			String addr = "";

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss",
					Locale.ENGLISH);
			Date dt = new Date();
			dateString = sdf.format(dt);

			try {
				if (loc != null) {
					address = new Geocoder(mContext, Locale.getDefault())
							.getFromLocation(loc.getLatitude(),
									loc.getLongitude(), 1);
					if (address.size() > 0) {
						city = address.get(0).getLocality();
						state = address.get(0).getAdminArea();
						country = address.get(0).getCountryName();
						addr = address.get(0).getThoroughfare();

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			nameOfSession = firstName + " " + lastInitial + ". - " + dateString;

			if (rapi.isConnectedToInternet()) {

				String experimentNumber = CarRampPhysicsV2.experimentNumber;
				if (address == null || address.size() <= 0) {
					sessionId = rapi.createSession(experimentNumber,
							nameOfSession + " (location not found)",
							"Automated Submission Through Android App", "", "",
							"");
				} else if (firstName.equals("") || lastInitial.equals("")) {
					sessionId = rapi.createSession(experimentNumber,
							"No Name Provided - " + dateString,
							"Automated Submission Through Android App", "",
							city + ", " + state, country);
				} else {
					sessionId = rapi.createSession(experimentNumber,
							nameOfSession,
							"Automated Submission Through Android App", "",
							city + ", " + state, country);
				}

				CarRampPhysicsV2.sessionUrl = CarRampPhysicsV2.baseSessionUrl
						+ sessionId;

				rapi.putSessionData(sessionId, experimentNumber, dataSet);
				uploadSuccessful = true;
			} else {

				uploadSuccessful = false;
				QDataSet ds = new QDataSet(QDataSet.Type.DATA, nameOfSession,
						"Car Ramp Physics", experimentNumber,
						dataSet.toString(), null, sessionId, city, state,
						country, addr);
				CarRampPhysicsV2.uq.addDataSetToQueue(ds);

				return;
			}

		}

	};
	public boolean uploadSuccessful;

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

}