package edu.uml.cs.isense.datawalk1;

import edu.uml.cs.isense.datawalk1.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private Context mContext;
	
	private static boolean dontToastMeTwice = false;
	
	static final public int NAME_SUCCESSFUL = 1;
	static final public int NAME_FAILED = 0;
	static final public int NAME_CANCELED = -1;
	
	boolean success;
	
	private static final String blankFields = "Do not leave any fields blank.  Please enter your first name and last initial.";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entername);
		
		mContext = this;
		
		final EditText firstNameInput   = (EditText) findViewById(R.id.nameInput);
		final EditText lastInitialInput = (EditText) findViewById(R.id.initialInput);
		final Button   okButton         = (Button)   findViewById(R.id.OK);
	
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(firstNameInput.length() == 0 || lastInitialInput.length() == 0) {
					   if (!dontToastMeTwice) {
						   showFailure();
					   	   new NoToastTwiceTask().execute();
					   }
				   } else {
					   DataWalk.firstName   = firstNameInput.getText().toString();
					   DataWalk.lastInitial = lastInitialInput.getText().toString();
					   setResult(NAME_SUCCESSFUL, null);
					   finish();
				   }
			}
		
		});
		
		// Display the End User Agreement
	    AlertDialog.Builder adb = new SimpleEula(this).show();
	    if(adb != null) {
	    	Dialog dialog = adb.create();
	    	
	    	Display display = getWindowManager().getDefaultDisplay(); 
	    	int mwidth = display.getWidth();
	    	int mheight = display.getHeight();
	    	
	    	dialog.show();
	    	
	    	int apiLevel = getApiLevel();
	    	if(apiLevel >= 11) {

	    		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    	
	    		lp.copyFrom(dialog.getWindow().getAttributes());
	    		lp.width = mwidth;
	    		lp.height = mheight;
	    		lp.gravity = Gravity.CENTER_VERTICAL;
	    		lp.dimAmount=0.7f;
	    	
	    		dialog.getWindow().setAttributes(lp);
	    		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    		
	    	}
	    }
		
		
	}
    
	private void showFailure() {
		Toast.makeText(mContext, blankFields, Toast.LENGTH_LONG).show();
	}
	
	static int getApiLevel() {
    	return android.os.Build.VERSION.SDK_INT;
    }
	
	@Override
    public void onBackPressed() {
		//don't do anything
	}
	
	private class NoToastTwiceTask extends AsyncTask <Void, Integer, Void> {
	    @Override protected void onPreExecute() {
	    	dontToastMeTwice = true;
	    }
		@Override protected Void doInBackground(Void... voids) {
	    	try {
	    		Thread.sleep(1500);
	    		Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        return null;
		}
	    @Override  protected void onPostExecute(Void voids) {
	    	dontToastMeTwice = false;
	    }
	}
	
	
    

}		
		

