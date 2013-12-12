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
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;

/**
 * This class opens a dialog message so that the user can login to iSENSE. If an
 * error occurs, it opens up a LoginError activity to display the error message.
 * 
 * @author iSENSE Android Development Team
 */
public class Login extends Activity {

	/* These are the keys for obtain the user credential preferences. */
	public static final String PREFERENCES_KEY_OBSCURRED_USER_INFO = "OBSCURRED_USER_INFO";
	public static final String PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME = "USERNAME";
	public static final String PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD = "PASSWORD";

	/* This is the key used to sent an error message to LoginError. */
	public static final String INTENT_KEY_MESSAGE = "MESSAGE";

	/* This is what the code returns when login fails. */
	public static final int RESULT_ERROR = 1;

	private static final String MESSAGE_UNKNOWN_USER = "Connection to Internet has been found, but the username or password was incorrect.  Please try again.";
	private static final String MESSAGE_NO_CONNECTION = "No connection to Internet through either WiFi or mobile found.  Please enable one to continue, then try again.";

	private static final String DEFAULT_USERNAME = "mobile";
	private static final String DEFAULT_PASSWORD = "mobile";

	/* This is the code reserved to identify the return of LoginError */
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
		api = API.getInstance();

		username = (EditText) findViewById(R.id.edittext_username);
		password = (EditText) findViewById(R.id.edittext_password);

		/*
		 * This block fetches the last successful username and password from
		 * preferences.
		 */
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				baseContext, baseContext.getSharedPreferences(
						PREFERENCES_KEY_OBSCURRED_USER_INFO, MODE_PRIVATE));
		username.setText(mPrefs.getString(
				PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
				DEFAULT_USERNAME));
		password.setText(mPrefs.getString(
				PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
				DEFAULT_PASSWORD));

		final Button ok = (Button) findViewById(R.id.button_ok);
		final Button cancel = (Button) findViewById(R.id.button_cancel);

		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/* User hit OK so attempt to login. */
				new LoginTask().execute();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/*
				 * Cancel button returns RESULT_CANCELED and doesn't save
				 * anything.
				 */
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

	/**
	 * If you failed in LoginTask set the most appropriate error message and
	 * call LoginError.
	 */
	private void showFailure() {

		if (Connection.hasConnectivity(baseContext)) {
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
			// You just returned from LoginError, so you should return error.
			setResult(RESULT_ERROR);
			finish();
		}

	}

	/**
	 * This class attempts to login to iSENSE and writes user info to
	 * preferences if it is successful. Otherwise, it calls LoginError.
	 * 
	 */
	private class LoginTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... voids) {
			// Login call (passes success to onPostExecute)
			Boolean success = api.createSession(username.getText().toString(),
					password.getText().toString());
			return success;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				/* Saved the user's credentials. */
				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						baseContext, baseContext.getSharedPreferences(
								PREFERENCES_KEY_OBSCURRED_USER_INFO,
								MODE_PRIVATE));
				mPrefs.edit()
						.putString(
								PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
								username.getText().toString()).commit();
				mPrefs.edit()
						.putString(
								PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
								password.getText().toString()).commit();

				// return success
				setResult(RESULT_OK);
				finish();
			} else {
				// show LoginError
				showFailure();
			}
		}

	}

}
