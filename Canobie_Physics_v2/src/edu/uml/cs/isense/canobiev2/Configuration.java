package edu.uml.cs.isense.canobiev2;

import edu.uml.cs.isense.proj.Setup;
import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class Configuration extends Activity {
	private EditText dataset; 
	private EditText sampleRate;
	private EditText studentNumber;
	public static Spinner rides;
	private Button ok;
	private Button select;
	private static CheckBox projectLater;
	private static CheckBox isCanobie;

	
	
	private int BROWSE_PROJECTS_REQUESTED = 101;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);
		
		
		dataset = (EditText) findViewById(R.id.dataName);
		projectLater = (CheckBox) findViewById(R.id.select_exp_later);
		sampleRate = (EditText) findViewById(R.id.srate);
		studentNumber = (EditText) findViewById(R.id.studentNumber);
		isCanobie = (CheckBox) findViewById(R.id.isCanobie);	
		rides = (Spinner) findViewById(R.id.rides);
		ok = (Button) findViewById(R.id.ok);
		select = (Button) findViewById(R.id.selectButton);		 
		
		dataset.setText(AmusementPark.dataName);
		sampleRate.setText(AmusementPark.rate);
		studentNumber.setText(AmusementPark.stNumber);
		
		
		/*Setup Addapter for rides*/
		
		 final ArrayAdapter<CharSequence> canobieAdapter = ArrayAdapter
                 .createFromResource(this, R.array.canobie_array,
                                 android.R.layout.simple_spinner_item);
		 canobieAdapter
                 .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 
		 final ArrayAdapter<CharSequence> generalAdapter = ArrayAdapter
                 .createFromResource(this, R.array.rides_array,
                                 android.R.layout.simple_spinner_item);
		  generalAdapter
		  		 .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		  
		  /*Set up checkboxes*/
		  projectLater.setChecked(AmusementPark.projectLaterChecked);
		  isCanobie.setChecked(AmusementPark.canobieChecked);

			if (projectLater.isChecked()) {
				select.setEnabled(false);
			} else {
				select.setEnabled(true);
			}

		 if (isCanobie.isChecked())
             rides.setAdapter(canobieAdapter);
		 else
             rides.setAdapter(generalAdapter);
		
		
		/*Checkbox on checked change listeners*/
		projectLater.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				AmusementPark.projectLaterChecked = projectLater.isChecked();
				if (projectLater.isChecked()) {
					select.setEnabled(false);
				} else {
					select.setEnabled(true);
				}
			}
			
		});
		
		isCanobie.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				AmusementPark.canobieChecked = isCanobie.isChecked();
				if (isCanobie.isChecked()) { 
					rides.setAdapter(canobieAdapter);
				} else {
					rides.setAdapter(generalAdapter);
				}
				
			}
			
		});
		
		/*select a project*/		
		select.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), Setup.class);
				intent.putExtra("constrictFields", true);
				intent.putExtra("app_name", "Canobie");
				intent.putExtra("showOKCancel", true);
				startActivityForResult(intent, BROWSE_PROJECTS_REQUESTED);
			}
			
		});
		
		
		ok.setOnClickListener(new OnClickListener (){
			@Override
			public void onClick(View v) {

				if(everythingSelected() == true) {
					AmusementPark.setupDone = true;
					finish();
				} else {
					return;
				}
			} 
		});
	}
	
	/*Check that all values have been filled in and set values in AmusementPark*/
	boolean everythingSelected(){
		boolean selected = true;
		
		if(dataset.getText().length() == 0) {
			dataset.setError("Please Enter a Data Set Name.");
			selected = false;
		} else {
			AmusementPark.dataName = dataset.getText().toString();
		}
		
		
		if(projectLater.isChecked() == false) {
			//check if project is selected
			if (AmusementPark.projectNum == -1) {
				selected = false;
				select.setError("Please Select a Project.");
			} 			
		} else {
			AmusementPark.projectNum = -1; 
		}
		
		
		if(sampleRate.getText().length() == 0) {
			sampleRate.setError("Please Enter a Value.");
			selected = false;
		} else {
			AmusementPark.rate = sampleRate.getText().toString();		
			}
		
		if(studentNumber.getText().length() == 0) {
			studentNumber.setError("Please Enter Seat/Student");
			selected = false;
		} else {
			AmusementPark.stNumber = studentNumber.getText().toString();
		}
		
		AmusementPark.rideNameString = rides.getSelectedItem().toString();

		
		return selected;
	}
	
	 protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         if (requestCode == BROWSE_PROJECTS_REQUESTED) {
        	 SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
				String eidString = mPrefs.getString("project_id", "");
				
				AmusementPark.projectNum = Integer.parseInt(eidString);		
         }
     }
 }

