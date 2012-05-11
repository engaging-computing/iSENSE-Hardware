package edu.uml.cs.isense.pictures;

/* Experiment 294 Now 347 */

import java.io.File;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

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
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.Picture;

public class pictures extends Activity implements LocationListener {
	private static final int CAMERA_PIC_REQUESTED = 1;
	
	private static final int DIALOG_REJECT = 0;
	private static final int DIALOG_NO_GPS = 1;
	private static final int DIALOG_DIFFICULTY = 2;
	private static final int DIALOG_NO_CONNECT = 3;
	private static final int DIALOG_NOT_LOGGED_IN = 4;
	
	private LocationManager mLocationManager;
	
	private Uri imageUri;
	
  	private Queue <Picture> mQ;
  	private static int QUEUE_COUNT = 0;
	
	RestAPI rapi;
	private static final String loginName = "RiverWalker";
	private static final String loginPass = "SimonKit";
	private static final String experimentNum = "347";
	
	private static boolean gpsWorking = false;
	private static boolean userLoggedIn = false;
	private static boolean smartUploading = false;
	private static boolean calledBySmartUp = false;
	private static boolean finishedUploadSetup = false;
	
	private Picture uploaderPic = null;
	
	private EditText name; 
	//private EditText experimentInput;
	private Location loc ;
	
	//private String teacherInfo       ;
	//private String schoolInfo        ;
	//private String teacher           ;
	//private String school            ;
	private double Lat  =  42.6404   ; 
	private double Long = -71.3533   ;
	private long   curTime           ;

	
	private Context mContext;
	
	private File picture;
	
	private Button takePicture;
	private Button describe;
	
	public static Button takePhoto;
	
	static boolean c1 = false;
	static boolean c2 = false;
	static boolean c3 = false;
	static boolean c4 = false;
	static boolean c5 = false;
	static boolean c6 = false;
	static boolean c7 = false;
	static boolean c8 = false;
	static boolean c9 = false;
	static boolean c10 = false;
	static boolean c11 = false;
	
	private ProgressDialog dia ;

