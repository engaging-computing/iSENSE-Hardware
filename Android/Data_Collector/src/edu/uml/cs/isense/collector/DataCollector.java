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

package edu.uml.cs.isense.collector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.text.method.NumberKeyListener;
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
import edu.uml.cs.isense.comm.RestAPI;

/* Experiment 422 on iSENSE and 277 on Dev */

public class DataCollector extends Activity implements SensorEventListener,
		LocationListener {

	private static EditText experimentInput;
	private static EditText seats;
	private static Spinner rides;
	private static TextView rideName;
	private static TextView time;
	private static CheckBox canobieCheck;

	private Button startStop;
	private Button browseButton;
	private TextView values;
	private Boolean running = false;
	private Vibrator vibrator;
	private TextView picCount;
	private TextView loginInfo;
	private String rideNameString = "NOT SET";
	private SensorManager mSensorManager;
	private LocationManager mLocationManager;
	private LocationManager mRoughLocManager;
	private Location loc;
	private Location roughLoc;
	private Timer timeTimer;

	private float rawAccel[];
	private float rawMag[];
	private float accel[];
	private float orientation[];
	private String temperature = "";
	private String pressure = "";
	private String light = "";

	private static final int INTERVAL = 200;

	private static final int MENU_ITEM_SETUP = 0;
	private static final int MENU_ITEM_LOGIN = 1;
	private static final int MENU_ITEM_UPLOAD = 2;
	private static final int MENU_ITEM_TIME = 3;

	private static final int SAVE_DATA = 4;
	private static final int DIALOG_SUMMARY = 5;
	private static final int DIALOG_CHOICE = 6;
	private static final int EXPERIMENT_CODE = 7;
	private static final int DIALOG_NO_ISENSE = 8;
	private static final int RECORDING_STOPPED = 9;
	private static final int DIALOG_NO_GPS = 10;
	private static final int DIALOG_FORCE_STOP = 11;

	public static final int DIALOG_CANCELED = 0;
	public static final int DIALOG_OK = 1;
	public static final int DIALOG_PICTURE = 2;

	public static final int CAMERA_PIC_REQUESTED = 1;
	public static final int CAMERA_VID_REQUESTED = 2;
	public static final int SYNC_TIME_REQUESTED = 3;
	public static final int CHOOSE_SENSORS_REQUESTED = 4;
	
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
	private static final int TEMPERATURE = 13;
	private static final int PRESSURE = 14;
	private static final int ALTITUDE = 15;
	private static final int LIGHT = 16;

	private int count = 0;
	private String data;

	private Uri imageUri;
	private Uri videoUri;

	private MediaPlayer mMediaPlayer;

	private ArrayList<File> pictures;
	private ArrayList<File> videos;

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

	RestAPI rapi;
	Waffle w;
	public static DataFieldManager dfm;
	Fields f;
	public static SensorCompatibility sc;
	LinkedList<String> acceptedFields;

	DecimalFormat toThou = new DecimalFormat("#,###,##0.000");

	ProgressDialog dia;
	double partialProg = 1.0;

	private EditText sessionName;
	String nameOfSession = "";
	String partialSessionName = "";

	public static boolean inPausedState = false;

	private static int mwidth = 1;
	private static int mediaCount = 0;
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

	public static JSONArray dataSet;

	public static Context mContext;

	private static ArrayList<File> pictureArray = new ArrayList<File>();

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

						mSensorManager.unregisterListener(DataCollector.this);
						running = false;
						startStop.setText(R.string.startString);
						time.setText(R.string.timeElapsed);
						rideName.setText("Ride/St#: NOT SET");

						timeTimer.cancel();
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

						registerSensors();

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

						data = "X Acceleration, Y Acceleration, Z Acceleration, Acceleration, "
								+ "Latitude, Longitude, Heading, Magnetic X, Magnetic Y, Magnetic Z, Time\n";
						running = true;
						startStop.setText(R.string.stopString);

						timeTimer = new Timer();
						timeTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {

								dataPointCount++;
								count = (count + 1) % 2;
								elapsedMillis += INTERVAL;
								totalMillis = elapsedMillis;

								if ((i % 5) == 0) {
									mHandler.post(new Runnable() {
										@Override
										public void run() {
											setTime(secondsElapsed++);
										}
									});
								}

								if (i >= 3000) {

									timeTimer.cancel();

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

									// NO NO
									/*data = toThou.format(accel[0]) + ", "
											+ toThou.format(accel[1]) + ", "
											+ toThou.format(accel[2]) + ", "
											+ toThou.format(accel[3]) + ", "
											+ loc.getLatitude() + ", "
											+ loc.getLongitude() + ", "
											+ toThou.format(orientation[0])
											+ ", " + rawMag[0] + ", "
											+ rawMag[1] + ", " + rawMag[2]
											+ ", " + currentTime
											+ elapsedMillis + "\n";*/
									
									data = "Nothing, nothing\n";

									if (dfm.enabledFields[ACCEL_X])
										f.accel_x = toThou.format(accel[0]);
									if (dfm.enabledFields[ACCEL_Y])
										f.accel_y = toThou.format(accel[1]);
									if (dfm.enabledFields[ACCEL_Z])
										f.accel_z = toThou.format(accel[2]);
									if (dfm.enabledFields[ACCEL_TOTAL])
										f.accel_total = toThou.format(accel[3]);
									if (dfm.enabledFields[LATITUDE])
										f.latitude = loc.getLatitude();
									if (dfm.enabledFields[LONGITUDE])
										f.longitude = loc.getLongitude();
									if (dfm.enabledFields[HEADING_DEG])
										f.angle_deg = toThou.format(orientation[0]);
									if (dfm.enabledFields[HEADING_RAD])
										f.angle_rad = "" + (Double.parseDouble(f.angle_deg) * (Math.PI / 180));
									if (dfm.enabledFields[MAG_X])
										f.mag_x = rawMag[0];
									if (dfm.enabledFields[MAG_Y])
										f.mag_y = rawMag[1];
									if (dfm.enabledFields[MAG_Z])
										f.mag_z = rawMag[2];
									if (dfm.enabledFields[MAG_TOTAL])
										f.mag_total = Math.sqrt(Math
												.pow(f.mag_x, 2)
												+ Math.pow(f.mag_y, 2)
												+ Math.pow(f.mag_z, 2));
									if (dfm.enabledFields[TIME])
										f.timeMillis = currentTime + elapsedMillis;
									if (dfm.enabledFields[TEMPERATURE])
										f.temperature = temperature;
									if (dfm.enabledFields[PRESSURE])
										f.pressure = pressure;
									if (dfm.enabledFields[ALTITUDE])
										f.altitude = calcAltitude();
									if (dfm.enabledFields[LIGHT])
										f.lux = light;

									dataSet.put(dfm.putData());

									if (beginWrite) {
										writeToSDCard(data, 's');
									} else {
										writeToSDCard(data, 'u');
									}

								}

							}
						}, 0, INTERVAL);
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
					DataCollector.this);
			mRoughLocManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, DataCollector.this);
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
			Date dt = new Date();

			dateString = sdf.format(dt);

			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/iSENSE");

			if (!folder.exists()) {
				folder.mkdir();
			}

			SDFile = new File(folder, rides.getSelectedItem() + "-" + stNumber
					+ "-" + dateString + ".csv");

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

	// Adds pictures to the SD Card
	public void pushPicture() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
		Date dt = new Date();

		dateString = sdf.format(dt);

		if (rideNameString.equals("NOT SET"))
			rideNameString = "Unspecified Ride";

		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}

		for (int i = 0; i < pictures.size(); i++) {
			File f = pictures.get(i);
			File newFile = new File(folder, rideNameString + "-" + stNumber
					+ "-" + dateString + "-" + (i + 1) + ".jpeg");
			f.renameTo(newFile);
			pictureArray.add(newFile);
		}

		pictures.clear();
	}

	// Adds videos to the SD Card
	public void pushVideo() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
		Date dt = new Date();

		dateString = sdf.format(dt);

		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}

		for (int i = 0; i < videos.size(); i++) {
			File f = videos.get(i);
			File newFile = new File(folder, rideNameString + "-" + stNumber
					+ "-" + dateString + "-" + (i + 1) + ".3gp");
			f.renameTo(newFile);
		}

		videos.clear();
	}

	@Override
	public void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(DataCollector.this);
		mRoughLocManager.removeUpdates(DataCollector.this);
		mSensorManager.unregisterListener(DataCollector.this);
		if (timeTimer != null)
			timeTimer.cancel();
		inPausedState = true;
		if (pictures.size() > 0)
			pushPicture();
		if (videos.size() > 0)
			pushVideo();
	}

	@Override
	public void onStop() {
		super.onStop();
		mLocationManager.removeUpdates(DataCollector.this);
		mRoughLocManager.removeUpdates(DataCollector.this);
		mSensorManager.unregisterListener(DataCollector.this);
		if (timeTimer != null)
			timeTimer.cancel();
		inPausedState = true;
		if (pictures.size() > 0)
			pushPicture();
		if (videos.size() > 0)
			pushVideo();
	}

	@Override
	public void onStart() {
		super.onStart();
		inPausedState = false;
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
				DataCollector.mContext,
				DataCollector.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		if (!(mPrefs.getString("username", "").equals("")))
			login();

		picCount.setText(getString(R.string.picAndVidCount) + mediaCount);
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
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!useMenu) {
			menu.getItem(MENU_ITEM_SETUP).setEnabled(false);
			menu.getItem(MENU_ITEM_LOGIN).setEnabled(false);
			menu.getItem(MENU_ITEM_UPLOAD).setEnabled(false);
			menu.getItem(MENU_ITEM_TIME).setEnabled(false);
		} else {
			menu.getItem(MENU_ITEM_SETUP).setEnabled(true);
			menu.getItem(MENU_ITEM_LOGIN).setEnabled(true);
			menu.getItem(MENU_ITEM_UPLOAD).setEnabled(true);
			menu.getItem(MENU_ITEM_TIME).setEnabled(true);
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SETUP:
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
			Intent i = new Intent(DataCollector.this, SyncTime.class);
			startActivityForResult(i, SYNC_TIME_REQUESTED);
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
			if (dfm.enabledFields[ACCEL_X] || dfm.enabledFields[ACCEL_Y] || 
					dfm.enabledFields[ACCEL_Z] || dfm.enabledFields[ACCEL_TOTAL]) {
				
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

				accel[3] = (float) Math.sqrt(Math.pow(accel[0], 2)
						+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2));
			}

		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			if (dfm.enabledFields[MAG_X] || dfm.enabledFields[MAG_Y] || 
					dfm.enabledFields[MAG_Z] || dfm.enabledFields[MAG_TOTAL] ||
					dfm.enabledFields[HEADING_DEG] || dfm.enabledFields[HEADING_RAD]) {
				
				rawMag = event.values.clone();

				float rotation[] = new float[9];

				
				if (SensorManager.getRotationMatrix(rotation, null, rawAccel,
						rawMag)) {
					orientation = new float[3];
					SensorManager.getOrientation(rotation, orientation);
				}
			}

		} else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
			if (dfm.enabledFields[TEMPERATURE])
				temperature = toThou.format(event.values[0]);
		} else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
			if (dfm.enabledFields[PRESSURE])
				pressure = toThou.format(event.values[0]);
		} else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			if (dfm.enabledFields[LIGHT])
			light = toThou.format(event.values[0]);
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
						if (pictures.size() > 0)
							pushPicture();
						if (videos.size() > 0)
							pushVideo();
						break;
					case DIALOG_CANCELED:
						canobieIsChecked = canobieBackup;
						if (pictures.size() > 0)
							pushPicture();
						if (videos.size() > 0)
							pushVideo();
						break;
					}
					rideName.setText("Ride/St#: " + rideNameString + " "
							+ stNumber);

				}
			}, "Configure Options");
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (pictures.size() > 0)
						pushPicture();
					if (videos.size() > 0)
						pushVideo();
				}
			});

			sessionName.setText(partialSessionName);
			break;

		case MENU_ITEM_LOGIN:
			LoginActivity la = new LoginActivity(this);
			dialog = la.getDialog(new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case LoginActivity.LOGIN_SUCCESSFULL:
						final SharedPreferences mPrefs = new ObscuredSharedPreferences(
								DataCollector.mContext, DataCollector.mContext
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
		case DIALOG_SUMMARY:

			mediaCount = 0;
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
				appendMe = "Filename: \n" + rideNameString + "-" + stNumber
						+ "-" + dateString;

			builder.setTitle("Session Summary")
					.setMessage(
							"Elapsed time: " + s_elapsedMinutes + ":"
									+ s_elapsedSeconds + "." + s_elapsedMillis
									+ "\n" + "Data points: " + dataPointCount
									+ "\n" + "End date and time: \n"
									+ dateString + "\n" + appendMe)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									dialoginterface.dismiss();
									picCount.setText(getString(R.string.picAndVidCount)
											+ mediaCount);
								}
							}).setCancelable(true);

			dialog = builder.create();
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
										showDialog(DIALOG_SUMMARY);
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
										showDialog(DIALOG_SUMMARY);
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
		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		experimentInput.setText(mPrefs.getString("experiment_id", ""));

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

		Button b = (Button) v.findViewById(R.id.pictureButton);

		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {

					ContentValues values = new ContentValues();

					imageUri = getContentResolver().insert(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							values);

					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
					intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
					startActivityForResult(intent, CAMERA_PIC_REQUESTED);

				} else {
					w.make("Permission isn't granted to write to external storage.  Please enable to take pictures.",
							Toast.LENGTH_LONG, "x");
				}

			}

		});

		Button bv = (Button) v.findViewById(R.id.videoButton);

		bv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {

					ContentValues valuesVideos = new ContentValues();

					videoUri = getContentResolver().insert(
							MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							valuesVideos);

					Intent intentVid = new Intent(
							MediaStore.ACTION_VIDEO_CAPTURE);
					intentVid.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
					intentVid.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
					startActivityForResult(intentVid, CAMERA_VID_REQUESTED);

				} else {
					w.make("Permission isn't granted to write to external storage.  Please enable to record videos.",
							Toast.LENGTH_LONG, "x");
				}
			}
		});

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
						if (pass) {
							rideIndex = rides.getSelectedItemPosition();
							stNumber = seats.getText().toString();

							rideNameString = (String) rides.getSelectedItem();

							nameOfSession = sessionName.getText().toString()
									+ " - " + rideNameString + " " + stNumber;

							new SensorCheckTask().execute();

							SharedPreferences mPrefs = getSharedPreferences(
									"EID", 0);
							SharedPreferences.Editor mEditor = mPrefs.edit();
							mEditor.putString("experiment_id",
									experimentInput.getText().toString())
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

	// Converts the captured picture's uri to a file that is save-able to the SD
	// Card
	@SuppressWarnings("deprecation")
	public static File convertImageUriToFile(Uri imageUri, Activity activity) {

		int apiLevel = getApiLevel();
		if (apiLevel >= 11) {

			String[] proj = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media._ID,
					MediaStore.Images.ImageColumns.ORIENTATION };
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;

			CursorLoader cursorLoader = new CursorLoader(mContext, imageUri,
					proj, selection, selectionArgs, sortOrder);

			Cursor cursor = cursorLoader.loadInBackground();

			int file_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			int orientation_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			if (cursor.moveToFirst()) {
				@SuppressWarnings("unused")
				String orientation = cursor.getString(orientation_ColumnIndex);
				return new File(cursor.getString(file_ColumnIndex));
			}
			return null;

		} else {

			Cursor cursor = null;
			try {
				String[] proj = { MediaStore.Images.Media.DATA,
						MediaStore.Images.Media._ID,
						MediaStore.Images.ImageColumns.ORIENTATION };
				cursor = activity.managedQuery(imageUri, proj, // Which columns
																// to return
						null, // WHERE clause; which rows to return (all rows)
						null, // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)
				int file_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				int orientation_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
				if (cursor.moveToFirst()) {
					@SuppressWarnings("unused")
					String orientation = cursor
							.getString(orientation_ColumnIndex);
					return new File(cursor.getString(file_ColumnIndex));
				}
				return null;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
	}

	// Converts the recorded video's uri to a file that is save-able to the SD
	// Card
	@SuppressWarnings("deprecation")
	public static File convertVideoUriToFile(Uri videoUri, Activity activity) {

		int apiLevel = getApiLevel();
		if (apiLevel >= 11) {

			String[] proj = { MediaStore.Video.Media.DATA,
					MediaStore.Video.Media._ID };
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;

			CursorLoader cursorLoader = new CursorLoader(mContext, videoUri,
					proj, selection, selectionArgs, sortOrder);

			Cursor cursor = cursorLoader.loadInBackground();
			int file_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			if (cursor.moveToFirst()) {
				return new File(cursor.getString(file_ColumnIndex));
			}
			return null;

		} else {

			Cursor cursor = null;

			try {
				String[] proj = { MediaStore.Video.Media.DATA,
						MediaStore.Video.Media._ID };
				cursor = activity.managedQuery(videoUri, proj, // Which columns
																// to return
						null, // WHERE clause; which rows to return (all rows)
						null, // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)
				int file_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
				if (cursor.moveToFirst()) {
					return new File(cursor.getString(file_ColumnIndex));
				}
				return null;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

	}

	// Performs tasks after returning to main UI from previous activities
	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CAMERA_PIC_REQUESTED) {
			if (resultCode == RESULT_OK) {
				File f = convertImageUriToFile(imageUri, this);
				pictures.add(f);
				mediaCount++;
				picCount.setText(getString(R.string.picAndVidCount)
						+ mediaCount);
			}
		} else if (requestCode == CAMERA_VID_REQUESTED) {
			if (resultCode == RESULT_OK) {
				File f = convertVideoUriToFile(videoUri, this);
				videos.add(f);
				mediaCount++;
				picCount.setText("" + getString(R.string.picAndVidCount)
						+ mediaCount);
			}
		} else if (requestCode == EXPERIMENT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				int eid = data.getExtras().getInt(
						"edu.uml.cs.isense.pictures.experiments.exp_id");
				experimentInput.setText("" + eid);
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
		} else if (requestCode == CHOOSE_SENSORS_REQUESTED) {
			if (resultCode == RESULT_OK) {
				acceptedFields = ChooseSensorDialog.acceptedFields;
				getEnabledFields();
			} else if (resultCode == RESULT_CANCELED) {
				// do more stuff
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

			try {
				if (roughLoc != null) {
					address = new Geocoder(DataCollector.this,
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

			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String eid = mPrefs.getString("experiment_id", "");

			if (address == null || address.size() <= 0) {
				sessionId = rapi.createSession(eid, nameOfSession,
						"Automated Submission Through Android App", "N/A",
						"N/A", "United States");
			} else {
				sessionId = rapi.createSession(eid, nameOfSession,
						"Automated Submission Through Android App", addr, city
								+ ", " + state, country);
			}

			// createSession Success Check
			if (sessionId == -1) {
				uploadSuccess = false;
				return;
			}

			// Experiment Closed Checker
			if (sessionId == -400) {
				status400 = true;
			} else {
				status400 = false;
				uploadSuccess = rapi.putSessionData(sessionId, experimentInput
						.getText().toString(), dataSet);
				if (!uploadSuccess)
					return;

				int pic = pictureArray.size();

				while (pic > 0) {
					if (nameOfSession.equals(""))
						rapi.uploadPictureToSession(pictureArray.get(pic - 1),
								eid, sessionId, "*Session Name Not Provided*",
								"N/A");
					else
						rapi.uploadPictureToSession(pictureArray.get(pic - 1),
								eid, sessionId, sessionName.getText()
										.toString(), "N/A");

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

			dia = new ProgressDialog(DataCollector.this);
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

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Void voids) {

			dia.setMessage("Done");
			dia.cancel();

			len = 0;
			len2 = 0;
			mediaCount = 0;

			if (status400)
				w.make("Your data cannot be uploaded to this experiment.  It has been closed.",
						Toast.LENGTH_LONG, "x");
			else if (!uploadSuccess)
				w.make("An error occured during upload.  Please check internet connectivity.",
						Toast.LENGTH_LONG, "x");
			else
				w.make("Upload Success", Toast.LENGTH_SHORT, "check");

			picCount.setText(getString(R.string.picAndVidCount) + mediaCount);
			showDialog(DIALOG_SUMMARY);

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

		picCount = (TextView) findViewById(R.id.pictureCount);
		picCount.setText(getString(R.string.picAndVidCount) + mediaCount);

		loginInfo = (TextView) findViewById(R.id.loginInfo);
		loginInfo.setText(R.string.notLoggedIn);
		loginInfo.setTextColor(Color.RED);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mRoughLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		w = new Waffle(this);
		f = new Fields();
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
				DataCollector.mContext,
				DataCollector.mContext.getSharedPreferences("USER_INFO",
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

	// Registers Sensors
	private void registerSensors() {
		if (mSensorManager != null && setupDone && dfm != null) {
			
			if (dfm.enabledFields[ACCEL_X] || dfm.enabledFields[ACCEL_Y] || 
					dfm.enabledFields[ACCEL_Z] || dfm.enabledFields[ACCEL_TOTAL]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_FASTEST);
			}
			
			if (dfm.enabledFields[MAG_X] || dfm.enabledFields[MAG_Y] || 
					dfm.enabledFields[MAG_Z] || dfm.enabledFields[MAG_TOTAL] ||
					dfm.enabledFields[HEADING_DEG] || dfm.enabledFields[HEADING_RAD]) {
				mSensorManager.registerListener(DataCollector.this, 
						mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
						SensorManager.SENSOR_DELAY_FASTEST);
			}
			
			if (dfm.enabledFields[TEMPERATURE] || dfm.enabledFields[ALTITUDE]) {
				mSensorManager.registerListener(DataCollector.this, 
						mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
						SensorManager.SENSOR_DELAY_FASTEST);
			}
			
			if (dfm.enabledFields[PRESSURE] || dfm.enabledFields[ALTITUDE]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
						SensorManager.SENSOR_DELAY_FASTEST);
			}
			
			if (dfm.enabledFields[LIGHT]) {
				mSensorManager.registerListener(DataCollector.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

		}
	}

	// Calculates Altitude from Temperature and Pressure if Possible
	private String calcAltitude() {
		if (dfm.enabledFields[TEMPERATURE] && dfm.enabledFields[PRESSURE]) {
			if (pressure.equals("") || temperature.equals("")) {
				return "";
			} else {
				double temp = Math.pow(1013.25 / Double.parseDouble(pressure),
						(1 / 5.257));
				temp *= (Double.parseDouble(temperature) + 273.15);
				temp /= 0.0065;

				String altitude = toThou.format(temp);
				return altitude;
			}
		} else return "";
	}

	// Task for checking sensor availability along with enabling/disabling
	private class SensorCheckTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(DataCollector.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Gathering experiment fields...");
			dia.setCancelable(false);
			dia.show();

		}

		@Override
		protected Void doInBackground(Void... voids) {

			dfm = new DataFieldManager(Integer
					.parseInt(experimentInput.getText()
							.toString()), rapi, mContext, f);
			dfm.getOrder();

			for (String s : dfm.order) {
				Log.d("field_order", s);
			}
			
			sc = dfm.checkCompatibility();

			publishProgress(100);
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {

			dia.setMessage("Done");
			dia.cancel();

			Intent i = new Intent(mContext, ChooseSensorDialog.class);
			startActivityForResult(i, CHOOSE_SENSORS_REQUESTED);
			
		}
	}
	
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
			if (s.equals(getString(R.string.temperature)))
				dfm.enabledFields[TEMPERATURE] = true;
			if (s.equals(getString(R.string.pressure)))
				dfm.enabledFields[PRESSURE] = true;
			if (s.equals(getString(R.string.altitude)))
				dfm.enabledFields[ALTITUDE] = true;
			if (s.equals(getString(R.string.luminous_flux)))
				dfm.enabledFields[LIGHT] = true;
		}
	}

}