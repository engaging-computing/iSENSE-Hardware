package edu.uml.cs.pincomm;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.Experiment;

public class ChangeExperiment extends Activity implements OnClickListener {
	Button back, next;
	TextView currPage;
	static LinearLayout expLayout;
	static Context myContext;
	int page = 1;
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_experiment);

		expLayout    = (LinearLayout) findViewById(R.id.exprList);
		back     = (Button)   findViewById(R.id.experiment_back);
		next = (Button)   findViewById(R.id.experiment_forward);
		currPage = (TextView) findViewById(R.id.currPageInd);

		back.setOnClickListener(this);
		next.setOnClickListener(this);
		back.setEnabled(false);

		myContext = this;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		reloadExperiments();
	}
	
	public void reloadExperiments() {
		LoadExperimentListTask lelt = new LoadExperimentListTask();
		lelt.setActivity(this);
		lelt.execute(Integer.valueOf(page));
		if(page == 1) {
			back.setEnabled(false);
		} else {
			back.setEnabled(true);
		}
		currPage.setText("Page "+page);
	}
	
	public void choseExp(int expId) {
		Intent result = new Intent();
		result.putExtra("experimentID", expId);
		
		setResult(RESULT_OK,result);
		finish();
	}

	@Override
	public void onClick(View v) {
		if (v == back) {
			page--;
			reloadExperiments();
		}
		if (v == next) {
			page++;
			reloadExperiments();
		}
	}	
}

class LoadExperimentListTask extends AsyncTask<Integer, Void, ArrayList<Experiment>> {

	RestAPI rapi;
	private ProgressDialog dialog;
	private ChangeExperiment myAct;
	
	public void setActivity (ChangeExperiment newAct) {
		myAct = newAct;
	}

	protected void onPreExecute() {
		ChangeExperiment.expLayout.removeAllViews();
		dialog = new ProgressDialog(ChangeExperiment.myContext);
		dialog.setMessage("Fetching Experiments from iSENSE");
		dialog.show();
	}

	@Override
	protected ArrayList<Experiment> doInBackground(Integer... params) {

		rapi = RestAPI.getInstance();
		return rapi.getExperiments(params[0], 10, "", "recent");

	}

	protected void onPostExecute(ArrayList<Experiment> results) {
		dialog.cancel();
		dialog = null;
		int i = 0;
		Resources res = ChangeExperiment.myContext.getResources();
		for(final Experiment exp : results) {
			i++;
			edu.uml.cs.pincomm.ExperimentRow currRow = new edu.uml.cs.pincomm.ExperimentRow(ChangeExperiment.myContext, null);
			currRow.setName(exp.name);
			currRow.setDesc(exp.description);
			currRow.setLastMod("Last modified "+exp.timemodified);
			if(i%2 != 0) {
				currRow.setBackgroundColor(res.getColor(R.color.rowcols));
			} else {
				currRow.setLayoutBg(res.getColor(R.color.rowcols2));
			}
			if((""+exp.experiment_id).equals(myAct.prefs.getString("isense_expId", "0"))) {
				currRow.setBackgroundColor(res.getColor(R.color.rowcolselected));
			}
			currRow.setClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					myAct.choseExp(exp.experiment_id);
				}
			});
			ChangeExperiment.expLayout.addView(currRow);
		}
	}


}
