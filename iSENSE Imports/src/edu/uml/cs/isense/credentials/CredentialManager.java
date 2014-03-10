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


public class CredentialManager extends Activity implements LoginWrapper, PersonWrapper {
	FragmentManager fragmentManager;
	FragmentTransaction fragmentTransaction;
	
	private static API api;
	
	private static boolean loggedin = false;
	
	/* These are the keys for obtain the user credential preferences. */
	public static final String PREFERENCES_KEY_OBSCURRED_USER_INFO = "OBSCURRED_USER_INFO";
	public static final String PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME = "USERNAME";
	public static final String PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD = "PASSWORD";

	/* This is the key used to sent an error message to LoginError. */
	public static final String INTENT_KEY_MESSAGE = "MESSAGE";

	/* This is what the code returns when login fails. */
	public static final int RESULT_ERROR = 1;

	
	public static final String DEFAULT_USERNAME = "mobile.fake@example.com";
	public static final String DEFAULT_PASSWORD = "mobile";

	private static Context baseContext;

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

	private static Waffle w;
    
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
    		
    	
    		if (loggedin == true) {
    			LoggedInView();
    		} else {
                LoggedOutView();
    		}
    		
            
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
	
    public void WrapperLogout() {
    	Logout();
    }
	
	public void Logout() {
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
	
	public static boolean addContributorKey(String key) {
		if (key == null) {
		}
		
		return false;
		
	}
	
	public void removeContributorKey(String key){
		
	}
	
	public static String getUsername(Context appContext) {
		baseContext = appContext;
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				baseContext, baseContext.getSharedPreferences(
						PREFERENCES_KEY_OBSCURRED_USER_INFO,
						Context.MODE_PRIVATE));
		
		return mPrefs.getString(PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "");	
	}
	
	public static String getPassword(Context appContext) {
		baseContext = appContext;
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				baseContext, baseContext.getSharedPreferences(
						PREFERENCES_KEY_OBSCURRED_USER_INFO,
						Context.MODE_PRIVATE));
		
		return mPrefs.getString( PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, "");
		
	}
	
	/**
	 * Calls LoginWithNewCredentialsTask. This is used when logging in with the wrapper
	 * fragment.
	 * 
	 * @param username
	 * @param password
	 */
	 public void WrapperLogin(String username, String password) {
			new LoginWithNewCredentialsTask().execute(username, password);
	 }

	
	/**
	 * This Task attempts to login to iSENSE and writes user info to
	 * preferences if it is successful. Otherwise, it calls LoginError.
	 * It displays a waffle if it fails and changes the layout to the 
	 * logged in layout if it succeeds.
	 */
	private class LoginWithNewCredentialsTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... userInfo) {
			String username = userInfo[0];
			String password = userInfo[1];
			
			person = api.createSession(username, password);
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
		
			if (person == null) { //Failed to log in
				w.make("Invalid Email or Password",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
				
				//make sure credentials are empty
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
				
				} else { //Successfully logged in
					
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
	
	
	
		/**
		 * This public static method is called from apps to log in to the
		 * api with saved credentials
		 * 
		 * @param appContext
		 */
		public static void Login(Context appContext, API appAPI) {
    		baseContext = appContext;
    		api = appAPI;

			if (Connection.hasConnectivity(baseContext)) {
				new LoginWithSavedCredentialsTask().execute();
			}
		}
		
		
		/**
		 * Sets username and password in saved preferences and then calls the 
		 * async task LoginWithSavedCredentialsTask to attempt and login.
		 * This would be used if an application wanted to log in without
		 * using the Credential Manager Login fragment.
		 * 
		 * @param username
		 * @param password
		 * @param appContext
		 * @param appAPI
		 */
		public static void SetUsernameAndPassword(String username, String password, Context appContext, API appAPI) {
    		baseContext = appContext;
    		api = appAPI;

			/* Saved the user's credentials. */
			final SharedPreferences mPrefs = new ObscuredSharedPreferences(
					baseContext, baseContext.getSharedPreferences(
							PREFERENCES_KEY_OBSCURRED_USER_INFO,
							MODE_PRIVATE));
			mPrefs.edit()
			.putString(
					PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
					username).commit();
			mPrefs.edit()
			.putString(
					PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
					password).commit();
			
			new LoginWithSavedCredentialsTask().execute();
		}
	 

		/**
		 * 		 
		 * This Task attempts to login to iSENSE with saved credentials.
		 * 
		 * @author Bobby
		 *
		 */
		private static class LoginWithSavedCredentialsTask extends AsyncTask<Void, Void, Void> {

			@Override
			protected Void doInBackground(Void... params) {
				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						baseContext, baseContext.getSharedPreferences(
								PREFERENCES_KEY_OBSCURRED_USER_INFO,
								Context.MODE_PRIVATE));
				
				String username = mPrefs.getString(PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, "");
				String password = mPrefs.getString( PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, "");
				
				
				
				person = api.createSession(username, password);
				
				if (person == null) {
					loggedin = false;
					} else {
					loggedin = true;
				}
				
				return null;
			}

			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			protected void onCancelled(Void result) {
				super.onCancelled(result);
				return;
			}
			
		}
	
	
}

