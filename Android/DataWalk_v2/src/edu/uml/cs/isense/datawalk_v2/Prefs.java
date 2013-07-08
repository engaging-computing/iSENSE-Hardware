package edu.uml.cs.isense.datawalk_v2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.exp.Setup;

public class Prefs extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private CheckBoxPreference uploadModeBox;
	private Preference exp_num;
	private Preference name_change;

	public static final int EXPERIMENT_REQUESTED = 9000;
	public static final int EXPERIMENT_NAME_REQUESTED = 5004;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		uploadModeBox = (CheckBoxPreference) getPreferenceScreen()
				.findPreference("UploadMode");
		if (uploadModeBox.isChecked()) {
			uploadModeBox.setSummary(getResources().getString(
					R.string.Upload_Summary_Auto));
		} else {
			uploadModeBox.setSummary(getResources().getString(
					R.string.Upload_Summary_Save));
		}
		exp_num = getPreferenceScreen().findPreference("Exp Num");
		exp_num.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivityForResult(new Intent(DataWalk.mContext,
						Setup.class), EXPERIMENT_REQUESTED);

				return false;
			}

		});

		name_change = getPreferenceScreen().findPreference("Name Change");
		name_change
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						startActivityForResult(new Intent(DataWalk.mContext,
								LoginActivity.class), EXPERIMENT_NAME_REQUESTED);

						return false;
					}

				});

	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("UploadMode")) {
			if (uploadModeBox.isChecked()) {
				uploadModeBox.setSummary(getResources().getString(
						R.string.Upload_Summary_Auto));
			} else {
				uploadModeBox.setSummary(getResources().getString(
						R.string.Upload_Summary_Save));
			}
		}
	}

	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		if (reqCode == EXPERIMENT_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences prefs = getSharedPreferences("EID", 0);
				DataWalk.experimentId = prefs.getString("experiment_id", null);
				if (DataWalk.experimentId == null) {
					DataWalk.experimentId = DataWalk.defaultExp;
				}
			}
		}

	}
}// ends class

