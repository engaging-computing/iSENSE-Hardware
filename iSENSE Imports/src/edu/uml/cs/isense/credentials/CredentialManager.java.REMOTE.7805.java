package edu.uml.cs.isense.credentials;

import java.io.IOException;
import java.net.URL;

import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.waffle.Waffle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

/**
 *
 * 
 * @author iSENSE Android Development Team
 */

public class CredentialManager extends Activity {
	FragmentManager fragmentManager;
	FragmentTransaction fragmentTransaction;
	
	private API api;
	
	private boolean loggedin = false;
	
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

	public static final String DEFAULT_USERNAME = "mobile.fake@example.com";
	public static final String DEFAULT_PASSWORD = "mobile";

	/* This is the code reserved to identify the return of LoginError */
	private static final int ACTIVITY_LOGIN_ERROR = 1;
	
	private String message = "";
	private Context baseContext;

	/* Fragments on screen */
	CredentialManagerLogin fragmentLogin = new CredentialManagerLogin();
    CredentialManagerKeys fragmentKeys = new CredentialManagerKeys();
    CredentialManagerPerson fragmentPerson = new CredentialManagerPerson();
    
    /* Transition codes for fragments*/
    int TRANSIT_FRAGMENT_OPEN = 4097;
    int TRANSIT_FRAGMENT_CLOSE = 8194;
    int TRANSIT_FRAGMENT_FADE = 4099;
	
    /* person object we get back after we login*/
	public static RPerson person;

	private Waffle w;
    
	 /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.credential_manager);
    		api = API.getInstance();
    		baseContext = getBaseContext();
    		
    		w = new Waffle(baseContext);

    		
    		/*
    		 * This fetches the last successful username and password from
    		 * preferences.
    		 */
    		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
    				baseContext, baseContext.getSharedPreferences(
    						PREFERENCES_KEY_OBSCURRED_USER_INFO, MODE_PRIVATE));
    		

            LoggedOutView();
            
    		
    }
	
	
	/*
	 * This is the standard view when user is logged out
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void LoggedOutView() {
		fragmentManager = getFragmentManager();
	    fragmentTransaction = fragmentManager.beginTransaction();
	    
	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_CLOSE);
	    fragmentTransaction.remove(fragmentPerson);
	    
	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_OPEN);
	    Log.e("Credential Manager ", "loggedOutView();");
	    fragmentTransaction.add(R.id.fragmentcontainer, fragmentLogin);
	    fragmentTransaction.add(R.id.fragmentcontainer2, fragmentKeys);
	    
	    fragmentTransaction.commit();

	}
	
	/*call this when user logs in*/
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void LoggedInView() {
		fragmentManager = getFragmentManager();
	    fragmentTransaction = fragmentManager.beginTransaction();
	    
	    fragmentTransaction.remove(fragmentLogin);
	    fragmentTransaction.remove(fragmentKeys);
	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_CLOSE);

	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_OPEN);
	    fragmentTransaction.add(R.id.fragmentcontainer, fragmentPerson);
	    
	    fragmentTransaction.commit();

	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	/*call this when user is logged out to make the keys fragment take up the whole screen */
	private void KeysOnlyView() {
		fragmentManager = getFragmentManager();
	    fragmentTransaction = fragmentManager.beginTransaction();
	    
	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_CLOSE);
	    fragmentTransaction.remove(fragmentLogin);

	    fragmentTransaction.commit();

	}
	
	public RPerson getUserInformation() {
		return person;
	}
	
	public boolean isLoggedIn() {
		return loggedin;
		
	}
	public void logout() {
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				baseContext, baseContext.getSharedPreferences(
						PREFERENCES_KEY_OBSCURRED_USER_INFO,
						MODE_PRIVATE));
		mPrefs.edit()
		.putString(
				PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
				"").commit();
		mPrefs.edit()
		.putString(
				PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
				"").commit();
		
		loggedin = false;
		LoggedOutView();
	}
	
	public boolean addContributorKey(String key) {
		if (key == null) {
			//TODO call key creation dialog
		}
		
		return false;
		
	}
	
	public void removeContributorKey(String key){
		
	}
	
	public String getUsername() {
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				baseContext, getSharedPreferences(
						PREFERENCES_KEY_OBSCURRED_USER_INFO,
						Context.MODE_PRIVATE));
		
		return mPrefs.getString(PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "");	
	}
	
	public String getPassword() {
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				baseContext, getSharedPreferences(
						PREFERENCES_KEY_OBSCURRED_USER_INFO,
						Context.MODE_PRIVATE));
		
		return mPrefs.getString( PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, "");
		
	}
	
	
	
	/* Calls LoginTask and passes in a user name and password
	 * if the user name and password are both null then it will try and login with
	 * the saved credentials from preferences. CredentialManager.Login(null,null) should
	 * be called in every apps oncreate method to login to the API with saved credentials.*/
	public void Login(String username, String password) {
		if (Connection.hasConnectivity(this)) {
			new LoginTask().execute(username, password);
		}
	}
	
	/**
	 * This class attempts to login to iSENSE and writes user info to
	 * preferences if it is successful. Otherwise, it calls LoginError.
	 * 
	 */
	private class LoginTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... userInfo) {
			String username = userInfo[0];
			String password = userInfo[1];
			
			/* if null username and password get info from preferences instead */
			if (username == null && password == null) {
				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						baseContext, getSharedPreferences(
								PREFERENCES_KEY_OBSCURRED_USER_INFO,
								Context.MODE_PRIVATE));
				
				username = mPrefs.getString(PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "");
				password = mPrefs.getString( PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, "");
				
				/* if no username or password was stored return and do not try to login*/
				if (username.equals("") && password.equals("")) {
					loggedin = false;
					return null;
				}
				
			}
			
			person = api.createSession(username, password);
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			/*if successfully logged in save credentials to preferences else display waffle*/
			if (person == null) {
				
				w.make("Invalid Email or Password",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
				
				/*username or password failed, make sure credentials are empty*/
				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						baseContext, baseContext.getSharedPreferences(
								PREFERENCES_KEY_OBSCURRED_USER_INFO,
								MODE_PRIVATE));
				mPrefs.edit()
				.putString(
						PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
						"").commit();
				mPrefs.edit()
				.putString(
						PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
						"").commit();
				loggedin = false;
				
				} else {
					
				/* Saved the user's credentials. */
				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						baseContext, baseContext.getSharedPreferences(
								PREFERENCES_KEY_OBSCURRED_USER_INFO,
								MODE_PRIVATE));
				mPrefs.edit()
				.putString(
						PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
						CredentialManagerLogin.getUsername()).commit();
				mPrefs.edit()
				.putString(
						PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
						CredentialManagerLogin.getPassword()).commit();
				
				loggedin = true;
				LoggedInView();
			}
		}

	}
	
	
}

