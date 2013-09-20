package edu.uml.cs.isense.collector.dialogs;

import java.util.LinkedList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.dfm.SensorCompatibility;
import edu.uml.cs.isense.objects.RProject;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class Step1Setup extends Activity {

	private static Button cancel, ok, selProj;
	private static CheckBox expCheck, remember;
	private static EditText dataSetName, sInterval, testLen;
	private static TextView projLabel;

	private static Context mContext;
	private static Waffle w;

	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mEdit;

	private static final int SETUP_REQUESTED = 100;
	private static final int CHOOSE_SENSORS_REQUESTED = 101;

	public static final int S_INTERVAL = 50;
	public static final int TEST_LENGTH = 600;
	public static final int MAX_DATA_POINTS = (1000 / S_INTERVAL) * TEST_LENGTH;

	public static API api;
	public static LinkedList<String> acceptedFields;
	public static DataFieldManager dfm;
	public static Fields f;
	public static SensorCompatibility sc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.step1setup);

		if (android.os.Build.VERSION.SDK_INT < 11)
			getWindow().setLayout(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);

		mContext = this;
		w = new Waffle(this);
		api = API.getInstance(getApplicationContext());
		f = new Fields();
		mPrefs = getSharedPreferences("PROJID", 0);
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
				boolean setUpFields = false;
				
				if (dataSetName.getText().toString().equals("")) {
					dataSetName.setError("Enter a data set name");
					w.make("Please enter a data set name first",
							Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
					ready = false;
				} else {
					dataSetName.setError(null);
				}
				long sint;
				if (sInterval.getText().toString().equals("")) {
					sint = S_INTERVAL;
				} else {
					sint = Integer.parseInt(sInterval.getText().toString());
				}
				if (sint < S_INTERVAL) {
					sInterval.setError("Enter a sample interval >= "
							+ S_INTERVAL + " ms");
					w.make("Please enter a valid sample interval",
							Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
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
				if (tlen * (1000 / sint) > MAX_DATA_POINTS) {
					testLen.setError("Enter a test length <= "
							+ (long) MAX_DATA_POINTS / (1000 / sint) + " s");
					w.make("Please enter a valid test length",
							Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
					ready = false;
				} else {
					testLen.setError(null);
				}
				if (!expCheck.isChecked()) {
					String projID = mPrefs.getString("project_id", "");
					String fields = mPrefs.getString("accepted_fields", "");
					String acceptedProj = mPrefs.getString("accepted_proj", "-1");
					if (projID.equals("") || projID.equals("-1")) {
						w.make("Please select a project", Waffle.LENGTH_SHORT,
								Waffle.IMAGE_X);
						ready = false;
					} else if (fields.equals("") || (!projID.equals(acceptedProj))) {
						w.make("Please select your project fields",
								Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
						ready = false;
						setUpFields = true;
					}
				}
				if (ready) {
					if (expCheck.isChecked())
						mEdit.putString("project_id", "-1").commit();
					mEdit.putString("data_set_name",
							dataSetName.getText().toString()).commit();

					if (remember.isChecked()) {
						mEdit.putString("s_interval",
								sInterval.getText().toString()).commit();
						mEdit.putString("t_length",
								testLen.getText().toString()).commit();
						mEdit.putBoolean("remember", true).commit();
					} else {
						mEdit.putBoolean("remember", false).commit();
					}

					Intent iRet = new Intent();
					iRet.putExtra(DataCollector.STEP_1_DATASET_NAME,
							dataSetName.getText().toString());
					iRet.putExtra(DataCollector.STEP_1_SAMPLE_INTERVAL, sint);
					iRet.putExtra(DataCollector.STEP_1_TEST_LENGTH, tlen);
					setResult(RESULT_OK, iRet);
					finish();
				} else if (setUpFields) {
					new SensorCheckTask().execute();
				}
			}
		});

		selProj = (Button) findViewById(R.id.step1_select_proj);
		selProj.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!api.hasConnectivity())
					w.make("No internet connectivity found - searching only cached projects",
							Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
				Intent iSetup = new Intent(mContext, Setup.class);
				iSetup.putExtra("from_where", "automatic");
				startActivityForResult(iSetup, SETUP_REQUESTED);
			}
		});

		expCheck = (CheckBox) findViewById(R.id.step1_checkbox);
		expCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (expCheck.isChecked()) {
					selProj.setEnabled(false);
					projLabel.setText("Project");
				} else {
					selProj.setEnabled(true);
					String exp = mPrefs.getString("project_id", "");
					if (!(exp.equals("") || exp.equals("-1"))) {
						projLabel.setText("Project (currently " + exp + ")");
					} else {
						projLabel.setText("Project");
					}
				}
			}
		});

		dataSetName = (EditText) findViewById(R.id.step1_data_set_name);
		dataSetName.setText(mPrefs.getString("data_set_name", ""));

		sInterval = (EditText) findViewById(R.id.step1_sample_interval);
		testLen = (EditText) findViewById(R.id.step1_test_length);

		projLabel = (TextView) findViewById(R.id.step1_proj_num_label);
		
		SharedPreferences globalProjPrefs = getSharedPreferences("GLOBAL_PROJ", 0);
		SharedPreferences.Editor gppEdit = globalProjPrefs.edit();
		String proj = globalProjPrefs.getString("project_id_dc", "");
		if (!(proj.equals(""))) {
			projLabel.setText("Project (currently " + proj + ")");
			dfm = new DataFieldManager(Integer.parseInt(proj), api, mContext, f);
			
			// reset the global project id so we don't pull it again
			gppEdit.putString("project_id_dc", "").commit();
			
			// switch the global project id to the local project id
			mEdit.putString("project_id", proj).commit();
		} else {
			proj = mPrefs.getString("project_id", "");
			if (!(proj.equals("") || proj.equals("-1"))) {
				projLabel.setText("Project (currently " + proj + ")");
				dfm = new DataFieldManager(Integer.parseInt(proj), api, mContext, f);
			} else {
				dfm = new DataFieldManager(-1, api, mContext, f);
				expCheck.toggle();
			}
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

				String projID = mPrefs.getString("project_id", "");
				if (!(projID.equals("") || projID.equals("-1"))) {
					projLabel.setText("Project (currently " + projID + ")");
				} else {
					projLabel.setText("Project");
				}
				new SensorCheckTask().execute();

			}
		} else if (requestCode == CHOOSE_SENSORS_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (ChooseSensorDialog.acceptedFields.isEmpty()) {
					Intent iSetup = new Intent(mContext, Setup.class);
					iSetup.putExtra("from_where", "automatic");
					startActivityForResult(iSetup, SETUP_REQUESTED);
				} else if (!ChooseSensorDialog.compatible) {
					Intent iSetup = new Intent(mContext, Setup.class);
					iSetup.putExtra("from_where", "automatic");
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
			for (String s : acceptedFields) {
				if (s.length() != 0)
					break;
			}
		} catch (NullPointerException e) {
			SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
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
			dia.setMessage("Gathering project fields...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {

			SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
			String projectInput = mPrefs.getString("project_id", "");

			Log.d("SensorCheck", "ProjectId = " + projectInput);

			dfm = new DataFieldManager(Integer.parseInt(projectInput), api,
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

			new ChooseSensorTask().execute();
		}
	}

	// Task for checking sensor availability along with enabling/disabling
	private class ChooseSensorTask extends AsyncTask<Void, Integer, Void> {
		RProject p;
		Intent i;
		String projNum;
		@Override
		protected Void doInBackground(Void... voids) {
			SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
			projNum = mPrefs.getString("project_id", "");
			i = new Intent(mContext, ChooseSensorDialog.class);
			p = api.getProject(Integer.parseInt(projNum));
			
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			i.putExtra("expnum", projNum);
			if (p != null)
				i.putExtra("expname", p.name);
			else
				i.putExtra("expname", "");
			startActivityForResult(i, CHOOSE_SENSORS_REQUESTED);
		}
	}

}