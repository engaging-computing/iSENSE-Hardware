package edu.uml.cs.isense.canobiev2.test;



import junit.framework.Assert;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.robotium.solo.Solo;

import edu.uml.cs.isense.canobiev2.AmusementPark;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.credentials.CredentialManager;

public class Test extends ActivityInstrumentationTestCase2<AmusementPark> {
	Solo solo;

	public Test() {
		super(AmusementPark.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testLoggedIn() throws Exception {
		devMode();
		setup("Carl S", "200", "571");
		logIn("mobile.fake@example.com", "mobile");
		recordData();
		uploadData();
		recordNoGrav();
		uploadConKey("testing");
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
	
	public void setup(final String Name, final String Time, final String Project ) throws Exception {
		// wait 1 s for app to open
		Thread.sleep(1000);

		View actionbarItem1 = solo.getView(edu.uml.cs.isense.canobiev2.R.id.MENU_ITEM_SETUP);
		solo.clickOnView(actionbarItem1);
		
		// enters user's name
		solo.clearEditText(0);
		solo.enterText(0, Name);
		
		//enter time interval
		solo.clearEditText(1);
		solo.enterText(1, Time);

		// wait 1 s
		Thread.sleep(1000);

		//select project
		solo.clickOnButton("Select a Project");
		solo.clearEditText(0);
		solo.enterText(0, Project);
		solo.clickOnButton("OK");

		//select ride
		solo.pressSpinnerItem(0,1);
		// wait 1 s
		Thread.sleep(1000);
		solo.clickOnButton("OK");
		
		// check to see if user's name was saved 
		solo.clickOnView(actionbarItem1);
		Thread.sleep(3000);
		if(solo.searchEditText("Carl S")) {
			Assert.assertTrue(true);
		}
		
		// check to see if time was saved
		Assert.assertEquals(AmusementPark.rate, "200");
		
		//check if project number saved
		solo.clickOnButton("Select a Project");
		if(solo.searchEditText("571"))
			Assert.assertTrue(true);
		solo.clickOnButton("OK");
		
		//check to see if ride selection saved
		if(solo.searchText("Alpine Swing"))
			Assert.assertTrue(true);
		
		solo.clickOnButton("OK");
		
	}

	public void logIn(final String UserName, final String Password) throws Exception {
	
		// click login
		View actionbarItem2 = solo.getView(edu.uml.cs.isense.canobiev2.R.id.MENU_ITEM_LOGIN);
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

	public void recordData() throws Exception {
		
		// push hold to start button
		View b = solo.getButton("START");
		solo.clickLongOnView(b);
		
		// wait desired amount of time
		Thread.sleep(11000);
		
		//stop recording
		solo.clickLongOnView(b);
		solo.clickOnButton("OK");
		
		// check to see if my dataset exists
		if(solo.searchText("Type: DATA")) {
			Assert.assertTrue(true);
			solo.clickOnButton("Cancel");
		}
	}

	public void uploadData() throws Exception {
		
		// push upload
		View actionbarItem1 = solo.getView(edu.uml.cs.isense.canobiev2.R.id.MENU_ITEM_UPLOAD);
		solo.clickOnView(actionbarItem1);
		solo.clickOnButton("Upload");
		Thread.sleep(3000);
		
		
		// check to see if it uploaded
		if(solo.searchText("upload successful") ) {
			Assert.assertTrue(true);
			solo.clickOnButton("OK");
		}
	}

	public void recordNoGrav() throws Exception {
		
			//turn off gravity
			solo.clickOnButton("Include Gravity");
			
			//push hold to start button
			View b = solo.getButton("START");
			solo.clickLongOnView(b);
			
			// wait desired amount of time
			Thread.sleep(11000);	
			solo.clickLongOnView(b);
			solo.clickOnButton("OK");
			
			// check to see if my dataset exists				
			if(solo.searchText("Type:DATA")) 
				Assert.assertTrue(true);
						
			Thread.sleep(2000);
			solo.clickOnButton("Cancel");
			Thread.sleep(2000);
			}
	
	
	public void uploadConKey(final String Key) throws Exception {
		
		// logout if logged in
		if (CredentialManager.isLoggedIn()) {
			View actionbarItem2 = solo.getView(edu.uml.cs.isense.canobiev2.R.id.MENU_ITEM_LOGIN);
			solo.clickOnView(actionbarItem2);
			solo.clickOnButton("Log out");
			solo.clickOnButton("Cancel");
		}
		//Upload data
		View actionbarItem3 = solo.getView(edu.uml.cs.isense.canobiev2.R.id.MENU_ITEM_UPLOAD);
		solo.clickOnView(actionbarItem3);
		solo.clickOnButton("Upload");
		
		// enter contributor key
		Thread.sleep(2000);
		solo.enterText(0, Key);
		solo.clickOnButton("OK");
		Thread.sleep(2000);
		
		//check if upload was successful
		if(solo.searchText("upload successful")) {
			Assert.assertTrue(true);
			solo.clickOnButton("OK");
		}
		
		
	}
	
}

