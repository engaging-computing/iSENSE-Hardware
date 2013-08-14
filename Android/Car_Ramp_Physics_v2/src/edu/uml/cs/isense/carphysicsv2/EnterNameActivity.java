package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.waffle.Waffle;

public class EnterNameActivity extends Activity {

	private Context mContext;

	static final public int NAME_SUCCESSFUL = 1;
	static final public int NAME_FAILED = 0;
	static final public int NAME_CANCELED = -1;

	static boolean dontToastMeTwice = false;
	boolean success;

	Waffle w;

	private static final String blankFields = "Do not leave any fields blank.  Please enter your first name and last initial.";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entername);

		mContext = this;

		w = new Waffle(mContext);
		
		SharedPreferences mPrefs = getSharedPreferences("NAME", 0);
		String defaultFirstName = mPrefs.getString("first_name", "");
		String defaultLastInitial = mPrefs.getString("last_initial", "");

		final EditText firstNameInput = (EditText) findViewById(R.id.nameInput);
		final EditText lastInitialInput = (EditText) findViewById(R.id.initialInput);
		final Button okButton = (Button) findViewById(R.id.OkButton);
		
		firstNameInput.setText(defaultFirstName);
		lastInitialInput.setText(defaultLastInitial);
		
		InputFilter[] filters = new InputFilter[2];
		filters[0] = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				if (end > start) {

					char[] acceptedChars = new char[] { 'a', 'b', 'c', 'd',
							'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
							'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
							'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
							'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
							'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1',
							'2', '3', '4', '5', '6', '7', '8', '9', '@', '.',
							'_', '-', '(', ')', ',' };

					for (int index = start; index < end; index++) {
						if (!new String(acceptedChars).contains(String
								.valueOf(source.charAt(index)))) {
							return "";
						}
					}
				}
				return null;
			}

		};
		filters[1] = new InputFilter.LengthFilter(20);
		firstNameInput.setFilters(filters);
		lastInitialInput.setFilters(new InputFilter[] { filters[0],
				new InputFilter.LengthFilter(1) });

		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (firstNameInput.length() == 0
						|| lastInitialInput.length() == 0) {
					showFailure();
				} else {
					SharedPreferences mPrefs = getSharedPreferences("NAME", 0);
					SharedPreferences.Editor mEdit = mPrefs.edit();
					mEdit.putString("first_name", firstNameInput.getText().toString()).commit();
					mEdit.putString("last_initial", lastInitialInput.getText().toString()).commit();
					
					CarRampPhysicsV2.firstName = firstNameInput.getText()
							.toString();
					CarRampPhysicsV2.lastInitial = lastInitialInput.getText()
							.toString();
					
					setResult(RESULT_OK, null);
					finish();
				}
			}

		});
	}

	@Override
	public void onBackPressed() {
		if (CarRampPhysicsV2.running)
			w.make(

			"Cannot exit via BACK while recording data; use HOME instead.",
					Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
		else if (CarRampPhysicsV2.inApp) {
			CarRampPhysicsV2.setupDone = false;
			setResult(RESULT_CANCELED);
			super.onBackPressed();
		} else {
			super.onBackPressed();
		}
			//w.make("Press back again to exit.", Waffle.LENGTH_SHORT);

	}

	private void showFailure() {
		w.make(blankFields, Waffle.LENGTH_LONG, Waffle.IMAGE_X);

	}

	static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

}

/*
 * final Message msg = Message.obtain(); msg.setTarget(h); msg.what =
 * NAME_FAILED; message = blankFields;
 * 
 * new AlertDialog.Builder(mContext) .setTitle("Error") .setMessage(message)
 * .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 * 
 * @Override public void onClick(DialogInterface dialog, int which) {
 * msg.sendToTarget(); } }) .setOnCancelListener(new OnCancelListener() { public
 * void onCancel(DialogInterface dialog) { msg.sendToTarget(); } }) .show();
 * 
 * }
 * 
 * }
 */

