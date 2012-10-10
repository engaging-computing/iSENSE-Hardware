package edu.uml.cs.isense.collector.dialogs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.collector.ManualEntry;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.collector.experiment.BrowseExperiments;
import edu.uml.cs.isense.waffle.Waffle;

public class ExperimentDialog extends Activity {

	private static final int BROWSE_REQUESTED  = 100;
	private static final int QR_CODE_REQUESTED = 101;
	private static final int NO_QR_REQUESTED   = 102;
	
	private EditText eid;
	private Waffle w;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.experiment_id);
		
		w = new Waffle(this);
		
		final SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		eid = (EditText) findViewById(R.id.experimentInput);
		eid.setText(mPrefs.getString("experiment_id", ""));
		
		final Button ok = (Button) findViewById(R.id.experiment_ok);
		ok.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (eid.length() == 0) {
					setResult(RESULT_CANCELED);
					finish();
				} else {
					Intent send = new Intent(ExperimentDialog.this, ManualEntry.class);
					send.putExtra("eid", eid.getText().toString());
					SharedPreferences.Editor mEdit = mPrefs.edit();
					mEdit.putString("experiment_id", eid.getText().toString()).commit();
					setResult(RESULT_OK, send);
					finish();
				}
			}	
		});
		
		final Button cancel = (Button) findViewById(R.id.experiment_cancel);
		cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}	
		});
		
		final Button browse = (Button) findViewById(R.id.experiment_browse);
		browse.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent experimentIntent = new Intent(getApplicationContext(),
						BrowseExperiments.class);
				experimentIntent.putExtra(
						"edu.uml.cs.isense.pictures.experiments.propose",
						BROWSE_REQUESTED);

				startActivityForResult(experimentIntent, BROWSE_REQUESTED);
			}	
		});
		
		final Button qr = (Button) findViewById(R.id.experiment_qr);
		qr.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
					Intent intent = new Intent(
							"com.google.zxing.client.android.SCAN");

					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

					intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
					startActivityForResult(intent, QR_CODE_REQUESTED);
				} catch (ActivityNotFoundException e) {
					Intent iNoQR = new Intent(ExperimentDialog.this, NoQR.class);
					startActivityForResult(iNoQR, NO_QR_REQUESTED);
				}
			}	
		});
			
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == BROWSE_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				int expId = data.getExtras().getInt(
						"edu.uml.cs.isense.pictures.experiments.exp_id");
				eid.setText("" + expId);

			}
		} else if (requestCode == QR_CODE_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");

				String delimiter = "id=";
				String[] split = contents.split(delimiter);

				try {
					eid.setText(split[1]);
				} catch (ArrayIndexOutOfBoundsException e) {
					w.make("Invalid QR Code!", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
				}

			}
		} else if (requestCode == NO_QR_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String url = "https://play.google.com/store/apps/details?id=com.google.zxing.client.android";
				Intent urlIntent = new Intent(Intent.ACTION_VIEW);
				urlIntent.setData(Uri.parse(url));
				startActivity(urlIntent);
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}
}
