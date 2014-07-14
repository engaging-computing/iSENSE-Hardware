package edu.uml.cs.isense.canobiev2.test; 

import com.robotium.solo.Solo;

import edu.uml.cs.isense.datawalk_v2.DataWalk;

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
	
}
