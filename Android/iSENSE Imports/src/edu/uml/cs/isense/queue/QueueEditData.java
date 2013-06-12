package edu.uml.cs.isense.queue;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;
import edu.uml.cs.isense.supplements.OrientationManager;

/**
 * Activity that allows the data-alteration of a data set.  
 * NOTE: Media objects have no "data" and thus cannot be altered.
 * Also, multiple-line data sets will only allow the first line of
 * data to be altered.
 * 
 * @author Mike Stowell and Jeremy Poulin of the iSENSE team.
 *
 */
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

		alter = QueueLayout.lastDataSetLongClicked;
		
		mContext = this;
		
		fieldOrder = new ArrayList<ExperimentField>();

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());

		okay = (Button) findViewById(R.id.queueedit_data_okay);
		cancel = (Button) findViewById(R.id.queueedit_data_cancel);

		okay.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getNewFields();
				setResult(RESULT_OK);
				finish();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
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
		
		// if the data is a space, remove the spaces
		for (int j = 0; j < fieldData.length; j++)
			if (fieldData[j].equalsIgnoreCase(" ")) fieldData[j] = "";

		for (ExperimentField ef : fieldOrder) {
			
			final View dataRow = View.inflate(mContext, R.layout.edit_row, null);
			
			TextView label = (TextView) dataRow.findViewById(R.id.edit_row_label);
			label.setText(ef.field_name);
			label.setBackgroundColor(Color.TRANSPARENT);
			label.setPadding(0, 10, 0, 0);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) label.getLayoutParams();
			params.setMargins(0, 0, 0, -10);
			label.setLayoutParams(params);

			EditText data = (EditText) dataRow.findViewById(R.id.edit_row_text);
			data.setText(fieldData[i]);
			// See if data is a number.  If not, change input type to text
			try {
				Double.parseDouble(fieldData[i]);
			} catch (NumberFormatException nfe) {
				data.setInputType(InputType.TYPE_CLASS_TEXT);
			}
			
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
			if (dataText.getText().toString().length() != 0)
				row.put(dataText.getText().toString());
			else
				row.put(" ");
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