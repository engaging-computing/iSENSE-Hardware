package edu.uml.cs.pincomm;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.pincomm.comm.RestAPI;
import edu.uml.cs.pincomm.objects.Experiment;
import edu.uml.cs.pincomm.objects.ExperimentField;

public class ChangeFields extends Activity implements OnClickListener {
	TextView fieldList;
	static Context myContext;
	static int experimentId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_fields);

		fieldList = (TextView) findViewById(R.id.fields_display);

		myContext = this;

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
		//
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
		dialog = new ProgressDialog(ChangeFields.myContext);
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
		int i = 0;
		for(final ExperimentField field : results) {
			i++;
			myAct.fieldList.append("Field " + i + ": " + field.field_name + " (" + field.unit_abbreviation + ")\n");
		}
	}


}
