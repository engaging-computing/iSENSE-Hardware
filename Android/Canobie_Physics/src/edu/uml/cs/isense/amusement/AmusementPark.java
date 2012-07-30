/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII            General Purpose Amusement Park App             SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                    Original Creator: John Fertita            SSS                **/
/**           III                    Edits By:         Jeremy Poulin,           SSS               **/
/**           III                                      Michael Stowell           SSSSSSSSS        **/
/**           III                    Faculty Advisor:  Fred Martin                      SSS       **/
/**           III                    Special Thanks:   Don Rhine                         SSS      **/
/**           III                    Group:            ECG, iSENSE                      SSS       **/
/**      IIIIIIIIIIIII               Property:         UMass Lowell              SSSSSSSSS        **/
/**                                                                                               **/
/***************************************************************************************************/
/***************************************************************************************************/

package edu.uml.cs.isense.amusement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.amusement.objects.DataFieldManager;
import edu.uml.cs.isense.amusement.objects.DataSet;
import edu.uml.cs.isense.amusement.objects.Fields;
import edu.uml.cs.isense.amusement.objects.SensorCompatibility;
import edu.uml.cs.isense.comm.RestAPI;

/* Experiment 422 on iSENSE and 491/277 on Dev */

@SuppressLint("NewApi")
public class AmusementPark extends Activity implements SensorEventListener,
		LocationListener {

	public static Queue<DataSet> uploadQueue;
	public static DataFieldManager dfm;
	public static SensorCompatibility sc;
	LinkedList<String> acceptedFields;
	Fields f;

	private static EditText experimentInput;
	private static EditText seats;
	private static Spinner rides;
	private static TextView rideName;
	private static TextView time;
	private static CheckBox canobieCheck;
	private static EditText sampleRate;

	private Button startStop;
	private Button browseButton;
	private TextView values;
	private Boolean running = false;
	private Vibrator vibrator;
	private TextView loginInfo;
	private String rideNameString = "NOT SET";
	private SensorManager mSensorManager;
	private LocationManager mLocationManager;
	private LocationManager mRoughLocManager;
	private Location loc;
	private Location roughLoc;
	private Timer timeTimer;
	private Timer timeElapsedTimer;

	private float rawAccel[];
	private float rawMag[];
	private float accel[];
	private float orientation[];

	private static final int INTERVAL = 200;
	private static long srate = INTERVAL;

	private static final int MENU_ITEM_SETUP = 0;
	private static final int MENU_ITEM_LOGIN = 1;
	private static final int MENU_ITEM_UPLOAD = 2;
	private static final int MENU_ITEM_TIME = 3;
	private static final int MENU_ITEM_MEDIA = 4;

	private static final int SAVE_DATA = 5;
	private static final int DIALOG_CHOICE = 6;
	private static final int DIALOG_NO_ISENSE = 7;
	private static final int RECORDING_STOPPED = 8;
	private static final int DIALOG_NO_GPS = 9;
	private static final int DIALOG_FORCE_STOP = 10;

	public static final int DIALOG_CANCELED = 0;
	public static final int DIALOG_OK = 1;
	public static final int DIALOG_PICTURE = 2;

	private static final int SYNC_TIME_REQUESTED = 0;
	private static final int QUEUE_UPLOAD_REQUESTED = 1;
	private static final int EXPERIMENT_CODE = 2;
	private static final int CHOOSE_SENSORS_REQUESTED = 3;

	private static final int TIME = 0;
	private static final int ACCEL_X = 1;
	private static final int ACCEL_Y = 2;
	private static final int ACCEL_Z = 3;
	private static final int ACCEL_TOTAL = 4;
	private static final int LATITUDE = 5;
	private static final int LONGITUDE = 6;
	private static final int MAG_X = 7;
	private static final int MAG_Y = 8;
	private static final int MAG_Z = 9;
	private static final int MAG_TOTAL = 10;
	private static final int HEADING_DEG = 11;
	private static final int HEADING_RAD = 12;
	private static final int TEMPERATURE_C = 13;
	private static final int PRESSURE = 14;
	private static final int ALTITUDE = 15;
	private static final int LIGHT = 16;

	private static final int TEMPERATURE_F = 17;
	private static final int TEMPERATURE_K = 18;

	private int count = 0;
	private String data;

	private MediaPlayer mMediaPlayer;

	public static ArrayList<File> pictures;
	public static ArrayList<File> videos;

	private static int rideIndex = 0;

	private int elapsedMinutes = 0;
	private int elapsedSeconds = 0;
	private int elapsedMillis = 0;
	private int totalMillis = 0;
	private int dataPointCount = 0;
	private int i = 0;
	private int len = 0;
	private int len2 = 0;
	private int secondsElapsed = 0;

	private long currentTime = 0;
	private long timeOffset = 0;

	private String dateString, s_elapsedSeconds, s_elapsedMillis,
			s_elapsedMinutes;

	public static RestAPI rapi;
	static Waffle w;

	DecimalFormat toThou = new DecimalFormat("#,###,##0.000");

	ProgressDialog dia;
	double partialProg = 1.0;

	private EditText sessionName;
	String nameOfSession = "";
	static String partialSessionName = "";

	public static boolean inPausedState = false;

	private static int mwidth = 1;
	private static boolean useMenu = true;
	private static boolean beginWrite = true;
	private static boolean setupDone = false;
	private static boolean choiceViaMenu = false;
	private static boolean canobieIsChecked = true;
	private static boolean canobieBackup = true;
	private static boolean successLogin = false;
	private static boolean status400 = false;
	private static boolean sdCardError = false;
	private static boolean uploadSuccess = false;

	private Handler mHandler;
	private boolean throughHandler = false;

	File SDFile;
	FileWriter gpxwriter;
	BufferedWriter out;

	public static String textToSession = "";
	public static String toSendOut = "";
	private static String stNumber = "1";

	private static String sdFileName = "";
	private static String niceDateString = "";

	public static JSONArray dataSet;

	public static Context mContext;

	public static ArrayList<File> pictureArray = new ArrayList<File>();

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;

		// Initialize everything you're going to need
		initVars();

		// Display the End User Agreement
		displayEula();

		// This block useful for if onBackPressed - retains some things from
		// previous session
		if (running)
			showDialog(DIALOG_FORCE_STOP);

		// Main Layout Button for Recording Data
		startStop.getBackground().setColorFilter(0xFFFF0000,
				PorterDuff.Mode.MULTIPLY);
		startStop.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				if (!setupDone || rideName.getText().toString() == null) {

					showDialog(MENU_ITEM_SETUP);
					w.make("You must setup before recording data.",
							Toast.LENGTH_LONG, "x");

				} else {

					vibrator.vibrate(300);
					mMediaPlayer.setLooping(false);
					mMediaPlayer.start();

					if (running) {

						writeToSDCard(null, 'f');
						setupDone = false;
						useMenu = true;

						mSensorManager.unregisterListener(AmusementPark.this);
						running = false;
						startStop.setText(R.string.startString);
						time.setText(R.string.timeElapsed);
						rideName.setText("Ride/St#: NOT SET");

						timeTimer.cancel();
						timeElapsedTimer.cancel();
						count++;
						startStop.getBackground().setColorFilter(0xFFFF0000,
								PorterDuff.Mode.MULTIPLY);
						choiceViaMenu = false;

						if (sdCardError)
							w.make("Could not write file to SD Card.",
									Toast.LENGTH_SHORT, "x");

						if (throughHandler)
							showDialog(RECORDING_STOPPED);
						else
							showDialog(DIALOG_CHOICE);

					} else {

						dataSet = new JSONArray();
						secondsElapsed = 0;
						elapsedMillis = 0;
						totalMillis = 0;
						len = 0;
						len2 = 0;
						dataPointCount = 0;
						i = 0;
						beginWrite = true;
						sdCardError = false;

						currentTime = getUploadTime();

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							w.make("Data recording interrupted! Time values may be inconsistent.",
									Toast.LENGTH_SHORT, "x");
							e.printStackTrace();
						}

						useMenu = false;

						if (mSensorManager != null) {
							mSensorManager
									.registerListener(
											AmusementPark.this,
											mSensorManager
													.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
											SensorManager.SENSOR_DELAY_FASTEST);
							mSensorManager
									.registerListener(
											AmusementPark.this,
											mSensorManager
													.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
											SensorManager.SENSOR_DELAY_FASTEST);
						}

						data = "X Acceleration, Y Acceleration, Z Acceleration, Acceleration, "
								+ "Latitude, Longitude, Heading, Magnetic X, Magnetic Y, Magnetic Z, Time\n";
						running = true;
						startStop.setText(R.string.stopString);
						
						timeElapsedTimer = new Timer();
						timeElapsedTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										setTime(secondsElapsed++);
									}
								});
							}
						}, 0, 1000);

						timeTimer = new Timer();
						timeTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {

								dataPointCount++;
								count = (count + 1) % 2;
								elapsedMillis += srate;
								totalMillis = elapsedMillis;

								if (i >= 3000) {

									timeTimer.cancel();
									timeElapsedTimer.cancel();
									
									mHandler.post(new Runnable() {
										@Override
										public void run() {
											throughHandler = true;
											startStop.performLongClick();
										}
									});

								} else {

									i++;
									len++;
									len2++;

									data = toThou.format(accel[0]) + ", "
											+ toThou.format(accel[1]) + ", "
											+ toThou.format(accel[2]) + ", "
											+ toThou.format(accel[3]) + ", "
											+ loc.getLatitude() + ", "
											+ loc.getLongitude() + ", "
											+ toThou.format(orientation[0])
											+ ", " + rawMag[0] + ", "
											+ rawMag[1] + ", " + rawMag[2]
											+ ", " + currentTime
											+ elapsedMillis + "\n";

									JSONArray dataJSON = new JSONArray();

									try {

										/* Accel-x */dataJSON.put(toThou
												.format(accel[0]));
										/* Accel-y */dataJSON.put(toThou
												.format(accel[1]));
										/* Accel-z */dataJSON.put(toThou
												.format(accel[2]));
										/* Accel-Total */dataJSON.put(toThou
												.format(accel[3]));
										/* Latitude */dataJSON.put(loc
												.getLatitude());
										/* Longitude */dataJSON.put(loc
												.getLongitude());
										/* Heading */dataJSON.put(toThou
												.format(orientation[0]));
										/* Magnetic-x */dataJSON.put(rawMag[0]);
										/* Magnetic-y */dataJSON.put(rawMag[1]);
										/* Magnetic-z */dataJSON.put(rawMag[2]);
										/* Time */dataJSON.put(currentTime
												+ elapsedMillis);

										dataSet.put(dataJSON);

									} catch (JSONException e) {
										e.printStackTrace();
									}

									if (beginWrite) {
										writeToSDCard(data, 's');
									} else {
										writeToSDCard(data, 'u');
									}

								}

							}
						}, 0, srate);
						startStop.getBackground().setColorFilter(0xFF00FF00,
								PorterDuff.Mode.MULTIPLY);
					}
					return running;

				}
				running = false;
				return running;
			}

		});

		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				&& mRoughLocManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0,
					AmusementPark.this);
			mRoughLocManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, AmusementPark.this);
		} else {
			showDialog(DIALOG_NO_GPS);
		}

		accel = new float[4];
		orientation = new float[3];
		rawAccel = new float[3];
		rawMag = new float[3];
		loc = new Location(mLocationManager.getBestProvider(c, true));

		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);

	}

	// (s)tarts, (u)pdates, and (f)inishes writing the .csv to the SD Card
	// containing "data"
	public void writeToSDCard(String data, char code) {
		switch (code) {
		case 's':
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
			SimpleDateFormat niceFormat = new SimpleDateFormat(
					"MM/dd/yyyy, HH:mm:ss");

			Date dt = new Date();

			dateString = sdf.format(dt);
			niceDateString = niceFormat.format(dt);

			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/iSENSE");

			if (!folder.exists()) {
				folder.mkdir();
			}

			sdFileName = rides.getSelectedItem() + "-" + stNumber + "-"
					+ dateString + ".csv";
			SDFile = new File(folder, sdFileName);

			try {
				gpxwriter = new FileWriter(SDFile);
				out = new BufferedWriter(gpxwriter);
				out.write(data);
				beginWrite = false;
			} catch (IOException e) {
				sdCardError = true;
			}

			break;

		case 'u':
			try {
				out.append(data);
			} catch (IOException e) {
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

	@Override
	public void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(AmusementPark.this);
		mRoughLocManager.removeUpdates(AmusementPark.this);
		mSensorManager.unregisterListener(AmusementPark.this);
		if (timeTimer != null)
			timeTimer.cancel();
		if (timeElapsedTimer != null)
			timeElapsedTimer.cancel();
		inPausedState = true;
	}

	@Override
	public void onStop() {
		super.onStop();
		mLocationManager.removeUpdates(AmusementPark.this);
		mRoughLocManager.removeUpdates(AmusementPark.this);
		mSensorManager.unregisterListener(AmusementPark.this);
		if (timeTimer != null)
			timeTimer.cancel();
		if (timeElapsedTimer != null)
			timeElapsedTimer.cancel();
		inPausedState = true;

		// stores uploadQueue in uploadqueue.ser (on SD card) and saves Q_COUNT
		storeQueue();
	}

	@Override
	public void onStart() {
		super.onStart();
		inPausedState = false;

		// rebuilds uploadQueue from saved info
		getUploadQueue();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
		super.onResume();
		inPausedState = false;
		if (running)
			showDialog(DIALOG_FORCE_STOP);

		// Will call the login dialogue if necessary and update UI
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				AmusementPark.mContext,
				AmusementPark.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		if (!(mPrefs.getString("username", "").equals("")))
			login();

	}

	// Overridden to prevent user from exiting app unless back button is pressed
	// twice
	@Override
	public void onBackPressed() {
		if (!w.dontToastMeTwice) {
			if (running)
				w.make("Cannot exit via BACK while recording data; use HOME instead.",
						Toast.LENGTH_LONG, "x");
			else
				w.make("Press back again to exit.", Toast.LENGTH_SHORT, "check");

			// new NoToastTwiceTask().execute();
		} else if (w.exitAppViaBack && !running) {
			setupDone = false;
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ITEM_SETUP, Menu.NONE, "Setup").setIcon(
				R.drawable.ic_menu_settings);
		menu.add(Menu.NONE, MENU_ITEM_LOGIN, Menu.NONE, "Login").setIcon(
				R.drawable.ic_menu_login);
		menu.add(Menu.NONE, MENU_ITEM_UPLOAD, Menu.NONE, "Upload").setIcon(
				R.drawable.ic_menu_upload);
		menu.add(Menu.NONE, MENU_ITEM_TIME, Menu.NONE, "Sync Time").setIcon(
				R.drawable.ic_menu_synctime);
		menu.add(Menu.NONE, MENU_ITEM_MEDIA, Menu.NONE, "Media").setIcon(
				R.drawable.ic_menu_media);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!useMenu) {
			menu.getItem(MENU_ITEM_SETUP).setEnabled(false);
			menu.getItem(MENU_ITEM_LOGIN).setEnabled(false);
			menu.getItem(MENU_ITEM_UPLOAD).setEnabled(false);
			menu.getItem(MENU_ITEM_TIME).setEnabled(false);
			menu.getItem(MENU_ITEM_MEDIA).setEnabled(false);
		} else {
			menu.getItem(MENU_ITEM_SETUP).setEnabled(true);
			menu.getItem(MENU_ITEM_LOGIN).setEnabled(true);
			menu.getItem(MENU_ITEM_UPLOAD).setEnabled(true);
			menu.getItem(MENU_ITEM_TIME).setEnabled(true);
			menu.getItem(MENU_ITEM_MEDIA).setEnabled(true);
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SETUP:
			startStop.setEnabled(false);
			showDialog(MENU_ITEM_SETUP);
			return true;
		case MENU_ITEM_LOGIN:
			showDialog(MENU_ITEM_LOGIN);
			return true;
		case MENU_ITEM_UPLOAD:
			choiceViaMenu = true;
			showDialog(DIALOG_CHOICE);
			return true;
		case MENU_ITEM_TIME:
			Intent iTime = new Intent(AmusementPark.this, SyncTime.class);
			startActivityForResult(iTime, SYNC_TIME_REQUESTED);
			return true;
		case MENU_ITEM_MEDIA:
			Intent iMedia = new Intent(AmusementPark.this, MediaManager.class);
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

		DecimalFormat oneDigit = new DecimalFormat("#,##0.0");

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			rawAccel = event.values.clone();
			accel[0] = event.values[0];
			accel[1] = event.values[1];
			accel[2] = event.values[2];

			String xPrepend = accel[0] > 0 ? "+" : "";
			String yPrepend = accel[1] > 0 ? "+" : "";
			String zPrepend = accel[2] > 0 ? "+" : "";

			if (count == 0) {
				values.setText("X: " + xPrepend + oneDigit.format(accel[0])
						+ ", Y: " + yPrepend + oneDigit.format(accel[1])
						+ ", Z: " + zPrepend + oneDigit.format(accel[2]));
			}

			accel[3] = (float) FloatMath.sqrt((float) ((Math.pow(accel[0], 2)
					+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2))));

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
		roughLoc = location;
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

	@SuppressLint("HandlerLeak")
	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(final int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Dialog dialog;// = builder.setView(new View(this)).create();

		// dialog.show();

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

		switch (id) {
		case MENU_ITEM_SETUP:

			dialog = getSavePrompt(new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case DIALOG_OK:
						partialSessionName = sessionName.getText().toString();
						setupDone = true;
						canobieBackup = canobieIsChecked;
						rideName.setText("Ride/St#: " + rideNameString + " "
								+ stNumber);
						new SensorCheckTask().execute();
						startStop.setEnabled(true); // TODO - move this to the onActiv of this intent ret
						break;
					case DIALOG_CANCELED:
						canobieIsChecked = canobieBackup;
						break;
					}
					rideName.setText("Ride/St#: " + rideNameString + " "
							+ stNumber);

				}
			}, "Configure Options");
			dialog.setCancelable(true);

			sessionName.setText(partialSessionName);
			break;

		case MENU_ITEM_LOGIN:
			LoginActivity la = new LoginActivity(this);
			dialog = la.getDialog(new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case LoginActivity.LOGIN_SUCCESSFULL:
						final SharedPreferences mPrefs = new ObscuredSharedPreferences(
								AmusementPark.mContext, AmusementPark.mContext
										.getSharedPreferences("USER_INFO",
												Context.MODE_PRIVATE));
						loginInfo.setText(" "
								+ mPrefs.getString("username", ""));
						loginInfo.setTextColor(Color.GREEN);
						successLogin = true;
						w.make("Login successful", Toast.LENGTH_LONG, "check");
						break;
					case LoginActivity.LOGIN_CANCELED:
						break;
					case LoginActivity.LOGIN_FAILED:
						successLogin = false;
						showDialog(MENU_ITEM_LOGIN);
						break;
					}
				}
			});
			break;

		case SAVE_DATA:

			dialog = getSavePrompt(new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case DIALOG_OK:
						if (len == 0 || len2 == 0)
							w.make("There is no data to upload!",
									Toast.LENGTH_LONG, "x");
						showDialog(DIALOG_CHOICE);
						partialSessionName = sessionName.getText().toString();
						break;
					case DIALOG_CANCELED:
						break;
					}
					rideName.setText("Ride/St#: " + rideNameString + " "
							+ stNumber);

				}
			}, "Final Step");
			sessionName.setText(partialSessionName);
			break;

		case DIALOG_CHOICE:

			builder.setTitle("Select An Action:")
					.setMessage(
							"Would you like to upload your data and media to iSENSE?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {

									dialoginterface.dismiss();

									if (len == 0 || len2 == 0)
										w.make("There is no data to upload!",
												Toast.LENGTH_LONG, "x");
									else {

										String isValid = experimentInput
												.getText().toString();
										if (successLogin
												&& (isValid.length() > 0)) {
											// executeIsenseTask = true;
											dialoginterface.dismiss();
											new Task().execute();
										} else {
											showDialog(DIALOG_NO_ISENSE);
										}

									}
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {

									dialoginterface.dismiss();
									if (!choiceViaMenu)
										showSummary();
								}
							}).setCancelable(true);

			dialog = builder.create();

			break;

		case DIALOG_NO_ISENSE:

			builder.setTitle("Cannot Upload to iSENSE")
					.setMessage(
							"You are either not logged into iSENSE, or you have not provided a valid Experiment ID to upload your data to. "
									+ "You will be returned to the main screen, but you may go to Menu -> Upload to upload this data set once you log in "
									+ "to iSENSE and provide a valid Experiment ID.  You are permitted to continue recording data; however if "
									+ "you choose to do so, you will not be able to upload the previous data set to iSENSE afterwards.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									dialoginterface.dismiss();
									if (!choiceViaMenu)
										showSummary();
								}
							}).setCancelable(true);

			dialog = builder.create();

			break;

		case RECORDING_STOPPED:

			throughHandler = false;

			builder.setTitle("Time Up")
					.setMessage(
							"You have been recording data for more than 10 minutes.  For the sake of memory, we have capped your maximum "
									+ "recording time at 10 minutes and have stopped recording for you.  Press OK to continue.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									dialoginterface.dismiss();
									showDialog(DIALOG_CHOICE);
								}
							}).setCancelable(false);

			dialog = builder.create();

			break;

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

			break;

		default:
			dialog = null;
			break;
		}

		return apiDialogCheckerCase(dialog, lp, id);

	}

	// Method to create a data-saving prompt with "message" as the message
	private AlertDialog getSavePrompt(final Handler h, String message) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		canobieIsChecked = canobieBackup;

		LayoutInflater vi = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = vi.inflate(R.layout.setup, null);

		builder.setView(v);

		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		
		rides = (Spinner) v.findViewById(R.id.rides);

		final ArrayAdapter<CharSequence> generalAdapter = ArrayAdapter
				.createFromResource(this, R.array.rides_array,
						android.R.layout.simple_spinner_item);
		generalAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		final ArrayAdapter<CharSequence> canobieAdapter = ArrayAdapter
				.createFromResource(this, R.array.canobie_array,
						android.R.layout.simple_spinner_item);
		canobieAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		if (canobieIsChecked)
			rides.setAdapter(canobieAdapter);
		else
			rides.setAdapter(generalAdapter);

		seats = (EditText) v.findViewById(R.id.studentNumber);

		sessionName = (EditText) v.findViewById(R.id.sessionName);

		experimentInput = (EditText) v.findViewById(R.id.ExperimentInput);
		experimentInput.setText(mPrefs.getString("experiment_id", ""));
		
		sampleRate = (EditText) v.findViewById(R.id.srate);
		sampleRate.setText(mPrefs.getString("sample_rate", ""));

		experimentInput.setKeyListener(new NumberKeyListener() {
			@Override
			public int getInputType() {
				return InputType.TYPE_CLASS_PHONE;
			}

			@Override
			protected char[] getAcceptedChars() {
				return new char[] { '0', '1', '2', '3', '4', '5', '6', '7',
						'8', '9' };
			}
		});

		browseButton = (Button) v.findViewById(R.id.BrowseButton);
		browseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!rapi.isConnectedToInternet()) {
					w.make("You must enable wifi or mobile connectivity to do this.",
							Toast.LENGTH_SHORT, "x");
				} else {

					Intent experimentIntent = new Intent(
							getApplicationContext(), Experiments.class);
					experimentIntent.putExtra(
							"edu.uml.cs.isense.amusement.experiments.propose",
							EXPERIMENT_CODE);

					startActivityForResult(experimentIntent, EXPERIMENT_CODE);
				}

			}

		});

		canobieCheck = (CheckBox) v.findViewById(R.id.isCanobie);
		if (canobieIsChecked)
			canobieCheck.setChecked(true);
		else
			canobieCheck.setChecked(false);

		canobieCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

				if (canobieCheck.isChecked()) {
					canobieIsChecked = true;
					rides.setAdapter(canobieAdapter);
				} else {
					canobieIsChecked = false;
					rides.setAdapter(generalAdapter);
				}

			}

		});

		rides.setSelection(rideIndex);
		seats.setText(stNumber);
		
		builder.setTitle(message)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialoginterface, int i) {
							}
						}).setCancelable(false);

		final AlertDialog ad = builder.create();
		ad.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Button positive = ad.getButton(DialogInterface.BUTTON_POSITIVE);
				positive.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean pass = true;

						if (sessionName.getText().length() == 0) {
							sessionName.setError("Enter a Name");
							pass = false;
						}
						if (experimentInput.getText().length() == 0) {
							experimentInput.setError("Enter an Experiment");
							pass = false;
						}
						if (seats.getText().length() == 0) {
							seats.setError("Enter a St#");
							pass = false;
						}
						if (sampleRate.getText().length() == 0) {
							sampleRate.setError("Enter a Sample Interval");
							pass = false;
						}
						if (Long.parseLong(sampleRate.getText().toString()) < 200) {
							sampleRate.setError("Interval Must be >= 200");
							pass = false;
						}
						
						if (pass) {
							rideIndex = rides.getSelectedItemPosition();
							stNumber = seats.getText().toString();

							rideNameString = (String) rides.getSelectedItem();

							nameOfSession = sessionName.getText().toString()
									+ " - " + rideNameString + " " + stNumber;

							srate = Long.parseLong(sampleRate.getText().toString());
							
							SharedPreferences mPrefs = getSharedPreferences(
									"EID", 0);
							SharedPreferences.Editor mEditor = mPrefs.edit();
							mEditor.putString("experiment_id",
									experimentInput.getText().toString())
									.commit();
							mEditor.putString("sample_rate",
									sampleRate.getText().toString())
									.commit();

							final Message dialogOk = Message.obtain();
							dialogOk.setTarget(h);
							dialogOk.what = DIALOG_OK;

							dialogOk.sendToTarget();

							ad.dismiss();
						}
					}
				});

				Button negative = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
				negative.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final Message dialogOk = Message.obtain();
						dialogOk.setTarget(h);
						dialogOk.what = DIALOG_CANCELED;
						dialogOk.sendToTarget();

						ad.dismiss();
					}
				});
			}
		});

		return ad;
	}

	// Performs tasks after returning to main UI from previous activities
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == EXPERIMENT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				int eid = data.getExtras().getInt(
						"edu.uml.cs.isense.experiments.exp_id");
				int sr  = data.getExtras().getInt(
						"edu.uml.cs.isense.experiments.srate");
				experimentInput.setText("" + eid);
				sampleRate.setText("" + sr);
			}
		} else if (requestCode == SYNC_TIME_REQUESTED) {
			if (resultCode == RESULT_OK) {
				timeOffset = data.getExtras().getLong("offset");
				SharedPreferences mPrefs = getSharedPreferences("time_offset",
						0);
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putLong("timeOffset", timeOffset);
				mEditor.commit();
				Log.e("intent", "timeOffset set to: " + timeOffset);
			} else if (resultCode == RESULT_CANCELED) {
				// oh no they canceled!
			}
		} else if (requestCode == QUEUE_UPLOAD_REQUESTED) {
			if (resultCode == RESULT_OK) {
				getUploadQueue();
			}
		}

	}

	// Assists with differentiating between displays for dialogues
	@SuppressWarnings("deprecation")
	private static int getApiLevel() {
		return Integer.parseInt(android.os.Build.VERSION.SDK);
	}

	// Calls the rapi primitives for actual uploading
	private Runnable uploader = new Runnable() {

		@Override
		public void run() {
			status400 = false;
			int sessionId = -1;
			String city = "", state = "", country = "", addr = "";
			List<Address> address = null;

			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy, HH:mm:ss");
			Date dt = new Date();
			String dateString = sdf.format(dt);

			try {
				if (roughLoc != null) {
					address = new Geocoder(AmusementPark.this,
							Locale.getDefault()).getFromLocation(
							roughLoc.getLatitude(), roughLoc.getLongitude(), 1);
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

			String description = "Automated Submission Through Android Canobie Physics App";

			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String eid = mPrefs.getString("experiment_id", "");

			if (address == null || address.size() <= 0) {
				sessionId = rapi.createSession(eid, nameOfSession, description,
						"N/A", "N/A", "United States");
			} else {
				sessionId = rapi.createSession(eid, nameOfSession, description,
						addr, city + ", " + state, country);
			}

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
					DataSet ds = new DataSet(DataSet.Type.DATA, nameOfSession
							+ " - " + dateString, description, eid,
							dataSet.toString(), null, sessionId, city, state,
							country, addr);
					uploadQueue.add(ds);
				}

				int pic = pictureArray.size();

				while (pic > 0) {
					boolean picSuccess = rapi.uploadPictureToSession(
							pictureArray.get(pic - 1), eid, sessionId,
							nameOfSession, description);

					// Saves pictures for later upload
					if (picSuccess) {
						DataSet ds = new DataSet(DataSet.Type.PIC,
								nameOfSession + " - " + dateString,
								description, eid, null,
								pictureArray.get(pic - 1), sessionId, city,
								state, country, addr);
						uploadQueue.add(ds);
					}
					pic--;
				}

				pictureArray.clear();
			}

		}

	};

	// Control task for uploading data
	private class Task extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(AmusementPark.this);
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

			len = 0;
			len2 = 0;
			MediaManager.mediaCount = 0;

			if (status400)
				w.make("Your data cannot be uploaded to this experiment.  It has been closed.",
						Toast.LENGTH_LONG, "x");
			else if (!uploadSuccess) {
				w.make("An error occured during upload.  Please check internet connectivity.",
						Toast.LENGTH_LONG, "x");
			} else {
				w.make("Upload Success", Toast.LENGTH_SHORT, "check");
				manageUploadQueue();
			}

			showSummary();

		}
	}

	public void setTime(int seconds) {
		int min = seconds / 60;
		int secInt = seconds % 60;

		String sec = "";
		if (secInt <= 9)
			sec = "0" + secInt;
		else
			sec = "" + secInt;

		time.setText("Time Elapsed: " + min + ":" + sec);
	}

	// Everything needed to be initialized for onCreate
	@SuppressWarnings("deprecation")
	private void initVars() {

		mHandler = new Handler();

		Display deviceDisplay = getWindowManager().getDefaultDisplay();
		mwidth = deviceDisplay.getWidth();

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());

		pictures = new ArrayList<File>();
		videos = new ArrayList<File>();

		startStop = (Button) findViewById(R.id.startStop);

		values = (TextView) findViewById(R.id.values);
		time = (TextView) findViewById(R.id.time);
		rideName = (TextView) findViewById(R.id.ridename);

		rideName.setText("Ride/St#: " + rideNameString);

		loginInfo = (TextView) findViewById(R.id.loginInfo);
		loginInfo.setText(R.string.notLoggedIn);
		loginInfo.setTextColor(Color.RED);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mRoughLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		w = new Waffle(this);
	}

	// Takes care of everything to do with EULA
	private void displayEula() {

		AlertDialog.Builder adb = new SimpleEula(this).show();
		if (adb != null) {
			Dialog dialog = adb.create();
			dialog.show();

			apiTabletDisplay(dialog);
		}
	}

	// apiTabletDisplay for Dialog Building on Tablets
	static boolean apiTabletDisplay(Dialog dialog) {
		int apiLevel = getApiLevel();
		if (apiLevel >= 11) {
			dialog.show();

			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

			lp.copyFrom(dialog.getWindow().getAttributes());
			lp.width = mwidth;
			lp.height = WindowManager.LayoutParams.MATCH_PARENT;
			lp.gravity = Gravity.CENTER_VERTICAL;
			lp.dimAmount = 0.7f;

			dialog.getWindow().setAttributes(lp);
			dialog.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_DIM_BEHIND);

			return true;
		} else
			return false;

	}

	// Deals with login and UI display
	void login() {
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				AmusementPark.mContext,
				AmusementPark.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		boolean success = rapi.login(mPrefs.getString("username", ""),
				mPrefs.getString("password", ""));
		if (success) {
			loginInfo.setText(" " + mPrefs.getString("username", ""));
			loginInfo.setTextColor(Color.GREEN);
			successLogin = true;
		}
	}

	// Gets the milliseconds since Epoch
	private long getUploadTime() {
		Calendar c = Calendar.getInstance();
		SharedPreferences mPrefs = getSharedPreferences("time_offset", 0);
		timeOffset = mPrefs.getLong("timeOffset", 0);

		return (((long) c.getTimeInMillis()) + timeOffset);
	}

	// Deals with Dialog creation whether api is tablet or not
	@SuppressWarnings("deprecation")
	Dialog apiDialogCheckerCase(Dialog dialog, LayoutParams lp, final int id) {
		if (apiTabletDisplay(dialog)) {

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

	// Task for checking sensor availability along with enabling/disabling
	private class SensorCheckTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			//OrientationManager.disableRotation(AmusementPark.this);

			dia = new ProgressDialog(AmusementPark.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Gathering experiment fields...");
			dia.setCancelable(false);
			dia.show();

		}

		@Override
		protected Void doInBackground(Void... voids) {

			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String eidInput = mPrefs.getString("experiment_id", "");

			dfm = new DataFieldManager(Integer.parseInt(eidInput), rapi,
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

			//OrientationManager.enableRotation(AmusementPark.this);

			Intent i = new Intent(mContext, ChooseSensorDialog.class);
			startActivityForResult(i, CHOOSE_SENSORS_REQUESTED);

		}
	}

	@SuppressWarnings("unused") //TODO - remove this suppresswarnings when it actually becomes used!!
	private void getEnabledFields() {
		for (String s : acceptedFields) {
			if (s.equals(getString(R.string.time)))
				dfm.enabledFields[TIME] = true;
			if (s.equals(getString(R.string.accel_x)))
				dfm.enabledFields[ACCEL_X] = true;
			if (s.equals(getString(R.string.accel_y)))
				dfm.enabledFields[ACCEL_Y] = true;
			if (s.equals(getString(R.string.accel_z)))
				dfm.enabledFields[ACCEL_Z] = true;
			if (s.equals(getString(R.string.accel_total)))
				dfm.enabledFields[ACCEL_TOTAL] = true;
			if (s.equals(getString(R.string.latitude)))
				dfm.enabledFields[LATITUDE] = true;
			if (s.equals(getString(R.string.longitude)))
				dfm.enabledFields[LONGITUDE] = true;
			if (s.equals(getString(R.string.magnetic_x)))
				dfm.enabledFields[MAG_X] = true;
			if (s.equals(getString(R.string.magnetic_y)))
				dfm.enabledFields[MAG_Y] = true;
			if (s.equals(getString(R.string.magnetic_z)))
				dfm.enabledFields[MAG_Z] = true;
			if (s.equals(getString(R.string.magnetic_total)))
				dfm.enabledFields[MAG_TOTAL] = true;
			if (s.equals(getString(R.string.heading_deg)))
				dfm.enabledFields[HEADING_DEG] = true;
			if (s.equals(getString(R.string.heading_rad)))
				dfm.enabledFields[HEADING_RAD] = true;
			if (s.equals(getString(R.string.temperature_c)))
				dfm.enabledFields[TEMPERATURE_C] = true;
			if (s.equals(getString(R.string.temperature_f)))
				dfm.enabledFields[TEMPERATURE_F] = true;
			if (s.equals(getString(R.string.temperature_k)))
				dfm.enabledFields[TEMPERATURE_K] = true;
			if (s.equals(getString(R.string.pressure)))
				dfm.enabledFields[PRESSURE] = true;
			if (s.equals(getString(R.string.altitude)))
				dfm.enabledFields[ALTITUDE] = true;
			if (s.equals(getString(R.string.luminous_flux)))
				dfm.enabledFields[LIGHT] = true;
		}
	}

	// Rebuilds uploadQueue from Q_COUNT and uploadqueue.ser
	public static void getUploadQueue() {

		uploadQueue = new LinkedList<DataSet>();

		// Makes sure there is an iSENSE folder
		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");
		if (!folder.exists())
			folder.mkdir();

		// Gets Q_COUNT back from Shared Prefs
		final SharedPreferences mPrefs = getSharedPreferences(mContext);
		int Q_COUNT = mPrefs.getInt("Q_COUNT", 0);

		try {
			// Deserialize the file as a whole
			File file = new File(folder.getAbsolutePath() + "/uploadqueue.ser");
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					file));
			// Deserialize the objects one by one
			for (int i = 0; i < Q_COUNT; i++) {
				DataSet dataSet = (DataSet) in.readObject();
				uploadQueue.add(dataSet);
			}
			in.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			w.make(e.toString(), Toast.LENGTH_SHORT, "x");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Prompts the user to upload the rest of their content
	// upon successful upload of data
	private void manageUploadQueue() {
		if (uploadSuccess) {
			if (!uploadQueue.isEmpty()) {
				Intent i = new Intent().setClass(mContext, QueueUploader.class);
				startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
			}
		}
	}

	// Saves Q_COUNT and uploadQueue into memory for later use
	public static void storeQueue() {

		// Save Q_COUNT in SharedPrefs
		final SharedPreferences mPrefs = getSharedPreferences(mContext);
		final SharedPreferences.Editor mPrefsEditor = mPrefs.edit();
		int Q_COUNT = uploadQueue.size();
		mPrefsEditor.putInt("Q_COUNT", Q_COUNT);
		mPrefsEditor.commit();

		// writes uploadqueue.ser
		File uploadQueueFile = new File(
				Environment.getExternalStorageDirectory() + "/iSENSE"
						+ "/uploadqueue.ser");
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(uploadQueueFile));

			// Serializes DataSets from uploadQueue into uploadqueue.ser
			while (Q_COUNT > 0) {
				DataSet ds = uploadQueue.remove();
				out.writeObject(ds);
				Q_COUNT--;
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// get shared Q_COUNT
	public static SharedPreferences getSharedPreferences(Context ctxt) {
		return ctxt.getSharedPreferences("Q_COUNT", MODE_PRIVATE);
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
				.putExtra("append", appendMe).putExtra("date", niceDateString)
				.putExtra("points", "" + dataPointCount);

		startActivity(iSummary);

	}
}