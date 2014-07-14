package edu.uml.cs.isense.canobiev2;

import edu.uml.cs.isense.proj.Setup;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class Configuration extends Activity {
	private static EditText dataset; 
	private static EditText sampleRate;
	private Button ok;
	private Button select;
	private static CheckBox projectLater;
	private static CheckBox isCanobie;
	private static Spinner rides;

	private int BROWSE_PROJECTS_REQUESTED = 101;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);
		
		
		dataset = (EditText) findViewById(R.id.dataName);
		projectLater = (CheckBox) findViewById(R.id.select_exp_later);
		sampleRate = (EditText) findViewById(R.id.srate);
		isCanobie = (CheckBox) findViewById(R.id.isCanobie);	
		rides = (Spinner) findViewById(R.id.rides);
		ok = (Button) findViewById(R.id.ok);
		select = (Button) findViewById(R.id.selectButton);		 
		
		dataset.setText(AmusementPark.dataName);
		sampleRate.setText(AmusementPark.rate);
		
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
		
		rides.setSelection(AmusementPark.spinnerid);
		
		rides.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				AmusementPark.spinnerid = arg2;
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		dataset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dataset.setError(null);
			}
			
		});
		
		sampleRate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sampleRate.setError(null);
			}
			
		});
		
		
		/*Checkbox on checked change listeners*/
		projectLater.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				AmusementPark.projectLaterChecked = projectLater.isChecked();
				if (projectLater.isChecked()) {
					AmusementPark.projectNum = -1; 
					
					select.setError(null);
					
					SharedPreferences mPrefs = getSharedPreferences(Setup.PROJ_PREFS_ID, 0);
					SharedPreferences.Editor mEdit = mPrefs.edit();
					mEdit.putString(Setup.PROJECT_ID, "-1").commit();
					
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
				select.setError(null);
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
			dataset.setError(null);
			AmusementPark.dataName = dataset.getText().toString();
		}
		
		/* check if project later is checked */
		
		if(!projectLater.isChecked()) {
			if (AmusementPark.projectNum == -1) {
				selected = false;
				select.setError("Please Select a Project.");
			} else {
				select.setError(null);
			}
			
		}
		
		
		if(sampleRate.getText().length() == 0) {
			sampleRate.setError("Please Enter a Value.");
			selected = false;
		} else {
			sampleRate.setError(null);
			if (Integer.decode(sampleRate.getText().toString()) < 50) {
				sampleRate.setError("Value must at least 50.");
				selected = false;
			} else {
				sampleRate.setError(null);
				AmusementPark.rate = sampleRate.getText().toString();		
			}
			
		}
		
		AmusementPark.rideNameString = rides.getSelectedItem().toString();
		
		return selected;
	}
	
	 protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         if (requestCode == BROWSE_PROJECTS_REQUESTED) {
        	 
        	SharedPreferences mPrefs = getSharedPreferences(Setup.PROJ_PREFS_ID, 0);
        	String eidString = mPrefs.getString(Setup.PROJECT_ID, "");
			AmusementPark.projectNum = Integer.parseInt(eidString);
			
         }
     }
 }

