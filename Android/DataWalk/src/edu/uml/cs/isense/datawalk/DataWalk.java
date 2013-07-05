package edu.uml.cs.isense.datawalk;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.PorterDuff;
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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.waffle.Waffle;

public class DataWalk extends Activity implements LocationListener, SensorEventListener {

	public static Boolean running = false;

	private Button startStop;
	private Vibrator vibrator;
	private TextView timeElapsedBox;
	private TextView pointsUploadedBox;
	private TextView latLong;
	private LocationManager mLocationManager;
	private PowerManager mPowerManager;
	private WakeLock runLock;
	private Boolean appTimedOut = false;
	private Boolean gpsWorking = false;
	private Boolean userLoggedIn = false;
	private int resultGotName;
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

	private static final int MENU_ITEM_ABOUT = 2;
	private static final int MENU_ITEM_QUIT = 3;
	private static final int DIALOG_VIEW_DATA = 4;
	private static final int DIALOG_NO_GPS = 5;
	private static final int DIALOG_FORCE_STOP = 6;
	private static final int DIALOG_EXPIRED = 7;
	private static final int DIALOG_NO_CONNECT = 8;
	private static final int DIALOG_NO_POINTS = 9;
	private static final int DIALOG_DIFFICULTY = 10;

	private static final int TIMER_LOOP = 1000;
	private static final int INTERVAL = 10000;

	private int elapsedMillis = 0;
	private int sessionId = -1;
	private int dataPointCount = 0;

	private MediaPlayer mMediaPlayer;

	RestAPI rapi;

	String s_elapsedSeconds, s_elapsedMillis, s_elapsedMinutes;
	String nameOfSession = "";
	String partialSessionName = "";

	DecimalFormat toThou = new DecimalFormat("#,###,##0.000");

	int i = 0;

	ProgressDialog dia;
	double partialProg = 1.0;

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
	static boolean thruUpload = false;

	private Handler mHandler;

	public static String textToSession = "";
	public static String toSendOut = "";

	//private static String loginName = "usasef.datawalk.app.user@gmail.com";
	//private static String loginPass = "iSENSErUS";
	private static String loginName = "sor";
	private static String loginPass = "sor";
	private static String experimentId = "595";
	private static String baseSessionUrl = "http://isensedev.cs.uml.edu/highvis.php?sessions=";
	private static String marketUrl = "https://play.google.com/store/apps/developer?id=UMass+Lowell";
	private static String sessionUrl = "http://isensedev.cs.uml.edu/highvis.php?sessions=406";
	private static boolean useDev = true;

	private static int waitingCounter = 0;

	public static JSONArray dataSet;
	public static JSONArray uploadSet;
	
	static int mheight = 1;
	static int mwidth = 1;

	public static Context mContext;
	private Waffle w;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;
		
		w = new Waffle(mContext);

