package edu.uml.cs.isense.sync;

import edu.uml.cs.isense.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

/**
 * Asks the user to retry when the socket fails to receive a time packet.
 * 
 * @author iSENSE Android Development Team
 */
public class TimeFailed extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_fail);

		getWindow().setLayout(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		final Button retry = (Button) findViewById(R.id.time_fail_retry);
		retry.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});

		final Button cancel = (Button) findViewById(R.id.time_fail_cancel);
		cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_CANCELED);
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