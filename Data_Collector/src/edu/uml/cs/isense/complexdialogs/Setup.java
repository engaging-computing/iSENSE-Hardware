package edu.uml.cs.isense.complexdialogs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.collector.DataCollector;
import edu.uml.cs.isense.collector.Experiments;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.Experiment;
import edu.uml.cs.isense.simpledialogs.NoQR;
import edu.uml.cs.isense.waffle.Waffle;

public class Setup extends Activity implements OnClickListener {

	private EditText sessionName;
	private EditText eidInput;
	private EditText srate;

	private Button okay;
	private Button cancel;
	private Button qrCode;
	private Button browse;

	private Context mContext;
	private Waffle w;
	private RestAPI rapi;

	private SharedPreferences mPrefs;

	private static final int QR_CODE_REQUESTED = 100;
	private static final int EXPERIMENT_CODE = 101;
	private static final int NO_QR_REQUESTED = 102;

	private static final long MIN_SAMPLE_INTERVAL = 200;

	private static boolean hasChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);

		mContext = this;

		w = new Waffle(mContext);

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());

		Bundle extras = getIntent().getExtras();
		final String eid = extras.getString("experiment_id");
		final String sample = extras.getString("srate");

		mPrefs = getSharedPreferences("EID", 0);

		sessionName = (EditText) findViewById(R.id.sessionName);
		sessionName.setText(DataCollector.partialSessionName);

		eidInput = (EditText) findViewById(R.id.ExperimentInput);
		eidInput.setText(eid);

		eidInput.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!(eidInput.isFocused())) {
					if ((!(eidInput.getText().toString().equals(eid)))
							|| hasChanged) {
						hasChanged = true;
						Experiment e = rapi.getExperiment(Integer
								.parseInt(eidInput.getText().toString()));
						if (e != null) {
							if (e.srate < 200) {
								srate.setText("" + MIN_SAMPLE_INTERVAL);
							} else {
								srate.setText("" + e.srate);
							}
						} else {
							srate.setText("" + MIN_SAMPLE_INTERVAL);
						}
					}
				}
			}
		});

		TextView.OnEditorActionListener tval = new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId,
					KeyEvent event) {
				Log.e("blargh", "" + event);
				if (event == null) {
					if ((!(eidInput.getText().toString().equals(eid)))
							|| hasChanged) {
						hasChanged = true;
						try {
							Experiment e = rapi.getExperiment(Integer
									.parseInt(eidInput.getText().toString()));
							if (e != null) {
								if (e.srate < 200) {
									srate.setText("" + MIN_SAMPLE_INTERVAL);
								} else {
									srate.setText("" + e.srate);
								}
							} else {
								srate.setText("" + MIN_SAMPLE_INTERVAL);
							}
						} catch (NumberFormatException nfe) {
							srate.setText("" + MIN_SAMPLE_INTERVAL);
						}

					}
				}
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(eidInput.getWindowToken(), 0);

				return true;
			}
		};
		eidInput.setOnEditorActionListener(tval);

		okay = (Button) findViewById(R.id.setup_ok);
		okay.setOnClickListener(this);

		cancel = (Button) findViewById(R.id.setup_cancel);
		cancel.setOnClickListener(this);

		qrCode = (Button) findViewById(R.id.qrCode);
		qrCode.setOnClickListener(this);

		browse = (Button) findViewById(R.id.BrowseButton);
		browse.setOnClickListener(this);

		srate = (EditText) findViewById(R.id.srate);
		try {
			if (Long.parseLong(sample) < MIN_SAMPLE_INTERVAL)
				srate.setText("" + MIN_SAMPLE_INTERVAL);
			else
				srate.setText(sample);
		} catch (NumberFormatException nfe) {
			srate.setText("" + MIN_SAMPLE_INTERVAL);
		}

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.setup_ok:
			boolean pass = true;

			if (sessionName.getText().length() == 0) {
				sessionName.setError("Enter a Name");
				pass = false;
			}
			if (eidInput.getText().length() == 0) {
				eidInput.setError("Enter an Experiment");
				pass = false;
			}
			if (srate.getText().length() == 0) {
				srate.setError("Enter a Sample Interval");
				pass = false;
			} else if (Long.parseLong(srate.getText().toString()) < 200) {
				srate.setError("Interval Must be >= 200");
				pass = false;
			}

			if (pass) {

				Intent i = new Intent(mContext, DataCollector.class);
				i.putExtra("sessionName", sessionName.getText().toString());
				i.putExtra("srate",
						Integer.parseInt(srate.getText().toString()));
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putString("experiment_id",
						eidInput.getText().toString()).commit();

				setResult(RESULT_OK, i);
				finish();
			}

			break;

		case R.id.setup_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;

		case R.id.qrCode:
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

			break;

		case R.id.BrowseButton:

			Intent experimentIntent = new Intent(getApplicationContext(),
					Experiments.class);
			experimentIntent.putExtra(
					"edu.uml.cs.isense.amusement.experiments.propose",
					EXPERIMENT_CODE);

			startActivityForResult(experimentIntent, EXPERIMENT_CODE);

			break;
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
					Experiment e = rapi.getExperiment(Integer
							.parseInt(split[1]));
					try {
						if (e.srate < MIN_SAMPLE_INTERVAL)
							srate.setText("" + MIN_SAMPLE_INTERVAL);
						else
							srate.setText("" + e.srate);
					} catch (NumberFormatException nfe) {
						srate.setText("" + MIN_SAMPLE_INTERVAL);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					w.make("Invalid QR Code!", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
				}

			}
		} else if (requestCode == EXPERIMENT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				int eid = data.getExtras().getInt(
						"edu.uml.cs.isense.pictures.experiments.exp_id");
				eidInput.setText("" + eid);

				try {
					long sr = data.getExtras().getLong(
							"edu.uml.cs.isense.pictures.experiments.srate");
					if (sr < MIN_SAMPLE_INTERVAL)
						srate.setText("" + MIN_SAMPLE_INTERVAL);
					else
						srate.setText("" + sr);
				} catch (NumberFormatException nfe) {
					srate.setText("" + MIN_SAMPLE_INTERVAL);
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
