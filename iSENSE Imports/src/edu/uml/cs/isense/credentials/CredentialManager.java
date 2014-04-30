package edu.uml.cs.isense.credentials;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * The credential manager *blah blah blah* TODO
 * 
 * @author Bobby
 * 
 */
public class CredentialManager extends Activity implements LoginWrapper,
		PersonWrapper {
	FragmentManager fragmentManager;
	FragmentTransaction fragmentTransaction;

	private static API api;

	private static boolean loggedIn = false;

	/* This is the key used to sent an error message to LoginError. */
	public static final String INTENT_KEY_MESSAGE = "MESSAGE";

	/* This is what the code returns when login fails. */
	public static final int RESULT_ERROR = 1;

	public static final String DEFAULT_USERNAME = "mobile.fake@example.com";
	public static final String DEFAULT_PASSWORD = "mobile";

	private Context baseContext;

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
		fragmentManager = getFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();

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

		fragmentManager = getFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();

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
		fragmentManager = getFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();

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

		Login.setCredentials(baseContext, "", "");

		loggedOutView();
	}

	/**
	 * Calls LoginWithNewCredentialsTask. This is used when logging in with the
	 * wrapper fragment.
	 * 
	 * @param username
	 * @param password
	 */
	public void wrapperLogin(String username, String password) {
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

				Login.setCredentials(baseContext, "", "");

				loggedIn = false;

			} else { // Successfully logged in

				Login.setCredentials(baseContext,
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
	 */
	public static void Login(Context appContext, API appAPI) {
		api = appAPI;

		if (Connection.hasConnectivity(appContext)) {
			new LoginWithSavedCredentialsTask().execute(appContext);
		}
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

		Login.setCredentials(appContext, username, password);

		new LoginWithSavedCredentialsTask().execute(appContext);
	}

	/**
	 * This Task attempts to login to iSENSE with saved credentials.
	 * 
	 * @author Bobby
	 * 
	 */
	private static class LoginWithSavedCredentialsTask extends
			AsyncTask<Context, Void, Void> {

		@Override
		protected Void doInBackground(Context... appContext) {

			String username = Login.getUsername(appContext[0]);
			String password = Login.getPassword(appContext[0]);

			if (api != null) {
				person = api.createSession(username, password);

				// Update logged in status
				loggedIn = (person != null);

			} else {
				Log.e("CredentialManager.java",
						"api passed in to CredentialManager.Login() was null");
			}

			return null;
		}

		@Override
		protected void onCancelled(Void result) {
			super.onCancelled(result);
			return;
		}
	}
}
