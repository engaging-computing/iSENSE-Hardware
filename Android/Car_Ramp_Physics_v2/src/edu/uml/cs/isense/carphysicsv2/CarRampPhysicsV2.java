/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII               iSENSE Car Ramp Physics App                 SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                    By: Michael Stowell                       SSS                **/
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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.exp.Setup;
import edu.uml.cs.isense.queue.DataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class CarRampPhysicsV2 extends FragmentActivity implements SensorEventListener,
		LocationListener {

	public static String experimentNumber = "409"; // HARD CODED
	// private static String userName = "accelapp"; // HARD CODED
	// private static String password = "ecgrul3s"; // HARD CODED
	private static String userName = "sor";
	private static String password = "sor";

	public static String baseSessionUrl = "http://isense.cs.uml.edu/newvis.php?sessions=";
	private static String marketUrl = "https://play.google.com/store/apps/developer?id=UMass+Lowell";
	public static String sessionUrl = "";

	private Button startStop;
	private TextView values;
	private Boolean running = false;
	// private Vibrator vibrator;

	private SensorManager mSensorManager;
	private LocationManager mLocationManager;

	public static Location loc;
	private float accel[];
	private float orientation[];
	private Timer timeTimer;
	private float rawAccel[];
	private float rawMag[];

	private static final int INTERVAL = 50;
	private static final int DIALOG_FORCE_STOP = 2;
	// private static final int DIALOG_NEED_NAME = 3;
	private static final int DIALOG_VIEW_DATA = 3;
	private static final int DIALOG_NO_CONNECT = 5;
	private static final int DIALOG_EXPIRED = 6;
	private static final int DIALOG_DIFFICULTY = 7;
	// private static final int EXPERIMENT_CODE = ;

	static final public int DIALOG_CANCELED = 0;
	static final public int DIALOG_OK = 1;
	static final public int DIALOG_PICTURE = 2;

	static final public int CAMERA_PIC_REQUESTED = 1;
	static final public int CAMERA_VID_REQUESTED = 2;

	public DataFieldManager dfm;
	public Fields f;

	private int count = 0;
	private int countdown = 10;

	static String firstName = "";
	static String lastInitial = "";
	private int resultGotName;

	private boolean timeHasElapsed = false;
	private boolean usedHomeButton = false;
	private boolean appTimedOut = false;

	private MediaPlayer mMediaPlayer;

	private int elapsedMillis = 0;

	private String dateString;
	RestAPI rapi;

	private boolean x = false, y = false, z = false, mag = false;

	DecimalFormat toThou = new DecimalFormat("#,###,##0.000");

	int i = 0;
	int len = 0;
	int len2 = 0;

	ProgressDialog dia;
	double partialProg = 1.0;

	String nameOfSession = "";

	static int mediaCount = 0;
	static boolean inPausedState = false;
	static boolean toastSuccess = false;
	static boolean useMenu = true;
	static boolean setupDone = false;
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

	public static UploadQueue uq;

	public static final int UPLOAD_OK_REQUESTED = 90000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(true);

		f = new Fields();
		dfm = new DataFieldManager(Integer.parseInt(experimentNumber), rapi,
				mContext, f);
		dfm.getOrder();
		uq = new UploadQueue("carrampphysics", mContext, rapi);
		uq.buildQueueFromFile();

		dateString = "";

		w = new Waffle(mContext);

		// pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		// wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakelock");
		// where login was

		mHandler = new Handler();

		startStop = (Button) findViewById(R.id.startStop);

		values = (TextView) findViewById(R.id.values);

		/*
		 * This block useful for if onBackPressed - retains some things from
		 * previous session
		 */

		startStop.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				// vibrator.vibrate(300);
				mMediaPlayer.setLooping(false);
				mMediaPlayer.start();

				// wl.acquire();

				if (running) {

					if (timeHasElapsed) {
						OrientationManager
								.enableRotation(CarRampPhysicsV2.this);
						setupDone = false;
						timeHasElapsed = false;
						useMenu = true;
						countdown = 10;

						mSensorManager
								.unregisterListener(CarRampPhysicsV2.this);
						running = false;
						startStop.setText("Hold to Start");

						timeTimer.cancel();
						count++;
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
							Toast.makeText(
									mContext,
									"Your app has timed out, you may not upload data any longer.",
									Toast.LENGTH_LONG).show();

					} else if (usedHomeButton) {
						setupDone = false;
						timeHasElapsed = false;
						useMenu = true;
						countdown = 10;

						mSensorManager
								.unregisterListener(CarRampPhysicsV2.this);
						running = false;
						startStop.setText("Hold to Start");

						timeTimer.cancel();
						count++;
						startStop.getBackground().clearColorFilter();
						choiceViaMenu = false;
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
						Toast.makeText(
								getBaseContext(),
								"Data recording has offset 100 milliseconds due to an error.",
								Toast.LENGTH_SHORT).show();
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

							count = (count + 1) % 1;
							elapsedMillis += INTERVAL;

							if (i >= 200) {

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
								SharedPreferences prefs = getSharedPreferences(
										RecordSettings.RECORD_SETTINGS, 0);

								x = prefs.getBoolean("X", x);
								y = prefs.getBoolean("Y", y);
								z = prefs.getBoolean("Z", z);
								mag = prefs.getBoolean("Magnitude", mag);
								if (x) {
									f.accel_x = toThou.format(accel[0]);
								}
								if (y) {
									f.accel_y = toThou.format(accel[1]);
								}
								if (z) {
									f.accel_z = toThou.format(accel[2]);
								}
								if (mag) {
									f.accel_total = toThou.format(accel[3]);
								}

								dataSet.put(dfm.putData());

							}

						}
					}, 0, INTERVAL);
					startStop.getBackground().setColorFilter(0xFF00FF00,
							PorterDuff.Mode.MULTIPLY);
				}
				// wl.release();
				return running;

			}

		});

		// vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		// mlocation stuff used to be here honk
		// .getBestProvider(c, true), 0, 0, CarRampPhysics.this);

		accel = new float[4];
		orientation = new float[3];
		rawAccel = new float[3];
		rawMag = new float[3];
		// loc = new Location(mLocationManager.getBestProvider(c, true));

		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);

		if (rapi.isConnectedToInternet()) {
			boolean success = rapi.login(userName, password);
			if (!success) {
				if (rapi.connection == "600") {
					w.make("Connection timed out.", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
					appTimedOut = true;
				} else {

				}
			}

		} else {
			Toast.makeText(
					this,
					"You are not connected to the Internet. Data saving enabled",
					Toast.LENGTH_LONG).show();
		}

	}

	public static final int EULA_REQUESTED = 8000;

	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = getPackageManager().getPackageInfo(getPackageName(),
					PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}

	void displayEula() {

		PackageInfo versionInfo = getPackageInfo();

		final String eulaKey = EulaActivity.EULA_PREFIX
				+ versionInfo.versionCode;
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean hasBeenShown = prefs.getBoolean(eulaKey, false);

		if (!hasBeenShown) {
			startActivityForResult(new Intent(this, EulaActivity.class),
					EULA_REQUESTED);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(eulaKey, true);
			editor.commit();

		}

	}

	long getUploadTime(int millisecond) {

		Calendar c = Calendar.getInstance();

		return (long) (c.getTimeInMillis() /*- 14400000*/);

	}

	@Override
	public void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(CarRampPhysicsV2.this);
		mSensorManager.unregisterListener(CarRampPhysicsV2.this);
		if (timeTimer != null)
			timeTimer.cancel();
		inPausedState = true;
	}

	@Override
	public void onStop() {
		super.onStop();
		mLocationManager.removeUpdates(CarRampPhysicsV2.this);
		mSensorManager.unregisterListener(CarRampPhysicsV2.this);
		if (timeTimer != null)
			timeTimer.cancel();
		inPausedState = true;
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

		// wl.release();

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

		values.setText(dataLabel);

		// if (running)

		if (firstName.equals("") || lastInitial.equals("")) {
			if (!dontPromptMeTwice) {
				startActivityForResult(new Intent(mContext,
						EnterNameActivity.class), resultGotName);
			}
		}

		if (uq != null)
			uq.buildQueueFromFile();

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
			setupDone = false;
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about_app:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case R.id.login:
			DialogFragment frag = new LoginDialogFragment();
			frag.show(getSupportFragmentManager(), "Login");
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
		}
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
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

			if (mag)
				accel[3] = (float) Math.sqrt(Math.pow(accel[0], 2)
						+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2));

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

			if (count == 0) {
				values.setText(data);
				// + ", Z: " + zPrepend + oneDigit.format(accel[2]));
			}

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
		Dialog dialog;// = builder.setView(new View(this)).create();

		// dialog.show();

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

		switch (id) {

		case DIALOG_FORCE_STOP:

			usedHomeButton = true;
			builder.setTitle("Data Recording Halted")
					.setMessage(
							"You exited the app while data were still being recorded.  Data recording has terminated.")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
									startStop.performLongClick();
									usedHomeButton = false;
								}
							});

			dialog = builder.create();

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

		case R.id.about_app:

			builder.setTitle("About")
					.setMessage(R.string.about_app)
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
										boolean success = rapi.login(userName,
												password);
										if (success) {
											Toast.makeText(mContext,
													"Connectivity found!",
													Toast.LENGTH_SHORT).show();
											if (!dontPromptMeTwice) {
												startActivityForResult(
														new Intent(
																mContext,
																EnterNameActivity.class),
														resultGotName);
											}
										} else {
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

		case DIALOG_EXPIRED:

			builder.setTitle("Timed Out")
					.setMessage(
							"This app has expired and you will no longer be able to use it for safety and security reasons. "
									+ "However, you may view our other apps on Google Play and download them there. Would "
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
									if (rapi.isConnectedToInternet()) {
										boolean success = rapi.login(userName,
												password);
										if (!success) {
											if (rapi.connection == "600") {
												showDialog(DIALOG_EXPIRED);
												appTimedOut = true;
											} else {
												showDialog(DIALOG_DIFFICULTY);
											}

										} else {
											if (firstName.length() == 0
													|| lastInitial.length() == 0) {
												if (!dontPromptMeTwice) {
													startActivityForResult(
															new Intent(
																	mContext,
																	EnterNameActivity.class),
															resultGotName);
												}
											}
										}
									}
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
			dialog.show(); /* works but doesnt center it */

			lp.copyFrom(dialog.getWindow().getAttributes());
			lp.width = mwidth;
			lp.height = WindowManager.LayoutParams.MATCH_PARENT;
			lp.gravity = Gravity.CENTER_VERTICAL;
			lp.dimAmount = 0.7f;

			dialog.getWindow().setAttributes(lp);
			dialog.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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

			if (dialog != null) {
				dialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						removeDialog(id);
					}
				});
			}

			return dialog;
		}

	}

	static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	public static final int EXPERIMENT_REQUESTED = 9000;
	public static final int QUEUE_UPLOAD_REQUESTED = 5000;

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		dontPromptMeTwice = false;

		if (reqCode == EXPERIMENT_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences prefs = getSharedPreferences("EID", 0);
				experimentNumber = prefs.getString("experiment_id", null);
				if (experimentNumber == null)
					experimentNumber = "409";
				dfm = new DataFieldManager(Integer.parseInt(experimentNumber),
						rapi, mContext, f);
				dfm.getOrder();
			}
		} else if (reqCode == QUEUE_UPLOAD_REQUESTED) {
			uq.buildQueueFromFile();

		} else if (reqCode == UPLOAD_OK_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (len == 0 || len2 == 0)
					Toast.makeText(this, "There are no data to upload!",
							Toast.LENGTH_LONG).show();

				else
					new UploadTask().execute();
			} else {
				Toast.makeText(this, "Data thrown away!", Toast.LENGTH_LONG)
						.show();
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
			} else {

				uploadSuccessful = false;
				DataSet ds = new DataSet(DataSet.Type.DATA, nameOfSession,
						"Car Ramp Physics", experimentNumber,
						dataSet.toString(), null, sessionId, city, state,
						country, addr);
				CarRampPhysicsV2.uq.addDataSetToQueue(ds);
				// CarRampPhysicsV2.uq.buildQueueFromFile();

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
				Toast.makeText(mContext, "Data upload successful.",
						Toast.LENGTH_SHORT).show();
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
				Toast.makeText(mContext, "Connectivity found!",
						Toast.LENGTH_SHORT).show();
			else
				showDialog(DIALOG_NO_CONNECT);
		}
	}

	public AlertDialog loginDialog(Context c) {
		LayoutInflater factory = LayoutInflater.from(c);
		final View textEntryView = factory.inflate(R.layout.login, null);
		final AlertDialog.Builder failAlert = new AlertDialog.Builder(c);
		failAlert.setTitle("Login/ Register Failed");
		failAlert.setNegativeButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		AlertDialog.Builder alert = new AlertDialog.Builder(c);
		alert.setTitle("Login to iSENSE");
		alert.setMessage("");
		alert.setView(textEntryView);
		alert.setPositiveButton("Login", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					final EditText usernameInput = (EditText) textEntryView
							.findViewById(R.id.userNameEditText);
					final EditText passwordInput = (EditText) textEntryView
							.findViewById(R.id.passwordEditText);
					RestAPI rapi = RestAPI
							.getInstance(
									(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
									getApplicationContext());
					if (rapi.isConnectedToInternet()) {
						boolean success = rapi
								.login(usernameInput.getText().toString(),
										passwordInput.getText().toString());
						if (success) {
							Toast.makeText(
									getApplicationContext(),
									"Login as "
											+ usernameInput.getText()
													.toString()
											+ " successful.",
									Toast.LENGTH_SHORT).show();
							loggedInAs.setText(getResources().getString(
									R.string.logged_in_as)
									+ " " + usernameInput.getText().toString());

						} else {
							Toast.makeText(
									getApplicationContext(),
									"Incorrect login credentials. Please try again.",
									Toast.LENGTH_SHORT).show();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		return alert.create();
	}

	private void manageUploadQueue() {

		if (!uq.emptyQueue()) {
			Intent i = new Intent().setClass(mContext, QueueLayout.class);
			i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
			startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
		} else {
			Toast.makeText(this, "No data to upload", Toast.LENGTH_LONG).show();
		}
	}

}