package edu.uml.cs.isense.credentials;

import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * The credential manager encapsulating activity.
 * 
 * @author Bobby
 */
public class CredentialManager extends FragmentActivity implements LoginWrapper,
		PersonWrapper {
	android.support.v4.app.FragmentTransaction fragmentTransaction;

	private static API api;

	private static boolean loggedIn = false;

	/* This is the key used to sent an error message to LoginError. */
	public static final String INTENT_KEY_MESSAGE = "MESSAGE";

	/* This is what the code returns when login fails. */
	public static final int RESULT_ERROR = 1;

	private Context baseContext;
	
	/* These are the keys for obtain the user credential preferences. */
	private static final String KEY_USER_INFO = "OBSCURRED_USER_INFO";
	private static final String SUBKEY_USERNAME = "USERNAME";
	private static final String SUBKEY_PASSWORD = "PASSWORD";

	/* Fragments on screen */
	CredentialManagerLogin fragmentLogin = new CredentialManagerLogin();
	CredentialManagerPerson fragmentPerson = new CredentialManagerPerson();

	/* Transition codes for fragments */
	int TRANSIT_FRAGMENT_OPEN = 4097;
	int TRANSIT_FRAGMENT_CLOSE = 8194;
	int TRANSIT_FRAGMENT_FADE = 4099;

	/* onActivityResult codes */
	final int NEW_KEY_REQUESTED = 101;

	/* Person object we get back after we login. */
	public static RPerson person;

	private static Waffle w;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.credential_manager);
		api = API.getInstance();
		baseContext = getBaseContext();

		w = new Waffle(baseContext);
		
		if (loggedIn) {
			loggedInView();
		} else {
			loggedOutView();
		}
	}

	/**
	 * The default view when the user is logged out.
	 */
	private void loggedOutView() {
		fragmentTransaction = getSupportFragmentManager().beginTransaction();

		fragmentLogin = new CredentialManagerLogin();
		fragmentTransaction.replace(R.id.fragmentcontainer, fragmentLogin);

		fragmentTransaction.commit();
	}

	/**
	 * The view when the user is logged in.
	 */
	private void loggedInView() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

		fragmentTransaction = getSupportFragmentManager().beginTransaction();


		fragmentPerson = new CredentialManagerPerson();
		fragmentTransaction.replace(R.id.fragmentcontainer, fragmentPerson);

		fragmentTransaction.commit();
	}

	/**
	 * The view when the user is logged out. The keys fragment take up the whole
	 * screen.
	 */
	@SuppressWarnings("unused")
	private void keysOnlyView() {
		fragmentTransaction = getSupportFragmentManager().beginTransaction();

		fragmentTransaction.setTransition(TRANSIT_FRAGMENT_CLOSE);
		fragmentTransaction.remove(fragmentLogin);

		fragmentTransaction.commit();
	}

	/**
	 * Retrieves the user information if a user is currently logged in.
	 * 
	 * @return RPerson or null
	 */
	public RPerson getUserInformation() {
		return person;
	}

	/**
	 * Returns the current login status.
	 * 
	 * @return status
	 */
	public static boolean isLoggedIn() {
		return loggedIn;
	}

	/**
	 * Logs the user out and swaps the visible fragment.
	 */
	public void logout() {
		loggedIn = false;
		setCredentials(baseContext, "", "");
		api.deleteSession();
		loggedOutView();
	}

	/**
	 * Calls LoginWithNewCredentialsTask. This is used when logging in with the
	 * wrapper fragment.
	 * 
	 * @param username
	 * @param password
	 */
	public void login(String username, String password) {
		new LoginWithNewCredentialsTask().execute(username, password);
	}

	/**
	 * This Task attempts to login to iSENSE and writes user info to preferences
	 * if it is successful. Otherwise, it calls LoginError. It displays a waffle
	 * if it fails and changes the layout to the logged in layout if it
	 * succeeds.
	 * 
	 * @author Bobby Donald
	 */
	private class LoginWithNewCredentialsTask extends
			AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... userInfo) {
			String username = userInfo[0];
			String password = userInfo[1];

			person = api.createSession(username, password);
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {

			if (person == null) { // Failed to log in
				w.make("Invalid Email or Password", Waffle.LENGTH_LONG,
						Waffle.IMAGE_X);

				setCredentials(baseContext, "", "");

				loggedIn = false;

			} else { // Successfully logged in

				setCredentials(baseContext,
						CredentialManagerLogin.getUsername(),
						CredentialManagerLogin.getPassword());

				loggedIn = true;
				loggedInView();
			}
		}
	}

	/**
	 * This method logs the app into iSENSE with presaved credentials.
	 * 
	 * @param appContext
	 *            Context of the calling application.
	 * @param appAPI
	 *            API object that belongs to the caller.
	 */
	public static void login(Context appContext, API appAPI) {
		api = appAPI;

		if (Connection.hasConnectivity(appContext)) {
			new LoginWithSavedCredentialsTask().execute(appContext);
		}
	}
	
	/**
	 * This method logs out the app.
	 * 
	 * @param appContext
	 *            Context of the calling application.
	 * @param appAPI
	 *            API object that belongs to the caller.
	 */
	public static void logout(Context appContext, API appAPI) {
		loggedIn = false;
		appAPI.deleteSession();
		setCredentials(appContext, "", "");
	}

	/**
	 * Sets username and password in saved preferences and then calls the async
	 * task LoginWithSavedCredentialsTask to attempt and login. This would be
	 * used if an application wanted to log in without using the Credential
	 * Manager Login fragment.
	 * 
	 * @param username
	 * @param password
	 * @param appContext
	 * @param appAPI
	 */
	public static void setUsernameAndPassword(String username, String password,
			Context appContext, API appAPI) {

		api = appAPI;

		setCredentials(appContext, username, password);

		new LoginWithSavedCredentialsTask().execute(appContext);
	}

	/**
	 * This Task attempts to login to iSENSE with saved credentials.
	 * 
	 * @author Bobby
	 */
	private static class LoginWithSavedCredentialsTask extends
			AsyncTask<Context, Void, Void> {

		@Override
		protected Void doInBackground(Context... appContext) {

			String username = getUsername(appContext[0]);
			String password = getPassword(appContext[0]);
			
			Log.e("username:", username);
			
			if (api != null && api.getCurrentUser() == null && !username.isEmpty()) {
				person = api.createSession(username, password);
				
				// Update logged in status
				loggedIn = (person != null);

			} 

			return null;
		}

		
	}

	/**
	 * Saves the username into SharedPreferences.
	 * 
	 * @param appContext
	 * @param username
	 */
	private static void setUsername(Context appContext, String username) {

		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				appContext, appContext.getSharedPreferences(KEY_USER_INFO,
						MODE_PRIVATE));
		mPrefs.edit().putString(SUBKEY_USERNAME, username).commit();
	}

	/**
	 * Saves the password into SharedPreferences.
	 * 
	 * @param appContext
	 * @param password
	 */
	private static void setPassword(Context appContext, String password) {

		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				appContext, appContext.getSharedPreferences(KEY_USER_INFO,
						MODE_PRIVATE));
		mPrefs.edit().putString(SUBKEY_PASSWORD, password).commit();
	}

	/**
	 * Saves username and password into SharedPreferences.
	 * 
	 * @param appContext
	 * @param username
	 * @param password
	 */
	public static void setCredentials(Context appContext, String username,
			String password) {
		setUsername(appContext, username);
		setPassword(appContext, password);
	}

	/**
	 * Retrieves the username of the current logged in user.
	 * 
	 * @param appContext
	 *            The context of the calling activity.
	 * @return username
	 */
	public static String getUsername(Context appContext) {
		
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				appContext, appContext.getSharedPreferences(KEY_USER_INFO,
						Context.MODE_PRIVATE));

		return mPrefs.getString(SUBKEY_USERNAME, "");
	}

	/**
	 * Retrieves the password of the current logged in user.
	 * 
	 * @param appContext
	 *            The context of the calling activity.
	 * @return password
	 */
	public static String getPassword(Context appContext) {

		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				appContext, appContext.getSharedPreferences(KEY_USER_INFO,
						Context.MODE_PRIVATE));

		return mPrefs.getString(SUBKEY_PASSWORD, "");
	}
}
