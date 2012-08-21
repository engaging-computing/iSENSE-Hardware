/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII               iSENSE uPPT Reader App                      SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                    By: Michael Stowell,                      SSS                **/
/**           III                        Jeremy Poulin,                         SSS               **/
/**           III                        Nick Ver Voort                          SSSSSSSSS        **/
/**           III                    Faculty Advisor:  Fred Martin                      SSS       **/
/**           III                    Group:            ECG,                              SSS      **/
/**           III                                      iSENSE                           SSS       **/
/**      IIIIIIIIIIIII               Property:         UMass Lowell              SSSSSSSSS        **/
/**                                                                                               **/
/***************************************************************************************************/
/***************************************************************************************************/

package edu.uml.cs.isense.uppt;

import java.util.Calendar;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import edu.uml.cs.isense.comm.RestAPI;

public class Main extends Activity {
    
	private static String username = "";
	private static String password = "";
	private static String experimentId = "";
	private static String sessionName = "";
	private static String sessionId = "";
	
	private static final String baseUrl = "http://isensedev.cs.uml.edu/newvis.php?sessions=";
	
	private Vibrator vibrator;
	
	private static final int MENU_ITEM_LOGIN   = 0;
	
    static final public int DIALOG_CANCELED = 0;
    static final public int DIALOG_OK = 1;
    static final public int DIALOG_PICTURE = 2;
    
    private RestAPI rapi ;
    
    private ProgressDialog dia;
     
    private static boolean useMenu = true;
    
	private long uploadTime;
	public JSONArray data;
	
	public static Context mContext;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mContext = this;
        
        rapi = RestAPI.getInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE), getApplicationContext());
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
    }
    
    long getUploadTime() {
        Calendar c = Calendar.getInstance();
        return (long) (c.getTimeInMillis()); 
    }


	@Override
    public void onPause() {
    	super.onPause();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    }
    
    @Override
    public void onStart() {
    	super.onStart();	
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    }
    
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ITEM_LOGIN, Menu.NONE, "Login");
		return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (!useMenu) {
            menu.getItem(0).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(true );
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case MENU_ITEM_LOGIN:
        		// do login stuff
        		return true;
        	
        }
        return false;
    }
    
    
    static int getApiLevel() {
    	return android.os.Build.VERSION.SDK_INT;
    }
    
    @Override  
    public void onActivityResult(int reqCode, int resultCode, Intent data) {  
        super.onActivityResult(reqCode, resultCode, data); 
        // do activity result stuff
    }
	
	private Runnable uploader = new Runnable() {

		public void run() {
			
			// Do rapi uploading stuff
			
		}
	};

	private class UploadTask extends AsyncTask <Void, Integer, Void> {
	    
	    @Override protected void onPreExecute() {
	    	
	    	vibrator.vibrate(250);
	        dia = new ProgressDialog(Main.this);
	        dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        dia.setMessage("Uploading uPPT data set to iSENSE...");
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
	        
	        // do post execute stuff

	    }
	}	
    
}