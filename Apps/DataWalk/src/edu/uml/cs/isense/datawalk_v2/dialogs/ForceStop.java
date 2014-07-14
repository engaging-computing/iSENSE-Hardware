package edu.uml.cs.isense.datawalk_v2.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.uml.cs.isense.datawalk_v2.R;

/**
 * Explains to the user that they left the app while it was recording.
 * 
 * @author Rajia
 */
public class ForceStop extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.force_stop);

		final Button ok = (Button) findViewById(R.id.force_stop_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});

	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}

}