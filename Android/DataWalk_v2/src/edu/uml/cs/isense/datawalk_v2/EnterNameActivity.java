package edu.uml.cs.isense.datawalk_v2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * Allows the user to enter his/her first name and last initial.
 * 
 * @author Rajia
 */
public class EnterNameActivity extends Activity {

	private Waffle w;

	private final String BLANK_FIELDS = "Do not leave any fields blank. Please enter your first name and last initial.";

	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entername);

		// Prepare waffles and our OK button
		w = new Waffle(this);
		Button okButton = (Button) findViewById(R.id.OK);

		// Prepare our text boxes for input
		final EditText firstNameInput = (EditText) findViewById(R.id.nameInput);
		final EditText lastInitialInput = (EditText) findViewById(R.id.initialInput);
		
		// Set the original text in our input boxes
		firstNameInput.setText(DataWalk.firstName);
		lastInitialInput.setText(DataWalk.lastInitial);

		
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// If either field is blank, toast the user
				if (firstNameInput.length() == 0
						|| lastInitialInput.length() == 0) {
					showFailure();
				} else {
					// Update the username and last initial in DataWalk.java then exit
					DataWalk.firstName = firstNameInput.getText().toString();
					DataWalk.lastInitial = lastInitialInput.getText()
							.toString();
					setResult(RESULT_OK, null);
					finish();
				}
			}

		});
	}

	/**
	 * Sets the result to canceled which we can check in onActivityResult in DataWalk.java
	 */
	@Override
	public void onBackPressed() {

		setResult(RESULT_CANCELED);
		super.onBackPressed();

	}// ends onBackPressed

	/**
	 * Called if a user hasn't filled in all of the fields.
	 */
	private void showFailure() {
		w.make(BLANK_FIELDS, Waffle.LENGTH_LONG, Waffle.IMAGE_X);
	}

}
