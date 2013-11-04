package edu.uml.cs.isense.credentials;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.waffle.Waffle;

public class EnterNameActivity extends Activity {
	
	public static final String PREFERENCES_KEY_USER_INFO = "USER_INFO";
	public static final String PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME = "FIRST_NAME";
	public static final String PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL = "LAST_INITIAL";

	private Waffle w;

	private static final String blankFields = "Do not leave any fields blank.  Please enter your first name and last initial.";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_name);

		w = new Waffle(this);
		API api = API.getInstance(this);
		
		String[] isenseAccountName = api.getCurrentUser().name.split("\\s+");

		SharedPreferences mPrefs = getSharedPreferences(PREFERENCES_KEY_USER_INFO, MODE_PRIVATE);
		String defaultFirstName = mPrefs.getString(PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME, isenseAccountName[0]);
		String defaultLastInitial = mPrefs.getString(PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL, isenseAccountName[1]);

		final EditText firstNameInput = (EditText) findViewById(R.id.edittext_name);
		final EditText lastInitialInput = (EditText) findViewById(R.id.edittext_initial);
		final Button okButton = (Button) findViewById(R.id.button_ok);
		
		firstNameInput.setText(defaultFirstName);
		lastInitialInput.setText(defaultLastInitial);

		okButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (firstNameInput.length() == 0
						|| lastInitialInput.length() == 0) {
					showFailure();
				} else {
					SharedPreferences mPrefs = getSharedPreferences(PREFERENCES_KEY_USER_INFO, MODE_PRIVATE);
					SharedPreferences.Editor mEdit = mPrefs.edit();
					mEdit.putString(PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME, firstNameInput.getText().toString()).commit();
					mEdit.putString(PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL, lastInitialInput.getText().toString() + ".").commit();
										
					setResult(RESULT_OK, null);
					finish();
				}
			}

		});
	}

	private void showFailure() {
		w.make(blankFields, Waffle.LENGTH_LONG, Waffle.IMAGE_X);
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED, null);
		super.onBackPressed();
	}
	
}
