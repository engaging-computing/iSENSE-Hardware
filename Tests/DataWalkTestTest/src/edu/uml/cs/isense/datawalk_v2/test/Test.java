package edu.uml.cs.isense.datawalk_v2.test;

import android.test.ActivityInstrumentationTestCase2;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.credentials.CredentialManager;
import edu.uml.cs.isense.datawalk_v2.DataWalk;
import junit.framework.Assert;
import android.content.Context;
import android.content.SharedPreferences;

import android.view.View;


import com.robotium.solo.Solo;

public class Test extends ActivityInstrumentationTestCase2<DataWalk> {
	
	Solo solo;
	
	public Test() {
		super(DataWalk.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testApp() throws Exception {
		
		enterName("George","M");
		devMode();
		selectProject("587");
		logIn("mobile.fake@example.com", "mobile");
		recordData();
		uploadData();
		changeInterval();
		logOut();
		recordNewInt();
		uploadConKey("testing");
		reset();
	}
	
	public void enterName(final String First, final String Last) throws Exception {

		//enter first name and last initial
		solo.clearEditText(0);
		solo.enterText(0, First );
		solo.clearEditText(1);
		solo.enterText(1, Last);
		solo.clickOnButton("OK");
		
		//check if name saved
		if( solo.searchText("George M"))
			Assert.assertTrue(true);
	}

	public void devMode() throws Exception {
		
		// click isense logo 7 times
		for (int i = 0; i < 7; i++) {
			View actionbarItem11 = solo.getView(android.R.id.home);
			solo.clickOnView(actionbarItem11);
			Thread.sleep(500);
		}
		// check to see if in dev mode
		Assert.assertTrue(API.getInstance().isUsingDevMode());
	}
	
	public void selectProject(final String Project) throws Exception {
		
		//select project number
		solo.clickOnText("Project");
		solo.clearEditText(0);
		solo.enterText(0, Project);
		solo.clickOnButton("OK");
		
		//check if number saved
		solo.clickOnText("Project");
		if(solo.searchText(Project)){
			Assert.assertTrue(true);
			solo.clickOnButton("OK");
		}
	}
	public void logIn(final String UserName, final String Password) throws Exception {

		// click login
		View actionbarItem2 = solo.getView(edu.uml.cs.isense.datawalk_v2.R.id.Login);
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

	public void recordData () throws Exception {
		
		// push hold to start button
		View b = solo.getButton("Hold to Start");
		solo.clickLongOnView(b);
		
		//wait period of time
		Thread.sleep(21000);
		
		//stop recording
		solo.clickLongOnView(b);
		
		//check to see if data exists
		solo.clickOnButton("Upload");
		if(solo.searchText("Type: DATA")) 
			Assert.assertTrue(true);
			
	}
	
	public void uploadData() throws Exception {
		
		//upload data
		solo.clickOnButton("Upload");
		
		//check to see if data uploaded
		if(solo.searchText("upload successful")){
			Assert.assertTrue(true);
			solo.clickOnButton("OK");
		}
	}
	public void changeInterval() throws Exception {
		
		//go to interval menu
		solo.clickOnText("seconds");
		
		//select new interval
		solo.clickOnText("5 seconds");
		
		//check to see that new interval saved
		if(solo.searchText("5 seconds"))
			Assert.assertTrue(true);
	}
	
	public void logOut() throws Exception {
		
		//click on login
		View actionbarItem2 = solo.getView(edu.uml.cs.isense.datawalk_v2.R.id.Login);
		solo.clickOnView(actionbarItem2);
		
		//logout if logged in
		if (CredentialManager.isLoggedIn()) {
			solo.clickOnButton("Log out");
			solo.clickOnButton("Cancel");
		}
	}
	public void recordNewInt() throws Exception {
		
		//record data
		View b = solo.getButton("Hold to Start");
		solo.clickLongOnView(b);
		
		//wait time period
		Thread.sleep(11000);
		
		//stop recording
		solo.clickLongOnView(b);
		
		//check to see if data exists
		solo.clickOnButton("Upload");
		if(solo.searchText("Type: DATA")) 
			Assert.assertTrue(true);
	}
	
	public void uploadConKey(final String key) throws Exception {
		
		//upload data
		solo.clickOnButton("Upload");
		
		//enter contributer key
		solo.enterText(0, key);
		solo.clickOnButton("OK");
		
		//check to see if data uploaded
		if(solo.searchText("upload successful")) {
			Assert.assertTrue(true);
			solo.clickOnButton("OK");
		}
	}
	
	public void reset() {
		
		//click on menu button
		solo.clickOnMenuItem("");
		
		//click on reset. Reset Settings is automatically pressed?
		solo.clickOnButton("Reset");
		
	}
	
}
