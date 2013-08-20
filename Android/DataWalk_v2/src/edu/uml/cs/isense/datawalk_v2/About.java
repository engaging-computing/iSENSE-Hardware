package edu.uml.cs.isense.datawalk_v2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import edu.uml.cs.isense.datawalk_v2.R;

/**
 * Displays information explaining the purpose of the iSENSE Data Walk Application.
 * 
 * @author Rajia
 */
public class About extends Activity {

	private Button okButton;

	@Override
	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.about);

		// Creates the OK button so users can leave
		okButton = (Button) findViewById(R.id.loginButton);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

}