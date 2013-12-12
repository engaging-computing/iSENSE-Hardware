package edu.uml.cs.isense.carphysicsv2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import edu.uml.cs.isense.comm.Connection;

public class DataActivity extends Activity {

	private Button iSENSE_Button;
	private Button discard_Button;

	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.upload_or_trash);

		if (android.os.Build.VERSION.SDK_INT > 11)
			setFinishOnTouchOutside(false);
		
		
		iSENSE_Button = (Button) findViewById(R.id.iSENSE_Button);
		discard_Button = (Button) findViewById(R.id.discard_Button);
		
		if (Connection.hasConnectivity(getApplicationContext()))
			setTitle("Publish Your Data?");
		else {
			setTitle("Save Your Data?");
			iSENSE_Button.setText("Save");
		}

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
