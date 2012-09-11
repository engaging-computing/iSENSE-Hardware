package edu.uml.cs.isense.manualentry;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.collector.splash.Splash;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.complexdialogs.ExperimentDialog;
import edu.uml.cs.isense.complexdialogs.LoginActivity;
import edu.uml.cs.isense.objects.ExperimentField;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.waffle.Waffle;

public class ManualEntry extends Activity implements OnClickListener {

	private static final int TYPE_DEFAULT = 1;
	private static final int TYPE_LATITUDE = 2;
	private static final int TYPE_LONGITUDE = 3;
	private static final int TYPE_TIME = 4;

	private static final int LOGIN_REQUESTED = 100;
	private static final int EXPERIMENT_REQUESTED = 101;

	private Waffle w;
	private RestAPI rapi;

	private Button uploadData;
	private Button saveData;
	private Button clearData;

	private static Context mContext;

	private TextView loginLabel;
	private TextView experimentLabel;

	private SharedPreferences loginPrefs;
	private SharedPreferences expPrefs;

	private LinearLayout dataFieldEntryList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manual_entry);

		mContext = this;

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());

		loginPrefs = new ObscuredSharedPreferences(Splash.mContext,
				Splash.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		expPrefs = getSharedPreferences("EID", 0);

		loginLabel = (TextView) findViewById(R.id.loginLabel);
		loginLabel.setText(getResources().getString(R.string.loggedInAs)
				+ loginPrefs.getString("username", ""));

		experimentLabel = (TextView) findViewById(R.id.experimentLabel);
		experimentLabel.setText(getResources().getString(
				R.string.usingExperiment)
				+ expPrefs.getString("experiment_id", ""));

		uploadData = (Button) findViewById(R.id.manual_upload);
		saveData = (Button) findViewById(R.id.manual_save);
		clearData = (Button) findViewById(R.id.manual_clear);
		
		uploadData.setOnClickListener(this);
		saveData.setOnClickListener(this);
		clearData.setOnClickListener(this);

		w = new Waffle(this);

		dataFieldEntryList = (LinearLayout) findViewById(R.id.field_view);

		Intent iGetExpId = new Intent(this, ExperimentDialog.class);
		startActivityForResult(iGetExpId, EXPERIMENT_REQUESTED);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.manual_clear:
			clearFields();
			break;
		case R.id.manual_save:
			saveFields();
			break;
		case R.id.manual_upload:
			uploadFields();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String returnCode = data.getStringExtra("returnCode");

				if (returnCode.equals("Success")) {

					String loginName = loginPrefs.getString("username", "");
					if (loginName.length() >= 18)
						loginName = loginName.substring(0, 18) + "...";
					loginLabel.setText(getResources().getString(
							R.string.loggedInAs)
							+ loginName);
					w.make("Login successful", Waffle.LENGTH_LONG,
							Waffle.IMAGE_CHECK);
				} else if (returnCode.equals("Failed")) {
					Intent i = new Intent(mContext, LoginActivity.class);
					startActivityForResult(i, LOGIN_REQUESTED);
				} else {
					// should never get here
				}
			}
		} else if (requestCode == EXPERIMENT_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String eidString = data.getStringExtra("eid");
				if (eidString != null) {
					int eid = Integer.parseInt(eidString);
					if (eid != -1) {
						experimentLabel.setText(getResources().getString(
								R.string.usingExperiment)
								+ eid);
						fillDataFieldEntryList(eid);
						rapi.getExperimentFields(eid);
					} else {
						w.make("Ballz x2");
					}
				}
			} else {
				w.make("Ballz");
			}
		}
	}

	private void fillDataFieldEntryList(int eid) {
		ArrayList<ExperimentField> fieldOrder = rapi.getExperimentFields(eid);
		for (ExperimentField expField : fieldOrder) {
			if (expField.type_id == expField.GEOSPACIAL) {
				if (expField.unit_id == expField.UNIT_LATITUDE) {
					addDataField(expField, TYPE_LATITUDE);
				} else {
					addDataField(expField, TYPE_LONGITUDE);
				}
			} else if (expField.type_id == expField.TIME) {
				addDataField(expField, TYPE_TIME);
			} else {
				addDataField(expField, TYPE_DEFAULT);
			}
		}
	}

	private void addDataField(ExperimentField expField, int type) {
		LinearLayout dataField = (LinearLayout) View.inflate(this,
				R.layout.manualentryfield, null);
		TextView fieldName = (TextView) dataField
				.findViewById(R.id.manual_dataFieldName);
		fieldName.setText(expField.field_name);
		if (type != TYPE_DEFAULT) {
			EditText fieldContents = (EditText) dataField
					.findViewById(R.id.manual_dataFieldContents);
			fieldContents.setText("Auto");
			fieldContents.setEnabled(false);
		}
		dataFieldEntryList.addView(dataField);
	}

	private void clearFields() {
		for (int i = 0; i < dataFieldEntryList.getChildCount(); i++) {
			EditText dataFieldContents = (EditText) dataFieldEntryList
					.getChildAt(i).findViewById(R.id.manual_dataFieldContents);
			if (dataFieldContents.isEnabled())
				dataFieldContents.setText("");
		}
	}

	private void saveFields() {
		// TODO
	}

	private void uploadFields() {
		// TODO
	}

	// Overridden to prevent user from exiting app unless back button is pressed
	// twice
	@Override
	public void onBackPressed() {

		if (!w.isDisplaying)
			w.make("Double press \"Back\" to exit.", Waffle.LENGTH_SHORT,
					Waffle.IMAGE_CHECK);
		else if (w.canPerformTask)
			super.onBackPressed();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_manual, menu);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_manual_experiment:
			Intent iExperiment = new Intent(mContext, ExperimentDialog.class);
			startActivityForResult(iExperiment, EXPERIMENT_REQUESTED);

			return true;

		case R.id.menu_item_manual_login:
			Intent iLogin = new Intent(mContext, LoginActivity.class);
			startActivityForResult(iLogin, LOGIN_REQUESTED);

			return true;

		}
		return false;

	}

}
