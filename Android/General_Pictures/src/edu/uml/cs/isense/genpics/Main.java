package edu.uml.cs.isense.genpics;

import java.io.File;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.genpics.dialogs.LoginActivity;
import edu.uml.cs.isense.genpics.dialogs.NoGps;
import edu.uml.cs.isense.genpics.experiments.BrowseExperiments;
import edu.uml.cs.isense.genpics.objects.Picture;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.waffle.Waffle;

public class Main extends Activity implements LocationListener {
	private static final int CAMERA_PIC_REQUESTED = 101;
	private static final int LOGIN_REQUESTED = 102;
	private static final int NO_GPS_REQUESTED = 103;

	private static final int DIALOG_NO_GPS = 1;
	private static final int DIALOG_DIFFICULTY = 2;
	private static final int DIALOG_NO_CONNECT = 3;
	private static final int DIALOG_NOT_LOGGED_IN = 4;
	private static final int DIALOG_READY_TO_UPLOAD = 5;

	private static final int MENU_ITEM_BROWSE = 0;
	private static final int MENU_ITEM_LOGIN = 1;

	private static final int EXPERIMENT_CODE = 101;

	private static final int TIMER_LOOP = 1000;

	private LocationManager mLocationManager;
	private PowerManager pm;
	private PowerManager.WakeLock wl;

	private Uri imageUri;

	private Queue<Picture> mQ;
	private static int QUEUE_COUNT = 0;

	RestAPI rapi;

	private static boolean gpsWorking = false;
	private static boolean userLoggedIn = false;
	private static boolean smartUploading = false;
	private static boolean calledBySmartUp = false;
	private static boolean finishedUploadSetup = false;
	private static boolean uploadError = false;
	private static boolean status400 = false;
	public  static boolean initialLoginStatus = true;
	private static boolean showGpsDialog = true;

	private Picture uploaderPic = null;

	private EditText name;
	private TextView experimentLabel;
	private Vibrator vibrator;
	private Timer mTimer = null;
	private Handler mHandler;
	private TextView latLong;
	private TextView queueCount;
	private static final double DEFAULT_LAT = 42.6404;
	private static final double DEFAULT_LONG = -71.3533;
	private double Lat = 0;
	private double Long = 0;
	private long curTime;
	private static int waitingCounter = 0;

	public static Context mContext;

	private Waffle w;

	private File picture;

	public static Button takePicture;

	static boolean useMenu = true;

	private ProgressDialog dia;

