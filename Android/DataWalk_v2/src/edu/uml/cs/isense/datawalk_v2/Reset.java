package edu.uml.cs.isense.datawalk_v2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Determines whether or not to restore default settings.
 * @author Rajia
 */
public class Reset extends Activity {
	private Button resetYes, resetNo;

	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		this.setContentView(R.layout.reset);

		setTitle("Are You Sure?");
		resetYes = (Button) findViewById(R.id.reset);
		resetNo = (Button) findViewById(R.id.noreset);

		resetYes.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// This is what happens when the reset button is clicked...
				SharedPreferences sp = getSharedPreferences("RecordingPrefs", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("DataUploadRate", "10000").commit();

				setResult(RESULT_OK);
				finish();
			}
		});// ends onClickListener for resetButton Yes

		resetNo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// This is what happens when the person says that they no longer
				// want to reset settings
				setResult(RESULT_CANCELED);
				finish();
			}
		});// ends onClickListener for resetButton No

	}// ends onCreate

}