	protected Dialog onCreateDialog(final int id) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    Dialog dialog;
	    switch(id) {
	    
	    case DIALOG_REJECT:
	    	builder.setMessage("Please enter a name.");
	        builder.setPositiveButton("Ok", null);
	        dialog = builder.create();
	        break;
	    
	    case DIALOG_NO_GPS:
	    	builder.setTitle("No GPS Provider Found")
	    	.setMessage("Enabling GPS satellites is recommended for this application.  Would you like to enable GPS?")
	    	.setCancelable(false)
	    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.cancel();
	            	   startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	                    dialoginterface.cancel();
	               }
	           });

	    	dialog = builder.create();
	       	break;
	    	
	    case DIALOG_NO_CONNECT:
	    	
	    	builder.setTitle("No Connectivity")
	    	.setMessage("Could not connect to the internet through either wifi or mobile service. " +
	    			"You will not be able to use this app until either is enabled.")
	    	.setPositiveButton("Turn on Smart-Uploading", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   smartUploading = true;
	            	   waitingForConnectivity();
	            	   dialoginterface.dismiss();
	            	   //((Activity) mContext).finish();
	               }
	    	})
	    	.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   try {
							Thread.sleep(100);
	            	   } catch (InterruptedException e) {
							e.printStackTrace();
	            	   }
	            	   if(rapi.isConnectedToInternet()) {
	            		   dialoginterface.dismiss();
	            		   boolean success = rapi.login(loginName, loginPass);
	            		   if(success) {
	            			   Toast.makeText(pictures.this, "Connectivity found!", Toast.LENGTH_SHORT).show();
	            			   userLoggedIn = true;
	            		   }
	            	   } else {
	            		   userLoggedIn = false;
	            		   dialoginterface.dismiss();
	            		   new NotConnectedTask().execute();
	            	   }
	               }
	    	})
	        .setOnCancelListener(new DialogInterface.OnCancelListener() {
	    		@Override
					public void onCancel(DialogInterface dialoginterface) {
	    				dialoginterface.dismiss();
	            	    ((Activity)mContext).finish();
					}
	    	})
	        .setCancelable(true);
	           
	    	dialog = builder.create();
	       	break;
	
	    case DIALOG_DIFFICULTY:
	    	
	    	builder.setTitle("Difficulties")
	    	.setMessage("This application has experienced WiFi connection difficulties.  Try to reconfigure your WiFi " +
	    			    "settings or turn it off and on, then hit \"Try Again\".")
	    	.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialoginterface, final int id) {
	    			dialoginterface.dismiss();
	    			if (!userLoggedIn) attemptLogin();
	    		}
	    	})
	        .setCancelable(false);
	           
	    	dialog = builder.create();
	       	break;
	       	
	    case DIALOG_NOT_LOGGED_IN:
	    	
	    	builder.setTitle("Upload Failed")
	    	.setMessage("This application is having trouble logging into iSENSE, and therefore cannot upload data at this time.  "
	    			+ "Press try again to reattempt login.")
	    	.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialoginterface, final int id) {
	    			dialoginterface.dismiss();
	    			if (!userLoggedIn) attemptLogin();
	    		}
	    	})
	        .setCancelable(false);
	           
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
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mQ = new LinkedList<Picture>();
        
        takePhoto = (Button) findViewById(R.id.takePicture);
        takePhoto.setEnabled(false);
        
        /*
        Button browse = (Button) findViewById(R.id.BrowseButton);
        browse.setEnabled(false);
        
        EditText input = (EditText) findViewById(R.id.ExperimentInput);
        input.setText("347");
        input.setEnabled(false);
        */
        
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       
        mContext = this;
    
        rapi = RestAPI.getInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE), getApplicationContext());
     
        name = (EditText) findViewById(R.id.name);
        //experimentInput = (EditText) findViewById(R.id.ExperimentInput);
		
		describe = (Button) findViewById(R.id.describeButton);
		describe.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent startDescribe = new Intent(pictures.this, Descriptor.class);
				startActivity(startDescribe);
			}
		});
        
        takePicture = (Button) findViewById(R.id.takePicture);
        takePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (name.getText().length() == 0 /*|| experimentInput.getText().length() == 0*/) {
					showDialog(DIALOG_REJECT);
					return;
				}
				
				//create parameters for Intent with filename
				ContentValues values = new ContentValues();
				//values.put(MediaStore.Images.Media.TITLE, img_name);
				//values.put(MediaStore.Images.Media.DESCRIPTION,img_desc);
				//imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
				imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				//create new Intent
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				
				startActivityForResult(intent, CAMERA_PIC_REQUESTED);
			}
        	
        });
        
        /*Button browseButton = (Button) findViewById(R.id.BrowseButton);
        browseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent experimentIntent = new Intent(getApplicationContext(), Experiments.class);
				
				experimentIntent.putExtra("edu.uml.cs.isense.pictures.experiments.prupose", EXPERIMENT_CODE);
				
				startActivityForResult(experimentIntent, EXPERIMENT_CODE);
			}
			
		});*/
    }
    
    /*
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem.OnMenuItemClickListener menuClicker = new
		MenuItem.OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem item) {
				
				if( item.getItemId() == 1 ) setSessionInfo ( "Teacher", "Please enter your teacher's name."   ) ;
				if( item.getItemId() == 2 )	setSchoolInfo( "School", "Please enter the name of your school.") ;
	    		
	    		return false;
			}
		};
		
		menu.add(1, 1, 1, "Teacher" ).setOnMenuItemClickListener( menuClicker );
        menu.add(1, 2, 2, "School"  ).setOnMenuItemClickListener( menuClicker ); 

        return super.onCreateOptionsMenu( menu ) ; 
		
	}
    
    protected void setTeacherInfo(String session_title, String session_desc) {
    	AlertDialog.Builder alert = new AlertDialog.Builder( mContext ) ;
		
		alert.setTitle( session_title ) ;
		alert.setMessage( session_desc ) ;
	 
			//Set an EditText view to get user input
			final EditText input = new EditText( mContext ) ; 
			alert.setView( input ) ;
			input.setText(teacherInfo) ;
			
			alert.setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
				public void onClick( DialogInterface dialog, int whichButton ) {
					teacherInfo = input.getText().toString();
					// Do something with value!
				}
			});
				
			alert.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
				public void onClick( DialogInterface dialog, int whichButton ) {
					// Canceled.
				}
			});

			alert.show();
	}
    
    protected void setSchoolInfo(String session_title, String session_desc) {
    	AlertDialog.Builder alert = new AlertDialog.Builder( mContext ) ;
		
		alert.setTitle( session_title ) ;
		alert.setMessage( session_desc ) ;
		 
			//Set an EditText view to get user input
		 	final EditText input2 = new EditText( mContext ) ;
		 	alert.setView( input2 ) ;
		 	input2.setText( schoolInfo ) ;
		 	
		 	alert.setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
		 		public void onClick( DialogInterface dialog, int whichButton ) {
		 			schoolInfo = input2.getText().toString();
		 			// Do something with value!
		 		}
			});
			
		 	alert.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
		 		public void onClick( DialogInterface dialog, int whichButton ) {
		 			// Canceled.
		 		}
		 	});

		 	alert.show();
    }
   */ 
    @Override
	protected void onResume() {
    	if (!rapi.isConnectedToInternet())
    		showDialog(DIALOG_NO_CONNECT);
    	if (!userLoggedIn) attemptLogin();
    	initLocManager();
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

public static File convertImageUriToFile (Uri imageUri, Activity activity)  {
		Cursor cursor = null;
		try {
		    String [] proj={MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
		    cursor = activity.managedQuery( imageUri,
		            proj, // Which columns to return
		            null,       // WHERE clause; which rows to return (all rows)
		            null,       // WHERE clause selection arguments (none)
		            null); // Order-by clause (ascending by name)
		    int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
		    if (cursor.moveToFirst()) {
		        @SuppressWarnings("unused")
		    		String orientation =  cursor.getString(orientation_ColumnIndex);
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
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }
	
	private Runnable uploader = new Runnable() {
		@Override
		public void run() {
			new TaskWait().execute();
			
			/*
			if (teacherInfo != null) teacher = " - " + teacherInfo ;
			else teacher = "" ;
			
			if (schoolInfo != null ) school = " - " + schoolInfo ;
			else school = "" ;
			*/
			if (!smartUploading || (smartUploading && !calledBySmartUp)) {
				if(Descriptor.desString.equals("")) Descriptor.desString = "No description provided.";
			
				int sessionId = rapi.createSession(experimentNum, 
						name.getText().toString() + ": " + Descriptor.desString, //teacher + school, 
						Descriptor.desString, "n/a", "Lowell, MA", "");
		
				JSONArray dataJSON = new JSONArray();
				try {
					dataJSON.put(curTime); dataJSON.put(Lat); dataJSON.put(Long); dataJSON.put(Descriptor.desString) ;
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			
				finishedUploadSetup = true;
				dia.setProgress(95);
			
				Boolean result = rapi.updateSessionData(sessionId, experimentNum, dataJSON);
			
				if (result) {
					rapi.uploadPictureToSession(picture, experimentNum, 
							sessionId, name.getText().toString() + ": " + Descriptor.desString,// + teacher + school, 
							name.getText().toString() + Descriptor.desString);
				}
			} else smartUploader(uploaderPic.file, uploaderPic.latitude, uploaderPic.longitude, 
					uploaderPic.name, uploaderPic.desc, uploaderPic.time);
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == CAMERA_PIC_REQUESTED) {
			if(resultCode == RESULT_OK) {
				
				curTime = System.currentTimeMillis();
				picture = convertImageUriToFile(imageUri, this);
		        takePicture.setEnabled(false);
		        if (smartUploading) {
		        	if (userLoggedIn)  new Task().execute();
		        	else qsave(picture);
		        } else {
		        	if (userLoggedIn) new Task().execute();
		        	else showDialog(DIALOG_NOT_LOGGED_IN);
		        }
			}
		} /*else if (requestCode == EXPERIMENT_CODE) {
    			if (resultCode == Activity.RESULT_OK) {
    				experimentInput.setText("" + data.getExtras().getInt("edu.uml.cs.isense.pictures.experiments.exp_id"));
    			}
			}*/
	}
	
    
	@Override
    public void onLocationChanged(Location location) {
	loc = location ;
	
		if(gpsWorking == true) {
			Lat  = loc.getLatitude ();
			Long = loc.getLongitude();
		} else {
			showDialog(DIALOG_NO_GPS);
		}
	}
      
	@Override
    public void onProviderDisabled(String provider) {
		gpsWorking = false;
    }

    @Override
    public void onProviderEnabled(String provider) {
    	gpsWorking = true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
    }

    private class Task extends AsyncTask <Void, Integer, Void> {
          
        @Override protected void onPreExecute() {
            dia = new ProgressDialog(pictures.this);
            dia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dia.setMessage("Please wait while your picture is uploaded...");
            dia.setCancelable(false);
            dia.show();
        }

        @Override protected Void doInBackground(Void... voids) {
           
            // run the thread stuff in the background
            //Thread thread = new Thread(null, uploader, "MagentoBackground");
            //thread.start();
           
            uploader.run();
            finishedUploadSetup = false;
           
            publishProgress(100);
            return null;
        }

        @Override public void onProgressUpdate(Integer... prog) {
            if (prog == null)
                return;
            dia.setProgress(prog[0]);
        }

        @Override  protected void onPostExecute(Void voids) {
            dia.setMessage("Done");
            dia.cancel();
            
            Toast.makeText(pictures.this, "Your picture has uploaded successfully.", Toast.LENGTH_LONG).show();
                        
            pictures.c1  = false; pictures.c2  = false; pictures.c3 = false;
	        pictures.c4  = false; pictures.c5  = false; pictures.c6 = false;
	        pictures.c7  = false; pictures.c8  = false; pictures.c9 = false;
	        pictures.c10 = false; pictures.c11 = false;
        }
    }
   
    private class TaskWait extends AsyncTask<Void,Integer, Void> {
        @Override protected Void doInBackground(Void... voids) {
            for(int i = 1; i <= 85; i++) {
            	if (!finishedUploadSetup) {
            		try {
            			publishProgress(i);
            			Thread.sleep(50);
            		} catch(InterruptedException e) {
            			e.printStackTrace();
            		}
            	}
            }
            return null;
        }
       
        @Override public void onProgressUpdate(Integer... prog) {
            if (prog == null)
                return;
            dia.setProgress(prog[0]);
        }
       
        @Override protected void onPostExecute(Void voids) {
            dia.setMessage("Your content is taking a while.  Please be patient.");
        }
    }
    
    //repeatedly tries to connect
    private class NotConnectedTask extends AsyncTask <Void, Integer, Void> {
	    
		@Override protected Void doInBackground(Void... voids) {
	    	try {
	    		Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        return null;
		}
		
	    @Override  protected void onPostExecute(Void voids) {
	    	if(rapi.isConnectedToInternet())
	    		Toast.makeText(pictures.this, "Connectivity found!", Toast.LENGTH_SHORT).show();
	    	else {
	    		userLoggedIn = false;
	    		showDialog(DIALOG_NO_CONNECT);
	    	}
	    }
	}
    
    @Override
    public void onBackPressed() {	
    }
    
    //gets the user's name if not already provided + login to web site
  	private void attemptLogin() {
  		if(rapi.isConnectedToInternet()) {
          	boolean success = rapi.login(loginName, loginPass);
          	if(!success) {
          		showDialog(DIALOG_DIFFICULTY);        		
          	} else userLoggedIn = true;
          	
          } else {
        	userLoggedIn = false;
          	showDialog(DIALOG_NO_CONNECT);
          }
  	}
  	
  	//initialize location listener to get a point
  	private void initLocManager() {
  		Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(c, true), 0, 0, pictures.this);
        new Location(mLocationManager.getBestProvider(c, true));
  	}
  	
  	//save picture data in a queue for later upload
  	private void qsave(File pictureFile) {
  		Picture mPic = new Picture(pictureFile, Lat, Long, name.getText().toString(), Descriptor.desString);		
  		mQ.add(mPic);
  		QUEUE_COUNT++;
  	}
  	
  	//get picture data from the q to upload
  	private Picture getPicFromQ() {
  		Picture mPic = null;
  		try {
  			mPic = mQ.remove();
  		} catch (NoSuchElementException e) 
  		{}
  		return mPic;
  	}
  	
  	//upload stuff from the queue
  	private void smartUploader(File f, double lat, double lon, String n, String d, long t) {
  		if (d == "") d = "No description provided.";
		int sessionId = rapi.createSession(experimentNum, name + ": " + d, d, "n/a", "Lowell, MA", "");
		
		JSONArray dataJSON = new JSONArray();
		try {
			dataJSON.put(t); dataJSON.put(lat); dataJSON.put(lon); dataJSON.put(d) ;
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	
		finishedUploadSetup = true;
		dia.setProgress(95);
	
		Boolean result = rapi.updateSessionData(sessionId, experimentNum, dataJSON);
	
		if (result)
			rapi.uploadPictureToSession(picture, experimentNum, sessionId,n + ": " + d, d);
  	}
 
  	//uploads pictures if smartUploading is enabled
  	private void uploadPicture() {
		if (QUEUE_COUNT > 0)  {
  			uploaderPic = getPicFromQ();
  			if (uploaderPic != null) {
  				QUEUE_COUNT--;
  				uploader.run();
  			}
		}
  	}
  	
  	@Override
	protected void onStop() {
  		mLocationManager.removeUpdates(pictures.this);
  		gpsWorking = false;
		super.onStop();
	}
  	
  	private class WaitTenSecondsTask extends AsyncTask <Void, Integer, Void> {  
		@Override protected Void doInBackground(Void... voids) {
	    	try {
	    		Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	
	    	return null;
		}
		@Override
		protected void onPostExecute(Void result) {
	 		if(rapi.isConnectedToInternet()) {
	  			boolean success = rapi.login(loginName, loginPass);
	  			if(success) {
	  				Toast.makeText(pictures.this, "Connectivity found!", Toast.LENGTH_SHORT).show();
	  				userLoggedIn = true;
	  				if (QUEUE_COUNT > 0) {
	 				   while (QUEUE_COUNT > 0) uploadPicture();
	 			   }
	  			}
	  		} else {
	  			userLoggedIn = false;
	  			if (smartUploading) waitingForConnectivity();
	  			else new NotConnectedTask().execute();
	 	   }
			super.onPostExecute(result);	
		}
  	}
  	public void waitingForConnectivity() {
  		new WaitTenSecondsTask().execute();
   	}

}