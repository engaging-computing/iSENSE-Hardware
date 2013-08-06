package edu.uml.cs.isense.collector.dialogs;

import java.util.LinkedList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.collector.DataCollector;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.dfm.SensorCompatibility;
import edu.uml.cs.isense.objects.Experiment;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class Step1Setup extends Activity {

	private static Button cancel, ok, selExp;
	private static CheckBox expCheck, remember;
	private static EditText sesName, sInterval, testLen;
	private static TextView expLabel;

	private static Context mContext;
	private static Waffle w;

	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mEdit;

	private static final int SETUP_REQUESTED = 100;
	private static final int CHOOSE_SENSORS_REQUESTED = 101;
	
	public static final int S_INTERVAL = 50;
	public static final int TEST_LENGTH = 600;
	public static final int MAX_DATA_POINTS = (1000/S_INTERVAL) *TEST_LENGTH;
	
	public static RestAPI rapi;
	public static LinkedList<String> acceptedFields;
	public static DataFieldManager dfm;
	public static Fields f;
	public static SensorCompatibility sc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.step1setup);
		
		if (android.os.Build.VERSION.SDK_INT < 11)
			getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		mContext = this;
		w = new Waffle(this);
		rapi = RestAPI.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		f = new Fields();
		mPrefs = getSharedPreferences("EID", 0);
		mEdit = mPrefs.edit();
		
		cancel = (Button) findViewById(R.id.step1_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		ok = (Button) findViewById(R.id.step1_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				boolean ready = true;
				
				if (sesName.getText().toString().equals("")) {
					sesName.setError("Enter a session name");
					w.make("Please enter a session name first", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
					ready = false;
				} else {
					sesName.setError(null);
				}
				long sint;
				if (sInterval.getText().toString().equals("")) {
					sint = S_INTERVAL;
				} else {
					sint = Integer.parseInt(sInterval.getText().toString());
				}
				if (sint < S_INTERVAL) {
					sInterval.setError("Enter a sample interval >= " + S_INTERVAL + " ms");
					w.make("Please enter a valid sample interval", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
					ready = false;
				} else {
					sInterval.setError(null);
				}
				long tlen;
				if (testLen.getText().toString().equals("")) {
					tlen = TEST_LENGTH;
				} else {
					tlen = Integer.parseInt(testLen.getText().toString());
				}
				if (tlen * (1000/sint) > MAX_DATA_POINTS) {
					testLen.setError("Enter a test length <= " + (long)MAX_DATA_POINTS/(1000/sint) + " s");
					w.make("Please enter a valid test length", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
					ready = false;
				} else {
					testLen.setError(null);
				}
				if (!expCheck.isChecked()) {
					String eid = mPrefs.getString("experiment_id", "");
					String fields = mPrefs.getString("accepted_fields", "");
					if (eid.equals("") || eid.equals("-1")) {
						w.make("Please select an experiment", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
						ready = false;
					} else if (fields.equals("")) {
						w.make("Please re-select your experiment and fields", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
						ready = false;
					}
				}
				if (ready) {
					if (expCheck.isChecked()) mEdit.putString("experiment_id", "-1").commit();
					mEdit.putString("session_name", sesName.getText().toString()).commit();
					
					if (remember.isChecked()) {
						mEdit.putString("s_interval", sInterval.getText().toString()).commit();
						mEdit.putString("t_length", testLen.getText().toString()).commit();
						mEdit.putBoolean("remember", true).commit();
					} else {
						mEdit.putBoolean("remember", false).commit();
					}
					
					Intent iRet = new Intent();
					iRet.putExtra(DataCollector.STEP_1_DATASET_NAME, sesName.getText().toString());
					iRet.putExtra(DataCollector.STEP_1_SAMPLE_INTERVAL, sint);
					iRet.putExtra(DataCollector.STEP_1_TEST_LENGTH, tlen);
					setResult(RESULT_OK, iRet);
					finish();	
				}
			}
		});

		selExp = (Button) findViewById(R.id.step1_select_exp);
		selExp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!rapi.isConnectedToInternet())
					w.make("No internet connectivity found - searching only cached experiments", Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
				Intent iSetup = new Intent(mContext, Setup.class);
				iSetup.putExtra("enable_no_exp_button", false);
				startActivityForResult(iSetup, SETUP_REQUESTED);
			}
		});

		expCheck = (CheckBox) findViewById(R.id.step1_checkbox);
		expCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (expCheck.isChecked()) {
					selExp.setEnabled(false);
					expLabel.setText("Experiment");
				} else {
					selExp.setEnabled(true);
					String exp = mPrefs.getString("experiment_id", "");
					if (!(exp.equals("") || exp.equals("-1"))) {
						expLabel.setText("Experiment (currently " + exp + ")");
					} else {
						expLabel.setText("Experiment");
					}
				}
			}
		});

		sesName = (EditText) findViewById(R.id.step1_session_name);
		sesName.setText(mPrefs.getString("session_name", ""));
		
		sInterval = (EditText) findViewById(R.id.step1_sample_interval);
		testLen = (EditText) findViewById(R.id.step1_test_length);

		expLabel = (TextView) findViewById(R.id.step1_exp_num_label);
		String exp = mPrefs.getString("experiment_id", "");
		if (!(exp.equals("") || exp.equals("-1"))) {
			expLabel.setText("Experiment (currently " + exp + ")");
			dfm = new DataFieldManager(Integer.parseInt(exp), rapi,
					mContext, f);
		} else {
			dfm = new DataFieldManager(-1, rapi, mContext, f);
			if (exp.equals("-1"))
				expCheck.toggle();
		}
		
		remember = (CheckBox) findViewById(R.id.step1_remember);
		remember.setChecked(mPrefs.getBoolean("remember", false));
		if (remember.isChecked()) {
			sInterval.setText(mPrefs.getString("s_interval", ""));
			testLen.setText(mPrefs.getString("t_length", ""));
		}
		
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SETUP_REQUESTED) {
			if (resultCode == RESULT_OK) {

				String exp = mPrefs.getString("experiment_id", "");
				if (!(exp.equals("") || exp.equals("-1"))) {
					expLabel.setText("Experiment (currently " + exp + ")");
				} else {
					expLabel.setText("Experiment");
				}
				new SensorCheckTask().execute();

			}
		} else if (requestCode == CHOOSE_SENSORS_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (ChooseSensorDialog.acceptedFields.isEmpty()) {
					Intent iSetup = new Intent(mContext, Setup.class);
					iSetup.putExtra("enable_no_exp_button", false);
					startActivityForResult(iSetup, SETUP_REQUESTED);
				} else if (!ChooseSensorDialog.compatible) {
					Intent iSetup = new Intent(mContext, Setup.class);
					iSetup.putExtra("enable_no_exp_button", true);
					startActivityForResult(iSetup, SETUP_REQUESTED);
				} else {
					acceptedFields = ChooseSensorDialog.acceptedFields;
					getEnabledFields();
				}
			}

		}
	}
	
	private void getEnabledFields() {

		try {
			for (String s : acceptedFields) { if (s.length() != 0) break; }
		} catch (NullPointerException e) {
			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String fields = mPrefs.getString("accepted_fields", "");
			getFieldsFromPrefsString(fields);
		}
		
		for (String s : acceptedFields) {
			if (s.equals(getString(R.string.time)))
				dfm.enabledFields[Fields.TIME] = true;

			if (s.equals(getString(R.string.accel_x)))
				dfm.enabledFields[Fields.ACCEL_X] = true;

			if (s.equals(getString(R.string.accel_y)))
				dfm.enabledFields[Fields.ACCEL_Y] = true;

			if (s.equals(getString(R.string.accel_z)))
				dfm.enabledFields[Fields.ACCEL_Z] = true;

			if (s.equals(getString(R.string.accel_total)))
				dfm.enabledFields[Fields.ACCEL_TOTAL] = true;

			if (s.equals(getString(R.string.latitude)))
				dfm.enabledFields[Fields.LATITUDE] = true;

			if (s.equals(getString(R.string.longitude)))
				dfm.enabledFields[Fields.LONGITUDE] = true;

			if (s.equals(getString(R.string.magnetic_x)))
				dfm.enabledFields[Fields.MAG_X] = true;

			if (s.equals(getString(R.string.magnetic_y)))
				dfm.enabledFields[Fields.MAG_Y] = true;

			if (s.equals(getString(R.string.magnetic_z)))
				dfm.enabledFields[Fields.MAG_Z] = true;

			if (s.equals(getString(R.string.magnetic_total)))
				dfm.enabledFields[Fields.MAG_TOTAL] = true;

			if (s.equals(getString(R.string.heading_deg)))
				dfm.enabledFields[Fields.HEADING_DEG] = true;

			if (s.equals(getString(R.string.heading_rad)))
				dfm.enabledFields[Fields.HEADING_RAD] = true;

			if (s.equals(getString(R.string.temperature_c)))
				dfm.enabledFields[Fields.TEMPERATURE_C] = true;

			if (s.equals(getString(R.string.temperature_f)))
				dfm.enabledFields[Fields.TEMPERATURE_F] = true;

			if (s.equals(getString(R.string.temperature_k)))
				dfm.enabledFields[Fields.TEMPERATURE_K] = true;

			if (s.equals(getString(R.string.pressure)))
				dfm.enabledFields[Fields.PRESSURE] = true;

			if (s.equals(getString(R.string.altitude)))
				dfm.enabledFields[Fields.ALTITUDE] = true;

			if (s.equals(getString(R.string.luminous_flux)))
				dfm.enabledFields[Fields.LIGHT] = true;

		}
	}
	
	private void getFieldsFromPrefsString(String fieldList) {

		String[] fields = fieldList.split(",");
		acceptedFields = new LinkedList<String>();

		for (String f : fields) {
			acceptedFields.add(f);
		}
	}

	// Task for checking sensor availability along with enabling/disabling
	private class SensorCheckTask extends AsyncTask<Void, Integer, Void> {

		ProgressDialog dia;
		
		@Override
		protected void onPreExecute() {
			OrientationManager.disableRotation(Step1Setup.this);

			dia = new ProgressDialog(Step1Setup.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Gathering experiment fields...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {

			SharedPreferences mPrefs = getSharedPreferences("EID", 0);
			String experimentInput = mPrefs.getString("experiment_id", "");

			dfm = new DataFieldManager(Integer.parseInt(experimentInput), rapi,
					mContext, f);
			dfm.getOrder();
			
			sc = dfm.checkCompatibility();

			publishProgress(100);
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			dia.setMessage("Done");
			dia.cancel();

			OrientationManager.enableRotation(Step1Setup.this);

			chooseSensorIntent();
		}
	}
	
	private void chooseSensorIntent() {
		SharedPreferences mPrefs = getSharedPreferences("EID", 0);
		String expNum = mPrefs.getString("experiment_id", "");
		Intent i = new Intent(mContext, ChooseSensorDialog.class);
		Experiment e = rapi.getExperiment(Integer.parseInt(expNum));
		i.putExtra("expnum", expNum);
		if (e != null)
			i.putExtra("expname", e.name);
		else
			i.putExtra("expname", "");
		startActivityForResult(i, CHOOSE_SENSORS_REQUESTED);	
	}

}