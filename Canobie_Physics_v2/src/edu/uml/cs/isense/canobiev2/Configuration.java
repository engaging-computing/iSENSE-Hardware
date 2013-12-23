package edu.uml.cs.isense.canobiev2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class Configuration extends Activity {
	private EditText session; 
	private EditText project;
	private CheckBox projectLater;
	private EditText sampleRate;
	private EditText studentNumber;
	private CheckBox isCanobie;
	private Spinner rides;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);

		
		session = (EditText) findViewById(R.id.sessionName);
		project = (EditText) findViewById(R.id.ExperimentInput);
		projectLater = (CheckBox) findViewById(R.id.select_exp_later);
		sampleRate = (EditText) findViewById(R.id.srate);
		studentNumber = (EditText) findViewById(R.id.studentNumber);
		isCanobie = (CheckBox) findViewById(R.id.isCanobie);	
		rides = (Spinner) findViewById(R.id.rides);
		
	
	}

}
