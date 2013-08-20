package edu.uml.cs.isense.datawalk_v2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * Asks for login credentials and tries to login to iSENSE.
 * 
 * @author Rajia
 */
public class LoginIsense extends Activity {

	/* UI Handles */
	private Button ok, cancel;
	private EditText user, pass;
	
	/* Manager Variables */
	private API api;
	private Waffle w;

	/* Convenience Variables */
	private String username;
	private String password;
	private final String DEFAULT = "";

	/**
	 * Called when the activity is created. Initializes variables and sets up onClick methods.
	 */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.login_website);

		// Initials handles for our UI elements
		ok = (Button) findViewById(R.id.loginB);
		cancel = (Button) findViewById(R.id.cancelB);
		user = (EditText) findViewById(R.id.userNameET);
		pass = (EditText) findViewById(R.id.passwordET);

		// Get the username and password from preferences
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				DataWalk.mContext, DataWalk.mContext.getSharedPreferences(
						DataWalk.USER_PREFS_KEY, Context.MODE_PRIVATE));
		username = mPrefs.getString(DataWalk.USERNAME_KEY, DEFAULT);
		password = mPrefs.getString(DataWalk.PASSWORD_KEY, DEFAULT);

		// Sets the EditTexts to their initial values
		user.setText(username);
		pass.setText(password);

		// Prepare waffles and our API
		w = new Waffle(DataWalk.mContext);
		api = API.getInstance(LoginIsense.this);

		// Handles the click of the OK button
		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// Get the entered username and password
				username = user.getText().toString();
				password = pass.getText().toString();

				// Try to login
				new LoginTask().execute();

			}
		});
		
		// Handles click of the cancel button
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// This is what happens if the cancel button is clicked
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}// Ends onCreate

	/** 
	 * Logs a user into iSENSE
	 * 
	 * @author Rajia
	 */
	public class LoginTask extends AsyncTask<Void, Integer, Void> {

		// Catches return values of our API calls
		boolean connect = false;
		boolean success = false;

		/**
		 * Tries to login to iSENSE on the background thread.
		 */
		@Override
		protected Void doInBackground(Void... arg0) {
			
			// Performs the login call if you have connectivity
			if (connect = api.hasConnectivity()) {
				success = api.createSession(username, password);
			}

			return null;
		}

		
		/**
		 * Handles the return of the login call after doInBackground has finished
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (connect) {
				if (success) {
					// Tell the user of our success
					w.make("Login as  " + username + "  Successful.",
							Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);

					// Set the new username and password in preferences
					final SharedPreferences mPrefs = new ObscuredSharedPreferences(
							DataWalk.mContext,
							DataWalk.mContext.getSharedPreferences(
									DataWalk.USER_PREFS_KEY,
									Context.MODE_PRIVATE));
					SharedPreferences.Editor mEditor = mPrefs.edit();
					mEditor.putString(DataWalk.USERNAME_KEY, username);
					mEditor.putString(DataWalk.PASSWORD_KEY, password);
					mEditor.commit();

					// Set result to OK so that it can be received onActivityResult in DataWalk.java
					setResult(RESULT_OK);
					finish();
					
				} else {
					// Invalid username or password, so tell user
					w.make("Incorrect login credentials. Please try again.",
							Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
				}

			} else {
				// Tell the user we are not connected
				w.make("Cannot login due to lack of Internet connection. Please try again later.",
						Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
				
				// Set result to canceled and exit
				setResult(RESULT_CANCELED);
				finish();
			}

		}

	}

}
