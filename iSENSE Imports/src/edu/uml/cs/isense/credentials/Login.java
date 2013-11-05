package edu.uml.cs.isense.credentials;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;

public class Login extends Activity {
	
	public static final String PREFERENCES_KEY_OBSCURRED_USER_INFO = "OBSCURRED_USER_INFO";
	public static final String PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME = "USERNAME";
	public static final String PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD = "PASSWORD";
	public static final String INTENT_KEY_MESSAGE = "MESSAGE";
	public static final int RESULT_ERROR = 1;
	
	private static final String MESSAGE_UNKNOWN_USER = "Connection to internet has been found, but the username or password was incorrect.  Please try again.";
	private static final String MESSAGE_NO_CONNECTION = "No connection to internet through either wifi or mobile found.  Please enable one to continue, then try again."; 

	private static final String DEFAULT_USERNAME = "mobile";
	private static final String DEFAULT_PASSWORD = "mobile";
	
	private static final int ACTIVITY_LOGIN_ERROR = 1;
	
	private API api;
	private String message = "";
	private Context baseContext;
	private EditText username, password;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_dialog);

		baseContext = getBaseContext();
		api = API.getInstance(baseContext);

		username = (EditText) findViewById(R.id.edittext_username);
		password = (EditText) findViewById(R.id.edittext_password);
		
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				   baseContext, baseContext.getSharedPreferences(PREFERENCES_KEY_OBSCURRED_USER_INFO, MODE_PRIVATE));
		username.setText(mPrefs.getString(PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, DEFAULT_USERNAME));
		password.setText(mPrefs.getString(PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, DEFAULT_PASSWORD));
		
		final Button ok = (Button) findViewById(R.id.button_ok);
		final Button cancel = (Button) findViewById(R.id.button_cancel);

		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new LoginTask().execute();
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}
	
	private void showFailure() {
		
		if (api.hasConnectivity()) {
			message = MESSAGE_UNKNOWN_USER;
		} else {
			message = MESSAGE_NO_CONNECTION;
		}
				
		Intent showLoginError = new Intent(baseContext, LoginError.class);
		showLoginError.putExtra(INTENT_KEY_MESSAGE, message);
		startActivityForResult(showLoginError, ACTIVITY_LOGIN_ERROR);
    	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == ACTIVITY_LOGIN_ERROR) {
			setResult(RESULT_ERROR);
			finish();
		}
		
	}
	
	private class LoginTask extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... voids) {
			Boolean success = api.createSession(username.getText().toString(), password.getText().toString());
			return success;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						baseContext, baseContext.getSharedPreferences(PREFERENCES_KEY_OBSCURRED_USER_INFO, Context.MODE_PRIVATE));
				
				   	mPrefs.edit().putString(PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, username.getText().toString()).commit();
				   	mPrefs.edit().putString(PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, password.getText().toString()).commit();
				   	
				   	setResult(RESULT_OK);
				   	finish();
			} else {
				showFailure();
			}
		}

	}

}
