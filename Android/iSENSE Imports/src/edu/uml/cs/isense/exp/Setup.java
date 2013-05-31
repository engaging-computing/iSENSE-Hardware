package edu.uml.cs.isense.exp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.waffle.Waffle;

public class Setup extends Activity implements OnClickListener {

	private EditText eidInput;

	private Button okay;
	private Button cancel;
	private Button qrCode;
	private Button browse;
	private Button noExp;

	private Context mContext;
	private Waffle w;

	private SharedPreferences mPrefs;

	private static final int QR_CODE_REQUESTED = 100;
	private static final int EXPERIMENT_CODE = 101;
	private static final int NO_QR_REQUESTED = 102;
	
	private static String prefsString = "EID";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.experiment_id);

		mContext = this;

		w = new Waffle(mContext);

		okay = (Button) findViewById(R.id.experiment_ok);
		okay.setOnClickListener(this);

		cancel = (Button) findViewById(R.id.experiment_cancel);
		cancel.setOnClickListener(this);

		qrCode = (Button) findViewById(R.id.experiment_qr);
		qrCode.setOnClickListener(this);

		browse = (Button) findViewById(R.id.experiment_browse);
		browse.setOnClickListener(this);
		
		noExp = (Button) findViewById(R.id.experiment_no_exp);
		noExp.setOnClickListener(this);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			boolean enableNoExpButton = extras.getBoolean("enable_no_exp_button");
			if (!enableNoExpButton) {
				noExp.setVisibility(View.GONE);
			} else {
				TextView t = (TextView) findViewById(R.id.experimentText);
				t.setText(getResources().getString(R.string.chooseExperimentAlt));
			}
			String fromWhere = extras.getString("from_where");
			if (fromWhere != null) {
				if (fromWhere.equals("manual")) {
					prefsString = "EID_MANUAL";
				} else if (fromWhere.equals("queue")) {
					prefsString = "EID_QUEUE";
				} else {
					prefsString = "EID";
				}
			} else {
				prefsString = "EID";
			}
			
		} else {
			noExp.setVisibility(View.GONE);
		}
		
		mPrefs = getSharedPreferences(prefsString, 0);
		String eid = mPrefs.getString("experiment_id", "").equals("-1") ? "" : mPrefs.getString("experiment_id", "");
		
		eidInput = (EditText) findViewById(R.id.experimentInput);
		eidInput.setText(eid);

	}

	public void onClick(View v) {

		int id = v.getId();
		if (id == R.id.experiment_ok) {
			boolean pass = true;
			if (eidInput.getText().length() == 0) {
				eidInput.setError("Enter an Experiment");
				pass = false;
			}
			if (pass) {
				
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putString("experiment_id",
						eidInput.getText().toString()).commit();

				setResult(RESULT_OK);
				finish();
			}
		} else if (id == R.id.experiment_cancel) {
			setResult(RESULT_CANCELED);
			finish();
		} else if (id == R.id.experiment_qr) {
			try {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");

				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, QR_CODE_REQUESTED);
			} catch (ActivityNotFoundException e) {
				Intent iNoQR = new Intent(Setup.this, NoQR.class);
				startActivityForResult(iNoQR, NO_QR_REQUESTED);
			}
		} else if (id == R.id.experiment_browse) {
			Intent experimentIntent = new Intent(getApplicationContext(),
					BrowseExperiments.class);
			experimentIntent.putExtra(
					"edu.uml.cs.isense.amusement.experiments.propose",
					EXPERIMENT_CODE);
			startActivityForResult(experimentIntent, EXPERIMENT_CODE);
		} else if (id == R.id.experiment_no_exp) {
			Intent iRet = new Intent();
			iRet.putExtra("no_exp", true);
			setResult(RESULT_OK, iRet);
			finish();
		}

	}

	// Performs tasks after returning to main UI from previous activities
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == QR_CODE_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");

				String delimiter = "id=";
				String[] split = contents.split(delimiter);

				try {
					eidInput.setText(split[1]);
					Integer.parseInt(split[1]);
				} catch (ArrayIndexOutOfBoundsException e) {
					w.make("Invalid QR code scanned", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
				} catch (NumberFormatException nfe) {
					w.make("Invalid QR code scanned", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
				}

			}
		} else if (requestCode == EXPERIMENT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				int eid = data.getExtras().getInt(
						"edu.uml.cs.isense.pictures.experiments.exp_id");
				eidInput.setText("" + eid);

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
