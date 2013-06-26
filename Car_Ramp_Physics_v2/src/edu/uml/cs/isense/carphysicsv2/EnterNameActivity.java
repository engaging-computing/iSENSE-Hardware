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
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.waffle.Waffle;

public class EnterNameActivity extends Activity {
	
	private Context mContext;
	
	static final public int NAME_SUCCESSFUL = 1;
	static final public int NAME_FAILED = 0;
	static final public int NAME_CANCELED = -1;
	
	static boolean dontToastMeTwice  = false;
	boolean success;

	Waffle w;
	
	private static final String blankFields = "Do not leave any fields blank.  Please enter your first name and last initial.";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entername);
		
		mContext = this;
		
		w = new Waffle(mContext);
		
		final EditText firstNameInput   = (EditText) findViewById(R.id.nameInput);
		final EditText lastInitialInput = (EditText) findViewById(R.id.initialInput);
		final Button   okButton         = (Button)   findViewById(R.id.OkButton);
		
		firstNameInput.setFilters( new InputFilter[] { new InputFilter.LengthFilter(20)});
		
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
					   setResult(RESULT_OK, null);
					   finish();
				   }
			}
		
		});
		
		displayEula();
	}
	
	@Override
	public void onBackPressed() {
		if (!dontToastMeTwice) {
			if (CarRampPhysicsV2.running)
				w.make(

				"Cannot exit via BACK while recording data; use HOME instead.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
			else if (CarRampPhysicsV2.inApp) {
				setResult(RESULT_CANCELED);
				finish();
			}
			else
				w.make("Press back again to exit.", Waffle.LENGTH_SHORT);
			new NoToastTwiceTask().execute();
		} else if (CarRampPhysicsV2.exitAppViaBack && !CarRampPhysicsV2.running) {
			CarRampPhysicsV2.setupDone = false;
			setResult(RESULT_CANCELED);
			finish();
		}
	}
    
	private void showFailure() {
		if(!dontToastMeTwice) {
			w.make(blankFields, Waffle.LENGTH_LONG, Waffle.IMAGE_X);		
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
	    	CarRampPhysicsV2.exitAppViaBack = true ;
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


