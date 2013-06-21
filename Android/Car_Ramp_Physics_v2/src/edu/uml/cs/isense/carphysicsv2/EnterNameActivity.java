package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.uml.cs.isense.R;

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
	
	public static final int EULA_REQUESTED = 8000;

	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = getPackageManager().getPackageInfo(getPackageName(),
					PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}

	void displayEula() {

		PackageInfo versionInfo = getPackageInfo();

		final String eulaKey = EulaActivity.EULA_PREFIX
				+ versionInfo.versionCode;
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean hasBeenShown = prefs.getBoolean(eulaKey, false);

		if (!hasBeenShown) {
			startActivityForResult(new Intent(this, EulaActivity.class),
					EULA_REQUESTED);
			SharedPreferences.Editor editor = prefs.edit();
        	editor.putBoolean(eulaKey, true);
        	editor.commit();
        	
		}

	}

	
	static int getApiLevel() {
    	return android.os.Build.VERSION.SDK_INT;
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


