package edu.uml.cs.pincomm;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;

public class ChangeFields extends Activity implements OnClickListener {
	LinearLayout sensorSpinnerLayout;
	LinearLayout fieldLabels;
	static int experimentId;
	ArrayAdapter<String> sensorAdapter;
	String[] sensorArray;
	ArrayList<String> fieldSelections;
	static int fieldIndex = 0;
	Button btnOK, btnCancel;
	boolean fieldsSet = false;
	SharedPreferences prefs2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_fields);

		fieldLabels = (LinearLayout) findViewById(R.id.field_labels);
		sensorSpinnerLayout = (LinearLayout) findViewById(R.id.sensor_spinners);
		btnOK = (Button) findViewById(R.id.fields_ok);
		btnCancel = (Button) findViewById(R.id.fields_cancel);

		//Fills Sensor Spinners with all the PINPoint's sensors
		//includes the names of external sensors the user has selected
		Resources res = getResources();
		SharedPreferences prefs = getSharedPreferences("SENSORS", 0);
		
		sensorArray = res.getStringArray(R.array.pptsensors_array);
		sensorArray[14] = prefs.getString("name_bta1", "BTA 1");
		sensorArray[15] = prefs.getString("name_bta2", "BTA 2");
		sensorArray[16] = prefs.getString("name_mini1", "Minijack 1");
		sensorArray[17] = prefs.getString("name_mini2", "Minijack 2");
		
		sensorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sensorArray);
		
		fieldIndex = 0;
		fieldSelections = new ArrayList<String>();
		
		prefs2 = PreferenceManager.getDefaultSharedPreferences(this);
		fieldsSet = prefs2.getBoolean("fields_set", false);
		
		btnOK.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		
		experimentId = getIntent().getIntExtra("expID",0);
		showFields();
	}

	public void showFields() {
		GetFieldsTask gft = new GetFieldsTask();
		gft.setActivity(this);
		gft.execute(Integer.valueOf(experimentId));
	}

	@Override
	public void onClick(View v) {
		if ( v == btnOK ) {
			Intent result = new Intent();
			setResult(RESULT_OK,result);
			result.putExtra("fields_array", fieldSelections.toArray(new String[fieldSelections.size()]));
			result.putExtra("fields_num", fieldSelections.size());
			finish();
		} else if ( v == btnCancel ) {
			Intent result = new Intent();
			setResult(RESULT_CANCELED,result);
			finish();
		}
	}	
}

class GetFieldsTask extends AsyncTask<Integer, Void, ArrayList<ExperimentField>> {

	edu.uml.cs.isense.comm.RestAPI rapi;
	private ProgressDialog dialog;
	private ChangeFields myAct;

	public void setActivity (ChangeFields newAct) {
		myAct = newAct;
	}

	protected void onPreExecute() {
		dialog = new ProgressDialog(myAct);
		dialog.setMessage("Fetching fields from iSENSE");
		dialog.show();
	}

	@Override
	protected ArrayList<ExperimentField> doInBackground(Integer... params) {

		rapi = RestAPI.getInstance();
		return rapi.getExperimentFields(params[0]);

	}

	protected void onPostExecute(ArrayList<ExperimentField> results) {
		dialog.cancel();
		dialog = null;
		for(final ExperimentField field : results) {
			ChangeFields.fieldIndex++;
			final int currIndex = ChangeFields.fieldIndex;
			
			//Makes sure that the generated TextViews are the same height as the generated spinners
			//So that they match up nicely
			TypedValue value = new TypedValue();
			((Activity) myAct).getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
			DisplayMetrics metrics = new DisplayMetrics();
			myAct.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			int listHeight = TypedValue.complexToDimensionPixelSize(value.data, metrics);
			
			//Generate a TextView for each field
			TextView newLabel = new TextView(myAct);
			newLabel.setText(field.field_name + " (" + field.unit_abbreviation + ")");
			newLabel.setMinHeight(listHeight);
			newLabel.setGravity(Gravity.CENTER_VERTICAL);
			myAct.fieldLabels.addView(newLabel);
			
			//Generate a Spinner with all of the PINPoint sensors
			Spinner newSpin = new Spinner(myAct);
			newSpin.setAdapter(myAct.sensorAdapter);
			myAct.sensorSpinnerLayout.addView(newSpin);
			newSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
			    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			    	myAct.fieldSelections.set(currIndex-1, myAct.sensorArray[pos]);
			    }
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//
				}
			});
						
			//Auto-detect field types and set their spinners accordingly, if this experiment hasn't already had its fields setup
			if(myAct.fieldsSet == false) {
				if(field.type_id == 7) {
					myAct.fieldSelections.add(myAct.sensorArray[1]);
					newSpin.setSelection(1); //auto-detected time
				} else if(field.type_id == 1) {
					myAct.fieldSelections.add(myAct.sensorArray[7]);
					newSpin.setSelection(7); //auto-detected temp
				} else if(field.type_id == 9) {
					myAct.fieldSelections.add(myAct.sensorArray[9]);
					newSpin.setSelection(9); //auto-detected light
				} else if(field.type_id == 27) {
					myAct.fieldSelections.add(myAct.sensorArray[6]);
					newSpin.setSelection(6); //auto-detected pressure
				} else if(field.type_id == 28) {
					myAct.fieldSelections.add(myAct.sensorArray[8]);
					newSpin.setSelection(8); //auto-detected humidity
				} else if(field.type_id == 3) {
					myAct.fieldSelections.add(myAct.sensorArray[5]);
					newSpin.setSelection(5); //auto-detected altitude
				} else {
					myAct.fieldSelections.add(myAct.sensorArray[0]);
				}
			} else {
				myAct.fieldSelections.add(myAct.prefs2.getString("trackedField"+(currIndex-1), ""));
				newSpin.setSelection(Arrays.asList(myAct.sensorArray).indexOf(myAct.prefs2.getString("trackedField"+(currIndex-1), "")));
			}
		}
	}


}
