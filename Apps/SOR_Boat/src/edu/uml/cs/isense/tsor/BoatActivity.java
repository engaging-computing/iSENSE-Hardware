/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII               iSENSE TSOR Boat App                        SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                    By: Michael Stowell,                      SSS                **/
/**           III                        Jeremy Poulin                          SSS               **/
/**           III                                                                SSSSSSSSS        **/
/**           III                    Faculty Advisor:  Fred Martin                      SSS       **/
/**           III                    Group:            ECG,                              SSS      **/
/**           III                                      iSENSE                           SSS       **/
/**      IIIIIIIIIIIII               Property:         UMass Lowell              SSSSSSSSS        **/
/**                                                                                               **/
/***************************************************************************************************/
/***************************************************************************************************/

package edu.uml.cs.isense.tsor;

import java.text.DecimalFormat;
import java.util.Calendar;

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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.method.DigitsKeyListener;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.comm.RestAPI;

public class BoatActivity extends Activity implements LocationListener {
    
	private static String experimentNumber = "350";        // HARD CODED
	private static String userName         = "tsorboat";   // HARD CODED
	private static String password         = "ecgrul3s";   // HARD CODED
	/*private static int    sessionNumbers[] = {           // isense
		2904,  // Canals
		5224,  // Claypit Brook
		2905,  // Docks
		2891,  // Down River
		2906   // Up River	
	};*/
	private static int    sessionNumbers[] = {             // isensedev
		2904,  // Canals
		3546,  // Claypit Brook
		2905,  // Docks
		2891,  // Down River
		2906   // Up River	
	};
	
	private Vibrator vibrator;
	private Button   submit;
	private TextView field1;	// Diss. Oxygen
	private TextView field2;	// Phosphate
	private TextView field3;	// Copper
	private TextView field4;	// Water Temp
	private TextView field5;	// Air Temp
	private TextView field6;	// pH
	private TextView field7;	// Vernier Clarity
	private TextView field8;	// Secchi Clarity
	private Spinner  schools;
	private Spinner  location;
	private boolean  running = false;
	
	private LocationManager mLocationManager;
	
	//private Location loc;
	
	private String tempField1    = "";
	private String tempField2    = "";
	private String tempField3    = "";
	private String tempField4    = "";
	private String tempField5    = "";
	private String tempField6    = "";
	private String tempField7    = "";
	private String tempField8    = "";
	private String studentSchool = "";
	
	private static final int T_INVAL_WATER = 101;
	private static final int T_INVAL_AIR   = 102;
	private static final int T_INVAL_PH    = 103;
	private static final int T_INVAL_NUM   = 104;
	
	private static final int DIALOG_SUMMARY    = 1;
	private static final int MENU_ITEM_ABOUT   = 2;
	private static final int MENU_ITEM_RESTORE = 3;
	private static final int DIALOG_NO_CONNECT = 4;
	private static final int DIALOG_EXPIRED    = 5;
	private static final int DIALOG_DIFFICULTY = 6;
	private static final int DIALOG_NO_SCHOOL  = 7;
	private static final int DIALOG_DATA       = 8;
	private static final int DIALOG_YOU_SURE   = 9;
	private static final int MENU_ITEM_UPLOAD  = 10;
	private static final int DIALOG_EXP_CLOSED = 11;
	
    static final public int DIALOG_CANCELED = 0;
    static final public int DIALOG_OK = 1;
    static final public int DIALOG_PICTURE = 2;

    private int toastId = 0;
    
    //private boolean timeHasElapsed = false;
    //private boolean usedHomeButton = false;
    //private boolean appTimedOut    = false;
    
    //private String dateString;
    RestAPI rapi ;
    
    DecimalFormat toThou = new DecimalFormat("#,###,##0.000");
    
    int i = 0;  int len = 0;  int len2 = 0;
    
    ProgressDialog dia;
    double partialProg = 1.0;
    
    String nameOfSession = "";
    
