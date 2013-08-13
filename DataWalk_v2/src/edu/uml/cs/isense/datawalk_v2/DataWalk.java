package edu.uml.cs.isense.datawalk_v2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.datawalk_v2.dialogs.DataRateDialog;
import edu.uml.cs.isense.datawalk_v2.dialogs.ForceStop;
import edu.uml.cs.isense.datawalk_v2.dialogs.NoConnect;
import edu.uml.cs.isense.datawalk_v2.dialogs.NoGps;
import edu.uml.cs.isense.datawalk_v2.dialogs.ViewData;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.objects.RProject;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class DataWalk extends Activity implements LocationListener,
		SensorEventListener, Listener {
	public static TextView loggedInAs;
	public static TextView NameTxtBox;
	public static Boolean inApp = false;
	public static Boolean umbChecked = true;
	public static Boolean ChkBoxChecked = true;
	public static Boolean running = false;
	public DataFieldManager dfm;
	public Fields f;
	private int resultGotName;
	private Button startStop;
	private Vibrator vibrator;
	private TextView timeElapsedBox;
	private TextView pointsUploadedBox;
	private TextView expNumBox;
	private TextView rateBox;
	private TextView latLong;
	private LocationManager mLocationManager;

	private Boolean appTimedOut = false;
	private Boolean gpsWorking = false;

	private SensorManager mSensorManager;

	private Location loc;
	private Timer timeTimer;
	private Timer mTimer;

	private float accel[];

	public static String firstName = "";
	public static String lastInitial = "";

	public static final int DIALOG_CANCELED = 0;
	public static final int DIALOG_OK = 1;
	private static final int DIALOG_VIEW_DATA = 4;
	private static final int DIALOG_NO_GPS = 5;
	private static final int DIALOG_FORCE_STOP = 6;
	private static final int DIALOG_NO_CONNECT = 8;

	private static final int QUEUE_UPLOAD_REQUESTED = 101;
	private static final int TIMER_LOOP = 1000;
	private static int INTERVAL = 10000;
	private int mInterval = INTERVAL;

	private int elapsedMillis = 0;
	private int sessionId = -1;
	private int dataPointCount = 0;

	private MediaPlayer mMediaPlayer;

	API api;
	String s_elapsedSeconds, s_elapsedMillis, s_elapsedMinutes;
	String nameOfSession = "";
	String partialSessionName = "";

	int i = 0;

	ProgressDialog dia;
	double partialProg = 1.0;
	UploadQueue uq;
	static boolean inPausedState = false;
	static boolean toastSuccess = false;
	static boolean setupDone = false;
	static boolean choiceViaMenu = false;
	static boolean dontToastMeTwice = false;
	static boolean exitAppViaBack = false;
	static boolean backWasPressed = false;
	static boolean useMenu = true;
	static boolean beginWrite = true;
	static boolean uploadPoint = false;
	static boolean uploadMode = false;
	static boolean savePoint = true;
	static boolean thruUpload = false;

	private Handler mHandler;

	public static String textToSession = "";
	public static String toSendOut = "";

	private static String loginName = "test";
	private static String loginPass = "test";
	public static String experimentId = "31";
	public static String defaultExp = "31";
	private static String baseSessionUrl = "http://isense.cs.uml.edu/highvis.php?sessions=";
	public static String sessionUrl = "";
	private static String experimentUrl = "http://129.63.16.30/projects/";
	private static String baseExperimentUrl = "http://129.63.16.30/projects/";

	private static int waitingCounter = 0;
	public static final int RESET_REQUESTED = 102;
	public static JSONArray dataSet;
	public static JSONArray uploadSet;

	static int mheight = 1;
	static int mwidth = 1;
	private Waffle w;
	public static Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Calling this during onCreate() ensures that your application is
		// properly initialized with default settings,
		// which your application may need to read in order to determine some
		// behaviors(such as whether to download data while on a cell network

		// when the third argument is false, the system sets the default values
		// only if this method has never been called in the past
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		mContext = this;
		w = new Waffle(mContext);
		OrientationManager.enableRotation(DataWalk.this);

		api = API.getInstance(mContext);
		api.useDev(true);

		mHandler = new Handler();

		startStop = (Button) findViewById(R.id.startStop);
		timeElapsedBox = (TextView) findViewById(R.id.timeElapsed);
		pointsUploadedBox = (TextView) findViewById(R.id.pointCount);
		expNumBox = (TextView) findViewById(R.id.expNumBx);
		loggedInAs = (TextView) findViewById(R.id.loginStatus);
		NameTxtBox = (TextView) findViewById(R.id.NameStatus);
		rateBox = (TextView) findViewById(R.id.RateBx);

		latLong = (TextView) findViewById(R.id.myLocation);

		uq = new UploadQueue("data_walk", mContext, api);
		uq.buildQueueFromFile();
		/*
		 * This block useful for if onBackPressed - retains some things from
		 * previous session
		 */
		if (running) {

			Intent i = new Intent(DataWalk.this, ForceStop.class);
			startActivityForResult(i, DIALOG_FORCE_STOP);
		}
		startStop.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				if (appTimedOut)
					return false;

				vibrator.vibrate(300);
				mMediaPlayer.setLooping(false);
				mMediaPlayer.start();

				if (running) {

					running = false;
					thruUpload = false;
					startStop.setText(getString(R.string.startPrompt));

					timeTimer.cancel();
					running = false;
					useMenu = true;
					w.make("Finished recording data! Click on Upload to publish data to iSENSE.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_CHECK);

					if (savePoint) {
						SimpleDateFormat sdf = new SimpleDateFormat(
								"MM/dd/yyyy, HH:mm:ss", Locale.US);
						Date dt = new Date();
						String dateString = sdf.format(dt);

						nameOfSession = firstName + " " + lastInitial + ". - "
								+ dateString;

						// get user's project #, or default if there is none
						SharedPreferences prefs = getSharedPreferences(
								"PROJID", 0);
						experimentId = prefs.getString("project_id", "");
						if (experimentId.equals("")) {
							experimentId = defaultExp;
						}

						experimentUrl = baseExperimentUrl + experimentId;

						QDataSet ds = new QDataSet(QDataSet.Type.DATA,
								nameOfSession,
								"Data Point Uploaded from Android DataWalk",
								experimentId, dataSet.toString(), null);
						uq.addDataSetToQueue(ds);
					} else if (uploadMode) {
						if (dataSet.length() != 0) {
							Log.d("tag", "" + sessionId);
							uploadSet = dataSet;
							dataSet = new JSONArray();

							if (uploadPoint == true) {
								Log.d("tag", "uploading");
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										new Task().execute();
									}
								});
							}
						}
					}

					if (uploadPoint) {
						if (dataPointCount >= 1) {
							Intent i = new Intent(DataWalk.this, ViewData.class);
							startActivityForResult(i, DIALOG_VIEW_DATA);

						}
					}

				} else {

					pointsUploadedBox.setText("Points Recorded: " + "0");
					timeElapsedBox.setText("Time Elapsed:" + " 0 seconds");
					dataPointCount = 0;

					Log.d("sessionId", sessionId + "= reseting session id");
					sessionId = -1;

					// THIS ALLOWS THE START BUTTON TO CONTINUOUSLY CALL THE
					// NAME ACTIVITY IF SOMEONE DID NOT ENTER A NAME!!!!
					if (!setupDone) {
						if (firstName.equals("") || lastInitial.equals("")) {
							startActivityForResult(new Intent(mContext,
									EnterNameActivity.class), resultGotName);
							w.make("You must enter your name before starting to record data.",
									Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
							setupDone = true;
							return false;
						}

					}
					getWindow().addFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					thruUpload = true;

					dataSet = new JSONArray();
					uploadSet = new JSONArray();

					final long startTime = System.currentTimeMillis();

					accel = new float[4];
					if (mSensorManager == null)
						mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

					mSensorManager.registerListener(
							DataWalk.this,
							mSensorManager
									.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
							SensorManager.SENSOR_DELAY_FASTEST);

					elapsedMillis = 0;
					i = 0;

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						Toast.makeText(
								getBaseContext(),
								"Data recording interrupted! Time values may be inconsistent.",
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}

					if (savePoint) {
						uploadMode = false;
					}

					useMenu = false;
					running = true;
					startStop.setText(getString(R.string.stopPrompt));
					OrientationManager.disableRotation(DataWalk.this);
					timeTimer = new Timer();
					timeTimer.scheduleAtFixedRate(new TimerTask() {
						public void run() {

							i++;
							Log.d("tag", "mInterval is: " + mInterval
									+ "elapsedMillis =:" + elapsedMillis);

							mHandler.post(new Runnable() {

								@Override
								public void run() {
									if (i == 1) {
										timeElapsedBox.setText("Time Elapsed: "
												+ i + " second");
									} else {
										timeElapsedBox.setText("Time Elapsed: "
												+ i + " seconds");
									}
								}
							});
							if (!api.hasConnectivity())
								uploadPoint = false;

							else if (uploadMode) {
								uploadPoint = true;
								ChkBoxChecked = true;

							} else {
								savePoint = true;
								ChkBoxChecked = false;
							}

							// Every n seconds which is determined by interval
							// (not including time 0)
							if ((i % (mInterval / 1000)) == 0 && i != 0) {
								JSONObject dataJSON = new JSONObject();
								elapsedMillis += mInterval;

								try {
									long time = startTime + elapsedMillis;
									dataJSON.put("1", accel[3]);
									dataJSON.put("2", loc.getLatitude());
									dataJSON.put("3", loc.getLongitude());
									dataJSON.put("0", "u " + time);

									if (gpsWorking) {
										dataSet.put(dataJSON);
										Log.d("Recording", "Number of points: "
												+ dataSet.length());

										dataPointCount++;
										runOnUiThread(new Runnable() {

											@Override
											public void run() {
												pointsUploadedBox
														.setText("Points Recorded: "
																+ dataPointCount);
											}

										});
									}

								} catch (JSONException e) {
									e.printStackTrace();
								}
								if (savePoint) {
									umbChecked = false;
									// DataSet ds = new DataSet(
									// DataSet.Type.DATA,
									// "",
									// "Data Point Uploaded from Android DataWalk",
									// "592", dataSet.toString(), null, -1,
									// "", "", "", "");
									// uq.addDataSetToQueue(ds);
									// todo
									/*
									 * mHandler.post(new Runnable() {
									 * 
									 * @Override public void run() {
									 * w.make("Data Point Saved!",
									 * Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
									 * } });
									 */

								}

								// upload points every 10 seconds (i is the
								// number of seconds)
								if (uploadMode && ((i % 10) == 0 && i > 9)) {
									Log.d("tag", "preparing to upload");
									uploadSet = dataSet;
									dataSet = new JSONArray();

									if (uploadPoint == true) {
										Log.d("tag", "uploading");
										mHandler.post(new Runnable() {
											@Override
											public void run() {
												new Task().execute();
											}
										});
									}
								}

							}
						}

					}, 0, TIMER_LOOP);

				}
				return running;

			}

		});

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		initLocationManager();
		waitingForGPS();

		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);

		new AttemptLoginTask().execute();

		if (savedInstanceState == null && api.hasConnectivity()) {
			if (firstName.equals("") || lastInitial.equals("")) {
				startActivityForResult(new Intent(mContext,
						EnterNameActivity.class), resultGotName);

			}
		}

		SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
		SharedPreferences.Editor mEdit = mPrefs.edit();
		mEdit.putString("project_id", defaultExp).commit();

	}// ends onCreate

	@Override
	public void onPause() {
		super.onPause();

		if (timeTimer != null)
			timeTimer.cancel();
		if (mTimer != null)
			mTimer.cancel();

		mTimer = null;
		inPausedState = true;

	}

	@Override
	public void onStart() {
		super.onStart();

		if (mLocationManager != null)
			initLocationManager();
	}

	@Override
	public void onStop() {
		super.onStop();

		if (!running) {
			if (mLocationManager != null)
				mLocationManager.removeUpdates(DataWalk.this);

			if (mSensorManager != null)
				mSensorManager.unregisterListener(DataWalk.this);
		}

		if (timeTimer != null)
			timeTimer.cancel();
		if (mTimer != null)
			mTimer.cancel();
		mTimer = null;

		inPausedState = true;

	}

	@Override
	public void onResume() {
		super.onResume();

		if (api.hasConnectivity()) {

			savePoint = true;
			uploadPoint = false;
			uploadMode = false;
			umbChecked = false;
		}
		if (umbChecked)
			uploadMode = true;
		// uploadMode =
		// PreferenceManager.getDefaultSharedPreferences(this).getBoolean("UploadMode",
		// true);

		mInterval = Integer.parseInt(getSharedPreferences("RecordingPrefs", 0)
				.getString("DataUploadRate", "10000"));

		if (uq != null)
			uq.buildQueueFromFile();

		inPausedState = false;

		if (running) {
			Intent i = new Intent(DataWalk.this, ForceStop.class);
			startActivityForResult(i, DIALOG_FORCE_STOP);
		}

		initLocationManager();

		if (mTimer == null)
			waitingForGPS();

		NameTxtBox.setText("Name: " + firstName + " " + lastInitial);
		loggedInAs.setText(getResources().getString(R.string.logged_in_as)
				+ " " + LoginIsense.uName);
		dataPointCount = 0;
		pointsUploadedBox.setText("Points Recorded: " + dataPointCount);
		i = 0;
		expNumBox.setText("Project Number: " + experimentId);
		timeElapsedBox.setText("Time Elapsed: " + i + " seconds");
		if (mInterval == 1000) {
			rateBox.setText("Data Recorded Every: 1 second");
		} else if (mInterval == 60000) {
			rateBox.setText("Data Recorded Every: 1 Minute");
		} else {
			rateBox.setText("Data Recorded Every: " + mInterval / 1000
					+ " seconds");
			Log.d("tag", "!!!!!!!The Project Id Is:" + experimentId);
		}

		new OnResumeLoginTask().execute();
	}// ends onCreate

	@Override
	public void onBackPressed() {
		if (running) {
			Toast.makeText(
					this,
					"Cannot exit via BACK while recording data; use HOME instead.",
					Toast.LENGTH_LONG).show();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location.getLatitude() != 0 && location.getLongitude() != 0) {
			loc = location;
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

	static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	private Runnable uploader = new Runnable() {

		@Override
		public void run() {

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss",
					Locale.US);
			Date dt = new Date();
			String dateString = sdf.format(dt);

			nameOfSession = firstName + " " + lastInitial + ". - " + dateString;

			if (sessionId == -1) {

				// sessionId = api.createSession(experimentId, nameOfSession,
				// "Automated Submission Through Android App",
				// "801 Mt Vernon Place NW", "Washington, DC",
				// "United States");
				// api.putSessionData(sessionId, experimentId, uploadSet);

				JSONObject jobj = new JSONObject();
				try {
					jobj.put("data", uploadSet);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jobj = api.rowsToCols(jobj);

				api.uploadDataSet(Integer.parseInt(experimentId), jobj,
						nameOfSession);

				sessionUrl = baseSessionUrl + sessionId;
			} else {
				// api.updateSessionData(sessionId, experimentId, uploadSet);
				JSONObject jobj = new JSONObject();
				try {
					jobj.put("data", uploadSet);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jobj = api.rowsToCols(jobj);

				api.appendDataSetData(sessionId, jobj);
			}

		}

	};

	private class Task extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

			w.make("Uploading data to iSENSE...", Waffle.LENGTH_SHORT);

		}

		@Override
		protected Void doInBackground(Void... voids) {

			api.createSession(loginName, loginPass);
			uploader.run();
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {

		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!useMenu) {
			menu.getItem(0).setEnabled(false);
			menu.getItem(1).setEnabled(false);
		} else {
			menu.getItem(0).setEnabled(true);
			menu.getItem(1).setEnabled(true);
		}
		return true;
	}

	private void manageUploadQueue() {
		if (!uq.emptyQueue()) {
			Intent i = new Intent().setClass(mContext, QueueLayout.class);
			i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
			startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);

		} else {
			w.make("No Data to Upload.", Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
		}
	}

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
			if (api.hasConnectivity()) {
				Toast.makeText(DataWalk.this, "Connectivity found!",
						Toast.LENGTH_SHORT).show();
			} else {
				Intent i = new Intent(DataWalk.this, NoConnect.class);
				startActivityForResult(i, DIALOG_NO_CONNECT);

			}
		}
	}

	private void initLocationManager() {
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		
		mLocationManager.addGpsStatusListener(this);
			
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(criteria, true), 0, 0,
					DataWalk.this);
		else {
			Intent i = new Intent(DataWalk.this, NoGps.class);
			startActivityForResult(i, DIALOG_NO_GPS);
		}

		loc = new Location(mLocationManager.getBestProvider(criteria, true));
	}

	private void waitingForGPS() {
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (gpsWorking) {
							latLong.setText("Latitude: " + loc.getLatitude()
									+ "\nLongitude: " + loc.getLongitude());
						} else {
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

	public static final int EXPERIMENT_REQUESTED = 969;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == DIALOG_VIEW_DATA) {

			if (resultCode == RESULT_OK) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(experimentUrl));
				startActivity(i);
			}
		} else if (requestCode == EXPERIMENT_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences prefs = getSharedPreferences("PROJID", 0);
				experimentId = prefs.getString("project_id", null);

				if (api.hasConnectivity()) {
					new GetProjectTask().execute();
				}

			} else {
				// experimentId = experimentId;
			}
		} else if (requestCode == DIALOG_NO_GPS) {
			if (resultCode == RESULT_OK) {
				startActivity(new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		} else if (requestCode == DIALOG_FORCE_STOP) {

			if (resultCode == RESULT_OK) {
				startStop.performLongClick();
			}

		} else if (requestCode == DIALOG_NO_CONNECT) {
			// SO IF THE DIALOG IS ON NO CONNECT WE WANT TO DO THIS

			// IF THE DIALOG IS ON NO CONNECT AND THE PERSON HITS THE BUTTON
			// DON'T SAVE OR DON'T TRY AGAIN WE SET THE RESULT TO OKAY, AND WE
			// JUST EXIT THE APP.
			if (resultCode == RESULT_OK) {
				savePoint = true;
				umbChecked = false;
				w.make("You Have Choosen to Turn Save Mode On!",
						Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
				startActivityForResult(new Intent(mContext,
						EnterNameActivity.class), resultGotName);
				Log.d("Tag",
						"Rajia you have indicated that you want to turn save mode on!!!!!");

			}
			// IF THE PERSON HITS THE TRY AGAIN BUTTON THE RESULT IS SET TO
			// RESULT_CANCELED IN WHICH WE TRY TO CONNECT TO THE INTERNET AGAIN
			else if (resultCode == RESULT_CANCELED) {
				Log.d("Tag",
						"Rajia you have indicated that you want to TRY TO CONNECT TO THE INTERNET AGAIN!!!!!");
				if (api.hasConnectivity()) {

					boolean success = api.createSession(loginName, loginPass);
					if (success)
						Toast.makeText(DataWalk.this, "Connectivity found!",
								Toast.LENGTH_SHORT).show();
					else {
						Intent i = new Intent(DataWalk.this, NoConnect.class);
						startActivityForResult(i, DIALOG_NO_CONNECT);
					}
				} else {
					new NotConnectedTask().execute();
				}
				// This else is when the person wants to turn save mode on
			} else {
				finish();
				Log.d("Tag",
						"Rajia you have indicated that you want to EXIT!!!!!");
			}

		}// ends resultCode = dialog_no_connect
		else if (requestCode == QUEUE_UPLOAD_REQUESTED) {
			uq.buildQueueFromFile();
			if (resultCode == RESULT_OK) {
				Intent i = new Intent(DataWalk.this, ViewData.class);
				startActivityForResult(i, DIALOG_VIEW_DATA);
			}
		} else if (requestCode == resultGotName) {
			if (resultCode == RESULT_OK) {
				if (!inApp)
					inApp = true;
				loggedInAs.setText(getResources().getString(
						R.string.logged_in_as)
						+ "test" + " Name: " + firstName + " " + lastInitial);
			} else {
				if (!inApp)
					finish();
			}
		} else if (requestCode == RESET_REQUESTED) {

			if (resultCode == RESULT_OK) {
				Log.d("tag", "Re-setting settings");
				mInterval = 10000;
				Log.d("tag", "!!!!!!!!! MInterval is:" + mInterval / 1000);
				loginName = "test";
				loginPass = "test";
				firstName = " ";
				lastInitial = "";
				experimentId = defaultExp;
				if (api.hasConnectivity())
					ChkBoxChecked = true;
				umbChecked = true;

				SharedPreferences prefs = getSharedPreferences("PROJID", 0);
				SharedPreferences.Editor mEdit = prefs.edit();
				mEdit.putString("project_id", defaultExp);
				mEdit.commit();
				w.make("Settings have been reset to Default",
						Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
				startActivityForResult(new Intent(mContext,
						EnterNameActivity.class), resultGotName);

			}
		} else if (requestCode == LOGIN_STATUS_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (loggedInAs == null)
					loggedInAs = (TextView) findViewById(R.id.loginStatus);
				loggedInAs.setText(getResources().getString(
						R.string.logged_in_as)
						+ data.getStringExtra("username")
						+ " Name: "
						+ firstName + " " + lastInitial);
			}

		} else if (requestCode == SPINNER_STARTED) {
			if (resultCode == RESULT_OK) {
				// Here is what happens if they click okay on the spinner
				Log.d("tag", "We clicked Okay on the Spinner");
				// w.make("you clicked okay!");

			}

		}
	}// Always b4 This guy

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accel[0] = event.values[0];
			accel[1] = event.values[1];
			accel[2] = event.values[2];
			accel[3] = (float) Math.sqrt((float) (Math.pow(accel[0], 2)
					+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2)));
		}
	}

	// Rajia's created Menu...
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public static final int LOGIN_STATUS_REQUESTED = 45;
	public static final int SPINNER_STARTED = 23;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.Upload:
			manageUploadQueue();
			return true;
		case R.id.reset:
			Intent i = new Intent(mContext, Reset.class);
			startActivityForResult(i, RESET_REQUESTED);
			Log.d("tag", "*** We are in ResetToDefaults");
			return true;
		case R.id.login:
			startActivityForResult(new Intent(this, LoginIsense.class),
					LOGIN_STATUS_REQUESTED);
			return true;
		case R.id.NameChange:
			startActivityForResult(new Intent(this, EnterNameActivity.class),
					resultGotName);
			Log.d("tag", "you clicked on NameChange");
			return true;
		case R.id.DataUploadRate:
			startActivity(new Intent(this, DataRateDialog.class));
			Log.d("tag", "you clicked on Change Recording Rate");
			return true;
		case R.id.ExpNum:
			Intent setup = new Intent(this, Setup.class);
			startActivityForResult(setup, EXPERIMENT_REQUESTED);
			Log.d("tag", "you clicked on Change Exp Num");
			return true;
		case R.id.About:
			startActivity(new Intent(this, About.class));
			return true;
		case R.id.help:
			startActivity(new Intent(this, Help.class));
			Log.d("tag", "!!Help Has been Clicked!!");
			return true;
		}
		return false;
	}// ENDS ON OPTIONS ITEM SELECTED

	public class AttemptLoginTask extends AsyncTask<Void, Integer, Void> {

		boolean connect = false;
		boolean success = false;

		@Override
		protected Void doInBackground(Void... arg0) {
			if (api.hasConnectivity()) {
				connect = true;
				success = api.createSession(loginName, loginPass);
			} else {
				connect = false;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (connect) {
				if (!success) {
					if (loginName.length() == 0 || loginPass.length() == 0)
						startActivityForResult(new Intent(mContext,
								LoginIsense.class), LOGIN_STATUS_REQUESTED);
				}
			} else {
				Intent i = new Intent(DataWalk.this, NoConnect.class);
				startActivityForResult(i, DIALOG_NO_CONNECT);
			}

		}

	}

	public class OnResumeLoginTask extends AsyncTask<Void, Integer, Void> {

		boolean connect = false;
		boolean success = false;

		@Override
		protected Void doInBackground(Void... arg0) {
			if (api.hasConnectivity()) {
				connect = true;
				success = api.createSession(LoginIsense.uName,
						LoginIsense.password);
			} else {
				connect = false;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (connect) {
				if (success) {
					// w.make("Login as  " + LoginIsense.uName +
					// "  Successful.",Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
					Log.d("tag", "login as test successful!!!!!!!!!!!!!!!!!!!!");
					Intent i = new Intent();
					i.putExtra("username", LoginIsense.uName);
				} else {
					w.make("Incorrect login credentials. Please try again.",
							Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
				}
			} else {
				// do nothing
			}

		}

	}

	public class GetProjectTask extends AsyncTask<Void, Integer, Void> {

		RProject proj;

		@Override
		protected Void doInBackground(Void... arg0) {
			proj = api.getProject(Integer.parseInt(experimentId));
			Log.d("tag", "Project name is: " + proj.name);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (proj.name == null || proj.name.equals("")) {
				Log.d("tag", "Invalid expiremnt number");
				w.make("Project Number Invalid! Please enter a new one.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
				startActivityForResult(new Intent(mContext, Setup.class),
						EXPERIMENT_REQUESTED);
			}
		}

	}

	@Override
	public void onGpsStatusChanged(int event) {
			GpsStatus status = mLocationManager.getGpsStatus(null);
			int count = 0;
			Iterable<GpsSatellite> sats = status.getSatellites();
			
            for (Iterator<GpsSatellite> i = sats.iterator(); i.hasNext(); i.next()) {
            	count++;
            }
            
            if (count < 4) gpsWorking = false;
	}

}// Ends DataWalk.java Class
