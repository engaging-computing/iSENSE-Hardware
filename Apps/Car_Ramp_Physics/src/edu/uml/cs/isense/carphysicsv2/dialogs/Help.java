package edu.uml.cs.isense.carphysicsv2.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import edu.uml.cs.isense.carphysicsv2.R;

public class Help extends Activity {

	private Button okButton;

	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.help);

		okButton = (Button) findViewById(R.id.okButton);

		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
