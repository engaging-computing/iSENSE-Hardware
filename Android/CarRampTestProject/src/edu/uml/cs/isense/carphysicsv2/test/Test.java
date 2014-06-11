package edu.uml.cs.isense.carphysicsv2.test;

import android.annotation.SuppressLint;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Button;
import com.robotium.solo.Solo;
import edu.uml.cs.isense.carphysicsv2.CarRampPhysicsV2;

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
	
	
	//all tests must begin with the word test
	 @SuppressLint("InlinedApi")
	public void testThis() throws Exception {
		 
		 Thread.sleep(1000);
		 
		 solo.enterText(0,"Carl");
		 solo.enterText(1,"S");
		 
		 Thread.sleep(1000);
		 
		//push button OK
		 solo.clickOnButton("OK");
		 
		 Thread.sleep(3000);
		 
		//click home button on action bar 7 times (this puts app in and out of dev mode)
		 for(int i = 0; i < 7; i++) {
			 View actionbarItem11 = solo.getView(android.R.id.home);
			 solo.clickOnView(actionbarItem11);
			 Thread.sleep(500);
		 }
		 
		 View actionbarItem1 = solo.getView(edu.uml.cs.isense.carphysicsv2.R.id.project_select);
		 solo.clickOnView(actionbarItem1);
		 
		 //enters project number
		 solo.enterText(0,"419");
		 
		 solo.clickOnButton("OK");
		 
		 
		 
		 //starts timer
		 Button b = solo.getButton("Hold to Start");
		 solo.clickLongOnView(b);
		 
		 Thread.sleep(11000);
		 
		 //solo.clickOnButton("Upload");
		 solo.clickOnButton("Cancel");
		 
		 //enter contributer key
		// solo.enterText(0,"crp");
		 
		// solo.clickOnButton("OK");
		 
		 //opens menu
		 solo.clickOnMenuItem("");
		 
		//displays recording length intervals
		 solo.clickOnButton("Recording Length");
		 
		 
		 View radio0 = solo.getView("edu.uml.cs.isense.carphysicsv2.R.id.radio0");
		 solo.clickOnView(radio0);
		 
		 		 
		 
		 
		
		 
		
		 
		 
}

	

}
