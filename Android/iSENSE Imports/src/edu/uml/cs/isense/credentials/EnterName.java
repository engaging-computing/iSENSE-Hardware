package edu.uml.cs.isense.credentials;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * Allows the user to identify his/her data sets with their chosen name. This
 * can allow for students to use a teacher's iSENSE Account, because each
 * student will have a unique identity. The Login activity should be called
 * before this activity.
 * 
 * @author jpoulin
 */
public class EnterName extends Activity {

	/* Use these to access the name and last initial from preferences. */
	public static final String PREFERENCES_KEY_USER_INFO = "USER_INFO";
	public static final String PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME = "FIRST_NAME";
	public static final String PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL = "LAST_INITIAL";
	/*
	 * This is the most important preference. If this flag is set to true, you
	 * should use the current account credentials instead of the saved values.
	 */
	public static final String PREFERENCES_USER_INFO_SUBKEY_USE_ACCOUNT_NAME = "USE_ACCOUNT_NAME";

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

		/* Get current user credentials if it exists. */
		String[] isenseAccountName = null;
		if (api.getCurrentUser() != null) {
			isenseAccountName = api.getCurrentUser().name.split("\\s+");
		}

		/*
		 * Set the default user credentials to the current account credentials
		 * or the default account.
		 */
		final String accountFirstName = (isenseAccountName != null) ? isenseAccountName[0]
				: "Mobile";
		final String accountLastInitial = (isenseAccountName != null) ? isenseAccountName[1]
				: "U.";

		/*
		 * Get the current state of affairs from preferences.
		 */
		SharedPreferences mPrefs = baseContext.getSharedPreferences(
				PREFERENCES_KEY_USER_INFO, MODE_PRIVATE);
		final String defaultFirstName = mPrefs.getString(
				PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME, accountFirstName);
		final String defaultLastInitial = mPrefs.getString(
				PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL, accountLastInitial);
		boolean useAccountName = mPrefs.getBoolean(
				PREFERENCES_USER_INFO_SUBKEY_USE_ACCOUNT_NAME, true);

		final EditText firstNameInput = (EditText) findViewById(R.id.edittext_name);
		final EditText lastInitialInput = (EditText) findViewById(R.id.edittext_initial);
		final Button okButton = (Button) findViewById(R.id.button_ok);
		final CheckBox useIsenseName = (CheckBox) findViewById(R.id.checkbox_use_isense_name);

		if (useAccountName) {
			/* Initialize the EditText To Account Credentials */
			firstNameInput.setText(accountFirstName);
			lastInitialInput.setText(accountLastInitial);
		} else {
			/* Initialize the EditText Boxes to Last Saved Credentials */
			useIsenseName.setChecked(false);
			firstNameInput.setEnabled(true);
			lastInitialInput.setEnabled(true);
			firstNameInput.setText(defaultFirstName);
			lastInitialInput.setText(defaultLastInitial);
		}

		/*
		 * If the user checks the box, reset the text boxes back to their
		 * account information and disable the text boxes. Otherwise allow them
		 * to edit the first name and last initial boxes.
		 */
		useIsenseName.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (useIsenseName.isChecked()) {
					/*
					 * Disable the edittext boxes and set the text back to the
					 * account name.
					 */
					firstNameInput.setEnabled(false);
					lastInitialInput.setEnabled(false);
					firstNameInput.setText(accountFirstName);
					lastInitialInput.setText(accountLastInitial);
				} else {
					/*
					 * Reenabled the edittext boxes and set the text back to the
					 * last saved user credentials.
					 */
					firstNameInput.setEnabled(true);
					lastInitialInput.setEnabled(true);
					firstNameInput.setText(defaultFirstName);
					lastInitialInput.setText(defaultLastInitial);
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
					/*
					 * Prepare to write the current state of affairs back to
					 * preferences.
					 */
					SharedPreferences mPrefs = baseContext
							.getSharedPreferences(PREFERENCES_KEY_USER_INFO,
									MODE_PRIVATE);
					SharedPreferences.Editor mEdit = mPrefs.edit();
					mEdit.putBoolean(
							PREFERENCES_USER_INFO_SUBKEY_USE_ACCOUNT_NAME,
							useIsenseName.isChecked()).commit();

					if (!useIsenseName.isChecked()) {
						/* The user wants you to save the current text into preferences. */
						mEdit.putString(
								PREFERENCES_USER_INFO_SUBKEY_FIRST_NAME,
								firstNameInput.getText().toString()).commit();
						mEdit.putString(
								PREFERENCES_USER_INFO_SUBKEY_LAST_INITIAL,
								lastInitialInput.getText().toString() + ".")
								.commit();
					}
					
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
