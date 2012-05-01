package edu.uml.cs.isense.pictures;

/* Experiment 294 Now 347 */

import java.io.File;

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
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.uml.cs.isense.comm.RestAPI;

public class pictures extends Activity implements LocationListener {
	private final static int CAMERA_PIC_REQUESTED = 1;
	private static final int EXPERIMENT_CODE = 2;

	static final private int DIALOG_LOGIN_ID = 0;
	static final private int DIALOG_REJECT = 1;

	private LocationManager mLocationManager;
	
	private Uri imageUri; 
	
	RestAPI rapi;
	
	private EditText name; 
	
	private EditText experimentInput;
	
	private Location loc ;
	
	private String teacherInfo       ;
	private String schoolInfo        ;
	private String teacher           ;
	private String school            ;
	private double Lat  =  42.6404   ; 
	private double Long = -71.3533   ;
	private long   curTime           ;

	
	private Context mContext;
	
	private File picture;
	
	private Button takePicture;
	
	/** MIK **/
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
	/**     **/
	
	protected Dialog onCreateDialog(final int id) {
	    Dialog dialog;
	    switch(id) {
	    case DIALOG_LOGIN_ID:
	    	LoginActivity la = new LoginActivity(mContext);
	        dialog = la.getDialog(new Handler() {
			      public void handleMessage(Message msg) { 
			    	  switch (msg.what) {
			    	  	case LoginActivity.LOGIN_SUCCESSFULL:
			    	  	  Toast.makeText(mContext, "Loggin successful", Toast.LENGTH_LONG);
			    	  	  break;
			    	  	case LoginActivity.LOGIN_CANCELED:
			    		  break;
			    	  	case LoginActivity.LOGIN_FAILED:
				    	  showDialog(DIALOG_LOGIN_ID);
			    		  break;
			    	  }
			      }
        		});
                
	        break;
	    case DIALOG_REJECT:
	        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setMessage("Please enter a name and experiment ID");
	        builder.setPositiveButton("Ok", null);
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
        
        takePhoto = (Button) findViewById(R.id.takePicture);
        takePhoto.setEnabled(false);
        
        Button browse = (Button) findViewById(R.id.BrowseButton);
        browse.setEnabled(false);
        
        EditText input = (EditText) findViewById(R.id.ExperimentInput);
        input.setText("347");
        input.setEnabled(false);
        
        
        
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        
        mContext = this;
        
        rapi = RestAPI.getInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE), getApplicationContext());
        
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);

        mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(c, true), 0, 0, pictures.this);
        
        new Location(mLocationManager.getBestProvider(c, true));
        
        name = (EditText) findViewById(R.id.name);
        
		experimentInput = (EditText) findViewById(R.id.ExperimentInput);
		
		describe = (Button) findViewById(R.id.describeButton);
	/** MIK2 **/
		describe.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent startDescribe = new Intent(pictures.this, Descriptor.class);
				startActivity(startDescribe);
				//takePhoto.setEnabled(true);
			}
		});
	/**********/
        
        takePicture = (Button) findViewById(R.id.takePicture);
        
        takePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (name.getText().length() == 0 || experimentInput.getText().length() == 0) {
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
        
        Button browseButton = (Button) findViewById(R.id.BrowseButton);
        browseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				/*Intent experimentIntent = new Intent(getApplicationContext(), Experiments.class);
				
				experimentIntent.putExtra("edu.uml.cs.isense.pictures.experiments.prupose", EXPERIMENT_CODE);
				
				startActivityForResult(experimentIntent, EXPERIMENT_CODE);*/
			}
			
		});
	
     showDialog(DIALOG_LOGIN_ID);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem.OnMenuItemClickListener menuClicker = new
		MenuItem.OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem item) {
				
				if( item.getItemId() == 1 ) setSessionInfo ( "Teacher", "Please enter your teacher's name."   ) ;
				if( item.getItemId() == 2 )	setSessionInfo2( "School", "Please enter the name of your school.") ;
	    		
	    		return false;
			}
		};
		
		menu.add(1, 1, 1, "Teacher" ).setOnMenuItemClickListener( menuClicker );
        menu.add(1, 2, 2, "School"  ).setOnMenuItemClickListener( menuClicker ); 

        return super.onCreateOptionsMenu( menu ) ; 
		
	}
    
    protected void setSessionInfo(String session_title, String session_desc) {
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
    
    protected void setSessionInfo2(String session_title, String session_desc) {
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
    
    @Override
	protected void onResume() {
		super.onResume();
		//showDialog(DIALOG_LOGIN_ID);
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
			
			/**JER 3**************************************************************************************************************/
			if (teacherInfo != null) teacher = " - " + teacherInfo ;
			else teacher = "" ;
			
			if (schoolInfo != null ) school = " - " + schoolInfo ;
			else school = "" ;
									
			if(Descriptor.desString.equals("")) Descriptor.desString = "No description provided.";
			
			int sessionId = rapi.createSession(experimentInput.getText().toString(), 
					name.getText().toString() + teacher + school, 
					Descriptor.desString, "n/a", "Lowell, MA", "");
			/**********************************************************************************************************************/
		
			JSONArray dataJSON = new JSONArray();
			try {
				dataJSON.put(curTime); dataJSON.put(Lat); dataJSON.put(Long); dataJSON.put(Descriptor.desString) ;
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
				
			dia.setProgress(95);
			
			Boolean result = rapi.updateSessionData(sessionId, experimentInput.getText().toString(), dataJSON);
			
			if (result) {
				rapi.uploadPictureToSession(picture, experimentInput.getText().toString(), 
						sessionId, name.getText().toString() + teacher + school, 
						name.getText().toString() + Descriptor.desString);
			}
			
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == CAMERA_PIC_REQUESTED) {
			if(resultCode == RESULT_OK) {
				
				curTime = System.currentTimeMillis()/1000 ;
				picture = convertImageUriToFile(imageUri, this);
				/*String picturePath = picture.getAbsolutePath();
	            
				
				Bitmap bitmapOrg ;
				// load the original BitMap (500 x 500 px)
				if( BitmapFactory.decodeFile(picturePath) != null ) {
					bitmapOrg = BitmapFactory.decodeFile(picturePath) ;
					int width = bitmapOrg.getWidth();
					int height = bitmapOrg.getHeight();
					int newWidth = 1024;
					int newHeight = 768;
		        
					// calculate the scale
					float scaleWidth = ((float) newWidth) / width;
					float scaleHeight = ((float) newHeight) / height;
		        
					// create a matrix for the manipulation
					Matrix matrix = new Matrix();
		        
					// resize the bit map
					matrix.postScale(scaleWidth, scaleHeight);
		        
					// recreate the new Bitmap
					bitmapOrg = Bitmap.createBitmap(bitmapOrg, 1, 1, 
							1024, 768, matrix, true);
					try {
						bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 10, new FileOutputStream( mpicture )) ;
						//FileOutputStream.this.close();
					} catch (FileNotFoundException e) {
						// Auto-generated catch block
						e.printStackTrace();
					}	        		        
				}*/
				
		        takePicture.setEnabled(false);
		        new Task().execute();
			}
		} else if (requestCode == EXPERIMENT_CODE) {
    		if (resultCode == Activity.RESULT_OK) {
    			experimentInput.setText("" + data.getExtras().getInt("edu.uml.cs.isense.pictures.experiments.exp_id"));
    		}
		}
	}
	
    
	@Override
    public void onLocationChanged(Location location) {
	loc = location ;
	
		if( loc.getLatitude() != 0 && loc.getLongitude() != 0 ){
			Lat  = loc.getLatitude ();
			Long = loc.getLongitude();
		}
		else {
			Toast.makeText(mContext, "Please make sure GPS is on.", Toast.LENGTH_SHORT).show();
		}
	}
      
	@Override
    public void onProviderDisabled(String provider) {
      Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
      Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
    }

    /** Mike's AsyncTask 1 and 2 */
    
    private class Task extends AsyncTask <Void, Integer, Void> {
          
        @Override protected void onPreExecute() {
            dia = new ProgressDialog(pictures.this);
            dia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dia.setMessage("Please wait while your picture is uploaded...");
            dia.setCancelable(false);
            dia.show();
        }

        @Override protected Void doInBackground(Void... voids) {
           
            // run the thread crap in the background
            /*Thread thread = new Thread(null, uploader, "MagentoBackground");
            thread.start();*/
           
            uploader.run();
           
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
                try {
                    publishProgress(i);
                    Thread.sleep(50);
                } catch(InterruptedException e) {
                    e.printStackTrace();
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
   
   
   
    /*********************/
    
}