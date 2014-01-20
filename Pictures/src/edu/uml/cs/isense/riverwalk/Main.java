package edu.uml.cs.isense.riverwalk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.credentials.Login;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.riverwalk.dialogs.CameraPreview;
import edu.uml.cs.isense.riverwalk.dialogs.Continuous;
import edu.uml.cs.isense.riverwalk.dialogs.Description;
import edu.uml.cs.isense.riverwalk.dialogs.NoGps;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

//import android.app.ProgressDialog;

public class Main extends Activity implements LocationListener {
	private static final int CAMERA_PIC_REQUESTED = 101;
	private static final int LOGIN_REQUESTED = 102;
	private static final int NO_GPS_REQUESTED = 103;
	private static final int EXPERIMENT_REQUESTED = 104;
	private static final int QUEUE_UPLOAD_REQUESTED = 105;
	private static final int DESCRIPTION_REQUESTED = 106;
	private static final int SELECT_PICTURE_REQUESTED = 107;
	
	private MediaPlayer mMediaPlayer;

	public static boolean continuous = false;
	public static int continuousInterval = 1;
	private boolean recording = false;

	private static final int TIMER_LOOP = 1000;

	private LocationManager mLocationManager;
	private LocationManager mRoughLocManager;
	private Location loc;

	private Uri imageUri;

	public static API api;
	public static UploadQueue uq;
	public static final String activityName = "genpicsmain";

	private static boolean uploadError = false;
	private static boolean status400 = false;
	public static boolean initialLoginStatus = true;
	private static boolean showGpsDialog = true;

	private EditText name;
	private TextView experimentLabel;
	private Timer mTimer = null;
	private Handler mHandler;
	private TextView latLong;
	private TextView queueCount;
	private long curTime;
	private static int waitingCounter = 0;
	private static String descriptionStr = "";
	
	/* Action Bar */
	private static int actionBarTapCount = 0;
	private static boolean useDev = false;

	public static Context mContext;

	private Waffle w;
	private File picture;
	public static Button takePicture;
	public static Button addPicture;
	static boolean useMenu = true;

	// private ProgressDialog dia;
	private DataFieldManager dfm;
	private Fields f;

	public static final int MEDIA_TYPE_IMAGE = 1;
	private static Camera mCamera;
	private CameraPreview mPreview;
	private FrameLayout preview;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;

		// Initialize action bar customization for API >= 11
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			ActionBar bar = getActionBar();

