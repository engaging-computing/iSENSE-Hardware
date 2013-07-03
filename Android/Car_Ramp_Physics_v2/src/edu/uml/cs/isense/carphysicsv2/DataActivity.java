package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import edu.uml.cs.isense.carphysicsv2.R;

public class DataActivity extends Activity {

	private Button iSENSE_Button;
	private Button discard_Button;

	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.upload_or_trash);
		
		setTitle("Publish Your Data?");

		iSENSE_Button = (Button) findViewById(R.id.iSENSE_Button);
		discard_Button = (Button) findViewById(R.id.discard_Button);

		iSENSE_Button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();

			}
		});

		discard_Button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	
}
