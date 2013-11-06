package edu.uml.cs.isense.riverwalk;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
//import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.riverwalk.dialogs.Continuous;
import edu.uml.cs.isense.riverwalk.dialogs.Description;
import edu.uml.cs.isense.riverwalk.dialogs.LoginActivity;
import edu.uml.cs.isense.riverwalk.dialogs.NoGps;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;


public class Main extends Activity implements LocationListener {
	private static final int CAMERA_PIC_REQUESTED = 101;
	private static final int LOGIN_REQUESTED = 102;
	private static final int NO_GPS_REQUESTED = 103;
	private static final int EXPERIMENT_REQUESTED = 104;
	private static final int QUEUE_UPLOAD_REQUESTED = 105;
	private static final int DESCRIPTION_REQUESTED = 106;
	private static final int CONTINUOUS_REQUESTED = 107;
	
	
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

	public static Context mContext;

	private Waffle w;
	private File picture;
	public static Button takePicture;
	static boolean useMenu = true;

	//private ProgressDialog dia;
	private DataFieldManager dfm;
	private Fields f;
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	private Camera mCamera;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;

		w = new Waffle(mContext);

		f = new Fields();

		api = API.getInstance(mContext);
		api.useDev(true);

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

		takePicture = (Button) findViewById(R.id.takePicture);
		takePicture.getBackground().setColorFilter(0xFF99CCFF,
				PorterDuff.Mode.MULTIPLY);
		takePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//TODO
				
				
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
				
				if (continuous == false){
					String state = Environment.getExternalStorageState();
					if (Environment.MEDIA_MOUNTED.equals(state)) {
	
						ContentValues values = new ContentValues();
	
						imageUri = getContentResolver().insert(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								values);
	
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
						intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
	
						OrientationManager.disableRotation(Main.this);
						startActivityForResult(intent, CAMERA_PIC_REQUESTED);
					} else {
						w.make("Cannot write to external storage.",
								Waffle.LENGTH_LONG, Waffle.IMAGE_X);
					}

				} else if( continuous == true) { //if continuous == true
					if (recording == false){
						takePicture.setBackgroundColor(0xFF00FF00);
						takePicture.setText("Recording Push to Stop");
						recording = true;
						new continuouslytakephotos().execute();
					} else {
						Main.takePicture.setText(R.string.takePicContinuous);
						Main.takePicture.setBackgroundColor(R.drawable.button_rsense);
						recording = false;
					}
				}
			}
		});

	}
	


private class continuouslytakephotos extends AsyncTask<Void, Void, Void>
{
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //this method will be running on UI thread
        OrientationManager.disableRotation(Main.this);	
    }
    @Override
    protected Void doInBackground(Void... params) {

        //this method will be running on background thread so don't update UI frome here
        //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here

    	while(recording){
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				
			mCamera = null;	
		    try {
		        mCamera = Camera.open(); // attempt to get a Camera instance
		    }
		    catch (Exception e){
		        // Camera is not available (in use or does not exist)
		    	mCamera.release();	//release camera so other applications can use it
		    	return null;
		    }
		    
	    	ContentValues values = new ContentValues();
	    	
			imageUri = getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					values);
			
			   
		    mCamera.takePicture(null, null, mPicture);	//takes a picture

			
			
			} else {
//				w.make("Cannot write to external storage.",
//						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
				//TODO
				return null;
			}

			try {Thread.sleep(1000 * continuousInterval);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}
    	
        return null;
    }
    
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        		Main.takePicture.setText(R.string.takePicContinuous);
				Main.takePicture.setBackgroundColor(R.drawable.button_rsense);
				recording = false;
				OrientationManager.enableRotation(Main.this);	
        //this method will be running on UI thread

    }
}

private PictureCallback mPicture = new PictureCallback() {

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
    	
    	
        picture = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        
        if (picture == null){
//            Log.d(TAG, "Error creating media file, check storage permissions: " +
//                e.getMessage());
            return;
        }
    }
};

