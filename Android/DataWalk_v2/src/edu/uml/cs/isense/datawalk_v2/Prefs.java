package edu.uml.cs.isense.datawalk_v2;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import edu.uml.cs.isense.datawalk_v2.R;
import edu.uml.cs.isense.waffle.Waffle;


public class Prefs extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public static CheckBoxPreference uploadModeBox;
	Waffle w;


	public static final int EXPERIMENT_REQUESTED = 9000;
	public static final int EXPERIMENT_NAME_REQUESTED = 5004;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("tag", "we are in prefs class");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		uploadModeBox = (CheckBoxPreference) getPreferenceScreen().findPreference("UploadMode");
		
		this.setTitle("Collection Mode");
		if(DataWalk.umbChecked==true){
			      uploadModeBox.setChecked(true);
			      uploadModeBox.setEnabled(false);
		}else{
			      uploadModeBox.setChecked(false);
			      uploadModeBox.setEnabled(false);
		}if (!uploadModeBox.isChecked()){
			Log.d("tag", "Rajia u just decided to turn save mode on(:");
		}
		
		//Create a boolean saveModeBox we cannot change 
		
	}//ends onCreate

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("UploadMode")) {
			if (uploadModeBox.isChecked()) {
				//uploadModeBox.setSummary(getResources().getString(R.string.Upload_Summary_Auto));
			} else {
				//uploadModeBox.setSummary(getResources().getString(R.string.Upload_Summary_Auto));
			}
		}
	}

	
}// ends class