		mPowerManager = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		runLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"WakeLock");

		Display deviceDisplay = getWindowManager().getDefaultDisplay();
		mwidth = deviceDisplay.getWidth();
		mheight = deviceDisplay.getHeight();

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(useDev);

		mHandler = new Handler();

		startStop = (Button) findViewById(R.id.startStop);
		timeElapsedBox = (TextView) findViewById(R.id.timeElapsed);
		pointsUploadedBox = (TextView) findViewById(R.id.pointCount);

		latLong = (TextView) findViewById(R.id.myLocation);

		/*
		 * This block useful for if onBackPressed - retains some things from
		 * previous session
		 */
		if (running)
			showDialog(DIALOG_FORCE_STOP);

		startStop.getBackground().setColorFilter(0xFFFFFF33,
				PorterDuff.Mode.MULTIPLY);
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
					sessionId = -1;
					startStop.setText(getString(R.string.startPrompt));

					timeTimer.cancel();
					running = false;
					useMenu = true;

					startStop.getBackground().setColorFilter(0xFFFFFF33,
							PorterDuff.Mode.MULTIPLY);

					if (dataPointCount >= 1)
						showDialog(DIALOG_VIEW_DATA);
					else {
						showDialog(DIALOG_NO_POINTS);
					}

					if (runLock.isHeld())
						runLock.release();

				} else {

					runLock.acquire();
					
					dataSet = new JSONArray();
					uploadSet = new JSONArray();
					
					thruUpload = true;
					
					accel = new float[4];
					if (mSensorManager == null)
						mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
					
					mSensorManager.registerListener(DataWalk.this,
							mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
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

							elapsedMillis += INTERVAL;
							i++;

							mHandler.post(new Runnable() {
								@Override
								public void run() {
									timeElapsedBox.setText("Time Elapsed: " + i
											+ " seconds");
								}
							});

							// determine if we can upload
							if (!rapi.isConnectedToInternet())
								uploadPoint = false;
							else
								uploadPoint = true;

							
							// record the data point
							JSONArray dataRow = new JSONArray();

							try {
								
								//dataJSON.put(accel[3]);
								dataRow.put(loc.getLatitude());
								dataRow.put(loc.getLongitude());
								dataRow.put(System.currentTimeMillis() + elapsedMillis);

								dataSet.put(dataRow);

							} catch (JSONException e) {
								e.printStackTrace();
							}

							if ((((i % 10) == 0) && i >= 9) && (uploadPoint)) {
								
								// setup the upload set and reset the data set
								uploadSet = new JSONArray();
								uploadSet = dataSet;
								dataSet = new JSONArray();
								
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										new Task().execute();
									}
								});
							}
							
						}

					}, 0, TIMER_LOOP);
					startStop.getBackground().setColorFilter(0xFF00FF00,
							PorterDuff.Mode.MULTIPLY);

				}
				return running;

			}

		});

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		initLocationManager();
		waitingForGPS();

		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);

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
		userLoggedIn = false;
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
		userLoggedIn = false;
	}

	@Override
	public void onResume() {
		super.onResume();

		inPausedState = false;

		if (running)
			showDialog(DIALOG_FORCE_STOP);

		if (!userLoggedIn)
			attemptLogin();
		initLocationManager();

		if (mTimer == null)
			waitingForGPS();

	}

	@Override
	public void onBackPressed() {
		if (!dontToastMeTwice) {
			if (running)
				Toast.makeText(
						this,
						"Cannot exit via BACK while recording data; use HOME instead.",
						Toast.LENGTH_LONG).show();
			else
				Toast.makeText(this, "Press back again to exit.",
						Toast.LENGTH_SHORT).show();
			new NoToastTwiceTask().execute();
		} else if (exitAppViaBack && !running) {

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
			uploadPoint = true;
			gpsWorking = true;
		} else {
			uploadPoint = false;
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

	protected Dialog onCreateDialog(final int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Dialog dialog;

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

		switch (id) {
		case DIALOG_NO_GPS:

			builder.setTitle("No GPS Provider Found")
					.setMessage(
							"Enabling GPS satellites is recommended for this application.  Would you like to enable GPS?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.cancel();
									startActivity(new Intent(
											Settings.ACTION_LOCATION_SOURCE_SETTINGS));
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.cancel();
								}
							});

			dialog = builder.create();

			break;

		case DIALOG_FORCE_STOP:

			builder.setTitle("Data Recording Halted")
					.setMessage(
							"You exited the app while data was still being recorded.  Data has stopped recording.")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
									startStop.performLongClick();
								}
							});

			dialog = builder.create();

			if (runLock.isHeld())
				runLock.release();

			break;

		case DIALOG_VIEW_DATA:

			builder.setTitle("Web Browser")
					.setMessage(
							"Would you like to view your data on the iSENSE website?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
									Intent i = new Intent(Intent.ACTION_VIEW);
									i.setData(Uri.parse(sessionUrl));
									startActivity(i);
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
								}
							}).setCancelable(true);

			dialog = builder.create();

			break;

		case MENU_ITEM_ABOUT:

			builder.setTitle("About")
					.setMessage(R.string.aboutApp)
					.setCancelable(false)
					.setNegativeButton("Back",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
								}
							}).setCancelable(true);

			dialog = builder.create();

			break;

		case DIALOG_EXPIRED:

			builder.setTitle("Timed Out")
					.setMessage(
							"This app has expired and you will no longer be able to use it for safety and security reasons. "
									+ "However, you may view our other apps on the Android Marketplace and download them there. Would "
									+ "you like to do this?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
									((Activity) mContext).finish();
									Intent i = new Intent(Intent.ACTION_VIEW);
									i.setData(Uri.parse(marketUrl));
									startActivity(i);
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
									((Activity) mContext).finish();
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(
										DialogInterface dialoginterface) {
									dialoginterface.dismiss();
									((Activity) mContext).finish();
								}
							}).setCancelable(true);

			dialog = builder.create();

			break;

		case DIALOG_NO_CONNECT:

			builder.setTitle("No Connectivity")
					.setMessage(
							"Could not connect to the internet through either wifi or mobile service. "
									+ "You will not be able to use this app until either is enabled.")
					.setPositiveButton("Dismiss",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
									((Activity) mContext).finish();
								}
							})
					.setNegativeButton("Try Again",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									if (rapi.isConnectedToInternet()) {
										dialoginterface.dismiss();
										boolean success = rapi.login(loginName,
												loginPass);
										if (success)
											Toast.makeText(DataWalk.this,
													"Connectivity found!",
													Toast.LENGTH_SHORT).show();
										else {
											showDialog(DIALOG_EXPIRED);
											appTimedOut = true;
										}
									} else {
										dialoginterface.dismiss();
										new NotConnectedTask().execute();
									}
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(
										DialogInterface dialoginterface) {
									dialoginterface.dismiss();
									((Activity) mContext).finish();
								}
							}).setCancelable(true);

			dialog = builder.create();

			break;

		case DIALOG_NO_POINTS:

			builder.setTitle("No Points Found")
					.setMessage(
							"No points were successfully uploaded.  "
									+ "GPS points may be difficult to obtain at this location.")
					.setPositiveButton("Dismiss",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(
										DialogInterface dialoginterface) {
									dialoginterface.dismiss();
								}
							}).setCancelable(true);

			dialog = builder.create();

			break;

		case DIALOG_DIFFICULTY:

			builder.setTitle("Difficulties")
					.setMessage(
							"This application has experienced WiFi connection difficulties.  Try to reconfigure your WiFi "
									+ "settings or turn it off and on, then hit \"Try Again\".")
					.setPositiveButton("Try Again",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
									if (!userLoggedIn)
										attemptLogin();
								}
							}).setCancelable(false);

			dialog = builder.create();

			break;

		default:
			dialog = null;
			break;
		}

		int apiLevel = getApiLevel();
		if (apiLevel >= 11) {
			dialog.show(); /* works but doesn't center it */

			lp.copyFrom(dialog.getWindow().getAttributes());
			lp.width = mwidth;
			lp.height = WindowManager.LayoutParams.FILL_PARENT;
			lp.gravity = Gravity.CENTER_VERTICAL;
			lp.dimAmount = 0.7f;

			dialog.getWindow().setAttributes(lp);
			dialog.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_DIM_BEHIND);

			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					removeDialog(id);
				}
			});

			return null;

		} else {

			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					removeDialog(id);
				}
			});

			return dialog;
		}

	}

	static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	private Runnable uploader = new Runnable() {

		@Override
		public void run() {

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss", Locale.US);
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

			w.make("Uploading data to iSENSE...",
					Waffle.LENGTH_SHORT);
				
		}

		@Override
		protected Void doInBackground(Void... voids) {

			rapi.login(loginName, loginPass);
			uploader.run();
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {
			dataPointCount++;
			pointsUploadedBox.setText("Points Uploaded: " + dataPointCount);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ITEM_ABOUT, Menu.NONE, "About");
		menu.add(Menu.NONE, MENU_ITEM_QUIT, Menu.NONE, "Quit");
		return true;
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_ABOUT:
			showDialog(MENU_ITEM_ABOUT);
			return true;
		case MENU_ITEM_QUIT:
			((Activity) mContext).finish();
		}
		return false;
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
			else
				showDialog(DIALOG_NO_CONNECT);
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
			showDialog(DIALOG_NO_GPS);
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

	protected void onActivityResult(int resultCode, Intent data) {
		if (resultCode == LoginActivity.NAME_SUCCESSFUL) {
		}
	}

	// gets the user's name if not already provided + login to web site
	private void attemptLogin() {
		if (rapi.isConnectedToInternet()) {
			boolean success = rapi.login(loginName, loginPass);
			if (!success) {
				if (rapi.connection == "600") {
					showDialog(DIALOG_EXPIRED);
					appTimedOut = true;
				} else {
					showDialog(DIALOG_DIFFICULTY);
				}

			} else {
				userLoggedIn = true;
				if (firstName.length() == 0 || lastInitial.length() == 0)
					startActivityForResult(new Intent(mContext,
							LoginActivity.class), resultGotName);
			}
		} else {
			showDialog(DIALOG_NO_CONNECT);
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
}