/** Create a File for saving an image or video */
private static File getOutputMediaFile(int type){
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
              Environment.DIRECTORY_PICTURES), "MyCameraApp");
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (! mediaStorageDir.exists()){
        if (! mediaStorageDir.mkdirs()){
            Log.d("MyCameraApp", "failed to create directory");
            return null;
        }
    }
    
    // Create a media file name
    File mediaFile;
    if (type == MEDIA_TYPE_IMAGE){
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
        "IMG_"+ ".jpg");
//    } else if(type == MEDIA_TYPE_VIDEO) {
//        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//        "VID_"+ timeStamp + ".mp4");
    } else {
        return null;
    }

    return mediaFile;
}
    
	// double tap back button to exit
	@Override
	public void onBackPressed() {
		if (!w.isDisplaying) {
			w.make("Double press \"Back\" to exit.", Waffle.LENGTH_SHORT);
		} else if (w.canPerformTask)
			super.onBackPressed();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
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
				startActivityForResult(iExperiment, EXPERIMENT_REQUESTED);
	            return true;
	            
	        case R.id.MENU_ITEM_LOGIN:
	        	startActivityForResult(new Intent(getApplicationContext(),
						LoginActivity.class), LOGIN_REQUESTED);
	            return true;
	            
	        case R.id.MENU_ITEM_CONTINUOUS:
	        	Intent continuous = new Intent(getApplicationContext(),
						Continuous.class);
				startActivity(continuous);
	            return true;    
	            
	        default:
	            return false;
	    }
	}
	

	@Override
	protected void onResume() {
		super.onResume();

		if (api.getCurrentUser() == null)
			attemptLogin();

		// Rebuilds uploadQueue from saved info
		uq.buildQueueFromFile();
		queueCount.setText(getResources().getString(R.string.queueCount)
				+ uq.queueSize());
	}

	//uploads the data if logged in and queue is not empty
	private void manageUploadQueue() {

		if (api.getCurrentUser() == null) {
			w.make("Must be logged in to upload.", Waffle.IMAGE_X);
			return;
		}

		if (uq.emptyQueue()) {
			w.make("No data to upload.", Waffle.IMAGE_CHECK);
			return;
		}

		Intent i = new Intent().setClass(mContext, QueueLayout.class);
		i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
		startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);

	}

	//onStart initialize location manager
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

	//upload pictures
	private Runnable uploader = new Runnable() {
		@Override
		public void run() {

			SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
			String experimentNum = mPrefs.getString("project_id", "Error");

			if (experimentNum.equals("Error")) {
				uploadError = true;
				postRunnableWaffleError("No project selected to upload pictures to");
				return;
			}

			//if (dfm == null)
				initDfm();

			JSONArray dataJSON = new JSONArray();		//data is set into JSONArray to be uploaded
			JSONObject dataRow = new JSONObject();
			
			if (!api.hasConnectivity()){
				experimentNum = "-1";
			}
				
				
			if (loc.getLatitude() != 0) {
				f.timeMillis = curTime;
				System.out.println("curTime =" + f.timeMillis);
				f.latitude = loc.getLatitude();
				System.out.println("Latitude =" + f.latitude);
				f.longitude = loc.getLongitude();
				System.out.println("Longitude =" + f.longitude);
				
				if (!experimentNum.equals("-1")){
					dataJSON.put(dfm.putData());
				}else{
					dataJSON.put(dfm.putDataForNoProjectID());
				}
				
			} else { //no gps
				loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
				f.timeMillis = curTime;	
				System.out.println("curTime (no gps) =" + f.timeMillis);   
				f.latitude = loc.getLatitude();  
				System.out.println("Latitude (no gps) =" + f.latitude);
				f.longitude = loc.getLongitude(); 
				System.out.println("Longitude (no gps) =" + f.longitude);
				
				if (!experimentNum.equals("-1")){
					dataJSON.put(dfm.putData());
				}else{
					dataJSON.put(dfm.putDataForNoProjectID());
				}
			}
			
			dataJSON.put(dataRow); 

			QDataSet ds = new QDataSet(QDataSet.Type.BOTH, name.getText()  //data set to be uploaded
					.toString() + ": " + descriptionStr,
					makeThisDatePretty(curTime), experimentNum,
					dataJSON.toString(), picture);

			System.out.println("experimentNum = " + experimentNum);
			
			uq.addDataSetToQueue(ds);

		}
	};

	private void initDfm() {											//sets up data field manager
		SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
		String experimentInput = mPrefs.getString("project_id", "");
		System.out.println("experimentInput ="+ experimentInput);
		dfm = new DataFieldManager(Integer.parseInt(experimentInput), api,
				mContext, f);
		dfm.getOrderWithExternalAsyncTask();
		dfm.enableAllFields();
		System.out.println("order =" + dfm.getOrderList());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { //passes in a request code
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CAMERA_PIC_REQUESTED) { 	//request to takes picture

				if (resultCode == RESULT_OK) {
					curTime = System.currentTimeMillis();
					picture = convertImageUriToFile(imageUri);
	
					uq.buildQueueFromFile();
					queueCount.setText(getResources()
							.getString(R.string.queueCount) + uq.queueSize());

					if(continuous == false){ //if continuously recording do not ask for description
						Intent iDesc = new Intent(Main.this, Description.class);
						startActivityForResult(iDesc, DESCRIPTION_REQUESTED);
					} 
					
					new UploadTask().execute();
				
				}
			
			
			//TODO
				
		} else if (requestCode == CONTINUOUS_REQUESTED) {
			
			
			
		} else if (requestCode == EXPERIMENT_REQUESTED) {			//obtains data fields from project on isense
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
		} else if (requestCode == LOGIN_REQUESTED) {				//shows dialog to login
			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences mPrefs = getSharedPreferences("LOGIN", 0);
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putBoolean("logged_in", true);
				mEditor.commit();
			}
		} else if (requestCode == NO_GPS_REQUESTED) {				//asks the user if they would like to enable gps
			showGpsDialog = true;
			if (resultCode == RESULT_OK) {
				startActivity(new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		} else if (requestCode == DESCRIPTION_REQUESTED) {
			
			descriptionStr = Description.photo_description;  // set descriptionStr equal to photo_description in Description.java

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

	private class UploadTask extends AsyncTask<Void, Integer, Void> { //adds picture to queue 

		@Override
		protected void onPreExecute() {
			OrientationManager.disableRotation(Main.this);

//			dia = new ProgressDialog(Main.this);
//			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//			dia.setMessage("Saving picture...");
//			dia.setCancelable(false);
//			dia.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {

			uploader.run();
			publishProgress(100);

			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {

	//		dia.cancel();

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
	private void attemptLogin() {

		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				mContext, getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		if (mPrefs.getString("username", "").equals("")
				&& mPrefs.getString("password", "").equals("")) {
			return;
		}

		if (api.hasConnectivity()) {
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
				&& mRoughLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

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

		mCamera.release();	//release camera so other applications can use it
		
		if (mLocationManager != null)
			mLocationManager.removeUpdates(Main.this);

		if (mRoughLocManager != null)
			mRoughLocManager.removeUpdates(Main.this);

		if (mTimer != null)
			mTimer.cancel();
		mTimer = null;
	}

	//no gps signal
	private void waitingForGPS() {
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						
						Log.d("tag", "latitude ="+ loc.getLatitude());
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
					mContext, mContext.getSharedPreferences("USER_INFO",
							Context.MODE_PRIVATE));

			boolean success = api.createSession(
					mPrefs.getString("username", ""),
					mPrefs.getString("password", ""));
			return success;
		}

	}

}