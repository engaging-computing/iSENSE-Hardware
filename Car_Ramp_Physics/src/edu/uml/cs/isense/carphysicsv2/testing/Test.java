package edu.uml.cs.isense.carphysicsv2.testing;

import com.robotium.solo.Solo;

import edu.uml.cs.isense.carphysicsv2.CarRampPhysicsV2;
import android.test.ActivityInstrumentationTestCase2;

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
	 public void testThis() throws Exception {
		 
		 //click home button on action bar 7 times (this puts app in and out of dev mode)
		 for(int i = 0; i < 7; i++) {
			 solo.clickOnActionBarHomeButton();
		 }
		 
		 //push button 0
		 solo.clickOnButton(0);
		 
		 //click on button that says this button
		 solo.clickOnButton("This Button");
		 
		 //click where text says click
		 solo.clickOnText("Click");
	    
}

	

}
