package edu.uml.cs.isense.carphysicsv2;

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

public class EnterNameActivity extends Activity {
	
	private Context mContext;
	
	static final public int NAME_SUCCESSFUL = 1;
	static final public int NAME_FAILED = 0;
	static final public int NAME_CANCELED = -1;
	
	static boolean dontToastMeTwice  = false;
	boolean success;
	
	private static final String blankFields = "Do not leave any fields blank.  Please enter your first name and last initial.";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entername);
		
		mContext = this;
		
		final EditText firstNameInput   = (EditText) findViewById(R.id.nameInput);
		final EditText lastInitialInput = (EditText) findViewById(R.id.initialInput);
		final Button   okButton         = (Button)   findViewById(R.id.OkButton);
		
		/*final Message loginSuccess = Message.obtain();
		loginSuccess.what = NAME_SUCCESSFULL;
		
		final Message rejectMsg = Message.obtain();
		rejectMsg.what = NAME_CANCELED;*/

	
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(firstNameInput.length() == 0 || lastInitialInput.length() == 0) {
					   showFailure();
				   } else {
					   CarRampPhysicsV2.firstName   = firstNameInput.getText().toString();
					   CarRampPhysicsV2.lastInitial = lastInitialInput.getText().toString();
					   setResult(NAME_SUCCESSFUL, null);
					   finish();
				   }
			}
		
		});
		
		displayEula();
	}
	
	@Override
    public void onBackPressed() {
		/* to prevent user from escaping. muahahaha! */
	}
    
	private void showFailure() {
		if(!dontToastMeTwice) {
			Toast.makeText(mContext, blankFields, Toast.LENGTH_LONG).show();		
    		new NoToastTwiceTask().execute();
		}
			
	}
	
	void displayEula() {
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
	
	static int getApiLevel() {
    	return Integer.parseInt(android.os.Build.VERSION.SDK);
    }
	
	public class NoToastTwiceTask extends AsyncTask <Void, Integer, Void> {
	    @Override protected void onPreExecute() {
	    	dontToastMeTwice = true;
	    }
		@Override protected Void doInBackground(Void... voids) {
	    	try {
	    		Thread.sleep(3500);
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
		
		/*final Message msg = Message.obtain();
		msg.setTarget(h);
		msg.what = NAME_FAILED;
		message = blankFields;
			
		new AlertDialog.Builder(mContext)
			.setTitle("Error")
			.setMessage(message)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					msg.sendToTarget();
				}
			})
			.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            	msg.sendToTarget();
            }
			})
			.show();
    	
	}
	
}*/


