package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
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

    
	 /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.credential_manager);
    		api = API.getInstance();
    		baseContext = getBaseContext();
    		
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

	    fragmentTransaction.add(R.id.fragment_first, fragmentLogin);
	    fragmentTransaction.add(R.id.fragment_second, fragmentKeys);
	    
	    fragmentTransaction.commit();

	}
	
	/*call this when user logs in*/
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void LoggedInView() {
		fragmentManager = getFragmentManager();
	    fragmentTransaction = fragmentManager.beginTransaction();
	    
	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_CLOSE);
	    fragmentTransaction.remove(fragmentLogin);
	    fragmentTransaction.remove(fragmentKeys);
	    
	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_OPEN);
	    fragmentTransaction.add(R.id.fragment_first, fragmentPerson);
	    
	    fragmentTransaction.addToBackStack(null);
	    
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


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onBackPressed() {
		
		fragmentManager = getFragmentManager();
	    fragmentTransaction = fragmentManager.beginTransaction();
	    
	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_CLOSE);
	    fragmentTransaction.remove(fragmentLogin);
	    fragmentTransaction.remove(fragmentKeys);
	    fragmentTransaction.commit();
	    	
		Log.e("in credential manager", "back pressed");
		finish();
		}
	
	
	/* If usercalls this function with null arguments, open the login dialog 
	 * (ret true on success, false on failure)
	 */
	public boolean login(String username, String password) {
		return false;
		
	}
	
	

	
	public RPerson getUserInformation() {
		return null;
		
	}
	public boolean isLoggedIn() {
		return false;
		
	}
	public void logout() {
		
	}
	
	/*if user passes null,call key creation dialog */
	public boolean addContributorKey(String key) {
		return false;
		
	}
	
	public void removeContributorKey(String key){
		
	}

	private boolean saveCredentials(String username, String password) {
		return false;
		
	}
	
	private String getUsername() {
		return "";
		
	}
	
	private String getPassword() {
		return "";
		
	}
	
	
	
	
	public void LoginWithNewInfo() {
		if (Connection.hasConnectivity(this)) {
			new LoginWithNewInfoTask().execute();
		}
	}
	
	/**
	 * This class attempts to login to iSENSE and writes user info to
	 * preferences if it is successful. Otherwise, it calls LoginError.
	 * 
	 */
	private class LoginWithNewInfoTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			// Login call (passes success to onPostExecute)
			person = api.createSession(CredentialManagerLogin.getUsername(),
					CredentialManagerLogin.getPassword());
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
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

			// return success
			//setResult(RESULT_OK);
			//finish();
			//LoggedInView();
		}

	}
	
	
//	/**
//	 * This class attempts to login to iSENSE and writes user info to
//	 * preferences if it is successful. Otherwise, it calls LoginError.
//	 * 
//	 */
//	/* attempt to login with stored username and password */
//	public void attemptLoginWithSavedInfo() {
//		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
//				this, getSharedPreferences(
//						PREFERENCES_KEY_OBSCURRED_USER_INFO,
//						Context.MODE_PRIVATE));
//
//		if (mPrefs.getString(
//				PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "")
//				.equals("")
//				&& mPrefs.getString(
//						PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
//						"").equals("")) {
//			return;
//		}
//
//		if (Connection.hasConnectivity(this)) {
//			new LoginWithSavedInfoTask().execute();
//
//		}
//	}
//	
//	// Attempts to login with current user information
//	private class LoginWithSavedInfoTask extends AsyncTask<Void, Void, Boolean> {
//
//			@Override
//			protected Boolean doInBackground(Void... params) {
//				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
//						baseContext, getSharedPreferences(
//								PREFERENCES_KEY_OBSCURRED_USER_INFO,
//								Context.MODE_PRIVATE));
//	
//				person = api
//						.createSession(
//								mPrefs.getString(
//										PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
//										""),
//								mPrefs.getString(
//										PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
//										""));
//				
//				return null;
//			}
//
//		}
	
	
}

