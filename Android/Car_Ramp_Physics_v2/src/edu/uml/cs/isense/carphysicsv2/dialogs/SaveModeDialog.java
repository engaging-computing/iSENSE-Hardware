package edu.uml.cs.isense.carphysicsv2.dialogs;

import edu.uml.cs.isense.carphysicsv2.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SaveModeDialog extends Activity {

	Button ok, cancel;

	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.save_mode_dialog);

		ok = (Button) findViewById(R.id.saveModeButton);
		cancel = (Button) findViewById(R.id.tryAgainButton);

		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();

			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();

			}
		});

	}

}
