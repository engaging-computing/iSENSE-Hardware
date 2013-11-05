package edu.uml.cs.isense.credentials;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * Allows the user to identify his/her data sets with their chosen name.  This can allow for
 * students to use a teacher's iSENSE Account, because each student will have a unique identity.
 * 
 * @author jpoulin
 */
public class EnterName extends Activity {
	
	/* Use these to access the name and last initial from preferences. */
	public static final String PREFERENCES_KEY_USER_INFO = "USER_INFO";
	public static final String PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME = "FIRST_NAME";
	public static final String PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL = "LAST_INITIAL";

	private Waffle w;
	private Context baseContext;

	private static final String blankFields = "Do not leave any fields blank.  Please enter your first name and last initial.";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_name);

		baseContext = getBaseContext();
		w = new Waffle(baseContext);
		API api = API.getInstance(baseContext);
		
		final String[] isenseAccountName = api.getCurrentUser().name.split("\\s+");

		SharedPreferences mPrefs = baseContext.getSharedPreferences(PREFERENCES_KEY_USER_INFO, MODE_PRIVATE);
		String defaultFirstName = mPrefs.getString(PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME, isenseAccountName[0]);
		String defaultLastInitial = mPrefs.getString(PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL, isenseAccountName[1]);		

		final EditText firstNameInput = (EditText) findViewById(R.id.edittext_name);
		final EditText lastInitialInput = (EditText) findViewById(R.id.edittext_initial);
		final Button okButton = (Button) findViewById(R.id.button_ok);
		final CheckedTextView useIsenseName = (CheckedTextView) findViewById(R.id.checkedtextview_use_isense_name);
		
		/* Initialize the EditText Boxes */
		firstNameInput.setText(defaultFirstName);
		lastInitialInput.setText(defaultLastInitial);
		
		/*
		 * If the user checks the box, reset the text boxes back to their account information and disable the text boxes.
		 * Otherwise allow them to edit the first name and last initial boxes. 
		 */
		useIsenseName.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (useIsenseName.isChecked()) {
					firstNameInput.setEnabled(false);
					lastInitialInput.setEnabled(false);
					firstNameInput.setText(isenseAccountName[0]);
					lastInitialInput.setText(isenseAccountName[1]);
				} else {
					firstNameInput.setEnabled(true);
					lastInitialInput.setEnabled(true);
				}
			}
		});
		

		/*
		 * Write the user's information into memory.
		 */
		okButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (firstNameInput.length() == 0
						|| lastInitialInput.length() == 0) {
					showFailure();
				} else {
					SharedPreferences mPrefs = baseContext.getSharedPreferences(PREFERENCES_KEY_USER_INFO, MODE_PRIVATE);
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
