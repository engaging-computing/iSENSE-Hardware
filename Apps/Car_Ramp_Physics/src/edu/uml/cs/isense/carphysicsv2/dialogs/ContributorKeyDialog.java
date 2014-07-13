package edu.uml.cs.isense.carphysicsv2.dialogs;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.carphysicsv2.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.waffle.Waffle;

public class ContributorKeyDialog extends Activity {
	
	Button ok, cancel;
	EditText keyField;
	String key;
	Waffle w;
	API api;
	JSONObject data;
	int projID;
	String dataSetName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contrib_key);
		
		ok = (Button) findViewById(R.id.button2);
		cancel = (Button) findViewById(R.id.button1);
		keyField = (EditText) findViewById(R.id.editText1);
		w = new Waffle(this);
		api=  API.getInstance();
		try {
			data = new JSONObject(this.getIntent().getExtras().getString("data"));
			projID = this.getIntent().getExtras().getInt("ID");
			dataSetName = this.getIntent().getExtras().getString("name");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				w.make("Data discarded", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
				finish();
				
			}
		});
		
		ok.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (keyField.getText().length() != 0) {
					key = keyField.getText().toString();
					new UploadViaContributorKeyTask().execute();
				} else if (keyField.getText().length() == 0) {
					keyField.setError("Key can not be empty.");
				}
			}
		});
	}
	
	public class UploadViaContributorKeyTask extends AsyncTask<Void,Void,Void> {
		
		ProgressDialog dia;

		@Override
		protected void onPreExecute() {
			dia = new ProgressDialog(ContributorKeyDialog.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setIndeterminate(true);
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			api.uploadDataSet(projID, data, dataSetName, key, "");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dia.setMessage("Done");
			if (dia != null && dia.isShowing())
				dia.dismiss();
			w.make("Data uploaded!", Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
			finish();
		}
		
	}

}