	@Override
	protected Dialog onCreateDialog(final int id) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Dialog dialog;
		switch (id) {

		case DIALOG_NO_GPS:
			builder.setTitle("No GPS Provider Found")
					.setMessage(
							"Enabling GPS satellites is recommended for this application.  Would you like to enable GPS?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
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
								@Override
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.cancel();
								}
							});

			dialog = builder.create();
			break;

		case DIALOG_NO_CONNECT:

			builder.setTitle("No Connectivity")
					.setMessage(
							"Could not connect to the internet through either wifi or mobile service. "
									+ "You will not be able to use this app until either is enabled.")
					.setPositiveButton("Turn on Smart-Uploading",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									smartUploading = true;
									waitingForConnectivity();
									if (wl.isHeld())
										wl.acquire();
									dialoginterface.dismiss();
								}
							})
					.setNegativeButton("Try Again",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

									ObscuredSharedPreferences mPrefs = new ObscuredSharedPreferences(
											mContext, getSharedPreferences(
													"USER_INFO",
													Context.MODE_PRIVATE));

									if (rapi.isConnectedToInternet()) {
										dialoginterface.dismiss();
										boolean success = rapi.login(
												mPrefs.getString("username", ""),
												mPrefs.getString("password", ""));
										if (success) {
											w.make("Connectivity found!",
													Waffle.LENGTH_SHORT,
													Waffle.IMAGE_CHECK);
											userLoggedIn = true;
										}
									} else {
										userLoggedIn = false;
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

		case DIALOG_DIFFICULTY:

			builder.setTitle("Difficulties")
					.setMessage(
							"This application has experienced WiFi connection difficulties.  Try to reconfigure your WiFi "
									+ "settings or turn it off and on, then hit \"Try Again\".")
					.setPositiveButton("Try Again",
							new DialogInterface.OnClickListener() {
								@Override
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

		case DIALOG_NOT_LOGGED_IN:

			builder.setTitle("Upload Failed")
					.setMessage(
							"This application is having trouble logging into iSENSE, and therefore cannot upload data at this time.  "
									+ "Press try again to reattempt login.")
					.setPositiveButton("Try Again",
							new DialogInterface.OnClickListener() {
								@Override
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

		case DIALOG_READY_TO_UPLOAD:

			builder.setTitle("Ready to Upload Pictures")
					.setMessage(
							"Now that there is a connection to iSENSE, would you like to upload your pictures?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialoginterface,
										final int id) {
									dialoginterface.dismiss();
									if (QUEUE_COUNT > 0) {
										uploadPicture();
									}
								}
							}).setCancelable(false);

			dialog = builder.create();
			break;

		default:
			dialog = null;
		}

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

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_CALL) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;

		w = new Waffle(mContext);

		mQ = new LinkedList<Picture>();

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		SharedPreferences mPrefs2 = getSharedPreferences("LOGIN", 0);

		boolean isLoggedIn = mPrefs2.getBoolean("logged_in", false);
		if (!isLoggedIn) {
			startActivityForResult(new Intent(getApplicationContext(),
					LoginActivity.class), LOGIN_REQUESTED);
		}

		experimentLabel = (TextView) findViewById(R.id.ExperimentLabel);
		experimentLabel.setText(getResources().getString(R.string.experiment)
				+ mPrefs.getString("experiment_number", "None Set"));

		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Full Wake Lock");

		// keyguardManager = (KeyguardManager)
		// getSystemService(KEYGUARD_SERVICE);
		// lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mHandler = new Handler();

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());

		name = (EditText) findViewById(R.id.name);

		latLong = (TextView) findViewById(R.id.myLocation);
		queueCount = (TextView) findViewById(R.id.queueCountLabel);
		queueCount.setText("Queue Count: " + QUEUE_COUNT);

		takePicture = (Button) findViewById(R.id.takePicture);
		takePicture.getBackground().setColorFilter(0xFFFFFF33,
				PorterDuff.Mode.MULTIPLY);
		takePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (name.getText().length() == 0) {
					name.setError("Enter a name");
					return;
				} else {
					name.setError(null);
				}

				SharedPreferences mPrefs = getSharedPreferences("EID", 0);
				String experimentNum = mPrefs.getString("experiment_number",
						"Error");

				if (experimentNum.equals("Error")) {
					w.make("Please select an experiment first.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_X);
					return;
				}

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
					w.make("Cannot write to external storage.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_X);

				}

			}

		});

	}

	@Override
	public void onBackPressed() {
		if (!w.isDisplaying) {
			w.make("Double press \"Back\" to exit.", Waffle.LENGTH_SHORT,
					Waffle.IMAGE_CHECK);
		} else if (w.canPerformTask) 
			super.onBackPressed();	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ITEM_BROWSE, Menu.NONE, "Pick Experiment");
		menu.add(Menu.NONE, MENU_ITEM_LOGIN, Menu.NONE, "Login");
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!useMenu) {
			menu.getItem(0).setEnabled(false);
		} else {
			menu.getItem(0).setEnabled(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_BROWSE:

			Intent experimentIntent = new Intent(getApplicationContext(),
					BrowseExperiments.class);
			experimentIntent.putExtra(
					"edu.uml.cs.isense.pictures.experiments.prupose",
					EXPERIMENT_CODE);
			startActivityForResult(experimentIntent, EXPERIMENT_CODE);

			return true;

		case MENU_ITEM_LOGIN:
			startActivityForResult(new Intent(getApplicationContext(),
					LoginActivity.class), LOGIN_REQUESTED);
			return true;
		}

		return false;
	}

	@Override
	protected void onResume() {
		if (!smartUploading && !rapi.isConnectedToInternet())
			showDialog(DIALOG_NO_CONNECT);
		if (!userLoggedIn && !initialLoginStatus)
			attemptLogin();
		if (userLoggedIn) {
			if (smartUploading && (QUEUE_COUNT > 0))
				showDialog(DIALOG_READY_TO_UPLOAD);
		}
		// lock.reenableKeyguard();
		super.onResume();
	}

	@Override
	protected void onStart() {
		if (!gpsWorking)
			initLocManager();
		if (mTimer == null)
			waitingForGPS();
		// lock.reenableKeyguard();
		super.onStart();
	}

	public static File convertImageUriToFile(Uri imageUri, Activity activity) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaColumns.DATA, BaseColumns._ID,
					MediaStore.Images.ImageColumns.ORIENTATION };
			cursor = activity.managedQuery(imageUri, proj, // Which columns to
															// return
					null, // WHERE clause; which rows to return (all rows)
					null, // WHERE clause selection arguments (none)
					null); // Order-by clause (ascending by name)
			int file_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaColumns.DATA);
			int orientation_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			if (cursor.moveToFirst()) {
				@SuppressWarnings("unused")
				String orientation = cursor.getString(orientation_ColumnIndex);
				return new File(cursor.getString(file_ColumnIndex));
			}
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	private Runnable uploader = new Runnable() {
		@Override
		public void run() {
			new TaskWait().execute();

			if ((Lat == 0) || (Long == 0)) {
				Lat = DEFAULT_LAT;
				Long = DEFAULT_LONG;
			}

			if (!smartUploading || (smartUploading && !calledBySmartUp)) {

				SharedPreferences mPrefs = getSharedPreferences("EID", 0);
				String experimentNum = mPrefs.getString("experiment_number",
						"Error");

				if (experimentNum.equals("Error")) {
					uploadError = true;
					return;
				}

				int sessionId = rapi.createSession(experimentNum, name
						.getText().toString(), "" + curTime, "", "Lowell, MA",
						"");

				if (sessionId == -1) {
					uploadError = true;
					return;
				}

				JSONArray dataJSON = new JSONArray();
				try {
					dataJSON.put(curTime);
					dataJSON.put(Lat);
					dataJSON.put(Long);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				finishedUploadSetup = true;
				dia.setProgress(95);

				// Experiment Closed Checker
				if (sessionId == -400) {
					status400 = true;
					return;
				} else {
					status400 = false;
					if (!rapi.updateSessionData(sessionId, experimentNum,
							dataJSON)) {
						uploadError = true;
						return;
					}
					dia.setProgress(99);

					if (!rapi.uploadPictureToSession(picture, experimentNum,
							sessionId, name.getText().toString(),
							"No description provided.")) {
						uploadError = true;
					}
				}
			} else {
				smartUploader(uploaderPic.file, uploaderPic.latitude,
						uploaderPic.longitude, uploaderPic.name,
						uploaderPic.time);
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CAMERA_PIC_REQUESTED) {
			if (resultCode == RESULT_OK) {
				curTime = System.currentTimeMillis();
				picture = convertImageUriToFile(imageUri, this);

				takePicture.setEnabled(true);
				if (smartUploading) {
					if (userLoggedIn) {
						calledBySmartUp = false;
						new UploadTask().execute();
					} else
						qsave(picture);
				} else {
					if (userLoggedIn) {
						calledBySmartUp = false;
						new UploadTask().execute();
					} else
						showDialog(DIALOG_NOT_LOGGED_IN);
				}

			}

		} else if (requestCode == EXPERIMENT_CODE) {
			if (resultCode == Activity.RESULT_OK) {

				SharedPreferences mPrefs = getSharedPreferences("EID", 0);
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putString(
						"experiment_number",
						""
								+ data.getExtras()
										.getInt("edu.uml.cs.isense.pictures.experiments.exp_id"));
				mEditor.commit();

				experimentLabel.setText(getResources().getString(
						R.string.experiment)
						+ mPrefs.getString("experiment_number", "None Set"));
			}
		} else if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences mPrefs = getSharedPreferences("LOGIN", 0);
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putBoolean("logged_in", true);
				mEditor.commit();
			}
		} else if (requestCode == NO_GPS_REQUESTED) {
			showGpsDialog = true;
			if (resultCode == RESULT_OK) {
				startActivity(new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}

		}

	}

	@Override
	public void onLocationChanged(Location location) {
		if (((Lat = location.getLatitude()) != 0)
				&& ((Long = location.getLongitude()) != 0)) {
			if (gpsWorking == false) {
				vibrator.vibrate(100);
			}
			gpsWorking = true;
		} else {
			Lat = DEFAULT_LAT;
			Long = DEFAULT_LONG;
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

	private class UploadTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			finishedUploadSetup = false;
			dia = new ProgressDialog(Main.this);
			dia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dia.setMessage("Please wait while your picture is uploaded...");
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
		public void onProgressUpdate(Integer... prog) {
			if (prog == null)
				return;
			dia.setProgress(prog[0]);
		}

		@Override
		protected void onPostExecute(Void voids) {
			dia.setMessage("Done");
			dia.cancel();

			if (status400)
				w.make("Your data cannot be uploaded to this experiment.  It has been closed.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			else if (uploadError)
				w.make("An error occured during upload.", Waffle.LENGTH_LONG,
						Waffle.IMAGE_X);
			else
				w.make("Your picture has uploaded successfully.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_CHECK);

			uploadError = false;

			if (QUEUE_COUNT > 0)
				uploadPicture();
		}
	}

	private class TaskWait extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			for (int i = 1; i <= 85; i++) {
				if (!finishedUploadSetup) {
					try {
						publishProgress(i);
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		@Override
		public void onProgressUpdate(Integer... prog) {
			if (prog == null)
				return;
			dia.setProgress(prog[0]);
		}

		@Override
		protected void onPostExecute(Void voids) {
			dia.setMessage("Finalizing...");
		}
	}

	// repeatedly tries to connect
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
				w.make("Connectivity found!", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
			else {
				userLoggedIn = false;
				if (!smartUploading)
					showDialog(DIALOG_NO_CONNECT);
			}
		}
	}

	// gets the user's name if not already provided + login to web site
	private void attemptLogin() {

		ObscuredSharedPreferences mPrefs = new ObscuredSharedPreferences(
				mContext, getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		if (rapi.isConnectedToInternet()) {
			boolean success = rapi.login(mPrefs.getString("username", ""),
					mPrefs.getString("password", ""));
			if (!success) {
				showDialog(DIALOG_DIFFICULTY);
			} else {
				userLoggedIn = true;
				if (smartUploading && (QUEUE_COUNT > 0))
					showDialog(DIALOG_READY_TO_UPLOAD);
			}

		} else {
			userLoggedIn = false;
			if (!smartUploading)
				showDialog(DIALOG_NO_CONNECT);
		}
	}

	// initialize location listener to get a point
	private void initLocManager() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0, Main.this);
			new Location(mLocationManager.getBestProvider(c, true));
		} else {
			if (showGpsDialog) {
				Intent iNoGps = new Intent(mContext, NoGps.class);
				startActivityForResult(iNoGps, NO_GPS_REQUESTED);
				showGpsDialog = false;
			}
		}
			
	}

	// save picture data in a queue for later upload
	private void qsave(File pictureFile) {
		Picture mPic = new Picture(pictureFile, Lat, Long, name.getText()
				.toString(), System.currentTimeMillis());
		mQ.add(mPic);
		QUEUE_COUNT++;
		queueCount.setText("Queue Count: " + QUEUE_COUNT);
	}

	// get picture data from the q to upload
	private Picture getPicFromQ() {
		Picture mPic = null;
		try {
			mPic = mQ.remove();
		} catch (NoSuchElementException e) {
		}
		return mPic;
	}

	// upload stuff from the queue
	private void smartUploader(File f, double lat, double lon, String n, long t) {

		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		String experimentNum = mPrefs.getString("experiment_number", "Error");

		if (experimentNum.equals("Error")) {
			uploadError = true;
			return;
		}

		int sessionId;
		if ((sessionId = rapi.createSession(experimentNum, n,
				"No description provided.", "n/a", "Lowell, MA", "")) == -1) {
			uploadError = true;
			return;
		}

		JSONArray dataJSON = new JSONArray();
		try {
			dataJSON.put(t);
			dataJSON.put(lat);
			dataJSON.put(lon);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		finishedUploadSetup = true;
		dia.setProgress(90);

		// Experiment Closed Checker
		if (sessionId == -400) {
			status400 = true;
			return;
		} else {
			status400 = false;
			boolean success = rapi.updateSessionData(sessionId, experimentNum,
					dataJSON);
			if (!success) {
				uploadError = true;
				return;
			}
			dia.setProgress(99);

			success = rapi.uploadPictureToSession(f, experimentNum, sessionId,
					n, "No description provided.");
			if (!success)
				uploadError = true;
		}
	}

	// uploads pictures if smartUploading is enabled
	private void uploadPicture() {

		if (QUEUE_COUNT > 0) {
			uploaderPic = getPicFromQ();
			if (uploaderPic != null) {
				QUEUE_COUNT--;
				queueCount.setText("Queue Count: " + QUEUE_COUNT);
				calledBySmartUp = true;
				new UploadTask().execute();
			}
		} else {
			smartUploading = false;
			if (wl.isHeld())
				wl.release();
		}

	}

	@Override
	protected void onStop() {
		mLocationManager.removeUpdates(Main.this);
		gpsWorking = false;
		if (mTimer != null)
			mTimer.cancel();
		if (wl.isHeld())
			wl.release();
		mTimer = null;
		// lock.disableKeyguard();
		super.onStop();
	}

	// takes care of smart uploading as well as background checking
	private class WaitTenSecondsTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (rapi.isConnectedToInternet()) {
				if (userLoggedIn) {
					if (QUEUE_COUNT > 0) {
						showDialog(DIALOG_READY_TO_UPLOAD);
						waitingForConnectivity();
					}
				}

				ObscuredSharedPreferences mPrefs = new ObscuredSharedPreferences(
						mContext, getSharedPreferences("USER_INFO",
								Context.MODE_PRIVATE));
				boolean success = rapi.login(mPrefs.getString("username", ""),
						mPrefs.getString("password", ""));
				if (success) {
					w.make("Connectivity found!", Waffle.LENGTH_SHORT,
							Waffle.IMAGE_CHECK);
					userLoggedIn = true;
					if (QUEUE_COUNT > 0)
						showDialog(DIALOG_READY_TO_UPLOAD);

				}

			} else {
				userLoggedIn = false;
				if (smartUploading)
					waitingForConnectivity();
				else
					new NotConnectedTask().execute();
			}
			super.onPostExecute(result);
		}
	}

	public void waitingForConnectivity() {
		new WaitTenSecondsTask().execute();
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
							latLong.setText("Lat: " + Lat + "\nLong: " + Long);
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

}