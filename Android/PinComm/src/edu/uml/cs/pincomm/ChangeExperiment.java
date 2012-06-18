package edu.uml.cs.pincomm;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import edu.uml.cs.pincomm.comm.RestAPI;
import edu.uml.cs.pincomm.objects.Experiment;

public class ChangeExperiment extends Activity implements OnClickListener {
	Button ok, cancel;
	static LinearLayout expLayout;
	static Context myContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_experiment);
		
		expLayout    = (LinearLayout) findViewById(R.id.exprList);
		
		ok     = (Button)   findViewById(R.id.experiment_ok);
		cancel = (Button)   findViewById(R.id.experiment_cancel);
				
		ok.setOnClickListener(this);
		cancel.setOnClickListener(this);
		
		myContext = this;
		
		new LoadExperimentListTask().execute();
	}

	@Override
	public void onClick(View v) {
		if (v == ok) {
			
		}
		
		finish();
	}	
}

class LoadExperimentListTask extends AsyncTask<Void, Void, ArrayList<Experiment>> {

	RestAPI rapi;
	private ProgressDialog dialog;
	
	protected void onPreExecute() {
		dialog = new ProgressDialog(ChangeExperiment.myContext);
        dialog.setMessage("Fetching Experiments from iSENSE");
        dialog.show();
	}
	
	@Override
	protected ArrayList<Experiment> doInBackground(Void... params) {
		
		rapi = RestAPI.getInstance();
		return rapi.getExperiments(0, 10, "browse", "");

	}
	
	protected void onPostExecute(ArrayList<Experiment> results) {
		dialog.cancel();
		for(Experiment exp : results) {
			edu.uml.cs.pincomm.ExperimentRow currRow = new edu.uml.cs.pincomm.ExperimentRow(ChangeExperiment.myContext, null);
			currRow.setName(exp.name);
			currRow.setDesc(exp.description);
			ChangeExperiment.expLayout.addView(currRow);
		}
    }

	
}
