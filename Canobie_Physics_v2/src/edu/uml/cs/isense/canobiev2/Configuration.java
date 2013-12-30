package edu.uml.cs.isense.canobiev2;

import edu.uml.cs.isense.proj.Setup;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Configuration extends Activity {
	public static EditText session; 
	public static EditText project;
	public static CheckBox projectLater;
	public static EditText sampleRate;
	public static EditText studentNumber;
	public static CheckBox isCanobie;
	public static Spinner rides;
	private Button ok;
	private Button selected;
	private TextView rideLabel;
	
	private int BROWSE_PROJECTS_REQUESTED = 101;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);
		
		
		session = (EditText) findViewById(R.id.sessionName);
		projectLater = (CheckBox) findViewById(R.id.select_exp_later);
		sampleRate = (EditText) findViewById(R.id.srate);
		studentNumber = (EditText) findViewById(R.id.studentNumber);
		isCanobie = (CheckBox) findViewById(R.id.isCanobie);	
		rides = (Spinner) findViewById(R.id.rides);
		ok = (Button) findViewById(R.id.ok);
		selected = (Button) findViewById(R.id.selectButton);
		rideLabel = (TextView) findViewById(R.id.rideNameLabel);
		 
		selected.setEnabled(false);
		
		projectLater.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (projectLater.isChecked()) {
					selected.setEnabled(false);
				} else {
					selected.setEnabled(true);
				}
			}
			
		});
		
		isCanobie.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isCanobie.isChecked()) { 
					rides.setEnabled(true);
					rideLabel.setEnabled(true);
				} else {
					rides.setEnabled(false);
					rideLabel.setEnabled(false);

				}
				
			}
			
		});
		
		selected.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getApplicationContext(),
						Setup.class), BROWSE_PROJECTS_REQUESTED);
			}
			
		});
		
		ok.setOnClickListener(new OnClickListener (){
			@Override
			public void onClick(View v) {
//				try{
//					 = Integer.parseInt(sampleRate.getText().toString());
//				} catch(NumberFormatException e) {
//					sampleRate.setError("Please Enter a Value.");
//				}
				
				
				
				if(everythingSelected() == true) {
					finish();
				}
			} 
		});
		
		
	
	}
	
	boolean everythingSelected(){
		boolean selected = true;
		
		if(session.getText().length() == 0) {
			session.setError("Please Enter a Data Set Name.");
			selected = false;
		}
		if(isCanobie.isChecked() == false) {
			//check if project is selected
		}
		
		
		if(sampleRate.getText().length() == 0) {
			sampleRate.setError("Please Enter a Value.");
			selected = false;
		}
		if(studentNumber.getText().length() == 0) {
			studentNumber.setError("Please Enter Seat/Student");
			selected = false;
		}
		
		return selected;
	}
	
	 protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         if (requestCode == BROWSE_PROJECTS_REQUESTED) {
             
         }
     }
 }

