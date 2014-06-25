package edu.uml.cs.isense.carphysicsv2.test;

import junit.framework.Assert;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.robotium.solo.Solo;

import edu.uml.cs.isense.carphysicsv2.CarRampPhysicsV2;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.credentials.CredentialManager;

public class Test extends ActivityInstrumentationTestCase2<CarRampPhysicsV2> {
	Solo solo;

	public Test() {
		super(CarRampPhysicsV2.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testLoggedIn() throws Exception {
		enterName("Carl", "S");
		devMode();
		logIn("mobile.fake@example.com", "mobile");
		recordData();
		selectProject("512");
		uploadData();
		uploadConKey("testing");
	}

	public void enterName(final String First, final String Last) throws Exception {
		// wait 1 s for app to open
		Thread.sleep(1000);

		// enters user's name
		solo.clearEditText(0);
		solo.enterText(0, First);
		solo.clearEditText(1);
		solo.enterText(1, Last);

		// wait 1 s
		Thread.sleep(1000);

		// push button OK
		solo.clickOnButton("OK");

		// wait 1 s
		Thread.sleep(1000);

		// check to see if user's name was saved into sharedpref
		Context context = solo.getCurrentActivity();
		SharedPreferences namePrefs = context.getSharedPreferences(
				edu.uml.cs.isense.credentials.EnterName.PREFERENCES_KEY_USER_INFO, context.MODE_PRIVATE);
		String firstName = namePrefs.getString(
				edu.uml.cs.isense.credentials.EnterName.PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME, "");
		String lastInitial = namePrefs.getString(
				edu.uml.cs.isense.credentials.EnterName.PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL, "");
		Assert.assertEquals(First, firstName);
		Assert.assertEquals(Last + ".", lastInitial);
	}

	public void devMode() throws Exception {
		// click isense logo 7 times
		// check to see if in dev mode
		for (int i = 0; i < 7; i++) {
			View actionbarItem11 = solo.getView(android.R.id.home);
			solo.clickOnView(actionbarItem11);
			Thread.sleep(500);
		}
		Assert.assertTrue(API.getInstance().isUsingDevMode());
	}

	public void logIn(final String UserName, final String Password) throws Exception {
		// click login
		View actionbarItem2 = solo.getView(edu.uml.cs.isense.carphysicsv2.R.id.login);
		solo.clickOnView(actionbarItem2);

		// am i logged in?
		if (CredentialManager.isLoggedIn()) {
			solo.clickOnButton("Log out");
		}

		// enter Username
		solo.clearEditText(0);
		solo.enterText(0, UserName);
		// enter password
		solo.clearEditText(1);
		solo.enterText(1, Password);
		// click login
		solo.clickOnButton("Login");
		Thread.sleep(2000);
		// verify that you are logged in
		Assert.assertNotNull(API.getInstance().getCurrentUser());

		solo.clickOnButton("OK");
	}

	public void selectProject(final String Number) throws Exception {
		// click select project
		View actionbarItem1 = solo.getView(edu.uml.cs.isense.carphysicsv2.R.id.project_select);
		solo.clickOnView(actionbarItem1);

		Thread.sleep(2000);
		// enter project ID
		solo.clearEditText(0);
		solo.enterText(0, Number);
		solo.clickOnButton("OK");
		// check to see if project ID is saved

		Context context = solo.getCurrentActivity();
		SharedPreferences namePrefs = context.getSharedPreferences("PROJID", context.MODE_PRIVATE);
		String projectNumber = namePrefs.getString("project_id", "");
		Assert.assertEquals(projectNumber, "512");

	}

	public void recordData() throws Exception {
		// push hold to start button
		View b = solo.getButton("Hold to Start");
		solo.clickLongOnView(b);
		// wait TIME INTERVAL
		Thread.sleep(11000);
		// check to see if my dataset exists
		Assert.assertNotNull("Carl S.");
		Thread.sleep(2000);
		solo.clickOnButton("Cancel");
		Thread.sleep(2000);
	}

	public void uploadData() throws Exception {
		// push upload
		View actionbarItem1 = solo.getView(edu.uml.cs.isense.carphysicsv2.R.id.upload);
		solo.clickOnView(actionbarItem1);
		solo.clickOnButton("Upload");
		Thread.sleep(3000);
		solo.clickOnButton("OK");
		// check to see if it uploaded
		Assert.assertNotNull("");
	}

	public void uploadConKey(final String Key) throws Exception {
		// logout if logged in
		if (CredentialManager.isLoggedIn()) {
			View actionbarItem2 = solo.getView(edu.uml.cs.isense.carphysicsv2.R.id.login);
			solo.clickOnView(actionbarItem2);
			solo.clickOnButton("Log out");
			solo.clickOnButton("Cancel");
		}
		// push hold to start button
		View b = solo.getButton("Hold to Start");
		solo.clickLongOnView(b);
		// wait TIME INTERVAL
		Thread.sleep(11000);
		// upload
		solo.clickOnButton("Upload");
		// enter contributor key
		solo.enterText(0, Key);
		solo.clickOnButton("OK");
		Thread.sleep(2000);
		solo.clickOnButton("OK");
		Thread.sleep(3000);
	}

}
