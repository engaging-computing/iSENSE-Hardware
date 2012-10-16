package edu.uml.cs.isense.collector.shared;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.collector.objects.DataSet;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;
import edu.uml.cs.isense.supplements.OrientationManager;

public class QueueEditData extends Activity {

	private Button okay, cancel;
	private LinearLayout editDataList;
	
	public static DataSet alter;
	private RestAPI rapi;
	
	private Context mContext;
	private ArrayList<ExperimentField> fieldOrder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.queueedit_data);
		super.onCreate(savedInstanceState);

		alter = QueueUploader.lastDataSetLongClicked;
		
		mContext = this;
		
		fieldOrder = new ArrayList<ExperimentField>();

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());

		okay = (Button) findViewById(R.id.queueedit_data_okay);
		cancel = (Button) findViewById(R.id.queueedit_data_cancel);

		okay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getNewFields();
				setResult(RESULT_OK);
				finish();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		editDataList = (LinearLayout) findViewById(R.id.queueedit_data_layout);

		new LoadExperimentFieldsTask().execute();

	}

	private void fillScrollView() {
		
		int i = 0;
		String rawFieldData = alter.getData().replace("[", "").replace("]", "").replace("\"", "");
		String[] fieldData = rawFieldData.split(",");
		
		for (ExperimentField ef : fieldOrder) {
			
			Log.e("tag", "i     = " + i);
			Log.e("tag", "label = " + ef.field_name);
			Log.e("tag", "data  = " + fieldData[i]);
			
			final View dataRow = View.inflate(mContext, R.layout.edit_row, null);
			dataRow.setBackgroundResource(R.drawable.listelement_bkgd_changer);
			dataRow.setVisibility(View.VISIBLE);
			
			TextView label = (TextView) dataRow.findViewById(R.id.edit_row_label);
			label.setText(ef.field_name);

			EditText data = (EditText) dataRow.findViewById(R.id.edit_row_text);
			data.setText(fieldData[i]);
			
			editDataList.addView(dataRow);
			
			++i;
		}

	}
	
	private void getNewFields() {
		
		int max = editDataList.getChildCount();
		JSONArray data = new JSONArray(),
				  row  = new JSONArray();
		
		for (int i = 0; i < max; i++) {
			
			View v = editDataList.getChildAt(i);
			EditText dataText = (EditText) v.findViewById(R.id.edit_row_text);
			row.put(dataText.getText().toString());
		}
		
		data.put(row);
		
		alter.setData(data.toString());
		
	}

	private class LoadExperimentFieldsTask extends
			AsyncTask<Void, Integer, Void> {
		ProgressDialog dia;
		private boolean error = false;

		@Override
		protected void onPreExecute() {

			OrientationManager.disableRotation(QueueEditData.this);

			dia = new ProgressDialog(QueueEditData.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Loading data fields...");
			dia.setCancelable(false);
			dia.show();

			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			int eid = Integer.parseInt(alter.getEID());
			if (eid != -1) {
				fieldOrder = rapi.getExperimentFields(eid);
			} else {
				Log.e("tag", "CRITICAL ERROR!!!!");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (!error) {
				
				fillScrollView();

				dia.dismiss();
				OrientationManager.enableRotation(QueueEditData.this);
			}

			super.onPostExecute(result);
		}

	}

}