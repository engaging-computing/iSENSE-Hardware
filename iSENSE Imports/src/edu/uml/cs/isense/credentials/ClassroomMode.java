package edu.uml.cs.isense.credentials;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import edu.uml.cs.isense.R;

public class ClassroomMode extends Activity {

	public static final String PREFS_KEY_CLASSROOM_MODE = "PREFS_KEY_CLASSROOM_MODE";
	public static final String PREFS_BOOLEAN_CLASSROOM_MODE = "PREFS_BOOLEAN_CLASSROOM_MODE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.classroom_mode);

		final CheckedTextView classModeCheck = (CheckedTextView) findViewById(R.id.classroom_mode_checkbox);
		final Button ok = (Button) findViewById(R.id.classroom_mode_ok);
		
		final SharedPreferences mPrefs = getSharedPreferences(PREFS_KEY_CLASSROOM_MODE, MODE_PRIVATE);
		classModeCheck.setChecked(mPrefs.getBoolean(PREFS_BOOLEAN_CLASSROOM_MODE, true));
				
		final SharedPreferences.Editor mEdit = mPrefs.edit();
		
		classModeCheck.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				classModeCheck.toggle();
				if (classModeCheck.isChecked())
					mEdit.putBoolean(PREFS_BOOLEAN_CLASSROOM_MODE, true).commit();
				else
					mEdit.putBoolean(PREFS_BOOLEAN_CLASSROOM_MODE, false).commit();
			}
		});
		
		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
		
	}
}
