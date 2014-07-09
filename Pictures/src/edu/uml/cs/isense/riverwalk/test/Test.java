package edu.uml.cs.isense.riverwalk.test;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

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
		

	}
}