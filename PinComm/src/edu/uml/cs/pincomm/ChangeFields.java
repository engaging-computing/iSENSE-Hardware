package edu.uml.cs.pincomm;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uml.cs.pincomm.comm.RestAPI;
import edu.uml.cs.pincomm.objects.ExperimentField;

public class ChangeFields extends Activity implements OnClickListener {
	LinearLayout sensorSpinnerLayout;
	LinearLayout fieldLabels;
	static int experimentId;
	ArrayAdapter<String> sensorAdapter;
	String[] sensorArray;
	Button btnOK, btnCancel;

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
			
		} else if ( v == btnCancel ) {
			Intent result = new Intent();
			setResult(RESULT_CANCELED,result);
			finish();
		}
	}	
}

class GetFieldsTask extends AsyncTask<Integer, Void, ArrayList<ExperimentField>> {

	RestAPI rapi;
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
		//int i = 0;
		for(final ExperimentField field : results) {
			//i++;
			
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
			
			//Auto-detect field types and set their spinners accordingly
			if(field.type_id == 7) {
				newSpin.setSelection(1); //auto-detected time
			} else if(field.type_id == 1) {
				newSpin.setSelection(7); //auto-detected temp
			}
		}
	}


}
