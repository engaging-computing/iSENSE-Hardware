package edu.uml.cs.isense.datawalk_v2;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.datawalk_v2.dialogs.ForceStop;
import edu.uml.cs.isense.datawalk_v2.dialogs.NoConnect;
import edu.uml.cs.isense.datawalk_v2.dialogs.NoGps;
import edu.uml.cs.isense.datawalk_v2.dialogs.ViewData;
import edu.uml.cs.isense.queue.DataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.waffle.Waffle;

public class DataWalk extends Activity implements LocationListener,
		SensorEventListener {

	public static Boolean running = false;

	private Button startStop;
	private Vibrator vibrator;
	private TextView timeElapsedBox;
	private TextView pointsUploadedBox;
	private TextView latLong;
	private LocationManager mLocationManager;
	
	private Boolean appTimedOut = false;
	private Boolean gpsWorking = false;
	
	private double latitude;
	private double longitude;

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


	private static final int LOGIN_ACTIVITY_REQUESTED = 100;
	private static final int QUEUE_UPLOAD_REQUESTED = 101;
	private static final int TIMER_LOOP = 1000;
	private static final int INTERVAL = 10000;
	private int mInterval = INTERVAL;

	private int elapsedMillis = 0;
	private int sessionId = -1;
	private int dataPointCount = 0;

	private MediaPlayer mMediaPlayer;

	RestAPI rapi;

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
	static boolean uploadMode = true;
	static boolean savePoint = false;
	static boolean thruUpload = false;

	private Handler mHandler;

	public static String textToSession = "";
	public static String toSendOut = "";

	// private static String loginName = "usasef.datawalk.app.user@gmail.com";
	// private static String loginPass = "iSENSErUS";
	private static String loginName = "sor";
	private static String loginPass = "sor";
	public static String experimentId = "592";
	public static String defaultExp = "592";
	private static String baseSessionUrl = "http://isensedev.cs.uml.edu/highvis.php?sessions=";
	//private static String marketUrl = "https://play.google.com/store/apps/developer?id=UMass+Lowell";
	private static String sessionUrl = "http://isensedev.cs.uml.edu/highvis.php?sessions=406";

	private static int waitingCounter = 0;

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
		//TODO
		//Calling this during onCreate() ensures that your application is properly initialized with default settings, 
		//which your application may need  to read in order to determine some behaviors(such as whether to download data while on a cell network  
		
		//when the third argument is false, the system sets the default values only if this method has never been called in the past
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		mContext = this;
		w = new Waffle(mContext);
	

		Display deviceDisplay = getWindowManager().getDefaultDisplay();
		mwidth = deviceDisplay.getWidth();
		mheight = deviceDisplay.getHeight();

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(true);

		mHandler = new Handler();

		startStop = (Button) findViewById(R.id.startStop);
		timeElapsedBox = (TextView) findViewById(R.id.timeElapsed);
		pointsUploadedBox = (TextView) findViewById(R.id.pointCount);

		latLong = (TextView) findViewById(R.id.myLocation);


		uq = new UploadQueue("data_walk", mContext, rapi);
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

				//TODO RAJIA COME BACK TOOOO
				timeElapsedBox.setText("Time Elapsed:" + " seconds" );
				
				if (appTimedOut)
					return false;

				vibrator.vibrate(300);
				mMediaPlayer.setLooping(false);
				mMediaPlayer.start();

				if (running) {

					running = false;
					thruUpload = false;
					sessionId = -1;
					startStop.setText(getString(R.string.startPrompt));

					timeTimer.cancel();
					running = false;
					useMenu = true;

					if (dataPointCount >= 1) {
						Intent i = new Intent(DataWalk.this, ViewData.class);
						startActivityForResult(i, DIALOG_VIEW_DATA);
					}

					else {

					}

				} else {
					// todo
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

					useMenu = false;
					running = true;
					startStop.setText(getString(R.string.stopPrompt));

					timeTimer = new Timer();
					timeTimer.scheduleAtFixedRate(new TimerTask() {
						public void run() {

							i++;
							Log.d("tag", "mInterval is: " + mInterval + "elapsedMillis =:" + elapsedMillis);

							mHandler.post(new Runnable() {
								
								
								@Override
								public void run() {
									timeElapsedBox.setText("Time Elapsed: " + i
											+ " seconds");
								}
							});
							if (!rapi.isConnectedToInternet())
								uploadPoint = false;
							else
								if (uploadMode)
								uploadPoint = true;
								else
								savePoint = true;
								

							if ((i % (mInterval/1000)) == 0 && i!=0) {
								Log.d("tag", "saving point");
								JSONArray dataJSON = new JSONArray();
								elapsedMillis += mInterval;
								
								dataPointCount++;
								runOnUiThread(new Runnable(){

									@Override
									public void run() {
										pointsUploadedBox.setText("Points Uploaded: " + dataPointCount);
									}
									
								});
								
								try {	
									dataJSON.put(accel[3]);
									dataJSON.put(loc.getLatitude());
									dataJSON.put(loc.getLongitude());
									dataJSON.put(startTime
											+ elapsedMillis);

									dataSet.put(dataJSON);

								} catch (JSONException e) {
									e.printStackTrace();
								}
								//TODO
								if (savePoint){
									//TODO PUT A SESSION NAME
									DataSet ds = new DataSet(DataSet.Type.DATA,"", "Data Point Uploaded from Android DataWalk", "-1",dataSet.toString(),null, -1,"", "","","");
									uq.addDataSetToQueue(ds);
									mHandler.post(new Runnable() {
										@Override
										public void run() {
											w.make("Data Point Saved!", Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
										}
									});
									
								}
								
								//TODO 
								//WE WANT TO KEEP THIS BUT IT WILL BE TRUE ONLY WHEN SAVE POINT IS TRUE AND THE USER SAYS YES THEY WOULD LIKE TO UPLOAD THE DATA
								if ((i % 10) == 0 && i > 9) {
									Log.d("tag", "preparing to upload");
									uploadSet = new JSONArray();
									uploadSet = dataSet;
									dataSet = new JSONArray();
									
									if (uploadPoint==true) {
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

		attemptLogin();
	}

	@Override
	public void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(DataWalk.this);
		if (timeTimer != null)
			timeTimer.cancel();
		if (mTimer != null)
			mTimer.cancel();

		mTimer = null;
		inPausedState = true;
		
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
	
		uploadMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("UploadMode", true);
		mInterval = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("Data UploadRate", "10000"));
		
		
		
		if (uq!=null)
			uq.buildQueueFromFile();
		
		inPausedState = false;

		if (running) {
			Intent i = new Intent(DataWalk.this, ForceStop.class);
			startActivityForResult(i, DIALOG_FORCE_STOP);
		}

		initLocationManager();

		if (mTimer == null)
			waitingForGPS();

	}

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
			loc = location;
		if (((latitude = location.getLatitude()) != 0)
				&& ((longitude = location.getLongitude()) != 0)) {
			if (gpsWorking == false) {
				vibrator.vibrate(100);
			}
			
			// TODO - uncomment?
			/*if (uploadMode) {
				uploadPoint = true;
			} else {
				savePoint = true;
				gpsWorking = true;
			}*/
			gpsWorking = true;
			
		} else {
			/*if(!uploadMode) {
				savePoint = true; 
			} else {
				uploadPoint = false;
				gpsWorking = false;
			}*/
			gpsWorking = false;
		}
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
				if (nameOfSession.equals("")) {
					sessionId = rapi.createSession(experimentId,
							"Session name not provided",
							"Automated Submission Through Android App",
							"801 Mt Vernon Place NW", "Washington D.C.",
							"United States");
					rapi.putSessionData(sessionId, experimentId, uploadSet);

					sessionUrl = baseSessionUrl + sessionId;
				} else {
					sessionId = rapi.createSession(experimentId, nameOfSession,
							"Automated Submission Through Android App",
							"801 Mt Vernon Place NW", "Washington, DC",
							"United States");
					rapi.putSessionData(sessionId, experimentId, uploadSet);

					sessionUrl = baseSessionUrl + sessionId;
				}
			} else {
				rapi.updateSessionData(sessionId, experimentId, uploadSet);
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

			rapi.login(loginName, loginPass);
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

	private void manageUploadQueue(){
		if(!uq.emptyQueue()){
			Intent i = new Intent().setClass(mContext, QueueLayout.class);
			i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
			startActivityForResult(i,QUEUE_UPLOAD_REQUESTED);
			
			  
			
		}else{
			w.make("There is no data to upload.", Waffle.LENGTH_LONG,Waffle.IMAGE_WARN);
		}
	}
	private class NoToastTwiceTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected void onPreExecute() {
			dontToastMeTwice = true;
			if (!thruUpload)
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
			if (rapi.isConnectedToInternet())
				Toast.makeText(DataWalk.this, "Connectivity found!",
						Toast.LENGTH_SHORT).show();
			else {
				Intent i = new Intent(DataWalk.this, NoConnect.class);
				startActivityForResult(i, DIALOG_NO_CONNECT);

			}
		}
	}

	private void initLocationManager() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0,
					DataWalk.this);
		else {
			Intent i = new Intent(DataWalk.this, NoGps.class);
			startActivityForResult(i, DIALOG_NO_GPS);
		}

		loc = new Location(mLocationManager.getBestProvider(c, true));
	}

	private void waitingForGPS() {
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (gpsWorking)
							latLong.setText("Lat: " + latitude + "\nLong: "
									+ longitude);
						else {
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_ACTIVITY_REQUESTED) {
			if (resultCode == RESULT_CANCELED) {
				finish();
			}
		} else if (requestCode == DIALOG_VIEW_DATA) {

			if (resultCode == RESULT_OK) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(sessionUrl));
				startActivity(i);
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

			if (resultCode == RESULT_OK) {
				finish();
			} else {
				if (rapi.isConnectedToInternet()) {

					boolean success = rapi.login(loginName, loginPass);
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
			}

		}else if (requestCode == QUEUE_UPLOAD_REQUESTED){
			uq.buildQueueFromFile();
		}

	}// Always b4 This guy

	// gets the user's name if not already provided + login to web site
	private void attemptLogin() {
		if (rapi.isConnectedToInternet()) {
			boolean success = rapi.login(loginName, loginPass);
			if (!success) {
				if (rapi.connection == "600") {
					appTimedOut = true;
				} else {
					
				}

			} else {
				
				if (firstName.length() == 0 || lastInitial.length() == 0)
					startActivityForResult(new Intent(mContext,
							LoginActivity.class), LOGIN_ACTIVITY_REQUESTED);
			}
		} else {
			Intent i = new Intent(DataWalk.this, NoConnect.class);
			startActivityForResult(i, DIALOG_NO_CONNECT);

		}

	}

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
//TODO THIS IS WHERE WE CAN ADD THINGS TO TH MENU!
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			return true;
		case R.id.upload:
			manageUploadQueue();
			return true;
		}

		return false;
	}
	

}
