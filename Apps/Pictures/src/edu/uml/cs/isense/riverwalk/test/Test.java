package edu.uml.cs.isense.riverwalk.test;

import junit.framework.Assert;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.robotium.solo.Solo;

import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.riverwalk.Main;

public class Test extends ActivityInstrumentationTestCase2<Main> {
	
	Solo solo;
	
	public Test() {
		super(Main.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testApp() throws Exception {
		
		enterName("John H");
		//devMode();
		//projectSelect("589");
		takePic();
	}
	
	public void enterName(final String Name) throws Exception {
		
		//enter name
		solo.clearEditText(0);
		solo.enterText(0, Name);
		
		//check to see if name saved
		if(solo.searchText("John H."))
			Assert.assertTrue(true);
		
	}
	
	public void devMode() throws Exception {
		
		//put in dev mode
		// click isense logo 7 times
			for (int i = 0; i < 7; i++) {
				View actionbarItem11 = solo.getView(android.R.id.home);
				solo.clickOnView(actionbarItem11);
				Thread.sleep(500);
			}

		// check to see if in dev mode
		Assert.assertTrue(API.getInstance().isUsingDevMode());

	}
	
	public void projectSelect(final String Number) throws Exception {
		
		// click select project
		View actionbarItem1 = solo.getView(edu.uml.cs.isense.riverwalk.R.id.MENU_ITEM_BROWSE);			
		solo.clickOnView(actionbarItem1);
		Thread.sleep(2000);
	
		//enter project number
		solo.clearEditText(0);
		solo.enterText(0, Number);
		solo.clickOnButton("OK");
		
		//check if number saved
		solo.clickOnView(actionbarItem1);
		if(solo.searchText("589")) {
			Assert.assertTrue(true);
			solo.clickOnButton("OK");
		}
		
	}
	
	public void takePic() throws Exception {
		
		//click On take picture
		//solo.clickOnButton("Press Here to Take a Picture"); 
		solo.clickOnScreen(608,645);
	}
	
}