			// make the actionbar clickable
			bar.setDisplayHomeAsUpEnabled(true);
		}

		useMenu = true;
		
		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);

		w = new Waffle(mContext);

		f = new Fields();

		api = API.getInstance();
		api.useDev(useDev);
		
		attemptLoginOnAppStart();
		
		uq = new UploadQueue("generalpictures", mContext, api);

		SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
		if (mPrefs.getString("project_id", "").equals("")) {
			if (dfm == null) {
				dfm = new DataFieldManager(Integer.parseInt(mPrefs.getString(
						"project_id", "-1")), api, mContext, f);
				dfm.getOrder();
			}
		}

		experimentLabel = (TextView) findViewById(R.id.ExperimentLabel);
		experimentLabel.setText(getResources().getString(R.string.experiment)
				+ mPrefs.getString("project_id", "None Set"));

		mHandler = new Handler();

		name = (EditText) findViewById(R.id.name);

		latLong = (TextView) findViewById(R.id.myLocation);
		queueCount = (TextView) findViewById(R.id.queueCountLabel);

		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.getLayoutParams().height = 0;

		addPicture = (Button) findViewById(R.id.addPicture);
		takePicture = (Button) findViewById(R.id.takePicture);

		if (continuous == true) {
			takePicture.setText(R.string.takePicContinuous);
			addPicture.setVisibility(View.GONE);
		}

		takePicture.setOnClickListener(new OnClickListener() {

			// Push take picture button
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// Check that a group name was entered, and a project was
				// selected
				if (name.getText().length() == 0) {
					name.setError("Enter a name");
					return;
				} else {
					name.setError(null);
				}

				SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
				String experimentNum = mPrefs.getString("project_id", "Error");

				if (experimentNum.equals("Error")) {
					w.make("Please select an project first.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_X);
					return;
				}

				// take a single picture when continuous mode is not active
				if (continuous == false) {
					String state = Environment.getExternalStorageState();
					if (Environment.MEDIA_MOUNTED.equals(state)) {

						ContentValues values = new ContentValues();

						imageUri = getContentResolver().insert(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								values);

						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
						intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

						OrientationManager.disableRotation(Main.this);
						startActivityForResult(intent, CAMERA_PIC_REQUESTED);

					} else {
						w.make("Cannot write to external storage.",
								Waffle.LENGTH_LONG, Waffle.IMAGE_X);
					}

					OrientationManager.enableRotation(Main.this);

					// Continuously take pictures
				} else if (continuous == true) {
					
					mMediaPlayer.setLooping(false);
					mMediaPlayer.start();
					
					if (recording == false) {
						// disable menu
						useMenu = false;
						if (android.os.Build.VERSION.SDK_INT >= 11)
							invalidateOptionsMenu();
						int dps = 176;

						final float scale = getResources().getDisplayMetrics().density;
						int pixels = (int) (dps * scale + 0.5f);
						preview.getLayoutParams().height = pixels;

						takePicture.setBackgroundResource(R.drawable.button_rsense_green);
						takePicture.setTextColor(0xFF000000);
						takePicture.setText("Recording Press to Stop");

						recording = true;

						safeCameraOpen(0);

						preview.setVisibility(View.VISIBLE);
						mPreview = new CameraPreview(mContext, mCamera);

						if (mPreview.getHolder() != null) {
							Log.d("Main", "mPreview is " + mPreview.getHolder());
						}

						preview.addView(mPreview);

						new continuouslytakephotos().execute();

						// Stop continuously taking pictures
					} else {
						Main.takePicture.setText(R.string.takePicContinuous);
						Main.takePicture.setTextColor(0xFF0066FF);
						Main.takePicture
								.setBackgroundResource(R.drawable.button_rsense);
						recording = false;
					}
				}
			}
		});

		/* Add a Picture to upload queue from gallery */
		addPicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (name.getText().length() == 0) {
					name.setError("Enter a name");
					return;
				} else {
					name.setError(null);
				}

				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						SELECT_PICTURE_REQUESTED);

			}

		});

	}

	// continuously take pictures in AsyncTask (a seperate thread)
	private class continuouslytakephotos extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			OrientationManager.disableRotation(Main.this);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			// this method will be running on background thread so don't update
			// UI frome here
			// do your long running http tasks here,you dont want to pass
			// argument and u can access the parent class' variable url over
			// here

			while (recording) {

				// sleep for interval as long as recording is equal to true
				for (int i = 0; i < continuousInterval && recording == true; i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Log.e(getString(R.string.app_name),
								"failed to sleep while continuously taking pictures");
						e.printStackTrace();
					}
				}

				// do not take one last picture when user pushes to stop
				// recording
				if (recording == false) {
					return null;
				}

				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {

					Log.d("CameraMain", "Camera is: " + mCamera.toString());
					Log.d("CameraMain", "About to try to take a picture.");

					runOnUiThread(new Runnable() {
						public void run() {
							try {
								mCamera.takePicture(null, null, mPicture); // takes
																			// a
																			// picture
							} catch (Exception e) {
								Log.d("CameraMain", "Failed taking picture");
								e.printStackTrace();
							}

							if (picture != null) {
								Log.d("CameraMain",
										"Successfully captured picture.");
							}
						}
					});

					curTime = System.currentTimeMillis();
					uploader.run();
					uq.buildQueueFromFile();

					runOnUiThread(new Runnable() {
						public void run() {
							w.make("Picture saved!", Waffle.LENGTH_SHORT,
									Waffle.IMAGE_CHECK);
							queueCount.setText(getResources().getString(
									R.string.queueCount)
									+ uq.queueSize());
							mCamera.stopPreview();
							mCamera.startPreview();
						}
					});

				} else {
					return null;
				}
			}
			return null;
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Boolean result) {// this method will be
														// running on UI thread
			super.onPostExecute(result);
			// enable menu
			useMenu = true;
			if (android.os.Build.VERSION.SDK_INT >= 11)
				invalidateOptionsMenu();

			Main.takePicture.setText(R.string.takePicContinuous);
			Main.takePicture.setTextColor(0xFF0066FF);
			Main.takePicture.setBackgroundResource(R.drawable.button_rsense);

			OrientationManager.enableRotation(Main.this);

			preview.getLayoutParams().height = 0;
			preview.removeView(mPreview);
			preview.setVisibility(View.INVISIBLE);

			recording = false;

			Log.d("CameraMain",
					"Camera in onPostExecute is:" + mCamera.toString());
			// mCamera.stopPreview();
			mCamera.release();
			mCamera = null;

			OrientationManager.enableRotation(Main.this);
		}
	}

	private boolean safeCameraOpen(int id) {
		boolean qOpened = false;
		try {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}

			Log.d("CameraMain",
					"Number of cameras " + Camera.getNumberOfCameras());
			Camera.CameraInfo c = new Camera.CameraInfo();

			Log.d("CameraMain", "Camera info cameras " + c.toString());
			mCamera = Camera.open(id);

			Display display = getWindowManager().getDefaultDisplay();
			int rotation = display.getRotation();

			if (rotation == Surface.ROTATION_0) {
				mCamera.setDisplayOrientation(90);
			} else if (rotation == Surface.ROTATION_90) {
				mCamera.setDisplayOrientation(0);
			} else if (rotation == Surface.ROTATION_270) {
				mCamera.setDisplayOrientation(180);
			}

			Log.d("CameraMain", "Camera is: " + mCamera.toString());
			qOpened = (mCamera != null);
		} catch (Exception e) {
			Log.e(getString(R.string.app_name), "failed to open Camera");
			e.printStackTrace();
		}

		return qOpened;
	}

	// called automatically after picture is taken to save picture data
	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			picture = getOutputMediaFile(MEDIA_TYPE_IMAGE);

			if (picture == null) {
				Log.d("CameraMain", "picture is null");
				return;
			}

			Log.d("CameraMain", "PictureCallback");

			try {
				FileOutputStream fos = new FileOutputStream(picture);
				fos.write(data);
				fos.close();
			} catch (IOException e) {
				Log.e("onPictureTaken in main", "failed to save picture");
			}

		}
	};

	/** Create a File for saving an image or video */
	@SuppressLint("SimpleDateFormat")
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
			// } else if(type == MEDIA_TYPE_VIDEO) {
			// mediaFile = new File(mediaStorageDir.getPath() + File.separator +
			// "VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * Turns the action bar menu on and off.
	 * 
	 * @return Whether or not the menu was prepared successfully.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (!useMenu) {
			menu.getItem(0).setEnabled(false);
			menu.getItem(1).setEnabled(false);
			menu.getItem(2).setEnabled(false);
			menu.getItem(3).setEnabled(false);

		}

		else {
			menu.getItem(0).setEnabled(true);
			menu.getItem(1).setEnabled(true);
			menu.getItem(2).setEnabled(true);
			menu.getItem(3).setEnabled(true);

		}
		return true;
	}

	// menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.MENU_ITEM_UPLOAD:
			manageUploadQueue();
			return true;

		case R.id.MENU_ITEM_BROWSE:
			Intent iExperiment = new Intent(getApplicationContext(),
					Setup.class);
			iExperiment.putExtra("constrictFields", true);
			iExperiment.putExtra("app_name", "Pictures");
			startActivityForResult(iExperiment, EXPERIMENT_REQUESTED);
			return true;

		case R.id.MENU_ITEM_LOGIN:
			startActivityForResult(new Intent(getApplicationContext(),
					Login.class), LOGIN_REQUESTED);
			return true;

		case R.id.MENU_ITEM_CONTINUOUS:
			Intent continuous = new Intent(getApplicationContext(),
					Continuous.class);
			startActivity(continuous);
			return true;
			
		case android.R.id.home:
			CountDownTimer cdt = null;

			// Give user 10 seconds to switch dev/prod mode
			if (actionBarTapCount == 0) {
				cdt = new CountDownTimer(5000, 5000) {
					public void onTick(long millisUntilFinished) {
					}

					public void onFinish() {
						actionBarTapCount = 0;
					}
				}.start();
			}

			String other = (useDev) ? "production" : "dev";

			switch (++actionBarTapCount) {
			case 5:
				w.make(getResources().getString(R.string.two_more_taps) + other
						+ getResources().getString(R.string.mode_type));
				break;
			case 6:
				w.make(getResources().getString(R.string.one_more_tap) + other
						+ getResources().getString(R.string.mode_type));
				break;
			case 7:
				w.make(getResources().getString(R.string.now_in_mode) + other
						+ getResources().getString(R.string.mode_type));
				useDev = !useDev;

				if (cdt != null)
					cdt.cancel();

				if (api.getCurrentUser() != null) {
					Runnable r = new Runnable() {
						public void run() {
							api.deleteSession();
							api.useDev(useDev);
						}
					};
					new Thread(r).start();
				} else
					api.useDev(useDev);

				actionBarTapCount = 0;
				break;
			}

			return true;
		
		default:
			return false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (recording == true) {
			if (mCamera == null) {
				safeCameraOpen(0);
			}
			preview.setVisibility(View.VISIBLE);
			mPreview = new CameraPreview(mContext, mCamera);
			preview.addView(mPreview);

		}

		if (api.getCurrentUser() == null)
			attemptLogin();

		// Rebuilds uploadQueue from saved info
		uq.buildQueueFromFile();
		queueCount.setText(getResources().getString(R.string.queueCount)
				+ uq.queueSize());
	}

	// uploads the data if logged in and queue is not empty
	private void manageUploadQueue() {

		if (api.getCurrentUser() == null) {
			w.make("Must be logged in to upload.", Waffle.IMAGE_X);
			return;
		}

		if (uq.emptyQueue()) {
			w.make("No data to upload.", Waffle.IMAGE_X);
			return;
		}

		Intent i = new Intent().setClass(mContext, QueueLayout.class);
		i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
		startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);

	}

	// onStart initialize location manager
	@Override
	protected void onStart() {
		super.onStart();

		initLocManager();

		if (mTimer == null)
			waitingForGPS();
	}

	private static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	@SuppressLint("NewApi")
	public static File convertImageUriToFile(Uri imageUri) {
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
				ContentResolver cr = mContext.getContentResolver();
				cursor = cr.query(imageUri, proj, // Which columns
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

	private void postRunnableWaffleError(final String message) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				w.make(message, Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			}
		});
	}

	/* Add pictures and Data assosiated with picture to queue */
	private Runnable uploader = new Runnable() {
		@Override
		public void run() {

			SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
			String projNum = mPrefs.getString("project_id", "Error");

			if (projNum.equals("Error")) {
				uploadError = true;
				postRunnableWaffleError("No project selected to upload pictures to");
				return;
			}

			// if (dfm == null)
			initDfm();

			JSONArray dataJSON = new JSONArray(); // data is set into JSONArray
													// to be uploaded

			if (!Connection.hasConnectivity(mContext))
				projNum = "-1";

			if (loc.getLatitude() != 0) {
				f.timeMillis = curTime;
				System.out.println("curTime =" + f.timeMillis);
				f.latitude = loc.getLatitude();
				System.out.println("Latitude =" + f.latitude);
				f.longitude = loc.getLongitude();
				System.out.println("Longitude =" + f.longitude);

				dataJSON.put(dfm.putData());

			} else { // no gps
				loc = mLocationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				f.timeMillis = curTime;
				System.out.println("curTime (no gps) =" + f.timeMillis);
				f.latitude = loc.getLatitude();
				System.out.println("Latitude (no gps) =" + f.latitude);
				f.longitude = loc.getLongitude();
				System.out.println("Longitude (no gps) =" + f.longitude);

				dataJSON.put(dfm.putData());
			}
			
			QDataSet ds;
			
			if (dfm.projectContainsTimeStamp() && dfm.projectContainsLocation()) {
				ds = new QDataSet(name.getText().toString()
						+ (descriptionStr.equals("") ? "" : ": " + descriptionStr),
						makeThisDatePretty(curTime), QDataSet.Type.BOTH,
						dataJSON.toString(), picture, projNum, null);
			} else {
				ds = new QDataSet(name.getText().toString()
						+ (descriptionStr.equals("") ? "" : ": " + descriptionStr),
						makeThisDatePretty(curTime), QDataSet.Type.PIC,
						null, picture, projNum, null);
			}
			
			

			System.out.println("projectNum = " + projNum);

			uq.addDataSetToQueue(ds);
			// TODO
		}
	};

	private void initDfm() { // sets up data field manager
		SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
		String experimentInput = mPrefs.getString("project_id", "");
		System.out.println("experimentInput =" + experimentInput);
		dfm = new DataFieldManager(Integer.parseInt(experimentInput), api,
				mContext, f);
		dfm.getOrderWithExternalAsyncTask();
		dfm.enableAllFields();
		System.out.println("order =" + dfm.getOrderList());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { // passes
																					// in
																					// a
																					// request
																					// code
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CAMERA_PIC_REQUESTED) { // request to takes picture

			if (resultCode == RESULT_OK) {
				curTime = System.currentTimeMillis();
				picture = convertImageUriToFile(imageUri);

				Intent iDesc = new Intent(Main.this, Description.class);
				startActivityForResult(iDesc, DESCRIPTION_REQUESTED);

			}

		} else if (requestCode == EXPERIMENT_REQUESTED) { // obtains data fields
															// from project on
															// isense
			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
				String eidString = mPrefs.getString("project_id", "");

				experimentLabel.setText(getResources().getString(
						R.string.experiment)
						+ eidString);

				dfm = new DataFieldManager(Integer.parseInt(eidString), api,
						mContext, f);
				dfm.getOrder();
			}
		} else if (requestCode == LOGIN_REQUESTED) { // shows dialog to login
			if (resultCode == Activity.RESULT_OK) {

				w.make("Login successful", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);

			} else if (resultCode == Login.RESULT_ERROR) {

				startActivityForResult(new Intent(mContext, Login.class),
						LOGIN_REQUESTED);

			}
		} else if (requestCode == NO_GPS_REQUESTED) { // asks the user if they
														// would like to enable
														// gps
			showGpsDialog = true;
			if (resultCode == RESULT_OK) {
				startActivity(new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}

		} else if (requestCode == DESCRIPTION_REQUESTED) {
			descriptionStr = Description.photo_description;

			uq.buildQueueFromFile();
			queueCount.setText(getResources().getString(R.string.queueCount)
					+ uq.queueSize());

			new UploadTask().execute();

		} else if (requestCode == SELECT_PICTURE_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImageUri = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImageUri,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();

				curTime = System.currentTimeMillis();
				picture = new File(picturePath);

				Intent iDesc = new Intent(Main.this, Description.class);
				startActivityForResult(iDesc, DESCRIPTION_REQUESTED);
				// TODO
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

	private class UploadTask extends AsyncTask<Void, Integer, Void> { // adds
																		// picture
																		// to
																		// queue

		@Override
		protected void onPreExecute() {
			OrientationManager.disableRotation(Main.this);

			// dia = new ProgressDialog(Main.this);
			// dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// dia.setMessage("Saving picture...");
			// dia.setCancelable(false);
			// dia.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {

			uploader.run();
			publishProgress(100);

			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {

			// dia.cancel();

			OrientationManager.enableRotation(Main.this);

			if (status400) {
				w.make("Your data cannot be uploaded to this project.  It has been closed.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			} else if (uploadError) {
				// Do nothing - postRunnableWaffleError takes care of this
				// Waffle
			} else {
				w.make("Picture saved!", Waffle.LENGTH_LONG, Waffle.IMAGE_CHECK);
			}

			queueCount.setText(getResources().getString(R.string.queueCount)
					+ uq.queueSize());
			uq.buildQueueFromFile();

			uploadError = false;
		}
	}
	
	// gets the user's name if not already provided + login to web site
		private void attemptLoginOnAppStart() {

			final SharedPreferences mPrefs = new ObscuredSharedPreferences(
					mContext, getSharedPreferences(
							Login.PREFERENCES_KEY_OBSCURRED_USER_INFO,
							Context.MODE_PRIVATE));

			if (mPrefs.getString(
					Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "")
					.equals("")
					&& mPrefs.getString(
							Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
							"").equals("")) {
				mPrefs.edit()
				.putString(
						Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
						Login.DEFAULT_USERNAME).commit();
				mPrefs.edit()
				.putString(
						Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
						Login.DEFAULT_PASSWORD).commit();
			}

			if (Connection.hasConnectivity(mContext)) {
				new LoginTask().execute();

			}
		}

	// gets the user's name if not already provided + login to web site
	private void attemptLogin() {

		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				mContext, getSharedPreferences(
						Login.PREFERENCES_KEY_OBSCURRED_USER_INFO,
						Context.MODE_PRIVATE));

		if (mPrefs.getString(
				Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "")
				.equals("")
				&& mPrefs.getString(
						Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
						"").equals("")) {
			return;
		}

		if (Connection.hasConnectivity(mContext)) {
			new LoginTask().execute();

		}
	}

	// initialize location listener to get a pair of coordinates
	private void initLocManager() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mRoughLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				&& mRoughLocManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0, Main.this);
			mRoughLocManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, Main.this);

		} else {
			if (showGpsDialog) {
				Intent iNoGps = new Intent(mContext, NoGps.class);
				startActivityForResult(iNoGps, NO_GPS_REQUESTED);
				showGpsDialog = false;
			}
		}

		loc = new Location(mLocationManager.getBestProvider(c, true));
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (mLocationManager != null)
			mLocationManager.removeUpdates(Main.this);

		if (mRoughLocManager != null)
			mRoughLocManager.removeUpdates(Main.this);

		if (mTimer != null)
			mTimer.cancel();
		mTimer = null;
	}

	// no gps signal
	private void waitingForGPS() {
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {

						if (loc.getLatitude() != 0)
							latLong.setText("Lat: " + loc.getLatitude()
									+ "\nLong: " + loc.getLongitude());
						else {
							switch (waitingCounter % 4) {
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
							}
							waitingCounter++;
						}
					}
				});
			}
		}, 0, TIMER_LOOP);
	}

	@Override
	protected void onPause() {
		if (recording == true) {
			recording = false;
			preview.removeView(mPreview);
			preview.setVisibility(View.INVISIBLE);
		}

		super.onPause();
	}

	private String makeThisDatePretty(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS, MM/dd/yy",
				Locale.US);
		return sdf.format(time);
	}

	// Attempts to login with current user information
	private class LoginTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			final SharedPreferences mPrefs = new ObscuredSharedPreferences(
					mContext, mContext.getSharedPreferences(
							Login.PREFERENCES_KEY_OBSCURRED_USER_INFO,
							Context.MODE_PRIVATE));

			boolean success = api
					.createSession(
							mPrefs.getString(
									Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
									""),
							mPrefs.getString(
									Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
									""));
			return success;
		}

	}

}