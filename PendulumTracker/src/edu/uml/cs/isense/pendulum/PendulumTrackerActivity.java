package edu.uml.cs.isense.pendulum;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.credentials.CredentialManager;
import edu.uml.cs.isense.credentials.CredentialManagerKey;
import edu.uml.cs.isense.credentials.EnterName;
import edu.uml.cs.isense.objects.RProjectField;
import edu.uml.cs.isense.queue.UploadQueue;

public class PendulumTrackerActivity extends Activity implements
// OnTouchListener, CvCameraViewListener2 {
		OnLongClickListener, CvCameraViewListener2 {

	private static final String TAG = "PendulumTracker::Activity";

	Paint paint;

	public static Context mContext;

	// iSENSE member variables
	// use development site
	Boolean useDevSite = false;
	// iSENSE uploader
	API api;

	private TextView initInstr;

	// create session name based upon first name and last initial user enters
	static String firstName = "";
	static String lastInitial = "";
	public static final int ENTERNAME_REQUEST = 1098;
	public static final int CREDENTIAL_KEY_REQUESTED  = 1099;
	public static final int LOGIN_REQUESTED  = 2000;

	
	Boolean sessionNameEntered = false;

	private static String experimentNumber = "29"; // production = 29, dev = 39
	
	private static String baseSessionUrl = "http://isenseproject.org/projects/"
			+ experimentNumber + "data_sets/";
	private static String baseSessionUrlDev = "http://rsense-dev.cs.uml.edu/projects/"
			+ experimentNumber + "/data_sets/";
	// private static String baseSessionUrlDev = "http://129.63.16.30/projects/"
	private static String sessionUrl = "";
	private String dateString;

	// upload progress dialogue
	ProgressDialog dia;
	// JSON array for uploading pendulum position data,
	// accessed from ColorBlobDetectionView
	public static JSONArray mDataSet = new JSONArray();

	// OpenCV

	// displayed image width and height
	private int mImgWidth = 0;
	private int mImgHeight = 0;

	private boolean mIsColorSelected = false;
	private Mat mRgba;
	private Scalar mBlobColorRgba;
	private Scalar mBlobColorHsv;

	private MarkerDetector mDetector;
	private Mat mSpectrum;
	private Size SPECTRUM_SIZE;
	private Scalar CONTOUR_COLOR;

	private CameraBridgeViewBase mOpenCvCameraView;
	static volatile boolean mDataCollectionEnabled = false; // volatile because
															// written on 1
															// thread, read-only
															// on another
	private static boolean mSessionCreated = false;
	private boolean mDisplayStatus = false;
	private boolean mEnableTouchMode = false;

	// start / stop icons
	Menu menu;
	MenuItem startStopButton;
	Drawable startIcon;
	Drawable stopIcon;

	Handler mHandler;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				mOpenCvCameraView
				// .setOnTouchListener(PendulumTrackerActivity.this);
						.setOnLongClickListener(PendulumTrackerActivity.this); // is
																				// like
																				// setting
																				// button.setOnTouchListener()
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public PendulumTrackerActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);

		// set context (for starting new Intents,etc)
		mContext = this;

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// set CBD activity content from xml layout file.
		// all Views, e.g. JavaCameraView will be inflated (e.g. created)
		setContentView(R.layout.color_blob_detection_surface_view);

		// think base class pointer: make mOpenCvCameraView become a
		// JavaCameraView
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);

		// set higher level camera parameters
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.enableFpsMeter();
		// mOpenCvCameraView.setMaxFrameSize(1280,720);
		// mOpenCvCameraView.setMaxFrameSize(640, 480); // debug!
		mOpenCvCameraView.setMaxFrameSize(320, 240);

		// iSENSE network connectivity stuff
		api = API.getInstance();
		api.useDev(useDevSite);

		// TextView for instruction overlay
		initInstr = (TextView) findViewById(R.id.instructions);
		initInstr.setVisibility(View.VISIBLE);

		// set start and stop icons for data collections
		startIcon = getResources().getDrawable(R.drawable.start_icon);
		stopIcon = getResources().getDrawable(R.drawable.stop_icon);

		startStopButton = (MenuItem) findViewById(R.id.menu_start);
		//Menu menu = (Menu) findViewById(R.layout.menu);
		//startStopButton = menu.getItem(R.id.menu_start);
		
		// TODO: add remembe-me login stuff here!
		// Jeremy Note: Actually the remembering happens automagically
		// in the API calls
		CredentialManager.Login(mContext, api);
		
		// Event handler
		mHandler = new Handler();
	}

	@Override
	public void onPause() {
		super.onPause();
		// if (mOpenCvCameraView != null)
		// mOpenCvCameraView.disableView();
	}

	// this is called any time an activity starts interacting with the user.
	// OpenCV library
	// reloaded anytime activity is resumed (e.g. brought to forefront)
	@Override
	public void onResume() {
		super.onResume();

		Log.i(TAG, "Trying to load OpenCV library");
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this,
				mLoaderCallback)) {
			Log.e(TAG, "Cannot connect to OpenCV Manager");
		}
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {

		mImgWidth = width;
		mImgHeight = height;

		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mDetector = new MarkerDetector();

		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
		mBlobColorHsv = new Scalar(255);
		SPECTRUM_SIZE = new Size(200, 64);
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

	}

	public void onCameraViewStopped() {
		mRgba.release();
	}

	@Override
	public boolean onLongClick(View v) {

		// public boolean onTouch(View v, MotionEvent event) {

		/*
		if(this.mSessionCreated)
		{
	
			if(!this.mDataCollectionEnabled)
			{	
				this.mDataCollectionEnabled = true;
				Toast.makeText(PendulumTrackerActivity.this,
						"STARTING Data Collection", Toast.LENGTH_SHORT).show();
				
				startStopButton.setVisible(false);
			}
			else if(this.mDataCollectionEnabled)
			{	
				this.mDataCollectionEnabled = false;
				Toast.makeText(PendulumTrackerActivity.this,
						"STOPPING Data Collection", Toast.LENGTH_SHORT).show();
				
				startStopButton.setVisible(true);
			}
		}
		
		*/
		
		/*
		 * int cols = mRgba.cols(); int rows = mRgba.rows();
		 * 
		 * int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2; int yOffset
		 * = (mOpenCvCameraView.getHeight() - rows) / 2;
		 * 
		 * int x = (int) event.getX() - xOffset; int y = (int) event.getY() -
		 * yOffset;
		 * 
		 * Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
		 * 
		 * if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
		 * 
		 * Rect touchedRect = new Rect();
		 * 
		 * touchedRect.x = (x > 4) ? x - 4 : 0; touchedRect.y = (y > 4) ? y - 4
		 * : 0;
		 * 
		 * touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols -
		 * touchedRect.x; touchedRect.height = (y + 4 < rows) ? y + 4 -
		 * touchedRect.y : rows - touchedRect.y;
		 * 
		 * Mat touchedRegionRgba = mRgba.submat(touchedRect);
		 * 
		 * Mat touchedRegionHsv = new Mat(); Imgproc.cvtColor(touchedRegionRgba,
		 * touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
		 * 
		 * // Calculate average color of touched region mBlobColorHsv =
		 * Core.sumElems(touchedRegionHsv); int pointCount = touchedRect.width *
		 * touchedRect.height; for (int i = 0; i < mBlobColorHsv.val.length;
		 * i++) mBlobColorHsv.val[i] /= pointCount;
		 * 
		 * mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
		 * 
		 * Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " +
		 * mBlobColorRgba.val[1] + ", " + mBlobColorRgba.val[2] + ", " +
		 * mBlobColorRgba.val[3] + ")");
		 * 
		 * mDetector.setHsvColor(mBlobColorHsv);
		 * 
		 * Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
		 * 
		 * mIsColorSelected = true;
		 * 
		 * touchedRegionRgba.release(); touchedRegionHsv.release();
		 * 
		 * return false; // allows subsequent touch events
		 */

		// return true;
		return false;
	}

	// invoked when camera frame delivered
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) { // processFrame(VideoCapture
																// vc)
		boolean useGrey = true; // debug = false;
		boolean debug = false; // debug = true;
		int contourSize = -1;

		if (useGrey) {
			Point point = new Point(0, 0);

			// get latest camera frame
			mRgba = inputFrame.gray();

			// get location of detected points
			point = mDetector.processGrey(mRgba);

			// ---- DEBUG -----
			if (debug) {
				mRgba = mDetector.getLastDebugImg();
				Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_GRAY2RGB);
				contourSize = 2;
				this.drawDetectedContours(contourSize);
			}
			// ------------------
			else
				// convert grey image to color so we can draw color overlay
				Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_GRAY2RGB); // current
																		// frame

			if (mDataCollectionEnabled) {

				this.addStatusOverlay(mRgba);
				// add data point to final data set

				// yScale, -xScale
				if (point.x != 0 && point.y != 0) {
					// shift x-axis so center vertical axis is set to x=0 in
					// pendulum coordinates
					final int shiftX = (int) (mImgWidth / 2);
					this.addDataPoint(point.x - shiftX, point.y);
				}

				// Make TextView disappear
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						initInstr.setVisibility(View.GONE);
					}
				});
			} else {
				this.addBoxOverlay(mRgba);

				// Make TextView disappear
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						initInstr.setVisibility(View.VISIBLE);
					}
				});
			}

			// TODO: fix flipping of y-axis
			Log.i(TAG, "(x,y) = (" + point.x + "," + -point.y + ")");
			Core.circle(mRgba, new Point(point.x, -point.y), 7, new Scalar(255,
					0, 0, 255), 2);

		} else {
			mRgba = inputFrame.rgba();

			if (mIsColorSelected) {
				mDetector.process(mRgba);

				this.drawDetectedContours(contourSize);
				// List<MatOfPoint> contours = mDetector.getContours();;
				// Log.e(TAG, "Contours count: " + contours.size());
				// Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

				Mat colorLabel = mRgba.submat(4, 68, 4, 68);
				colorLabel.setTo(mBlobColorRgba);

				Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70,
						70 + mSpectrum.cols());
				mSpectrum.copyTo(spectrumLabel);

			}

		}

		return mRgba;
	}

	@SuppressWarnings("unused")
	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL,
				4);

		return new Scalar(pointMatRgba.get(0, 0));

	}

	void drawDetectedContours(int contourSize) {
		// if contourSize = -1, contour will be filled
		List<MatOfPoint> contours = mDetector.getContours();
		;
		Log.e(TAG, "Contours count: " + contours.size());
		Imgproc.drawContours(mRgba, contours, contourSize, CONTOUR_COLOR);
	}

	// ------ screen overlays ------------------------

	void addBoxOverlay(Mat img) {
		// makes this 10% of width?
		// final int boxSize = 10;
		int boxSize = (int) (0.1 * img.width());

		final Point centerUL = new Point(img.width() / 2 - boxSize,
				img.height() / 2 - boxSize / 8);
		final Point centerLR = new Point(img.width() / 2 + boxSize,
				img.height() - 5);

		Core.rectangle(img, centerUL, centerLR, new Scalar(255, 0, 0, 255), 2);

		// Core.putText(img, new String("center pendulum in box"), new
		// Point(img.width()/2 - 3*boxSize, img.height()/2 - boxSize),
		// 0/* don't know what font this is!x */, 0.5, new Scalar(255, 0, 0,
		// 255), 1);

	}

	void addStatusOverlay(Mat img) {

		if (mDisplayStatus == true) {
			// Core.putText(img, new String("[COLLECTING DATA]"), new Point(0,
			// img.height() - 10 ),
			// 0/* CV_FONT_HERSHEY_COMPLEX */, 0.15, new Scalar(0, 255, 0, 255),
			// 2);
			Core.circle(mRgba, new Point(10, mRgba.height() - 10), 2,
					new Scalar(0, 255, 0, 255), -1);
			mDisplayStatus = false;
		} else {
			// Core.putText(img, new String("[COLLECTING DATA]"), new Point(0,
			// img.height() - 10 ),
			// 0/* CV_FONT_HERSHEY_COMPLEX */, 0.15, new Scalar(0, 255, 0, 255),
			// 1);
			Core.circle(mRgba, new Point(10, mRgba.height() - 10), 3,
					new Scalar(0, 255, 0, 255), -1);

			mDisplayStatus = true;
		}
	}

	// ------ iSENSE upload/ActionBar/menu stuff-------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// item.set
		switch (item.getItemId()) {
		// STOP experiment and data collection and
		// UPLOAD data
		/*
		 * case R.id.menu_upload:
		 * 
		 * mDataCollectionEnabled = false;
		 * 
		 * new LoginBeforeUploadTask().execute(); return true;
		 */
		// START experiment and data collection
		case R.id.menu_start:

			// Selected item becomes start/stop button (for onActivityResult())
			startStopButton = item;

			// Create session with first name/last initial
			if (PendulumTrackerActivity.mSessionCreated == false) {
				if (firstName.length() == 0 || lastInitial.length() == 0) {
					// Boolean dontPromptMeTwice = true;
					startActivityForResult(new Intent(mContext, EnterName.class), // used to be login activity
							ENTERNAME_REQUEST);
				}

				return true;
			}

			// START data collection
			if (PendulumTrackerActivity.mDataCollectionEnabled == false) {
				// be sure to disable data collection before uploading data to
				// iSENSE
				PendulumTrackerActivity.mDataCollectionEnabled = true;

				// set STOP button and text
				item.setIcon(stopIcon);
				item.setTitle(R.string.stopCollection);

			} else {
				// enable data collection before uploading data to iSENSE
				PendulumTrackerActivity.mDataCollectionEnabled = false;

				// set START button and text
				item.setIcon(startIcon);
				item.setTitle(R.string.startCollection);
				
				
				
				LoginThenUpload(); //TODO was login then upload task

			}
			
			return true;

		// BETA/Experimental touch screen mode using onLongClick
/*		case R.id.menu_touchmode:
			
			
			//mEnableTouchMode = (mDataCollectionEnabled && mEnableTouchMode) ? false : true; 
			
			// only enable mode if not in data collection mode (e.g., mDataCollectionEnable = true;
			if(!this.mDataCollectionEnabled)
			{	
				if(!this.mEnableTouchMode)
				{
					this.mEnableTouchMode = true;
				
					startStopButton.setVisible(false);
					
					Toast.makeText(PendulumTrackerActivity.this,
							"Touch screen mode enabled.", Toast.LENGTH_SHORT).show();
				
				}
				else 
				{
					this.mEnableTouchMode = false;
					startStopButton.setVisible(true);
					
					Toast.makeText(PendulumTrackerActivity.this,
							"Touch screen mode disabled.", Toast.LENGTH_SHORT).show();
				}
			}
			return true;
*/			
		case R.id.menu_login:
			startActivityForResult(new Intent(getApplicationContext(),
					CredentialManager.class), LOGIN_REQUESTED);
			return true;	
			
		case R.id.menu_exit:

			// Exit app neatly
			// this.finish(); // this only exits Activity not app completely.
			exitNeatly();
			return true;

		case R.id.menu_instructions:

			String strInstruct = "Center at-rest pendulum in center of image. Select 'Start data collection button' to start. Pull pendulum back to left or right edge of image and release when selecting 'OK'. Select 'Stop and upload to iSENSE' to stop. ";
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			// chain together various setter methods to set the dialog
			// characteristics
			builder.setMessage(strInstruct)
					.setTitle("Instructions:")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								// @Override
								public void onClick(DialogInterface dialog,
										int id) {
									// grab position of target and pass it along
									// If this were Cancel for
									// setNegativeButton() , just do nothin'!

								}

							});

			// get the AlertDialog from create()
			AlertDialog dialog = builder.create();
			dialog.show(); // make me appear!

			return true;
		}
		return true;
	}

	// do these things after an Activity is finished
	public void onActivityResult(int reqCode, int resultCode, Intent data) {

		super.onActivityResult(reqCode, resultCode, data);

		// Enter session name
		if (reqCode == ENTERNAME_REQUEST) {

			if (resultCode == RESULT_OK) {

				PendulumTrackerActivity.mSessionCreated = true;

				Toast.makeText(
						PendulumTrackerActivity.this,
						"Session created. Press 'Start Data Collection' to begin!",
						Toast.LENGTH_SHORT).show();

				// set START button and text
				startStopButton.setIcon(startIcon);
				startStopButton.setTitle(R.string.startCollection);

				// how come this doesn't work???
				// menu.getItem(R.id.menu_start).setIcon(startIcon);
				// startStopButton = (MenuItem)menu.findItem(R.id.menu_start);

			}
		} else if (reqCode == CREDENTIAL_KEY_REQUESTED) {
			if (resultCode == RESULT_OK) {
				new uploadTask().execute();
			}
		} else if(reqCode == LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {
				
			}
		}
	}

	void exitNeatly() {
		// kill process so app completely restarts next time & maintains no
		// state
		int pid = android.os.Process.myPid();
		android.os.Process.killProcess(pid);
	}

	private Runnable uploader = new Runnable() {

		// @Override
		@SuppressLint("SimpleDateFormat")
		public void run() {

			// Create location-less session (for now)
			int sessionId = -1;
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss");
			Date dt = new Date();
			dateString = sdf.format(dt);
			
			final SharedPreferences mPrefs = getSharedPreferences(
							EnterName.PREFERENCES_KEY_USER_INFO,
							Context.MODE_PRIVATE);
		
			String nameOfSession = mPrefs.getString(EnterName.PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME, "") 
									+ mPrefs.getString(EnterName.PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL, "")
									+ " - " + dateString;

			Log.i(TAG, "Uploading data set...");

			JSONObject jobj = new JSONObject();
			try {
				jobj.put("data", mDataSet);
				jobj = api.rowsToCols(jobj);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			int projectID = Integer.parseInt(experimentNumber);

			
			
			if (UploadQueue.getAPI().getCurrentUser() != null) {
				Log.i(TAG, "Uploading new dataset");
				sessionId = UploadQueue.getAPI().uploadDataSet(projectID, jobj, nameOfSession);
			}else{
				String key = CredentialManagerKey.getKey();
				String conName = CredentialManagerKey.getName();
				sessionId = UploadQueue.getAPI().uploadDataSet(projectID, jobj, nameOfSession, key, conName);
			}
			

			if (sessionId == -1)
				Log.i(TAG, "Dataset failed to upload!");

			if (useDevSite) {
				sessionUrl = baseSessionUrlDev + sessionId;
				Log.i(TAG, sessionUrl);
			} else
				sessionUrl = baseSessionUrl + sessionId;

		}

	};

	// Task for uploading data to iSENSE
	public class uploadTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(PendulumTrackerActivity.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while your data is uploaded to iSENSE...");
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

			// reset data array
			mDataSet = new JSONArray();

			dia.setMessage("Done");
			dia.cancel();
			Toast.makeText(PendulumTrackerActivity.this,
					"Data upload successful!", Toast.LENGTH_SHORT).show();
		}
	}

	// HACKY test data
	// ---- HACKY TEST DATA ----
	void addTestPoint(JSONArray dataSet) {
		int i = 0;

		while (i < 10) {
			JSONObject dataJSON = new JSONObject();
			Calendar c = Calendar.getInstance();
			long currentTime = (long) (c.getTimeInMillis() /*- 14400000*/);

			/* Convert floating point to String to send data via HTML */
			try {
				/* Posn-x */dataJSON.put("1", i);
				/* Posn-y */dataJSON.put("2", i);
				/* Time */dataJSON.put("0", "u " + currentTime);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			dataSet.put(dataJSON);
			i++;

			Log.i(TAG, "--------------- ADDING DATA POINT ---------------");
		}

	}

	// ---- END HACKY TEST DATA -----

	void addDataPoint(double x, double y) {

		JSONObject dataJSON = new JSONObject();
		// Calendar c = Calendar.getInstance();
		long currentTime = (long) System.currentTimeMillis(); // (c.getTimeInMillis()
																// /*-
																// 14400000*/);

		// new! works!
		ArrayList<RProjectField> pf = api.getProjectFields(Integer.parseInt(experimentNumber));
		
		
		/* Convert floating point to String to send data via HTML */
		try {
			/* Posn-x */dataJSON.put( "" + pf.get(1).field_id, x);
			/* Posn-y */dataJSON.put( "" + pf.get(2).field_id, y);
			/* Time */dataJSON.put( "" + pf.get(0).field_id , "u " + currentTime);
			
			
			/* Posn-x *///dataJSON.put("1", x);
			/* Posn-y *///dataJSON.put("2", y);
			/* Time *///dataJSON.put("0", "u " + currentTime);
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mDataSet.put(dataJSON);

		Log.i(TAG, "--------------- ADDING DATA POINT ---------------");

	}



	void LoginThenUpload() { //TODO

			// am i connected to the internet?
			boolean connect = Connection.hasConnectivity(mContext);
			boolean loginStatus = CredentialManager.isLoggedIn();

			if (connect) {

				// check to see if a session name has been created before we
				// start collecting data.
				if (!PendulumTrackerActivity.mSessionCreated) {

					Toast.makeText(
							PendulumTrackerActivity.this,
							"You must first START data collection to create session name.",
							Toast.LENGTH_LONG).show();
					return;
				}
				
				/*try to login if successful upload if not prompt for key*/
				CredentialManager.Login(mContext, api);
				

				// am I logged in/session created to iSENSE?
					// yes! yes! yes! so upload my data
					if (!mDataCollectionEnabled && mDataSet.length() > 0) {

						if (CredentialManager.isLoggedIn() == true) {
							/*logged in so upload*/
							new uploadTask().execute();
						}else{
							/*start dialog activity to ask for key and upload using key in onActivityResult*/
							Intent key_intent = new Intent().setClass(mContext, CredentialManagerKey.class);
							startActivityForResult(key_intent, CREDENTIAL_KEY_REQUESTED);
						}					
						
					} else {
						Toast.makeText(
								PendulumTrackerActivity.this,
								"You must first START data collection to upload data.",
								Toast.LENGTH_LONG).show();
					}
				

			
			// I am not connected to the internet - oops.
			} else {
				Toast.makeText(
						PendulumTrackerActivity.this,
						"You are not connected to the Intertubes. Check connectivity and try again (previously recorded data will be saved).",
						Toast.LENGTH_LONG).show();
				return;
			}

	

	}

}