    static int     mediaCount        = 0;
    static boolean inPausedState     = false;
    static boolean toastSuccess      = false;
    static boolean useMenu           = true ;
    static boolean setupDone         = false;
    static boolean choiceViaMenu     = false;
    static boolean dontToastMeTwice  = false;
    static boolean exitAppViaBack    = false;
    static boolean backWasPressed    = false;
    static boolean nameSuccess       = false;
    static boolean dontPromptMeTwice = false;
    static boolean needNewArray      = true;
    static boolean usedMenu          = false;
    static boolean status400         = false;
    
    public static String textToSession = "";
    public static String toSendOut = "";
    public static String experimentId = "";
    
    static int mheight = 1;
	static int mwidth = 1;
	long currentTime;
	public JSONArray data;
	public String uploadSchool;
	
	public static Context mContext;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mContext = this;
        
        Display deviceDisplay = getWindowManager().getDefaultDisplay(); 
    	mwidth  = deviceDisplay.getWidth();
    	mheight = deviceDisplay.getHeight();
        
        rapi = RestAPI.getInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE), getApplicationContext());
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                      
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
         
        //loc = new Location(mLocationManager.getBestProvider(c, true));  
        
        schools = (Spinner) findViewById(R.id.schools);
        final ArrayAdapter<CharSequence> schoolAdapter = ArrayAdapter.createFromResource(this, R.array.school_array, android.R.layout.simple_spinner_item);
        schoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schools.setAdapter(schoolAdapter);
        
        location = (Spinner) findViewById(R.id.location);
        final ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(this, R.array.location_array, android.R.layout.simple_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        location.setAdapter(locationAdapter);
        
        field1 = (TextView) findViewById(R.id.field1Field);
        field2 = (TextView) findViewById(R.id.field2Field);
        field3 = (TextView) findViewById(R.id.field3Field);
        field4 = (TextView) findViewById(R.id.field4Field);
        field5 = (TextView) findViewById(R.id.field5Field);
        field6 = (TextView) findViewById(R.id.field6Field);
        field7 = (TextView) findViewById(R.id.field7Field);
        field8 = (TextView) findViewById(R.id.field8Field);
        
        submit = (Button) findViewById(R.id.submitButton);  
        submit.getBackground().setColorFilter(0xff00ffff, PorterDuff.Mode.MULTIPLY);
        submit.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (schools.getSelectedItem().toString().equals("Other") && studentSchool.equals(""))
					showDialog(DIALOG_NO_SCHOOL);
				else {
					boolean canUpload = canBeginUploading();
					if (canUpload) {
					
						usedMenu = false;
						new Task().execute();
						
					} else
						displayToast(toastId);
				}
				
				return running;
			}
			
        });
        
        
        if(rapi.isConnectedToInternet()) {
        	boolean success = rapi.login(userName, password);
        	if(!success) {
        		if(rapi.connection == "600") {
        			showDialog(DIALOG_EXPIRED);
            		//appTimedOut = true;
        		} else {
        			showDialog(DIALOG_DIFFICULTY);
        		}
        		
        	} 
        } else {
        	showDialog(DIALOG_NO_CONNECT);
        }
            
    } 
    
    void makeToast(String message, int length) {
    	if (length == Toast.LENGTH_SHORT) {
    		if(!dontToastMeTwice)
    			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    		new NoToastTwiceTask().execute();
    	} else {
    		if(!dontToastMeTwice)
    			Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    		new NoToastTwiceTask().execute();
    	}
    }
    
    boolean canBeginUploading() {
    	try {
    		
    	if(!field1.getText().toString().equals(""))
    		
    		if(!field1.getText().toString().equals(""))	{
    			if ((Float.parseFloat(field1.getText().toString())) > 1000000000000.0 ) {
    	    		return true;
    	    	}
    		} else if(!field2.getText().toString().equals("")) {
    			if ((Float.parseFloat(field2.getText().toString())) > 1000000000000.0 ) {
    	    		return true;
    	    	}
    		} else if(!field3.getText().toString().equals("")) {
    			if ((Float.parseFloat(field3.getText().toString())) > 1000000000000.0 ) {
    	    		return true;
    	    	}
    		} else if(!field4.getText().toString().equals("")) {
    			if ((Float.parseFloat(field4.getText().toString())) < 0.0 || (Float.parseFloat(field4.getText().toString())) > 100.0) {
    	    		toastId = T_INVAL_WATER;
    	    	}
    		} else if(!field5.getText().toString().equals("")) {
    			if ((Float.parseFloat(field5.getText().toString())) < 0.0 || (Float.parseFloat(field5.getText().toString())) > 100.0) {
    	    		toastId = T_INVAL_AIR;
    	    		return false;
    	    	}
    		} else if(!field6.getText().toString().equals("")) {
    			if ((Float.parseFloat(field6.getText().toString())) < 0.0 || (Float.parseFloat(field6.getText().toString())) > 14.0) {
    	    		toastId = T_INVAL_PH;
    	    		return false;
    	    	}
    		} else if(!field7.getText().toString().equals("")) {
    			if ((Float.parseFloat(field7.getText().toString())) > 1000000000000.0 ) {
    	    		return true;
    	    	}
    		} else if(!field8.getText().toString().equals("")) {
    			if ((Float.parseFloat(field8.getText().toString())) > 1000000000000.0 ) {
    	    		return true;
    	    	}
    		}
    	
    	} catch (java.lang.NumberFormatException nfe) {
    		toastId = T_INVAL_NUM;
    		return false;
    	}
    	
    	return true;
    }
    
    void displayToast(int id) {
    	switch (id) {
    		
    	case T_INVAL_PH:
    		if(!dontToastMeTwice)
    			Toast.makeText(mContext, "Invalid pH value.  Enter a value between 0 - 14.", Toast.LENGTH_LONG).show();
    		new NoToastTwiceTask().execute();
    		break;
    	
    	case T_INVAL_WATER:
    		if(!dontToastMeTwice)
    			Toast.makeText(mContext, "Invalid water temperature value.  Enter a value between 0 - 100.", Toast.LENGTH_LONG).show();
    		new NoToastTwiceTask().execute();
    		break;
    		
    	case T_INVAL_AIR:
    		if(!dontToastMeTwice)
    			Toast.makeText(mContext, "Invalid air temperature value.  Enter a value between 0 - 100.", Toast.LENGTH_LONG).show();
    		new NoToastTwiceTask().execute();
    		break;
    		
    	case T_INVAL_NUM:
    		if(!dontToastMeTwice)
    			Toast.makeText(mContext, "Something you entered is not a legal number.  Check for multiple decimal points.", Toast.LENGTH_LONG).show();
    		new NoToastTwiceTask().execute();
    		break;
    		
    	default:
    		if(!dontToastMeTwice)
    			Toast.makeText(mContext, "Error!", Toast.LENGTH_SHORT).show();
    		new NoToastTwiceTask().execute();
    		break;
    	}
    }
    
    long getUploadTime() {
        Calendar c = Calendar.getInstance();
        return (long) (c.getTimeInMillis()); 
    }


	@Override
    public void onPause() {
    	super.onPause();
    	mLocationManager.removeUpdates(BoatActivity.this);
    	inPausedState = true;
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	mLocationManager.removeUpdates(BoatActivity.this);
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
    	if(!rapi.isConnectedToInternet())
    		showDialog(DIALOG_NO_CONNECT);
    }
    
    @Override
    public void onBackPressed() {
    	if(!dontToastMeTwice) {
    		Toast.makeText(this, "Do not press back to exit app - use home button instead.", 
    				Toast.LENGTH_LONG).show();
    		new NoToastTwiceTask().execute();
    	} /*else if(exitAppViaBack) {
    		setupDone = false;
    		super.onBackPressed();
    	}*/
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ITEM_ABOUT, Menu.NONE, "About");
		menu.add(Menu.NONE, MENU_ITEM_RESTORE, Menu.NONE, "Restore Fields");
		menu.add(Menu.NONE, MENU_ITEM_UPLOAD, Menu.NONE, "Upload");
		return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (!useMenu) {
            menu.getItem(0).setEnabled(false);
            menu.getItem(1).setEnabled(false);
            menu.getItem(2).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(true );
            menu.getItem(1).setEnabled(true );
            menu.getItem(2).setEnabled(true );
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case MENU_ITEM_ABOUT:
        		showDialog(MENU_ITEM_ABOUT);
        		return true;
        	case MENU_ITEM_RESTORE:
        		field1.setText(tempField1);
        		field2.setText(tempField2);
        		field3.setText(tempField3);
        		field4.setText(tempField4);
        		field5.setText(tempField5);
        		field6.setText(tempField6);
        		field7.setText(tempField7);
        		field8.setText(tempField8);
        		return true;
        	case MENU_ITEM_UPLOAD:
        		if(data == null || data.length() == 0) 
        			makeToast("No data to upload!", Toast.LENGTH_SHORT);
        		else {
        			if(rapi.isConnectedToInternet()) {
        				usedMenu = true;
        				new Task().execute();
        			} else
        				makeToast("No connectivity found yet.  Try again later.", Toast.LENGTH_LONG);
        		}
        		return true;
        }
        return false;
    }

	@Override
	public void onLocationChanged(Location location) {
		//loc = location;
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
	    	
	    case DIALOG_SUMMARY:
	    	String appendMe;
	    	if (needNewArray)
	    		appendMe = "\n\nNOTE: All of your data has uploaded, so exiting the app at " +
	    				   "this point is safe.";
	    	else
	    		appendMe = "\n\nWARNING: Your data has NOT been uploaded to iSENSE, so " +
	    				   "exiting the app will erase ALL recorded data.";
	    	
	    	builder.setTitle("Process Complete")
	    	.setMessage("Would you like to continue to enter more data or quit the app?" + appendMe)
	    	.setCancelable(false)
	    	.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.dismiss();
	               }
	    	})
	    	.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.dismiss();
	            	   showDialog(DIALOG_YOU_SURE);
	            	   //((Activity) mContext).finish();
	               }
	    	})
	        .setCancelable(true);
	           
	    	dialog = builder.create();
	    
	    	break;
	    	
	    case MENU_ITEM_ABOUT:
	    	
	    	builder.setTitle("About")
	    	.setMessage("This is the Android Application designed for the teacher to upload data collected at the river to iSENSE.\n\n" +
	    				"If connectivity is available, data is immediately uploaded upon holding the the \"Submit\" button. " +
	    				"If no connectivity is available, data is stored until uploaded at a later time.  The user may attempt " +
	    				"to upload again by using Menu->Upload.")
	    	.setCancelable(false)
	    	.setNegativeButton("Back", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.dismiss();
	               }
	    	})
	        .setCancelable(true);
	           
	    	dialog = builder.create();
	    
	    	break;
	    	
	    	
	    case DIALOG_NO_CONNECT:
	    	
	    	builder.setTitle("No Connectivity")
	    	.setMessage("Could not connect to the internet through either wifi or mobile service. " +
	    			"You may continue to use the app, but data cannot be uploaded to iSENSE. It will instead " +
	    			"be stored until you attempt to upload it again with wifi or mobile service connectivity.  Would you like to " +
	    			"attempt to find a wifi signal or mobile connectivity again?")
	    	.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
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
	            		   boolean success = rapi.login(userName, password);
	            		   if(success) {
	            			   Toast.makeText(BoatActivity.this, "Connectivity found!", Toast.LENGTH_SHORT).show();
	            		   } else {
	            			   showDialog(DIALOG_EXPIRED);
	            			   //appTimedOut = true;
	            		   }
	            	   } else {
	            		   dialoginterface.dismiss();
	            		   new NotConnectedTask().execute();
	            	   }
	               }
	    	})
	        .setOnCancelListener(new DialogInterface.OnCancelListener() {
	    		@Override
					public void onCancel(DialogInterface dialoginterface) {
	    				dialoginterface.dismiss();
	            	    //((Activity)mContext).finish();
					}
	    	})
	        .setCancelable(true);
	           
	    	dialog = builder.create();
	    
	    	break;
	    	
	    case DIALOG_EXPIRED:
	    	
	    	builder.setTitle("Timed Out")
	    	.setMessage("This app has experienced difficulties logging in.  It appears the username/password for the default " +
	    				"uploading account has been changed.  You may allow the app to attempt to login again.")
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.dismiss();
	            	   if(rapi.isConnectedToInternet()) {
		    	        	boolean success = rapi.login(userName, password);
		    	        	if(!success) {
		    	        		if(rapi.connection == "600") {
		    	        			showDialog(DIALOG_EXPIRED);
		    	            		//appTimedOut = true;
		    	        		} else {
		    	        			showDialog(DIALOG_DIFFICULTY);
		    	        		}
		    	        		
		    	        	}	
		    			}
	               }
	    	})
	    	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.dismiss();
	            	   ((Activity)mContext).finish();
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
	    			if(rapi.isConnectedToInternet()) {
	    	        	boolean success = rapi.login(userName, password);
	    	        	if(!success) {
	    	        		if(rapi.connection == "600") {
	    	        			showDialog(DIALOG_EXPIRED);
	    	            		//appTimedOut = true;
	    	        		} else {
	    	        			showDialog(DIALOG_DIFFICULTY);
	    	        		}
	    	        		
	    	        	}	
	    			}
	    		}
	    	})
	        .setCancelable(true);
	           
	    	dialog = builder.create();
	    
	    	break;
	    	
	    case DIALOG_NO_SCHOOL:
	    	LinearLayout layout = new LinearLayout(this);
	        layout.setOrientation(LinearLayout.VERTICAL);
	        layout.setGravity(Gravity.CENTER_HORIZONTAL);
	        final EditText input = new EditText(this);
	        input.setSingleLine(true);
	        input.setKeyListener(DigitsKeyListener.getInstance("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz -_.,01234567879()"));
	        layout.setPadding(5, 0, 5, 0);
	        layout.addView(input);
	    	
	    	final AlertDialog d = new AlertDialog.Builder(mContext)
            .setTitle("Specify School")
            .setMessage("You chose \"Other\" for your school.  Please enter your school's name below.")
            .setCancelable(false)
            .setView(layout)
            .setPositiveButton("OK",
                    new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            //Do nothing here. We override the onclick
                        }
                    })
            .setNegativeButton("Cancel", null)
            .create();

	    	
	    	d.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {

                    Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                        	studentSchool = input.getText().toString();
     	            	   	if (studentSchool.equals("")) {
     	            	   		if (!dontToastMeTwice)
     	            	   			Toast.makeText(mContext, "Don't leave the field blank!", Toast.LENGTH_SHORT).show();
     	            	   		new NoToastTwiceTask().execute();  
     	            	   	} else {
     	            	   		d.dismiss();
     	            	   		boolean canUpload = canBeginUploading();
     	            	   			if (canUpload) {
     	            	   				usedMenu = false;
     	            	   				new Task().execute();
     	            	   			} else
     	            		   			displayToast(toastId);      	   
     	            	   	}
                        }
                    });
                }
            });
	           
	    	dialog = d;
	    
	    	break;
	    	
	    case DIALOG_DATA:
	    	String dataUploaded = "";
	    	StringBuilder sb = new StringBuilder();
	    		
	    	if(tempField1.length() != 0) {
	    		sb.append("Diss. Oxygen: ")
	    		  .append(tempField1)
	    		  .append(" ppm\n");
	    	}
	    	if(tempField2.length() != 0) {
	    		sb.append("Phosphate: ")
	    		  .append(tempField2)
	    		  .append(" ppm\n");
	    	}
	    	if(tempField3.length() != 0) {
	    		sb.append("Copper: ")
	    		  .append(tempField3)
	    		  .append(" ppm\n");
	    	}
	    	if(tempField4.length() != 0) {
	    		sb.append("Water Temp.: ")
	    		  .append(tempField4)
	    		  .append(" ¡C\n");
	    	}
	    	if(tempField5.length() != 0) {
	    		sb.append("Air Temp.: ")
	    		  .append(tempField5)
	    		  .append(" ¡C\n");
	    	}
	    	if(tempField6.length() != 0) {
	    		sb.append("pH: ")
	    		  .append(tempField6)
	    		  .append("\n");
	    	}
	    	if(tempField7.length() != 0) {
	    		sb.append("Vernier Clarity: ")
	    		  .append(tempField7)
	    		  .append(" NTU\n");
	    	}
	    	if(tempField8.length() != 0) {
	    		sb.append("Secchi Clarity: ")
	    		  .append(tempField8)
	    		  .append(" m\n");
	    	}
	    	
	    	if (needNewArray) {
	    		dataUploaded = "Yes";
	    	} else {
	    		dataUploaded = "No";
	    	}
	    	
	    	builder.setTitle("Data and Upload Summary")
	    	.setMessage("Data Uploaded: " + dataUploaded + "\n\n" +
	    				"Data Entered -\n" + 
	    				sb.toString())
	    	.setCancelable(false)
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.dismiss();
	            	   showDialog(DIALOG_SUMMARY);
	               }
	    	})
	        .setCancelable(false);
	           
	    	dialog = builder.create();
	    	
	    	break;
	    	
	    case DIALOG_YOU_SURE:
	    	builder
	    	.setMessage("\nAre you sure you want to exit?\n")
	    	.setCancelable(false)
	    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.dismiss();
	            	   ((Activity) mContext).finish();
	               }
	    	})
	    	.setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.dismiss();
	            	   showDialog(DIALOG_SUMMARY);
	               }
	    	})
	        .setCancelable(true);
	           
	    	dialog = builder.create();
	    	
	    	break;
	    	
	    case DIALOG_EXP_CLOSED:
	    	
	    	builder.setTitle("Experiment Closed")
	    	.setMessage("The experiment that this application is attempting to upload data to has been closed and can "
	    				+ "no longer accept data at this time.  Therefore, this app is useless until an iSENSE developer " 
	    				+ "has been contacted and has re-opened the experiment.")
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialoginterface, final int id) {
	    			dialoginterface.dismiss();
	    		}
	    	})
	    	.setNegativeButton("Quit App", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialoginterface, final int id) {
	    			dialoginterface.dismiss();
	    			((Activity) mContext).finish();
	    		}
	    	})
	        .setCancelable(true);
	           
	    	dialog = builder.create();
	    
	    	break;
	    	
	    default:
	    	dialog = null;
	    	break;
	    }
	   	    	
	    int apiLevel = getApiLevel();
	    if(apiLevel >= 11) {
	    	dialog.show(); /* works but doesnt center it */
		    	
		   	lp.copyFrom(dialog.getWindow().getAttributes());
		   	lp.width = mwidth;
		   	lp.height = WindowManager.LayoutParams.FILL_PARENT;
		   	lp.gravity = Gravity.CENTER_VERTICAL;
		   	lp.dimAmount=0.7f;
		   	
		   	dialog.getWindow().setAttributes(lp);
		   	dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		   	dialog.getWindow().setAttributes(lp);
		    dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		    	
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
    	return Integer.parseInt(android.os.Build.VERSION.SDK);
    }
    
    @Override  
    public void onActivityResult(int reqCode, int resultCode, Intent data) {  
        super.onActivityResult(reqCode, resultCode, data); 
        dontPromptMeTwice = false;
    }
	
	private Runnable uploader = new Runnable() {
		
		@Override
		public void run() {
		
			int sessionId  = -1;
			int locationId =  0;
			uploadSchool = "";
			double myLat = 0, myLon = 0;
			status400 = false;
			
			if (needNewArray)
				data = new JSONArray();
			
			JSONArray dataSet = new JSONArray();
			
			locationId = location.getSelectedItemPosition();
			sessionId = sessionNumbers[locationId];

			if (schools.getSelectedItem().toString().equals("Other"))
				uploadSchool = studentSchool;
			else
				uploadSchool = schools.getSelectedItem().toString();
			
			if (location.getSelectedItem().toString().equals("Up River")) {
			    myLat = 42.6379998;
			    myLon = -71.3560938;
			} else if (location.getSelectedItem().toString().equals("Down River")) {
			    myLat = 42.639705;
			    myLon = -71.354086;
			} else if (location.getSelectedItem().toString().equals("Docks")) {
			    myLat = 42.640084;
			    myLon = -71.352403;
			} else if (location.getSelectedItem().toString().equals("Canals")) {
			    myLat = 42.641031;
			    myLon = -71.328886;
			} else if (location.getSelectedItem().toString().equals("Claypit Brook")) {
				myLat = 42.642420;
				myLon = -71.352910;
			} else {
			    myLat = 0.0;
			    myLon = 0.0;
			    makeToast("Fatal error uploading latitude/longitude!", Toast.LENGTH_LONG);
			}
				
			if (!usedMenu) {
				try {
					dataSet .put(getUploadTime())
							.put(uploadSchool)
							.put("Manual")
							.put(myLat)
							.put(myLon)
							.put(field4.getText().toString())
							.put(field6.getText().toString())
							.put(field7.getText().toString())
							.put(field8.getText().toString())
							.put(field1.getText().toString())
							.put(field3.getText().toString())
							.put(field2.getText().toString())
							.put(field5.getText().toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			
				data.put(dataSet);
			}
			
			if (rapi.isConnectedToInternet()) {
				needNewArray = true;
				boolean success = rapi.updateSessionData(sessionId, experimentNumber, data);
				if (!success)
					status400 = true;
			} else
				needNewArray = false;
						
		}
		
	};

	private class Task extends AsyncTask <Void, Integer, Void> {
	    
	    @Override protected void onPreExecute() {
	    	
	    	vibrator.vibrate(250);
	        dia = new ProgressDialog(BoatActivity.this);
	        dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        dia.setMessage("Attempting Upload...");
	        dia.setCancelable(false);
	        dia.show();
	        
	    }

	    @Override protected Void doInBackground(Void... voids) {

	        uploader.run();

	    	publishProgress(100);
	        return null;
	        
	    }

	    @Override  protected void onPostExecute(Void voids) {
	        
	    	dia.setMessage("Done");
	        dia.cancel();
	        
	        len = 0; len2 = 0;
	        
	        if (needNewArray)
	        	data = new JSONArray();
	        
	        if (status400)
	        	showDialog(DIALOG_EXP_CLOSED);
	        else {
	        
	        	tempField1 = field1.getText().toString();
	        	field1.setText("");
	        	tempField2 = field2.getText().toString();
	        	field2.setText("");
	        	tempField3 = field3.getText().toString();
	        	field3.setText("");
	        	tempField4 = field4.getText().toString();
	        	field4.setText("");
	        	tempField5 = field5.getText().toString();
	        	field5.setText("");
	        	tempField6 = field6.getText().toString();
	        	field6.setText("");
	        	tempField7 = field7.getText().toString();
	        	field7.setText("");
	        	tempField8 = field8.getText().toString();
	        	field8.setText("");
	        
	        	if(usedMenu) {
	        		makeToast("All data uploaded successfully.", Toast.LENGTH_SHORT);
	        		showDialog(DIALOG_SUMMARY);
	        	} else
	        		showDialog(DIALOG_DATA);
	        }

	    }
	}	
	
	private class NoToastTwiceTask extends AsyncTask <Void, Integer, Void> {
	    @Override protected void onPreExecute() {
	    	dontToastMeTwice = true;
	    	exitAppViaBack   = true;
	    }
		@Override protected Void doInBackground(Void... voids) {
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
	    @Override  protected void onPostExecute(Void voids) {
	    	dontToastMeTwice = false;
	    }
	}
	
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
	    		Toast.makeText(BoatActivity.this, "Connectivity found!", Toast.LENGTH_SHORT).show();
	    	else
	    		showDialog(DIALOG_NO_CONNECT);
	    }
	}
    
    
}