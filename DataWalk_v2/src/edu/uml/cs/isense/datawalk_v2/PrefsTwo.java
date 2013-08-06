package edu.uml.cs.isense.datawalk_v2;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import edu.uml.cs.isense.datawalk_v2.R;

public class PrefsTwo extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	
	public static Preference recRateOptions;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d("tag", "We are in the PrefsTwo class where arrays are displayed");
		addPreferencesFromResource(R.xml.preferences2);
		recRateOptions = (Preference) getPreferenceScreen().findPreference("Data UploadRate");
		this.setTitle("Recording Interval");
	}//ends onCreate
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("Data UploadRate")) {
			Log.d("tag", "Rajia you are STILL IN PREFSTWO CLASS");
			setResult(RESULT_CANCELED);
			finish();
		}
		
	}
	
	
	
}//ends PrefsTwo class
